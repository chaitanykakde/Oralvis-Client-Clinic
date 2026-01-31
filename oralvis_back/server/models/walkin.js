const mongoose = require("mongoose");

const walkinPatientSchema = new mongoose.Schema({
  name: { type: String, required: true },
  phoneNo: { type: String, required: true },
  patientId: { 
    type: String, 
    unique: true,
    sparse: true, // Allows null values but ensures uniqueness when present
  },
  email: { type: String, default: "" },
  abhaId: { type: String, default: "" },
  tokenNumber: { type: String, default: "" },
  clinic: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "Clinic",
    required: true,
  },
  createdAt: { type: Date, default: Date.now }
});

// Add index to speed up queries
walkinPatientSchema.index({ clinic: 1, phoneNo: 1 });

const WalkinPatient = mongoose.model("WalkinPatient", walkinPatientSchema);
module.exports = WalkinPatient;
