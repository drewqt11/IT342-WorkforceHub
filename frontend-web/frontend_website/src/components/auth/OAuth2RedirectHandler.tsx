import React, { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';

const OAuth2RedirectHandler: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { setUser, setIsLoading } = useAuth();

  useEffect(() => {
    const handleOAuthRedirect = () => {
      setIsLoading(true);
      const searchParams = new URLSearchParams(location.search);
      const token = searchParams.get('token');
      const error = searchParams.get('error');

      if (error) {
        console.error('OAuth Login Error:', error);
        navigate('/login', { replace: true });
        return;
      }

      if (!token) {
        console.error('No token received from OAuth provider');
        navigate('/login', { replace: true });
        return;
      }

      // Store token in localStorage
      localStorage.setItem('token', token);

      // Extract and store user information
      const userId = searchParams.get('userId');
      const email = searchParams.get('email');
      const role = searchParams.get('role');
      const employeeId = searchParams.get('employeeId') || undefined; // Convert null to undefined
      const firstName = searchParams.get('firstName');
      const lastName = searchParams.get('lastName');

      if (userId && email) {
        const userInfo = {
          userId,
          emailAddress: email,
          role: role || 'ROLE_EMPLOYEE',
          employeeId, // Now correctly typed as string | undefined
          firstName: firstName || '',
          lastName: lastName || '',
        };

        localStorage.setItem('user', JSON.stringify(userInfo));

        // Update AuthContext user state
        setUser({
          userId,
          email,
          role: role || 'ROLE_EMPLOYEE',
          employeeId, // Now correctly typed as string | undefined
          firstName: firstName || '',
          lastName: lastName || '',
        });
      }

      // Redirect to landing page instead of dashboard
      setIsLoading(false);
      navigate('/landing', { replace: true });
    };

    handleOAuthRedirect();
  }, [location, navigate, setUser, setIsLoading]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full space-y-8 p-10 bg-white rounded-xl shadow-md">
        <div className="text-center">
          <h2 className="mt-6 text-xl font-semibold text-gray-900">
            Processing login...
          </h2>
          <div className="mt-4">
            <div className="w-8 h-8 border-t-2 border-b-2 border-blue-500 rounded-full animate-spin mx-auto"></div>
          </div>
          <p className="mt-4 text-sm text-gray-600">
            Please wait while we complete your authentication.
          </p>
        </div>
      </div>
    </div>
  );
};

export default OAuth2RedirectHandler; 