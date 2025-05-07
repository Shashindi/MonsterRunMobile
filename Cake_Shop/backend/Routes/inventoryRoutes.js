import express from 'express'
const router = express.Router();
import { 
    getAllItems, 
    getLowStockItems, 
    createItem, 
    getItemById, 
    updateItem, 
    deleteItem
  } from '../Controllers/InventoryController.js';

// Get all inventory items
router.get('/', getAllItems);

// Get low stock items
router.get('/low-stock', getLowStockItems);

// Create new inventory item
router.post('/', createItem);

// Get single inventory item
router.get('/:id', getItemById);

// Update inventory item
router.put('/:id', updateItem);

// Delete inventory item
router.delete('/:id', deleteItem);

export default router;
