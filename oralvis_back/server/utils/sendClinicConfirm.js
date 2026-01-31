const twilio = require("twilio");

const accountSid = process.env.TWILIO_ACCOUNT_SID;
const authToken = process.env.TWILIO_AUTH_TOKEN;

const client = twilio(accountSid, authToken);

const sendClinicConfirm = async (whatsappNumber, patientName, date, time,link) => {
  try {
    const payload = {
      from: `whatsapp:${process.env.TWILIO_WHATSAPP_FROM}`, // Twilio Business Number
      to: `whatsapp:+${whatsappNumber}`,
      contentSid: process.env.TWILIO_CLINIC_TEMPLATE_SID,
      contentVariables: JSON.stringify({
        "1": patientName,
        "2": date,
        "3": time,
        "4":link
      }),
    };
    // Log the payload for debugging
    console.log("📤 Sending Clinic Confirmation:", payload);

    const msg = await client.messages.create(payload);
    console.log("✅ Clinic confirmation sent:", msg.sid);
  } catch (error) {
    console.error("❌ Error sending clinic confirmation:", error);
  }
};

module.exports = sendClinicConfirm;
