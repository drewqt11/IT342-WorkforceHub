'use client';

import { useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { authService } from '@/lib/auth';

export default function OAuth2Redirect() {
    const router = useRouter();
    const searchParams = useSearchParams();

    useEffect(() => {
        const handleRedirect = async () => {
            try {
                // Get all parameters from the URL
                const token = searchParams.get('token');
                const userId = searchParams.get('userId');
                const email = searchParams.get('email');
                const role = searchParams.get('role');
                const employeeId = searchParams.get('employeeId');
                const firstName = searchParams.get('firstName');
                const lastName = searchParams.get('lastName');

                if (!token || !email) {
                    // Clear all cookies and redirect to home page
                    authService.clearTokens();
                    router.push('/');
                    throw new Error('Missing required parameters');
                }

                // Store the token in both cookies and localStorage
                document.cookie = `token=${token}; path=/; secure; samesite=strict`;
                localStorage.setItem('token', token);

                // Store user info in cookies only
                if (userId) {
                    document.cookie = `userId=${userId}; path=/; secure; samesite=strict`;
                }

                if (email) {
                    document.cookie = `email=${email}; path=/; secure; samesite=strict`;
                }

                if (role) {
                    document.cookie = `role=${role}; path=/; secure; samesite=strict`;
                }

                // Redirect based on role
                if (role?.toLowerCase() === 'hr administrator') {
                    router.push('/hr/admin/dashboard');
                } else if (role?.toLowerCase() === 'employee') {
                    router.push('/employee/dashboard');
                } else {
                    // Default to employee dashboard if role is not recognized
                    router.push('/employee/dashboard');
                }
            } catch (error) {
                console.error('OAuth2 redirect error:', error);
                // Clear all cookies and redirect to home page
                authService.clearTokens();
                router.push('/');
            }
        };

        handleRedirect();
    }, [router, searchParams]);

    return (
        <div className="flex items-center justify-center min-h-screen">
            <div className="text-center">
                <h1 className="text-2xl font-bold mb-4">Processing authentication...</h1>
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mx-auto"></div>
            </div>
        </div>
    );
} 