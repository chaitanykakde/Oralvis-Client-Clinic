const Clinic = require("../models/clinic");
const User = require("../models/user");
const moment = require("moment");
const Slot = require("../models/slot");
const mongoose = require("mongoose");
const redis = require("../config/redis");
const ClinicRegistrationRequest = require("../models/ClinicRegistrationRequest");
const bcrypt = require("bcryptjs");
const Booking = require("../models/booking");
const { Types } = require("mongoose");
const WalkinPatient = require("../models/walkin");
const redisClient = require("../config/redis");
const generatePatientId = require("../utils/generatePatientId");
const ClinicalRecord = require("../models/clinicalRecord");
const uploadToS3 = require("../utils/uploadToS3");
const s3 = require("../config/aws");
const MedicalHistory = require("../models/medicalHistory");
exports.registerClinic = async (req, res) => {
  try {
    const { name, phoneNo, email, password } = req.body;

    const existingUser = await User.findOne({ phoneNo });
    if (existingUser) {
      return res.status(400).json({ error: "Phone number already registered" });
    }

    // 2. Check if a pending request with the same phoneNo already exists
    const existingRequest = await ClinicRegistrationRequest.findOne({
      phoneNo,
    });
    if (existingRequest) {
      return res.status(400).json({ error: "Request already submitted" });
    }

    const hashedPassword = await bcrypt.hash(password, 10);

    const request = new ClinicRegistrationRequest({
      name,
      phoneNo,
      email,
      password: hashedPassword,
    });
    await request.save();

    res.json({ message: "Clinic registration request submitted successfully" });
  } catch (error) {
    console.error("Registration error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

exports.createClinic = async (req, res) => {
  try {
    const { userId } = req.params;

    const {
      name,
      mainarea,
      introline,
      address,
      phoneNo,
      image,
      coverimage,
      noofpatients,
      yearsofexp,
      sterlizedequipmentpercentage,
      coverVideo,
      services,
      dentistIds,
      about,
      location,
      patient_experiences,
      fees,
      city,
      mainDoctor,
    } = req.body;

    if (!name || !mainarea) {
      return res
        .status(400)
        .json({ message: "Name and mainarea are required" });
    }

    // Check if the user exists and is of role clinic
    const user = await User.findById(userId);
    if (!user || user.role !== "clinic") {
      return res.status(400).json({ message: "Invalid clinic user" });
    }

    const clinic = new Clinic({
      name,
      mainarea,
      introline,
      address,
      phoneNo,
      image,
      coverimage,
      noofpatients,
      yearsofexp,
      sterlizedequipmentpercentage,
      coverVideo,
      services,
      dentists: dentistIds,
      mainDoctor, // ✅ added mainDoctor
      fees, // ✅ added fees
      about,
      location,
      city, // ✅ added city
      patient_experiences,
      owner: userId, // ✅ assign owner as the userId
    });

    await clinic.save();

    // Optional: add clinic ID to dentist profiles
    if (Array.isArray(dentistIds) && dentistIds.length > 0) {
      await User.updateMany(
        { _id: { $in: dentistIds } },
        { $addToSet: { clinics: clinic._id } }
      );
    }

    res.status(201).json({ message: "Clinic created successfully", clinic });
  } catch (err) {
    console.error("❌ Error creating clinic:", err);
    res.status(500).json({ message: "Failed to create clinic" });
  }
};

exports.addDentistToClinic = async (req, res) => {
  try {
    const { clinicId } = req.params;
    const { dentistId } = req.body;

    // Add dentist to clinic
    const clinic = await Clinic.findByIdAndUpdate(
      clinicId,
      { $addToSet: { dentists: dentistId } }, // prevent duplicates
      { new: true }
    ).populate("dentists", "name email phoneNo");

    if (!clinic) {
      return res.status(404).json({ message: "Clinic not found" });
    }

    // Add clinic to dentist
    await User.findByIdAndUpdate(
      dentistId,
      { $addToSet: { clinics: clinicId } },
      { new: true }
    );

    res.json({ message: "Dentist added to clinic", clinic });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Failed to add dentist to clinic" });
  }
};
exports.getClinicById = async (req, res) => {
  try {
    const { clinicId } = req.params;

    const clinic = await Clinic.findById(clinicId).populate(
      "dentists",
      "name image qualification"
    );

    if (!clinic) {
      return res.status(404).json({ message: "Clinic not found" });
    }

    res.json(clinic);
  } catch (error) {
    console.error("Error fetching clinic by ID:", error);
    res.status(500).json({ message: "Server error" });
  }
};

exports.getSlotsByDate = async (req, res) => {
  const { clinicId } = req.params;
  const { date } = req.query;

  if (!mongoose.Types.ObjectId.isValid(clinicId)) {
    return res.status(400).json({ error: "Invalid clinic ID" });
  }

  // Parse Date
  let targetDate;
  if (date === "today") {
    targetDate = moment();
  } else if (date === "tomorrow") {
    targetDate = moment().add(1, "days");
  } else if (date === "dayafter") {
    targetDate = moment().add(2, "days");
  } else if (moment(date, "YYYY-MM-DD", true).isValid()) {
    targetDate = moment(date);
  } else {
    return res.status(400).json({ error: "Invalid date format or keyword" });
  }

  const formattedDate = targetDate.format("YYYY-MM-DD");

  try {
    let query = {
      clinic: clinicId,
      date: formattedDate,
    };

    if (date === "today") {
      const nowTime = moment().utcOffset("+05:30").format("HH:mm");
      query.time = { $gt: nowTime };
    }

    const slots = await Slot.find(query).lean();

    // Fetch bookings for this clinic (we'll filter by day in code to avoid timezone issues)
    const bookings = await Booking.find({
      clinic: clinicId,
      status: { $ne: "cancelled" }, // Exclude cancelled bookings
    }).lean();

    // Build a set of times that are booked on this specific day (convert to 24-hour format)
    const bookedSlots = new Set();
    bookings.forEach((booking) => {
      if (!booking.appointmentDate) return;

      const bookingDay = moment(booking.appointmentDate).format("YYYY-MM-DD");
      if (bookingDay !== formattedDate) return;

      let slotTime24 = booking.slotTime;
      // Convert AM/PM format to 24-hour if needed
      if (booking.slotTime.includes("AM") || booking.slotTime.includes("PM")) {
        const [time, modifier] = booking.slotTime.split(" ");
        let [hours, minutes] = time.split(":").map(Number);
        if (modifier === "PM" && hours !== 12) hours += 12;
        if (modifier === "AM" && hours === 12) hours = 0;
        slotTime24 = `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}`;
      }
      bookedSlots.add(slotTime24);
    });

    // Mark slots as unavailable if they're booked or held in Redis
    for (let slot of slots) {
      const redisKey = `slot:hold:${clinicId}:${formattedDate}:${slot.time}`;
      const held = await redisClient.get(redisKey);
      
      // Check if slot is booked (either in Slot collection or in Booking collection)
      if (held || bookedSlots.has(slot.time) || slot.isAvailable === false) {
        slot.isAvailable = false;
      }
    }

    res.json(slots);
  } catch (err) {
    console.error("Error fetching slots:", err);
    res.status(500).json({ error: "Failed to fetch slots" });
  }
};

exports.rescheduleBooking = async (req, res) => {
  const { bookingId } = req.params;
  const { newSlotId } = req.body;

  if (
    !mongoose.Types.ObjectId.isValid(bookingId) ||
    !mongoose.Types.ObjectId.isValid(newSlotId)
  ) {
    return res.status(400).json({ error: "Invalid ID(s)" });
  }

  try {
    // 1. Find the booking
    const booking = await Booking.findById(bookingId);
    if (!booking) return res.status(404).json({ error: "Booking not found" });

    // 2. Find the new slot
    const newSlot = await Slot.findById(newSlotId);
    if (!newSlot || !newSlot.isAvailable) {
      return res.status(400).json({ error: "Selected slot is unavailable" });
    }

    // 3. Mark the previous slot as available
    if (booking.slotId) {
      await Slot.findByIdAndUpdate(booking.slotId, { isAvailable: true });
    }

    // 4. Reserve the new slot
    newSlot.isAvailable = false;
    await newSlot.save();

    // 5. Update booking
    booking.appointmentDate = newSlot.date;
    booking.slotTime = newSlot.time;
    booking.slotId = newSlot._id;
    await booking.save();

    res.json({ message: "Booking rescheduled successfully", booking });
  } catch (error) {
    console.error("Reschedule error:", error);
    res.status(500).json({ error: "Failed to reschedule appointment" });
  }
};

exports.updateClinicLocation = async (req, res) => {
  try {
    const clinicId = req.params.id;
    const { latitude, longitude } = req.body;

    if (!latitude || !longitude) {
      return res
        .status(400)
        .json({ error: "Latitude and longitude are required" });
    }

    const clinic = await Clinic.findByIdAndUpdate(
      clinicId,
      {
        location: {
          type: "Point",
          coordinates: [longitude, latitude], // GeoJSON format: [lng, lat]
        },
      },
      { new: true }
    );

    if (!clinic) {
      return res.status(404).json({ error: "Clinic not found" });
    }
    res.status(200).json({ message: "Location updated successfully", clinic });
  } catch (err) {
    console.error("Error updating clinic location:", err);
    res.status(500).json({ error: "Failed to update location" });
  }
};

// Update notes/records for a specific booking (clinic side)
exports.updateBookingNotesByClinic = async (req, res) => {
  try {
    const { bookingId } = req.params;
    const { notes } = req.body;

    if (!mongoose.Types.ObjectId.isValid(bookingId)) {
      return res.status(400).json({ error: "Invalid booking ID" });
    }

    const booking = await Booking.findByIdAndUpdate(
      bookingId,
      { notes: notes || "" },
      { new: true }
    );

    if (!booking) {
      return res.status(404).json({ error: "Booking not found" });
    }

    res.json({ message: "Records updated successfully", booking });
  } catch (err) {
    console.error("Error updating booking notes:", err);
    res.status(500).json({ error: "Failed to update records" });
  }
};

exports.updateAboutSection = async (req, res) => {
  try {
    const clinicId = req.params.id;
    const { parah, points_to_be_highlighted } = req.body;

    const updatedClinic = await Clinic.findByIdAndUpdate(
      clinicId,
      {
        about: {
          parah,
          points_to_be_highlighted,
        },
      },
      { new: true, runValidators: true }
    );
    if (!updatedClinic) {
      return res.status(404).json({ message: "Clinic not found" });
    }
    res.json({ message: "About section updated", clinic: updatedClinic });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Error updating about section" });
  }
};

exports.getNearbyClinics = async (req, res) => {
  const { lat, lng } = req.query;

  if (!lat || !lng) {
    return res
      .status(400)
      .json({ error: "Latitude and longitude are required" });
  }

  try {
    const cacheKey = `nearby:${lat}:${lng}`;
    const cachedResults = await redis.get(cacheKey);
    if (cachedResults) {
      return res.json(JSON.parse(cachedResults));
    }

    const ids = await redis.georadius(
      "clinics:geo",
      parseFloat(lng),
      parseFloat(lat),
      5,
      "km"
    );
    

    if (ids.length === 0) {
      const clinics = await Clinic.find(
        {
          location: {
            $near: {
              $geometry: {
                type: "Point",
                coordinates: [parseFloat(lng), parseFloat(lat)],
              },
              $maxDistance: 5000,
            },
          },
        },
        "name image mainarea mainDoctor fees"
      ).populate("mainDoctor", "name image");
      await redis.set(cacheKey, JSON.stringify(clinics), 'EX', 300); // 5 min
      return res.json(clinics);
    }

    const objectIds = ids.map((id) => mongoose.Types.ObjectId(id));
    const clinics = await Clinic.find(
      { _id: { $in: objectIds } },
      "name image mainarea mainDoctor"
    ).populate("mainDoctor", "name image");

    await redis.set(cacheKey, JSON.stringify(clinics), 'EX', 300);
    res.json(clinics);
  } catch (err) {
    console.error("Error finding nearby clinics:", err);
    res.status(500).json({ error: "Failed to fetch nearby clinics" });
  }
};

// Add dentist to a clinic
exports.addDentist = async (req, res) => {
  const { clinicId, dentistId } = req.body;

  try {
    const clinic = await Clinic.findById(clinicId);
    const dentist = await User.findById(dentistId);

    if (!clinic || !dentist) {
      return res.status(404).json({ message: "Clinic or Dentist not found" });
    }

    if (!clinic.dentists.includes(dentistId)) {
      clinic.dentists.push(dentistId);
      await clinic.save();
    }

    if (!dentist.clinics.includes(clinicId)) {
      dentist.clinics.push(clinicId);
      await dentist.save();
    }

    const updatedClinic = await Clinic.findById(clinicId).populate(
      "dentists",
      "name phoneNo"
    );
    res.json(updatedClinic);
  } catch (err) {
    console.error("Error adding dentist:", err);
    res.status(500).json({ error: "Internal server error" });
  }
};

// Remove dentist from a clinic
exports.removeDentist = async (req, res) => {
  const { clinicId, dentistId } = req.body;

  try {
    const clinic = await Clinic.findById(clinicId);
    const dentist = await User.findById(dentistId);

    if (!clinic || !dentist) {
      return res.status(404).json({ message: "Clinic or Dentist not found" });
    }

    clinic.dentists = clinic.dentists.filter(
      (id) => id.toString() !== dentistId
    );
    await clinic.save();

    dentist.clinics = dentist.clinics.filter(
      (id) => id.toString() !== clinicId
    );
    await dentist.save();

    const updatedClinic = await Clinic.findById(clinicId).populate(
      "dentists",
      "name phoneNo"
    );
    res.json(updatedClinic);
  } catch (err) {
    console.error("Error removing dentist:", err);
    res.status(500).json({ error: "Internal server error" });
  }
};

// Get all clinics
exports.getAllClinics = async (req, res) => {
  try {
    const clinics = await Clinic.find({}, "name city image mainarea");
    res.json(clinics);
  } catch (err) {
    res.status(500).json({ error: "Failed to fetch clinics" });
  }
};

exports.getAllClinicWithDoctor = async (req, res) => {
  try {
    const cacheKey = "clinics:all:basic";
    const staleKey = "clinics:all:stale";

    const cachedClinics = await redis.get(cacheKey);
    if (cachedClinics) {
      const isStale = await redis.exists(staleKey);
      if (!isStale) {
        await redis.set(staleKey, "1", "EX", 60);
        setTimeout(async () => {
          try {
            const freshClinics = await Clinic.find(
              {},
              "name image mainarea mainDoctor fees city"
            ).populate("mainDoctor", "name image");
            await redis.set(cacheKey, JSON.stringify(freshClinics), "EX", 3600);
            await redis.del(staleKey);
          } catch (err) {
            console.error("Background refresh failed:", err);
          }
        }, 0);
      }
      return res.json(JSON.parse(cachedClinics));
    }

    // ✅ First fetch when cache is empty
    const clinics = await Clinic.find(
      {},
      "name image mainarea mainDoctor fees city"
    ).populate("mainDoctor", "name image");
    await redis.set(cacheKey, JSON.stringify(clinics), "EX", 3600);
    res.json(clinics);
  } catch (err) {
    console.error("Error fetching all clinics:", err);
    res.status(500).json({ error: "Failed to fetch clinics" });
  }
};

// Get a specific clinic
exports.getClinic = async (req, res) => {
  try {
    const clinic = await Clinic.findById(req.params.clinicId).populate(
      "dentists",
      "name phoneNo image bio"
    );
    if (!clinic) {
      return res.status(404).json({ error: "Clinic not found" });
    }
    res.json(clinic);
  } catch (err) {
    res.status(500).json({ error: "Failed to fetch clinic details" });
  }
};

exports.getClinicDashboardStats = async (req, res) => {
  const { clinicId } = req.params;

  if (!mongoose.Types.ObjectId.isValid(clinicId)) {
    return res.status(400).json({ error: "Invalid clinic ID" });
  }

  try {
    // Total patients
    const totalPatients = await Booking.countDocuments({ clinic: clinicId });

    // Today's appointments
    const todayStart = moment().startOf("day").toDate();
    const todayEnd = moment().endOf("day").toDate();
    const todaysAppointments = await Booking.countDocuments({
      clinic: clinicId,
      appointmentDate: { $gte: todayStart, $lte: todayEnd },
    });

    // Completed appointments
    const completedAppointments = await Booking.countDocuments({
      clinic: clinicId,
      status: "completed",
    });

    // Appointments over time (grouped by date)
    const appointmentsOverTime = await Booking.aggregate([
      { $match: { clinic: new mongoose.Types.ObjectId(clinicId) } },
      {
        $group: {
          _id: {
            $dateToString: { format: "%Y-%m-%d", date: "$appointmentDate" },
          },
          count: { $sum: 1 },
        },
      },
      { $sort: { _id: 1 } },
    ]);

    // Earnings = total paid appointments * fees
    const clinic = await Clinic.findById(clinicId);
    const paidAppointments = await Booking.countDocuments({
      clinic: clinicId,
      status: { $in: ["paid", "completed"] },
    });
    const earnings = paidAppointments * (clinic?.fees || 0);

    res.json({
      totalPatients,
      todaysAppointments,
      completedAppointments,
      appointmentsOverTime,
      earnings,
    });
  } catch (err) {
    console.error("Error in getClinicDashboardStats:", err);
    res.status(500).json({ error: "Server error" });
  }
};

exports.getAppointmentStatusCounts = async (req, res) => {
  const { clinicId } = req.params;

  if (!mongoose.Types.ObjectId.isValid(clinicId)) {
    return res.status(400).json({ error: "Invalid clinic ID" });
  }

  try {
    const statusCounts = await Booking.aggregate([
      { $match: { clinic: new mongoose.Types.ObjectId(clinicId) } },
      {
        $group: {
          _id: "$status",
          count: { $sum: 1 },
        },
      },
    ]);

    // Initialize all possible statuses to 0
    const result = {
      paid: 0,
      pending: 0,
      confirmed: 0,
      completed: 0,
      cancelled: 0,
    };

    statusCounts.forEach((entry) => {
      result[entry._id] = entry.count;
    });

    res.json(result);
  } catch (err) {
    console.error("Error fetching status counts:", err);
    res.status(500).json({ error: "Server error" });
  }
};
exports.getClinicAppointments = async (req, res) => {
  const { clinicId } = req.params;

  try {
    const appointments = await Booking.find({ clinic: clinicId })
      .populate("patient", "name")
      .populate("walkinPatient", "name")
      .populate("clinic", "name")
      .lean();

    // Sort: 1) Descending date, 2) Ascending time
    appointments.sort((a, b) => {
      const dateA = new Date(a.appointmentDate);
      const dateB = new Date(b.appointmentDate);

      if (dateA > dateB) return -1;
      if (dateA < dateB) return 1;

      const [hoursA, minutesA] = a.slotTime.split(":").map(Number);
      const [hoursB, minutesB] = b.slotTime.split(":").map(Number);

      return hoursA !== hoursB ? hoursA - hoursB : minutesA - minutesB;
    });

    res.json(
      appointments.map((appt) => ({
        ...appt,
        patientName:
          appt.patient?.name || appt.walkinPatient?.name || "Unknown",
      }))
    );
  } catch (err) {
    console.error("Error fetching appointments:", err);
    res.status(500).json({ error: "Failed to fetch clinic appointments" });
  }
};

exports.getClinicProfile = async (req, res) => {
  const { userId } = req.params;
  try {
    const clinic = await Clinic.findOne({ owner: userId }).populate(
      "mainDoctor dentists"
    );
    if (!clinic) {
      return res.status(200).json({ message: "No profile yet", clinic: null });
    }
    res.json(clinic);
  } catch (err) {
    console.error("Error fetching clinic profile:", err);
    res.status(500).json({ error: "Failed to fetch clinic profile" });
  }
};

exports.createClinicProfile = async (req, res) => {
  const { userId } = req.params;
  const {
    name,
    mainarea,
    introline,
    address,
    phoneNo,
    image,
    coverimage,
    noofpatients,
    yearsofexp,
    sterlizedequipmentpercentage,
    coverVideo,
    services,
    dentists,
    mainDoctor,
    city,
    fees,
    about,
    coordinates,
  } = req.body;

  try {
    const existingClinic = await Clinic.findOne({ owner: userId });
    if (existingClinic) {
      return res
        .status(400)
        .json({ error: "Clinic already exists for this user" });
    }

    const newClinic = new Clinic({
      name,
      mainarea,
      introline,
      address,
      phoneNo,
      image,
      coverimage,
      noofpatients,
      yearsofexp,
      sterlizedequipmentpercentage,
      coverVideo,
      services,
      dentists,
      mainDoctor,
      city,
      fees,
      about,
      location: {
        type: "Point",
        coordinates: coordinates,
      },
      owner: userId,
    });

    await newClinic.save();
    res.status(201).json(newClinic);
  } catch (err) {
    console.error("Error creating clinic profile:", err);
    res.status(500).json({ error: "Failed to create clinic profile" });
  }
};

exports.updateClinicAndUser = async (req, res) => {
  const { userId } = req.params;
  const { clinicData, userData } = req.body;

  try {
    const clinic = await Clinic.findOne({ owner: userId });
    const user = await User.findById(userId);

    if (!clinic || !user) {
      return res.status(404).json({ error: "Clinic or User not found" });
    }

    // Update clinic
    Object.assign(clinic, {
      ...clinicData,
      location: {
        type: "Point",
        coordinates: clinicData.coordinates,
      },
    });

    // Update user
    Object.assign(user, userData);

    await clinic.save();
    await user.save();

    res.status(200).json({ message: "Clinic and user updated", clinic, user });
  } catch (err) {
    console.error("Error updating clinic & user:", err);
    res.status(500).json({ error: "Failed to update profile" });
  }
};

exports.cancelSingleBookingByClinic = async (req, res) => {
  const { bookingId } = req.params;

  try {
    const booking = await Booking.findById(bookingId);
    if (!booking) {
      return res.status(404).json({ error: "Booking not found" });
    }

    booking.status = "cancelled";
    await booking.save();

    return res
      .status(200)
      .json({ message: "Booking cancelled by clinic", booking });
  } catch (err) {
    console.error("Error cancelling booking:", err);
    return res.status(500).json({ error: "Server error cancelling booking" });
  }
};

exports.cancelBookingsByDateForClinic = async (req, res) => {
  const { clinicId } = req.params;
  const { date } = req.body; // Expecting ISO format or YYYY-MM-DD

  if (!date) {
    return res.status(400).json({ error: "Date is required in body" });
  }

  try {
    const start = new Date(date);
    const end = new Date(date);
    end.setHours(23, 59, 59, 999);

    const result = await Booking.updateMany(
      {
        clinic: clinicId,
        appointmentDate: { $gte: start, $lte: end },
        status: { $ne: "cancelled" },
      },
      { $set: { status: "cancelled" } }
    );

    return res.status(200).json({
      message: `Cancelled ${result.modifiedCount} bookings for ${date}`,
    });
  } catch (err) {
    console.error("Error cancelling bookings by date:", err);
    return res.status(500).json({ error: "Server error cancelling bookings" });
  }
};

exports.getBookingsByDate = async (req, res) => {
  const { clinicId } = req.params;
  const { date } = req.query; // expecting date in "YYYY-MM-DD" format

  if (!date) {
    return res.status(400).json({ error: "Date is required in query params" });
  }

  try {
    const start = new Date(date);
    const end = new Date(date);
    end.setHours(23, 59, 59, 999);

    const bookings = await Booking.find({
      clinic: clinicId,
      appointmentDate: { $gte: start, $lte: end },
    })
      .populate("patient", "name email phoneNo") // include patient info
      .populate("clinic", "name");

    return res.status(200).json({ bookings });
  } catch (err) {
    console.error("Error fetching bookings by date:", err);
    return res.status(500).json({ error: "Server error fetching bookings" });
  }
};

exports.getClinicIdByUserId = async (req, res) => {
  const { userId } = req.params;

  try {
    // Validate userId
    if (!userId || userId === "undefined" || userId === "null") {
      return res.status(400).json({ error: "Invalid user ID provided." });
    }

    // Validate ObjectId format
    if (!mongoose.Types.ObjectId.isValid(userId)) {
      return res.status(400).json({ error: "Invalid user ID format." });
    }

    const clinic = await Clinic.findOne({ owner: userId });

    if (!clinic) {
      return res.status(404).json({ error: "Clinic not found for this user." });
    }

    return res.status(200).json({ clinicId: clinic._id });
  } catch (err) {
    console.error("Error fetching clinic ID:", err);
    return res.status(500).json({ error: "Server error" });
  }
};

exports.getClinicEarnings = async (req, res) => {
  const { clinicId } = req.params;

  try {
    const bookings = await Booking.find({
      clinic: clinicId,
      status: { $in: ["completed", "paid", "confirmed"] },
    });

    if (!bookings || bookings.length === 0) {
      return res.json({
        totalEarnings: 0,
        monthlyEarnings: [],
      });
    }

    // Get the clinic's fee
    const clinic = await Clinic.findById(clinicId);
    if (!clinic) {
      return res.status(404).json({ error: "Clinic not found" });
    }

    const fee = clinic.fees;
    const totalEarnings = bookings.length * fee;

    // Calculate month-wise earnings
    const monthlyMap = {};

    bookings.forEach((booking) => {
      const date = new Date(booking.appointmentDate);
      const monthKey = `${date.getFullYear()}-${(date.getMonth() + 1)
        .toString()
        .padStart(2, "0")}`;

      if (!monthlyMap[monthKey]) {
        monthlyMap[monthKey] = 0;
      }
      monthlyMap[monthKey] += fee;
    });

    const monthlyEarnings = Object.entries(monthlyMap)
      .map(([month, amount]) => ({ month, amount }))
      .sort((a, b) => new Date(a.month) - new Date(b.month));

    res.json({
      totalEarnings,
      monthlyEarnings,
    });
  } catch (err) {
    console.error("Error calculating earnings:", err);
    res.status(500).json({ error: "Internal server error" });
  }
};

exports.getEarningClinicDashboardStats = async (req, res) => {
  const { clinicId } = req.params;

  try {
    // Step 1: Fetch all bookings for the clinic
    const bookings = await Booking.find({ clinic: clinicId });

    if (!bookings.length) {
      return res.json({
        totalPatients: 0,
        totalAppointments: 0,
        completedAppointments: 0,
        earnings: 0,
      });
    }

    // Step 2: Compute distinct patient IDs (include walk-ins, guard undefined)
    const uniquePatientIds = new Set(
      bookings
        .map((b) => {
          if (b.patient) return `p:${b.patient.toString()}`;
          if (b.walkinPatient) return `w:${b.walkinPatient.toString()}`;
          return null;
        })
        .filter((id) => id !== null)
    );
    const totalPatients = uniquePatientIds.size;

    // Step 3: Count completed appointments
    const completedAppointments = bookings.filter(
      (b) => b.status === "completed"
    ).length;

    // Step 4: Filter for earnings (confirmed or paid)
    const earningBookings = bookings.filter((b) =>
      ["confirmed", "paid"].includes(b.status)
    );

    // Step 5: Get clinic fee
    const clinic = await Clinic.findById(clinicId);
    if (!clinic) return res.status(404).json({ error: "Clinic not found" });

    const earnings = earningBookings.length * clinic.fees;

    // Response
    res.json({
      totalPatients,
      totalAppointments: bookings.length,
      completedAppointments,
      earnings,
    });
  } catch (err) {
    console.error("Error fetching dashboard stats:", err);
    res.status(500).json({ error: "Internal server error" });
  }
};

exports.markBookingAsPaid = async (req, res) => {
  const { bookingId } = req.params;

  try {
    const booking = await Booking.findById(bookingId);

    if (!booking) {
      return res.status(404).json({ message: "Booking not found" });
    }

    if (booking.status !== "confirmed") {
      return res
        .status(400)
        .json({ message: "Only confirmed bookings can be marked as paid" });
    }

    booking.status = "paid";
    await booking.save();

    res.status(200).json({ message: "Booking marked as paid", booking });
  } catch (err) {
    console.error("Error updating booking:", err);
    res.status(500).json({ error: "Failed to mark booking as paid" });
  }
};

exports.getCityAreaMap = async (req, res) => {
  try {
    const clinics = await Clinic.find({}, "city mainarea");

    const map = {};

    clinics.forEach((clinic) => {
      const city = clinic.city;
      const area = clinic.mainarea;

      if (!map[city]) {
        map[city] = new Set(); // use Set to avoid duplicates
      }
      map[city].add(area);
    });

    // Convert sets to arrays
    const result = {};
    for (const city in map) {
      result[city] = Array.from(map[city]);
    }

    res.json(result);
  } catch (err) {
    console.error("Error fetching city-area map:", err);
    res.status(500).json({ error: "Failed to fetch city-area map" });
  }
};

exports.getAppointmentsByMonthOrDate = async (req, res) => {
  try {
    const { clinicId } = req.params;
    const { month, year, date } = req.query;

    const monthNum = parseInt(month);
    const yearNum = parseInt(year);

    // Require either month/year or a specific date
    if ((!monthNum || !yearNum) && !date) {
      return res
        .status(400)
        .json({ message: "Provide either month/year or date" });
    }

    if (!mongoose.Types.ObjectId.isValid(clinicId)) {
      return res.status(400).json({ message: "Invalid clinic ID" });
    }

    const clinic = await Clinic.findById(clinicId);
    if (!clinic) {
      return res.status(404).json({ message: "Clinic not found" });
    }

    // If a specific date is requested, fetch all clinic bookings
    // and filter by that calendar day in code to avoid timezone issues.
    if (date) {
      const allAppointments = await Booking.find({
        clinic: new mongoose.Types.ObjectId(clinicId),
      })
        .populate({ path: "patient", select: "name" })
        .populate({ path: "walkinPatient", select: "name phoneNo" })
        .lean();

      const targetDay = moment(date).format("YYYY-MM-DD");

      const filtered = allAppointments.filter((appt) => {
        if (!appt.appointmentDate) return false;
        const apptDay = moment(appt.appointmentDate).format("YYYY-MM-DD");
        return apptDay === targetDay;
      });

      const result = filtered.map((appt) => ({
        patientName:
          appt.patient?.name || appt.walkinPatient?.name || "Unknown",
        slotTime: appt.slotTime,
        appointmentDate: appt.appointmentDate,
        durationMinutes: appt.durationMinutes,
      }));

      return res.json(result);
    }

    // Month summary path (keep existing DB-side date filtering)
    let startDate, endDate;
    startDate = new Date(yearNum, monthNum - 1, 1);
    endDate = new Date(yearNum, monthNum, 0, 23, 59, 59, 999);

    const appointments = await Booking.find({
      clinic: new mongoose.Types.ObjectId(clinicId),
      appointmentDate: { $gte: startDate, $lte: endDate },
    })
      .populate({ path: "patient", select: "name" })
      .populate({ path: "walkinPatient", select: "name phoneNo" })
      .lean();

    const result = {};
    for (const appt of appointments) {
      const apptDate = new Date(appt.appointmentDate);
      const day = apptDate.getDate().toString().padStart(2, "0");

      if (!result[day]) result[day] = { backend: 0, walkin: 0 };

      if (appt.walkinPatient) {
        result[day].walkin += 1;
      } else {
        result[day].backend += 1;
      }
    }

    return res.json(result);
  } catch (err) {
    console.error(err);
    return res.status(500).json({ message: "Server error" });
  }
};

exports.bookWalkinPatient = async (req, res) => {
  const session = await Booking.startSession();
  session.startTransaction();

  try {
    const {
      clinicId,
      name,
      phoneNo,
      email,
      abhaId,
      tokenNumber,
      appointmentDate,
      slotTime,
      notes,
      plannedProcedures,
      doctor,
      duration,
    } = req.body;

    if (!Types.ObjectId.isValid(clinicId)) {
      await session.abortTransaction();
      session.endSession();
      return res.status(400).json({ message: "Invalid clinic ID" });
    }

    const clinic = await Clinic.findById(clinicId);
    if (!clinic) {
      await session.abortTransaction();
      session.endSession();
      return res.status(404).json({ message: "Clinic not found" });
    }

    // Format slotTime - keep AM/PM format for booking, convert to HH:mm for slot matching
    let formattedSlotTime = slotTime; // Keep original format for booking
    let slotTime24Hour = slotTime; // For slot matching
    
    if (slotTime.includes("AM") || slotTime.includes("PM")) {
      const [time, modifier] = slotTime.split(" ");
      let [hours, minutes] = time.split(":").map(Number);
      if (modifier === "PM" && hours !== 12) hours += 12;
      if (modifier === "AM" && hours === 12) hours = 0;
      slotTime24Hour = `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}`;
    } else {
      // If already in 24-hour format, convert to AM/PM for booking
      const [hours, minutes] = slotTime.split(":").map(Number);
      let hour12 = hours;
      let modifier = "AM";
      if (hours > 12) {
        hour12 = hours - 12;
        modifier = "PM";
      } else if (hours === 12) {
        modifier = "PM";
      } else if (hours === 0) {
        hour12 = 12;
      }
      formattedSlotTime = `${hour12}:${String(minutes).padStart(2, "0")} ${modifier}`;
    }

    // Check if slot is already booked (check both formats)
    const existingBooking = await Booking.findOne({
      clinic: clinicId,
      appointmentDate: new Date(appointmentDate),
      $or: [
        { slotTime: formattedSlotTime },
        { slotTime: slotTime24Hour }
      ],
      status: { $ne: "cancelled" },
    }).session(session);

    if (existingBooking) {
      await session.abortTransaction();
      session.endSession();
      return res.status(400).json({ message: "This slot is already booked" });
    }

    // Check if a walk-in with same phone already exists for this clinic
    let walkinPatient = await WalkinPatient.findOne({
      phoneNo,
      clinic: clinicId,
    }).session(session);

    if (!walkinPatient) {
      // Generate unique patient ID
      const generatedPatientId = await generatePatientId();
      
      walkinPatient = await WalkinPatient.create(
        [
          {
            name,
            phoneNo,
            patientId: generatedPatientId,
            email: email || "",
            abhaId: abhaId || "",
            tokenNumber: tokenNumber || "",
            clinic: clinicId,
          },
        ],
        { session }
      );
      walkinPatient = walkinPatient[0];
    } else {
      // Update existing walk-in patient with new information
      walkinPatient.name = name;
      // Only generate patientId if it doesn't exist
      if (!walkinPatient.patientId) {
        walkinPatient.patientId = await generatePatientId();
      }
      if (email) walkinPatient.email = email;
      if (abhaId) walkinPatient.abhaId = abhaId;
      if (tokenNumber) walkinPatient.tokenNumber = tokenNumber;
      await walkinPatient.save({ session });
    }

    // Combine notes and planned procedures
    const combinedNotes = [
      notes || "",
      plannedProcedures ? `Planned Procedures: ${plannedProcedures}` : "",
    ]
      .filter(Boolean)
      .join("\n");

    // Parse duration from "15 Min" format to minutes
    let durationMinutes = 30; // default
    if (duration) {
      const match = duration.match(/(\d+)\s*Min/i);
      if (match) {
        durationMinutes = parseInt(match[1], 10);
      }
    }

    const booking = new Booking({
      clinic: clinicId,
      walkinPatient: walkinPatient._id,
      appointmentDate: new Date(appointmentDate),
      slotTime: formattedSlotTime,
      notes: combinedNotes,
      status: "confirmed",
      paymentId: "WALKIN-" + Date.now(),
      durationMinutes,
    });

    await booking.save({ session });

    // Mark slot as unavailable (use 24-hour format for slot matching)
    const slotDate = new Date(appointmentDate).toISOString().split("T")[0];
    const slotUpdate = await Slot.findOneAndUpdate(
      {
        clinic: clinicId,
        date: slotDate,
        time: slotTime24Hour,
      },
      { isAvailable: false },
      { new: true, session }
    );

    // If slot doesn't exist, create it as unavailable
    if (!slotUpdate) {
      const newSlot = new Slot({
        clinic: clinicId,
        date: slotDate,
        time: slotTime24Hour,
        isAvailable: false,
      });
      await newSlot.save({ session });
    }

    await session.commitTransaction();
    session.endSession();

    res.status(201).json({ message: "Walk-in appointment booked", booking });
  } catch (err) {
    await session.abortTransaction();
    session.endSession();
    console.error("Error booking walk-in:", err);
    res.status(500).json({ message: "Server error" });
  }
};

exports.getAppointmentsByWeek = async (req, res) => {
  try {
    const { clinicId, dates } = req.body;

    if (!clinicId || !Array.isArray(dates) || dates.length === 0) {
      return res.status(400).json({ message: "Invalid clinic ID or dates" });
    }

    if (!mongoose.Types.ObjectId.isValid(clinicId)) {
      return res.status(400).json({ message: "Invalid clinic ID" });
    }

    const clinic = await Clinic.findById(clinicId);
    if (!clinic) {
      return res.status(404).json({ message: "Clinic not found" });
    }

    const startDate = new Date(dates[0]);
    const endDate = new Date(dates[dates.length - 1]);
    endDate.setHours(23, 59, 59, 999);

    const appointments = await Booking.find({
      clinic: new mongoose.Types.ObjectId(clinicId),
      appointmentDate: { $gte: startDate, $lte: endDate },
    })
      .populate({ path: "patient", select: "name" })
      .populate({ path: "walkinPatient", select: "name phoneNo" })
      .lean();

    const result = {};
    for (const appt of appointments) {
      const apptDate = new Date(appt.appointmentDate);
      const key = apptDate.toISOString().split("T")[0];

      if (!result[key]) result[key] = [];

      result[key].push({
        patientName:
          appt.patient?.name || appt.walkinPatient?.name || "Unknown",
        slotTime: appt.slotTime,
        appointmentDate: appt.appointmentDate,
      });
    }

    return res.json(result);
  } catch (err) {
    console.error("Error in getAppointmentsByWeek:", err);
    return res.status(500).json({ message: "Server error" });
  }
};

exports.clinicWithDoctor = async (req, res) => {
  try {
    const clinics = await Clinic.find({}, { mainDoctor: 1, _id: 1 }).populate(
      "mainDoctor",
      "name qualification image"
    );

    res.status(200).json(clinics);
  } catch (err) {
    console.error("Error fetching clinics:", err);
    res.status(500).json({ message: "Server error" });
  }
};

exports.getDentistsByClinic = async (req, res) => {
  try {
    const { clinicId } = req.params;

    const clinic = await Clinic.findById(clinicId).populate({
      path: "dentists",
      select: "name qualification image",
    });

    if (!clinic) {
      return res.status(404).json({ message: "Clinic not found" });
    }

    res.status(200).json({ dentists: clinic.dentists });
  } catch (error) {
    console.error("Error fetching dentists:", error);
    res.status(500).json({ message: "Internal server error" });
  }
};

// Create or update clinical record for a booking
exports.createOrUpdateClinicalRecord = async (req, res) => {
  try {
    const { bookingId } = req.params;
    const {
      complaints,
      observations,
      diagnoses,
      notes,
      prescriptions,
      vitalSigns,
      labOrders,
      files,
      treatmentPlan,
      shareWithPatient,
    } = req.body;

    if (!mongoose.Types.ObjectId.isValid(bookingId)) {
      return res.status(400).json({ error: "Invalid booking ID" });
    }

    const booking = await Booking.findById(bookingId)
      .populate("patient")
      .populate("walkinPatient");

    if (!booking) {
      return res.status(404).json({ error: "Booking not found" });
    }

    // Check if clinical record already exists
    let clinicalRecord = await ClinicalRecord.findOne({ booking: bookingId });

    const recordData = {
      booking: bookingId,
      clinic: booking.clinic,
      patient: booking.patient || null,
      walkinPatient: booking.walkinPatient || null,
      complaints: complaints || { text: "", attachments: [] },
      observations: observations || { text: "", attachments: [] },
      diagnoses: diagnoses || [],
      notes: notes || "",
      prescriptions: prescriptions || [],
      vitalSigns: vitalSigns || null,
      labOrders: labOrders || [],
      files: files || [],
      treatmentPlan: treatmentPlan || [],
      shareWithPatient: shareWithPatient || false,
    };

    if (clinicalRecord) {
      // Update existing record
      clinicalRecord = await ClinicalRecord.findByIdAndUpdate(
        clinicalRecord._id,
        recordData,
        { new: true }
      );
    } else {
      // Create new record
      clinicalRecord = new ClinicalRecord(recordData);
      await clinicalRecord.save();
    }

    res.json({
      message: "Clinical record saved successfully",
      clinicalRecord,
    });
  } catch (err) {
    console.error("Error saving clinical record:", err);
    res.status(500).json({ error: "Failed to save clinical record" });
  }
};

// Get clinical record for a booking
exports.getClinicalRecord = async (req, res) => {
  try {
    const { bookingId } = req.params;

    if (!mongoose.Types.ObjectId.isValid(bookingId)) {
      return res.status(400).json({ error: "Invalid booking ID" });
    }

    const clinicalRecord = await ClinicalRecord.findOne({
      booking: bookingId,
    }).populate("patient", "name").populate("walkinPatient", "name phoneNo");

    if (!clinicalRecord) {
      return res.status(404).json({ error: "Clinical record not found" });
    }

    res.json({ clinicalRecord });
  } catch (err) {
    console.error("Error fetching clinical record:", err);
    res.status(500).json({ error: "Failed to fetch clinical record" });
  }
};

// Upload prescription image
exports.uploadPrescriptionImage = async (req, res) => {
  try {
    const { bookingId } = req.params;
    const prescriptionIndex = req.body.prescriptionIndex;

    if (!req.file) {
      return res.status(400).json({ error: "No file uploaded" });
    }

    if (!mongoose.Types.ObjectId.isValid(bookingId)) {
      return res.status(400).json({ error: "Invalid booking ID" });
    }

    const booking = await Booking.findById(bookingId)
      .populate("patient")
      .populate("walkinPatient");

    if (!booking) {
      return res.status(404).json({ error: "Booking not found" });
    }

    // Upload to S3
    const fileKey = `clinical-records/${bookingId}/prescriptions/${Date.now()}-${req.file.originalname}`;
    const params = {
      Bucket: process.env.AWS_BUCKET_NAME,
      Key: fileKey,
      Body: req.file.buffer,
      ContentType: req.file.mimetype,
    };

    await s3.putObject(params).promise();
    const fileUrl = `https://${process.env.AWS_BUCKET_NAME}.s3.${process.env.AWS_REGION}.amazonaws.com/${fileKey}`;

    res.json({
      message: "Prescription image uploaded successfully",
      imageUrl: fileUrl,
    });
  } catch (err) {
    console.error("Error uploading prescription image:", err);
    res.status(500).json({ error: "Failed to upload prescription image" });
  }
};

// Upload file for clinical record
exports.uploadFile = async (req, res) => {
  try {
    const { bookingId } = req.params;

    if (!req.file) {
      return res.status(400).json({ error: "No file uploaded" });
    }

    if (!mongoose.Types.ObjectId.isValid(bookingId)) {
      return res.status(400).json({ error: "Invalid booking ID" });
    }

    const booking = await Booking.findById(bookingId)
      .populate("patient")
      .populate("walkinPatient");

    if (!booking) {
      return res.status(404).json({ error: "Booking not found" });
    }

    // Upload to S3
    const fileKey = `clinical-records/${bookingId}/files/${Date.now()}-${req.file.originalname}`;
    const params = {
      Bucket: process.env.AWS_BUCKET_NAME,
      Key: fileKey,
      Body: req.file.buffer,
      ContentType: req.file.mimetype,
    };

    await s3.putObject(params).promise();
    const fileUrl = `https://${process.env.AWS_BUCKET_NAME}.s3.${process.env.AWS_REGION}.amazonaws.com/${fileKey}`;

    // Determine file type
    const fileName = req.file.originalname;
    const ext = fileName.split(".").pop()?.toLowerCase();
    let fileType = "document";
    if (["jpg", "jpeg", "png", "gif", "bmp", "webp"].includes(ext)) {
      fileType = "image";
    } else if (["pdf", "doc", "docx", "txt", "xls", "xlsx"].includes(ext)) {
      fileType = "document";
    } else {
      fileType = "other";
    }

    const fileData = {
      url: fileUrl,
      fileName: fileName,
      fileType: fileType,
      uploadedAt: new Date(),
    };

    res.json({
      message: "File uploaded successfully",
      file: fileData,
    });
  } catch (err) {
    console.error("Error uploading file:", err);
    res.status(500).json({ error: "Failed to upload file" });
  }
};

// Create or update medical history
exports.createOrUpdateMedicalHistory = async (req, res) => {
  try {
    const { clinicId, patientId, walkinPatientId, condition, details } = req.body;

    if (!clinicId || !condition) {
      return res.status(400).json({ error: "Clinic ID and condition are required" });
    }

    if (!patientId && !walkinPatientId) {
      return res.status(400).json({ error: "Either patientId or walkinPatientId is required" });
    }

    if (!mongoose.Types.ObjectId.isValid(clinicId)) {
      return res.status(400).json({ error: "Invalid clinic ID" });
    }

    // Check if medical history already exists for this condition
    const query = {
      clinic: clinicId,
      condition: condition,
    };

    if (patientId) {
      query.patient = patientId;
    } else if (walkinPatientId) {
      query.walkinPatient = walkinPatientId;
    }

    let medicalHistory = await MedicalHistory.findOne(query);

    if (medicalHistory) {
      // Update existing record
      medicalHistory.details = details || "";
      await medicalHistory.save();
    } else {
      // Create new record
      medicalHistory = new MedicalHistory({
        clinic: clinicId,
        patient: patientId || null,
        walkinPatient: walkinPatientId || null,
        condition: condition,
        details: details || "",
      });
      await medicalHistory.save();
    }

    res.json({
      message: "Medical history saved successfully",
      medicalHistory,
    });
  } catch (err) {
    console.error("Error saving medical history:", err);
    res.status(500).json({ error: "Failed to save medical history" });
  }
};

// Get medical history for a patient
exports.getMedicalHistory = async (req, res) => {
  try {
    const { clinicId } = req.params;
    const { patientId, walkinPatientId } = req.query;

    if (!clinicId) {
      return res.status(400).json({ error: "Clinic ID is required" });
    }

    if (!patientId && !walkinPatientId) {
      return res.status(400).json({ error: "Either patientId or walkinPatientId is required" });
    }

    if (!mongoose.Types.ObjectId.isValid(clinicId)) {
      return res.status(400).json({ error: "Invalid clinic ID" });
    }

    const query = {
      clinic: clinicId,
    };

    if (patientId) {
      query.patient = patientId;
    } else if (walkinPatientId) {
      query.walkinPatient = walkinPatientId;
    }

    const medicalHistory = await MedicalHistory.find(query).sort({ createdAt: -1 });
    console.log(medicalHistory);

    res.json({
      medicalHistory,
    });
  } catch (err) {
    console.error("Error fetching medical history:", err);
    res.status(500).json({ error: "Failed to fetch medical history" });
  }
};

// Upload attachment for clinical record
exports.uploadClinicalAttachment = async (req, res) => {
  try {
    const { bookingId } = req.params;
    const { type, field } = req.body; // type: 'complaints' or 'observations', field: 'image' or 'file'

    if (!req.file) {
      return res.status(400).json({ error: "No file uploaded" });
    }

    if (!mongoose.Types.ObjectId.isValid(bookingId)) {
      return res.status(400).json({ error: "Invalid booking ID" });
    }

    const booking = await Booking.findById(bookingId)
      .populate("patient")
      .populate("walkinPatient");

    if (!booking) {
      return res.status(404).json({ error: "Booking not found" });
    }

    // Get patient info for folder structure
    const patientName =
      booking.patient?.name || booking.walkinPatient?.name || "unknown";
    const phoneNo =
      booking.patient?.phoneNo ||
      booking.walkinPatient?.phoneNo ||
      "0000000000";

    // Upload to S3
    const fileKey = `clinical-records/${bookingId}/${type}/${Date.now()}-${req.file.originalname}`;
    const params = {
      Bucket: process.env.AWS_BUCKET_NAME,
      Key: fileKey,
      Body: req.file.buffer,
      ContentType: req.file.mimetype,
    };

    // Use putObject for standard S3 upload
    await s3.putObject(params).promise();
    const fileUrl = `https://${process.env.AWS_BUCKET_NAME}.s3.${process.env.AWS_REGION}.amazonaws.com/${fileKey}`;

    // Find or create clinical record
    let clinicalRecord = await ClinicalRecord.findOne({ booking: bookingId });

    if (!clinicalRecord) {
      clinicalRecord = new ClinicalRecord({
        booking: bookingId,
        clinic: booking.clinic,
        patient: booking.patient || null,
        walkinPatient: booking.walkinPatient || null,
        complaints: { text: "", attachments: [] },
        observations: { text: "", attachments: [] },
        diagnoses: [],
        notes: "",
        shareWithPatient: false,
      });
    }

    // Add attachment to the appropriate field
    const attachment = {
      url: fileUrl,
      type: req.file.mimetype.startsWith("image/") ? "image" : "file",
      uploadedAt: new Date(),
    };

    if (type === "complaints") {
      clinicalRecord.complaints.attachments.push(attachment);
    } else if (type === "observations") {
      clinicalRecord.observations.attachments.push(attachment);
    }

    await clinicalRecord.save();

    res.json({
      message: "Attachment uploaded successfully",
      attachment,
      clinicalRecord,
    });
  } catch (err) {
    console.error("Error uploading attachment:", err);
    res.status(500).json({ error: "Failed to upload attachment" });
  }
};