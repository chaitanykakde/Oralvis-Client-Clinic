const jwt = require("jsonwebtoken");

const WINDOW_MS = 60 * 1000; // 1 minute window
const DEFAULT_MAX = 20; // unauthenticated/public
const ELEVATED_MAX = 120; // authenticated admins/dentists/clinics

// Map<string, number[]> where key is userId (if authenticated) or IP
const keyToRequests = new Map();

const getIdentityFromRequest = (req) => {
  // Prefer authenticated user id from JWT cookie if available
  try {
    const token = req.cookies?.accessToken;
    if (token) {
      const decoded = jwt.verify(token, process.env.ACCESS_TOKEN_SECRET);
      if (decoded?.id) {
        return { key: `user:${decoded.id}`, role: decoded.role || null, isAuthenticated: true };
      }
    }
  } catch (_) {
    // ignore token errors; fall back to IP
  }

  const ip = req.ip || req.connection?.remoteAddress || "unknown";
  return { key: `ip:${ip}`, role: null, isAuthenticated: false };
};

const shouldBypass = (req) => {
  if (req.method === "OPTIONS") return true;
  // health checks and static-like paths
  if (req.path === "/" || req.path === "/health" || req.path === "/status") return true;
  return false;
};

const getMaxForRequest = (req, role, isAuthenticated) => {
  // elevate limits for any authenticated user (token present)
  if (isAuthenticated) return ELEVATED_MAX;
  return DEFAULT_MAX;
};

const rateLimiter = (req, res, next) => {
  if (shouldBypass(req)) return next();

  const now = Date.now();
  const { key, role, isAuthenticated } = getIdentityFromRequest(req);
  const maxRequests = getMaxForRequest(req, role, isAuthenticated);

  if (!keyToRequests.has(key)) {
    keyToRequests.set(key, []);
  }

  const timestamps = keyToRequests.get(key);

  // Remove timestamps older than WINDOW_MS
  while (timestamps.length && now - timestamps[0] > WINDOW_MS) {
    timestamps.shift();
  }

  if (timestamps.length >= maxRequests) {
    res.setHeader("Retry-After", Math.ceil(WINDOW_MS / 1000));
    res.setHeader("X-RateLimit-Limit", String(maxRequests));
    res.setHeader("X-RateLimit-Remaining", "0");
    res.setHeader(
      "X-RateLimit-Reset",
      String(Math.floor((timestamps[0] + WINDOW_MS) / 1000))
    );
    return res.status(429).json({
      success: false,
      message: "Too many requests. Please try again later.",
    });
  }

  timestamps.push(now);
  res.setHeader("X-RateLimit-Limit", String(maxRequests));
  res.setHeader(
    "X-RateLimit-Remaining",
    String(Math.max(0, maxRequests - timestamps.length))
  );
  res.setHeader(
    "X-RateLimit-Reset",
    String(Math.floor((timestamps[0] + WINDOW_MS) / 1000))
  );
  next();
};

module.exports = rateLimiter;