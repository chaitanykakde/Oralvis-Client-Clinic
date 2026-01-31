const express = require("express");
const {
  createBlog,
  getAllBlogs,
  getBlogById,
} = require("../controllers/blogController.js");

const router = express.Router();

router.post("/create", createBlog);
router.get("/", getAllBlogs);
router.get("/:id", getBlogById);

module.exports = router;
