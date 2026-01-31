const mongoose =require('mongoose');

const SectionSchema = new mongoose.Schema({
  title: { type: String, required: true },
  points: { type: [String], required: true },
  image: { type: String }, // S3 URL
});
const FinalTipSchema = new mongoose.Schema({
  bold: { type: String, required: true },
  soft: { type: String },
}, { _id: false });
const BlogSchema = new mongoose.Schema(
  {
    title: { type: String, required: true },
    image: { type: String, required: true },
    intro: { type: String, required: true },
    sections: { type: [SectionSchema], required: true },
    finalTip: { type: FinalTipSchema },
  },
  { timestamps: true }
);

// Add index to speed up queries
BlogSchema.index({ title: 1, intro: 1 });

module.exports = mongoose.model('Blog', BlogSchema);

