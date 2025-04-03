import axios from 'axios';
import { Employee, Department, Job } from '../types';

export const API_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add auth token to requests if available
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Types for auth
interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  [key: string]: any;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface RegisterData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  gender?: string;
  phone?: string;
  address?: string;
}

// Auth API calls
export const login = async (email: string, password: string): Promise<AuthResponse> => {
  try {
    const response = await api.post('/auth/login', { email, password });
    
    // Extract token and user data from response
    return {
      token: response.data.token,
      user: {
        id: response.data.userId || 'unknown',
        firstName: response.data.firstName || '',
        lastName: response.data.lastName || '',
        email: email,
        role: response.data.role || 'EMPLOYEE'
      }
    };
  } catch (error) {
    throw new Error('Invalid credentials. Please try again.');
  }
};

export const register = async (data: RegisterData): Promise<void> => {
  try {
    await api.post('/auth/register', data);
  } catch (error) {
    throw new Error('Registration failed. Please try again.');
  }
};

// Add this function to handle Google login
export const handleGoogleLogin = async (): Promise<void> => {
  // Redirect to Google OAuth endpoint
  window.location.href = `${API_URL}/oauth2/authorization/google`;
};

// Employee API calls
export const getAllEmployees = async (): Promise<Employee[]> => {
  const response = await api.get<Employee[]>('/employees');
  return response.data;
};

export const getEmployeeById = async (id: string): Promise<Employee> => {
  const response = await api.get<Employee>(`/employees/${id}`);
  return response.data;
};

export const getCurrentEmployeeProfile = async (): Promise<Employee> => {
  const response = await api.get<Employee>('/employees/profile');
  return response.data;
};

export const createEmployee = async (employee: Partial<Employee>): Promise<Employee> => {
  const response = await api.post<Employee>('/employees', employee);
  return response.data;
};

export const updateEmployee = async (id: string, employee: Partial<Employee>): Promise<Employee> => {
  const response = await api.put<Employee>(`/employees/${id}`, employee);
  return response.data;
};

export const deleteEmployee = async (id: string): Promise<void> => {
  await api.delete(`/employees/${id}`);
};

// Department API calls
export const getAllDepartments = async (): Promise<Department[]> => {
  const response = await api.get<Department[]>('/departments');
  return response.data;
};

export const getDepartmentById = async (id: string): Promise<Department> => {
  const response = await api.get<Department>(`/departments/${id}`);
  return response.data;
};

export const createDepartment = async (department: Partial<Department>): Promise<Department> => {
  const response = await api.post<Department>('/departments', department);
  return response.data;
};

export const updateDepartment = async (id: string, department: Partial<Department>): Promise<Department> => {
  const response = await api.put<Department>(`/departments/${id}`, department);
  return response.data;
};

export const deleteDepartment = async (id: string): Promise<void> => {
  await api.delete(`/departments/${id}`);
};

// Job API calls
export const getAllJobs = async (): Promise<Job[]> => {
  const response = await api.get<Job[]>('/jobs');
  return response.data;
};

export const getJobById = async (id: string): Promise<Job> => {
  const response = await api.get<Job>(`/jobs/${id}`);
  return response.data;
};

export const createJob = async (job: Partial<Job>): Promise<Job> => {
  const response = await api.post<Job>('/jobs', job);
  return response.data;
};

export const updateJob = async (id: string, job: Partial<Job>): Promise<Job> => {
  const response = await api.put<Job>(`/jobs/${id}`, job);
  return response.data;
};

export const deleteJob = async (id: string): Promise<void> => {
  await api.delete(`/jobs/${id}`);
};

export default api; 