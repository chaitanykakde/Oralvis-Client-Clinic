// Centralized error handling middleware
// Usage: place after all routes in server.js with app.use(errorHandler)

// 404 handler for unmatched routes
const notFoundHandler = (req, res, next) => {
  res.status(404).json({
    success: false,
    message: "Route not found",
    path: req.originalUrl,
  });
};

// Main error handler
// eslint-disable-next-line no-unused-vars
const errorHandler = (err, req, res, next) => {
  const statusCode = err.statusCode || err.status || 500;

  // Handle common known errors
  let message = err.message || "Internal Server Error";

  // JWT errors
  if (err.name === "JsonWebTokenError") {
    message = "Invalid token";
  }
  if (err.name === "TokenExpiredError") {
    message = "Token expired";
  }

  // Mongoose validation errors
  if (err.name === "ValidationError") {
    message = Object.values(err.errors || {})
      .map((e) => e.message)
      .join(", ") || "Validation error";
  }

  // Duplicate key error
  if (err.code === 11000) {
    message = `Duplicate key: ${Object.keys(err.keyValue || {}).join(", ")}`;
  }

  const response = {
    success: false,
    message,
  };

  // Include stack in non-production for easier debugging
  if (process.env.NODE_ENV !== "production") {
    response.stack = err.stack;
  }

  res.status(statusCode).json(response);
};

module.exports = { errorHandler, notFoundHandler };


