require("dotenv").config();
const jwt = require("jsonwebtoken");
const User = require("../models/user");
const redisClient = require("../config/redis");

const authMiddleware = async (req, res, next) => {
  try {
    const token = req.cookies.accessToken;
    if (!token) {
      return res.status(401).json({ error: "No token, authorization denied" });
    }
    const decoded = jwt.verify(token, process.env.ACCESS_TOKEN_SECRET);

    const isBlacklisted = await redisClient.get(`blacklist:${token}`);
    if (isBlacklisted) {
      return res.status(401).json({ error: "Token invalidated" });
    }

   
    const user = await User.findById(decoded.id);
    if (!user) {
      return res.status(401).json({ error: "User not found" });
    }

    req.user = user;
    next();
  } catch (error) {
    console.error("Authentication error:", error.message);
    if (error.name === "JsonWebTokenError") {
      return res.status(401).json({ error: "Invalid token" });
    }
    if (error.name === "TokenExpiredError") {
      return res.status(401).json({ error: "Token expired" });
    }
    res.status(500).json({ error: "Server error during authentication" });
  }
};

const requireDentist = (req, res, next) => {
  if (req.user?.role !== "dentist") {
    return res.status(403).json({ error: "Dentist access required" });
  }
  next();
};

const requireAdmin = (req, res, next) => {
  const adminRoles = ["admin", "super_admin", "report_approval_admin", "monitoring_admin"];
  if (!adminRoles.includes(req.user?.role)) {
    return res.status(403).json({ error: "Admin access required" });
  }
  next();
};

const requireSuperAdmin = (req, res, next) => {
  if (req.user?.role !== "super_admin") {
    return res.status(403).json({ error: "Super admin access required" });
  }
  next();
};

const requireReportApprovalAdmin = (req, res, next) => {
  const allowedRoles = ["super_admin", "report_approval_admin"];
  if (!allowedRoles.includes(req.user?.role)) {
    return res.status(403).json({ error: "Report approval access required" });
  }
  next();
};

const requireMonitoringAdmin = (req, res, next) => {
  const allowedRoles = ["super_admin", "monitoring_admin"];
  if (!allowedRoles.includes(req.user?.role)) {
    return res.status(403).json({ error: "Monitoring access required" });
  }
  next();
};

const requireAdminOrDentist = (req, res, next) => {
  const role = req.user?.role;
  const allowedRoles = ["admin", "dentist", "super_admin", "report_approval_admin", "monitoring_admin"];
  if (allowedRoles.includes(role)) {
    next();
  } else {
    return res.status(403).json({ error: "Access denied. Unauthorized role." });
  }
};

module.exports = {
  authMiddleware,
  requireDentist,
  requireAdmin,
  requireSuperAdmin,
  requireReportApprovalAdmin,
  requireMonitoringAdmin,
  requireAdminOrDentist
};
