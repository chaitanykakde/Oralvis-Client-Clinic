const mongoose = require("mongoose");

const slotSchema = new mongoose.Schema(
  {
    clinic: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Clinic",
      required: true,
    },
    date: {
      type: String,
      required: true,
    },
    time: {
      type: String,
      required: true,
    },
    isAvailable: {
      type: Boolean,
      default: true,
    },
  },
  {
    timestamps: true,
  }
);

// Add index to speed up queries
slotSchema.index({ clinic: 1, date: 1, time: 1 });
const Slot = mongoose.model("Slot", slotSchema);

module.exports = Slot;
