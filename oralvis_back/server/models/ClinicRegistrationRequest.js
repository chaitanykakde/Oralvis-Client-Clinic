const mongoose = require("mongoose");

const ClinicRegistrationRequestSchema = new mongoose.Schema({
  name: { type: String, required: true },
  phoneNo: { type: String, required: true },
  email: { type: String, required: true },
  password: { type: String, required: true }, // Will be hashed
  status: {
    type: String,
    enum: ["pending", "approved", "rejected"],
    default: "pending",
  },
  submittedAt: { type: Date, default: Date.now },
  approvedAt: { type: Date },
});

module.exports = mongoose.model("ClinicRegistrationRequest", ClinicRegistrationRequestSchema);
