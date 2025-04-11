'use client';

import { useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';

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
                    throw new Error('Missing required parameters');
                }

                // Store the token in a cookie
                document.cookie = `token=${token}; path=/; secure; samesite=strict`;

                // Store user info in cookies if needed
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
                router.push('/?error=oauth_failed');
            }
        };

        handleRedirect();
    }, [router, searchParams]);

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-900 dark:to-gray-800">
            <div className="text-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 dark:border-white mx-auto"></div>
                <p className="mt-4 text-gray-600 dark:text-gray-300">Completing authentication...</p>
            </div>
        </div>
    );
} 