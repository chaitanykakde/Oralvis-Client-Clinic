const mongoose = require("mongoose");

const userSchema = new mongoose.Schema(
  {
    name: {
      type: String,
      required: true,
    },
    phoneNo: {
      type: String,
      required: true,
      unique: true,
    },
    email: {
      type: String,
      required: false,
      lowercase: true,
      trim: true,
    },
    password: {
      type: String,
      required: true,
    },
    role: {
      type: String,
      enum: ["patient", "dentist", "admin", "clinic", "super_admin", "report_approval_admin", "monitoring_admin"],
      default: "patient",
      required: true,
    },
    adminType: {
      type: String,
      enum: ["super_admin", "report_approval_admin", "monitoring_admin"],
      required: function() {
        return ["admin", "super_admin", "report_approval_admin", "monitoring_admin"].includes(this.role);
      }
    },
    clinics: [{ type: mongoose.Schema.Types.ObjectId, ref: "Clinic" }],
    qualification: {
      type: String,
      required: function () {
        return this.role === "dentist";
      },
    },
    image: {
      type: String,
      default: "",
    },
    lastLogin: {
      type: Date,
      default: null,
    },
    patientId: {
      type: String,
      unique: true,
      sparse: true, // Allows null values but ensures uniqueness when present
    },
  },
  {
    timestamps: true,
  }
);

// Add index to speed up queries
userSchema.index({ email: 1 }, { unique: true });
// phoneNo index is automatically created by unique: true on phoneNo field



const User = mongoose.model("User", userSchema);

module.exports = User;
