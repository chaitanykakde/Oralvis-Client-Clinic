const twilio = require("twilio");
require("dotenv").config();

const client = twilio(
  process.env.TWILIO_ACCOUNT_SID,
  process.env.TWILIO_AUTH_TOKEN
);

const sendPdfWhatsapp = async (phoneNumber, pdfUrl) => {


  try {
    console.log("the pdf url is ", pdfUrl);
    console.log("the phone number is ", phoneNumber);
    const fileNameOnly = pdfUrl.split("/").pop().replace(".pdf", "");
    console.log("the file name is ", );
    const payload = {
      from: `whatsapp:${process.env.TWILIO_WHATSAPP_FROM}`,
      to: `whatsapp:+91${phoneNumber}`,
      contentSid: process.env.TWILIO_WHATSAPP_TEMPLATE_3_SID,
      contentVariables: JSON.stringify({
        1: fileNameOnly
      }),
    };

    const message = await client.messages.create(payload);
    console.log(`✅ WhatsApp PDF sent: ${message.sid}`);
    return message.sid;
  } catch (error) {
    console.error("❌ Failed to send WhatsApp PDF:", error);
    throw error;
  }
};


module.exports = sendPdfWhatsapp;
