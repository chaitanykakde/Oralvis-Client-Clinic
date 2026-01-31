
require('dotenv').config();
const twilio = require("twilio");

const twilioAccountSid = process.env.TWILIO_ACCOUNT_SID;
const twilioAuthToken = process.env.TWILIO_AUTH_TOKEN;
const twilioNumber = process.env.TWILIO_WHATSAPP_FROM; 

const client = twilio(twilioAccountSid, twilioAuthToken);


const templateSid = process.env.TWILIO_WHATSAPP_TEMPLATE_2_SID;

const sendBookingConfirmWhatsApp = async (whatsappNumber, date, time, clinicName) => {

  try {
    const toNumber = `whatsapp:+91${whatsappNumber}`;

    const msg = await client.messages.create({
      from: `whatsapp:${process.env.TWILIO_WHATSAPP_FROM}`,
      to: toNumber,
      contentSid: templateSid,
      contentVariables: JSON.stringify({
        "1": date,
        "2": time,
        "3": clinicName,
      }),
    });
    console.log("✅ WhatsApp confirmation sent:", msg.sid);
    console.log("sent you ",toNumber)
    console.log("sendt",whatsappNumber)
  } catch (error) {
    console.error("❌ Error sending WhatsApp confirmation:", error);
  }
};

module.exports = sendBookingConfirmWhatsApp;
