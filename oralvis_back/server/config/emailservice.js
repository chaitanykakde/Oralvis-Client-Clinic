// emailService.js
const nodemailer = require("nodemailer");

const transporter = nodemailer.createTransport({
  service: "gmail", 
  auth: {
    user: process.env.EMAIL_USER,     
    pass: process.env.EMAIL_PASSWORD, 
  },
});

const sendWelcomeEmail = async (toEmail, userName) => {
  const mailOptions = {
    from: `"OralVis Healthcare" <${process.env.EMAIL_USER}>`,
    to: toEmail,
    subject: "Welcome to OralVis!",
    html: `
      <h2>Welcome, ${userName}!</h2>
      <p>Thank you for registering with OralVis. We're excited to have you onboard.</p>
      <p>You can now access dental consultations, diagnostics, and more from top clinics.</p>
      <br/>
      <p>Stay smiling,</p>
      <p>Team OralVis</p>
    `,
  };

  await transporter.sendMail(mailOptions);
};

module.exports = { sendWelcomeEmail };
