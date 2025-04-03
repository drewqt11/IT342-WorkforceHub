import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../components/auth/AuthContext';
import employeeService from '../services/employeeService';
import { Employee, Department } from '../types/employee';

const Dashboard: React.FC = () => {
  const { user, isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      navigate('/login');
    }
  }, [isAuthenticated, isLoading, navigate]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [employeesData, departmentsData] = await Promise.all([
          employeeService.getAllEmployees(),
          employeeService.getAllDepartments()
        ]);
        setEmployees(employeesData);
        setDepartments(departmentsData);
        setError(null);
      } catch (err: any) {
        console.error('Error fetching dashboard data:', err);
        setError('Failed to load dashboard data. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    if (isAuthenticated) {
      fetchData();
    }
  }, [isAuthenticated]);

  // Calculate some statistics
  const totalEmployees = employees.length;
  const activeEmployees = employees.filter(emp => emp.status === 'ACTIVE').length;
  const departmentCounts = departments.map(dept => {
    const count = employees.filter(emp => emp.departmentId === dept.departmentId).length;
    return { ...dept, count };
  });

  // Get recent employees (hired in the last 30 days)
  const thirtyDaysAgo = new Date();
  thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
  const recentEmployees = employees
    .filter(emp => {
      const hireDate = new Date(emp.hireDate);
      return hireDate >= thirtyDaysAgo;
    })
    .sort((a, b) => new Date(b.hireDate).getTime() - new Date(a.hireDate).getTime())
    .slice(0, 5);

  if (isLoading || loading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-700">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">
          Welcome, {user?.firstName}!
        </h1>
        <p className="text-gray-600">
          Here's what's happening with your workforce today.
        </p>
      </div>

      {error && (
        <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-8">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-red-500" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm text-red-700">{error}</p>
            </div>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="bg-blue-100 rounded-full p-3">
              <svg className="h-8 w-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            </div>
            <div className="ml-4">
              <h2 className="text-lg font-semibold text-gray-700">Total Employees</h2>
              <p className="text-3xl font-bold text-gray-900">{totalEmployees}</p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="bg-green-100 rounded-full p-3">
              <svg className="h-8 w-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div className="ml-4">
              <h2 className="text-lg font-semibold text-gray-700">Active Employees</h2>
              <p className="text-3xl font-bold text-gray-900">{activeEmployees}</p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="bg-purple-100 rounded-full p-3">
              <svg className="h-8 w-8 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
              </svg>
            </div>
            <div className="ml-4">
              <h2 className="text-lg font-semibold text-gray-700">Departments</h2>
              <p className="text-3xl font-bold text-gray-900">{departments.length}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-800">Department Distribution</h3>
          </div>
          <div className="p-6">
            {departmentCounts.length > 0 ? (
              <div className="space-y-4">
                {departmentCounts.map(dept => (
                  <div key={dept.departmentId}>
                    <div className="flex justify-between mb-1">
                      <span className="text-sm font-medium text-gray-700">{dept.departmentName}</span>
                      <span className="text-sm font-medium text-gray-700">{dept.count} employees</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2.5">
                      <div
                        className="bg-blue-600 h-2.5 rounded-full"
                        style={{ width: `${(dept.count / totalEmployees) * 100}%` }}
                      ></div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500 text-center py-4">No departments data available</p>
            )}
          </div>
        </div>

        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-800">Recently Hired Employees</h3>
          </div>
          <div className="divide-y divide-gray-200">
            {recentEmployees.length > 0 ? (
              recentEmployees.map(employee => (
                <div key={employee.employeeId} className="px-6 py-4">
                  <div className="flex items-center">
                    <div className="bg-gray-100 rounded-full h-10 w-10 flex items-center justify-center">
                      <span className="text-gray-700 font-medium">
                        {employee.firstName.charAt(0)}{employee.lastName.charAt(0)}
                      </span>
                    </div>
                    <div className="ml-4">
                      <h4 className="text-sm font-medium text-gray-900">{employee.firstName} {employee.lastName}</h4>
                      <p className="text-sm text-gray-500">{employee.jobName || 'Not assigned'}</p>
                    </div>
                    <div className="ml-auto">
                      <span className="text-xs text-gray-500">
                        Hired {new Date(employee.hireDate).toLocaleDateString()}
                      </span>
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <p className="text-gray-500 text-center py-6">No recent hires</p>
            )}
          </div>
          <div className="px-6 py-4 border-t border-gray-200">
            <button
              onClick={() => navigate('/employees')}
              className="text-sm text-blue-600 hover:text-blue-800 font-medium"
            >
              View all employees →
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard; 