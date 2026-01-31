// controllers/dentistController.js
const Booking = require("../models/booking");
const User = require("../models/user");
const mongoose = require('mongoose');
const DentistProfile = require('../models/DentistProfile')
exports.getDashboardStats = async (req, res) => {
  try {
    const dentistId = req.user._id;

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const stats = await Booking.aggregate([
      {
        $match: { dentist: new mongoose.Types.ObjectId(dentistId) },
      },
      {
        $group: {
          _id: "$status",
          count: { $sum: 1 },
        },
      },
    ]);

    
    const statusCounts = stats.reduce(
      (acc, curr) => {
        acc[curr._id] = curr.count;
        return acc;
      },
      { pending: 0, confirmed: 0, completed: 0, cancelled: 0 }
    );

    const todaysAppointments = await Booking.find({
      dentist: dentistId,
      appointmentDate: { $gte: today, $lt: tomorrow },
      status: { $in: ["pending", "confirmed"] },
    })
      .populate("patient", "name phoneNo")
      .sort({ appointmentDate: 1 });


    const recentBookings = await Booking.find({ dentist: dentistId })
      .populate("patient", "name phoneNo")
      .sort({ createdAt: -1 })
      .limit(5);
      const uniquePatients = await Booking.distinct("patient", { dentist: dentistId });
      const totalPatients = uniquePatients.length;
      res.json({
        statusCounts,
        todaysAppointments,
        recentBookings,
        totalPatients,
        totalEarnings: 0 
      });
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: "Server error" });
  }
};
exports.confirmAppointment = async (req, res) => {
  try {
    const { bookingId } = req.params;

    const updated = await Booking.findByIdAndUpdate(
      bookingId,
      { status: 'completed' },
      { new: true }
    ).populate('patient', 'name phoneNo')
     .populate('dentist', 'name');

    if (!updated) {
      return res.status(404).json({ message: 'Booking not found' });
    }

    res.json({ message: 'Appointment marked as completed', booking: updated });
  } catch (error) {
    console.error('Error confirming appointment:', error);
    res.status(500).json({ message: 'Server error' });
  }
};

// @desc    Create or update dentist profile
// @route   POST /api/dentists/profile
// @access  Dentist
exports.upsertDentistProfile = async (req, res) => {
  try {
    const { qualifications, experience, clinics, bio, languages, profileImage } = req.body;
    const userId = req.user._id;

    const updated = await DentistProfile.findOneAndUpdate(
      { user: userId },
      {
        $set: {
          qualifications,
          experience,
          clinics,
          bio,
          languages,
          profileImage,
        },
      },
      { new: true, upsert: true, runValidators: true }
    ).populate("clinics", "name");

    res.status(200).json(updated);
  } catch (error) {
    console.error("Error updating dentist profile:", error);
    res.status(500).json({ message: "Failed to update profile" });
  }
};
exports.getDentistProfile = async (req, res) => {
  try {
    const profile = await DentistProfile.findOne({ user: req.user._id }).populate("clinics", "name address");

    if (!profile) {
      return res.status(404).json({ message: "Profile not found" });
    }

    res.status(200).json(profile);
  } catch (error) {
    console.error("Error fetching dentist profile:", error);
    res.status(500).json({ message: "Failed to fetch profile" });
  }
};
