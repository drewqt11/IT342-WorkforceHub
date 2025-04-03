import React, { createContext, useState, useContext, useEffect, ReactNode } from 'react';
import api, { login as apiLogin, register as apiRegister } from '../services/api';

interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  [key: string]: any;
}

interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
}

interface LoginData {
  email: string;
  password: string;
}

interface RegisterData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  gender?: string;
  phone?: string;
  address?: string;
}

export interface AuthContextType {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
  login: (data: LoginData) => Promise<boolean>;
  register: (data: RegisterData) => Promise<boolean>;
  logout: () => void;
  error: string | null;
  clearError: () => void;
  setAuthState: (state: AuthState) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [authState, setAuthState] = useState<AuthState>({
    isAuthenticated: false,
    user: null,
    token: null,
  });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is already logged in
    const token = localStorage.getItem('token');
    
    if (token) {
      // Set token in axios defaults
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      
      // Fetch current user info
      api.get('/employees/profile')
        .then(response => {
          setAuthState({
            isAuthenticated: true,
            user: response.data,
            token
          });
        })
        .catch(() => {
          // Token might be invalid or expired
          localStorage.removeItem('token');
          delete api.defaults.headers.common['Authorization'];
        })
        .finally(() => {
          setLoading(false);
        });
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (data: LoginData): Promise<boolean> => {
    try {
      setError(null);
      const response = await apiLogin(data.email, data.password);
      
      if (response.token) {
        localStorage.setItem('token', response.token);
        api.defaults.headers.common['Authorization'] = `Bearer ${response.token}`;
        
        setAuthState({
          isAuthenticated: true,
          user: response.user,
          token: response.token
        });
        
        return true;
      }
      
      return false;
    } catch (err) {
      const errorMessage = err instanceof Error 
        ? err.message 
        : 'Failed to login. Please try again.';
      
      setError(errorMessage);
      return false;
    }
  };

  const register = async (data: RegisterData): Promise<boolean> => {
    try {
      setError(null);
      await apiRegister(data);
      return true;
    } catch (err) {
      const errorMessage = err instanceof Error 
        ? err.message 
        : 'Failed to register. Please try again.';
      
      setError(errorMessage);
      return false;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    delete api.defaults.headers.common['Authorization'];
    
    setAuthState({
      isAuthenticated: false,
      user: null,
      token: null,
    });
  };

  const clearError = () => {
    setError(null);
  };

  const contextValue: AuthContextType = {
    isAuthenticated: authState.isAuthenticated,
    user: authState.user,
    token: authState.token,
    login,
    register,
    logout,
    error,
    clearError,
    setAuthState,
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  
  return context;
}; 