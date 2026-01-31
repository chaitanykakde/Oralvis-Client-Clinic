const Booking = require("../models/booking");
const moment = require("moment");
const User = require("../models/user");
const sendCancelWhatsApp = require("../utils/sendCancelWhatsapp");
const razorpayInstance = require("../config/razorpay");
const crypto = require("crypto");
const Slot = require("../models/slot");
const sendBookingConfirmWhatsApp = require("../utils/sendBookingConfirmWhatsApp");
const sendClinicConfirm = require("../utils/sendClinicConfirm");
const { v4: uuidv4 } = require("uuid");
const redisClient = require("../config/redis");
const Clinic = require("../models/clinic");
exports.confirmWithoutPayment = async (req, res) => {
  const session = await Booking.startSession();
  session.startTransaction();

  try {
    const { user, phoneNo, date, time, notes, clinic } = req.body;

    // Require minimal identifiers; we'll fetch full docs server-side
    if (!user || !date || !time || !clinic) {
      return res.status(400).json({ error: "Missing required fields" });
    }
    // Fetch user and clinic to ensure valid references and contact info
    const [userDoc, clinicDoc] = await Promise.all([
      User.findById(user).select("name phoneNo"),
      Clinic.findById(clinic).select("name phoneNo"),
    ]);

    if (!userDoc || !clinicDoc) {
      return res.status(400).json({ error: "Invalid user or clinic" });
    }

    const normalizedPatientId = userDoc._id;
    const normalizedClinicId = clinicDoc._id;

    const booking = new Booking({
      patient: normalizedPatientId,
      appointmentDate: date, // stored as provided string (YYYY-MM-DD)
      slotTime: time,        // stored as provided string (HH:mm or label)
      notes,
      clinic: normalizedClinicId,
      paymentId: "Pay in Clinic",
      status: "confirmed",
    });

    const savedBooking = await booking.save({ session });
    const slotUpdate = await Slot.findOneAndUpdate(
      { clinic: normalizedClinicId, date, time },
      { isAvailable: false },
      { new: true, session }
    );
    if (!slotUpdate) {
      throw new Error("Failed to update slot availability");
    }
    const holdKey = `slot:hold:${normalizedClinicId}:${date}:${time}`;
    await redisClient.del(holdKey);
    req.io.emit("slotUpdated", { clinic: normalizedClinicId, date, time, isAvailable: false });
    await session.commitTransaction();
    session.endSession();

    res
      .status(201)
      .json({ message: "Booking confirmed", booking: savedBooking });
    // Fallback to user/clinic details from DB when request didn't include phone/name
    const patientPhone = phoneNo || userDoc.phoneNo;
    sendBookingConfirmWhatsApp(patientPhone, date, time, clinicDoc.name);
    sendClinicConfirm(
      clinicDoc.phoneNo,
      userDoc.name,
      date,
      time,
      "https://oralvis.com/"
    );
  } catch (error) {
    await session.abortTransaction();
    session.endSession();
    console.error("Error in confirm-without-payment:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

exports.createOrder = async (req, res) => {
  try {
    const { amount, currency } = req.body;

    const options = {
      amount: amount * 100,
      currency: currency || "INR",
      receipt: `receipt_${Date.now()}`,
    };
    const order = await razorpayInstance.orders.create(options);
    res.status(201).json(order);
  } catch (error) {
    console.error("Error in create-order:", error);
    res.status(500).json({ error: error.message });
  }
};

exports.verifyPayment = async (req, res) => {
  const session = await Booking.startSession();
  session.startTransaction();

  try {
    const {
      razorpay_payment_id,
      razorpay_order_id,
      razorpay_signature,
      bookingDetails,
    } = req.body;

    if (
      !bookingDetails ||
      !bookingDetails.date ||
      !bookingDetails.time ||
      !bookingDetails.user ||
      !bookingDetails.clinic
    ) {
      return res
        .status(400)
        .json({ error: "Missing required booking details" });
    }

    const appointmentDate = moment(bookingDetails.date, "YYYY-MM-DD").toDate();
    const slotTime = bookingDetails.time;

    const razorpaySecret = process.env.KEY_SECRET;

    const generatedSignature = crypto
      .createHmac("sha256", razorpaySecret)
      .update(razorpay_order_id + "|" + razorpay_payment_id)
      .digest("hex");

    if (generatedSignature !== razorpay_signature) {
      return res.status(400).json({ error: "Invalid payment signature" });
    }
    const payment = await razorpayInstance.payments.fetch(razorpay_payment_id);
    const amountInRupees = payment.amount / 100;
    const booking = new Booking({
      patient: bookingDetails.user,
      appointmentDate,
      slotTime,
      paymentId: razorpay_payment_id,
      orderId: razorpay_order_id,
      status: "paid",
      clinic: bookingDetails.clinic,
      amountPaid: amountInRupees,
      notes: bookingDetails.notes || "",
    });
    const savedBooking = await booking.save({ session });
    const slotUpdate = await Slot.findOneAndUpdate(
      {
        clinic: bookingDetails.clinic,
        date: bookingDetails.date,
        time: bookingDetails.time,
      },
      { isAvailable: false },
      { new: true, session }
    );
    if (!slotUpdate) {
      throw new Error("Failed to update slot availability");
    }
    const holdKey = `slot:hold:${bookingDetails.clinic}:${bookingDetails.date}:${bookingDetails.time}`;
    await redisClient.del(holdKey);

    req.io.emit("slotUpdated", {
      clinic: bookingDetails.clinic,
      date: bookingDetails.date,
      time: bookingDetails.time,
      isAvailable: false,
    });
    await session.commitTransaction();
    session.endSession();

    res
      .status(201)
      .json({ message: "Booking confirmed", booking: savedBooking });
  } catch (error) {
    await session.abortTransaction();
    session.endSession();
    console.error("Error in verify-payment:", error);
    res.status(500).json({ error: error.message });
  }
};

exports.processRefund = async (req, res) => {
  try {
    const { bookingId } = req.body;

    const booking = await Booking.findById(bookingId);
    if (!booking) {
      return res.status(404).json({ error: "Booking not found" });
    }
    if (!booking.paymentId) {
      return res
        .status(400)
        .json({ error: "No payment ID found for this booking" });
    }

    const refund = await razorpayInstance.payments.refund(booking.paymentId, {
      amount: 8 * 100,
    });
    booking.status = "cancelled";
    booking.refundId = refund.id;
    await booking.save();

    res.json({ message: "Refund processed successfully", refund });
  } catch (error) {
    console.error("Error processing refund:", error);
    res.status(500).json({ error: error.message });
  }
};

exports.cancelAppointment = async (req, res) => {
  try {
    const { bookingId, userId } = req.params;

    const booking = await Booking.findById(bookingId).populate("clinic");
    if (!booking) {
      return res.status(404).json({ error: "Booking not found" });
    }

    if (booking.patient.toString() !== userId) {
      return res.status(403).json({ error: "You can only cancel your own appointments" });
    }

    if (booking.status !== "confirmed" && booking.status !== "paid") {
      return res.status(400).json({ error: "Only confirmed or paid appointments can be cancelled" });
    }

    if (booking.status === "paid") {
      booking.status = "refund-requested";
      booking.refundStatus = "requested";
      booking.refundRequestedAt = new Date();
    } else if (booking.status === "confirmed") {
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
    const user = await User.findById(userId);
    if (!user) {
      return res.status(404).json({ error: "User not found" });
    }

    sendCancelWhatsApp(
      user.phoneNo,
      user.name,
      booking.appointmentDate.toISOString().split("T")[0],
      booking.slotTime,
      booking.clinic.name,
      "https://yourapp.com/rebook"
    );

    res.status(200).json({
      message:
        booking.status === "refund-requested"
          ? "Refund request submitted and pending admin approval"
          : "Appointment cancelled successfully",
      booking,
    });
  } catch (err) {
    console.error("Error cancelling appointment:", err);
    res.status(500).json({ error: "Failed to cancel appointment" });
  }
};



exports.getCancelledBookings = async (req, res) => {
  try {
    const userId = req.params.userId;

    const bookings = await Booking.find({
      patient: userId,
      status: "cancelled",
    })
      .populate({
        path: "clinic",
        select: "name image mainarea mainDoctor",
        populate: {
          path: "mainDoctor",
          select: "name",
        },
      })
      .sort({ appointmentDate: -1 });

    res.status(200).json(bookings);
  } catch (err) {
    console.error("Error fetching cancelled bookings:", err);
    res.status(500).json({ error: "Failed to fetch cancelled bookings" });
  }
};

exports.changeStatus = async (req, res) => {
  try {
    const { id } = req.params;
    const { status } = req.body;

    const booking = await Booking.findById(id);
    if (!booking) {
      return res.status(404).json({ error: "Booking not found" });
    }

    booking.status = status;
    await booking.save();

    res.status(200).json(booking);
  } catch (error) {
    console.error("Error in changeStatus:", error);
    res.status(500).json({ error: error.message });
  }
};

// Get Confirmed Bookings
exports.getConfirmedBookings = async (req, res) => {
  try {
    const bookings = await Booking.find({ status: "confirmed" }).populate(
      "patient dentist"
    );
    res.status(200).json(bookings);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

// Get Pending Bookings
exports.getPendingBookings = async (req, res) => {
  try {
    const bookings = await Booking.find({ status: "pending" }).populate(
      "patient dentist"
    );
    res.status(200).json(bookings);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

exports.getCancelledBookingsadmin = async (req, res) => {
  try {
    const bookings = await Booking.find({ status: "cancelled" }).populate(
      "patient dentist"
    );
    res.status(200).json(bookings);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

// Delete Booking
exports.deleteBooking = async (req, res) => {
  try {
    const { id } = req.params;

    const deletedBooking = await Booking.findByIdAndDelete(id);

    if (!deletedBooking) {
      return res.status(404).json({ error: "Booking not found" });
    }

    res.status(200).json({
      message: "Booking deleted successfully",
      booking: deletedBooking,
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

// Cancel Booking by ID
exports.cancelBookingById = async (req, res) => {
  try {
    const { id } = req.params;

    const booking = await Booking.findByIdAndUpdate(
      id,
      { status: "cancelled" },
      { new: true }
    );

    if (!booking) {
      return res.status(404).json({ error: "Booking not found" });
    }

    res
      .status(200)
      .json({ message: "Booking cancelled successfully", booking });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.createBookingSession = async (req, res) => {
  try {
    const { clinicId, date, time } = req.body;

    if (!clinicId || !date || !time) {
      return res.status(400).json({ error: "Missing required fields" });
    }

    // Fetch minimal clinic info
    const clinic = await Clinic.findById(clinicId)
      .select("name image mainarea mainDoctor fees")
      .lean();

    if (!clinic) {
      return res.status(404).json({ error: "Clinic not found" });
    }

    // Fetch doctor basic info
    const doctor = await User.findById(clinic.mainDoctor)
      .select("name image qualification")
      .lean();

    const sessionId = uuidv4();

    const sessionData = {
      clinic: {
        _id: clinic._id,
        name: clinic.name,
        image: clinic.image,
        mainarea: clinic.mainarea,
        fees: clinic.fees || 200,
        mainDoctor: doctor
          ? {
              name: doctor.name,
              image: doctor.image,
              qualification: doctor.qualification,
            }
          : null,
      },
      date,
      time,
      createdAt: new Date().toISOString(),
    };

    await redisClient.set(
      `booking:session:${sessionId}`,
      JSON.stringify(sessionData),
      "EX",
      300
    );
    const slotHeld = await redisClient.set(
      `slot:hold:${clinicId}:${date}:${time}`,
      "held",
      "EX",
      360,
      "NX" // <-- Ensures the key is only set if it doesn't exist
    );
    if (!slotHeld) {
      return res.status(409).json({
        error: "Slot already held by another user. Please pick another.",
      });
    }
    res.json({ sessionId });
  } catch (err) {
    console.error("Error creating booking session", err);
    res.status(500).json({ error: "Internal server error" });
  }
};

exports.getBookingSession = async (req, res) => {
  const { sessionId } = req.params;
  const key = `booking:session:${sessionId}`;

  try {
    const data = await redisClient.get(key);
    if (!data) return res.status(404).json({ error: "Session not found" });

    const sessionData = JSON.parse(data);
    const ttl = await redisClient.ttl(key);

    res.json({ ...sessionData, ttl });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Server error" });
  }
};

exports.cancelBookingSession = async (req, res) => {
  const { sessionId } = req.params;
  try {
    const sessionData = await redisClient.get(`booking:session:${sessionId}`);
    if (!sessionData)
      return res.status(404).json({ error: "Session not found" });

    const { clinic, date, time } = JSON.parse(sessionData);

    await redisClient.del(`booking:session:${sessionId}`);
    await redisClient.del(`slot:hold:${clinic._id}:${date}:${time}`);

    res.json({ message: "Session cancelled" });
  } catch (err) {
    console.error("Cancel session error:", err);
    res.status(500).json({ error: "Server error" });
  }
};
