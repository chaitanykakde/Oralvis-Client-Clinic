const express = require("express");
const router = express.Router();
const bookingController = require("../controllers/bookingController");
const adminController = require("../controllers/adminController");

const {
  authMiddleware,
  requireAdmin,
  requireSuperAdmin,
  requireReportApprovalAdmin,
  requireMonitoringAdmin,
} = require("../middleware/authMiddleware");

// Public routes (no authentication required)
router.get("/bookings/confirmed", bookingController.getConfirmedBookings);
router.get("/bookings/pending", bookingController.getPendingBookings);
router.get("/bookings/cancelled", bookingController.getCancelledBookingsadmin);
router.get("/bookings/:id", bookingController.deleteBooking);
router.put("/bookings/:id/cancel", bookingController.cancelBookingById);
router.patch("/bookings/:id", bookingController.changeStatus);
router.post("/assign-clinic-owner", adminController.assignClinicOwner);
router.delete("/delete-by-phone", adminController.deleteUserByPhone);

// Super Admin Routes - Admin Management
// Test route to check super admin creation
router.get("/test-super-admin", async (req, res) => {
  try {
    const User = require("../models/user");
    const superAdmin = await User.findOne({ role: 'super_admin' });
    if (superAdmin) {
      res.json({ 
        message: "Super admin exists", 
        admin: {
          id: superAdmin._id,
          name: superAdmin.name,
          email: superAdmin.email,
          phoneNo: superAdmin.phoneNo,
          role: superAdmin.role,
          adminType: superAdmin.adminType
        }
      });
    } else {
      res.json({ message: "Super admin not found" });
    }
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Test route to manually create super admin
router.post("/create-super-admin", async (req, res) => {
  try {
    const createSuperAdmin = require("../utils/createSuperAdmin");
    const superAdmin = await createSuperAdmin();
    res.json({ 
      message: "Super admin created/verified", 
      admin: {
        id: superAdmin._id,
        name: superAdmin.name,
        email: superAdmin.email,
        phoneNo: superAdmin.phoneNo,
        role: superAdmin.role,
        adminType: superAdmin.adminType
      }
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

router.post(
  "/create-admin",
  authMiddleware,
  requireSuperAdmin,
  adminController.createAdmin
);

router.get(
  "/admins",
  authMiddleware,
  requireSuperAdmin,
  adminController.getAllAdmins
);

router.put(
  "/admins/:adminId",
  authMiddleware,
  requireSuperAdmin,
  adminController.updateAdmin
);

router.delete(
  "/admins/:adminId",
  authMiddleware,
  requireSuperAdmin,
  adminController.deleteAdmin
);

// Report Approval Admin Routes
router.get(
  "/reports",
  authMiddleware,
  requireReportApprovalAdmin,
  adminController.getAllReports
);

router.patch(
  "/reports/:reportId/approve",
  authMiddleware,
  requireReportApprovalAdmin,
  adminController.approveReport
);

router.patch(
  "/reports/:reportId/reject",
  authMiddleware,
  requireReportApprovalAdmin,
  adminController.rejectReport
);

// Monitoring Admin Routes - All other admin features except report approval
router.get(
  "/dashboard-stats",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.getDashboardStats
);

router.get(
  "/bookings",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.getAllBookings
);

router.get(
  "/users/patients",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.getAllPatients
);

router.get(
  "/clinics/get",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.getAllClinics
);

router.get(
  "/clinics",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.getAllClinicsName
);

router.get(
  "/clinic-requests",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.getAllRequests
);

router.post(
  "/clinic-requests/approve/:requestId",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.approveClinic
);

router.post(
  "/clinic-requests/reject/:requestId",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.rejectClinic
);

router.post(
  "/clinics/admin-create",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.adminCreateClinicWithUser
);

router.post(
  "/clinic/:userId",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.createOrUpdateClinic
);

router.post(
  "/clinics/create/:userId",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.createClinic
);

router.put(
  "/clinics/update/:clinicId",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.updateClinic
);

router.delete(
  "/clinics/:clinicId",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.deleteClinic
);

router.post(
  "/create-dentist",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.createDentist
);

router.put(
  "/dentists/:dentistId",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.updateDentist
);

router.delete(
  "/dentists/:dentistId",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.deleteDentist
);

router.get(
  "/clinic-profile/:userId",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.getClinicProfileByUserId
);

router.get(
  "/clinics-with-bookings",
  authMiddleware,
  requireMonitoringAdmin,
  adminController.getClinicsWithBookings
);

// Refund routes - accessible to both monitoring and super admin
router.get(
  "/refund",
  authMiddleware,
  requireAdmin,
  adminController.getRefundsByType
);

router.post(
  "/refund/approve/:bookingId",
  authMiddleware,
  requireAdmin,
  adminController.approveRefund
);

router.post(
  "/refund/reject/:bookingId",
  authMiddleware,
  requireAdmin,
  adminController.rejectRefund
);

module.exports = router;
