'use client';

import { useEffect } from 'react';
import { authService } from '@/lib/auth';

export function SessionManager() {
    useEffect(() => {
        // Set a timestamp when the page loads
        const pageLoadTime = Date.now();
        localStorage.setItem('pageLoadTime', pageLoadTime.toString());

        // Function to handle session cleanup
        const handleBeforeUnload = () => {
            // We don't do anything in beforeunload
            // The actual check happens in the unload event
        };

        // Function to handle unload (this runs after beforeunload)
        const handleUnload = () => {
            // Get the current time
            const currentTime = Date.now();

            // Get the page load time from localStorage
            const storedLoadTime = localStorage.getItem('pageLoadTime');

            if (storedLoadTime) {
                const loadTime = parseInt(storedLoadTime, 10);

                // If the time difference is very small (less than 100ms), it's likely a reload
                // If the time difference is larger, it's likely a tab/window close
                if (currentTime - loadTime > 100) {
                    // This is likely a tab/window close, end the session
                    // We use sendBeacon to ensure the request is sent even during page unload
                    const token = authService.getToken();
                    if (token) {
                        // Use sendBeacon to send the logout request
                        const data = new FormData();
                        data.append('token', token);

                        navigator.sendBeacon(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'}/auth/logout`, data);
                    }

                    // Clear tokens from localStorage
                    localStorage.removeItem('token');
                }
            }
        };

        // Add event listeners
        window.addEventListener('beforeunload', handleBeforeUnload);
        window.addEventListener('unload', handleUnload);

        // Clean up event listeners when component unmounts
        return () => {
            window.removeEventListener('beforeunload', handleBeforeUnload);
            window.removeEventListener('unload', handleUnload);
        };
    }, []);

    return null; // This component doesn't render anything
} 