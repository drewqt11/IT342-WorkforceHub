'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { authService } from '@/lib/auth';

export function TokenChecker() {
    const router = useRouter();

    useEffect(() => {
        const checkToken = () => {
            // Check if token exists in localStorage but not in cookies
            const localToken = localStorage.getItem('token');
            const cookieToken = document.cookie.split('; ').find(row => row.startsWith('token='))?.split('=')[1];

            if (localToken && !cookieToken) {
                // Token exists in localStorage but not in cookies, set it as a cookie
                document.cookie = `token=${localToken}; path=/; secure; samesite=strict`;

                // Fetch user profile to get role and other info
                fetchUserProfile(localToken);
            }
        };

        const fetchUserProfile = async (token: string) => {
            try {
                const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'}/employee/profile`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });

                if (response.ok) {
                    const profile = await response.json();

                    // Set role cookie if available
                    if (profile.role) {
                        document.cookie = `role=${profile.role}; path=/; secure; samesite=strict`;
                    }

                    // Redirect to appropriate dashboard based on role
                    if (profile.role?.toLowerCase() === 'hr administrator') {
                        router.push('/hr/admin/dashboard');
                    } else {
                        router.push('/employee/dashboard');
                    }
                } else {
                    // If profile fetch fails, clear token from localStorage
                    localStorage.removeItem('token');
                }
            } catch (error) {
                console.error('Error fetching user profile:', error);
                // If profile fetch fails, clear token from localStorage
                localStorage.removeItem('token');
            }
        };

        checkToken();
    }, [router]);

    return null; // This component doesn't render anything
} 