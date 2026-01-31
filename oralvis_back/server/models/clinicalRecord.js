const mongoose = require("mongoose");

const clinicalRecordSchema = new mongoose.Schema(
  {
    booking: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Booking",
      required: true,
    },
    clinic: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Clinic",
      required: true,
    },
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
    complaints: {
      text: {
        type: String,
        default: "",
      },
      attachments: [
        {
          url: String,
          type: {
            type: String,
            enum: ["image", "file"],
            default: "image",
          },
          uploadedAt: {
            type: Date,
            default: Date.now,
          },
        },
      ],
    },
    observations: {
      text: {
        type: String,
        default: "",
      },
      attachments: [
        {
          url: String,
          type: {
            type: String,
            enum: ["image", "file"],
            default: "image",
          },
          uploadedAt: {
            type: Date,
            default: Date.now,
          },
        },
      ],
    },
    diagnoses: [
      {
        code: String,
        description: String,
        addedAt: {
          type: Date,
          default: Date.now,
        },
      },
    ],
    notes: {
      type: String,
      default: "",
    },
    prescriptions: [
      {
        medication: String,
        dosage: String,
        frequency: String,
        duration: String,
        instructions: String,
        images: [
          {
            url: String,
            uploadedAt: {
              type: Date,
              default: Date.now,
            },
          },
        ],
        addedAt: {
          type: Date,
          default: Date.now,
        },
      },
    ],
    vitalSigns: {
      weight: {
        value: String,
        unit: { type: String, default: "kg" },
      },
      bloodPressure: {
        systolic: String,
        diastolic: String,
        position: { type: String, default: "Standing" },
        unit: { type: String, default: "mmHg" },
      },
      pulse: {
        value: String,
        unit: { type: String, default: "beats/min" },
      },
      temperature: {
        value: String,
        method: { type: String, default: "Axillary (Armpit)" },
        unit: { type: String, default: "°C" },
      },
      respiratoryRate: {
        value: String,
        unit: { type: String, default: "breaths/min" },
      },
      recordedAt: {
        type: Date,
        default: Date.now,
      },
    },
    labOrders: [
      {
        labTest: String,
        instruction: String,
        addedAt: {
          type: Date,
          default: Date.now,
        },
      },
    ],
    files: [
      {
        url: String,
        fileName: String,
        fileType: {
          type: String,
          enum: ["image", "document", "other"],
          default: "document",
        },
        uploadedAt: {
          type: Date,
          default: Date.now,
        },
      },
    ],
    treatmentPlan: [
      {
        procedure: String,
        unit: { type: Number, default: 1 },
        cost: { type: Number, default: 0 },
        discount: { type: Number, default: 0 },
        total: { type: Number, default: 0 },
        addedAt: {
          type: Date,
          default: Date.now,
        },
      },
    ],
    shareWithPatient: {
      type: Boolean,
      default: false,
    },
    createdBy: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
    },
  },
  {
    timestamps: true,
  }
);

// Indexes for faster queries
clinicalRecordSchema.index({ booking: 1 });
clinicalRecordSchema.index({ clinic: 1 });
clinicalRecordSchema.index({ patient: 1 });

const ClinicalRecord = mongoose.model("ClinicalRecord", clinicalRecordSchema);

module.exports = ClinicalRecord;

