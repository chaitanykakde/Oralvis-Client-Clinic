// utils/clinicapprvalwhatsapp.js
const twilio = require("twilio");
const client = twilio(process.env.TWILIO_ACCOUNT_SID, process.env.TWILIO_AUTH_TOKEN);

const sendClinicApprovalWhatsApp = async (phoneNo, clinicName) => {
  const payload = {
    from: `whatsapp:${process.env.TWILIO_WHATSAPP_FROM}`, 
    to: `whatsapp:+${phoneNo}`,
    contentSid: process.env.TWILIO_CLINIC_APPROVAL_TEMPLATE_SID,
    contentVariables: JSON.stringify({
      "1": clinicName,
    }),
  };

  try {
    const msg = await client.messages.create(payload);
    console.log("✅ WhatsApp approval message sent successfully");
    console.log("📦 Payload:", payload);
    console.log("📨 Twilio SID:", msg.sid);
  } catch (err) {
    console.error("❌ Error sending WhatsApp approval message");
    console.error("📦 Payload:", payload);
    console.error("🛑 Error:", err.message);
  }
};

module.exports = sendClinicApprovalWhatsApp;
