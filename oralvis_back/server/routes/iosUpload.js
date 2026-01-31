const express = require("express");
const multer = require("multer");
const s3 = require("../config/aws");
const path = require("path");

const router = express.Router();

const upload = multer({ storage: multer.memoryStorage() });

router.post("/ios-upload", upload.array("images"), async (req, res) => {
  const { patientName, patientPhone, patientId, patientAge } = req.body;

  if (!patientName || !patientPhone || !patientAge) {
    return res
      .status(400)
      .send({ error: "Missing patient name or phone. or age" });
  }

  if (!req.files || req.files.length === 0) {
    return res.status(400).send({ error: "No images uploaded." });
  }

  try {
    const patientFolder = `${patientName}_${patientPhone}_${patientAge}`;
    const uploadedFiles = [];

    for (const file of req.files) {
      const fileKey = `ios-reports/${patientFolder}/${Date.now()}_${path.basename(
        file.originalname
      )}`;

      const params = {
        Bucket: process.env.AWS_BUCKET_NAME,
        Key: fileKey,
        Body: file.buffer,
        ContentType: file.mimetype,
      };
      await s3.upload(params).promise();
      uploadedFiles.push(fileKey);
    }

    res.json({
      message: "Images uploaded successfully",
      patientId,
      files: uploadedFiles,
    });
  } catch (error) {
    console.error(error);
    res.status(500).send({ error: "Image upload failed" });
  }
});

module.exports = router;
