// models/Scan.js
const mongoose = require('mongoose');

const scanSchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
  },
  images: [
    {
      label: String, 
      url: String,  
    },
  ],
  createdAt: {
    type: Date,
    default: Date.now,
  },
});


scanSchema.index({ user: 1, createdAt: -1 });
module.exports = mongoose.model('Scan', scanSchema);
