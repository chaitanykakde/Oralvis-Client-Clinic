const AWS = require("aws-sdk");

const s3 = new AWS.S3({
  accessKeyId: process.env.AWS_ACCESS_KEY,
  secretAccessKey: process.env.AWS_SECRET_KEY,
  region: process.env.AWS_REGION,
});

const uploadPdfToS3 = async (buffer, fileName) => {
  const params = {
    Bucket: process.env.AWS_BUCKET_NAME,
    Key: `oralvis-reports/${fileName}`,
    Body: buffer,
    ContentType: "application/pdf",
  };

  const data = await s3.upload(params).promise();
  return data.Location;
};

module.exports = uploadPdfToS3;
