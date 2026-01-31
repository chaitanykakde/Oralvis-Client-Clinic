const express = require("express");
const router = express.Router();
const controller = require("../controllers/patientProfileController");
const upload = require("../middleware/uploadmulter.js");

router.get("/profile/:userId", controller.getPatientProfile);
router.get('/patient/image/:userId', controller.getPatientImageUrl);

router.put("/profile/:userId", upload.single('profilePic'),controller.updatePatientProfile);

module.exports = router;
