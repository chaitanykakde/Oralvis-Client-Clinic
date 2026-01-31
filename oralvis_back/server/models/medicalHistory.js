const mongoose = require("mongoose");

const medicalHistorySchema = new mongoose.Schema(
  {
    patient: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: false,
    },
    walkinPatient: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "WalkinPatient",
      required: false,
    },
    clinic: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Clinic",
      required: true,
    },
    condition: {
      type: String,
      required: true,
    },
    details: {
      type: String,
      default: "",
    },
    addedBy: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
    },
  },
  {
    timestamps: true,
  }
);

// Indexes for faster queries
medicalHistorySchema.index({ patient: 1, clinic: 1 });
medicalHistorySchema.index({ walkinPatient: 1, clinic: 1 });
medicalHistorySchema.index({ clinic: 1 });

const MedicalHistory = mongoose.model("MedicalHistory", medicalHistorySchema);

module.exports = MedicalHistory;

