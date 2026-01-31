const User = require("../models/user");

/**
 * Assigns a report to an admin using round-robin method
 * @returns {Promise<ObjectId|null>} The ID of the assigned admin, or null if no admins available
 */
async function assignReportRoundRobin() {
  try {
    // Get all available report approval admins
    const admins = await User.find({ role: "report_approval_admin" })
      .select("_id")
      .sort({ _id: 1 }); // Sort for consistent ordering

    if (admins.length === 0) {
      console.log("⚠️ No report approval admins available for assignment");
      return null;
    }

    // Get the count of pending reports assigned to each admin
    const Report = require("../models/report");
    const assignmentCounts = await Report.aggregate([
      {
        $match: {
          status: "PendingReview",
          assignedTo: { $in: admins.map((a) => a._id) },
        },
      },
      {
        $group: {
          _id: "$assignedTo",
          count: { $sum: 1 },
        },
      },
    ]);

    // Create a map of admin ID to assignment count
    const countMap = new Map();
    assignmentCounts.forEach((item) => {
      countMap.set(item._id.toString(), item.count);
    });

    // Find the admin with the least assignments
    let minCount = Infinity;
    let selectedAdmin = null;

    for (const admin of admins) {
      const adminId = admin._id.toString();
      const count = countMap.get(adminId) || 0;

      if (count < minCount) {
        minCount = count;
        selectedAdmin = admin._id;
      }
    }

    console.log(
      `✅ Round-robin assignment: Selected admin ${selectedAdmin} with ${minCount} pending reports`
    );
    return selectedAdmin;
  } catch (error) {
    console.error("❌ Error in round-robin assignment:", error);
    return null;
  }
}

module.exports = assignReportRoundRobin;

