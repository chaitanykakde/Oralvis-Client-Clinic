const mongoose = require("mongoose");

const patientSchema = new mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
      unique: true,
    },
    address: {
      type: String,
      default: "",
    },
    age: {
      type: Number,
    },
    gender: {
      type: String,
      enum: ["male", "female", "other"],
    },
    imageUrl: {
        type: String,
        default: "",
      },
   
  },
  { timestamps: true }
);

module.exports = mongoose.model("Patient", patientSchema);