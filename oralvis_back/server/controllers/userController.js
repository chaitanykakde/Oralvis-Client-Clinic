const User = require("../models/user");
const twilio = require("twilio");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const redis = require("../config/redis");
const { sendWelcomeEmail } = require("../config/emailservice");
const { sendWhatsAppWelcome } = require("../config/whatsappservice");
const { generateAccessToken, generateRefreshToken } = require("../utils/token");
const generatePatientId = require("../utils/generatePatientId");

const client = twilio(
  process.env.TWILIO_ACCOUNT_SID,
  process.env.TWILIO_AUTH_TOKEN
);

const JWT_SECRET = process.env.JWT_SECRET;

exports.getDentistsWithClinics = async (req, res) => {
  try {
    const dentists = await User.find({ role: "dentist" })
      .populate("clinics", "name address phoneNo")
      .select("name phoneNo email clinics image qualification");

    res.status(200).json(dentists);
  } catch (error) {
    console.error("Error fetching dentists:", error);
    res.status(500).json({ message: "Failed to fetch dentists" });
  }
};

exports.startRegistration = async (req, res) => {
  console.log("TWILIO SID:", process.env.TWILIO_VERIFY_SERVICE_SID);
  try {
    const { name, phoneNo, email, password } = req.body;
console.log("the status is ",redis.status)
    if (!redis.status || redis.status !== "ready") {
      return res.status(503).json({ error: "Redis not ready" });
    }    

    const existingUser = await User.findOne({ phoneNo });
    if (existingUser) {
      return res.status(400).json({ error: "Phone number already registered" });
    }

    await client.verify.v2
      .services(process.env.TWILIO_VERIFY_SERVICE_SID)
      .verifications.create({ to: `+91${phoneNo}`, channel: "sms" });

      await redis.set(
        `pending:${phoneNo}`,
        JSON.stringify({ name, phoneNo, email, password }),
        "EX",
        300
      );
    res.json({ message: "OTP sent to mobile number" });
  } catch (err) {
    console.error("Error in /start-registration:", err);
    res.status(500).json({
      error: "Internal server error",
      details: process.env.NODE_ENV === "development" ? err.message : undefined,
    });
  }
};

exports.verifyRegistration = async (req, res) => {
  
  try {
    const { phoneNo, otp } = req.body;

    const verification = await client.verify.v2
      .services(process.env.TWILIO_VERIFY_SERVICE_SID)
      .verificationChecks.create({ to: `+91${phoneNo}`, code: otp });

    if (verification.status !== "approved") {
      return res.status(400).json({ error: "Invalid OTP" });
    }

    const data = await redis.get(`pending:${phoneNo}`);
    if (!data) {
      return res.status(400).json({ error: "OTP expired or invalid session" });
    }

    const { name, email, password } = JSON.parse(data);
    const hashedPassword = await bcrypt.hash(password, 10);

    // Generate unique patient ID
    const patientId = await generatePatientId();

    const patient = new User({
      name,
      phoneNo,
      email,
      password: hashedPassword,
      role: "patient",
      patientId,
    });
    await patient.save();
    await redis.del(`pending:${phoneNo}`);
    await Promise.all([
      sendWelcomeEmail(email, name),
      sendWhatsAppWelcome(phoneNo, name),
    ]);
    const accessToken = generateAccessToken(patient);
const refreshToken = generateRefreshToken(patient);

// --- Save session in Redis
const sessionData = {
  userAgent: req.headers["user-agent"],
  ip: req.ip,
  lastActive: new Date(),
};

await redis.set(
  `refreshToken:${patient._id}:${refreshToken}`,
  JSON.stringify(sessionData),
  "EX",
  7 * 24 * 60 * 60 // 7 days
);

// --- Set cookies
res.cookie("accessToken", accessToken, {
  httpOnly: true,
  secure: process.env.NODE_ENV === "production",
  sameSite: "lax",
  maxAge: 15 * 60 * 1000, // 15 mins
});

res.cookie("refreshToken", refreshToken, {
  httpOnly: true,
  secure: process.env.NODE_ENV === "production",
  sameSite: "lax",
  maxAge: 7 * 24 * 60 * 60 * 1000, 
});

res.status(201).json({
  message: "Patient registered successfully",
  user: {
    id: patient._id,
    name: patient.name,
    phoneNo: patient.phoneNo,
    role: "patient",
    image: patient.image,
  },
});
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

exports.registerPatient = async (req, res) => {
  try {
    const { name, phoneNo, email, password } = req.body;

    const existingUser = await User.findOne({ phoneNo });
    if (existingUser) {
      return res.status(400).json({ error: "Phone number already registered" });
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    
    // Generate unique patient ID
    const patientId = await generatePatientId();
    
    const patient = new User({
      name,
      phoneNo,
      email,
      password: hashedPassword,
      role: "patient",
      patientId,
    });
    await patient.save();
    res
      .status(201)
      .json({ message: "Patient registered successfully", patient });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.startPasswordReset = async (req, res) => {
  const { phoneNo } = req.body;

  try {
    const user = await User.findOne({ phoneNo });
    if (!user) {
      return res
        .status(404)
        .json({ error: "User with this phone number not found" });
    }

    await client.verify.v2
      .services(process.env.TWILIO_VERIFY_SERVICE_SID)
      .verifications.create({ to: `+91${phoneNo}`, channel: "sms" });

    res.json({ message: "OTP sent to your registered phone number" });
  } catch (err) {
    console.error("Error in /start-password-reset:", err);
    res.status(500).json({ error: "Internal server error" });
  }
};

exports.verifyAndResetPassword = async (req, res) => {
  const { phoneNo, otp, newPassword } = req.body;

  try {
    const verification = await client.verify.v2
      .services(process.env.TWILIO_VERIFY_SERVICE_SID)
      .verificationChecks.create({ to: `+91${phoneNo}`, code: otp });

    if (verification.status !== "approved") {
      return res.status(400).json({ error: "Invalid or expired OTP" });
    }
    const user = await User.findOne({ phoneNo });
    if (!user) {
      return res.status(404).json({ error: "User not found" });
    }

    user.password = await bcrypt.hash(newPassword, 10);
    await user.save();

    res.json({ message: "Password reset successfully" });
  } catch (err) {
    console.error("Error in /verify-reset-password:", err);
    res.status(500).json({ error: "Internal server error" });
  }
};

exports.registerDentist = async (req, res) => {
  try {
    const { name, phoneNo, email, password, qualification, image } = req.body;

    const existingUser = await User.findOne({ phoneNo });
    if (existingUser) {
      return res.status(400).json({ error: "Phone number already registered" });
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    const dentist = new User({
      name,
      phoneNo,
      email,
      password: hashedPassword,
      role: "dentist",
      qualification,
      image,
    });
    await dentist.save();

    res
      .status(201)
      .json({ message: "Dentist registered successfully", dentist });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.registerAdmin = async (req, res) => {
  try {
    const { name, phoneNo, email, password } = req.body;

    const existingUser = await User.findOne({ phoneNo });
    if (existingUser) {
      return res.status(400).json({ error: "Phone number already registered" });
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    const admin = new User({
      name,
      phoneNo,
      email,
      password: hashedPassword,
      role: "admin",
    });
    await admin.save();

    res.status(201).json({ message: "Admin registered successfully", admin });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.startClinicRegistration = async (req, res) => {
  try {
    const { name, phoneNo, clinicemail, clinicpassword, website } = req.body;

    if (!redis.status || redis.status !== "ready") {
      return res.status(503).json({ error: "Redis not ready" });
    }

    const existingUser = await User.findOne({ phoneNo });
    if (existingUser) {
      return res.status(400).json({ error: "Phone number already registered" });
    }

    await client.verify.v2
      .services(process.env.TWILIO_VERIFY_SERVICE_SID)
      .verifications.create({ to: `+91${phoneNo}`, channel: "sms" });

    await redis.set(
      `pending:clinic:${phoneNo}`,
      JSON.stringify({ name, phoneNo, email: clinicemail, password: clinicpassword, website }),
      "EX",
      300
    );

    res.json({ message: "OTP sent to mobile number" });
  } catch (err) {
    console.error("Error in /clinics/start-registration:", err);
    res.status(500).json({
      error: "Internal server error",
      details: process.env.NODE_ENV === "development" ? err.message : undefined,
    });
  }
};

exports.verifyClinicRegistration = async (req, res) => {
  try {
    const { phoneNo, otp } = req.body;

    const verification = await client.verify.v2
      .services(process.env.TWILIO_VERIFY_SERVICE_SID)
      .verificationChecks.create({ to: `+91${phoneNo}`, code: otp });

    if (verification.status !== "approved") {
      return res.status(400).json({ error: "Invalid OTP" });
    }

    const data = await redis.get(`pending:clinic:${phoneNo}`);
    if (!data) {
      return res.status(400).json({ error: "OTP expired or invalid session" });
    }

    const { name, email, password, website } = JSON.parse(data);

    // Check if user already exists
    const existingUser = await User.findOne({ phoneNo });
    if (existingUser) {
      return res.status(400).json({ error: "Phone number already registered" });
    }

    const hashedPassword = await bcrypt.hash(password, 10);

    const clinicUser = new User({
      name,
      phoneNo,
      email,
      password: hashedPassword,
      role: "clinic",
    });
    await clinicUser.save();
    await redis.del(`pending:clinic:${phoneNo}`);

    const accessToken = generateAccessToken(clinicUser);
    const refreshToken = generateRefreshToken(clinicUser);

    // Save session in Redis
    const sessionData = {
      userAgent: req.headers["user-agent"],
      ip: req.ip,
      lastActive: new Date(),
    };

    await redis.set(
      `refreshToken:${clinicUser._id}:${refreshToken}`,
      JSON.stringify(sessionData),
      "EX",
      7 * 24 * 60 * 60 // 7 days
    );

    // Set cookies
    res.cookie("accessToken", accessToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      maxAge: 15 * 60 * 1000, // 15 mins
    });

    res.cookie("refreshToken", refreshToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      maxAge: 7 * 24 * 60 * 60 * 1000, 
    });

    res.status(201).json({
      message: "Clinic registered successfully",
      user: {
        id: clinicUser._id,
        name: clinicUser.name,
        phoneNo: clinicUser.phoneNo,
        role: "clinic",
        image: clinicUser.image,
      },
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

exports.sendClinicOtp = async (req, res) => {
  try {
    const { phoneNo } = req.body;

    const user = await User.findOne({ phoneNo });
    if (!user) {
      return res.status(404).json({ error: "User not found" });
    }

    await client.verify.v2
      .services(process.env.TWILIO_VERIFY_SERVICE_SID)
      .verifications.create({ to: `+91${phoneNo}`, channel: "sms" });

    res.json({ message: "OTP sent to mobile number" });
  } catch (err) {
    console.error("Error in sendClinicOtp:", err);
    res.status(500).json({ error: "Internal server error" });
  }
};

exports.registerClinicUser = async (req, res) => {
  try {
    const { name, phoneNo, email, password } = req.body;

    // Validate required fields
    if (!name || !phoneNo || !password) {
      return res
        .status(400)
        .json({ error: "Name, phone number, and password are required." });
    }

    // Check if phone number already exists
    const existingUser = await User.findOne({ phoneNo });
    if (existingUser) {
      return res
        .status(409)
        .json({ error: "Phone number already registered." });
    }

    // Hash the password
    const hashedPassword = await bcrypt.hash(password, 10);

    // Create the user with role 'clinic'
    const user = new User({
      name,
      phoneNo,
      email,
      password: hashedPassword,
      role: "clinic",
    });

    await user.save();

    res.status(201).json({
      message: "Clinic user registered successfully",
      userId: user._id,
      name: user.name,
      phoneNo: user.phoneNo,
      email: user.email,
      role: user.role,
    });
  } catch (error) {
    console.error("Error in registerClinicUser:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

exports.login = async (req, res) => {
  try {
    const { phoneNo, password } = req.body;

    const user = await User.findOne({ phoneNo });
    if (!user) {
      return res
        .status(400)
        .json({ error: "Invalid phone number or password" });
    }

    const isPasswordValid = await bcrypt.compare(password, user.password);
    if (!isPasswordValid) {
      return res
        .status(400)
        .json({ error: "Invalid phone number or password" });
    }

    const token = jwt.sign({ id: user._id, role: user.role }, JWT_SECRET, {
      expiresIn: "1d",
    });
    user.lastLogin = new Date();
    await user.save();

    res.status(200).json({
      message: "Login successful",
      token,
      user: {
        id: user._id,
        name: user.name,
        phoneNo: user.phoneNo,
        role: user.role,
        lastLogin: user.lastLogin,
        image: user.image,
      },
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.startOtpLogin = async (req, res) => {
  try {
    const { phoneNo } = req.body;

    const user = await User.findOne({ phoneNo });
    if (!user) {
      return res.status(404).json({ error: "User not found" });
    }

    await client.verify.v2
      .services(process.env.TWILIO_VERIFY_SERVICE_SID)
      .verifications.create({ to: `+91${phoneNo}`, channel: "sms" });

    res.json({ message: "OTP sent to mobile number" });
  } catch (err) {
    console.error("Error in startOtpLogin:", err);
    res.status(500).json({ error: "Internal server error" });
  }
};

exports.verifyOtpLogin = async (req, res) => {
  try {
    const { phoneNo, otp } = req.body;

    const verification = await client.verify.v2
      .services(process.env.TWILIO_VERIFY_SERVICE_SID)
      .verificationChecks.create({ to: `+91${phoneNo}`, code: otp });

    if (verification.status !== "approved") {
      return res.status(400).json({ error: "Invalid OTP" });
    }

    const user = await User.findOne({ phoneNo });
    if (!user) {
      return res.status(404).json({ error: "User not found" });
    }

    // --- Generate Tokens ---
    const accessToken = generateAccessToken(user);
    const refreshToken = generateRefreshToken(user);

    // --- Store session in Redis ---
    const sessionData = {
      userAgent: req.headers["user-agent"],
      ip: req.ip,
      lastActive: new Date(),
    };

    await redis.set(
      `refreshToken:${user._id}:${refreshToken}`,
      JSON.stringify(sessionData),
      "EX",
      7 * 24 * 60 * 60 // 7 days
    );

    // --- Update last login ---
    user.lastLogin = new Date();
    await user.save();

    // --- Set cookies ---
    res.cookie("accessToken", accessToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      maxAge: 15 * 60 * 1000, // 15 minutes
    });

    res.cookie("refreshToken", refreshToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      maxAge: 7 * 24 * 60 * 60 * 1000, // 7 days
    });

    // --- Final response ---
    res.status(200).json({
      message: "OTP Login successful",
      user: {
        id: user._id,
        name: user.name,
        phoneNo: user.phoneNo,
        role: user.role,
        lastLogin: user.lastLogin,
        image: user.image,
        ...(user.role === "clinic" &&
          user.clinics?.length > 0 && {
            clinicId: user.clinics[0],
          }),
      },
    });
  } catch (err) {
    console.error("Error in verifyOtpLogin:", err);
    res.status(500).json({ error: "Internal server error" });
  }
};


exports.getPatients = async (req, res) => {
  try {
    const patients = await User.find({ role: "patient" });
    res.status(200).json(patients);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.getDentists = async (req, res) => {
  try {
    const dentists = await User.find({ role: "dentist" });
    res.status(200).json(dentists);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.getAdmins = async (req, res) => {
  try {
    const admins = await User.find({ role: "admin" });
    res.status(200).json(admins);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.loginew = async (req, res) => {
  try {
    const { phoneNo, email, password } = req.body;
    
    console.log('🔐 Login attempt:', { phoneNo, email, role: 'admin' });
    
    // Determine login identifier (phone or email)
    const loginIdentifier = email || phoneNo;
    const loginAttemptsKey = `loginAttempts:${loginIdentifier}`;
    const attempts = await redis.incr(loginAttemptsKey);
    if (attempts === 1) {
      await redis.expire(loginAttemptsKey, 3600);
    }
    if (attempts > 5) {
      return res
        .status(429)
        .json({ error: "Too many attempts. Try again later." });
    }

    // Find user by email or phone number
    let user;
    if (email) {
      user = await User.findOne({ email });
    } else if (phoneNo) {
      user = await User.findOne({ phoneNo });
    }

    if (!user) {
      console.log("❌ User not found for:", loginIdentifier);
      return res
        .status(400)
        .json({ error: "Invalid credentials" });
    }

    console.log('👤 User found:', { 
      id: user._id, 
      name: user.name, 
      email: user.email, 
      phoneNo: user.phoneNo, 
      role: user.role,
      adminType: user.adminType 
    });

    const isPasswordValid = await bcrypt.compare(password, user.password);
    if (!isPasswordValid) {
      console.log("❌ Password invalid for user:", user.email);
      return res
        .status(400)
        .json({ error: "Invalid credentials" });
    }

    console.log('✅ Login successful for user:', user.email, 'Role:', user.role);
    await redis.del(loginAttemptsKey);

    const accessToken = generateAccessToken(user);
    const refreshToken = generateRefreshToken(user);
    const sessionData = {
      userAgent: req.headers["user-agent"],
      ip: req.ip,
      lastActive: new Date(),
    };

    await redis.set(
      `refreshToken:${user._id}:${refreshToken}`,
      JSON.stringify(sessionData),
      "EX",
      7 * 24 * 60 * 60
    );

    user.lastLogin = new Date();
    await user.save();

    res.cookie("accessToken", accessToken, {
      httpOnly: true,
      secure: false, // Important for localhost
      sameSite: "lax",
      maxAge: 15 * 60 * 1000,
    });

    res.cookie("refreshToken", refreshToken, {
      httpOnly: true,
      secure: false,
      sameSite: "lax",
      maxAge: 7 * 24 * 60 * 60 * 1000,
    });

    res.status(200).json({
      message: "Login successful",
      user: {
        id: user._id,
        name: user.name,
        phoneNo: user.phoneNo,
        role: user.role,
        lastLogin: user.lastLogin,
        image: user.image,
        ...(user.role === "clinic" &&
          user.clinics?.length > 0 && {
            clinicId: user.clinics[0],
          }),
      },
    });
  } catch (error) {
    console.error("Login error:", error.message);
    res.status(500).json({ error: error.message });
  }
};

exports.logout = async (req, res) => {
  try {
    const { refreshToken } = req.cookies;

    if (refreshToken) {
      const decoded = jwt.verify(
        refreshToken,
        process.env.REFRESH_TOKEN_SECRET
      );
      await redis.del(`refreshToken:${decoded.id}:${refreshToken}`);
    }

    // Clear cookies
    res.clearCookie("accessToken");
    res.clearCookie("refreshToken");

    res.status(200).json({ message: "Logout successful" });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.refreshToken = async (req, res) => {
  try {
    const { refreshToken } = req.cookies;

    if (!refreshToken) {
      return res.status(401).json({ error: "No refresh token provided" });
    }

    const decoded = jwt.verify(refreshToken, process.env.REFRESH_TOKEN_SECRET);

    const redisKey = `refreshToken:${decoded.id}:${refreshToken}`;
    const sessionData = await redis.get(redisKey);

    if (!sessionData) {
      return res.status(401).json({ error: "Invalid refresh token" });
    }

    const user = await User.findById(decoded.id);
    if (!user) {
      return res.status(401).json({ error: "User not found" });
    }

    const newAccessToken = generateAccessToken(user);
    const newRefreshToken = generateRefreshToken(user);

    // Set new token in Redis
    const newRedisKey = `refreshToken:${user._id}:${newRefreshToken}`;
    await redis.set(newRedisKey, sessionData, "EX", 7 * 24 * 60 * 60);

    // Delete old refresh token
    await redis.del(redisKey);

    // Set cookies
    res.cookie("accessToken", newAccessToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "strict",
      maxAge: 15 * 60 * 1000,
    });

    res.cookie("refreshToken", newRefreshToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "strict",
      maxAge: 7 * 24 * 60 * 60 * 1000,
    });

    res.status(200).json({ message: "Tokens refreshed successfully" });
  } catch (error) {
    console.error("🔥 Refresh token error:", error.message);
    if (error.name === "JsonWebTokenError") {
      return res.status(401).json({ error: "Invalid token" });
    }
    if (error.name === "TokenExpiredError") {
      return res.status(401).json({ error: "Token expired" });
    }
    res.status(500).json({ error: error.message });
  }
};
