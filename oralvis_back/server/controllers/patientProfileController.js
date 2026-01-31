const Patient = require("../models/patient");
const User = require("../models/user");
const { uploadProfilePicToS3 } = require("../utils/uploadProfile");
// GET /patient/profile/:userId
exports.getPatientProfile = async (req, res) => {
  const { userId } = req.params;

  try {
    const user = await User.findById(userId).select("name email phoneNo");
    const patient = await Patient.findOne({ user: userId });

    if (!user) return res.status(404).json({ error: "User not found" });

    res.json({
      user,
      patient,
    });
  } catch (err) {
    console.error("Error fetching profile:", err);
    res.status(500).json({ error: "Failed to fetch profile" });
  }
};

// exports.updatePatientProfile = async (req, res) => {
//   const { userId } = req.params;
//   const { name, phoneNo, address, age, gender } = req.body;

//   try {
//     const user = await User.findByIdAndUpdate(
//       userId,
//       { name, phoneNo },
//       { new: true }
//     );

//     let patient = await Patient.findOne({ user: userId });

//     if (patient) {
//       patient.address = address;
//       patient.age = age;
//       patient.gender = gender;
//       await patient.save();
//     } else {
//       patient = await Patient.create({
//         user: userId,
//         address,
//         age,
//         gender,
//       });
//     }

//     res.json({ user, patient });
//   } catch (err) {
//     console.error("Error updating profile:", err);
//     res.status(500).json({ error: "Failed to update profile" });
//   }
// };

// exports.updatePatientProfile = async (req, res) => {
//   const { userId } = req.params;
//   const { name, phoneNo, address, age, gender } = req.body;

//   try {
//     let imageUrl;

//     if (req.file) {
//       const safeName = name.replace(/\s+/g, "_").toLowerCase();
//       const fileName = `${safeName}_${Date.now()}.jpg`;
//       imageUrl = await uploadProfilePicToS3(req.file.buffer, fileName);
//     }

//     const userUpdateFields = { name, phoneNo };
//     if (imageUrl) userUpdateFields.imageUrl = imageUrl;

//     const user = await User.findByIdAndUpdate(userId, userUpdateFields, {
//       new: true,
//     });

//     let patient = await Patient.findOne({ user: userId });

//     if (patient) {
//       patient.address = address;
//       patient.age = age;
//       patient.gender = gender;
//       if (imageUrl) patient.imageUrl = imageUrl;
//       await patient.save();
//     } else {
//       patient = await Patient.create({
//         user: userId,
//         address,
//         age,
//         gender,
//         imageUrl: imageUrl || "",
//       });
//     }

//     res.json({ user, patient });
//   } catch (err) {
//     console.error("Error updating profile:", err);
//     res.status(500).json({ error: "Failed to update profile" });
//   }
// };

exports.updatePatientProfile = async (req, res) => {
  const { userId } = req.params;
  const { name, phoneNo, address, age, gender } = req.body;

  try {
    let imageUrl;

    if (req.file) {
      const safeName = name.replace(/\s+/g, "_").toLowerCase();
      const fileName = `${safeName}_${Date.now()}.jpg`;
      imageUrl = await uploadProfilePicToS3(req.file.buffer, fileName);
    }

    // Update user
    const userUpdateFields = { name, phoneNo };
    if (imageUrl) userUpdateFields.image = imageUrl; // ✅ Use 'image' not 'imageUrl'

    const user = await User.findByIdAndUpdate(userId, userUpdateFields, {
      new: true,
    });

    // Update or create patient
    let patient = await Patient.findOne({ user: userId });

    if (patient) {
      patient.address = address;
      patient.age = age;
      patient.gender = gender;
      await patient.save();
    } else {
      patient = await Patient.create({
        user: userId,
        address,
        age,
        gender,
      });
    }

    res.json({ user, patient });
  } catch (err) {
    console.error("Error updating profile:", err);
    res.status(500).json({ error: "Failed to update profile" });
  }
};

exports.getPatientImageUrl = async (req, res) => {
  const { userId } = req.params;

  try {
    const patient = await Patient.findOne({ user: userId }).select("imageUrl");

    if (!patient) {
      return res.status(404).json({ error: "Patient not found" });
    }

    res.json({ imageUrl: patient.imageUrl });
  } catch (err) {
    console.error("Error fetching profile image:", err);
    res.status(500).json({ error: "Server error" });
  }
};
