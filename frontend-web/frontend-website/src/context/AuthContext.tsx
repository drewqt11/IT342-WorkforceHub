import React, { createContext, useState, useEffect, ReactNode, useCallback } from 'react';
import AuthService from '../services/auth';

interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  loginWithMicrosoft: () => void;
}

// Create context with default values
export const AuthContext = createContext<AuthContextType>({
  user: null,
  isLoading: true,
  isAuthenticated: false,
  login: async () => {},
  logout: () => {},
  loginWithMicrosoft: () => {},
});

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  // Check if user is already logged in on initial load
  useEffect(() => {
    const checkAuthStatus = async () => {
      setIsLoading(true);
      try {
        if (AuthService.isAuthenticated()) {
          const userData = await AuthService.getCurrentUser();
          setUser(userData);
          setIsAuthenticated(true);
        }
      } catch (error) {
        console.error('Failed to get current user:', error);
        AuthService.logout();
      } finally {
        setIsLoading(false);
      }
    };

    checkAuthStatus();
  }, []);

  // Login with email and password
  const login = async (email: string, password: string) => {
    setIsLoading(true);
    try {
      const response = await AuthService.login({ email, password });
      setUser(response.user);
      setIsAuthenticated(true);
    } finally {
      setIsLoading(false);
    }
  };

  // Logout user
  const logout = () => {
    AuthService.logout();
    setUser(null);
    setIsAuthenticated(false);
  };

  // Login with Microsoft
  const loginWithMicrosoft = useCallback(() => {
    try {
      // Generate a random state for CSRF protection
      const state = Math.random().toString(36).substring(2, 15);
      
      // Store state in sessionStorage for verification upon return
      sessionStorage.setItem('microsoft_oauth_state', state);
      
      // Store timestamp to track login duration
      sessionStorage.setItem('microsoft_oauth_start', Date.now().toString());
      
      // Get the OAuth URL and redirect the user
      const oauthUrl = AuthService.getMicrosoftOAuthUrl();
      
      // Add our state parameter to the URL
      const redirectUrl = oauthUrl.includes('?') 
        ? `${oauthUrl}&state=${state}&redirect_uri=${window.location.origin}/oauth/callback` 
        : `${oauthUrl}?state=${state}&redirect_uri=${window.location.origin}/oauth/callback`;
      
      window.location.href = redirectUrl;
    } catch (error) {
      console.error('Failed to initiate Microsoft login:', error);
    }
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated,
        login,
        logout,
        loginWithMicrosoft,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}; 