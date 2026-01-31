const express = require("express");
const router = express.Router();
const userProfileController = require("../controllers/userProfileController");

router.get(
  "/bookings/upcoming/:userId",
  userProfileController.getUpcomingBookings
);

router.get("/bookings/past/:userId", userProfileController.getPastBookings);

module.exports = router;
