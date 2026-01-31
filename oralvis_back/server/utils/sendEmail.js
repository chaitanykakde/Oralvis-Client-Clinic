const nodemailer = require("nodemailer");

const sendEmail = async ({ to, subject, text, attachments }) => {
  const transporter = nodemailer.createTransport({
    service: "gmail",
    auth: {
      user: process.env.EMAIL_USER,
      pass: process.env.EMAIL_PASSWORD,
    },
  });

  await transporter.sendMail({
    from: `"OralVis" <${process.env.EMAIL_USER}>`,
    to,
    subject,
    text,
    attachments,
  });
};

module.exports = sendEmail;
