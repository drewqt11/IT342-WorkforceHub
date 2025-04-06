import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Input } from '../components/ui/Input';
import { Button } from '../components/ui/Button';
import { useAuth } from '../hooks/useAuth';
import Logo from '../assets/logo.svg';

// Form validation schema
const loginSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

const LoginPage = () => {
  const navigate = useNavigate();
  const { login, loginWithMicrosoft } = useAuth();
  const [authError, setAuthError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isMicrosoftLoading, setIsMicrosoftLoading] = useState(false);
  
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });
  
  const onSubmit = async (data: LoginFormValues) => {
    setIsLoading(true);
    setAuthError(null);
    
    try {
      await login(data.email, data.password);
      navigate('/dashboard');
    } catch (error: any) {
      console.error('Login failed:', error);
      if (error.response?.status === 401) {
        setAuthError('Invalid email or password');
      } else if (error.response?.status === 404) {
        setAuthError('Account not found. Please sign up with Microsoft first.');
        setTimeout(() => {
          navigate('/signup');
        }, 3000);
      } else {
        setAuthError('An error occurred. Please try again later.');
      }
    } finally {
      setIsLoading(false);
    }
  };
  
  const handleMicrosoftLogin = () => {
    setIsMicrosoftLoading(true);
    setAuthError(null);
    try {
      // Display a message to the user
      console.log("Redirecting to Microsoft login...");
      // Add a small delay to show loading state
      setTimeout(() => {
        loginWithMicrosoft();
      }, 500);
    } catch (error) {
      console.error('Error initiating Microsoft login:', error);
      setAuthError('Failed to connect to Microsoft. Please try again.');
      setIsMicrosoftLoading(false);
    }
  };
  
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <img
          className="mx-auto h-12 w-auto"
          src={Logo}
          alt="WorkforceHub Logo"
        />
        <h2 className="mt-6 text-center text-3xl font-bold tracking-tight text-gray-800">
          Sign in to your account
        </h2>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          {authError && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
              {authError}
            </div>
          )}
          
          <form className="space-y-6" onSubmit={handleSubmit(onSubmit)}>
            <Input
              label="Email address"
              type="email"
              id="email"
              autoComplete="email"
              error={errors.email?.message}
              {...register('email')}
            />

            <Input
              label="Password"
              type="password"
              id="password"
              autoComplete="current-password"
              error={errors.password?.message}
              {...register('password')}
            />

            <div>
              <Button
                type="submit"
                fullWidth={true}
                isLoading={isLoading}
              >
                Sign in
              </Button>
            </div>
          </form>

          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-200" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="bg-white px-2 text-gray-500">
                  Or continue with
                </span>
              </div>
            </div>

            <div className="mt-6">
              <Button
                type="button"
                fullWidth={true}
                variant="outline"
                onClick={handleMicrosoftLogin}
                isLoading={isMicrosoftLoading}
              >
                {!isMicrosoftLoading && (
                  <svg
                    className="w-5 h-5 mr-2"
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 23 23"
                  >
                    <path fill="#f3f3f3" d="M0 0h23v23H0z" />
                    <path fill="#f35325" d="M1 1h10v10H1z" />
                    <path fill="#81bc06" d="M12 1h10v10H12z" />
                    <path fill="#05a6f0" d="M1 12h10v10H1z" />
                    <path fill="#ffba08" d="M12 12h10v10H12z" />
                  </svg>
                )}
                {isMicrosoftLoading ? 'Connecting to Microsoft...' : 'Continue with Microsoft'}
              </Button>
            </div>
          </div>
          
          <div className="mt-6 text-center">
            <p className="text-sm text-gray-500">
              Don't have an account?{' '}
              <a
                href="/signup"
                className="font-medium text-primary-500 hover:text-primary-700"
              >
                Sign up
              </a>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage; 