import api from './api';

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface SignupData {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: string;
  };
}

const AuthService = {
  // Login with email and password
  login: async (credentials: LoginCredentials): Promise<AuthResponse> => {
    const response = await api.post('/api/auth/login', credentials);
    const data = response.data;
    
    // Store tokens in localStorage
    localStorage.setItem('auth_token', data.accessToken);
    localStorage.setItem('refresh_token', data.refreshToken);
    
    return data;
  },
  
  // Register a new user
  signup: async (userData: SignupData): Promise<AuthResponse> => {
    const response = await api.post('/api/auth/signup', userData);
    return response.data;
  },
  
  // Logout user
  logout: () => {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('refresh_token');
    window.location.href = '/login';
  },
  
  // Get current user info
  getCurrentUser: async () => {
    const response = await api.get('/api/auth/user');
    return response.data;
  },
  
  // Check if user is authenticated
  isAuthenticated: () => {
    return !!localStorage.getItem('auth_token');
  },
  
  // Handle Microsoft OAuth
  getMicrosoftOAuthUrl: () => {
    const baseUrl = 'http://localhost:8080/oauth2/authorization/microsoft';
    const params = new URLSearchParams({
      prompt: 'select_account',
      response_mode: 'query'
    });
    
    return `${baseUrl}?${params.toString()}`;
  },
  
  // Process OAuth redirect with code
  handleOAuthRedirect: async (code: string, state: string) => {
    const response = await api.get(`/api/auth/oauth2/callback?code=${code}&state=${state}`);
    const data = response.data;
    
    // Store tokens in localStorage
    localStorage.setItem('auth_token', data.accessToken);
    localStorage.setItem('refresh_token', data.refreshToken);
    
    return data;
  }
};

export default AuthService; 