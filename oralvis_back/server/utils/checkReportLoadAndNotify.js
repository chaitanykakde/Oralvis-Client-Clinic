const Report = require("../models/report");
const User = require("../models/user");
const sendEmail = require("../utils/sendEmail");

/**
 * Check how many pending reports (created today) are assigned to a given
 * report approval admin, and if the count exceeds 3, send a warning email.
 *
 * @param {import("mongoose").Types.ObjectId | string} adminId
 */
async function checkReportLoadAndNotify(adminId) {
  try {
    if (!adminId) return;

    const adminObjectId =
      typeof adminId === "string" ? adminId : adminId.toString();

    // Calculate today's date range (local server time)
    const startOfDay = new Date();
    startOfDay.setHours(0, 0, 0, 0);

    const endOfDay = new Date(startOfDay);
    endOfDay.setDate(endOfDay.getDate() + 1);

    const pendingTodayCount = await Report.countDocuments({
      assignedTo: adminObjectId,
      status: "PendingReview",
      createdAt: { $gte: startOfDay, $lt: endOfDay },
    });

    console.log(
      `[REPORT LOAD] Admin ${adminObjectId} has ${pendingTodayCount} pending reports for today`
    );

    if (pendingTodayCount <= 3) return;

    const admin = await User.findById(adminObjectId).select("name email");

    const subject = "Warning: High pending report load for approval admin";
    const text = `Report approval admin ${
      admin?.name || adminObjectId
    } currently has ${pendingTodayCount} pending reports assigned today.`;

    await sendEmail({
      to: "amitsahani2322003@gmail.com",
      subject,
      text,
    });

    console.log(
      `✅ [REPORT LOAD] Warning email sent for admin ${adminObjectId} with ${pendingTodayCount} pending reports`
    );
  } catch (error) {
    console.error(
      "❌ [REPORT LOAD] Error while checking report load / sending warning email:",
      error
    );
  }
}

module.exports = checkReportLoadAndNotify;


