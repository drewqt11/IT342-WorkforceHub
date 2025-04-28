"use client";

import { useEffect, useState } from 'react';
import { authService } from '@/lib/auth';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Clock, User, Calendar, FileText } from 'lucide-react';
import { ProfileCompletion } from '@/components/cmp/employee/profile-completion';
import { ClockInOut } from '@/components/cmp/clock-in-out';
import { useUser } from '@/contexts/UserContext';
import { useRouter } from 'next/navigation';

interface EmployeeProfile {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  department: string;
  position: string;
  hireDate: string;
  profileCompletion: number;
  status: boolean;
}

export default function EmployeeDashboard() {
  const [profile, setProfile] = useState<EmployeeProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { userStatus } = useUser();
  const isActive = userStatus === "Active";
  const router = useRouter();

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await authService.getEmployeeProfile();
        setProfile(data);
      } catch (err) {
        setError('Failed to load profile data');
        console.error('Error fetching profile:', err);
        setLoading(false);
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [router]);

  if (loading) {
    return <div className="flex items-center justify-center min-h-screen">Loading...</div>;
  }

  if (error) {
    return <div className="flex items-center justify-center min-h-screen text-red-500">{error}</div>;
  }

  return (
    <div className="container mx-auto p-6 h-full">
      <h1 className="text-3xl font-bold mb-8">
        Welcome, {profile?.firstName}!
      </h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 h-[calc(100vh-12rem)]">
        {isActive ? (
          <div className="h-full lg:col-span-3">
            <ClockInOut />
          </div>
        ) : (
          <div className="h-full lg:col-span-3">
            <ProfileCompletion />
          </div>
        )}
      </div>
    </div>
  );
}
