const Blog = require('../models/blog');

// Add a new blog
const createBlog = async (req, res) => {
  try {
    const blog = new Blog(req.body);
    const saved = await blog.save();
    res.status(201).json(saved);
  } catch (err) {
    console.error('Create blog error:', err);
    res.status(400).json({ error: 'Failed to create blog' });
  }
};

// Get all blogs (only summaries)

const getAllBlogs = async (req, res) => {
  try {
    const blogs = await Blog.find({}, 'title image intro');
    res.json(blogs);
  } catch (err) {
    console.error('Fetch blogs error:', err);
    res.status(500).json({ error: 'Failed to fetch blogs' });
  }
};

// Get individual blog by ID
const getBlogById = async (req, res) => {
  try {
    const blog = await Blog.findById(req.params.id);
    if (!blog) return res.status(404).json({ error: 'Blog not found' });
    res.json(blog);
  } catch (err) {
    console.log(err) ;
    console.error('Fetch blog error:', err);
    res.status(500).json({ error: 'Failed to fetch blog' });
  }
};

// Export functions using CommonJS
module.exports = {
  createBlog,
  getAllBlogs,
  getBlogById,
};
