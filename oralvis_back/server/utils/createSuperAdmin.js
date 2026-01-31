const bcrypt = require('bcryptjs');
const User = require('../models/user');

const createSuperAdmin = async () => {
  try {
    console.log('🔍 Checking for existing super admin...');

  
    // Check if super admin already exists
    const existingSuperAdmin = await User.findOne({ role: 'super_admin' });
    if (existingSuperAdmin) {
      console.log('✅ Super admin already exists:', existingSuperAdmin.email);
      return existingSuperAdmin;
    }

    console.log('🔨 Creating new super admin...');
    
    // Create super admin
    const hashedPassword = await bcrypt.hash('superadmin123', 10);
    
    const superAdmin = new User({
      name: 'Super Admin',
      email: 'superadmin@oralvis.com',
      phoneNo: '9876543210',
      password: hashedPassword,
      role: 'super_admin',
      adminType: 'super_admin'
    });

    await superAdmin.save();
    console.log('✅ Super admin created successfully:', superAdmin.email);
    console.log('📋 Super admin details:', {
      id: superAdmin._id,
      name: superAdmin.name,
      email: superAdmin.email,
      phoneNo: superAdmin.phoneNo,
      role: superAdmin.role,
      adminType: superAdmin.adminType
    });
    return superAdmin;
  } catch (error) {
    console.error('❌ Error creating super admin:', error);
    throw error;
  }
};

module.exports = createSuperAdmin;
