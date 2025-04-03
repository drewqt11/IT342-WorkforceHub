import api from './api';
import { Employee, Department, JobTitle, Role } from '../types/employee';

const EMPLOYEE_ENDPOINTS = {
  EMPLOYEES: '/employees',
  DEPARTMENTS: '/departments',
  JOB_TITLES: '/jobs',
  ROLES: '/roles'
};

class EmployeeService {
  // Employee operations
  async getAllEmployees(): Promise<Employee[]> {
    const response = await api.get<Employee[]>(EMPLOYEE_ENDPOINTS.EMPLOYEES);
    return response.data;
  }

  async getEmployeeById(id: string): Promise<Employee> {
    const response = await api.get<Employee>(`${EMPLOYEE_ENDPOINTS.EMPLOYEES}/${id}`);
    return response.data;
  }

  async createEmployee(employee: Omit<Employee, 'employeeId'>): Promise<Employee> {
    const response = await api.post<Employee>(EMPLOYEE_ENDPOINTS.EMPLOYEES, employee);
    return response.data;
  }

  async updateEmployee(id: string, employee: Partial<Employee>): Promise<Employee> {
    const response = await api.put<Employee>(`${EMPLOYEE_ENDPOINTS.EMPLOYEES}/${id}`, employee);
    return response.data;
  }

  async deleteEmployee(id: string): Promise<void> {
    await api.delete(`${EMPLOYEE_ENDPOINTS.EMPLOYEES}/${id}`);
  }

  // Department operations
  async getAllDepartments(): Promise<Department[]> {
    const response = await api.get<Department[]>(EMPLOYEE_ENDPOINTS.DEPARTMENTS);
    return response.data;
  }

  async getDepartmentById(id: string): Promise<Department> {
    const response = await api.get<Department>(`${EMPLOYEE_ENDPOINTS.DEPARTMENTS}/${id}`);
    return response.data;
  }

  // Job title operations
  async getAllJobTitles(): Promise<JobTitle[]> {
    const response = await api.get<JobTitle[]>(EMPLOYEE_ENDPOINTS.JOB_TITLES);
    return response.data;
  }

  async getJobTitleById(id: string): Promise<JobTitle> {
    const response = await api.get<JobTitle>(`${EMPLOYEE_ENDPOINTS.JOB_TITLES}/${id}`);
    return response.data;
  }

  // Role operations
  async getAllRoles(): Promise<Role[]> {
    const response = await api.get<Role[]>(EMPLOYEE_ENDPOINTS.ROLES);
    return response.data;
  }

  async getRoleById(id: string): Promise<Role> {
    const response = await api.get<Role>(`${EMPLOYEE_ENDPOINTS.ROLES}/${id}`);
    return response.data;
  }
}

export default new EmployeeService(); 