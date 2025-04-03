export interface Employee {
  employeeId: string;
  firstName: string;
  lastName: string;
  email: string;
  gender?: string;
  hireDate: string;
  dateOfBirth?: string;
  address?: string;
  phoneNumber?: string;
  maritalStatus?: string;
  status: string;
  departmentId?: string;
  departmentName?: string;
  jobId?: string;
  jobName?: string;
  roleId: string;
  roleName: string;
}

export interface Department {
  departmentId: string;
  departmentName: string;
}

export interface JobTitle {
  jobId: string;
  jobName: string;
  jobDescription?: string;
  payGrade?: string;
}

export interface Role {
  roleId: string;
  roleName: string;
} 