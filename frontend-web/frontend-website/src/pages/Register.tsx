import React, { useState, useEffect } from 'react';
import { 
  TextField, 
  Button, 
  Box, 
  Typography, 
  Paper, 
  Container, 
  Link, 
  IconButton, 
  InputAdornment,
  Divider,
  useTheme,
  Alert,
  Snackbar,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormHelperText,
  SelectChangeEvent
} from '@mui/material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import GoogleIcon from '@mui/icons-material/Google';
import VisibilityIcon from '@mui/icons-material/Visibility';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import { handleGoogleLogin } from '../services/api';
import { useAuth } from '../context/AuthContext';
import GridItem from '../components/CustomGrid';

interface FormData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
  gender: string;
  phone: string;
  address: string;
}

interface FormErrors {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
  gender: string;
  phone: string;
  address: string;
}

const Register: React.FC = () => {
  const [formData, setFormData] = useState<FormData>({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    gender: '',
    phone: '',
    address: ''
  });
  
  const [formErrors, setFormErrors] = useState<FormErrors>({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    gender: '',
    phone: '',
    address: ''
  });
  
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [localError, setLocalError] = useState('');
  const [openSnackbar, setOpenSnackbar] = useState(false);
  
  const { register, error: authError, clearError } = useAuth();
  const navigate = useNavigate();
  const theme = useTheme();

  useEffect(() => {
    // Clear server errors when form changes
    clearError();
    setLocalError('');
  }, [formData, clearError]);

  const validateForm = (): boolean => {
    let valid = true;
    const newErrors: FormErrors = {
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      confirmPassword: '',
      gender: '',
      phone: '',
      address: ''
    };

    // First Name validation
    if (!formData.firstName.trim()) {
      newErrors.firstName = 'First name is required';
      valid = false;
    }

    // Last Name validation
    if (!formData.lastName.trim()) {
      newErrors.lastName = 'Last name is required';
      valid = false;
    }

    // Email validation
    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
      valid = false;
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email is invalid';
      valid = false;
    }

    // Password validation
    if (!formData.password) {
      newErrors.password = 'Password is required';
      valid = false;
    } else if (formData.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters';
      valid = false;
    }

    // Confirm Password validation
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
      valid = false;
    }

    // Phone validation
    if (formData.phone && !/^\+?[\d\s-]{10,15}$/.test(formData.phone)) {
      newErrors.phone = 'Please enter a valid phone number';
      valid = false;
    }

    setFormErrors(newErrors);
    return valid;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSelectChange = (e: SelectChangeEvent) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (validateForm()) {
      setLoading(true);
      try {
        // Get a copy of form data without confirmPassword
        const { confirmPassword, ...registrationData } = formData;
        
        const success = await register(registrationData);
        if (success) {
          navigate('/login');
        }
      } catch (error) {
        let errorMessage = 'Registration failed. Please try again.';
        if (error instanceof Error) {
          errorMessage = error.message;
        }
        setLocalError(errorMessage);
        setOpenSnackbar(true);
      } finally {
        setLoading(false);
      }
    }
  };

  const handleGoogleLoginClick = async () => {
    try {
      await handleGoogleLogin();
    } catch (error) {
      let errorMessage = 'Google login failed.';
      if (error instanceof Error) {
        errorMessage = error.message;
      }
      setLocalError(errorMessage);
      setOpenSnackbar(true);
    }
  };

  const handleCloseSnackbar = () => {
    setOpenSnackbar(false);
  };

  const displayError = authError || localError;

  return (
    <Container component="main" maxWidth="md" sx={{ mt: 8, mb: 8 }}>
      <Paper 
        elevation={0} 
        sx={{ 
          p: { xs: 3, md: 5 }, 
          borderRadius: 2,
          border: `1px solid ${theme.palette.divider}`,
          boxShadow: '0 8px 40px rgba(0,0,0,0.12)',
          overflow: 'hidden',
          position: 'relative'
        }}
      >
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            height: '4px',
            backgroundImage: `linear-gradient(to right, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`
          }}
        />
        
        <Box sx={{ textAlign: 'center', mb: 3 }}>
          <Box 
            sx={{ 
              display: 'inline-flex',
              bgcolor: theme.palette.primary.main, 
              p: 2, 
              borderRadius: '50%',
              mb: 2,
            }}
          >
            <PersonAddIcon sx={{ color: 'white' }} />
          </Box>
          <Typography component="h1" variant="h4" fontWeight="bold" gutterBottom>
            Create an Account
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Join our platform to manage your workforce efficiently
          </Typography>
        </Box>

        <Snackbar 
          open={openSnackbar && !!displayError} 
          autoHideDuration={6000} 
          onClose={handleCloseSnackbar}
          anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
        >
          <Alert onClose={handleCloseSnackbar} severity="error" sx={{ width: '100%' }}>
            {displayError}
          </Alert>
        </Snackbar>

        <Box component="form" onSubmit={handleSubmit} noValidate>
          <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 2, mb: 2 }}>
            <GridItem item xs={12} md={6}>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<GoogleIcon />}
                onClick={handleGoogleLoginClick}
                size="large"
                sx={{ 
                  py: 1.2,
                  borderRadius: 1,
                  color: 'text.primary',
                  borderColor: theme.palette.divider,
                  '&:hover': {
                    backgroundColor: 'rgba(0, 0, 0, 0.04)',
                    borderColor: 'text.secondary'
                  }
                }}
              >
                Continue with Google
              </Button>
            </GridItem>
            
            <GridItem item xs={12} md={6} sx={{ display: 'flex', alignItems: 'center' }}>
              <Divider sx={{ flexGrow: 1, display: { xs: 'none', md: 'block' } }} />
              <Typography 
                variant="body2" 
                color="text.secondary" 
                sx={{ 
                  mx: 2,
                  my: { xs: 2, md: 0 },
                  display: 'flex',
                  width: { xs: '100%', md: 'auto' },
                  justifyContent: 'center',
                  alignItems: 'center',
                  '&::before': {
                    content: '""',
                    flexGrow: 1,
                    height: '1px',
                    bgcolor: 'divider',
                    mr: 1,
                    display: { xs: 'block', md: 'none' }
                  },
                  '&::after': {
                    content: '""',
                    flexGrow: 1,
                    height: '1px',
                    bgcolor: 'divider',
                    ml: 1,
                    display: { xs: 'block', md: 'none' }
                  }
                }}
              >
                OR
              </Typography>
              <Divider sx={{ flexGrow: 1, display: { xs: 'none', md: 'block' } }} />
            </GridItem>
          </Box>

          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
            <GridItem item xs={12} md={6}>
              <TextField
                margin="normal"
                required
                fullWidth
                id="firstName"
                label="First Name"
                name="firstName"
                autoComplete="given-name"
                value={formData.firstName}
                onChange={handleChange}
                error={!!formErrors.firstName}
                helperText={formErrors.firstName}
                InputProps={{ sx: { borderRadius: 1 } }}
              />
            </GridItem>
            
            <GridItem item xs={12} md={6}>
              <TextField
                margin="normal"
                required
                fullWidth
                id="lastName"
                label="Last Name"
                name="lastName"
                autoComplete="family-name"
                value={formData.lastName}
                onChange={handleChange}
                error={!!formErrors.lastName}
                helperText={formErrors.lastName}
                InputProps={{ sx: { borderRadius: 1 } }}
              />
            </GridItem>
            
            <GridItem item xs={12}>
              <TextField
                margin="normal"
                required
                fullWidth
                id="email"
                label="Email Address"
                name="email"
                autoComplete="email"
                value={formData.email}
                onChange={handleChange}
                error={!!formErrors.email}
                helperText={formErrors.email}
                InputProps={{ sx: { borderRadius: 1 } }}
              />
            </GridItem>
            
            <GridItem item xs={12} md={6}>
              <TextField
                margin="normal"
                required
                fullWidth
                name="password"
                label="Password"
                type={showPassword ? 'text' : 'password'}
                id="password"
                autoComplete="new-password"
                value={formData.password}
                onChange={handleChange}
                error={!!formErrors.password}
                helperText={formErrors.password}
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        aria-label="toggle password visibility"
                        onClick={() => setShowPassword(!showPassword)}
                        edge="end"
                      >
                        {showPassword ? <VisibilityOffIcon /> : <VisibilityIcon />}
                      </IconButton>
                    </InputAdornment>
                  ),
                  sx: { borderRadius: 1 }
                }}
              />
            </GridItem>
            
            <GridItem item xs={12} md={6}>
              <TextField
                margin="normal"
                required
                fullWidth
                name="confirmPassword"
                label="Confirm Password"
                type={showConfirmPassword ? 'text' : 'password'}
                id="confirmPassword"
                autoComplete="new-password"
                value={formData.confirmPassword}
                onChange={handleChange}
                error={!!formErrors.confirmPassword}
                helperText={formErrors.confirmPassword}
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        aria-label="toggle confirm password visibility"
                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                        edge="end"
                      >
                        {showConfirmPassword ? <VisibilityOffIcon /> : <VisibilityIcon />}
                      </IconButton>
                    </InputAdornment>
                  ),
                  sx: { borderRadius: 1 }
                }}
              />
            </GridItem>
            
            <GridItem item xs={12} md={6}>
              <FormControl 
                fullWidth 
                margin="normal" 
                error={!!formErrors.gender}
                sx={{ borderRadius: 1 }}
              >
                <InputLabel id="gender-label">Gender</InputLabel>
                <Select
                  labelId="gender-label"
                  id="gender"
                  name="gender"
                  value={formData.gender}
                  label="Gender"
                  onChange={handleSelectChange}
                  sx={{ borderRadius: 1 }}
                >
                  <MenuItem value="MALE">Male</MenuItem>
                  <MenuItem value="FEMALE">Female</MenuItem>
                  <MenuItem value="OTHER">Other</MenuItem>
                  <MenuItem value="PREFER_NOT_TO_SAY">Prefer not to say</MenuItem>
                </Select>
                {formErrors.gender && <FormHelperText>{formErrors.gender}</FormHelperText>}
              </FormControl>
            </GridItem>
            
            <GridItem item xs={12} md={6}>
              <TextField
                margin="normal"
                fullWidth
                id="phone"
                label="Phone Number"
                name="phone"
                autoComplete="tel"
                value={formData.phone}
                onChange={handleChange}
                error={!!formErrors.phone}
                helperText={formErrors.phone}
                InputProps={{ sx: { borderRadius: 1 } }}
              />
            </GridItem>
            
            <GridItem item xs={12}>
              <TextField
                margin="normal"
                fullWidth
                id="address"
                label="Address"
                name="address"
                autoComplete="street-address"
                value={formData.address}
                onChange={handleChange}
                error={!!formErrors.address}
                helperText={formErrors.address}
                multiline
                rows={2}
                InputProps={{ sx: { borderRadius: 1 } }}
              />
            </GridItem>
          </Box>
          
          <Button
            type="submit"
            fullWidth
            variant="contained"
            size="large"
            sx={{ 
              mt: 3, 
              mb: 2, 
              py: 1.2,
              borderRadius: 1,
              fontWeight: 'bold'
            }}
            disabled={loading}
          >
            {loading ? <CircularProgress size={24} /> : 'Create Account'}
          </Button>
          
          <Box sx={{ textAlign: 'center', mt: 2 }}>
            <Typography variant="body2">
              Already have an account?{' '}
              <Link component={RouterLink} to="/login" variant="body2" sx={{ fontWeight: 'bold' }}>
                Sign In
              </Link>
            </Typography>
          </Box>
        </Box>
      </Paper>
    </Container>
  );
};

export default Register; 