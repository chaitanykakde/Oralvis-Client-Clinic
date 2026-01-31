const express = require("express");
const router = express.Router();
const Report = require("../models/report");
const {
  authMiddleware,
  requireAdmin,
  requireAdminOrDentist,
} = require("../middleware/authMiddleware");
const generateReportPDF = require("../utils/generateReportPDF.js");
const uploadPdfToS3 = require("../utils/uploadPdfToS3.js");
const sendEmail = require("../utils/sendEmail.js");
const sendPdfWhatsapp = require("../utils/sendPdfWhatsapp.js");
router.get("/admin", authMiddleware, requireAdminOrDentist, async (req, res) => {
  try {
    const reports = await Report.find({ status: "PendingReview" })
      .populate("patientId", "name phoneNo")
      .sort({ createdAt: -1 });
    const updatedReports = reports.map((report) => {
      const images = report.images || [];
      const results = report.aiDiagnosis?.results || [];

      const enrichedResults = results.map((r, idx) => {
        // Prefer provided imageUrl; fallback by filename; fallback by index
        const byFilename = images.find((imgUrl) => (r.filename ? imgUrl.endsWith(r.filename) : false));
        const fallbackByIndex = images[idx] || null;
        return {
          ...r,
          imageUrl: r.imageUrl || byFilename || fallbackByIndex,
        };
      });

      return {
        ...report.toObject(),
        aiDiagnosis: {
          ...report.aiDiagnosis,
          results: enrichedResults,
        },
      };
    });

    res.json({ reports: updatedReports });
  } catch (err) {
    console.error("Error fetching dentist reports:", err);
    res.status(500).json({ error: "Failed to fetch reports" });
  }
});

// New: Fetch single report for review
router.get("/:id", authMiddleware, requireAdminOrDentist, async (req, res) => {
  try {
    console.log('🔍 Fetching report with ID:', req.params.id);
    console.log('👤 User role:', req.user?.role);
    const report = await Report.findById(req.params.id).populate("patientId", "name phoneNo email");
    if (!report) {
      console.log('❌ Report not found for ID:', req.params.id);
      return res.status(404).json({ error: "Report not found" });
    }
    console.log('✅ Report found:', report._id);

    const images = report.images || [];
    const results = report.aiDiagnosis?.results || [];

    const enrichedResults = results.map((r, idx) => {
      const byFilename = images.find((imgUrl) => (r.filename ? imgUrl.endsWith(r.filename) : false));
      const fallbackByIndex = images[idx] || null;
      return {
        ...r,
        imageUrl: r.imageUrl || byFilename || fallbackByIndex,
      };
    });

    const payload = {
      ...report.toObject(),
      aiDiagnosis: {
        ...report.aiDiagnosis,
        results: enrichedResults,
      },
    };

    res.json({ report: payload });
  } catch (err) {
    console.error("Error fetching report:", err);
    res.status(500).json({ error: "Failed to fetch report" });
  }
});

router.patch(
  "/:id/update-ai",
  authMiddleware,
  requireAdminOrDentist,
  async (req, res) => {
    const { results } = req.body;

    try {
      const report = await Report.findById(req.params.id);
      if (!report) {
        return res.status(404).json({ error: "Report not found" });
      }

      if (!Array.isArray(results)) {
        return res.status(400).json({ error: "results must be an array" });
      }

      const formattedResults = results.map((r) => ({
        imageUrl: r.imageUrl,
        diagnosis: r.diagnosis,
        annotations: Array.isArray(r.annotations)
        ? r.annotations.map((ann) => ({
            points: ann.points,
            label: ann.label || "",
            color: ann.color || "#ff0000",
          }))
        : [],
      }));

      report.aiDiagnosis = {
        results: formattedResults,
        modifiedByDentist: true,
        modifiedAt: new Date(),
      };

      await report.save();

      res.json({ message: "Diagnosis updated", report });
    } catch (err) {
      console.error("Error updating diagnosis:", err);
      res.status(500).json({ error: "Failed to update diagnosis" });
    }
  }
);

router.patch("/:id/comment", authMiddleware, requireAdminOrDentist, async (req, res) => {
  const { comment } = req.body;
  try {
    const report = await Report.findByIdAndUpdate(
      req.params.id,
      { dentistComment: comment, commentedAt: new Date() },
      { new: true }
    );
    res.json({ message: "Comment added", report });
  } catch (err) {
    res.status(500).json({ error: "Failed to add comment" });
  }
});

router.patch("/:id/approve", authMiddleware, requireAdminOrDentist, async (req, res) => {
  try {
    const report = await Report.findById(req.params.id).populate("patientId");

    if (!report) return res.status(404).json({ error: "Report not found" });

    report.status = "Approved";

    const pdfBuffer = await generateReportPDF(report);

    const firstName = report.patientId.name.split(" ")[0];
    const fileName = `Report_${firstName}_${Date.now()}.pdf`;

    const pdfUrl = await uploadPdfToS3(pdfBuffer, fileName);

    report.pdfUrl = pdfUrl;
    await report.save();

    await sendEmail({
      to: report.patientId.email,
      subject: "Your Dental Report from OralVis",
      text: "Please find your attached dental report.",
      attachments: [
        {
          filename: `OralVis_Report_${report._id}.pdf`,
          content: pdfBuffer,
          contentType: "application/pdf",
        },
      ],
    });
    console.log("the file name is ", fileName);
    console.log("the ", report.patientId);
    console.log("thee", report.patientId.phoneNo);
    await sendPdfWhatsapp(report.patientId.phoneNo, pdfUrl);
    res.status(200).json({ message: "Report approved and sent", pdfUrl });
    // res.status(200).json({ message: "Report approved and sent" });
  } catch (err) {
    console.error("Approval error:", err);
    res.status(500).json({ error: "Failed to approve and send report" });
  }
});

router.get("/patient/:id", authMiddleware, async (req, res) => {
  console.log("🔍 Patient reports request:", {
    userId: req.user._id,
    userRole: req.user.role,
    requestedId: req.params.id,
    isPatient: req.user.role === "patient",
    idMatch: req.user._id.toString() === req.params.id
  });

  if (
    req.user.role !== "patient" ||
    req.user._id.toString() !== req.params.id
  ) {
    console.log("❌ Access denied:", {
      userRole: req.user.role,
      expectedRole: "patient",
      userId: req.user._id,
      requestedId: req.params.id
    });
    return res.status(403).json({ error: "Access denied to patient reports." });
  }

  try {
    const reports = await Report.find({
      patientId: req.params.id,
      status: "Approved",
    })
    .sort({ createdAt: -1 }) 
    .select("createdAt status pdfUrl");

    res.json({ reports });
  } catch (err) {
    console.error("Error fetching reports:", err);
    res.status(500).json({ error: "Failed to fetch reports" });
  }
});

module.exports = router;
