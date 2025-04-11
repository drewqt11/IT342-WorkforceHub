"use client";

import { useEffect, useState } from "react";
import { authService } from "@/lib/auth";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Clock, User, Calendar, FileText } from "lucide-react";
import { ProfileCompletion } from "@/components/cmp/employee/profile-completion";
import { ClockInOut } from "@/components/cmp/clock-in-out";

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
}

export default function EmployeeDashboard() {
  const [profile, setProfile] = useState<EmployeeProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await authService.getEmployeeProfile();
        setProfile(data);
      } catch (err) {
        setError("Failed to load profile data");
        console.error("Error fetching profile:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        Loading...
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen text-red-500">
        {error}
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <h1 className="text-3xl font-bold mb-8">
        Welcome, Admin {profile?.firstName}!
      </h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2">
          <div className="max-w-4xl mx-auto">
            <ClockInOut />
          </div>
        </div>
        
        <div className="2xl:col-span-1">
          <Card className="border-0 shadow-md bg-white dark:bg-blue-900">
            <CardContent className="p-6">
              <h2 className="text-lg font-semibold text-blue-900 dark:text-blue-100 mb-4">Quick Stats</h2>
              <div className="space-y-4">
                <div className="flex items-center justify-between p-3 rounded-lg bg-blue-50 dark:bg-blue-800/30">
                  <div className="flex items-center gap-2">
                    <User className="h-5 w-5 text-blue-600 dark:text-blue-400" />
                    <span className="text-blue-900 dark:text-blue-100">Active Employees</span>
                  </div>
                  <span className="text-blue-700 dark:text-blue-300 font-bold">24</span>
                </div>
                <div className="flex items-center justify-between p-3 rounded-lg bg-blue-50 dark:bg-blue-800/30">
                  <div className="flex items-center gap-2">
                    <Calendar className="h-5 w-5 text-blue-600 dark:text-blue-400" />
                    <span className="text-blue-900 dark:text-blue-100">Today's Attendance</span>
                  </div>
                  <span className="text-blue-700 dark:text-blue-300 font-bold">18</span>
                </div>
                <div className="flex items-center justify-between p-3 rounded-lg bg-blue-50 dark:bg-blue-800/30">
                  <div className="flex items-center gap-2">
                    <FileText className="h-5 w-5 text-blue-600 dark:text-blue-400" />
                    <span className="text-blue-900 dark:text-blue-100">Pending Requests</span>
                  </div>
                  <span className="text-blue-700 dark:text-blue-300 font-bold">5</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
