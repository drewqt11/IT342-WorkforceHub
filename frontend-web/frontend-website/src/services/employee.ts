import api from './api';

export interface Employee {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  address?: string;
  departmentId?: number;
  departmentName?: string;
  jobTitleId?: number;
  jobTitleName?: string;
  roleId?: number;
  roleName?: string;
  startDate?: string;
  status: 'ACTIVE' | 'INACTIVE';
  dateOfBirth?: string;
  gender?: string;
  emergencyContact?: string;
  emergencyPhone?: string;
}

export interface EmployeeFormData {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  address?: string;
  departmentId?: number;
  jobTitleId?: number;
  roleId?: number;
  startDate?: string;
  dateOfBirth?: string;
  gender?: string;
  emergencyContact?: string;
  emergencyPhone?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface Department {
  id: number;
  name: string;
  description?: string;
}

export interface JobTitle {
  id: number;
  name: string;
  description?: string;
  departmentId?: number;
}

export interface Role {
  id: number;
  name: string;
  description?: string;
}

export interface Certification {
  id: number;
  employeeId: number;
  name: string;
  issuer: string;
  issueDate: string;
  expiryDate?: string;
  documentUrl?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
}

export interface CertificationFormData {
  name: string;
  issuer: string;
  issueDate: string;
  expiryDate?: string;
  document?: File;
}

const EmployeeService = {
  // Employee CRUD operations
  getAllEmployees: async (page = 0, size = 10, search = ''): Promise<PaginatedResponse<Employee>> => {
    const response = await api.get('/api/employees', { 
      params: { page, size, search } 
    });
    return response.data;
  },
  
  getEmployeeById: async (id: number): Promise<Employee> => {
    const response = await api.get(`/api/employees/${id}`);
    return response.data;
  },
  
  createEmployee: async (employeeData: EmployeeFormData): Promise<Employee> => {
    const response = await api.post('/api/employees', employeeData);
    return response.data;
  },
  
  updateEmployee: async (id: number, employeeData: EmployeeFormData): Promise<Employee> => {
    const response = await api.put(`/api/employees/${id}`, employeeData);
    return response.data;
  },
  
  deleteEmployee: async (id: number): Promise<void> => {
    await api.delete(`/api/employees/${id}`);
  },
  
  // Department operations
  getAllDepartments: async (): Promise<Department[]> => {
    const response = await api.get('/api/departments');
    return response.data;
  },
  
  createDepartment: async (data: { name: string, description?: string }): Promise<Department> => {
    const response = await api.post('/api/departments', data);
    return response.data;
  },
  
  updateDepartment: async (id: number, data: { name: string, description?: string }): Promise<Department> => {
    const response = await api.put(`/api/departments/${id}`, data);
    return response.data;
  },
  
  deleteDepartment: async (id: number): Promise<void> => {
    await api.delete(`/api/departments/${id}`);
  },
  
  // Job title operations
  getAllJobTitles: async (): Promise<JobTitle[]> => {
    const response = await api.get('/api/job-titles');
    return response.data;
  },
  
  createJobTitle: async (data: { name: string, description?: string, departmentId?: number }): Promise<JobTitle> => {
    const response = await api.post('/api/job-titles', data);
    return response.data;
  },
  
  updateJobTitle: async (id: number, data: { name: string, description?: string, departmentId?: number }): Promise<JobTitle> => {
    const response = await api.put(`/api/job-titles/${id}`, data);
    return response.data;
  },
  
  deleteJobTitle: async (id: number): Promise<void> => {
    await api.delete(`/api/job-titles/${id}`);
  },
  
  // Role operations
  getAllRoles: async (): Promise<Role[]> => {
    const response = await api.get('/api/roles');
    return response.data;
  },
  
  // Certification operations
  getEmployeeCertifications: async (employeeId: number): Promise<Certification[]> => {
    const response = await api.get(`/api/employees/${employeeId}/certifications`);
    return response.data;
  },
  
  addCertification: async (employeeId: number, data: CertificationFormData): Promise<Certification> => {
    const formData = new FormData();
    formData.append('name', data.name);
    formData.append('issuer', data.issuer);
    formData.append('issueDate', data.issueDate);
    
    if (data.expiryDate) {
      formData.append('expiryDate', data.expiryDate);
    }
    
    if (data.document) {
      formData.append('document', data.document);
    }
    
    const response = await api.post(`/api/employees/${employeeId}/certifications`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    
    return response.data;
  },
  
  updateCertification: async (employeeId: number, certificationId: number, data: CertificationFormData): Promise<Certification> => {
    const formData = new FormData();
    formData.append('name', data.name);
    formData.append('issuer', data.issuer);
    formData.append('issueDate', data.issueDate);
    
    if (data.expiryDate) {
      formData.append('expiryDate', data.expiryDate);
    }
    
    if (data.document) {
      formData.append('document', data.document);
    }
    
    const response = await api.put(`/api/employees/${employeeId}/certifications/${certificationId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    
    return response.data;
  },
  
  deleteCertification: async (employeeId: number, certificationId: number): Promise<void> => {
    await api.delete(`/api/employees/${employeeId}/certifications/${certificationId}`);
  },
  
  approveCertification: async (employeeId: number, certificationId: number): Promise<Certification> => {
    const response = await api.post(`/api/employees/${employeeId}/certifications/${certificationId}/approve`);
    return response.data;
  },
  
  rejectCertification: async (employeeId: number, certificationId: number): Promise<Certification> => {
    const response = await api.post(`/api/employees/${employeeId}/certifications/${certificationId}/reject`);
    return response.data;
  },
};

export default EmployeeService; 