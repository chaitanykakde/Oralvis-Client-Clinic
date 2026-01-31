const User = require("../models/user");
const WalkinPatient = require("../models/walkin");

/**
 * Generates a unique patient ID in format PAT-XXXXXX
 * where XXXXXX is a 6-digit number
 */
const generatePatientId = async () => {
  let patientId;
  let isUnique = false;
  let attempts = 0;
  const maxAttempts = 100;

  while (!isUnique && attempts < maxAttempts) {
    // Generate 6-digit random number
    const randomNum = Math.floor(100000 + Math.random() * 900000);
    patientId = `PAT-${randomNum}`;

    // Check if it exists in User collection (for regular patients)
    const existingUser = await User.findOne({ patientId });
    
    // Check if it exists in WalkinPatient collection (for walk-in patients)
    const existingWalkin = await WalkinPatient.findOne({ patientId });

    if (!existingUser && !existingWalkin) {
      isUnique = true;
    }
    attempts++;
  }

  if (!isUnique) {
    // Fallback: use timestamp-based ID if we can't find a unique random one
    const timestamp = Date.now().toString().slice(-6);
    patientId = `PAT-${timestamp}`;
  }

  return patientId;
};

module.exports = generatePatientId;


