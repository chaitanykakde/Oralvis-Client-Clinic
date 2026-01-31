const express = require("express");
const router = express.Router();
const clinicController = require("../controllers/clinicController");
const Clinic = require("../models/clinic");
const upload = require("../middleware/uploadmulter");

router.post("/create/:userId", clinicController.createClinic);
router.post("/add-dentist", clinicController.addDentist);
router.post("/remove-dentist", clinicController.removeDentist);
router.post("/register-clinic", clinicController.registerClinic);
router.post("/book-walkin", clinicController.bookWalkinPatient);
router.post("/clinic-profile/:userId", clinicController.createClinicProfile);

router.get("/clinic-id/:userId", clinicController.getClinicIdByUserId);
router.get("/dentists/:clinicId", clinicController.getDentistsByClinic);
router.get("/clinic-earnings/:clinicId", clinicController.getClinicEarnings); //done
router.get("/with-main-doctor", clinicController.clinicWithDoctor);
router.get(
  "/calendar/:clinicId",
  clinicController.getAppointmentsByMonthOrDate
);
router.get(
  "/earning-dashboard-stats/:clinicId",
  clinicController.getEarningClinicDashboardStats
); //done
router.get("/bookings-by-date/:clinicId", clinicController.getBookingsByDate);
router.get(
  "/dashboard-stats-clinic/:clinicId",
  clinicController.getClinicDashboardStats
); //done
router.get(
  "/appointment-status-counts/:clinicId",
  clinicController.getAppointmentStatusCounts
); //done
router.get("/appointments/:clinicId", clinicController.getClinicAppointments); //done
router.get("/clinic-profile/:userId", clinicController.getClinicProfile);
router.post("/calendar/week", clinicController.getAppointmentsByWeek);
router.get("/get", clinicController.getAllClinics);
router.get("/get/:clinicId", clinicController.getClinicById);
router.get("/slotss/:clinicId", clinicController.getSlotsByDate);
router.get("/nearby", clinicController.getNearbyClinics);
router.get("/all", clinicController.getAllClinics);
router.get("/city-area-map", clinicController.getCityAreaMap);
router.get("/getallclinicwithdoctor", clinicController.getAllClinicWithDoctor);
router.get("/nearby-clinics", async (req, res) => {
  const { lat, lng } = req.query;
  if (!lat || !lng) {
    return res.status(400).json({ message: "Latitude and longitude required" });
  }

  try {
    const clinics = await Clinic.aggregate([
      {
        $geoNear: {
          near: {
            type: "Point",
            coordinates: [parseFloat(lng), parseFloat(lat)],
          },
          distanceField: "distance",
          maxDistance: 5000, // 5 km
          spherical: true,
        },
      },
    ]);

    res.json(clinics);
  } catch (error) {
    console.error("❌ Error fetching nearby clinics:", error);
    res.status(500).json({ message: "Internal Server Error" });
  }
});

router.get("/:clinicId", clinicController.getClinic);

router.put("/:id/location", clinicController.updateClinicLocation);

router.patch(
  "/cancel-booking/:bookingId",
  clinicController.cancelSingleBookingByClinic
); //done
router.patch("/clinic-profile/:userId", clinicController.updateClinicAndUser);
router.patch(
  "/cancel-bookings/date/:clinicId",
  clinicController.cancelBookingsByDateForClinic
); //done
router.patch(
  "/bookings/:bookingId/mark-paid",
  clinicController.markBookingAsPaid
);
router.patch(
  "/bookings/:bookingId/notes",
  clinicController.updateBookingNotesByClinic
);
router.patch("/:clinicId/add-dentist", clinicController.addDentistToClinic);
router.patch("/:id/about", clinicController.updateAboutSection);

// Clinical Records Routes
router.post(
  "/bookings/:bookingId/clinical-records",
  clinicController.createOrUpdateClinicalRecord
);
router.get(
  "/bookings/:bookingId/clinical-records",
  clinicController.getClinicalRecord
);
router.post(
  "/bookings/:bookingId/clinical-records/upload",
  upload.single("file"),
  clinicController.uploadClinicalAttachment
);
router.post(
  "/bookings/:bookingId/clinical-records/upload-prescription-image",
  upload.single("file"),
  clinicController.uploadPrescriptionImage
);
router.post(
  "/bookings/:bookingId/clinical-records/upload-file",
  upload.single("file"),
  clinicController.uploadFile
);

// Medical History Routes
router.post("/medical-history", clinicController.createOrUpdateMedicalHistory);
router.get("/medical-history/:clinicId", clinicController.getMedicalHistory);

module.exports = router;
