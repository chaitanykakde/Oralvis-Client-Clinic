require("dotenv").config();
const express = require("express");
const cookieParser = require("cookie-parser");
const connectDB = require("./config/db");
const http = require("http");
const { Server } = require("socket.io");
const bookingRoutes = require("./routes/booking");
const userRoutes = require("./routes/user");
const slotRoutes = require("./routes/slotRoute");
const adminRoutes = require("./routes/admin");
const dentistRoutes = require("./routes/dentist");
const clinicRoutes = require("./routes/clinic");
const userProfileRoutes = require("./routes/userProfileRoute");
const patientRoutes = require("./routes/patientRoutes");
// const scanRoutes = require("./routes/scan");

const scanRoutes = require("./routes/scanRoutes");
const blogRoutes = require("./routes/blog");
const reportRoutes = require("./routes/reportRoutes");
const iosRoutes = require("./routes/iosUpload");
const cors = require("cors");
const helmet = require("helmet");
const { errorHandler, notFoundHandler } = require("./middleware/errorHandler");
const rateLimiter = require("./middleware/rateLimiter");

const app = express();
const createSuperAdmin = require('./utils/createSuperAdmin');
connectDB();

const PORT = 4000;

// Use Helmet to set secure HTTP headers
app.use(
  helmet({
    contentSecurityPolicy: false, 
    crossOriginEmbedderPolicy: true,
    crossOriginOpenerPolicy: true,
    frameguard: { action: "deny" }, 
  })
);


const allowedOrigins = [
  "http://localhost:5173",
  "http://127.0.0.1:5173",
  "http://192.168.56.1:4000",
  "http://192.168.0.104:5173",
  "http://192.168.0.106:4000",
  "http://192.168.0.104:3000",
  "http://54.146.239.116:3000",
  "https://scan-ios.vercel.app",
  "https://oralvis.com",
  "https://www.oralvis.com", 
  "https://oralvis.in",
  "https://www.oralvis.in",
  "https://testing.oravis.in",
];

app.use(
  cors({
    origin: (origin, callback) => {
      if (!origin || allowedOrigins.includes(origin)) {
        callback(null, true);
      } else {
        callback(new Error("Not allowed by CORS"));
      }
    },
    credentials: true,
    methods: "GET,HEAD,PUT,PATCH,POST,DELETE",
    allowedHeaders: "Content-Type,Authorization",
  })
);

// Cookies must be parsed BEFORE routes that need auth (e.g., generate-scan-session)
app.use(cookieParser());

// Mount scan routes BEFORE JSON/urlencoded parsers and rate limiter so multipart is unaffected
app.use("/api", scanRoutes);

// Keep global body limits small; upload routes handle large payloads explicitly
app.use(express.json({ limit: "300kb" }));
app.use(express.urlencoded({ extended: true, limit: "300kb" }));

app.use(rateLimiter);

// (already applied above to support rate limiter identity)
const server = http.createServer(app);
// const io = new Server(server, {
//   cors: {
//     origin: "http://localhost:5173",
//     methods: "*",
//     credentials: true,
//   },

// });
const io = new Server(server, {
  cors: {
    origin: allowedOrigins,
    methods: "*",
    credentials: true,
  },
});

io.on("connection", (socket) => {
  console.log("A client connected:", socket.id);

  socket.on("disconnect", () => {
    console.log("A client disconnected:", socket.id);
  });
});

app.use((req, res, next) => {
  req.io = io;
  next();
});
app.get("/", (req, res) => {
  res.status(200).json({ status: "healthy" });
});

app.use("/api", userRoutes);
app.use("/api", bookingRoutes);
app.use("/api", slotRoutes);
app.use("/api/admin", adminRoutes);
app.use("/api/dentist", dentistRoutes);
app.use("/api/clinics", clinicRoutes);
app.use("/api/users", userProfileRoutes);
app.use("/api/patient", patientRoutes);
app.use("/api", iosRoutes);
app.use("/api/blogs", blogRoutes);
app.use("/api/reports", reportRoutes);

// 404 for unmatched routes
app.use(notFoundHandler);

// Centralized error handler (must be last)
app.use(errorHandler);

// Create super admin on server start
createSuperAdmin().catch(console.error);

server.listen(PORT, "0.0.0.0", () => {
  console.log(`Server running on http://192.168.0.104:${PORT} `);
});



