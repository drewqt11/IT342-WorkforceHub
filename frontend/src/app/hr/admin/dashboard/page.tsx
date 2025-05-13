"use client";

import { useEffect, useState } from "react";
import { authService } from "@/lib/auth";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Clock, User, Calendar, FileText, CheckCircle, XCircle, AlertCircle, TrendingUp, Users } from "lucide-react";
import { useRouter } from "next/navigation";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { format, subDays, eachDayOfInterval } from "date-fns";
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from "recharts";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";

interface ActiveEmployee {
  employeeId: string;
  idNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  gender: string;
  hireDate: string;
  dateOfBirth: string;
  address: string;
  phoneNumber: string;
  maritalStatus: string;
  status: boolean;
  employmentStatus: string;
  departmentId: string;
  departmentName: string;
  jobId: string;
  jobName: string;
  roleId: string;
  roleName: string;
  createdAt: string;
  workTimeInSched: {
    hour: number;
    minute: number;
    second: number;
    nano: number;
  };
  workTimeOutSched: {
    hour: number;
    minute: number;
    second: number;
    nano: number;
  };
  userId: string;
  lastLogin: string;
  isActive: boolean;
}

interface PaginatedResponse<T> {
  totalPages: number;
  totalElements: number;
  size: number;
  content: T[];
  number: number;
  sort: {
    empty: boolean;
    unsorted: boolean;
    sorted: boolean;
  };
  numberOfElements: number;
  pageable: {
    offset: number;
    sort: {
      empty: boolean;
      unsorted: boolean;
      sorted: boolean;
    };
    paged: boolean;
    unpaged: boolean;
    pageNumber: number;
    pageSize: number;
  };
  first: boolean;
  last: boolean;
  empty: boolean;
}

interface AttendanceRecord {
  attendanceId: string;
  employeeId: string;
  employeeName: string;
  employeeEmail: string;
  date: string;
  clockInTime: string | null;
  clockOutTime: string | null;
  totalHours: number | null;
  status: string;
  remarks: string | null;
  overtimeHours: number | null;
  tardinessMinutes: number | null;
  undertimeMinutes: number | null;
  approvedByManager: boolean;
}

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

export default function AdminDashboard() {
  const [profile, setProfile] = useState<EmployeeProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [attendanceRecords, setAttendanceRecords] = useState<AttendanceRecord[]>([]);
  const [activeEmployees, setActiveEmployees] = useState<ActiveEmployee[]>([]);
  const [totalEmployees, setTotalEmployees] = useState(0);
  const [todayAttendance, setTodayAttendance] = useState(0);
  const [pendingRequests, setPendingRequests] = useState(0);
  const [timeRange, setTimeRange] = useState("week");
  const router = useRouter();

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await authService.getEmployeeProfile();
        setProfile(data);
      } catch (err) {
        setError("Failed to load profile data");
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, []);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const token = authService.getToken();

        if (!token) {
          router.push("/");
          return;
        }

        // Fetch active employees
        const employeesResponse = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/hr/employees/active?page=0&size=100`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        if (!employeesResponse.ok) {
          throw new Error(`Failed to fetch active employees: ${employeesResponse.status} ${employeesResponse.statusText}`);
        }

        const employeesData: PaginatedResponse<ActiveEmployee> = await employeesResponse.json();
        setActiveEmployees(employeesData.content);
        setTotalEmployees(employeesData.totalElements);

        // Fetch attendance records
        const attendanceResponse = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/hr/attendance/all?page=0&size=30&sortBy=date&direction=desc`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        if (!attendanceResponse.ok) {
          throw new Error(`Failed to fetch attendance records: ${attendanceResponse.status} ${attendanceResponse.statusText}`);
        }

        const attendanceData = await attendanceResponse.json();
        setAttendanceRecords(attendanceData.content);
        
        // Calculate statistics
        const today = format(new Date(), 'yyyy-MM-dd');
        const todayRecords = attendanceData.content.filter((record: AttendanceRecord) => record.date === today);
        setTodayAttendance(todayRecords.length);
        
        // Count pending approvals
        const pending = attendanceData.content.filter((record: AttendanceRecord) => !record.approvedByManager).length;
        setPendingRequests(pending);

      } catch (err) {
        setError(err instanceof Error ? err.message : "An error occurred");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [router]);

  // Calculate attendance statistics
  const getPresentCount = () => {
    return attendanceRecords.filter(record => record.remarks?.toLowerCase() === "present").length;
  };

  const getAbsentCount = () => {
    return attendanceRecords.filter(record => record.remarks?.toLowerCase() === "absent").length;
  };

  const getLateCount = () => {
    return attendanceRecords.filter(record => record.remarks?.toLowerCase() === "late").length;
  };

  const getPresentPercentage = () => {
    if (attendanceRecords.length === 0) return 0;
    return Math.round((getPresentCount() / attendanceRecords.length) * 100);
  };

  // Generate chart data
  const getChartData = () => {
    let days;
    const today = new Date();

    switch (timeRange) {
      case "week":
        days = 7;
        break;
      case "month":
        days = 30;
        break;
      case "quarter":
        days = 90;
        break;
      default:
        days = 7;
    }

    const dateRange = eachDayOfInterval({
      start: subDays(today, days - 1),
      end: today,
    });

    return dateRange.map(date => {
      const dateStr = format(date, 'yyyy-MM-dd');
      const dayRecords = attendanceRecords.filter(record => record.date === dateStr);
      return {
        date: format(date, 'MMM dd'),
        fullDate: dateStr,
        present: dayRecords.filter(r => r.remarks?.toLowerCase() === "present").length,
        absent: dayRecords.filter(r => r.remarks?.toLowerCase() === "absent").length,
        late: dayRecords.filter(r => r.remarks?.toLowerCase() === "late").length,
      };
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
        <div className="w-full max-w-6xl mx-auto space-y-8">
          <Skeleton className="h-12 w-64" />
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {Array.from({ length: 3 }).map((_, index) => (
              <Skeleton key={index} className="h-[180px] w-full rounded-xl" />
            ))}
          </div>
          <Skeleton className="h-[300px] w-full rounded-xl" />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6 flex items-center justify-center">
        <div className="text-center p-8 bg-white dark:bg-slate-800 rounded-xl shadow-xl border border-rose-200 dark:border-rose-800">
          <AlertCircle className="h-12 w-12 text-rose-500 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-slate-800 dark:text-white mb-2">Error</h2>
          <p className="text-rose-600 dark:text-rose-400">{error}</p>
          <Button
            className="mt-4 bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488]"
            onClick={() => window.location.reload()}
          >
            Try Again
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
      <div className="w-full max-w-6xl mx-auto space-y-8">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-2">
              <div className="h-12 w-12 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-full flex items-center justify-center mr-2 shadow-lg">
                <User className="h-6 w-6 text-white" />
              </div>
              Welcome, Admin {profile?.firstName}!
            </h1>
            <p className="text-slate-600 dark:text-slate-300 mt-1 text-lg">
              {format(new Date(), "EEEE, MMMM d, yyyy")}
            </p>
          </div>
        </div>

        {/* Quick Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Card className="border-none shadow-xl overflow-hidden bg-white dark:bg-slate-800 rounded-xl hover:shadow-2xl transition-shadow duration-200 group">
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] to-[#14B8A6]"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-slate-500 dark:text-slate-400">Active Employees</p>
                  <h3 className="text-3xl font-bold text-slate-800 dark:text-white mt-1">
                    {loading ? <Skeleton className="h-9 w-16" /> : totalEmployees}
                  </h3>
                </div>
                <div className="h-14 w-14 bg-blue-50 dark:bg-blue-900/30 rounded-full flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
                  <Users className="h-7 w-7 text-blue-500 dark:text-blue-400" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="border-none shadow-xl overflow-hidden bg-white dark:bg-slate-800 rounded-xl hover:shadow-2xl transition-shadow duration-200 group">
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] to-[#14B8A6]"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-slate-500 dark:text-slate-400">Today's Attendance</p>
                  <h3 className="text-3xl font-bold text-slate-800 dark:text-white mt-1">
                    {loading ? <Skeleton className="h-9 w-16" /> : todayAttendance}
                  </h3>
                </div>
                <div className="h-14 w-14 bg-emerald-50 dark:bg-emerald-900/30 rounded-full flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
                  <CheckCircle className="h-7 w-7 text-emerald-500 dark:text-emerald-400" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="border-none shadow-xl overflow-hidden bg-white dark:bg-slate-800 rounded-xl hover:shadow-2xl transition-shadow duration-200 group">
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] to-[#14B8A6]"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-slate-500 dark:text-slate-400">Pending Approvals</p>
                  <h3 className="text-3xl font-bold text-slate-800 dark:text-white mt-1">
                    {loading ? <Skeleton className="h-9 w-16" /> : pendingRequests}
                  </h3>
                </div>
                <div className="h-14 w-14 bg-amber-50 dark:bg-amber-900/30 rounded-full flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
                  <AlertCircle className="h-7 w-7 text-amber-500 dark:text-amber-400" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Attendance Overview Card */}
        <Card className="border-none shadow-xl overflow-hidden bg-white dark:bg-slate-800 rounded-xl">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] to-[#14B8A6]"></div>
          <CardHeader className="px-6 pt-6 pb-0">
            <div className="flex flex-col md:flex-row justify-between md:items-center gap-4">
              <div className="flex items-center gap-2">
                <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-full flex items-center justify-center shadow-md">
                  <TrendingUp className="h-5 w-5 text-white" />
                </div>
                <CardTitle className="text-xl font-bold text-slate-800 dark:text-white">
                  Attendance Overview
                </CardTitle>
              </div>
              <div className="flex items-center gap-4">
                <Badge
                  variant="outline"
                  className="bg-emerald-50 text-emerald-600 border-emerald-200 dark:bg-emerald-900/30 dark:text-emerald-400 dark:border-emerald-800 px-3 py-1.5 text-sm font-medium"
                >
                  <CheckCircle className="h-3.5 w-3.5 mr-1.5" />
                  {getPresentPercentage()}% Present Rate
                </Badge>
                <Tabs defaultValue="week" className="w-full md:w-auto" onValueChange={setTimeRange}>
                  <TabsList className="bg-slate-100 dark:bg-slate-700 p-1 rounded-full">
                    <TabsTrigger
                      value="week"
                      className="data-[state=active]:bg-white dark:data-[state=active]:bg-slate-800 rounded-full px-4"
                    >
                      Week
                    </TabsTrigger>
                    <TabsTrigger
                      value="month"
                      className="data-[state=active]:bg-white dark:data-[state=active]:bg-slate-800 rounded-full px-4"
                    >
                      Month
                    </TabsTrigger>
                    <TabsTrigger
                      value="quarter"
                      className="data-[state=active]:bg-white dark:data-[state=active]:bg-slate-800 rounded-full px-4"
                    >
                      Quarter
                    </TabsTrigger>
                  </TabsList>
                </Tabs>
              </div>
            </div>
          </CardHeader>
          <CardContent className="p-6">
            <div className="h-[300px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={getChartData()} margin={{ top: 20, right: 30, left: 0, bottom: 0 }}>
                  <defs>
                    <linearGradient id="presentGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#10B981" stopOpacity={0.8} />
                      <stop offset="95%" stopColor="#10B981" stopOpacity={0.1} />
                    </linearGradient>
                    <linearGradient id="lateGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#F59E0B" stopOpacity={0.8} />
                      <stop offset="95%" stopColor="#F59E0B" stopOpacity={0.1} />
                    </linearGradient>
                    <linearGradient id="absentGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#EF4444" stopOpacity={0.8} />
                      <stop offset="95%" stopColor="#EF4444" stopOpacity={0.1} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" />
                  <XAxis dataKey="date" stroke="#6B7280" fontSize={12} />
                  <YAxis stroke="#6B7280" fontSize={12} />
                  <Tooltip />
                  <Legend />
                  <Area
                    type="monotone"
                    dataKey="present"
                    name="Present"
                    stroke="#10B981"
                    fillOpacity={1}
                    fill="url(#presentGradient)"
                  />
                  <Area
                    type="monotone"
                    dataKey="late"
                    name="Late"
                    stroke="#F59E0B"
                    fillOpacity={1}
                    fill="url(#lateGradient)"
                  />
                  <Area
                    type="monotone"
                    dataKey="absent"
                    name="Absent"
                    stroke="#EF4444"
                    fillOpacity={1}
                    fill="url(#absentGradient)"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
