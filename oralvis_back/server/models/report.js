const mongoose = require("mongoose");
const annotationSchema = new mongoose.Schema(
  {
    points: [
      {
        x: Number,
        y: Number,
      },
    ],
    color: { type: String, default: "#ff0000" },
    label: { type: String, default: "" },
  },
  { _id: false }
);

const diagnosisResultSchema = new mongoose.Schema(
  {
    imageUrl: String,
    diagnosis: String,
    annotations: [annotationSchema],
  },
  { _id: false }
);

const aiDiagnosisSchema = new mongoose.Schema(
  {
    results: [diagnosisResultSchema],
    modifiedByDentist: Boolean,
    modifiedAt: Date,
  },
  { _id: false }
);

const reportSchema = new mongoose.Schema({
  patientId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
    required: true,
  },
  clinicId: { type: mongoose.Schema.Types.ObjectId, ref: "Clinic" },
  images: [String],
  aiDiagnosis: aiDiagnosisSchema,

  dentistComment: { type: String },
  commentedAt: { type: Date },

  status: {
    type: String,
    enum: ["PendingReview", "Approved", "Rejected"],
    default: "PendingReview",
  },
  approvedBy: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
    default: null,
  },
  approvedAt: { type: Date },
  rejectedBy: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
    default: null,
  },
  rejectedAt: { type: Date },
  rejectionReason: { type: String },
  chiefComplaint: {
    type: String,
  },
  pdfUrl: { type: String },
  assignedTo: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
    default: null,
  },
  assignedAt: { type: Date },
  createdAt: { type: Date, default: Date.now },
});

module.exports = mongoose.model("Report", reportSchema);
