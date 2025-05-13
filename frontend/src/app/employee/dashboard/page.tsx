"use client"

import { useEffect, useState } from "react"
import { authService } from "@/lib/auth"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import {
  User,
  Calendar,
  LogOut,
  CheckCircle,
  XCircle,
  Clock3,
  TrendingUp,
  Award,
  Zap,
  Coffee,
  BarChart4,
} from "lucide-react"
import { ProfileCompletion } from "@/components/cmp/employee/profile-completion"
import { ClockInOut } from "@/components/cmp/clock-in-out"
import { useUser } from "@/contexts/UserContext"
import { useRouter } from "next/navigation"
import { Badge } from "@/components/ui/badge"
import { format, subDays, eachDayOfInterval } from "date-fns"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Skeleton } from "@/components/ui/skeleton"

// Import the Line chart component from recharts
import { Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend, AreaChart, Area } from "recharts"

interface EmployeeProfile {
  id: number
  firstName: string
  lastName: string
  email: string
  role: string
  department: string
  position: string
  hireDate: string
  profileCompletion: number
  status: boolean
}

interface AttendanceRecord {
  attendanceId: string
  employeeId: string
  employeeName: string
  employeeEmail: string
  date: string
  clockInTime: string | null
  clockOutTime: string | null
  totalHours: number | null
  status: string
  remarks: string | null
  overtimeHours: number | null
  tardinessMinutes: number | null
  undertimeMinutes: number | null
  approvedByManager: boolean
}
interface AttendanceSummary {
  totalDays: number
  presentDays: number
  absentDays: number
  lateDays: number
  presentPercentage: number
  currentStreak: number
  bestDay: string
}

export default function EmployeeDashboard() {
  const [profile, setProfile] = useState<EmployeeProfile | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [attendanceRecords, setAttendanceRecords] = useState<AttendanceRecord[]>([])
  const [attendanceSummary, setAttendanceSummary] = useState<AttendanceSummary>({
    totalDays: 0,
    presentDays: 0,
    absentDays: 0,
    lateDays: 0,
    presentPercentage: 0,
    currentStreak: 0,
    bestDay: "N/A",
  })
  const [attendanceLoading, setAttendanceLoading] = useState(true)
  const [timeRange, setTimeRange] = useState("week")
  const { user } = useUser()
  const isActive = user?.status === true
  const router = useRouter()

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await authService.getEmployeeProfile()
        setProfile(data)
      } catch (err) {
        router.push("/")
        authService.logout()
        setLoading(false)
      } finally {
        setLoading(false)
      }
    }

    fetchProfile()
  }, [router])

  useEffect(() => {
    const fetchAttendanceData = async () => {
      if (!isActive) return

      try {
        setAttendanceLoading(true)
        const token = authService.getToken()

        if (!token) {
          throw new Error("No authentication token found")
        }

        // Fetch attendance records
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/employee/attendance/paged?page=0&size=30&sortBy=date&direction=desc`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          },
        )

        if (!response.ok) {
          throw new Error(`Failed to fetch attendance records: ${response.status} ${response.statusText}`)
        }

        const data = await response.json()
        
        if (!data.content || !Array.isArray(data.content)) {
          console.error('Invalid data format received:', data)
          throw new Error('Invalid data format received from server')
        }

        const records = data.content.map((record: any) => ({
          attendanceId: record.attendanceId,
          employeeId: record.employeeId,
          employeeName: record.employeeName,
          employeeEmail: record.employeeEmail,
          date: record.date,
          clockInTime: record.clockInTime,
          clockOutTime: record.clockOutTime,
          totalHours: record.totalHours || 0,
          status: record.status || 'absent',
          remarks: record.remarks || null,
          overtimeHours: record.overtimeHours || null,
          tardinessMinutes: record.tardinessMinutes || null,
          undertimeMinutes: record.undertimeMinutes || null,
          approvedByManager: record.approvedByManager || false
        }))

        setAttendanceRecords(records)

        // Calculate summary
        const presentDays = records.filter((data: AttendanceRecord) => data.remarks?.toLowerCase() === "present").length
        const absentDays = records.filter((data: AttendanceRecord) => data.remarks?.toLowerCase() === "absent").length
        const lateDays = records.filter((data: AttendanceRecord) => data.remarks?.toLowerCase() === "late").length
        const totalDays = records.length
        const presentPercentage = totalDays > 0 ? Math.round((presentDays / totalDays) * 100) : 0


        // Calculate current streak
        let streak = 0
        const sortedRecords = [...records].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())

        for (const record of sortedRecords) {
          if (record.remarks?.toLowerCase() === "present") {
            streak++
          } else {
            break
          }
        }

        // Calculate best day
        const dayMap = {
          0: "Sunday",
          1: "Monday",
          2: "Tuesday",
          3: "Wednesday",
          4: "Thursday",
          5: "Friday",
          6: "Saturday",
        }

        const dayDistribution = records.reduce(
          (acc: Record<number, number>, record: AttendanceRecord) => {
            if (record.remarks?.toLowerCase() === "present") {
              const day = new Date(record.date).getDay()
              acc[day] = (acc[day] || 0) + 1
            }
            return acc
          },
          {} as Record<number, number>,
        )

        let bestDay = "N/A"
        let maxCount = 0

        type DayDistributionEntry = [string, number]
        const entries = Object.entries(dayDistribution) as DayDistributionEntry[]
        entries.forEach(([day, count]) => {
          if (count > maxCount) {
            maxCount = count
            bestDay = dayMap[Number.parseInt(day) as keyof typeof dayMap]
          }
        })

        setAttendanceSummary({
          totalDays,
          presentDays,
          absentDays,
          lateDays,
          presentPercentage,
          currentStreak: streak,
          bestDay,
        })
      } catch (error) {
        console.error("Error fetching attendance data:", error)
      } finally {
        setAttendanceLoading(false)
      }
    }

    fetchAttendanceData()
  }, [isActive])

  // Generate chart data based on time range
  const getChartData = () => {
    if (!attendanceRecords.length) return []

    let days
    const today = new Date()

    switch (timeRange) {
      case "week":
        days = 7
        break
      case "month":
        days = 30
        break
      case "quarter":
        days = 90
        break
      default:
        days = 7
    }

    const dateRange = eachDayOfInterval({
      start: subDays(today, days - 1),
      end: today,
    })

    // Create a map of dates to attendance status
    const attendanceMap = attendanceRecords.reduce(
      (acc, record) => {
        acc[record.date] = {
          status: record.remarks?.toLowerCase() || 'absent',
          hours: record.totalHours || 0,
        }
        return acc
      },
      {} as Record<string, { status: string; hours: number }>,
    )

    // Generate data for each day in the range
    return dateRange.map((date) => {
      const dateStr = format(date, "yyyy-MM-dd")
      const dayData = attendanceMap[dateStr] || { status: "absent", hours: 0 }

      return {
        date: format(date, "MMM dd"),
        fullDate: dateStr,
        hours: dayData.hours,
        present: dayData.status === "present" ? 1 : 0,
        absent: dayData.status === "absent" ? 1 : 0,
        late: dayData.status === "late" ? 1 : 0,
        status: dayData.status || "absent",
      }
    })
  }

  const chartData = getChartData()

  // Custom tooltip for the chart
  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload
      return (
        <div className="bg-white dark:bg-slate-800 p-3 rounded-lg shadow-lg border border-slate-200 dark:border-slate-700">
          <p className="font-medium text-slate-800 dark:text-white">{label}</p>
          <p className="text-sm text-slate-600 dark:text-slate-300">
            Status:{" "}
            <span
              className={
                data.status === "present"
                  ? "text-emerald-600 dark:text-emerald-400"
                  : data.status === "absent"
                    ? "text-rose-600 dark:text-rose-400"
                    : "text-amber-600 dark:text-amber-400"
              }
            >
              {data.status.charAt(0).toUpperCase() + data.status.slice(1)}
            </span>
          </p>
          {data.hours > 0 && (
            <p className="text-sm text-slate-600 dark:text-slate-300">
              Hours: <span className="font-medium">{data.hours}</span>
            </p>
          )}
        </div>
      )
    }
    return null
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-teal-50 via-cyan-50 to-sky-50 dark:from-slate-900 dark:via-teal-950 dark:to-slate-950 p-4 md:p-6">
        <div className="w-full max-w-6xl mx-auto space-y-8">
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
            <Skeleton className="h-12 w-64 rounded-lg" />
          </div>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            {Array.from({ length: 4 }).map((_, index) => (
              <Skeleton key={index} className="h-[180px] w-full rounded-xl" />
            ))}
          </div>
          <Skeleton className="h-[300px] w-full rounded-xl" />
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-teal-50 via-cyan-50 to-sky-50 dark:from-slate-900 dark:via-teal-950 dark:to-slate-950 p-4 md:p-6 flex items-center justify-center">
        <div className="text-center p-8 bg-white dark:bg-slate-800 rounded-xl shadow-xl border border-rose-200 dark:border-rose-800">
          <XCircle className="h-12 w-12 text-rose-500 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-slate-800 dark:text-white mb-2">Error</h2>
          <p className="text-rose-600 dark:text-rose-400">{error}</p>
          <Button
            className="mt-4 bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-600 hover:to-cyan-600"
            onClick={() => window.location.reload()}
          >
            Try Again
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-teal-50 via-cyan-50 to-sky-50 dark:from-slate-900 dark:via-teal-950 dark:to-slate-950 p-4 md:p-6">
      <div className="w-full max-w-6xl mx-auto space-y-8">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-2">
              <div className="h-12 w-12 bg-gradient-to-br from-teal-400 to-cyan-500 rounded-full flex items-center justify-center mr-2 shadow-lg shadow-teal-200 dark:shadow-teal-900/30">
                <User className="h-6 w-6 text-white" />
              </div>
              Welcome, {profile?.firstName}!
            </h1>
            <p className="text-slate-600 dark:text-slate-300 mt-1 text-lg">
              {format(new Date(), "EEEE, MMMM d, yyyy")}
            </p>
          </div>
          <div className="flex items-center gap-3">
            <Badge className="bg-gradient-to-r from-teal-500 to-cyan-500 text-white px-3 py-1.5 text-sm">
              {profile?.department} • {profile?.position}
            </Badge>
            <Button
              variant="outline"
              size="sm"
              className="border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300"
              onClick={() => {
                authService.logout()
                router.push("/")
              }}
            >
              <LogOut className="h-4 w-4 mr-2" />
              Logout
            </Button>
          </div>
        </div>

        {!isActive ? (
          <div className="h-full">
            <ProfileCompletion />
          </div>
        ) : (
          <>
          
            {/* Analytics Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="grid grid-cols-1 md:grid-cols-1">
              <div className="grid grid-cols-4 md:grid-cols-2 gap-4 h-fit">
                
                <Card className="border-none shadow-xl overflow-hidden bg-white dark:bg-slate-800 rounded-xl hover:shadow-2xl transition-shadow duration-200 group">
                  <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-teal-400 to-cyan-500"></div>
                  <CardContent className="p-6">
                    <div className="flex justify-between items-center">
                      <div>
                        <p className="text-sm font-medium text-slate-500 dark:text-slate-400">Total Days Tracked</p>
                        <h3 className="text-3xl font-bold text-slate-800 dark:text-white mt-1">
                          {attendanceLoading ? <Skeleton className="h-9 w-16" /> : attendanceSummary.totalDays}
                        </h3>
                      </div>
                      <div className="h-14 w-14 bg-teal-50 dark:bg-teal-900/30 rounded-full flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
                        <Calendar className="h-7 w-7 text-teal-500 dark:text-teal-400" />
                      </div>
                    </div>
                    <div className="mt-4 pt-4 border-t border-slate-100 dark:border-slate-700">
                      <p className="text-xs text-slate-500 dark:text-slate-400">
                        Your attendance journey started on{" "}
                        {attendanceRecords.length > 0
                          ? format(new Date(attendanceRecords[attendanceRecords.length - 1].date), "MMMM d, yyyy")
                          : "..."}
                      </p>
                    </div>
                  </CardContent>
                </Card>

                <Card className="border-none shadow-xl overflow-hidden bg-white dark:bg-slate-800 rounded-xl hover:shadow-2xl transition-shadow duration-200 group">
                  <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-teal-400 to-cyan-500"></div>
                  <CardContent className="p-6">
                    <div className="flex justify-between items-center">
                      <div>
                        <p className="text-sm font-medium text-slate-500 dark:text-slate-400">Current Streak</p>
                        <h3 className="text-3xl font-bold text-slate-800 dark:text-white mt-1">
                          {attendanceLoading ? (
                            <Skeleton className="h-9 w-16" />
                          ) : (
                            `${attendanceSummary.currentStreak} days`
                          )}
                        </h3>
                      </div>
                      <div className="h-14 w-14 bg-cyan-50 dark:bg-cyan-900/30 rounded-full flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
                        <Zap className="h-7 w-7 text-cyan-500 dark:text-cyan-400" />
                      </div>
                    </div>
                    <div className="mt-4 pt-4 border-t border-slate-100 dark:border-slate-700">
                      <p className="text-xs text-slate-500 dark:text-slate-400">
                        {attendanceSummary.currentStreak > 5
                          ? "Amazing streak! Keep it up! 🔥"
                          : "Build your streak by being present consistently"}
                      </p>
                    </div>
                  </CardContent>
                </Card>

                <Card className="border-none shadow-xl overflow-hidden bg-white dark:bg-slate-800 rounded-xl hover:shadow-2xl transition-shadow duration-200 group">
                  <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-teal-400 to-cyan-500"></div>
                  <CardContent className="p-6">
                    <div className="flex justify-between items-center">
                      <div>
                        <p className="text-sm font-medium text-slate-500 dark:text-slate-400">Best Day</p>
                        <h3 className="text-3xl font-bold text-slate-800 dark:text-white mt-1">
                          {attendanceLoading ? <Skeleton className="h-9 w-16" /> : attendanceSummary.bestDay}
                        </h3>
                      </div>
                      <div className="h-14 w-14 bg-sky-50 dark:bg-sky-900/30 rounded-full flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
                        <Award className="h-7 w-7 text-sky-500 dark:text-sky-400" />
                      </div>
                    </div>
                    <div className="mt-4 pt-4 border-t border-slate-100 dark:border-slate-700">
                      <p className="text-xs text-slate-500 dark:text-slate-400">
                        You're most consistent on {attendanceSummary.bestDay}
                      </p>
                    </div>
                  </CardContent>
                </Card>

                <Card className="border-none shadow-xl overflow-hidden bg-white dark:bg-slate-800 rounded-xl hover:shadow-2xl transition-shadow duration-200 group">
                  <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-teal-400 to-cyan-500"></div>
                  <CardContent className="p-6">
                    <div className="flex justify-between items-center">
                      <div>
                        <p className="text-sm font-medium text-slate-500 dark:text-slate-400">Attendance Rate</p>
                        <h3 className="text-3xl font-bold text-slate-800 dark:text-white mt-1">
                          {attendanceLoading ? (
                            <Skeleton className="h-9 w-16" />
                          ) : (
                            `${attendanceSummary.presentPercentage}%`
                          )}
                        </h3>
                      </div>
                      <div className="h-14 w-14 bg-emerald-50 dark:bg-emerald-900/30 rounded-full flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
                        <TrendingUp className="h-7 w-7 text-emerald-500 dark:text-emerald-400" />
                      </div>
                    </div>
                    <div className="mt-4 pt-4 border-t border-slate-100 dark:border-slate-700">
                      <div className="h-2 w-full bg-slate-100 dark:bg-slate-700 rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-emerald-400 to-teal-500 rounded-full transition-all duration-1000 ease-out"
                          style={{ width: `${attendanceLoading ? 0 : attendanceSummary.presentPercentage}%` }}
                        ></div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-1 mt-6">
                  {/* Attendance Chart */}
                  <Card className="border-none shadow-xl overflow-hidden bg-white dark:bg-slate-800 rounded-xl">
                    <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-teal-400 to-cyan-500"></div>
                    <CardHeader className="px-6 pt-6 pb-0">
                      <div className="flex flex-col md:flex-row justify-between md:items-center gap-4">
                        <div className="flex items-center gap-2">
                          <div className="h-10 w-10 bg-gradient-to-br from-teal-400 to-cyan-500 rounded-full flex items-center justify-center shadow-md">
                            <BarChart4 className="h-5 w-5 text-white" />
                          </div>
                          <CardTitle className="text-xl font-bold text-slate-800 dark:text-white">
                            Attendance Trends
                          </CardTitle>
                        </div>
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
                    </CardHeader>
                    <CardContent className="p-6">
                      {attendanceLoading ? (
                        <Skeleton className="h-[300px] w-full rounded-lg" />
                      ) : chartData.length > 0 ? (
                        <div className="h-[300px] w-full">
                          <ResponsiveContainer width="100%" height="100%">
                            <AreaChart data={chartData} margin={{ top: 20, right: 30, left: 0, bottom: 0 }}>
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
                                <linearGradient id="hoursGradient" x1="0" y1="0" x2="0" y2="1">
                                  <stop offset="5%" stopColor="#0EA5E9" stopOpacity={0.8} />
                                  <stop offset="95%" stopColor="#0EA5E9" stopOpacity={0.1} />
                                </linearGradient>
                              </defs>
                              <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" />
                              <XAxis
                                dataKey="date"
                                stroke="#6B7280"
                                fontSize={12}
                                tickLine={false}
                                axisLine={{ stroke: "#E5E7EB" }}
                              />
                              <YAxis
                                yAxisId="left"
                                stroke="#6B7280"
                                fontSize={12}
                                tickLine={false}
                                axisLine={{ stroke: "#E5E7EB" }}
                                tickFormatter={(value) => (value === 0 ? "No" : value === 1 ? "Yes" : "")}
                              />
                              <YAxis
                                yAxisId="right"
                                orientation="right"
                                stroke="#6B7280"
                                fontSize={12}
                                tickLine={false}
                                axisLine={{ stroke: "#E5E7EB" }}
                                label={{ value: "Hours", angle: -90, position: "insideRight", fill: "#6B7280", fontSize: 12 }}
                              />
                              <Tooltip content={<CustomTooltip />} />
                              <Legend />
                              <Area
                                type="monotone"
                                dataKey="present"
                                name="Present"
                                stroke="#10B981"
                                fillOpacity={1}
                                fill="url(#presentGradient)"
                                yAxisId="left"
                                strokeWidth={2}
                              />
                              <Area
                                type="monotone"
                                dataKey="late"
                                name="Late"
                                stroke="#F59E0B"
                                fillOpacity={1}
                                fill="url(#lateGradient)"
                                yAxisId="left"
                                strokeWidth={2}
                              />
                              <Area
                                type="monotone"
                                dataKey="absent"
                                name="Absent"
                                stroke="#EF4444"
                                fillOpacity={1}
                                fill="url(#absentGradient)"
                                yAxisId="left"
                                strokeWidth={2}
                              />
                              <Line
                                type="monotone"
                                dataKey="hours"
                                name="Hours Worked"
                                stroke="#0EA5E9"
                                yAxisId="right"
                                strokeWidth={2}
                                dot={{ r: 4, fill: "#0EA5E9", stroke: "#0EA5E9", strokeWidth: 1 }}
                                activeDot={{ r: 6, fill: "#0EA5E9", stroke: "#fff", strokeWidth: 2 }}
                              />
                            </AreaChart>
                          </ResponsiveContainer>
                        </div>
                      ) : (
                        <div className="h-[300px] w-full flex items-center justify-center">
                          <div className="text-center">
                            <Calendar className="h-12 w-12 text-slate-400 mx-auto mb-4" />
                            <h3 className="text-lg font-medium text-slate-700 dark:text-slate-300">
                              No attendance data available
                            </h3>
                            <p className="text-slate-500 dark:text-slate-400 max-w-md">
                              Start clocking in to see your attendance trends
                            </p>
                          </div>
                        </div>
                      )}
                    </CardContent>
                  </Card>

              </div>

              </div>

               {/* Clock In/Out Component */}
            <div className="mt-8 -space-y-8">
              <ClockInOut />
            </div>

            </div>


            {/* Status Summary */}
            <Card className="border-none shadow-xl overflow-hidden bg-white dark:bg-slate-800 rounded-xl">
              <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-teal-400 to-cyan-500"></div>
              <CardContent className="p-6">
                <div className="flex items-center justify-between mb-6">
                  <div className="flex items-center gap-2">
                    <div className="h-10 w-10 bg-gradient-to-br from-teal-400 to-cyan-500 rounded-full flex items-center justify-center shadow-md">
                      <TrendingUp className="h-5 w-5 text-white" />
                    </div>
                    <h3 className="text-xl font-bold text-slate-800 dark:text-white">Your Attendance Progress</h3>
                  </div>
                  <Badge
                    variant="outline"
                    className="bg-teal-50 text-teal-600 border-teal-200 dark:bg-teal-900/30 dark:text-teal-400 dark:border-teal-800 px-3 py-1.5 text-sm font-medium"
                  >
                    <CheckCircle className="h-3.5 w-3.5 mr-1.5" />
                    {attendanceSummary.presentPercentage}% Present Rate
                  </Badge>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div className="space-y-3">
                    <div className="flex justify-between mb-1">
                      <span className="text-sm font-medium text-slate-600 dark:text-slate-300 flex items-center">
                        <CheckCircle className="h-4 w-4 mr-1.5 text-emerald-500" />
                        Present
                      </span>
                      <span className="text-sm font-medium text-emerald-600 dark:text-emerald-400">
                        {attendanceLoading ? "..." : attendanceSummary.presentDays}
                      </span>
                    </div>
                    <div className="h-3 w-full bg-slate-100 dark:bg-slate-700 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-gradient-to-r from-emerald-400 to-teal-500 rounded-full transition-all duration-1000 ease-out"
                        style={{
                          width: `${attendanceLoading ? 0 : (attendanceSummary.presentDays / attendanceSummary.totalDays) * 100}%`,
                        }}
                      ></div>
                    </div>
                  </div>

                  <div className="space-y-3">
                    <div className="flex justify-between mb-1">
                      <span className="text-sm font-medium text-slate-600 dark:text-slate-300 flex items-center">
                        <XCircle className="h-4 w-4 mr-1.5 text-rose-500" />
                        Absent
                      </span>
                      <span className="text-sm font-medium text-rose-600 dark:text-rose-400">
                        {attendanceLoading ? "..." : attendanceSummary.absentDays}
                      </span>
                    </div>
                    <div className="h-3 w-full bg-slate-100 dark:bg-slate-700 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-gradient-to-r from-rose-400 to-pink-500 rounded-full transition-all duration-1000 ease-out"
                        style={{
                          width: `${attendanceLoading ? 0 : (attendanceSummary.absentDays / attendanceSummary.totalDays) * 100}%`,
                        }}
                      ></div>
                    </div>
                  </div>

                  <div className="space-y-3">
                    <div className="flex justify-between mb-1">
                      <span className="text-sm font-medium text-slate-600 dark:text-slate-300 flex items-center">
                        <Clock3 className="h-4 w-4 mr-1.5 text-amber-500" />
                        Late
                      </span>
                      <span className="text-sm font-medium text-amber-600 dark:text-amber-400">
                        {attendanceLoading ? "..." : attendanceSummary.lateDays}
                      </span>
                    </div>
                    <div className="h-3 w-full bg-slate-100 dark:bg-slate-700 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-gradient-to-r from-amber-400 to-orange-500 rounded-full transition-all duration-1000 ease-out"
                        style={{
                          width: `${attendanceLoading ? 0 : (attendanceSummary.lateDays / attendanceSummary.totalDays) * 100}%`,
                        }}
                      ></div>
                    </div>
                  </div>
                </div>

                <div className="mt-8 pt-6 border-t border-slate-100 dark:border-slate-700">
                  <div className="flex flex-wrap gap-3">
                    <Badge className="bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-400 py-1.5 px-3">
                      <CheckCircle className="h-3.5 w-3.5 mr-1.5" />
                      {attendanceSummary.presentDays} Present Days
                    </Badge>
                    <Badge className="bg-rose-100 text-rose-800 dark:bg-rose-900/30 dark:text-rose-400 py-1.5 px-3">
                      <XCircle className="h-3.5 w-3.5 mr-1.5" />
                      {attendanceSummary.absentDays} Absent Days
                    </Badge>
                    <Badge className="bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400 py-1.5 px-3">
                      <Clock3 className="h-3.5 w-3.5 mr-1.5" />
                      {attendanceSummary.lateDays} Late Days
                    </Badge>
                    <Badge className="bg-sky-100 text-sky-800 dark:bg-sky-900/30 dark:text-sky-400 py-1.5 px-3">
                      <Coffee className="h-3.5 w-3.5 mr-1.5" />
                      {attendanceRecords.filter((r) => r.status?.toLowerCase() === "leave").length} Leave Days
                    </Badge>
                  </div>
                </div>
              </CardContent>
            </Card>

           
          </>
        )}
      </div>
    </div>
  )
}
