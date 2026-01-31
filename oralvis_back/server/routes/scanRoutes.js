const express = require("express");
const router = express.Router();
const multer = require("multer");
const fs = require("fs");
const path = require("path");
const uploadToS3 = require("../utils/uploadToS3");
const Report = require("../models/report");
const { authMiddleware } = require("../middleware/authMiddleware");
const jwt = require("jsonwebtoken");
const User = require("../models/user");
const redis = require("../config/redis");
const { v4: uuidv4 } = require("uuid");

// Prefer memory storage for cross-browser reliability and to avoid disk I/O issues
const storage = multer.memoryStorage();
const upload = multer({ storage });

// Optional auth: populate req.user if cookie token is present; otherwise continue
const optionalAuth = async (req, _res, next) => {
  try {
    const token = req.cookies && req.cookies.accessToken;
    if (!token) return next();
    const decoded = jwt.verify(token, process.env.ACCESS_TOKEN_SECRET);
    const user = await User.findById(decoded.id);
    if (user) req.user = user;
  } catch (_e) {
    // ignore errors to allow sessionToken flow
  } finally {
    next();
  }
};

// Generate scan session token for QR code
router.post("/generate-scan-session", authMiddleware, async (req, res) => {
  try {
    console.log("🔄 Generating scan session for user:", req.user._id);
    const sessionToken = uuidv4();
    const sessionData = {
      userId: req.user._id,
      userName: req.user.name,
      phoneNo: req.user.phoneNo,
      clinicId: null, // Patients don't have clinicId, this will be set during booking if needed
      createdAt: new Date().toISOString(),
    };

    console.log("📋 Session data:", sessionData);

    // Store session data in Redis with 5 minutes expiry
    await redis.setex(`scan_session:${sessionToken}`, 300, JSON.stringify(sessionData));
    console.log("✅ Session stored in Redis with token:", sessionToken);

    res.status(200).json({
      success: true,
      sessionToken,
      message: "Scan session created successfully",
    });
  } catch (err) {
    console.error("❌ Generate scan session error:", err);
    res.status(500).json({ error: "Failed to generate scan session" });
  }
});

// Get session data by token (for mobile scan page)
router.get("/scan-session/:token", async (req, res) => {
  try {
    const { token } = req.params;
    console.log("🔍 Fetching session data for token:", token);
    
    const sessionData = await redis.get(`scan_session:${token}`);

    if (!sessionData) {
      console.log("❌ Session not found or expired for token:", token);
      return res.status(404).json({ error: "Session expired or not found" });
    }

    const parsedData = JSON.parse(sessionData);
    console.log("✅ Session data retrieved:", parsedData);
    
    res.status(200).json({
      success: true,
      sessionData: parsedData,
    });
  } catch (err) {
    console.error("❌ Get scan session error:", err);
    res.status(500).json({ error: "Failed to get scan session" });
  }
});

// router.post(
//   "/upload-and-analyze",
//   authMiddleware,
//   upload.array("images", 7),
//   async (req, res) => {
//     const files = req.files;
//     try {
//       if (!files || files.length === 0) {
//         return res.status(400).json({ error: "No images uploaded" });
//       }

//       const userName = req.query.userName;
//       const phoneNo = req.query.phoneNo;

//       const form = new FormData();
//       for (const file of files) {
//         form.append("images", fs.createReadStream(file.path));
//       }

//       const aiResponse = await axios.post(
//         "http://localhost:8003/predict",
//         form,
//         {
//           headers: form.getHeaders(),
//           maxBodyLength: Infinity,
//           maxContentLength: Infinity,
//         }
//       );
//       const uploadedS3Urls = [];
//       for (const file of files) {
//         const s3Url = await uploadToS3(file.path, file.filename, {
//           userName,
//           phoneNo,
//         });
//         uploadedS3Urls.push(s3Url);
//       }

//       for (const file of files) {
//         fs.unlinkSync(file.path);
//       }

//       const newReport = new Report({
//         patientId: req.user._id,
//         clinicId: req.user.clinicId || null,
//         images: uploadedS3Urls,
//         aiDiagnosis: aiResponse.data,
//         status: "PendingReview",
//       });

//       await newReport.save();

//       res.status(200).json({
//         message: "Images analyzed and saved successfully",
//         report: newReport,
//       });
//     } catch (err) {
//       console.error("Upload+Analyze Error:", err);
//       res.status(500).json({ error: "Failed to analyze or upload images" });
//     } finally {
//       for (const file of files) {
//         try {
//           if (fs.existsSync(file.path)) {
//             fs.unlinkSync(file.path);
//           }
//         } catch (cleanupErr) {
//           console.error("Error cleaning up file:", file.path, cleanupErr);
//         }
//       }
//     }
//   }
// );

// Removed express-validator block as we handle minimal validation inline

router.post(
  "/upload-and-analyze",
  // Parse multipart FIRST
  multer({ storage: multer.memoryStorage() }).any(),
  // Then optional auth that reads cookies if available
  optionalAuth,
  async (req, res) => {
    const files = req.files;
    console.log("📤 Upload request received");
    console.log("📊 Request details:", {
      filesCount: files ? files.length : 0,
      hasUser: !!req.user,
      queryParams: req.query,
      bodyParams: req.body,
      headers: {
        'content-type': req.headers['content-type'],
        'user-agent': req.headers['user-agent']
      }
    });

    // Check database connection
    try {
      const mongoose = require('mongoose');
      if (mongoose.connection.readyState !== 1) {
        console.error("❌ Database not connected. State:", mongoose.connection.readyState);
        return res.status(500).json({ error: "Database connection not available" });
      }
      console.log("✅ Database connection verified");
    } catch (dbErr) {
      console.error("❌ Database connection error:", dbErr);
      return res.status(500).json({ error: "Database connection error" });
    }
    
    // Log the exact request body for debugging
    console.log("🔍 Full request body:", JSON.stringify(req.body, null, 2));
    
    // Log files details
    if (files && files.length > 0) {
      console.log("📁 Files received:");
      files.forEach((file, index) => {
        console.log(`  File ${index + 1}:`, {
          fieldname: file.fieldname,
          originalname: file.originalname,
          encoding: file.encoding,
          mimetype: file.mimetype,
          size: file.size,
          path: file.path,
          inMemory: !!file.buffer
        });
      });
    } else {
      console.log("❌ No files received in req.files");
    }

    // Manual validation for chief complaint
    const chiefComplaint = req.body.chiefComplaint;
    if (chiefComplaint && !['Pain', 'Bleeding gums', 'Cavities', 'Bad breath', 'General'].includes(chiefComplaint)) {
      console.log("❌ Invalid chief complaint:", chiefComplaint);
      return res.status(400).json({ 
        error: 'Validation failed',
        details: [{ msg: 'Invalid chief complaint', param: 'chiefComplaint', value: chiefComplaint }]
      });
    }

    // Validate image count for teeth scanning (expecting exactly 3 images)
    if (files && files.length !== 3) {
      console.log("⚠️ Expected 3 images for teeth scan, received:", files.length);
      return res.status(400).json({ 
        error: 'Invalid image count',
        details: 'Teeth scanning requires exactly 3 images (Front, Upper, Lower)'
      });
    }

    try {
      if (!files || files.length === 0) {
        console.log("❌ No images uploaded");
        return res.status(400).json({ error: "No images uploaded" });
      }

      let userData;
      const sessionToken = req.query.sessionToken || req.body.sessionToken;
      console.log("🔑 Session token:", sessionToken ? "Present" : "Missing");

      if (sessionToken) {
        console.log("🔍 Looking up session data for token:", sessionToken);
        // Get user data from session token
        const sessionData = await redis.get(`scan_session:${sessionToken}`);
        if (!sessionData) {
          console.log("❌ Session expired or not found for token:", sessionToken);
          // Try to resolve user by provided phone/name to avoid null patientId
          const fbUserName = req.body.userName || req.query.userName || null;
          const fbPhoneNo = req.body.phoneNo || req.query.phoneNo || null;
          if (!fbPhoneNo) {
            return res.status(404).json({ error: "Session expired or not found" });
          }
          const existing = await User.findOne({ phoneNo: fbPhoneNo });
          if (!existing) {
            return res.status(404).json({ error: "User not found for provided phone number" });
          }
          userData = {
            userId: existing._id,
            userName: fbUserName || existing.name,
            phoneNo: existing.phoneNo,
            clinicId: null,
          };
          console.log("🧪 Resolved user from phone for expired session:", userData);
        } else {
          userData = JSON.parse(sessionData);
          console.log("✅ Session data retrieved:", userData);
        }
      } else {
        console.log("👤 Using authenticated user flow");
        // Use authenticated user data (from cookie-based auth)
        if (!req.user) {
          console.log("❌ No authenticated user and no session token");
          return res.status(401).json({ error: "Authentication required" });
        }
        userData = {
          userId: req.user._id,
          userName: req.user.name,
          phoneNo: req.user.phoneNo,
          clinicId: null, // Patients don't have clinicId by default
        };
        console.log("✅ User data from auth:", userData);
      }

      const userName = req.query.userName || req.body.userName || userData.userName;
      const phoneNo = req.query.phoneNo || req.body.phoneNo || userData.phoneNo;
      const chiefComplaint = req.query.chiefComplaint || req.body.chiefComplaint || null;

      console.log("📋 Final user data:", { userName, phoneNo, chiefComplaint });

      const imageFiles = files || [];
      console.log("☁️ Starting S3 upload for", imageFiles.length, "files (memory storage)");
      console.log("🦷 Teeth scanning: Front, Upper, Lower images");
      const uploadedS3Urls = [];
      const teethTypes = ['Front', 'Upper', 'Lower'];
      
      for (let i = 0; i < imageFiles.length; i++) {
        const file = imageFiles[i];
        const teethType = teethTypes[i] || `Image${i + 1}`;
        const originalFileName = file.originalname || file.filename || `${teethType.toLowerCase()}-teeth-${Date.now()}.jpg`;
        console.log(`📁 Uploading ${teethType} teeth image:`, originalFileName, "Size:", file.size, "inMemory:", !!file.buffer);

        // Persist a local copy in uploads/ as requested
        try {
          const uploadsDir = path.join(__dirname, "..", "uploads");
          if (!fs.existsSync(uploadsDir)) {
            fs.mkdirSync(uploadsDir, { recursive: true });
          }
          const localPath = path.join(uploadsDir, `${Date.now()}-${originalFileName}`);
          if (file.buffer && file.buffer.length) {
            fs.writeFileSync(localPath, file.buffer);
            console.log("💾 Saved local copy:", localPath);
          }
          const s3Url = await uploadToS3(localPath, originalFileName, {
            userName,
            phoneNo,
            buffer: file.buffer,
            teethType: teethType,
            imageOrder: i + 1,
          });
          uploadedS3Urls.push(s3Url);
          console.log(`✅ ${teethType} teeth image uploaded to S3:`, s3Url);
        } catch (persistErr) {
          console.error("❌ Error saving/uploading file:", persistErr);
          throw persistErr;
        }
      }

      // Cleanup local files (best-effort)
      for (const file of files) {
        try {
          if (file.path && fs.existsSync(file.path)) {
            fs.unlinkSync(file.path);
            console.log("🗑️ Cleaned up local file:", file.path);
          }
        } catch (cleanupErr) {
          console.error("❌ Cleanup error for:", file.path, cleanupErr);
        }
      }

      console.log("💾 Creating report in database");
      console.log("📋 Report data:", {
        patientId: userData.userId,
        clinicId: userData.clinicId,
        imagesCount: uploadedS3Urls.length,
        chiefComplaint,
        status: "PendingReview"
      });

      // Assign report to admin using round-robin
      const assignReportRoundRobin = require("../utils/assignReportRoundRobin");
      const checkReportLoadAndNotify = require("../utils/checkReportLoadAndNotify");
      const assignedAdminId = await assignReportRoundRobin();
      
      if (assignedAdminId) {
        console.log("✅ Report assigned to admin:", assignedAdminId);
      } else {
        console.log("⚠️ No admin available for assignment");
      }

      const newReport = new Report({
        patientId: userData.userId,
        clinicId: userData.clinicId,
        images: uploadedS3Urls,
        aiDiagnosis: null,
        chiefComplaint,
        status: "PendingReview",
        assignedTo: assignedAdminId,
        assignedAt: assignedAdminId ? new Date() : null,
      });

      const savedReport = await newReport.save();

      // After saving, check today's pending count for that admin and send warning if needed
      if (assignedAdminId) {
        await checkReportLoadAndNotify(assignedAdminId);
      }
      console.log("✅ Report saved with ID:", savedReport._id);
      console.log("📊 Report details:", {
        id: savedReport._id,
        patientId: savedReport.patientId,
        images: savedReport.images.length,
        status: savedReport.status,
        chiefComplaint: savedReport.chiefComplaint
      });

      // Verify the report was actually saved by querying the database
      const verifyReport = await Report.findById(savedReport._id);
      if (!verifyReport) {
        console.error("❌ Report verification failed - report not found in database");
        throw new Error("Report was not saved to database");
      }
      console.log("✅ Report verification successful - found in database");

      // Clean up session token after successful upload (only if it existed)
      if (sessionToken) {
        const key = `scan_session:${sessionToken}`;
        const exists = await redis.get(key);
        if (exists) {
          await redis.del(key);
          console.log("🗑️ Session token cleaned up:", sessionToken);
        }
      }

      const response = {
        message: "Teeth scan images uploaded and report created successfully",
        report: {
          id: newReport._id,
          patientId: newReport.patientId,
          images: newReport.images,
          status: newReport.status,
          teethScan: {
            totalImages: uploadedS3Urls.length,
            imageTypes: teethTypes,
            chiefComplaint: chiefComplaint,
            scanDate: new Date().toISOString()
          }
        },
      };
      console.log("✅ Teeth scan upload completed successfully");
      console.log("🦷 Scan summary:", {
        patient: userName,
        images: uploadedS3Urls.length,
        types: teethTypes,
        complaint: chiefComplaint
      });
      res.status(200).json(response);
    } catch (err) {
      console.error("❌ Upload Error:", err?.message || err);
      console.error("Error stack:", err?.stack);
      console.error("Error details:", {
        name: err?.name,
        code: err?.code,
        status: err?.status,
        response: err?.response?.data
      });
      res.status(500).json({ error: "Failed to upload images" });
    }
  }
);

module.exports = router;
