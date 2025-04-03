// Auth types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  gender?: string;
  phone?: string;
  address?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
}

// User types
export interface User {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

// Employee types
export interface Employee {
  employeeId: string;
  firstName: string;
  lastName: string;
  email: string;
  gender?: string;
  hireDate?: string;
  birthDate?: string;
  address?: string;
  phone?: string;
  status: string;
  roleId?: string;
  roleName?: string;
  jobId?: string;
  jobName?: string;
  departmentId?: string;
  departmentName?: string;
}

// Department types
export interface Department {
  departmentId: string;
  departmentName: string;
}

// Job Title types
export interface JobTitle {
  jobId: string;
  jobName: string;
  jobDescription?: string;
  payGrade?: string;
}

export interface Job {
  id: string;
  title: string;
  description: string;
  department?: string;
  departmentId?: string;
  salary?: number;
  requirements?: string;
  responsibilities?: string;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
} 