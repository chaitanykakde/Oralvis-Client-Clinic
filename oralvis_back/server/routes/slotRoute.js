const express = require("express");
const Slot = require("../models/slot");
const Clinic = require("../models/clinic");
const router = express.Router();

router.post("/slots", async (req, res) => {
  try {
    const { clinicId, date, startTime: startTimeStr, endTime: endTimeStr, intervalMinutes } = req.body;

    if (!clinicId || !date) {
      return res.status(400).json({ error: "Missing required fields" });
    }

    const clinic = await Clinic.findById(clinicId);
    if (!clinic) {
      return res.status(400).json({ error: "Invalid clinic ID" });
    }

    const startLabel = startTimeStr && /^(\d{2}):(\d{2})$/.test(startTimeStr) ? `${startTimeStr}:00` : "10:00:00";
    const endLabel = endTimeStr && /^(\d{2}):(\d{2})$/.test(endTimeStr) ? `${endTimeStr}:00` : "13:00:00";
    const step = Number.isFinite(Number(intervalMinutes)) && Number(intervalMinutes) > 0 ? Number(intervalMinutes) : 15;

    const startTime = new Date(`${date}T${startLabel}`);
    const endTime = new Date(`${date}T${endLabel}`);
    const slots = [];

    while (startTime < endTime) {
      // store time in 24h HH:mm format for consistent querying
      const hh = String(startTime.getHours()).padStart(2, "0");
      const mm = String(startTime.getMinutes()).padStart(2, "0");
      const timeString = `${hh}:${mm}`;
      slots.push({
        clinic: clinicId,
        date,
        time: timeString,
        isAvailable: true,
      });
      startTime.setMinutes(startTime.getMinutes() + step);
    }

    const createdSlots = await Slot.insertMany(slots);
    res.status(201).json(createdSlots);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

router.post("/slots-month", async (req, res) => {
  try {
    const { clinicId, month, year, startTime: startTimeStr, endTime: endTimeStr, intervalMinutes } = req.body;

    if (!clinicId || !month || !year) {
      return res
        .status(400)
        .json({ error: "Missing required fields (clinicId, month, year)" });
    }

    const clinic = await Clinic.findById(clinicId);
    if (!clinic) {
      return res.status(400).json({ error: "Invalid clinic ID" });
    }

    const totalDays = new Date(year, month, 0).getDate(); // days in the given month
    const allSlots = [];

    for (let day = 1; day <= totalDays; day++) {
      const dateStr = `${year}-${String(month).padStart(2, "0")}-${String(
        day
      ).padStart(2, "0")}`;

      // Optionally skip Sundays
      const weekday = new Date(dateStr).getDay(); // 0 = Sunday
      if (weekday === 0) continue;

      const startLabel = startTimeStr && /^(\d{2}):(\d{2})$/.test(startTimeStr) ? `${startTimeStr}:00` : "10:00:00";
      const endLabel = endTimeStr && /^(\d{2}):(\d{2})$/.test(endTimeStr) ? `${endTimeStr}:00` : "13:00:00";
      const step = Number.isFinite(Number(intervalMinutes)) && Number(intervalMinutes) > 0 ? Number(intervalMinutes) : 15;

      const startTime = new Date(`${dateStr}T${startLabel}`);
      const endTime = new Date(`${dateStr}T${endLabel}`);

      while (startTime < endTime) {
        const hh = String(startTime.getHours()).padStart(2, "0");
        const mm = String(startTime.getMinutes()).padStart(2, "0");
        const timeString = `${hh}:${mm}`;

        allSlots.push({
          clinic: clinicId,
          date: dateStr,
          time: timeString,
          isAvailable: true,
        });

        startTime.setMinutes(startTime.getMinutes() + step);
      }
    }

    const createdSlots = await Slot.insertMany(allSlots);
    res.status(201).json({
      message: "Slots created for entire month",
      count: createdSlots.length,
    });
  } catch (error) {
    console.error("Error creating monthly slots:", error);
    res.status(500).json({ error: error.message });
  }
});

router.post("/slots-many-month", async (req, res) => {
  try {
    const { clinicIds, month, year, startTime: startTimeStr, endTime: endTimeStr, intervalMinutes } = req.body;

    if (
      !clinicIds ||
      !Array.isArray(clinicIds) ||
      clinicIds.length === 0 ||
      !month ||
      !year
    ) {
      return res
        .status(400)
        .json({ error: "Missing required fields (clinicIds[], month, year)" });
    }

    const validClinics = await Clinic.find({ _id: { $in: clinicIds } });
    if (validClinics.length === 0) {
      return res.status(400).json({ error: "No valid clinic IDs provided" });
    }

    const totalDays = new Date(year, month, 0).getDate(); // total days in month
    const allSlots = [];


    for (const clinic of validClinics) {
      for (let day = 1; day <= totalDays; day++) {
        const dateStr = `${year}-${String(month).padStart(2, "0")}-${String(
          day
        ).padStart(2, "0")}`;

        const weekday = new Date(dateStr).getDay();
        if (weekday === 0) continue; // skip Sundays

        const startLabel = startTimeStr && /^(\d{2}):(\d{2})$/.test(startTimeStr) ? `${startTimeStr}:00` : "10:00:00";
        const endLabel = endTimeStr && /^(\d{2}):(\d{2})$/.test(endTimeStr) ? `${endTimeStr}:00` : "13:00:00";
        const step = Number.isFinite(Number(intervalMinutes)) && Number(intervalMinutes) > 0 ? Number(intervalMinutes) : 15;

        const startTime = new Date(`${dateStr}T${startLabel}`);
        const endTime = new Date(`${dateStr}T${endLabel}`);

        while (startTime < endTime) {
          const hh = String(startTime.getHours()).padStart(2, "0");
          const mm = String(startTime.getMinutes()).padStart(2, "0");
          const timeString = `${hh}:${mm}`;

          allSlots.push({
            clinic: clinic._id,
            date: dateStr,
            time: timeString,
            isAvailable: true,
          });

          startTime.setMinutes(startTime.getMinutes() + step);
        }
      }
    }

    const createdSlots = await Slot.insertMany(allSlots);

    res.status(201).json({
      message: `Slots created for ${validClinics.length} clinics for the entire month`,
      totalSlots: createdSlots.length,
    });
  } catch (error) {
    console.error("Error creating monthly slots:", error);
    res.status(500).json({ error: error.message });
  }
});

router.get("/slots/:clinicId", async (req, res) => {
  try {
    const { clinicId } = req.params;
    const { date } = req.query;

    const clinic = await Clinic.findById(clinicId);
    if (!clinic) {
      return res.status(400).json({ error: "Invalid clinic ID" });
    }

    const query = { clinic: clinicId };
    if (date) {
      query.date = date;
    }

    const availableSlots = await Slot.find(query).sort({ time: 1 });
    res.status(200).json(availableSlots);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

router.patch("/slots/:slotId", async (req, res) => {
  try {
    const { slotId } = req.params;

    const slot = await Slot.findById(slotId);
    if (!slot) {
      return res.status(404).json({ error: "Slot not found" });
    }

    slot.isAvailable = false;
    await slot.save();

    res.status(200).json(slot);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

router.delete("/slots/delete-all", async (req, res) => {
  try {
    const result = await Slot.deleteMany({}); // ⚡️ Deletes all slot documents
    res.json({ message: `Deleted ${result.deletedCount} slot(s).` });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Error deleting slots" });
  }
});

// Delete slots for a specific day with optional time window
router.post("/slots-delete-day", async (req, res) => {
  try {
    const { clinicId, date, startTime: startTimeStr, endTime: endTimeStr } = req.body;

    if (!clinicId || !date) {
      return res.status(400).json({ error: "Missing required fields (clinicId, date)" });
    }

    const clinic = await Clinic.findById(clinicId);
    if (!clinic) {
      return res.status(400).json({ error: "Invalid clinic ID" });
    }

    const query = { clinic: clinicId, date };
    if (startTimeStr && endTimeStr) {
      const valid = /^(\d{2}):(\d{2})$/;
      if (!valid.test(startTimeStr) || !valid.test(endTimeStr)) {
        return res.status(400).json({ error: "Invalid time format, expected HH:mm" });
      }
      query.time = { $gte: startTimeStr, $lt: endTimeStr };
    }

    const result = await Slot.deleteMany(query);
    res.json({ message: `Deleted ${result.deletedCount} slot(s).` });
  } catch (error) {
    console.error("Error deleting day slots:", error);
    res.status(500).json({ error: error.message });
  }
});

// Delete slots for a specific month for one clinic (optional time window)
router.post("/slots-delete-month", async (req, res) => {
  try {
    const { clinicId, month, year, startTime: startTimeStr, endTime: endTimeStr } = req.body;

    if (!clinicId || !month || !year) {
      return res.status(400).json({ error: "Missing required fields (clinicId, month, year)" });
    }

    const clinic = await Clinic.findById(clinicId);
    if (!clinic) {
      return res.status(400).json({ error: "Invalid clinic ID" });
    }

    const monthStr = String(month).padStart(2, "0");
    const dateRegex = new RegExp(`^${year}-${monthStr}-\\d{2}$`);

    const query = { clinic: clinicId, date: { $regex: dateRegex } };
    if (startTimeStr && endTimeStr) {
      const valid = /^(\d{2}):(\d{2})$/;
      if (!valid.test(startTimeStr) || !valid.test(endTimeStr)) {
        return res.status(400).json({ error: "Invalid time format, expected HH:mm" });
      }
      query.time = { $gte: startTimeStr, $lt: endTimeStr };
    }

    const result = await Slot.deleteMany(query);
    res.json({ message: `Deleted ${result.deletedCount} slot(s).` });
  } catch (error) {
    console.error("Error deleting monthly slots:", error);
    res.status(500).json({ error: error.message });
  }
});

// Delete slots for a specific month for many clinics (optional time window)
router.post("/slots-delete-many-month", async (req, res) => {
  try {
    const { clinicIds, month, year, startTime: startTimeStr, endTime: endTimeStr } = req.body;

    if (!clinicIds || !Array.isArray(clinicIds) || clinicIds.length === 0 || !month || !year) {
      return res
        .status(400)
        .json({ error: "Missing required fields (clinicIds[], month, year)" });
    }

    const validClinics = await Clinic.find({ _id: { $in: clinicIds } }, { _id: 1 });
    const validIds = validClinics.map(c => c._id);
    if (validIds.length === 0) {
      return res.status(400).json({ error: "No valid clinic IDs provided" });
    }

    const monthStr = String(month).padStart(2, "0");
    const dateRegex = new RegExp(`^${year}-${monthStr}-\\d{2}$`);

    const query = { clinic: { $in: validIds }, date: { $regex: dateRegex } };
    if (startTimeStr && endTimeStr) {
      const valid = /^(\d{2}):(\d{2})$/;
      if (!valid.test(startTimeStr) || !valid.test(endTimeStr)) {
        return res.status(400).json({ error: "Invalid time format, expected HH:mm" });
      }
      query.time = { $gte: startTimeStr, $lt: endTimeStr };
    }

    const result = await Slot.deleteMany(query);
    res.json({ message: `Deleted ${result.deletedCount} slot(s). for ${validIds.length} clinic(s)` });
  } catch (error) {
    console.error("Error deleting slots for many clinics:", error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
