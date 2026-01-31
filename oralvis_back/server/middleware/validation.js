const { body, validationResult } = require('express-validator');

// Validation middleware
const handleValidationErrors = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({
      error: 'Validation failed',
      details: errors.array().map(err => ({
        field: err.path,
        message: err.msg,
        value: err.value
      }))
    });
  }
  next();
};

// Common validation rules
const phoneValidation = body('phoneNo')
  .isLength({ min: 10, max: 10 })
  .withMessage('Phone number must be exactly 10 digits')
  .matches(/^[6-9]\d{9}$/)
  .withMessage('Please enter a valid Indian phone number');

const emailValidation = body('email')
  .isEmail()
  .withMessage('Please enter a valid email address')
  .normalizeEmail();

const passwordValidation = body('password')
  .isLength({ min: 6, max: 50 })
  .withMessage('Password must be between 6 and 50 characters')
  .matches(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/)
  .withMessage('Password must contain at least one uppercase letter, one lowercase letter, and one number');

const nameValidation = body('name')
  .isLength({ min: 2, max: 100 })
  .withMessage('Name must be between 2 and 100 characters')
  .matches(/^[a-zA-Z\s]+$/)
  .withMessage('Name can only contain letters and spaces');

// Registration validation
const registrationValidation = [
  nameValidation,
  phoneValidation,
  emailValidation,
  passwordValidation,
  handleValidationErrors
];

// Login validation (relaxed to support existing clinic creds)
const loginValidation = [
  body('phoneNo')
    .optional()
    .isLength({ min: 10, max: 10 })
    .withMessage('Phone number must be exactly 10 digits')
    .matches(/^[6-9]\d{9}$/)
    .withMessage('Please enter a valid Indian phone number'),
  body('email')
    .optional()
    .isEmail()
    .withMessage('Please enter a valid email address')
    .normalizeEmail(),
  body('password')
    .notEmpty()
    .withMessage('Password is required'),
  // Ensure at least one of email or phoneNo is provided
  (req, res, next) => {
    const { email, phoneNo } = req.body || {};
    if (!email && !phoneNo) {
      return res.status(400).json({ error: 'Either email or phone number is required' });
    }
    next();
  },
  handleValidationErrors
];

// Clinic registration validation
const clinicRegistrationValidation = [
  body('name')
    .isLength({ min: 2, max: 100 })
    .withMessage('Clinic name must be between 2 and 100 characters'),
  phoneValidation,
  body('clinicemail')
    .isEmail()
    .withMessage('Please enter a valid email address')
    .normalizeEmail(),
  body('clinicpassword')
    .isLength({ min: 6, max: 50 })
    .withMessage('Password must be between 6 and 50 characters'),
  body('website')
    .optional()
    .isURL()
    .withMessage('Please enter a valid website URL'),
  handleValidationErrors
];

// OTP validation
const otpValidation = [
  body('otp')
    .isLength({ min: 4, max: 6 })
    .withMessage('OTP must be between 4 and 6 digits')
    .isNumeric()
    .withMessage('OTP must contain only numbers'),
  handleValidationErrors
];

module.exports = {
  registrationValidation,
  loginValidation,
  clinicRegistrationValidation,
  otpValidation,
  handleValidationErrors,
  phoneValidation,
  emailValidation,
  passwordValidation,
  nameValidation
};
