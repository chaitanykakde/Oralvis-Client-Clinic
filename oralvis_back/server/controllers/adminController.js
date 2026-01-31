// controllers/adminController.js
const Booking = require("../models/booking");
const Clinic = require("../models/clinic");
const User = require("../models/user");
// const mongoose = require("mongoose");
const bcrypt = require("bcryptjs");
const ClinicRegistrationRequest = require("../models/ClinicRegistrationRequest");
const sendClinicApprovalWhatsApp = require("../utils/clinicApprovalWhatsapp");
const razorpayInstance = require("../config/razorpay.js");
const  sendCancelWhatsApp  = require("../utils/sendCancelWhatsapp.js");
const Slot = require("../models/slot.js");
// Admin Get All Bookings
exports.getAllBookings = async (req, res) => {
  try {
    const {
      status,
      patientId,
      clinicId,
      date,
      page = 1,
      limit = 20,
    } = req.query;

    const filter = {};
    if (status) filter.status = status;
    if (patientId) filter.patient = patientId;
    if (clinicId) filter.clinic = clinicId;
    if (date) {
      const startDate = new Date(date);
      startDate.setHours(0, 0, 0, 0);
      const endDate = new Date(startDate);
      endDate.setDate(endDate.getDate() + 1);
      filter.appointmentDate = { $gte: startDate, $lt: endDate };
    }

    const bookingsRaw = await Booking.find(filter)
      .populate("patient", "name phoneNo")
      .populate("walkinPatient", "name phoneNo")
      .populate("clinic", "name")
      .sort({ appointmentDate: -1 })
      .skip((page - 1) * limit)
      .limit(Number(limit))
      .lean();

    // 🛠️ Unify patient fields
    const bookings = bookingsRaw.map((booking) => {
      if (!booking.patient && booking.walkinPatient) {
        booking.patient = booking.walkinPatient;
      }
      delete booking.walkinPatient;
      return booking;
    });

    const total = await Booking.countDocuments(filter);

    res.json({
      bookings,
      total,
      page: Number(page),
      totalPages: Math.ceil(total / limit),
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: "Server error" });
  }
};
// Admin Cancel Booking
exports.cancelBooking = async (req, res) => {
  try {
    const { bookingId } = req.params;
    const { user } = req.body;
    console.log("is it me ");
    const booking = await Booking.findById(bookingId)
      .populate("clinic")
      .populate("patient");

    if (!booking) {
      return res.status(404).json({ message: "Booking not found" });
    }
    if (booking.status !== "confirmed" && booking.status !== "paid") {
      return res.status(400).json({
        message: "Only confirmed or paid appointments can be cancelled",
      });
    }

    if (booking.status === "paid") {
      booking.status = "refund-requested";
      booking.refundStatus = "requested";
      booking.refundRequestedAt = new Date();
    } else {
      booking.status = "cancelled";
    }

    await booking.save();
    await Slot.findOneAndUpdate(
      {
        clinic: booking.clinic._id,
        date: booking.appointmentDate.toISOString().split("T")[0],
        time: booking.slotTime,
      },
      { isAvailable: true }
    );
    if (user) {
      sendCancelWhatsApp(
        user.phoneNo,
        user.name,
        booking.appointmentDate.toISOString().split("T")[0],
        booking.slotTime,
        booking.clinic.name,
        "https://yourapp.com/rebook"
      );
    }

    res.status(200).json({
      message:
        booking.status === "refund-requested"
          ? "Refund request submitted and pending admin approval"
          : "Appointment cancelled successfully",
      booking,
    });
  } catch (error) {
    console.error("Error cancelling appointment:", error);
    res.status(500).json({ message: "Server error" });
  }
};
// Admin Get All Patients
exports.getAllPatients = async (req, res) => {
  try {
    const patients = await User.find({ role: "patient" })
      .select("name email phoneNo lastLogin createdAt")
      .sort({ createdAt: -1 });

    res.json(patients);
  } catch (err) {
    console.error("Error fetching patients:", err);
    res.status(500).json({ error: "Failed to fetch patients" });
  }
};
// Admin Get Dashboard Stats
exports.getDashboardStats = async (req, res) => {
  try {
    const stats = await Booking.aggregate([
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

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const now = new Date();

    const todaysAppointmentsRaw = await Booking.find({
      appointmentDate: { $gte: today, $lt: tomorrow },
      status: { $in: ["pending", "confirmed"] },
    })
      .populate("patient", "name phoneNo")
      .populate("walkinPatient", "name phoneNo")
      .populate("clinic", "name")
      .sort({ appointmentDate: 1 });

    const todaysAppointments = todaysAppointmentsRaw.map((booking) => {
      const b = booking.toObject();

      // alias walkinPatient -> patient if normal patient doesn't exist
      if (!b.patient && b.walkinPatient) {
        b.patient = {
          name: b.walkinPatient.name,
          phoneNo: b.walkinPatient.phoneNo,
        };
      }

      // Fix time-based status adjustment
      if (b.status === "confirmed" && b.appointmentDate < now) {
        b.status = "pending";
      }

      return b;
    });

    const recentBookingsRaw = await Booking.find()
      .populate("patient", "name phoneNo")
      .populate("walkinPatient", "name phoneNo")
      .populate("clinic", "name")
      .sort({ createdAt: -1 })
      .limit(5);

    const recentBookings = recentBookingsRaw.map((booking) => {
      const b = booking.toObject();

      if (!b.patient && b.walkinPatient) {
        b.patient = {
          name: b.walkinPatient.name,
          phoneNo: b.walkinPatient.phoneNo,
        };
      }

      return b;
    });

    res.json({
      statusCounts,
      todaysAppointments,
      recentBookings,
      totalPatients: await User.countDocuments({ role: "patient" }),
      totalDentists: await User.countDocuments({ role: "dentist" }),
      totalClinics: await User.countDocuments({ role: "clinic" }),
      totalBookings: await Booking.countDocuments(),
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: "Server error" });
  }
};
// Admin Create Dentist
exports.createDentist = async (req, res) => {
  try {
    const { name, phoneNo, email, password, clinicIds, qualification, profileImage, image } = req.body;

    if (!qualification) {
      return res.status(400).json({ message: "Qualification is required" });
    }

    const existingByEmail = email ? await User.findOne({ email }) : null;
    if (existingByEmail) {
      return res.status(400).json({ message: "Email already exists" });
    }

    const existingByPhone = await User.findOne({ phoneNo });
    if (existingByPhone) {
      return res.status(400).json({ message: "Phone number already exists" });
    }

    const hashedPassword = await bcrypt.hash(password, 10);

    const dentist = new User({
      name,
      phoneNo,
      email,
      password: hashedPassword,
      role: "dentist",
      clinics: clinicIds,
      qualification,
      image: profileImage || image || "",
    });

    await dentist.save();

    if (Array.isArray(clinicIds) && clinicIds.length > 0) {
      await Clinic.updateMany(
        { _id: { $in: clinicIds } },
        { $addToSet: { dentists: dentist._id } }
      );
    }

    res.status(201).json({ message: "Dentist created successfully", dentist });
  } catch (err) {
    console.error("Error creating dentist:", err);
    res.status(500).json({ message: "Server error" });
  }
};

// Admin Update Dentist
exports.updateDentist = async (req, res) => {
  try {
    const { dentistId } = req.params;
    const {
      name,
      phoneNo,
      email,
      password,
      qualification,
      image,
      clinicIds,
    } = req.body;

    const dentist = await User.findById(dentistId);
    if (!dentist || dentist.role !== "dentist") {
      return res.status(404).json({ message: "Dentist not found" });
    }

    if (email) {
      const existing = await User.findOne({ email, _id: { $ne: dentistId } });
      if (existing) return res.status(400).json({ message: "Email already in use" });
    }

    if (phoneNo) {
      const existingPhone = await User.findOne({ phoneNo, _id: { $ne: dentistId } });
      if (existingPhone) return res.status(400).json({ message: "Phone already in use" });
    }

    if (typeof name !== "undefined") dentist.name = name;
    if (typeof phoneNo !== "undefined") dentist.phoneNo = phoneNo;
    if (typeof email !== "undefined") dentist.email = email;
    if (typeof qualification !== "undefined") dentist.qualification = qualification;
    if (typeof image !== "undefined") dentist.image = image;
    if (typeof password === "string" && password.trim()) {
      dentist.password = await bcrypt.hash(password, 10);
    }

    if (Array.isArray(clinicIds)) {
      dentist.clinics = clinicIds;
    }

    await dentist.save();

    res.json({ message: "Dentist updated successfully", dentist });
  } catch (error) {
    console.error("❌ Error updating dentist:", error);
    res.status(500).json({ message: "Failed to update dentist" });
  }
};

// Admin Delete Dentist
exports.deleteDentist = async (req, res) => {
  try {
    const { dentistId } = req.params;
    const dentist = await User.findById(dentistId);
    if (!dentist || dentist.role !== "dentist") {
      return res.status(404).json({ message: "Dentist not found" });
    }

    await User.findByIdAndDelete(dentistId);

    // Optionally, also remove dentist from any clinics arrays if stored there
    await Clinic.updateMany(
      { dentists: dentistId },
      { $pull: { dentists: dentistId } }
    );

    res.json({ message: "Dentist deleted successfully" });
  } catch (error) {
    console.error("❌ Error deleting dentist:", error);
    res.status(500).json({ message: "Failed to delete dentist" });
  }
};

// Admin Get Clinics With Bookings
exports.getClinicsWithBookings = async (req, res) => {
  try {
    const clinics = await Clinic.find();

    const clinicsWithStats = await Promise.all(
      clinics.map(async (clinic) => {
        const recentBookingsRaw = await Booking.find({ clinic: clinic._id })
          .populate("patient", "name email phoneNo")
          .populate("walkinPatient", "name phoneNo")
          .sort({ appointmentDate: -1 })
          .limit(2);

        const recentBookings = recentBookingsRaw.map((booking) => {
          const b = booking.toObject();
          if (!b.patient && b.walkinPatient) {
            b.patient = {
              name: b.walkinPatient.name,
              email: b.walkinPatient.email || "", // fallback if no email
            };
          }
          return b;
        });

        const statusCountsRaw = await Booking.aggregate([
          { $match: { clinic: clinic._id } },
          {
            $group: {
              _id: "$status",
              count: { $sum: 1 },
            },
          },
        ]);

        const statusCounts = {
          pending: 0,
          confirmed: 0,
          completed: 0,
          cancelled: 0,
          paid: 0,
        };

        statusCountsRaw.forEach(({ _id, count }) => {
          statusCounts[_id] = count;
        });

        return {
          clinic,
          recentBookings,
          statusCounts,
        };
      })
    );

    res.json(clinicsWithStats);
  } catch (err) {
    console.error("Error fetching clinics with bookings:", err);
    res.status(500).json({ error: "Failed to fetch data" });
  }
};
// Admin Delete User By Phone
exports.deleteUserByPhone = async (req, res) => {
  const { phone } = req.body;

  if (!phone) {
    return res.status(400).json({ error: "Phone number is required" });
  }

  try {
    const deletedUser = await User.findOneAndDelete({ phoneNo: phone });

    if (!deletedUser) {
      return res
        .status(404)
        .json({ error: "User not found with this phone number" });
    }

    res.json({ message: "User deleted successfully", user: deletedUser });
  } catch (err) {
    console.error("Error deleting user:", err);
    res.status(500).json({ error: "Server error" });
  }
};
// Admin Approve Clinic
exports.approveClinic = async (req, res) => {
  try {
    const { requestId } = req.params;

    const request = await ClinicRegistrationRequest.findById(requestId);
    if (!request) {
      return res.status(404).json({ error: "Request not found" });
    }

    if (request.status !== "pending") {
      return res.status(400).json({ error: "Request already processed" });
    }

    // If a user with same email or phone already exists, avoid duplicate
    const existingUser = await User.findOne({
      $or: [
        request.email ? { email: request.email } : {},
        request.phoneNo ? { phoneNo: request.phoneNo } : {},
      ],
    });

    if (!existingUser) {
      // Create new clinic user from request payload
      const user = new User({
        name: request.name,
        phoneNo: request.phoneNo,
        email: request.email,
        password: request.password,
        role: "clinic",
      });
      await user.save();
    }

    request.status = "approved";
    request.approvedAt = new Date();
    await request.save();
    await sendClinicApprovalWhatsApp(request.phoneNo, request.name);
    res.json({
      message: existingUser
        ? "Clinic request approved; user already existed, no new account created"
        : "Clinic request approved and user created",
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Internal server error" });
  }
};
// Admin Get All Requests
exports.getAllRequests = async (req, res) => {
  try {
    const requests = await ClinicRegistrationRequest.find({
      status: "pending",
    });
    res.json(requests);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Error retrieving requests" });
  }
};

// Admin Reject Clinic
exports.rejectClinic = async (req, res) => {
  try {
    const { requestId } = req.params;

    const request = await ClinicRegistrationRequest.findById(requestId);
    if (!request) {
      return res.status(404).json({ error: "Request not found" });
    }

    if (request.status !== "pending") {
      return res.status(400).json({ error: "Request already processed" });
    }

    request.status = "rejected";
    request.approvedAt = new Date();
    await request.save();

    res.json({ message: "Clinic request rejected" });
  } catch (error) {
    res.status(500).json({ error: "Internal server error" });
  }
};

// Admin Get All Clinics
exports.getAllClinics = async (req, res) => {
  try {
    // Get all users with role 'clinic'
    const clinicUsers = await User.find({ role: "clinic" })
      .select("name email phoneNo createdAt")
      .lean();

    // Fetch clinic data for each user
    const clinics = await Clinic.find({})
      .populate("mainDoctor", "name phoneNo")
      .lean();

    const enriched = clinicUsers.map((user) => {
      const clinic = clinics.find(
        (c) => c.owner?.toString() === user._id.toString()
      );
      return {
        userId: user._id,
        name: user.name,
        email: user.email,
        phoneNo: user.phoneNo,
        createdAt: user.createdAt,
        clinicId: clinic?._id || null,
        address: clinic?.mainarea || "",
        mainDoctor: clinic?.mainDoctor || null,
      };
    });
    res.json(enriched);
  } catch (error) {
    console.error("❌ Error fetching clinic users with linked clinics:", error);
    res.status(500).json({ message: "Internal server error" });
  }
};
// Admin Create Or Update Clinic
exports.createOrUpdateClinic = async (req, res) => {
  try {
    const { userId } = req.params; // The associated clinic user
    const clinicData = req.body;

    // Check if clinic already exists for this user
    let clinic = await Clinic.findOne({ mainDoctor: userId });

    if (clinic) {
      // Update existing clinic
      clinic = await Clinic.findOneAndUpdate(
        { mainDoctor: userId },
        clinicData,
        { new: true }
      );
      return res.json({ message: "Clinic updated successfully", clinic });
    } else {
      // Create new clinic
      const newClinic = new Clinic({
        ...clinicData,
        mainDoctor: userId,
      });
      await newClinic.save();
      return res
        .status(201)
        .json({ message: "Clinic created successfully", clinic: newClinic });
    }
  } catch (error) {
    console.error("Error in createOrUpdateClinic:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};
// Admin Assign Clinic Owner
exports.assignClinicOwner = async (req, res) => {
  const { clinicId, userId } = req.body; // Pass from frontend or route

  try {
    const user = await User.findById(userId);
    if (!user || user.role !== "clinic") {
      return res.status(400).json({ error: "Invalid clinic user ID" });
    }

    const clinic = await Clinic.findById(clinicId);
    if (!clinic) {
      return res.status(404).json({ error: "Clinic not found" });
    }

    clinic.owner = user._id;
    await clinic.save();

    res.json({ message: "Clinic owner assigned successfully", clinic });
  } catch (error) {
    console.error("❌ Error linking clinic to user:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};
// Admin Get Clinic Profile By User Id
exports.getClinicProfileByUserId = async (req, res) => {
  try {
    const { userId } = req.params;

    // Fetch user
    const user = await User.findById(userId);
    if (!user || user.role !== "clinic") {
      return res.status(404).json({ error: "Clinic user not found" });
    }

    // Fetch clinic
    const clinic = await Clinic.findOne({ owner: userId }).populate(
      "mainDoctor",
      "name phoneNo"
    );

    if (clinic) {
      // Return clinic details + user name, email, phoneNo
      return res.json({
        exists: true,
        clinic: {
          ...clinic.toObject(),
          name: user.name,
          phoneNo: user.phoneNo,
          email: user.email,
        },
      });
    } else {
      // Return empty clinic + user fields
      return res.json({
        exists: false,
        clinic: {
          name: user.name || "",
          email: user.email || "",
          phoneNo: user.phoneNo || "",
          mainarea: "",
          introline: "",
          address: "",
          image: "",
          coverimage: "",
          noofpatients: null,
          yearsofexp: null,
          sterlizedequipmentpercentage: null,
          coverVideo: "",
          services: [],
          dentists: [],
          mainDoctor: null,
          fees: 0,
          about: { parah: "", points_to_be_highlighted: [] },
          location: { coordinates: [] },
          patient_experiences: [],
          owner: userId,
        },
      });
    }
  } catch (error) {
    console.error("❌ Error getting clinic profile:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

// Admin Create Clinic 
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

// Admin Update Clinic from here we are doing the update clinic
exports.updateClinic = async (req, res) => {
  try {
    const { clinicId } = req.params;

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
      mainDoctor,
      fees,
      about,
      location,
      patient_experiences,
    } = req.body;

    const clinic = await Clinic.findById(clinicId);
    if (!clinic) {
      return res.status(404).json({ message: "Clinic not found" });
    }

    // Update fields
    clinic.name = name || clinic.name;
    clinic.mainarea = mainarea || clinic.mainarea;
    clinic.introline = introline || clinic.introline;
    clinic.address = address || clinic.address;
    clinic.phoneNo = phoneNo || clinic.phoneNo;
    clinic.image = image || clinic.image;
    clinic.coverimage = coverimage || clinic.coverimage;
    clinic.noofpatients = noofpatients ?? clinic.noofpatients;
    clinic.yearsofexp = yearsofexp ?? clinic.yearsofexp;
    clinic.sterlizedequipmentpercentage =
      sterlizedequipmentpercentage ?? clinic.sterlizedequipmentpercentage;
    clinic.coverVideo = coverVideo || clinic.coverVideo;
    clinic.services = services || clinic.services;
    clinic.mainDoctor = mainDoctor || clinic.mainDoctor;
    clinic.fees = fees ?? clinic.fees;
    clinic.about = about || clinic.about;
    clinic.location = location || clinic.location;
    clinic.patient_experiences =
      patient_experiences || clinic.patient_experiences;

    if (Array.isArray(dentistIds)) {
      clinic.dentists = dentistIds;

      await User.updateMany(
        { _id: { $in: dentistIds } },
        { $addToSet: { clinics: clinic._id } }
      );
    }

    await clinic.save();

    res.status(200).json({ message: "Clinic updated successfully", clinic });
  } catch (err) {
    console.error("❌ Error updating clinic:", err);
    res.status(500).json({ message: "Failed to update clinic" });
  }
};

// Admin Delete Clinic
exports.deleteClinic = async (req, res) => {
  try {
    const { clinicId } = req.params;

    const clinic = await Clinic.findByIdAndDelete(clinicId);
    if (!clinic) {
      return res.status(404).json({ message: "Clinic not found" });
    }

    // Remove clinic reference from dentists
    await User.updateMany(
      { clinics: clinicId },
      { $pull: { clinics: clinicId } }
    );

    res.json({ message: "Clinic deleted successfully" });
  } catch (error) {
    console.error("❌ Error deleting clinic:", error);
    res.status(500).json({ message: "Failed to delete clinic" });
  }
};

// Admin Create Clinic With User
exports.adminCreateClinicWithUser = async (req, res) => {
  try {
    const {
      name,
      email,
      phoneNo,
      password,
      mainarea,
      introline,
      address,
      image,
      coverimage,
      noofpatients,
      yearsofexp,
      sterlizedequipmentpercentage,
      coverVideo,
      services,
      dentistIds,
      mainDoctor,
      fees,
      about,
      location,
      patient_experiences,
    } = req.body;

    // Validate required fields
    if (!name || !email || !phoneNo || !password || !mainarea) {
      return res.status(400).json({ message: "Missing required fields" });
    }

    // Check if user already exists
    const existing = await User.findOne({ $or: [{ email }, { phoneNo }] });
    if (existing) {
      return res
        .status(400)
        .json({ message: "User with this email or phone already exists" });
    }

    // Hash password
    const hashedPassword = await bcrypt.hash(password, 10);

    // Create user
    const newUser = await User.create({
      name,
      email,
      phoneNo,
      password: hashedPassword,
      role: "clinic",
    });
  

    // Create clinic
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
      mainDoctor,
      fees,
      about,
      location,
      patient_experiences,
      owner: newUser._id,
    });

    await clinic.save();

    // Link clinic to dentists
    if (Array.isArray(dentistIds) && dentistIds.length > 0) {
      await User.updateMany(
        { _id: { $in: dentistIds } },
        { $addToSet: { clinics: clinic._id } }
      );
    }

    res
      .status(201)
      .json({ message: "Clinic and user created successfully", clinic });
  } catch (error) {
    console.error("❌ Error creating clinic:", error);
    res.status(500).json({ message: "Server error" });
  }
};

// Get All Clinics Name

exports.getAllClinicsName = async (req, res) => {
  try {
    const clinics = await Clinic.find({}, "name _id");
    res.json(clinics);
  } catch (err) {
    console.error("❌ Error fetching clinics:", err);
    res.status(500).json({ message: "Internal server error" });
  }
};
exports.getAllRefundRequests = async (req, res) => {
  try {
    const refunds = await Booking.find({ refundStatus: "requested" })
      .populate("patient", "name phoneNo")
      .populate("clinic", "name");
    res.json(refunds);
  } catch (error) {
    console.error("Error fetching refunds:", error);
    res.status(500).json({ error: "Failed to fetch refund requests" });
  }
};
// approve Refund
exports.approveRefund = async (req, res) => {
  try {
    const { bookingId } = req.params;
    const booking = await Booking.findById(bookingId).populate("clinic");

    if (!booking || booking.refundStatus !== "requested") {
      return res.status(400).json({ error: "Invalid refund request" });
    }

    if (!booking.paymentId) {
      return res.status(400).json({ error: "No payment ID found for refund" });
    }

    const refund = await razorpayInstance.payments.refund(booking.paymentId, {
      amount: booking.amountPaid * 100,
    });

    booking.status = "refunded";
    booking.refundStatus = "approved";
    booking.refundId = refund.id;
    booking.refundApprovedAt = new Date();
    await booking.save();

    res.json({ message: "Refund approved and processed", booking });
  } catch (error) {
    console.error("Error approving refund:", error);
    res.status(500).json({ error: "Failed to approve refund" });
  }
};

// Get Refunds By Type
exports.getRefundsByType = async (req, res) => {
  try {
    const { type } = req.query;

    let statusFilter;

    switch (type) {
      case "requested":
        statusFilter = "refund-requested";
        break;
      case "completed":
        statusFilter = "refunded";
        break;
      case "rejected":
        statusFilter = "cancelled-no-refund";
        break;
      default:
        return res.status(400).json({ error: "Invalid refund type" });
    }

    const bookings = await Booking.find({ status: statusFilter })
      .populate("patient", "name phoneNo")
      .populate("clinic", "name");

    res.json(bookings);
  } catch (error) {
    console.error("Error fetching refunds:", error);
    res.status(500).json({ error: "Failed to fetch refunds" });
  }
};

// Reject Refund

exports.rejectRefund = async (req, res) => {
  try {
    const { bookingId } = req.params;
    const { refundNote } = req.body;

    const booking = await Booking.findById(bookingId).populate("clinic");

    if (!booking || booking.refundStatus !== "requested") {
      return res.status(400).json({ error: "Invalid refund request" });
    }

    booking.status = "cancelled-no-refund";
    booking.refundStatus = "rejected";
    booking.refundRejectedAt = new Date();
    if (refundNote) booking.refundNote = refundNote;

    await booking.save();

    res.json({ message: "Refund request rejected", booking });
  } catch (error) {
    console.error("Error rejecting refund:", error);
    res.status(500).json({ error: "Failed to reject refund" });
  }
};

// Super Admin Create Admin
exports.createAdmin = async (req, res) => {
  try {
    const { name, email, phoneNo, password, adminType } = req.body;

    // Validate required fields
    if (!name || !email || !phoneNo || !password || !adminType) {
      return res.status(400).json({ message: "All fields are required" });
    }

    // Validate admin type
    const validAdminTypes = ["report_approval_admin", "monitoring_admin"];
    if (!validAdminTypes.includes(adminType)) {
      return res.status(400).json({ message: "Invalid admin type" });
    }

    // Check if user already exists
    const existingUser = await User.findOne({
      $or: [{ email }, { phoneNo }]
    });

    if (existingUser) {
      return res.status(400).json({ message: "User with this email or phone already exists" });
    }

    // Hash password
    const hashedPassword = await bcrypt.hash(password, 10);

    // Create admin user
    const admin = new User({
      name,
      email,
      phoneNo,
      password: hashedPassword,
      role: adminType,
      adminType
    });

    await admin.save();

    res.status(201).json({ 
      message: "Admin created successfully", 
      admin: {
        id: admin._id,
        name: admin.name,
        email: admin.email,
        phoneNo: admin.phoneNo,
        role: admin.role,
        adminType: admin.adminType
      }
    });
  } catch (error) {
    console.error("Error creating admin:", error);
    res.status(500).json({ message: "Server error" });
  }
};

// Super Admin Get All Admins
exports.getAllAdmins = async (req, res) => {
  try {
    const admins = await User.find({
      role: { $in: ["super_admin", "report_approval_admin", "monitoring_admin"] }
    }).select("name email phoneNo role adminType createdAt lastLogin");

    res.json(admins);
  } catch (error) {
    console.error("Error fetching admins:", error);
    res.status(500).json({ message: "Server error" });
  }
};

// Super Admin Update Admin
exports.updateAdmin = async (req, res) => {
  try {
    const { adminId } = req.params;
    const { name, email, phoneNo, password, adminType } = req.body;

    const admin = await User.findById(adminId);
    if (!admin || !["super_admin", "report_approval_admin", "monitoring_admin"].includes(admin.role)) {
      return res.status(404).json({ message: "Admin not found" });
    }

    // Prevent updating super admin
    if (admin.role === "super_admin") {
      return res.status(403).json({ message: "Cannot update super admin" });
    }

    // Check for duplicate email/phone
    if (email) {
      const existing = await User.findOne({ email, _id: { $ne: adminId } });
      if (existing) return res.status(400).json({ message: "Email already in use" });
    }

    if (phoneNo) {
      const existing = await User.findOne({ phoneNo, _id: { $ne: adminId } });
      if (existing) return res.status(400).json({ message: "Phone already in use" });
    }

    // Update fields
    if (name) admin.name = name;
    if (email) admin.email = email;
    if (phoneNo) admin.phoneNo = phoneNo;
    if (adminType && ["report_approval_admin", "monitoring_admin"].includes(adminType)) {
      admin.role = adminType;
      admin.adminType = adminType;
    }
    if (password && password.trim()) {
      admin.password = await bcrypt.hash(password, 10);
    }

    await admin.save();

    res.json({ message: "Admin updated successfully", admin });
  } catch (error) {
    console.error("Error updating admin:", error);
    res.status(500).json({ message: "Server error" });
  }
};

// Super Admin Delete Admin
exports.deleteAdmin = async (req, res) => {
  try {
    const { adminId } = req.params;

    const admin = await User.findById(adminId);
    if (!admin || !["super_admin", "report_approval_admin", "monitoring_admin"].includes(admin.role)) {
      return res.status(404).json({ message: "Admin not found" });
    }

    // Prevent deleting super admin
    if (admin.role === "super_admin") {
      return res.status(403).json({ message: "Cannot delete super admin" });
    }

    await User.findByIdAndDelete(adminId);

    res.json({ message: "Admin deleted successfully" });
  } catch (error) {
    console.error("Error deleting admin:", error);
    res.status(500).json({ message: "Server error" });
  }
};

// Report Approval Admin - Get All Reports
exports.getAllReports = async (req, res) => {
  try {
    const Report = require("../models/report");
    const User = require("../models/user");
    const assignReportRoundRobin = require("../utils/assignReportRoundRobin");
    const checkReportLoadAndNotify = require("../utils/checkReportLoadAndNotify");
    
    const currentUser = req.user;
    const isSuperAdmin = currentUser.role === "super_admin";

    // Get all reports
    let reports = await Report.find()
      .populate("patientId", "name phoneNo")
      .populate("assignedTo", "name email")
      .sort({ createdAt: -1 });

    // Assign unassigned pending reports using round-robin
    const unassignedPendingReports = reports.filter(
      (r) => r.status === "PendingReview" && !r.assignedTo
    );

    if (unassignedPendingReports.length > 0) {
      console.log(
        `🔄 Assigning ${unassignedPendingReports.length} unassigned pending reports using round-robin`
      );

      for (const report of unassignedPendingReports) {
        const assignedAdminId = await assignReportRoundRobin();
        if (assignedAdminId) {
          report.assignedTo = assignedAdminId;
          report.assignedAt = new Date();
          await report.save();

          // After assigning, check today's pending count for that admin
          // and send a warning email if it exceeds the threshold.
          await checkReportLoadAndNotify(assignedAdminId);
        }
      }

      // Refresh reports after assignment
      reports = await Report.find()
        .populate("patientId", "name phoneNo")
        .populate("assignedTo", "name email")
        .sort({ createdAt: -1 });
    }

    // Filter reports based on user role
    if (!isSuperAdmin) {
      // Report approval admins only see reports assigned to them
      reports = reports.filter((r) => {
        if (!r.assignedTo) return false;
        const assignedId = r.assignedTo._id 
          ? r.assignedTo._id.toString() 
          : r.assignedTo.toString();
        return assignedId === currentUser._id.toString();
      });
    }

    // Categorize reports into pending and approved
    const pendingReports = reports.filter(
      (r) => r.status === "PendingReview"
    );
    const approvedReports = reports.filter((r) => r.status === "Approved");

    res.json({
      reports,
      pending: pendingReports,
      approved: approvedReports,
    });
  } catch (error) {
    console.error("Error fetching reports:", error);
    res.status(500).json({ message: "Server error" });
  }
};

// Report Approval Admin - Approve Report
exports.approveReport = async (req, res) => {
  try {
    console.log("🚀 [APPROVE] Starting report approval process");
    const { reportId } = req.params;
    console.log("📋 [APPROVE] Report ID:", reportId);
    console.log("👤 [APPROVE] Approving user:", req.user._id, req.user.role);
    
    const Report = require("../models/report");
    const generateReportPDF = require("../utils/generateReportPDF");
    const uploadPdfToS3 = require("../utils/uploadPdfToS3");
    const sendEmail = require("../utils/sendEmail");
    const sendPdfWhatsapp = require("../utils/sendPdfWhatsapp");
    
    const report = await Report.findById(reportId).populate("patientId");
    if (!report) {
      console.error("❌ [APPROVE] Report not found:", reportId);
      return res.status(404).json({ message: "Report not found" });
    }

    console.log("✅ [APPROVE] Report found:", report._id);
    console.log("👤 [APPROVE] Patient info:", {
      id: report.patientId?._id,
      name: report.patientId?.name,
      phoneNo: report.patientId?.phoneNo,
      email: report.patientId?.email
    });

    // Update report status
    report.status = "Approved";
    report.approvedAt = new Date();
    report.approvedBy = req.user._id;
    
    console.log("📄 [APPROVE] Generating PDF...");
    const pdfBuffer = await generateReportPDF(report);
    console.log("✅ [APPROVE] PDF generated, size:", pdfBuffer.length, "bytes");

    const firstName = report.patientId?.name?.split(" ")[0] || "Patient";
    const fileName = `Report_${firstName}_${Date.now()}.pdf`;
    console.log("📁 [APPROVE] File name:", fileName);

    console.log("☁️ [APPROVE] Uploading PDF to S3...");
    const pdfUrl = await uploadPdfToS3(pdfBuffer, fileName);
    console.log("✅ [APPROVE] PDF uploaded to S3:", pdfUrl);

    report.pdfUrl = pdfUrl;
    await report.save();
    console.log("💾 [APPROVE] Report saved with PDF URL");

    // Send email
    if (report.patientId?.email) {
      console.log("📧 [APPROVE] Sending email to:", report.patientId.email);
      try {
        await sendEmail({
          to: report.patientId.email,
          subject: "Your Dental Report from OralVis",
          text: "Please find your attached dental report.",
          attachments: [
            {
              filename: `OralVis_Report_${report._id}.pdf`,
              content: pdfBuffer,
              contentType: "application/pdf",
            },
          ],
        });
        console.log("✅ [APPROVE] Email sent successfully");
      } catch (emailError) {
        console.error("⚠️ [APPROVE] Email send failed:", emailError);
        // Don't fail the whole request if email fails
      }
    } else {
      console.log("⚠️ [APPROVE] No email address for patient");
    }

    // Send WhatsApp
    if (report.patientId?.phoneNo) {
      console.log("📱 [APPROVE] Sending WhatsApp to:", report.patientId.phoneNo);
      console.log("🔗 [APPROVE] PDF URL for WhatsApp:", pdfUrl);
      try {
        await sendPdfWhatsapp(report.patientId.phoneNo, pdfUrl);
        console.log("✅ [APPROVE] WhatsApp sent successfully");
      } catch (whatsappError) {
        console.error("❌ [APPROVE] WhatsApp send failed:", whatsappError);
        console.error("❌ [APPROVE] WhatsApp error details:", {
          message: whatsappError.message,
          code: whatsappError.code,
          status: whatsappError.status
        });
        // Don't fail the whole request if WhatsApp fails
      }
    } else {
      console.error("❌ [APPROVE] No phone number for patient");
    }

    console.log("✅ [APPROVE] Report approval completed successfully");
    res.json({ message: "Report approved and sent", report, pdfUrl });
  } catch (error) {
    console.error("❌ [APPROVE] Error approving report:", error);
    console.error("❌ [APPROVE] Error stack:", error.stack);
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

// Report Approval Admin - Reject Report
exports.rejectReport = async (req, res) => {
  try {
    const { reportId } = req.params;
    const { reason } = req.body;
    const Report = require("../models/report");
    
    const report = await Report.findById(reportId);
    if (!report) {
      return res.status(404).json({ message: "Report not found" });
    }

    report.status = "Rejected";
    report.rejectedAt = new Date();
    report.rejectedBy = req.user._id;
    if (reason) report.rejectionReason = reason;
    
    await report.save();

    res.json({ message: "Report rejected successfully", report });
  } catch (error) {
    console.error("Error rejecting report:", error);
    res.status(500).json({ message: "Server error" });
  }
};
