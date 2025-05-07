import React, { useState, useEffect } from 'react';
import {
  Navbar,
  NavbarBrand,
  Nav,
  NavItem,
  NavLink,
  UncontrolledDropdown,
  DropdownToggle,
  DropdownMenu,
  DropdownItem,
  Button,
  Badge,
  Collapse,
  NavbarToggler,
  Container
} from 'reactstrap';
import { useNavigate, Link } from 'react-router-dom';
import { FaUserCircle, FaBell, FaSignOutAlt, FaCog, FaUser, FaTachometerAlt, FaExclamationTriangle, FaBoxes } from 'react-icons/fa';
import axios from 'axios';

const AdminHeader = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [lowStockItems, setLowStockItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const navigate = useNavigate();

  // Fetch low stock items on component mount
  useEffect(() => {
    fetchLowStockItems();
    
    // Set up interval to check for low stock items every 5 minutes
    const interval = setInterval(fetchLowStockItems, 5 * 60 * 1000);
    
    // Clean up interval on component unmount
    return () => clearInterval(interval);
  }, []);

  const fetchLowStockItems = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      
      const response = await axios.get('http://localhost:4000/api/v1/inventory/low-stock', {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      if (response.data.success) {
        setLowStockItems(response.data.data);
      }
    } catch (error) {
      console.error('Error fetching low stock items:', error);
      setError('Failed to fetch low stock alerts');
    } finally {
      setLoading(false);
    }
  };

  const toggle = () => setIsOpen(!isOpen);
  const toggleNotifications = () => setNotificationsOpen(!notificationsOpen);

  const handleLogout = () => {
    // Remove user data and token from localStorage
    localStorage.removeItem('admin');
    
    // Navigate to login page
    navigate('/login');
  };

  const handleAlertClick = (itemId) => {
    // Navigate to inventory page with filter for low stock
    navigate('/admin/inventory', { 
      state: { 
        activeTab: '2', // Set to low stock tab
        highlightItem: itemId // Optional: to highlight the specific item
      } 
    });
    
    // Close the notifications dropdown
    setNotificationsOpen(false);
  };

  return (
    <Navbar color="dark" dark expand="md" className="shadow-sm py-3" container={false}>
      <Container fluid>
        <NavbarBrand tag={Link} to="/admin/dashboard" className="d-flex align-items-center">
          <span className="h4 mb-0 me-2">🍰</span> 
          <div>
            <div className="fw-bold">HOLY SPATULA</div>
            <div className="small text-primary">Admin Portal</div>
          </div>
        </NavbarBrand>
        
        <NavbarToggler onClick={toggle} />
        
        <Collapse isOpen={isOpen} navbar>
          <Nav className="me-auto" navbar>
            <NavItem>
              <NavLink tag={Link} to="/admin/dashboard">
                <FaTachometerAlt className="me-1" /> Dashboard
              </NavLink>
            </NavItem>
            <NavItem>
              <NavLink tag={Link} to="/admin/orders">
                Orders
              </NavLink>
            </NavItem>
            <NavItem>
              <NavLink tag={Link} to="/admin/inventory">
                Inventory
              </NavLink>
            </NavItem>
            <NavItem>
              <NavLink tag={Link} to="/admin/users">
                Users
              </NavLink>
            </NavItem>
            <NavItem>
              <NavLink tag={Link} to="/admin/inquiries">
                Inquiries
              </NavLink>
            </NavItem>
          </Nav>
          
          <Nav className="ms-auto" navbar>
            <UncontrolledDropdown nav inNavbar className="me-3 my-1 my-md-0">
              <DropdownToggle nav className="text-light p-0 position-relative">
                <FaBell size={20} />
                {lowStockItems.length > 0 && (
                  <Badge color="danger" pill className="position-absolute top-0 start-100 translate-middle">
                    {lowStockItems.length}
                  </Badge>
                )}
              </DropdownToggle>
              <DropdownMenu end className="dropdown-menu-lg shadow-lg py-0">
                <div className="p-3 border-bottom">
                  <h6 className="mb-0">Inventory Alerts</h6>
                </div>
                
                <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                  {lowStockItems.length === 0 ? (
                    <div className="p-3 text-center text-muted">
                      <FaBoxes className="mb-2" size={20} />
                      <p className="mb-0">No inventory alerts</p>
                    </div>
                  ) : (
                    lowStockItems.map(item => (
                      <DropdownItem 
                        key={item._id}
                        onClick={() => handleAlertClick(item._id)}
                        className="p-3 border-bottom"
                      >
                        <div className="d-flex align-items-center">
                          <div className="me-3">
                            <div className="bg-warning text-white rounded-circle p-2">
                              <FaExclamationTriangle />
                            </div>
                          </div>
                          <div>
                            <h6 className="mb-0">{item.name}</h6>
                            <div className="small text-muted">
                              {item.quantity} {item.unit} remaining (Min: {item.minStockLevel})
                            </div>
                          </div>
                        </div>
                      </DropdownItem>
                    ))
                  )}
                </div>
                
                {lowStockItems.length > 0 && (
                  <div className="p-2 border-top text-center">
                    <Button 
                      color="link" 
                      size="sm" 
                      className="text-primary w-100"
                      onClick={() => navigate('/admin/inventory', { state: { activeTab: '2' } })}
                    >
                      View All Low Stock Items
                    </Button>
                  </div>
                )}
              </DropdownMenu>
            </UncontrolledDropdown>
            
            <UncontrolledDropdown nav inNavbar>
              <DropdownToggle nav caret className="d-flex align-items-center">
                <FaUserCircle size={20} className="me-2" />
                <span className="d-none d-md-inline">Admin</span>
              </DropdownToggle>
              <DropdownMenu end>
                <DropdownItem header>Admin Account</DropdownItem>
                <DropdownItem tag={Link} to="/admin/profile">
                  <FaUser className="me-2" /> Profile
                </DropdownItem>
                <DropdownItem tag={Link} to="/admin/settings">
                  <FaCog className="me-2" /> Settings
                </DropdownItem>
                <DropdownItem divider />
                <DropdownItem onClick={handleLogout}>
                  <FaSignOutAlt className="me-2" /> Logout
                </DropdownItem>
              </DropdownMenu>
            </UncontrolledDropdown>
          </Nav>
        </Collapse>
      </Container>
    </Navbar>
  );
};

export default AdminHeader;
