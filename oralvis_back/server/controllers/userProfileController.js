const Booking = require("../models/booking");
const moment = require("moment");

exports.getUpcomingBookings = async (req, res) => {
  try {
    const userId = req.params.userId;

    // Fetch all bookings for this patient, then derive upcoming in code
    // This avoids any timezone edge cases.
    const allBookings = await Booking.find({
      patient: userId,
    })
      .select("appointmentDate slotTime status clinic")
      .populate({
        path: "clinic",
        select: "name image mainarea mainDoctor",
        populate: {
          path: "mainDoctor",
          select: "name image",
        },
      })
      .sort({ appointmentDate: 1 });

    const startOfToday = moment().startOf("day");

    const upcoming = allBookings.filter((b) =>
      moment(b.appointmentDate).isSameOrAfter(startOfToday)
    );

    res.status(200).json(upcoming);
  } catch (err) {
    console.error("Error fetching upcoming bookings:", err);
    res.status(500).json({ error: "Failed to fetch upcoming bookings" });
  }
};

exports.getPastBookings = async (req, res) => {
  try {
    const userId = req.params.userId;

    // Fetch all bookings for this patient, then derive past in code
    const allBookings = await Booking.find({
      patient: userId,
    })
      .select("appointmentDate slotTime status clinic")
      .populate({
        path: "clinic",
        select: "name image mainarea mainDoctor",
        populate: {
          path: "mainDoctor",
          select: "name image",
        },
      })
      .sort({ appointmentDate: -1 });

    const startOfToday = moment().startOf("day");

    const past = allBookings.filter((b) =>
      moment(b.appointmentDate).isBefore(startOfToday)
    );

    res.status(200).json(past);
  } catch (err) {
    console.error("Error fetching past bookings:", err);
    res.status(500).json({ error: "Failed to fetch past bookings" });
  }
};
