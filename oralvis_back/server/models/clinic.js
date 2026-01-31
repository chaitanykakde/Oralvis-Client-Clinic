const mongoose = require("mongoose");

const clinicSchema = new mongoose.Schema({
  name: {
    type: String,
    required: true,
  },
  mainarea: String,
  introline: String,
  address: String,
  phoneNo: String,
  image: {
    type: String,
    default: "",
  },
  coverimage: {
    type: String,
    default: "",
  },
  noofpatients: Number,
  yearsofexp: Number,
  sterlizedequipmentpercentage: {
    type: Number,
    min: 0,
    max: 100,
  },
  coverVideo: {
    type: String,
    default: "",
  },
  services: [
    {
      type: String,
    },
  ],
  dentists: [
    {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
    },
  ],
  mainDoctor: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
  },
  city: {
    type: String,
    required: true,
  },
  fees: {
    type: Number,
    required: true,
    default: 0,
  },
  about: {
    parah: {
      type: String,
      default: "",
    },
    points_to_be_highlighted: {
      type: [String],
      default: [],
    },
  },
  location: {
    type: {
      type: String,
      enum: ["Point"],
      default: "Point",
    },
    coordinates: {
      type: [Number],
      required: true,
    },
  },
  patient_experiences: [
    {
      review_giver: String,
      review: String,
      rating: {
        type: Number,
        min: 1,
        max: 5,
      },
    },
  ],
  owner: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
  },
});

// Add index to speed up queries
clinicSchema.index({ owner: 1 });
clinicSchema.index({ name: 1 });
clinicSchema.index({ address: 1 });
clinicSchema.index({ city: 1 });

clinicSchema.index({ location: "2dsphere" });

module.exports = mongoose.model("Clinic", clinicSchema);
