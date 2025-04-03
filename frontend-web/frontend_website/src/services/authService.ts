import api from './api';
import { LoginCredentials, RegisterData, AuthResponse } from '../types/auth';

const AUTH_ENDPOINTS = {
  LOGIN: '/auth/login',
  REGISTER: '/auth/register',
  LOGOUT: '/auth/logout',
};

class AuthService {
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>(AUTH_ENDPOINTS.LOGIN, credentials);
    
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    
    return response.data;
  }

  async register(data: RegisterData): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>(AUTH_ENDPOINTS.REGISTER, data);
    
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    
    return response.data;
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  getCurrentUser(): AuthResponse | null {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      return JSON.parse(userStr);
    }
    return null;
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('token');
  }
}

export default new AuthService();
