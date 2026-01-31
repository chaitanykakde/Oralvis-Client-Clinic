const mongoose = require("mongoose");

const bookingSchema = new mongoose.Schema(
  {
    patient: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: false,
    },
    walkinPatient: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "WalkinPatient",
      default: null,
    },
    clinic: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Clinic",
      required: true,
    },
    appointmentDate: {
      type: Date,
      required: true,
    },
    slotTime: {
      type: String,
      required: true,
    },
    status: {
      type: String,
      enum: [
        "pending",
        "confirmed",
        "completed",
        "cancelled",
        "paid",
        "refund-requested",
        "refunded",
        "cancelled-no-refund",
      ],
      default: "pending",
    },
    notes: {
      type: String,
      default: "",
    },
    paymentId: {
      type: String,
      required: true,
    },
    refundId: {
      type: String,
      default: null,
    },
    refundStatus: {
      type: String,
      enum: ["none", "requested", "approved", "rejected"],
      default: "none",
    },
    refundRequestedAt: Date,
    refundApprovedAt: Date,
    refundRejectedAt: Date,
    amountPaid: {
      type: Number,
    },
    durationMinutes: {
      type: Number,
      default: 30,
    },
  },
  {
    timestamps: true,
  }
);

// Add index to speed up queries
bookingSchema.index({ patient: 1, clinic: 1, appointmentDate: 1, slotTime: 1 });
const Booking = mongoose.model("Booking", bookingSchema);

module.exports = Booking;
