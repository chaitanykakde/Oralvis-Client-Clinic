require("dotenv").config();
const express = require("express");
const userController = require("../controllers/userController");
const router = express.Router();
const { authMiddleware } = require("../middleware/authMiddleware");
const { 
  registrationValidation, 
  loginValidation, 
  clinicRegistrationValidation, 
  otpValidation 
} = require("../middleware/validation");

router.post("/start-registration", registrationValidation, userController.startRegistration);
router.post("/verify-registration", otpValidation, userController.verifyRegistration);
router.post("/register-patient", userController.registerPatient);
router.post("/register-dentist", userController.registerDentist);
router.post("/register-clinic-user", userController.registerClinicUser);
router.post("/register-admin", userController.registerAdmin);

// Clinic registration with OTP
router.post("/clinics/start-registration", clinicRegistrationValidation, userController.startClinicRegistration);
router.post("/clinics/verify-registration", otpValidation, userController.verifyClinicRegistration);
router.post("/clinics/send-otp", userController.sendClinicOtp);
router.post("/login", loginValidation, userController.loginew);
// Admin login with relaxed validation
router.post("/admin-login", userController.loginew);
// Developer-only login (phone + password) without OTP throttles (guard with ENV)
router.post("/dev-login", userController.loginew);
router.post("/logout", userController.logout);
router.post("/refresh-token", userController.refreshToken);
router.post("/start-otp-login", userController.startOtpLogin);
router.post("/verify-otp-login", userController.verifyOtpLogin);
router.post("/start-password-reset", userController.startPasswordReset);
router.post("/verify-reset-password", userController.verifyAndResetPassword);

router.get("/me", authMiddleware, (req, res) => {
  res.json(req.user);
});
router.get("/users/patients", userController.getPatients);
router.get("/users/dentists", userController.getDentists);
router.get("/users/admins", userController.getAdmins);
router.get(
  "/users/dentists-with-clinics",
  userController.getDentistsWithClinics
);

module.exports = router;


