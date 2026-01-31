const mongoose = require("mongoose");

const DentistProfileSchema = new mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
      unique: true,
    },
    qualifications: {
      type: String,
      required: true,
    },
    experience: {
      type: String,
      required: true,
    },
    clinics: [
      {
        type: mongoose.Schema.Types.ObjectId,
        ref: "Clinic",
      },
    ],
    bio: {
      type: String,
    },
    languages: {
      type: [String],
      default: [],
    },
    profileImage: {
      type: String,
    },
  },
  {
    timestamps: true,
  }
);

// Index is automatically created by unique: true on user field
module.exports = mongoose.model("DentistProfile", DentistProfileSchema);
