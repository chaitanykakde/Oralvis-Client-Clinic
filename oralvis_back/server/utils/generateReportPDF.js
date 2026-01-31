const PDFDocument = require("pdfkit");
const axios = require("axios");
const path = require("path");
const fs = require("fs");
const os = require("os");
const sharp = require("sharp");
const { createCanvas, loadImage } = require("canvas");

const generateReportPDF = async (report) => {
  const createdAt = new Date(report.createdAt);
  const formattedDate = `${String(createdAt.getDate()).padStart(
    2,
    "0"
  )}/${String(createdAt.getMonth() + 1).padStart(
    2,
    "0"
  )}/${createdAt.getFullYear()}`;
  const doc = new PDFDocument({ autoFirstPage: true, margin: 40 });
  const buffers = [];

  return new Promise(async (resolve, reject) => {
    doc.on("data", (chunk) => buffers.push(chunk));
    doc.on("end", () => resolve(Buffer.concat(buffers)));
    doc.on("error", reject);

    const patient = report.patientId || {};

    const logoPath = path.join(__dirname, "../assets/logo.png");

    const titleY = doc.y;

    try {
      doc.image(logoPath, 40, titleY, { width: 50 });
    } catch (e) {
      console.error("Failed to load logo image:", e);
    }

    doc.fontSize(22).fillColor("#004d40").font("Helvetica-Bold");
    doc.text("OralVis Dental Report", 180, titleY);
    const contactX = 470;
    const contactY = titleY;

    doc
      .fontSize(8)
      .fillColor("black")
      .font("Helvetica")
      .text("Hyderabad, Telangana 502285", contactX - 20, contactY)
      .text("+91 8670736828", contactX + 25, contactY + 12)
      .text("office@oralvis.com", contactX + 20, contactY + 24)
      .text("www.oralvis.com", contactX + 25, contactY + 36);
    doc.moveDown();
    // Draw container box
    const boxTop = doc.y;
    const boxLeft = 40;
    const boxWidth = 515; // Total width (555 - 40)
    const boxHeight = 40;

    doc
      .lineWidth(1)
      .strokeColor("#cccccc")
      .rect(boxLeft, boxTop, boxWidth, boxHeight)
      .stroke();

    // Add text inside box
    const padding = 10;
    const textY = boxTop + padding;
    doc
      .font("Helvetica-Bold")
      .fontSize(10)
      .fillColor("black")
      .text(`Name: ${patient.name || "N/A"}`, boxLeft + padding, textY, {
        width: 150,
      });

    doc.text(`Phone: ${patient.phoneNo || "-"}`, boxLeft + 200, textY, {
      width: 150,
    });

    doc.text(`Date: ${formattedDate}`, boxLeft + 370, textY, {
      width: 120,
    });

    doc.y = boxTop + boxHeight + 10;

    doc.moveDown(1);
    doc
      .lineWidth(1)
      .strokeColor("#cccccc")
      .moveTo(40, doc.y)
      .lineTo(555, doc.y)
      .stroke();
    doc.moveDown(1);
    doc.moveDown(0.5);
    doc.font("Helvetica-Bold").fontSize(11).fillColor("black");
    doc.text("Chief Complaint:", 40, doc.y, { continued: true });
    doc.font("Helvetica").text(` ${report.chiefComplaint || "N/A"}`);
    doc.moveDown(1);
    doc.moveDown(1);
    // AI SCREENING REPORT - Left Aligned
    doc.fillColor("black").font("Helvetica-Bold").fontSize(13);
    doc.text("Expert Screening Report:", 40);

    const results = report.aiDiagnosis?.results?.slice(0, 3) || [];
    const imageLabels = ["front Teeth", "upper Teeth", "lower Teeth"];

    // Target box for each image (scaled up 1.5x, no distortion, no hard crop)
    const baseWidth = 160;
    const targetWidth = baseWidth * 1.5; // 240
    const targetHeight = Math.round((targetWidth * 9) / 16); // 135
    // Calculate spacing to fit all 3 images in one row
    // PDFKit default page width is 612 (US Letter), with 40px margins = 532 usable width
    const pageWidth = 612; // Standard US Letter width
    const leftMargin = 40;
    const rightMargin = 40;
    const usableWidth = pageWidth - leftMargin - rightMargin; // 532
    const totalImageWidth = targetWidth * 3; // 720
    // Calculate spacing - if images don't fit, use minimal spacing (5px)
    // If still doesn't fit, the images will be slightly larger than page (PDFKit will handle)
    const spacing = totalImageWidth > usableWidth 
      ? 5 // Minimal spacing if tight fit
      : Math.floor((usableWidth - totalImageWidth) / 2); // Distribute extra space as spacing
    const startY = doc.y;

    let allLabels = new Set();
    let maxImageBottomY = startY + targetHeight + 20; // Track the bottom of images section

    for (let i = 0; i < results.length; i++) {
      const result = results[i];
      // Center the images if they don't fill the full width, then shift 100px left (90px + 10px more)
      const totalWidth = (targetWidth * 3) + (spacing * 2);
      const startX = leftMargin + (totalWidth < usableWidth ? (usableWidth - totalWidth) / 2 : 0) - 100;
      const x = startX + i * (targetWidth + spacing);

      try {
        // Fallbacks for missing imageUrl
        const candidateUrl = result.imageUrl || (Array.isArray(report.images) ? report.images[i] : null);
        if (!candidateUrl) {
          doc.fillColor("red").fontSize(10).text("Image unavailable.", x, startY);
          continue;
        }

        const response = await axios.get(candidateUrl, {
          responseType: "arraybuffer",
        });

        const image = await loadImage(response.data);
        const canvas = createCanvas(image.width, image.height);
        const ctx = canvas.getContext("2d");
        ctx.fillStyle = "#ffffff";
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        ctx.drawImage(image, 0, 0);

        (result.annotations || []).forEach((anno) => {
          ctx.strokeStyle = anno.color || "#FF0000";
          ctx.lineWidth = 2;
          ctx.beginPath();
          const start = anno.points[0];
          ctx.moveTo(start.x * image.width, start.y * image.height);
          for (let j = 1; j < anno.points.length; j++) {
            const p = anno.points[j];
            ctx.lineTo(p.x * image.width, p.y * image.height);
          }
          ctx.stroke();
          if (anno.label) allLabels.add(anno.label);
        });

        // Export as PNG (lossless). No resizing, no recompression here.
        const annotatedPng = canvas.toBuffer("image/png");

        // Draw a white background box to visualize the fit area
        doc.save();
        doc.rect(x, startY, targetWidth, targetHeight).fill("#ffffff");
        doc.restore();

        // Embed directly and let PDFKit scale with aspect ratio preserved
        doc.image(annotatedPng, x, startY, {
          fit: [targetWidth, targetHeight],
          align: "center",
          valign: "center",
        });

        doc.fillColor("#ff6b6b").fontSize(11).font("Helvetica-Bold");
        const labelY = startY + targetHeight + 5;
        doc.text(imageLabels[i] || `Image ${i + 1}`, x + 30 + 50, labelY);
        
        // Update max bottom Y position (just for label, no diagnosis text here)
        const currentImageBottomY = labelY + 15;
        if (currentImageBottomY > maxImageBottomY) {
          maxImageBottomY = currentImageBottomY;
        }
      } catch (e) {
        doc
          .fillColor("red")
          .fontSize(10)
          .text("Failed to load image.", x, startY);
      }
    }

    // Move down after images - use the maximum bottom position
    doc.y = maxImageBottomY + 20;

    // Color Legend (only those used)
    const labelColorMap = {
      "Inflammed / Red gums": "#A52A2A",
      Malaligned: "#FFFF00",
      "Receded gums": "#808080",
      Stains: "#FF0000",
      Attrition: "#0000FF",
      Crowns: "#FFC0CB",
      "Generalized recession": "#8B4513",
      "Localized recession": "#CD853F",
      "Dental caries": "#4B0082",
      Periodontitis: "#006400",
      Pericoronitis: "#FF8C00",
      Abrasion: "#00CED1",
      Calculus: "#556B2F",
    };

    let lx = 40;
    let ly = doc.y;
    doc.fontSize(8).fillColor("black");
    Array.from(allLabels).forEach((label) => {
      const color = labelColorMap[label] || "#000000";
      doc.rect(lx, ly, 8, 8).fillColor(color).fill();
      doc.fillColor("black").text(label, lx + 12, ly - 2);
      lx += doc.widthOfString(label) + 30;
    });

    doc.y = ly + 20;
    doc.moveDown(1);

    // TREATMENT RECOMMENDATIONS - Left Aligned
    doc.font("Helvetica-Bold").fontSize(13).fillColor("#003366");
    doc.text("TREATMENT RECOMMENDATIONS:", 40);

    const labelRecommendations = {
      "Inflammed / Red gums": "Scaling.",
      Malaligned: "Braces or Clear Aligner",
      "Receded gums": "Gum Surgery.",
      Stains: "Teeth cleaning and polishing.",
      Attrition: "Filling/ Night Guard.",
      Crowns:
        "If the crown is loose or broken, better get it checked. Teeth coloured caps are the best ones.",
      Calculus: "scaling and poslishing",
      Abrasion: "sensitivity tooth paste",
      Pericoronitis: "extraction of 3rd molar",
      Periodontitis:
        "scaling and polishing, periodontal surgery,brushing habbits recommended, use floss",
      "Dental caires": "RCT, Restoration, extraction",
      "Generalized recession": "periodontal surgery",
      "Localized recession": "periodontal surgery",
      Stains:
        "scaling and polishing, teeth whitening, avoid staining foods/drinks, maintain oral hygiene",
    };

    let ry = doc.y + 10;
    doc.font("Helvetica").fontSize(9);
    Array.from(allLabels).forEach((label) => {
      const color = labelColorMap[label] || "#000000";
      const recommendation = labelRecommendations[label] || "-";
      doc.rect(40, ry, 8, 8).fillColor(color).fill();
      doc.fillColor("black").text(`${label} :`, 55, ry);
      doc.text(recommendation, 150, ry);
      ry += 14;
    });

    // Move down after treatment recommendations
    doc.y = ry + 20;
    doc.moveDown(1);

    // DIAGNOSIS SECTION - Left Aligned
    doc.font("Helvetica-Bold").fontSize(13).fillColor("#003366");
    doc.text("DIAGNOSIS:", 40);
    doc.moveDown(0.5);

    // Display diagnosis for each image in pointwise format
    const diagnosisLabels = ["Front Teeth", "Upper Teeth", "Lower Teeth"];
    let diagnosisY = (doc.y || 0) + 5;
    doc.font("Helvetica").fontSize(9).fillColor("black");
    
    results.forEach((result, idx) => {
      if (result.diagnosis && result.diagnosis.trim()) {
        const label = diagnosisLabels[idx] || `Image ${idx + 1}`;
        
        // Ensure diagnosisY is a valid number
        if (isNaN(diagnosisY)) {
          diagnosisY = doc.y + 5;
        }
        
        // Draw label in bold
        doc.font("Helvetica-Bold").text(`${label}:`, 40, diagnosisY);
        
        // Add 15px spacing between label and diagnosis text
        const diagnosisTextY = diagnosisY + 15;
        
        // Draw diagnosis text with wrapping using explicit coordinates
        // Save current doc.y
        const savedY = doc.y || 0;
        // Draw text with explicit x, y coordinates and options
        doc.font("Helvetica").text(result.diagnosis, 55, diagnosisTextY, {
          width: 500,
          align: "left",
        });
        // Estimate text height for wrapped text (approximate calculation)
        const lineHeight = 12; // Approximate line height for font size 9
        const charsPerLine = Math.floor(500 / 5.5); // Approximate characters per line
        const numLines = Math.max(1, Math.ceil(result.diagnosis.length / charsPerLine));
        const textHeight = numLines * lineHeight;
        
        // Update diagnosisY to position after text, plus spacing
        diagnosisY = diagnosisTextY + textHeight + 8;
        // Restore doc.y
        doc.y = savedY;
      }
    });
    
    // Update doc.y to final diagnosis position (ensure it's a valid number)
    if (!isNaN(diagnosisY)) {
      doc.y = diagnosisY;
    }

    // Add extra spacing after diagnosis section
    doc.y = doc.y + 10;
    doc.moveDown(1);

    const signaturePath = path.join(__dirname, "../assets/sign.png");

    // Define positions near bottom left
    const signatureWidth = 120; // Adjust size as needed
    const signatureHeight = 50; // Adjust size as needed
    const marginBottom = 60; // Space from bottom edge
    const marginLeft = 40; // Same left margin as content

    // Calculate Y position near bottom, considering page height and margins
    const pageHeight = doc.page.height;
    const yPosition = pageHeight - marginBottom - signatureHeight;

    doc.font("Helvetica-Oblique").fontSize(11).fillColor("black");
    doc.text("Verified by:", marginLeft, yPosition - 15);

    try {
      doc.image(signaturePath, marginLeft, yPosition, {
        width: signatureWidth,
        height: signatureHeight,
      });
    } catch (e) {
      console.error("Failed to load signature image:", e);
    }
    doc.moveDown(4);

    const signX = marginLeft;
    const signY = yPosition + signatureHeight + 5;
    doc
      .font("Helvetica-Bold")
      .fontSize(10)
      .fillColor("black")

      doc.end();
    // fs.writeFileSync("output7_report.pdf", Buffer.concat(buffers));
  });
};


module.exports = generateReportPDF;
