const twilio = require("twilio");
require('dotenv').config();
const client = twilio(
  process.env.TWILIO_ACCOUNT_SID,
  process.env.TWILIO_AUTH_TOKEN
);

async function sendWhatsAppWelcome(phoneNo, name) {
  console.log("the message is ",process.env.TWILIO_WHATSAPP_FROM);
  try {
    const message = await client.messages.create({
      from: `whatsapp:${process.env.TWILIO_WHATSAPP_FROM}`,
      to: `whatsapp:+91${phoneNo}`,
      contentSid: process.env.TWILIO_WHATSAPP_TEMPLATE_SID,
    });

    console.log("WhatsApp message sent:", message.sid);
  } catch (err) {
    console.log("te sid ",process.env.TWILIO_WHATSAPP_TEMPLATE_SID)
    console.error("Failed to send WhatsApp message:", err.message);
  }
}

module.exports = { sendWhatsAppWelcome };
