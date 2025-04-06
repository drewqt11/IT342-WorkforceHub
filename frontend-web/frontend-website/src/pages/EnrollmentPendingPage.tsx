import React from 'react';
import { Button } from '../components/ui/Button';
import { useNavigate } from 'react-router-dom';
import Logo from '../assets/logo.svg';

const EnrollmentPendingPage = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <img
          className="mx-auto h-12 w-auto"
          src={Logo}
          alt="WorkforceHub Logo"
        />
        <h2 className="mt-6 text-center text-3xl font-bold tracking-tight text-gray-800">
          Enrollment Pending
        </h2>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10 text-center">
          <div className="flex justify-center mb-6">
            <svg
              className="w-16 h-16 text-primary-500"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          </div>
          
          <h3 className="text-xl font-medium text-gray-800 mb-2">
            Thank you for enrolling!
          </h3>
          
          <p className="text-gray-500 mb-6">
            Your enrollment request has been submitted and is pending approval from HR.
            You will receive an email notification once your account is approved.
          </p>
          
          <Button
            onClick={() => navigate('/login')}
            fullWidth={true}
          >
            Return to Login
          </Button>
        </div>
      </div>
    </div>
  );
};

export default EnrollmentPendingPage; 