import User from "../models/User.js";
import response from "../Utils/ResponseHandler/ResponseHandler.js";
import HttpType from "../Utils/ResponseHandler/HttpType.js";
import ResTypes from "../Utils/ResponseHandler/ResTypes.js";

// Register a new user
export const register = async (req, res) => {
  try {
    const { name, email, password, phone } = req.body;
    
    // Check if user already exists
    const existingUser = await User.findOne({ email });
    if (existingUser) {
      return response(res, HttpType.BAD_REQUEST.code, ResTypes.errors.user_exists);
    }
    
    // Create new user
    const newUser = new User({
      name,
      email,
      password, // In a real app, you should hash this password
      phone
    });
    
    await newUser.save();
    
    // Remove password from response
    const userWithoutPassword = { ...newUser._doc };
    delete userWithoutPassword.password;
    
    return response(res, HttpType.CREATED.code, {
      message: ResTypes.successMessages.user_created.message,
      user: userWithoutPassword
    });
  } catch (error) {
    console.error("Registration error:", error);
    return response(res, HttpType.INTERNAL_SERVER_ERROR.code, ResTypes.errors.server_error);
  }
};

// Login user
export const login = async (req, res) => {
  try {
    const { email, password } = req.body;
    
    // Find user by email
    const user = await User.findOne({ email });
    if (!user) {
      return response(res, HttpType.NOT_FOUND.code, ResTypes.errors.no_user);
    }
    
    // Check password
    if (user.password !== password) { // In a real app, you should compare hashed passwords
      return response(res, HttpType.UNAUTHORIZED.code, ResTypes.errors.invalid_password);
    }
    
    // Remove password from response
    const userWithoutPassword = { ...user._doc };
    delete userWithoutPassword.password;
    
    return response(res, HttpType.OK.code, {
      message: ResTypes.successMessages.login_successful.message,
      user: userWithoutPassword
    });
  } catch (error) {
    console.error("Login error:", error);
    return response(res, HttpType.INTERNAL_SERVER_ERROR.code, ResTypes.errors.server_error);
  }
};

// Get user profile
export const getUserProfile = async (req, res) => {
  try {
    const { userId } = req.params;
    
    const user = await User.findById(userId);
    if (!user) {
      return response(res, HttpType.NOT_FOUND.code, ResTypes.errors.no_user);
    }
    
    // Remove password from response
    const userWithoutPassword = { ...user._doc };
    delete userWithoutPassword.password;
    
    return response(res, HttpType.OK.code, {
      message: ResTypes.successMessages.data_retrieved.message,
      user: userWithoutPassword
    });
  } catch (error) {
    console.error("Get profile error:", error);
    return response(res, HttpType.INTERNAL_SERVER_ERROR.code, ResTypes.errors.server_error);
  }
};

// Get all users
export const getAllUsers = async (req, res) => {
  try {
    const users = await User.find({});
    
    // Remove passwords from response
    const usersWithoutPasswords = users.map(user => {
      const userObj = { ...user._doc };
      delete userObj.password;
      return userObj;
    });
    
    return response(res, HttpType.OK.code, {
      message: ResTypes.successMessages.data_retrieved.message,
      users: usersWithoutPasswords
    });
  } catch (error) {
    console.error("Get all users error:", error);
    return response(res, HttpType.INTERNAL_SERVER_ERROR.code, ResTypes.errors.server_error);
  }
};

// Update user profile
export const updateUserProfile = async (req, res) => {
  try {
    const { userId } = req.params;
    const updateData = req.body;
    
    // Don't allow role updates through this endpoint
    delete updateData.role;
    
    const updatedUser = await User.findByIdAndUpdate(
      userId,
      { $set: updateData },
      { new: true }
    );
    
    if (!updatedUser) {
      return response(res, HttpType.NOT_FOUND.code, ResTypes.errors.no_user);
    }
    
    // Remove password from response
    const userWithoutPassword = { ...updatedUser._doc };
    delete userWithoutPassword.password;
    
    return response(res, HttpType.OK.code, {
      message: ResTypes.successMessages.user_edited.message,
      user: userWithoutPassword
    });
  } catch (error) {
    console.error("Update profile error:", error);
    return response(res, HttpType.INTERNAL_SERVER_ERROR.code, ResTypes.errors.upadate_error);
  }
};

// Delete user account
export const deleteUserAccount = async (req, res) => {
  try {
    const { userId } = req.params;
    
    const deletedUser = await User.findByIdAndDelete(userId);
    
    if (!deletedUser) {
      return response(res, HttpType.NOT_FOUND.code, ResTypes.errors.no_user);
    }
    
    return response(res, HttpType.OK.code, {
      message: "User account deleted successfully"
    });
  } catch (error) {
    console.error("Delete account error:", error);
    return response(res, HttpType.INTERNAL_SERVER_ERROR.code, ResTypes.errors.delete_error);
  }
};