const express = require("express");
const router = express.Router();
const bookingController = require("../controllers/bookingController");
const clinicController = require("../controllers/clinicController");

router.post("/create-order", bookingController.createOrder);
router.post("/verify-payment", bookingController.verifyPayment);
router.post("/bookings/refund", bookingController.processRefund);
router.post("/booking/session", bookingController.createBookingSession);
router.post(
  "/confirm-without-payment",
  bookingController.confirmWithoutPayment
);
router.post("/reschedule/:bookingId", clinicController.rescheduleBooking);
 router.get("/booking/session/:sessionId", bookingController.getBookingSession);
router.get(
  "/bookings/cancelled/:userId",
  bookingController.getCancelledBookings
);
router.patch(
  "/bookings/cancel/:userId/:bookingId",
  bookingController.cancelAppointment
);
router.delete("/booking/session/:sessionId",bookingController.cancelBookingSession)
module.exports = router;
