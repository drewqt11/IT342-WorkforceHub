import React, { createContext, useState, useEffect, ReactNode } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import authService from '../../services/authService';
import { AuthContextType, User, LoginCredentials, RegisterData } from '../../types/auth';

// Create a context with a default value
export const AuthContext = createContext<AuthContextType>({
  user: null,
  isAuthenticated: false,
  isLoading: true,
  login: async () => {},
  register: async () => {},
  logout: () => {},
  isError: false,
  errorMessage: '',
  handleOAuthLogin: () => {},
  setUser: () => {},
  setIsLoading: () => {},
});

type AuthProviderProps = {
  children: ReactNode;
};

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isError, setIsError] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();
  const location = useLocation();

  // Function to get current user from local storage
  const getCurrentUserFromLocalStorage = () => {
    try {
      const currentUser = authService.getCurrentUser();
      if (currentUser) {
        setUser({
          userId: currentUser.userId,
          email: currentUser.emailAddress,
          role: currentUser.role,
          employeeId: currentUser.employeeId,
          firstName: currentUser.firstName,
          lastName: currentUser.lastName,
        });
      }
    } catch (error) {
      console.error('Failed to initialize auth:', error);
      authService.logout();
    } finally {
      setIsLoading(false);
    }
  };

  // Initialize auth on component mount
  useEffect(() => {
    getCurrentUserFromLocalStorage();
  }, []);

  // Process OAuth callback on navigation
  useEffect(() => {
    // Check if this is a redirect from OAuth
    const searchParams = new URLSearchParams(location.search);
    const token = searchParams.get('token');
    const error = searchParams.get('error');

    if (token) {
      // Store the token and remove from URL
      localStorage.setItem('token', token);
      
      // If user information is also passed in the URL
      const userInfo = searchParams.get('user');
      if (userInfo) {
        try {
          const userObj = JSON.parse(decodeURIComponent(userInfo));
          localStorage.setItem('user', JSON.stringify(userObj));
          setUser({
            userId: userObj.userId,
            email: userObj.emailAddress,
            role: userObj.role,
            employeeId: userObj.employeeId,
            firstName: userObj.firstName,
            lastName: userObj.lastName,
          });
        } catch (e) {
          console.error('Failed to parse user info:', e);
        }
      } else {
        // Alternatively, fetch user info from API using the token
        getCurrentUserFromLocalStorage();
      }
      
      // Clean URL parameters and redirect
      navigate('/dashboard', { replace: true });
    } else if (error) {
      setIsError(true);
      setErrorMessage(decodeURIComponent(error));
      navigate('/login', { replace: true });
    }
  }, [location, navigate]);

  const login = async (credentials: LoginCredentials): Promise<void> => {
    setIsLoading(true);
    setIsError(false);
    setErrorMessage('');
    
    try {
      const response = await authService.login(credentials);
      setUser({
        userId: response.userId,
        email: response.emailAddress,
        role: response.role,
        employeeId: response.employeeId,
        firstName: response.firstName,
        lastName: response.lastName,
      });
    } catch (err: any) {
      const message = err.response?.data?.message || 'Failed to login';
      setIsError(true);
      setErrorMessage(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (data: RegisterData): Promise<void> => {
    setIsLoading(true);
    setIsError(false);
    setErrorMessage('');
    
    try {
      const response = await authService.register(data);
      setUser({
        userId: response.userId,
        email: response.emailAddress,
        role: response.role,
        employeeId: response.employeeId,
        firstName: response.firstName,
        lastName: response.lastName,
      });
    } catch (err: any) {
      const message = err.response?.data?.message || 'Failed to register';
      setIsError(true);
      setErrorMessage(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    navigate('/login');
  };

  // Method to handle OAuth logins
  const handleOAuthLogin = () => {
    setIsLoading(true);
    setIsError(false);
    setErrorMessage('');
    
    // The actual redirection happens in the LoginForm component
    // This method is provided for consistency with the context API
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        register,
        logout,
        isError,
        errorMessage,
        handleOAuthLogin,
        setUser,
        setIsLoading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

// Custom hook to use the auth context
export const useAuth = () => {
  const context = React.useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}; 