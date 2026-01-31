const AWS = require('../config/aws'); 
const sharp = require('sharp');

const uploadProfilePicToS3 = async (fileBuffer, fileName) => {
  const compressedBuffer = await sharp(fileBuffer)
    .resize(300, 300) // optional
    .jpeg({ quality: 70 }) // compress quality
    .toBuffer();

  const params = {
    Bucket: process.env.AWS_BUCKET_NAME,
    Key: `patientsprofile/${fileName}`,
    Body: compressedBuffer,
    ContentType: 'image/jpeg',
  };

  const result = await AWS.upload(params).promise();
  return result.Location; 
};

module.exports = { uploadProfilePicToS3 };
