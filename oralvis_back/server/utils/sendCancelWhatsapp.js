const twilio = require("twilio");

const accountSid = process.env.TWILIO_ACCOUNT_SID;
const authToken = process.env.TWILIO_AUTH_TOKEN;

const client = twilio(accountSid, authToken);

const sendCancelWhatsApp = async (
  phoneNumber,
  patientName,
  date,
  time,
  clinicName,
  link
) => {
  const payload = {
    from: `whatsapp:${process.env.TWILIO_WHATSAPP_FROM}`,
    to: `whatsapp:+91${phoneNumber}`,
    contentSid: process.env.CANCEL_TEMPLATE_SID,
    contentVariables: JSON.stringify({
      1: patientName,
      2: date,
      3: time,
      4: clinicName,
      5: link,
    }),
  };
  
  // 👇 Log the payload
  console.log("📩 Sending Cancel WhatsApp:", payload);

  try {
    await client.messages.create(payload);
    console.log(`✅ Cancellation WhatsApp sent to ${phoneNumber}`);
  } catch (error) {
    console.error(`❌ Failed to send cancellation WhatsApp: ${error.message}`);
  }
};

module.exports = sendCancelWhatsApp;
