import React from 'react';
import { 
  Box, 
  Typography, 
  Button, 
  Container, 
  Grid, 
  Paper, 
  Card, 
  useTheme,
  Avatar
} from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import GridItem from '../components/CustomGrid';
import PeopleIcon from '@mui/icons-material/People';
import BusinessIcon from '@mui/icons-material/Business';
import WorkIcon from '@mui/icons-material/Work';
import AssignmentIcon from '@mui/icons-material/Assignment';
import BarChartIcon from '@mui/icons-material/BarChart';
import SecurityIcon from '@mui/icons-material/Security';

const Home: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const theme = useTheme();
  
  return (
    <>
      {/* Hero Section */}
      <Box 
        sx={{ 
          background: `linear-gradient(45deg, ${theme.palette.primary.main} 30%, ${theme.palette.primary.light} 90%)`,
          color: 'white',
          py: 8,
          borderRadius: 2,
          mb: 6,
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        <Box 
          sx={{
            position: 'absolute',
            top: 0,
            right: 0,
            width: '100%',
            height: '100%',
            opacity: 0.1,
            background: 'url("https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=1200") no-repeat center center',
            backgroundSize: 'cover',
            zIndex: 0
          }}
        />
        <Container maxWidth="lg" sx={{ position: 'relative', zIndex: 1 }}>
          <Grid container spacing={4} alignItems="center">
            <GridItem item xs={12} md={7}>
              <Typography 
                variant="h2" 
                component="h1" 
                fontWeight="bold" 
                gutterBottom
              >
                Streamline Your Workforce Management
              </Typography>
              <Typography variant="h5" paragraph sx={{ mb: 4, opacity: 0.9 }}>
                A complete solution for HR administrators and employees to manage personnel data, 
                department structures, and job information in one secure platform.
              </Typography>
              
              {!isAuthenticated && (
                <Box sx={{ mt: 4 }}>
                  <Button 
                    variant="contained" 
                    color="secondary" 
                    component={RouterLink} 
                    to="/register"
                    size="large"
                    sx={{ 
                      mr: 2, 
                      px: 4, 
                      py: 1.5,
                      fontSize: '1.1rem',
                      boxShadow: '0 4px 14px rgba(0,0,0,0.2)'
                    }}
                  >
                    Get Started
                  </Button>
                  <Button 
                    variant="outlined" 
                    component={RouterLink} 
                    to="/login"
                    size="large"
                    sx={{ 
                      px: 4, 
                      py: 1.5,
                      fontSize: '1.1rem',
                      color: 'white',
                      borderColor: 'white',
                      '&:hover': {
                        borderColor: 'white',
                        backgroundColor: 'rgba(255,255,255,0.1)'
                      }
                    }}
                  >
                    Sign In
                  </Button>
                </Box>
              )}
              
              {isAuthenticated && (
                <Button 
                  variant="contained" 
                  color="secondary" 
                  component={RouterLink} 
                  to="/dashboard"
                  size="large"
                  sx={{ 
                    mt: 2, 
                    px: 4, 
                    py: 1.5,
                    fontSize: '1.1rem',
                    boxShadow: '0 4px 14px rgba(0,0,0,0.2)'
                  }}
                >
                  Go to Dashboard
                </Button>
              )}
            </GridItem>
            <GridItem item xs={12} md={5}>
              <Box 
                component="img"
                src="https://img.freepik.com/free-vector/team-concept-illustration_114360-678.jpg?w=740&t=st=1712318574~exp=1712319174~hmac=1fd12ea73874b3b90b8d626a20fc4ac93ee00db38c4f1d6be747fe9c3fe3bfb1"
                alt="Workforce Management"
                sx={{
                  width: '100%',
                  height: 'auto',
                  borderRadius: 4,
                  boxShadow: '0 10px 30px rgba(0,0,0,0.15)',
                  transform: 'perspective(1500px) rotateY(-15deg)',
                  transition: 'transform 0.3s ease-in-out',
                  '&:hover': {
                    transform: 'perspective(1500px) rotateY(-5deg)',
                  }
                }}
              />
            </GridItem>
          </Grid>
        </Container>
      </Box>
      
      {/* Features Section */}
      <Container maxWidth="lg" sx={{ mb: 8 }}>
        <Box sx={{ textAlign: 'center', mb: 6 }}>
          <Typography 
            variant="h3" 
            component="h2" 
            gutterBottom
            fontWeight="bold"
            color="primary"
          >
            Key Features
          </Typography>
          <Typography variant="h6" color="text.secondary" sx={{ maxWidth: 700, mx: 'auto' }}>
            Our comprehensive platform provides everything you need to efficiently manage your organization's workforce
          </Typography>
        </Box>
        
        <Grid container spacing={4}>
          <GridItem item xs={12} md={4}>
            <Card 
              elevation={0} 
              sx={{ 
                height: '100%', 
                p: 2, 
                display: 'flex', 
                flexDirection: 'column',
                borderRadius: 4,
                transition: 'all 0.3s ease',
                border: `1px solid ${theme.palette.divider}`,
                '&:hover': {
                  boxShadow: '0 10px 30px rgba(0,0,0,0.08)',
                  borderColor: theme.palette.primary.main,
                  transform: 'translateY(-5px)'
                }
              }}
            >
              <Box 
                sx={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  mb: 2 
                }}
              >
                <Avatar 
                  sx={{ 
                    bgcolor: theme.palette.primary.main,
                    width: 56,
                    height: 56,
                    mr: 2
                  }}
                >
                  <PeopleIcon fontSize="large" />
                </Avatar>
                <Typography variant="h5" component="h3" fontWeight="bold">
                  Employee Management
                </Typography>
              </Box>
              <Typography variant="body1" color="text.secondary" paragraph sx={{ mb: 3 }}>
                Easily manage employee records, profiles, and personal information. Track career progression,
                department assignments, and maintain comprehensive personnel files.
              </Typography>
              <Box sx={{ flexGrow: 1 }} />
              <Button 
                variant="outlined" 
                color="primary" 
                component={RouterLink} 
                to={isAuthenticated ? "/employees" : "/register"}
                fullWidth
                sx={{ mt: 2 }}
              >
                {isAuthenticated ? "View Employees" : "Learn More"}
              </Button>
            </Card>
          </GridItem>
          
          <GridItem item xs={12} md={4}>
            <Card 
              elevation={0} 
              sx={{ 
                height: '100%', 
                p: 2, 
                display: 'flex', 
                flexDirection: 'column',
                borderRadius: 4,
                transition: 'all 0.3s ease',
                border: `1px solid ${theme.palette.divider}`,
                '&:hover': {
                  boxShadow: '0 10px 30px rgba(0,0,0,0.08)',
                  borderColor: theme.palette.primary.main,
                  transform: 'translateY(-5px)'
                }
              }}
            >
              <Box 
                sx={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  mb: 2 
                }}
              >
                <Avatar 
                  sx={{ 
                    bgcolor: theme.palette.primary.main,
                    width: 56,
                    height: 56,
                    mr: 2
                  }}
                >
                  <BusinessIcon fontSize="large" />
                </Avatar>
                <Typography variant="h5" component="h3" fontWeight="bold">
                  Department Organization
                </Typography>
              </Box>
              <Typography variant="body1" color="text.secondary" paragraph sx={{ mb: 3 }}>
                Create and manage departments, assign employees, and organize your company structure.
                Visualize organizational hierarchies and ensure proper departmental staffing.
              </Typography>
              <Box sx={{ flexGrow: 1 }} />
              <Button 
                variant="outlined" 
                color="primary" 
                component={RouterLink} 
                to={isAuthenticated ? "/departments" : "/register"}
                fullWidth
                sx={{ mt: 2 }}
              >
                {isAuthenticated ? "View Departments" : "Learn More"}
              </Button>
            </Card>
          </GridItem>
          
          <GridItem item xs={12} md={4}>
            <Card 
              elevation={0} 
              sx={{ 
                height: '100%', 
                p: 2, 
                display: 'flex', 
                flexDirection: 'column',
                borderRadius: 4,
                transition: 'all 0.3s ease',
                border: `1px solid ${theme.palette.divider}`,
                '&:hover': {
                  boxShadow: '0 10px 30px rgba(0,0,0,0.08)',
                  borderColor: theme.palette.primary.main,
                  transform: 'translateY(-5px)'
                }
              }}
            >
              <Box 
                sx={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  mb: 2 
                }}
              >
                <Avatar 
                  sx={{ 
                    bgcolor: theme.palette.primary.main,
                    width: 56,
                    height: 56,
                    mr: 2
                  }}
                >
                  <WorkIcon fontSize="large" />
                </Avatar>
                <Typography variant="h5" component="h3" fontWeight="bold">
                  Job Management
                </Typography>
              </Box>
              <Typography variant="body1" color="text.secondary" paragraph sx={{ mb: 3 }}>
                Define job titles, descriptions, and pay grades for better workforce organization.
                Standardize roles across your organization with detailed job responsibilities.
              </Typography>
              <Box sx={{ flexGrow: 1 }} />
              <Button 
                variant="outlined" 
                color="primary" 
                component={RouterLink} 
                to={isAuthenticated ? "/jobs" : "/register"}
                fullWidth
                sx={{ mt: 2 }}
              >
                {isAuthenticated ? "View Job Titles" : "Learn More"}
              </Button>
            </Card>
          </GridItem>
        </Grid>
      </Container>
      
      {/* Additional Features */}
      <Box sx={{ bgcolor: 'background.paper', py: 8, mb: 8 }}>
        <Container maxWidth="lg">
          <Box sx={{ textAlign: 'center', mb: 6 }}>
            <Typography variant="h3" component="h2" gutterBottom fontWeight="bold">
              Why Choose WorkforceHub?
            </Typography>
            <Typography variant="h6" color="text.secondary" sx={{ maxWidth: 700, mx: 'auto' }}>
              Our platform is designed with your organization's needs in mind
            </Typography>
          </Box>
          
          <Grid container spacing={4}>
            <GridItem item xs={12} sm={6} md={4}>
              <Box sx={{ textAlign: 'center', p: 2 }}>
                <Avatar 
                  sx={{ 
                    bgcolor: theme.palette.primary.light,
                    width: 70,
                    height: 70,
                    mx: 'auto',
                    mb: 2
                  }}
                >
                  <SecurityIcon fontSize="large" />
                </Avatar>
                <Typography variant="h5" component="h3" fontWeight="500" gutterBottom>
                  Secure Data Management
                </Typography>
                <Typography color="text.secondary">
                  Advanced security features to protect sensitive personnel information
                </Typography>
              </Box>
            </GridItem>
            <GridItem item xs={12} sm={6} md={4}>
              <Box sx={{ textAlign: 'center', p: 2 }}>
                <Avatar 
                  sx={{ 
                    bgcolor: theme.palette.primary.light,
                    width: 70,
                    height: 70,
                    mx: 'auto',
                    mb: 2
                  }}
                >
                  <BarChartIcon fontSize="large" />
                </Avatar>
                <Typography variant="h5" component="h3" fontWeight="500" gutterBottom>
                  Insightful Analytics
                </Typography>
                <Typography color="text.secondary">
                  Gain valuable insights into workforce composition and departmental structure
                </Typography>
              </Box>
            </GridItem>
            <GridItem item xs={12} sm={6} md={4}>
              <Box sx={{ textAlign: 'center', p: 2 }}>
                <Avatar 
                  sx={{ 
                    bgcolor: theme.palette.primary.light,
                    width: 70,
                    height: 70,
                    mx: 'auto',
                    mb: 2
                  }}
                >
                  <AssignmentIcon fontSize="large" />
                </Avatar>
                <Typography variant="h5" component="h3" fontWeight="500" gutterBottom>
                  Document Management
                </Typography>
                <Typography color="text.secondary">
                  Organize and store employee documents securely in one centralized system
                </Typography>
              </Box>
            </GridItem>
          </Grid>
        </Container>
      </Box>
      
      {/* Call to Action */}
      <Container maxWidth="md" sx={{ mb: 8 }}>
        <Paper 
          elevation={0} 
          sx={{ 
            p: 6, 
            textAlign: 'center',
            borderRadius: 4,
            background: `linear-gradient(45deg, ${theme.palette.secondary.dark} 30%, ${theme.palette.secondary.main} 90%)`,
            color: 'white',
            boxShadow: '0 10px 30px rgba(0,0,0,0.15)'
          }}
        >
          <Typography variant="h3" component="h2" gutterBottom fontWeight="bold">
            Ready to Transform Your HR Processes?
          </Typography>
          <Typography variant="h6" paragraph sx={{ mb: 4, maxWidth: 700, mx: 'auto' }}>
            Join thousands of organizations that use WorkforceHub to streamline their workforce management
          </Typography>
          
          {!isAuthenticated && (
            <Button 
              variant="contained" 
              color="primary" 
              component={RouterLink} 
              to="/register"
              size="large"
              sx={{ 
                px: 5, 
                py: 1.5, 
                fontSize: '1.2rem',
                backgroundColor: 'white',
                color: theme.palette.secondary.main,
                '&:hover': {
                  backgroundColor: 'rgba(255,255,255,0.9)'
                }
              }}
            >
              Get Started Today
            </Button>
          )}
          
          {isAuthenticated && (
            <Button 
              variant="contained" 
              color="primary" 
              component={RouterLink} 
              to="/dashboard"
              size="large"
              sx={{ 
                px: 5, 
                py: 1.5, 
                fontSize: '1.2rem',
                backgroundColor: 'white',
                color: theme.palette.secondary.main,
                '&:hover': {
                  backgroundColor: 'rgba(255,255,255,0.9)'
                }
              }}
            >
              Go to Dashboard
            </Button>
          )}
        </Paper>
      </Container>
    </>
  );
};

export default Home; 