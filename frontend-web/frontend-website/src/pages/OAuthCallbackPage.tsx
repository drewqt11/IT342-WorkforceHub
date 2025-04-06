import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import AuthService from '../services/auth';
import Logo from '../assets/logo.svg';

const OAuthCallbackPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState<string | null>(null);
  const [isProcessing, setIsProcessing] = useState(true);

  useEffect(() => {
    const processOAuthRedirect = async () => {
      try {
        // Parse query parameters
        const params = new URLSearchParams(location.search);
        const code = params.get('code');
        const state = params.get('state');
        const error = params.get('error');

        // Check for errors
        if (error) {
          console.error('OAuth error:', error);
          setError(`Authentication failed: ${error}`);
          setIsProcessing(false);
          return;
        }

        // Validate code and state
        if (!code || !state) {
          setError('Missing authentication parameters');
          setIsProcessing(false);
          return;
        }

        // Verify state matches what we sent (CSRF protection)
        const storedState = sessionStorage.getItem('microsoft_oauth_state');
        if (storedState && storedState !== state) {
          setError('Invalid authentication state');
          setIsProcessing(false);
          return;
        }

        // Process the OAuth callback
        await AuthService.handleOAuthRedirect(code, state);
        
        // Calculate login duration for analytics
        const startTime = sessionStorage.getItem('microsoft_oauth_start');
        if (startTime) {
          const duration = Date.now() - parseInt(startTime);
          console.log(`Microsoft OAuth login took ${duration}ms`);
        }
        
        // Clean up storage
        sessionStorage.removeItem('microsoft_oauth_state');
        sessionStorage.removeItem('microsoft_oauth_start');
        
        // Redirect to dashboard
        navigate('/dashboard');
      } catch (err) {
        console.error('Error processing OAuth callback:', err);
        setError('Failed to complete authentication. Please try again.');
        setIsProcessing(false);
      }
    };

    processOAuthRedirect();
  }, [location, navigate]);

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
        <div className="sm:mx-auto sm:w-full sm:max-w-md">
          <img
            className="mx-auto h-12 w-auto"
            src={Logo}
            alt="WorkforceHub Logo"
          />
          <h2 className="mt-6 text-center text-3xl font-bold tracking-tight text-gray-800">
            Authentication Error
          </h2>
        </div>

        <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
          <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10 text-center">
            <div className="flex justify-center mb-6">
              <svg
                className="w-16 h-16 text-red-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            
            <h3 className="text-xl font-medium text-gray-800 mb-2">
              Authentication Failed
            </h3>
            
            <p className="text-gray-500 mb-6">
              {error}
            </p>
            
            <div className="flex space-x-4 justify-center">
              <button
                onClick={() => navigate('/login')}
                className="bg-primary-500 hover:bg-primary-700 text-white px-4 py-2 rounded"
              >
                Return to Login
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <img
          className="mx-auto h-12 w-auto"
          src={Logo}
          alt="WorkforceHub Logo"
        />
        <h2 className="mt-6 text-center text-3xl font-bold tracking-tight text-gray-800">
          Completing Sign In
        </h2>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10 text-center">
          <div className="flex justify-center mb-6">
            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-500"></div>
          </div>
          
          <p className="text-gray-500 mb-6">
            Please wait while we complete your sign in...
          </p>
        </div>
      </div>
    </div>
  );
};

export default OAuthCallbackPage; 