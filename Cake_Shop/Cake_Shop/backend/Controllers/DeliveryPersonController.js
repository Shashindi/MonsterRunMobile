import DeliveryPerson from '../models/DeliveryPerson.js';
import response from '../Utils/ResponseHandler/ResponseHandler.js';
import ResTypes from '../Utils/ResponseHandler/ResTypes.js';

// Create a new delivery person (admin only)
export const createDeliveryPerson = async (req, res) => {
  try {
    const { name, email, password, contact, nic, city } = req.body;
    
    // Check if delivery person with email already exists
    const existingPerson = await DeliveryPerson.findOne({ email });
    if (existingPerson) {
      return response(res, 400, { message: 'Delivery person with this email already exists' });
    }
    
    // Check if delivery person with NIC already exists
    const existingNIC = await DeliveryPerson.findOne({ nic });
    if (existingNIC) {
      return response(res, 400, { message: 'Delivery person with this NIC already exists' });
    }
    
    const newDeliveryPerson = new DeliveryPerson({
      name,
      email,
      password,
      contact,
      nic,
      city
    });
    
    await newDeliveryPerson.save();
    
    // Remove password from response
    const deliveryPersonResponse = newDeliveryPerson.toObject();
    delete deliveryPersonResponse.password;
    
    return response(res, 201, { 
      message: 'Delivery person created successfully',
      deliveryPerson: deliveryPersonResponse 
    });
  } catch (error) {
    console.error('Error creating delivery person:', error);
    return response(res, 500, ResTypes.errors.create_error);
  }
};

// Get all delivery persons (admin only)
export const getAllDeliveryPersons = async (req, res) => {
  try {
    const deliveryPersons = await DeliveryPerson.find()
      .select('-password')
      .populate('city');
    
    return response(res, 200, { deliveryPersons });
  } catch (error) {
    console.error('Error fetching delivery persons:', error);
    return response(res, 500, ResTypes.errors.server_error);
  }
};

// Get delivery person by ID
export const getDeliveryPersonById = async (req, res) => {
  try {
    const { id } = req.params;
    
    const deliveryPerson = await DeliveryPerson.findById(id)
      .select('-password')
      .populate('city');
    
    if (!deliveryPerson) {
      return response(res, 404, ResTypes.errors.not_found);
    }
    
    return response(res, 200, { deliveryPerson });
  } catch (error) {
    console.error('Error fetching delivery person:', error);
    return response(res, 500, ResTypes.errors.server_error);
  }
};

// Update delivery person (admin only)
export const updateDeliveryPerson = async (req, res) => {
  try {
    const { id } = req.params;
    const { name, email, contact, city, isActive } = req.body;
    
    // Check if email is being changed and already exists
    if (email) {
      const existingPerson = await DeliveryPerson.findOne({ email, _id: { $ne: id } });
      if (existingPerson) {
        return response(res, 400, { message: 'Email already in use by another delivery person' });
      }
    }
    
    const updatedPerson = await DeliveryPerson.findByIdAndUpdate(
      id,
      { name, email, contact, city, isActive },
      { new: true, runValidators: true }
    ).select('-password');
    
    if (!updatedPerson) {
      return response(res, 404, ResTypes.errors.not_found);
    }
    
    return response(res, 200, { 
      message: 'Delivery person updated successfully',
      deliveryPerson: updatedPerson 
    });
  } catch (error) {
    console.error('Error updating delivery person:', error);
    return response(res, 500, ResTypes.errors.upadate_error);
  }
};

// Delete delivery person (admin only)
export const deleteDeliveryPerson = async (req, res) => {
  try {
    const { id } = req.params;
    
    const deletedPerson = await DeliveryPerson.findByIdAndDelete(id);
    
    if (!deletedPerson) {
      return response(res, 404, ResTypes.errors.not_found);
    }
    
    return response(res, 200, { 
      message: 'Delivery person deleted successfully' 
    });
  } catch (error) {
    console.error('Error deleting delivery person:', error);
    return response(res, 500, ResTypes.errors.delete_error);
  }
};

// Delivery person login
export const loginDeliveryPerson = async (req, res) => {
  try {
    const { email, nic } = req.body;
    
    // Find delivery person by email
    const deliveryPerson = await DeliveryPerson.findOne({ email });
    
    if (!deliveryPerson) {
      return response(res, 401, { message: 'Invalid email or NIC' });
    }
    
    // Check if delivery person is active
    if (!deliveryPerson.isActive) {
      return response(res, 403, { message: 'Your account has been deactivated. Please contact admin.' });
    }
    
    // Verify NIC
    if (deliveryPerson.nic !== nic) {
      return response(res, 401, { message: 'Invalid email or NIC' });
    }
    
    // Create delivery person object without password
    const deliveryPersonData = deliveryPerson.toObject();
    delete deliveryPersonData.password;
    
    return response(res, 200, { 
      message: 'Login successful',
      deliveryPerson: deliveryPersonData
    });
  } catch (error) {
    console.error('Error during login:', error);
    return response(res, 500, ResTypes.errors.server_error);
  }
};
// Change delivery person password
export const changePassword = async (req, res) => {
  try {
    const { id } = req.params;
    const { currentPassword, newPassword } = req.body;
    
    // Find delivery person
    const deliveryPerson = await DeliveryPerson.findById(id);
    
    if (!deliveryPerson) {
      return response(res, 404, ResTypes.errors.not_found);
    }
    
    // Verify current password
    const isPasswordValid = await deliveryPerson.comparePassword(currentPassword);
    
    if (!isPasswordValid) {
      return response(res, 401, { message: 'Current password is incorrect' });
    }
    
    // Update password
    deliveryPerson.password = newPassword;
    await deliveryPerson.save();
    
    return response(res, 200, { 
      message: 'Password changed successfully' 
    });
  } catch (error) {
    console.error('Error changing password:', error);
    return response(res, 500, ResTypes.errors.upadate_error);
  }
};
