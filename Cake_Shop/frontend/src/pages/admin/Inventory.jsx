import React, { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  Container,
  Row,
  Col,
  Card,
  CardHeader,
  CardBody,
  Table,
  Button,
  Modal,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Form,
  FormGroup,
  Label,
  Input,
  InputGroup,
  Badge,
  Alert,
  Spinner,
  InputGroupText,
  Nav,
  NavItem,
  NavLink,
  TabContent,
  TabPane,
  UncontrolledTooltip
} from 'reactstrap';
import { 
  FaPlus, 
  FaEdit, 
  FaTrash, 
  FaSearch, 
  FaFilePdf, 
  FaSave, 
  FaTimes, 
  FaExclamationTriangle,
  FaBoxOpen,
  FaBox
} from 'react-icons/fa';
import axios from 'axios';
import jsPDF from 'jspdf';
import 'jspdf-autotable';

const categoryOptions = [
  'Cake Ingredients',
  'Decorations',
  'Packaging',
  'Utensils',
  'Dairy Products',
  'Dry Goods',
  'Other'
];

const Inventory = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('1');
  const [inventoryItems, setInventoryItems] = useState([]);
  const [lowStockItems, setLowStockItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filteredItems, setFilteredItems] = useState([]);
  
  // Modal states
  const [modal, setModal] = useState(false);
  const [modalType, setModalType] = useState('add'); // 'add' or 'edit'
  const [currentItem, setCurrentItem] = useState(null);
  const [deleteModal, setDeleteModal] = useState(false);
  
  // Form states
  const [formData, setFormData] = useState({
    name: '',
    quantity: '',
    unit: '',
    price: '',
    category: '',
    description: '',
    minStockLevel: '5'
  });
  
  // Reference for table to highlight specific rows
  const tableRef = useRef(null);
  const highlightedItemId = location.state?.highlightItem;

  useEffect(() => {
    // Check if activeTab was passed in location state
    if (location.state?.activeTab) {
      setActiveTab(location.state.activeTab);
    }
    
    fetchInventoryItems();
  }, [location]);
  
  useEffect(() => {
    // Apply filter whenever search term changes
    if (searchTerm.trim() === '') {
      setFilteredItems(inventoryItems);
    } else {
      const filtered = inventoryItems.filter(item => 
        item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.category.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.description.toLowerCase().includes(searchTerm.toLowerCase())
      );
      setFilteredItems(filtered);
    }
  }, [searchTerm, inventoryItems]);
  
  useEffect(() => {
    // Scroll to and highlight the specified item if provided
    if (highlightedItemId && tableRef.current) {
      const row = document.getElementById(`inventory-item-${highlightedItemId}`);
      if (row) {
        row.scrollIntoView({ behavior: 'smooth', block: 'center' });
        row.classList.add('highlight-row');
        
        // Remove highlight after 3 seconds
        setTimeout(() => {
          row.classList.remove('highlight-row');
        }, 3000);
      }
    }
  }, [highlightedItemId, filteredItems]);

  const fetchInventoryItems = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      
      const response = await axios.get('http://localhost:4000/api/v1/inventory', {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      if (response.data.success) {
        setInventoryItems(response.data.data);
        setFilteredItems(response.data.data);
        
        // Filter out low stock items
        const lowStock = response.data.data.filter(item => 
          item.quantity <= item.minStockLevel
        );
        setLowStockItems(lowStock);
      }
    } catch (error) {
      console.error('Error fetching inventory items:', error);
      setError('Failed to fetch inventory items. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const toggleModal = () => {
    setModal(!modal);
    if (!modal) {
      // Reset form when opening
      setFormData({
        name: '',
        quantity: '',
        unit: '',
        price: '',
        category: '',
        description: '',
        minStockLevel: '5'
      });
    }
  };
  
  const toggleDeleteModal = () => {
    setDeleteModal(!deleteModal);
  };

  const handleAddItem = () => {
    setModalType('add');
    setCurrentItem(null);
    toggleModal();
  };

  const handleEditItem = (item) => {
    setModalType('edit');
    setCurrentItem(item);
    // Make sure to convert all number values to strings for input fields
    setFormData({
      name: item.name || '',
      quantity: item.quantity?.toString() || '',
      unit: item.unit || '',
      price: item.price?.toString() || '',
      category: item.category || '',
      description: item.description || '',
      minStockLevel: item.minStockLevel?.toString() || '5'
    });
    // Open the modal after setting the form data
    setModal(true);
  };
  

  const handleDeleteItem = (item) => {
    setCurrentItem(item);
    toggleDeleteModal();
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const token = localStorage.getItem('token');
      const headers = { Authorization: `Bearer ${token}` };
      
      const payload = {
        name: formData.name,
        quantity: parseFloat(formData.quantity),
        unit: formData.unit,
        price: parseFloat(formData.price),
        category: formData.category,
        description: formData.description,
        minStockLevel: parseInt(formData.minStockLevel)
      };
      
      let response;
      
      if (modalType === 'add') {
        response = await axios.post('http://localhost:4000/api/v1/inventory', payload, { headers });
      } else {
        response = await axios.put(`http://localhost:4000/api/v1/inventory/${currentItem._id}`, payload, { headers });
      }
      
      if (response.data.success) {
        toggleModal();
        fetchInventoryItems();
      }
    } catch (error) {
      console.error('Error saving inventory item:', error);
      setError('Failed to save inventory item. Please try again.');
    }
  };

  const confirmDelete = async () => {
    try {
      const token = localStorage.getItem('token');
      
      await axios.delete(`http://localhost:4000/api/v1/inventory/${currentItem._id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      toggleDeleteModal();
      fetchInventoryItems();
    } catch (error) {
      console.error('Error deleting inventory item:', error);
      setError('Error deleting inventory item. Please try again.');
    }
  };

  const generatePDF = () => {
    const doc = new jsPDF();
    
    // Add title
    doc.setFontSize(20);
    doc.text('Inventory Report', 14, 22);
    
    // Add date
    doc.setFontSize(10);
    doc.text(`Generated on: ${new Date().toLocaleDateString()}`, 14, 30);
    
    // Define the columns for the table
    const columns = [
      { header: 'Name', dataKey: 'name' },
      { header: 'Category', dataKey: 'category' },
      { header: 'Quantity', dataKey: 'quantity' },
      { header: 'Unit', dataKey: 'unit' },
      { header: 'Price(LKR: )', dataKey: 'price' }
    ];
    
    // Prepare the data
    const data = inventoryItems.map(item => ({
      name: item.name,
      category: item.category,
      quantity: item.quantity,
      unit: item.unit,
      price: item.price.toFixed(2)
    }));
    
    // Generate the table
    import('jspdf-autotable').then(module => {
      const autoTable = module.default;
      autoTable(doc, {
        columns,
        body: data,
        startY: 40,
        margin: { top: 40 },
        styles: { overflow: 'linebreak' },
        bodyStyles: { valign: 'top' },
        theme: 'striped',
        headStyles: { fillColor: [99, 0, 126] }
      });
      
      // Add summary section
      const totalItems = inventoryItems.length;
      const lowStockCount = lowStockItems.length;
      
      const pageHeight = doc.internal.pageSize.height;
      let finalY = doc.lastAutoTable.finalY + 10;
      
      if (finalY > pageHeight - 40) {
        doc.addPage();
        finalY = 20;
      }
      
      doc.setFontSize(12);
      doc.text('Inventory Summary', 14, finalY);
      doc.setFontSize(10);
      doc.text(`Total Items: ${totalItems}`, 14, finalY + 8);
      doc.text(`Low Stock Items: ${lowStockCount}`, 14, finalY + 16);
      
      // Save the PDF
      doc.save('inventory-report.pdf');
    });
  };  
  const toggleTab = (tab) => {
    if (activeTab !== tab) setActiveTab(tab);
  };

  if (loading && inventoryItems.length === 0) {
    return (
      <>
        <Container className="py-4">
          <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '60vh' }}>
            <Spinner color="primary" />
          </div>
        </Container>
      </>
    );
  }

  return (
    <>
      <Container fluid className="py-4">
        <Card className="shadow-sm">
          <CardHeader className="bg-white">
            <Row className="align-items-center">
              <Col>
                <h5 className="mb-0">Inventory Management</h5>
              </Col>
              <Col xs="auto">
                <div className="d-flex gap-2">
                  <Button color="success" size="sm" onClick={handleAddItem}>
                    <FaPlus className="me-1" /> Add Item
                  </Button>
                  <Button color="info" size="sm" onClick={generatePDF}>
                    <FaFilePdf className="me-1" /> Export PDF
                  </Button>
                </div>
              </Col>
            </Row>
          </CardHeader>
          <CardBody>
            {error && (
              <Alert color="danger" className="mb-4">
                {error}
              </Alert>
            )}
            
            <Row className="mb-4">
              <Col md="6">
                <InputGroup>
                  <InputGroupText>
                    <FaSearch />
                  </InputGroupText>
                  <Input 
                    type="text" 
                    placeholder="Search inventory items..." 
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                  />
                </InputGroup>
              </Col>
              <Col md="6">
                <Nav tabs className="card-header-tabs float-end">
                  <NavItem>
                    <NavLink
                      className={activeTab === '1' ? 'active' : ''}
                      onClick={() => toggleTab('1')}
                      href="#"
                    >
                      <FaBoxOpen className="me-1" /> All Items
                    </NavLink>
                  </NavItem>
                  <NavItem>
                    <NavLink
                      className={activeTab === '2' ? 'active' : ''}
                      onClick={() => toggleTab('2')}
                      href="#"
                    >
                      <div className="d-flex align-items-center">
                        <FaExclamationTriangle className="me-1" /> Low Stock 
                        {lowStockItems.length > 0 && (
                          <Badge color="danger" pill className="ms-2">
                            {lowStockItems.length}
                          </Badge>
                        )}
                      </div>
                    </NavLink>
                  </NavItem>
                </Nav>
              </Col>
            </Row>
            
            <TabContent activeTab={activeTab}>
              <TabPane tabId="1">
                <div className="table-responsive" ref={tableRef}>
                  {filteredItems.length === 0 ? (
                    <div className="text-center py-5">
                      <FaBox size={40} className="text-muted mb-3" />
                      <h5>No inventory items found</h5>
                      <p className="text-muted">
                        {searchTerm ? 'Try a different search term' : 'Add inventory items to get started'}
                      </p>
                    </div>
                  ) : (
                    <Table hover bordered responsive>
                      <thead>
                        <tr>
                          <th>Name</th>
                          <th>Category</th>
                          <th>Quantity</th>
                          <th>Unit</th>
                          <th>Price (LKR)</th>
                          <th>Min. Stock</th>
                          <th>Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {filteredItems.map(item => (
                          <tr 
                            key={item._id} 
                            id={`inventory-item-${item._id}`}
                            className={item.quantity <= item.minStockLevel ? 'table-warning' : ''}
                          >
                            <td>{item.name}</td>
                            <td>{item.category}</td>
                            <td>
                              {item.quantity <= item.minStockLevel ? (
                                <Badge color="warning" pill>
                                  {item.quantity}
                                </Badge>
                              ) : (
                                item.quantity
                              )}
                            </td>
                            <td>{item.unit}</td>
                            <td>LKR: {item.price} /=</td>
                            <td>{item.minStockLevel}</td>
                            <td>
                              <div className="d-flex gap-2">
                                <Button 
                                  color="primary" 
                                  size="sm" 
                                  onClick={() => handleEditItem(item)}
                                  id={`edit-${item._id}`}
                                >
                                  <FaEdit />
                                </Button>
                                <UncontrolledTooltip target={`edit-${item._id}`}>
                                  Edit Item
                                </UncontrolledTooltip>
                                
                                <Button 
                                  color="danger" 
                                  size="sm" 
                                  onClick={() => handleDeleteItem(item)}
                                  id={`delete-${item._id}`}
                                >
                                  <FaTrash />
                                </Button>
                                <UncontrolledTooltip target={`delete-${item._id}`}>
                                  Delete Item
                                </UncontrolledTooltip>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </Table>
                  )}
                </div>
              </TabPane>
              
              <TabPane tabId="2">
                <div className="table-responsive">
                  {lowStockItems.length === 0 ? (
                    <div className="text-center py-5">
                      <FaBoxOpen size={40} className="text-success mb-3" />
                      <h5>No low stock items</h5>
                      <p className="text-muted">All inventory items have sufficient stock</p>
                    </div>
                  ) : (
                    <Table hover bordered responsive>
                      <thead>
                        <tr>
                          <th>Name</th>
                          <th>Category</th>
                          <th>Quantity</th>
                          <th>Unit</th>
                          <th>Min. Stock</th>
                          <th>Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {lowStockItems.map(item => (
                          <tr key={item._id} id={`inventory-item-${item._id}`} className="table-warning">
                            <td>{item.name}</td>
                            <td>{item.category}</td>
                            <td>
                              <Badge color="warning" pill>
                                {item.quantity}
                              </Badge>
                            </td>
                            <td>{item.unit}</td>
                            <td>{item.minStockLevel}</td>
                            <td>
                              <Button 
                                color="primary" 
                                size="sm" 
                                onClick={() => handleEditItem(item)}
                              >
                                <FaEdit className="me-1" /> Update Stock
                              </Button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </Table>
                  )}
                </div>
              </TabPane>
            </TabContent>
          </CardBody>
        </Card>
      </Container>
      
      {/* Add/Edit Modal */}
      <Modal isOpen={modal} toggle={toggleModal} size="lg">
        <ModalHeader toggle={toggleModal}>
          {modalType === 'add' ? 'Add New Inventory Item' : 'Edit Inventory Item'}
        </ModalHeader>
        <Form onSubmit={handleSubmit}>
          <ModalBody>
            <Row>
              <Col md="6">
                <FormGroup>
                  <Label for="name">Item Name*</Label>
                  <Input
                    id="name"
                    name="name"
                    value={formData.name}
                    onChange={handleInputChange}
                    required
                  />
                </FormGroup>
              </Col>
              <Col md="6">
                <FormGroup>
                  <Label for="category">Category*</Label>
                  <Input
                    id="category"
                    name="category"
                    type="select"
                    value={formData.category}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">Select a category</option>
                    {categoryOptions.map((category, index) => (
                      <option key={index} value={category}>
                        {category}
                      </option>
                    ))}
                  </Input>
                </FormGroup>
              </Col>
            </Row>
            
            <Row>
              <Col md="4">
                <FormGroup>
                  <Label for="quantity">Quantity*</Label>
                  <Input
                    id="quantity"
                    name="quantity"
                    type="number"
                    min="0"
                    step="0.01"
                    value={formData.quantity}
                    onChange={handleInputChange}
                    required
                  />
                </FormGroup>
              </Col>
              <Col md="4">
                <FormGroup>
                  <Label for="unit">Unit*</Label>
                  <Input
                    id="unit"
                    name="unit"
                    placeholder="kg, pcs, liters, etc."
                    value={formData.unit}
                    onChange={handleInputChange}
                    required
                  />
                </FormGroup>
              </Col>
              <Col md="4">
                <FormGroup>
                  <Label for="price">Price (LKR)*</Label>
                  <Input
                    id="price"
                    name="price"
                    type="number"
                    min="0"
                    step="0.01"
                    value={formData.price}
                    onChange={handleInputChange}
                    required
                  />
                </FormGroup>
              </Col>
            </Row>
            
            <Row>
              <Col md="6">
                <FormGroup>
                  <Label for="minStockLevel">Minimum Stock Level*</Label>
                  <Input
                    id="minStockLevel"
                    name="minStockLevel"
                    type="number"
                    min="1"
                    value={formData.minStockLevel}
                    onChange={handleInputChange}
                    required
                  />
                  <small className="form-text text-muted">
                    Notification will be triggered when quantity falls below this level
                  </small>
                </FormGroup>
              </Col>
            </Row>
            
            <FormGroup>
              <Label for="description">Description</Label>
              <Input
                id="description"
                name="description"
                type="textarea"
                rows="3"
                value={formData.description}
                onChange={handleInputChange}
              />
            </FormGroup>
          </ModalBody>
          <ModalFooter>
            <Button color="secondary" onClick={toggleModal}>
              <FaTimes className="me-1" /> Cancel
            </Button>
            <Button color="primary" type="submit">
              <FaSave className="me-1" /> Save
            </Button>
          </ModalFooter>
        </Form>
      </Modal>
      
      {/* Delete Confirmation Modal */}
      <Modal isOpen={deleteModal} toggle={toggleDeleteModal}>
        <ModalHeader toggle={toggleDeleteModal}>Confirm Delete</ModalHeader>
        <ModalBody>
          Are you sure you want to delete <strong>{currentItem?.name}</strong>?
          This action cannot be undone.
        </ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={toggleDeleteModal}>
            Cancel
          </Button>
          <Button color="danger" onClick={confirmDelete}>
            Delete
          </Button>
        </ModalFooter>
      </Modal>
    </>
  );
};

export default Inventory;

