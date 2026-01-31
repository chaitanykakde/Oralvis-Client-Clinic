// middleware/upload.js
const multer = require("multer");
const multerS3 = require("multer-s3");
const s3 = require("../config/aws");

const awsupload = multer({
  storage: multerS3({
    s3,
    bucket: process.env.AWS_BUCKET_NAME,
    metadata: (req, file, cb) => {
      cb(null, { fieldName: file.fieldname });
    },
    key: (req, file, cb) => {
      const userName = req.query.userName;
      const phoneNo = req.query.phoneNo;
      const last5 = phoneNo.slice(-5);
      const folder = `oralvis-images/${userName}_${last5}`;
      const fileName = `${folder}/${Date.now()}-${file.originalname}`;

      cb(null, fileName);
    },
  }),
});

module.exports = awsupload;
