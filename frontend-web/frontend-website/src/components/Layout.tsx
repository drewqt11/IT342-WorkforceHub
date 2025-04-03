import React, { useState } from 'react';
import { Link as RouterLink, useNavigate, useLocation } from 'react-router-dom';
import { 
  AppBar, 
  Box, 
  Toolbar, 
  Typography, 
  Button, 
  IconButton,
  Menu, 
  MenuItem, 
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Avatar,
  Divider,
  useMediaQuery,
  useTheme,
  Badge,
  Tooltip
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PeopleIcon from '@mui/icons-material/People';
import BusinessIcon from '@mui/icons-material/Business';
import WorkIcon from '@mui/icons-material/Work';
import NotificationsIcon from '@mui/icons-material/Notifications';
import LogoutIcon from '@mui/icons-material/Logout';
import PersonIcon from '@mui/icons-material/Person';
import { useAuth } from '../context/AuthContext';

const DRAWER_WIDTH = 240;

const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [mobileOpen, setMobileOpen] = useState(false);
  
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const [notificationAnchorEl, setNotificationAnchorEl] = React.useState<null | HTMLElement>(null);

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleNotificationMenu = (event: React.MouseEvent<HTMLElement>) => {
    setNotificationAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleNotificationClose = () => {
    setNotificationAnchorEl(null);
  };

  const handleLogout = () => {
    logout();
    handleClose();
    navigate('/login');
  };

  const handleProfile = () => {
    handleClose();
    navigate('/profile');
  };

  const getInitials = (name?: string) => {
    if (!name) return "U";
    
    const nameParts = name.split(' ');
    if (nameParts.length >= 2) {
      return `${nameParts[0][0]}${nameParts[1][0]}`;
    }
    return name.substring(0, 2);
  };

  const drawer = (
    <>
      <Box sx={{ 
        display: 'flex', 
        flexDirection: 'column', 
        alignItems: 'center', 
        py: 2,
        backgroundColor: theme.palette.primary.main,
        color: theme.palette.primary.contrastText
      }}>
        <Avatar 
          sx={{ 
            width: 64, 
            height: 64, 
            mb: 1, 
            bgcolor: theme.palette.primary.dark,
            fontSize: '1.5rem',
            fontWeight: 'bold'
          }}
        >
          {getInitials(user?.firstName + ' ' + user?.lastName)}
        </Avatar>
        <Typography variant="subtitle1" fontWeight="bold">
          {user?.firstName} {user?.lastName}
        </Typography>
        <Typography variant="caption">
          {user?.role || 'User'}
        </Typography>
      </Box>
      <Divider />
      <List component="nav">
        <ListItemButton 
          component={RouterLink} 
          to="/dashboard" 
          selected={location.pathname === '/dashboard'}
          onClick={() => isMobile && setMobileOpen(false)}
        >
          <ListItemIcon>
            <DashboardIcon color={location.pathname === '/dashboard' ? 'primary' : undefined} />
          </ListItemIcon>
          <ListItemText primary="Dashboard" />
        </ListItemButton>
        
        {user?.role === 'HR_ADMIN' && (
          <>
            <ListItemButton 
              component={RouterLink} 
              to="/employees" 
              selected={location.pathname === '/employees'}
              onClick={() => isMobile && setMobileOpen(false)}
            >
              <ListItemIcon>
                <PeopleIcon color={location.pathname === '/employees' ? 'primary' : undefined} />
              </ListItemIcon>
              <ListItemText primary="Employees" />
            </ListItemButton>
            
            <ListItemButton 
              component={RouterLink} 
              to="/departments" 
              selected={location.pathname === '/departments'}
              onClick={() => isMobile && setMobileOpen(false)}
            >
              <ListItemIcon>
                <BusinessIcon color={location.pathname === '/departments' ? 'primary' : undefined} />
              </ListItemIcon>
              <ListItemText primary="Departments" />
            </ListItemButton>
            
            <ListItemButton 
              component={RouterLink} 
              to="/jobs" 
              selected={location.pathname === '/jobs'}
              onClick={() => isMobile && setMobileOpen(false)}
            >
              <ListItemIcon>
                <WorkIcon color={location.pathname === '/jobs' ? 'primary' : undefined} />
              </ListItemIcon>
              <ListItemText primary="Job Titles" />
            </ListItemButton>
          </>
        )}
      </List>
      <Divider />
      <List>
        <ListItemButton onClick={handleProfile}>
          <ListItemIcon>
            <PersonIcon />
          </ListItemIcon>
          <ListItemText primary="My Profile" />
        </ListItemButton>
        <ListItemButton onClick={handleLogout}>
          <ListItemIcon>
            <LogoutIcon />
          </ListItemIcon>
          <ListItemText primary="Logout" />
        </ListItemButton>
      </List>
    </>
  );

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar position="fixed" elevation={0} sx={{ zIndex: theme.zIndex.drawer + 1 }}>
        <Toolbar>
          {isAuthenticated && (
            <IconButton
              color="inherit"
              aria-label="open drawer"
              edge="start"
              onClick={handleDrawerToggle}
              sx={{ mr: 2, display: { md: 'none' } }}
            >
              <MenuIcon />
            </IconButton>
          )}
          
          <Typography
            variant="h6"
            component={RouterLink}
            to="/"
            sx={{ 
              flexGrow: 1, 
              textDecoration: 'none', 
              color: 'inherit',
              fontWeight: 'bold',
              display: 'flex',
              alignItems: 'center'
            }}
          >
            <Box 
              component="span" 
              sx={{ 
                bgcolor: 'primary.contrastText', 
                color: 'primary.main',
                px: 1,
                py: 0.5,
                borderRadius: 1,
                mr: 1,
                fontWeight: 'bold'
              }}
            >
              WH
            </Box>
            WorkforceHub
          </Typography>

          {isAuthenticated ? (
            <>
              {!isMobile && (
                <>
                  <Button color="inherit" component={RouterLink} to="/dashboard">
                    Dashboard
                  </Button>
                  {user?.role === 'HR_ADMIN' && (
                    <>
                      <Button color="inherit" component={RouterLink} to="/employees">
                        Employees
                      </Button>
                      <Button color="inherit" component={RouterLink} to="/departments">
                        Departments
                      </Button>
                      <Button color="inherit" component={RouterLink} to="/jobs">
                        Job Titles
                      </Button>
                    </>
                  )}
                </>
              )}
              
              <Tooltip title="Notifications">
                <IconButton color="inherit" onClick={handleNotificationMenu}>
                  <Badge badgeContent={3} color="error">
                    <NotificationsIcon />
                  </Badge>
                </IconButton>
              </Tooltip>
              
              <Menu
                id="notification-menu"
                anchorEl={notificationAnchorEl}
                anchorOrigin={{
                  vertical: 'bottom',
                  horizontal: 'right',
                }}
                keepMounted
                transformOrigin={{
                  vertical: 'top',
                  horizontal: 'right',
                }}
                open={Boolean(notificationAnchorEl)}
                onClose={handleNotificationClose}
              >
                <MenuItem onClick={handleNotificationClose}>New employee registered</MenuItem>
                <MenuItem onClick={handleNotificationClose}>Department update requested</MenuItem>
                <MenuItem onClick={handleNotificationClose}>System maintenance scheduled</MenuItem>
              </Menu>
              
              <IconButton
                size="large"
                aria-label="account of current user"
                aria-controls="menu-appbar"
                aria-haspopup="true"
                onClick={handleMenu}
                color="inherit"
              >
                <Avatar 
                  sx={{ 
                    width: 32, 
                    height: 32, 
                    bgcolor: theme.palette.primary.contrastText,
                    color: theme.palette.primary.main,
                    fontSize: '0.875rem',
                    fontWeight: 'bold'
                  }}
                >
                  {getInitials(user?.firstName + ' ' + user?.lastName)}
                </Avatar>
              </IconButton>
              <Menu
                id="menu-appbar"
                anchorEl={anchorEl}
                anchorOrigin={{
                  vertical: 'bottom',
                  horizontal: 'right',
                }}
                keepMounted
                transformOrigin={{
                  vertical: 'top',
                  horizontal: 'right',
                }}
                open={Boolean(anchorEl)}
                onClose={handleClose}
              >
                <MenuItem onClick={handleProfile}>Profile</MenuItem>
                <MenuItem onClick={handleLogout}>Logout</MenuItem>
              </Menu>
            </>
          ) : (
            <>
              <Button 
                color="inherit" 
                component={RouterLink} 
                to="/login"
                variant="outlined"
                sx={{ 
                  ml: 1,
                  borderColor: 'rgba(255,255,255,0.5)',
                  '&:hover': {
                    borderColor: 'white',
                    backgroundColor: 'rgba(255,255,255,0.08)'
                  }
                }}
              >
                Login
              </Button>
              <Button 
                variant="contained" 
                color="secondary" 
                component={RouterLink} 
                to="/register"
                sx={{ ml: 1 }}
              >
                Register
              </Button>
            </>
          )}
        </Toolbar>
      </AppBar>

      {isAuthenticated && (
        <>
          {/* Mobile drawer */}
          <Drawer
            variant="temporary"
            open={mobileOpen}
            onClose={handleDrawerToggle}
            ModalProps={{
              keepMounted: true, // Better open performance on mobile.
            }}
            sx={{
              display: { xs: 'block', md: 'none' },
              '& .MuiDrawer-paper': { boxSizing: 'border-box', width: DRAWER_WIDTH },
            }}
          >
            {drawer}
          </Drawer>
          
          {/* Desktop drawer */}
          <Drawer
            variant="permanent"
            sx={{
              display: { xs: 'none', md: 'block' },
              '& .MuiDrawer-paper': { 
                boxSizing: 'border-box', 
                width: DRAWER_WIDTH,
                borderRight: `1px solid ${theme.palette.divider}`,
                height: '100%',
                top: 64, // AppBar height
              },
            }}
            open
          >
            {drawer}
          </Drawer>
        </>
      )}

      <Box 
        component="main" 
        sx={{ 
          flexGrow: 1, 
          pt: isAuthenticated ? '64px' : '64px', // AppBar height
          ml: isAuthenticated ? { xs: 0, md: `${DRAWER_WIDTH}px` } : 0,
          px: { xs: 2, sm: 3, md: 4 },
          py: 4,
          maxWidth: isAuthenticated 
            ? { xs: '100%', md: `calc(100% - ${DRAWER_WIDTH}px)` } 
            : '100%',
        }}
      >
        {children}
      </Box>

      <Box 
        component="footer" 
        sx={{ 
          py: 2, 
          bgcolor: 'rgba(0, 0, 0, 0.03)', 
          textAlign: 'center',
          borderTop: `1px solid ${theme.palette.divider}`,
          ml: isAuthenticated ? { xs: 0, md: `${DRAWER_WIDTH}px` } : 0,
          maxWidth: isAuthenticated 
            ? { xs: '100%', md: `calc(100% - ${DRAWER_WIDTH}px)` } 
            : '100%',
        }}
      >
        <Typography variant="body2" color="text.secondary">
          © {new Date().getFullYear()} WorkforceHub. All rights reserved.
        </Typography>
      </Box>
    </Box>
  );
};

export default Layout; 