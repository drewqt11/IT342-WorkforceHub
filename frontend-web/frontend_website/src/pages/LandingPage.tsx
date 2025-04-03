import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../components/auth/AuthContext';

const LandingPage: React.FC = () => {
  const { user, isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      navigate('/login');
    }
  }, [isAuthenticated, isLoading, navigate]);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-700">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      <div className="container mx-auto px-4 py-16">
        <div className="max-w-3xl mx-auto">
          <div className="bg-white rounded-xl shadow-lg overflow-hidden">
            <div className="bg-blue-600 py-6 px-8">
              <h1 className="text-2xl font-bold text-white">Welcome to WorkforceHub!</h1>
            </div>
            
            <div className="p-8">
              <div className="flex items-center mb-8">
                <div className="bg-blue-100 rounded-full p-3">
                  <svg className="h-8 w-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div className="ml-4">
                  <h2 className="text-xl font-semibold text-gray-800">
                    Welcome, {user?.firstName || 'User'}!
                  </h2>
                  <p className="text-gray-600">
                    You have successfully logged in using Google.
                  </p>
                </div>
              </div>

              <div className="bg-gray-50 rounded-lg p-6 mb-8">
                <h3 className="text-lg font-medium text-gray-800 mb-4">Your Account Information</h3>
                <div className="space-y-3">
                  <div className="flex">
                    <span className="w-32 text-gray-500">Email:</span>
                    <span className="font-medium text-gray-800">{user?.email}</span>
                  </div>
                  <div className="flex">
                    <span className="w-32 text-gray-500">Full Name:</span>
                    <span className="font-medium text-gray-800">{user?.firstName} {user?.lastName}</span>
                  </div>
                  <div className="flex">
                    <span className="w-32 text-gray-500">User ID:</span>
                    <span className="font-medium text-gray-800">{user?.userId}</span>
                  </div>
                  <div className="flex">
                    <span className="w-32 text-gray-500">Role:</span>
                    <span className="font-medium text-gray-800">{user?.role}</span>
                  </div>
                  {user?.employeeId && (
                    <div className="flex">
                      <span className="w-32 text-gray-500">Employee ID:</span>
                      <span className="font-medium text-gray-800">{user.employeeId}</span>
                    </div>
                  )}
                </div>
              </div>

              <div className="flex justify-center space-x-4">
                <button
                  onClick={() => navigate('/dashboard')}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                >
                  Go to Dashboard
                </button>
                <button
                  onClick={() => window.open('https://accounts.google.com', '_blank')}
                  className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
                >
                  Manage Google Account
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LandingPage; 