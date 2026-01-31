// controllers/scanController.js
const Scan = require('../models/scan');
const mongoose = require('mongoose');
const uploadScanImages = async (req, res) => {
  try {
    const { userId } = req.body;
    if (!userId || !mongoose.Types.ObjectId.isValid(userId)|| !req.files) {
      return res.status(400).json({ error: 'Missing userId or files' });
    }

    const labels = [
      'Front teeth (closed bite)',
      'Right side front teeth (closed bite)',
      'Left side front teeth (closed bite)',
      'Upper jaw (maxillary occlusal view)',
      'Lower jaw (mandibular occlusal view)',
      'Right cheek (buccal view)',
      'Left cheek (buccal view)',
    ];

    const images = labels.map((label, index) => ({
      label,
      url: req.files[`image_${index + 1}`]?.[0]?.location || '',
    }));

    const scan = new Scan({
      user: userId,
      images,
    });

    await scan.save();
    res.status(201).json({ message: 'Images uploaded successfully', scan });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to upload images' });
  }
};

module.exports = { uploadScanImages };
