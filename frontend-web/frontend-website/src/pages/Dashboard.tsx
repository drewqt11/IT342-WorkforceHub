import React, { useEffect, useState } from 'react';
import { 
  Box, 
  Typography, 
  Paper, 
  Grid, 
  CircularProgress, 
  Card, 
  CardContent, 
  Divider, 
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  LinearProgress,
  Avatar,
  IconButton,
  Menu,
  MenuItem,
  Tooltip,
  useTheme
} from '@mui/material';
import { getCurrentEmployeeProfile } from '../services/api';
import { Employee } from '../types';
import { useAuth } from '../context/AuthContext';
import GridItem from '../components/CustomGrid';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import PersonIcon from '@mui/icons-material/Person';
import PhoneIcon from '@mui/icons-material/Phone';
import EmailIcon from '@mui/icons-material/Email';
import HomeIcon from '@mui/icons-material/Home';
import WorkIcon from '@mui/icons-material/Work';
import BusinessIcon from '@mui/icons-material/Business';
import EventIcon from '@mui/icons-material/Event';
import AssignmentIcon from '@mui/icons-material/Assignment';
import BarChartIcon from '@mui/icons-material/BarChart';
import WatchLaterIcon from '@mui/icons-material/WatchLater';

const Dashboard: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [employeeData, setEmployeeData] = useState<Employee | null>(null);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const { user } = useAuth();
  const theme = useTheme();
  
  // Mock data for quick statistics
  const stats = [
    { label: 'Tasks Completed', value: 12, percentage: 75 },
    { label: 'Projects Active', value: 3, percentage: 60 },
    { label: 'Days Present', value: 18, percentage: 90 }
  ];
  
  // Mock data for recent activities
  const recentActivities = [
    { activity: 'Updated personal information', time: '2 hours ago' },
    { activity: 'Submitted leave request', time: '1 day ago' },
    { activity: 'Completed quarterly review', time: '3 days ago' },
    { activity: 'Enrolled in training course', time: '1 week ago' }
  ];
  
  // Mock data for upcoming events
  const upcomingEvents = [
    { title: 'Team meeting', date: 'Today, 2:00 PM' },
    { title: 'Project deadline', date: 'Tomorrow, 5:00 PM' },
    { title: 'Performance review', date: 'Next Monday, 10:00 AM' }
  ];
  
  const handleMenuClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  
  const handleMenuClose = () => {
    setAnchorEl(null);
  };
  
  useEffect(() => {
    const fetchEmployeeProfile = async () => {
      try {
        const data = await getCurrentEmployeeProfile();
        setEmployeeData(data);
      } catch (error) {
        console.error('Error fetching employee profile:', error);
      } finally {
        setLoading(false);
      }
    };
    
    fetchEmployeeProfile();
  }, []);
  
  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <CircularProgress />
      </Box>
    );
  }
  
  return (
    <Box>
      {/* Welcome Section */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom fontWeight="bold">
          Dashboard
        </Typography>
        
        <Paper 
          elevation={0} 
          sx={{ 
            p: 3, 
            mb: 4, 
            backgroundImage: `linear-gradient(to right, ${theme.palette.primary.main}, ${theme.palette.primary.light})`,
            color: 'white',
            borderRadius: 2
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <Avatar 
              sx={{ 
                width: 64, 
                height: 64, 
                bgcolor: theme.palette.primary.dark,
                fontSize: '1.5rem',
                mr: 2,
                border: '2px solid white'
              }}
            >
              {user?.firstName?.charAt(0) || 'U'}
            </Avatar>
            <Box>
              <Typography variant="h5" fontWeight="bold">
                Welcome back, {user?.firstName || 'User'}!
              </Typography>
              <Typography variant="body1">
                {new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
              </Typography>
            </Box>
          </Box>
        </Paper>
      </Box>
      
      {/* Quick Stats Section */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {stats.map((stat, index) => (
          <GridItem item xs={12} md={4} key={index}>
            <Card 
              elevation={0} 
              sx={{ 
                height: '100%',
                borderRadius: 2,
                border: `1px solid ${theme.palette.divider}`,
                transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
                '&:hover': {
                  transform: 'translateY(-5px)',
                  boxShadow: '0 10px 20px rgba(0,0,0,0.1)'
                }
              }}
            >
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="h5" fontWeight="bold" color="primary">
                    {stat.value}
                  </Typography>
                  <Avatar sx={{ bgcolor: theme.palette.primary.light, width: 40, height: 40 }}>
                    {index === 0 ? <AssignmentIcon /> : index === 1 ? <BarChartIcon /> : <WatchLaterIcon />}
                  </Avatar>
                </Box>
                <Typography variant="body1" color="text.secondary" gutterBottom>
                  {stat.label}
                </Typography>
                <LinearProgress 
                  variant="determinate" 
                  value={stat.percentage}
                  sx={{ 
                    mt: 1,
                    height: 6,
                    borderRadius: 3,
                    backgroundColor: theme.palette.grey[200],
                    '& .MuiLinearProgress-bar': {
                      borderRadius: 3,
                      backgroundColor: index === 0 
                        ? theme.palette.primary.main 
                        : index === 1 
                          ? theme.palette.secondary.main 
                          : theme.palette.success.main
                    }
                  }}
                />
                <Typography variant="caption" sx={{ mt: 0.5, display: 'block', textAlign: 'right' }}>
                  {stat.percentage}%
                </Typography>
              </CardContent>
            </Card>
          </GridItem>
        ))}
      </Grid>
      
      {employeeData ? (
        <Grid container spacing={3}>
          {/* Personal Information Card */}
          <GridItem item xs={12} md={6}>
            <Card 
              elevation={0} 
              sx={{ 
                height: '100%', 
                borderRadius: 2,
                border: `1px solid ${theme.palette.divider}`
              }}
            >
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                  <Typography variant="h6" fontWeight="bold">
                    Personal Information
                  </Typography>
                  <Tooltip title="More options">
                    <IconButton onClick={handleMenuClick}>
                      <MoreVertIcon />
                    </IconButton>
                  </Tooltip>
                  <Menu
                    anchorEl={anchorEl}
                    open={Boolean(anchorEl)}
                    onClose={handleMenuClose}
                  >
                    <MenuItem onClick={handleMenuClose}>Edit profile</MenuItem>
                    <MenuItem onClick={handleMenuClose}>Update photo</MenuItem>
                    <MenuItem onClick={handleMenuClose}>View history</MenuItem>
                  </Menu>
                </Box>
                <Divider sx={{ mb: 2 }} />
                <List disablePadding>
                  <ListItem disableGutters>
                    <ListItemIcon sx={{ minWidth: 40 }}>
                      <PersonIcon color="primary" />
                    </ListItemIcon>
                    <ListItemText 
                      primary={<Typography variant="body2" color="text.secondary">Full Name</Typography>}
                      secondary={`${employeeData.firstName} ${employeeData.lastName}`}
                    />
                  </ListItem>
                  <ListItem disableGutters>
                    <ListItemIcon sx={{ minWidth: 40 }}>
                      <EmailIcon color="primary" />
                    </ListItemIcon>
                    <ListItemText 
                      primary={<Typography variant="body2" color="text.secondary">Email</Typography>}
                      secondary={employeeData.email}
                    />
                  </ListItem>
                  <ListItem disableGutters>
                    <ListItemIcon sx={{ minWidth: 40 }}>
                      <PhoneIcon color="primary" />
                    </ListItemIcon>
                    <ListItemText 
                      primary={<Typography variant="body2" color="text.secondary">Phone</Typography>}
                      secondary={employeeData.phone || 'Not specified'}
                    />
                  </ListItem>
                  <ListItem disableGutters>
                    <ListItemIcon sx={{ minWidth: 40 }}>
                      <HomeIcon color="primary" />
                    </ListItemIcon>
                    <ListItemText 
                      primary={<Typography variant="body2" color="text.secondary">Address</Typography>}
                      secondary={employeeData.address || 'Not specified'}
                    />
                  </ListItem>
                  <ListItem disableGutters>
                    <ListItemIcon sx={{ minWidth: 40 }}>
                      <PersonIcon color="primary" />
                    </ListItemIcon>
                    <ListItemText 
                      primary={<Typography variant="body2" color="text.secondary">Gender</Typography>}
                      secondary={employeeData.gender || 'Not specified'}
                    />
                  </ListItem>
                </List>
              </CardContent>
            </Card>
          </GridItem>
          
          {/* Employment Information Card */}
          <GridItem item xs={12} md={6}>
            <Card 
              elevation={0} 
              sx={{ 
                height: '100%', 
                borderRadius: 2,
                border: `1px solid ${theme.palette.divider}`
              }}
            >
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                  <Typography variant="h6" fontWeight="bold">
                    Employment Information
                  </Typography>
                </Box>
                <Divider sx={{ mb: 2 }} />
                <List disablePadding>
                  <ListItem disableGutters>
                    <ListItemIcon sx={{ minWidth: 40 }}>
                      <BusinessIcon color="primary" />
                    </ListItemIcon>
                    <ListItemText 
                      primary={<Typography variant="body2" color="text.secondary">Department</Typography>}
                      secondary={employeeData.departmentName || 'Not assigned'}
                    />
                  </ListItem>
                  <ListItem disableGutters>
                    <ListItemIcon sx={{ minWidth: 40 }}>
                      <WorkIcon color="primary" />
                    </ListItemIcon>
                    <ListItemText 
                      primary={<Typography variant="body2" color="text.secondary">Job Title</Typography>}
                      secondary={employeeData.jobName || 'Not assigned'}
                    />
                  </ListItem>
                  <ListItem disableGutters>
                    <ListItemIcon sx={{ minWidth: 40 }}>
                      <PersonIcon color="primary" />
                    </ListItemIcon>
                    <ListItemText 
                      primary={<Typography variant="body2" color="text.secondary">Role</Typography>}
                      secondary={employeeData.roleName || 'Not assigned'}
                    />
                  </ListItem>
                  <ListItem disableGutters>
                    <ListItemIcon sx={{ minWidth: 40 }}>
                      <EventIcon color="primary" />
                    </ListItemIcon>
                    <ListItemText 
                      primary={<Typography variant="body2" color="text.secondary">Hire Date</Typography>}
                      secondary={employeeData.hireDate || 'Not specified'}
                    />
                  </ListItem>
                  <ListItem disableGutters>
                    <ListItemIcon sx={{ minWidth: 40 }}>
                      <EventIcon color="primary" />
                    </ListItemIcon>
                    <ListItemText 
                      primary={<Typography variant="body2" color="text.secondary">Status</Typography>}
                      secondary={
                        <Box component="span" sx={{ 
                          px: 1, 
                          py: 0.5, 
                          borderRadius: 1, 
                          fontSize: '0.8125rem',
                          bgcolor: employeeData.status === 'ACTIVE' ? 'success.light' : 'info.light',
                          color: employeeData.status === 'ACTIVE' ? 'success.dark' : 'info.dark',
                        }}>
                          {employeeData.status}
                        </Box>
                      }
                    />
                  </ListItem>
                </List>
              </CardContent>
            </Card>
          </GridItem>
          
          {/* Recent Activities */}
          <GridItem item xs={12} md={6}>
            <Card 
              elevation={0} 
              sx={{ 
                height: '100%', 
                borderRadius: 2,
                border: `1px solid ${theme.palette.divider}`
              }}
            >
              <CardContent>
                <Typography variant="h6" fontWeight="bold" gutterBottom>
                  Recent Activities
                </Typography>
                <Divider sx={{ mb: 2 }} />
                <List disablePadding>
                  {recentActivities.map((activity, index) => (
                    <React.Fragment key={index}>
                      <ListItem 
                        disableGutters 
                        sx={{ py: 1.5 }}
                      >
                        <ListItemIcon sx={{ minWidth: 40 }}>
                          <Avatar sx={{ 
                            width: 32, 
                            height: 32, 
                            bgcolor: index % 3 === 0 ? 'primary.light' : index % 3 === 1 ? 'secondary.light' : 'success.light',
                            color: index % 3 === 0 ? 'primary.dark' : index % 3 === 1 ? 'secondary.dark' : 'success.dark',
                            fontSize: '0.875rem'
                          }}>
                            {activity.activity.charAt(0)}
                          </Avatar>
                        </ListItemIcon>
                        <ListItemText 
                          primary={activity.activity}
                          secondary={activity.time}
                        />
                      </ListItem>
                      {index < recentActivities.length - 1 && <Divider component="li" />}
                    </React.Fragment>
                  ))}
                </List>
              </CardContent>
            </Card>
          </GridItem>
          
          {/* Upcoming Events */}
          <GridItem item xs={12} md={6}>
            <Card 
              elevation={0} 
              sx={{ 
                height: '100%', 
                borderRadius: 2,
                border: `1px solid ${theme.palette.divider}`
              }}
            >
              <CardContent>
                <Typography variant="h6" fontWeight="bold" gutterBottom>
                  Upcoming Events
                </Typography>
                <Divider sx={{ mb: 2 }} />
                <List disablePadding>
                  {upcomingEvents.map((event, index) => (
                    <React.Fragment key={index}>
                      <ListItem 
                        disableGutters 
                        sx={{ py: 1.5 }}
                      >
                        <ListItemIcon sx={{ minWidth: 40 }}>
                          <Avatar sx={{ 
                            width: 32, 
                            height: 32, 
                            bgcolor: index === 0 
                              ? 'error.light' 
                              : index === 1 
                                ? 'warning.light' 
                                : 'info.light',
                            color: index === 0 
                              ? 'error.dark' 
                              : index === 1 
                                ? 'warning.dark' 
                                : 'info.dark',
                            fontSize: '0.875rem'
                          }}>
                            {event.title.charAt(0)}
                          </Avatar>
                        </ListItemIcon>
                        <ListItemText 
                          primary={event.title}
                          secondary={event.date}
                        />
                      </ListItem>
                      {index < upcomingEvents.length - 1 && <Divider component="li" />}
                    </React.Fragment>
                  ))}
                </List>
              </CardContent>
            </Card>
          </GridItem>
        </Grid>
      ) : (
        <Typography color="error">Failed to load employee data</Typography>
      )}
    </Box>
  );
};

export default Dashboard; 