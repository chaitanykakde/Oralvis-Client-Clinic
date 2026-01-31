const AWS = require("aws-sdk");
const fs = require("fs");
const path = require("path");

const s3 = new AWS.S3({
  accessKeyId: process.env.AWS_ACCESS_KEY,
  secretAccessKey: process.env.AWS_SECRET_KEY,
  region: process.env.AWS_REGION,
});

const uploadToS3 = async (filePath, originalFileName, { userName, phoneNo, buffer }) => {
  const last5 = phoneNo.slice(-5);
  const folder = `oralvis-images/${userName}_${last5}`;
  const fileName = `${folder}/${Date.now()}-${path.basename(originalFileName)}`;

  // Support both disk files (filePath) and in-memory uploads (buffer)
  const fileContent = buffer && Buffer.isBuffer(buffer)
    ? buffer
    : fs.readFileSync(filePath);

  const params = {
    Bucket: process.env.AWS_BUCKET_NAME,
    Key: fileName,
    Body: fileContent,
    ContentType: "image/jpeg",
  };

  const data = await s3.upload(params).promise();
  return data.Location;
};


module.exports = uploadToS3;
