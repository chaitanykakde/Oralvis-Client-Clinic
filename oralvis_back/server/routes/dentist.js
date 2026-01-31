const express = require('express');
const router = express.Router();
const dentistController = require('../controllers/dentistController');
const {authMiddleware} = require('../middleware/authMiddleware');

router.get('/dashboard-stats', authMiddleware, dentistController.getDashboardStats);
router.patch('/bookings/:bookingId/confirm', dentistController.confirmAppointment);
router.post("/profile", authMiddleware,dentistController.upsertDentistProfile)
router.get("/profile", authMiddleware,dentistController.getDentistProfile)
module.exports = router;