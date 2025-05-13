"use client"

import { useEffect, useState } from "react"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { format } from "date-fns"
import {
  Search,
  Clock,
  RefreshCw,
  CalendarDays,
  Filter,
  CheckCircle,
  XCircle,
  AlertCircle,
  Award,
  Calendar,
  TrendingUp,
  User,
  Coffee,
  Zap,
} from "lucide-react"
import { useRouter } from "next/navigation"
import { authService } from "@/lib/auth"
import { Skeleton } from "@/components/ui/skeleton"
import { cn } from "@/lib/utils"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Badge } from "@/components/ui/badge"
import { Toaster } from "sonner"
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination"

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

export default function EmployeeAttendanceLogsPage() {
  const router = useRouter()
  const [records, setRecords] = useState<AttendanceRecord[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [totalPages, setTotalPages] = useState(0)
  const [searchTerm, setSearchTerm] = useState("")
  const [sortBy, setSortBy] = useState("date")
  const [direction, setDirection] = useState("desc")
  const [totalRecords, setTotalRecords] = useState(0)
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [activeTab, setActiveTab] = useState("calendar")

  const fetchRecords = async () => {
    try {
      setLoading(true)
      setError(null)

      const token = authService.getToken()
      if (!token) {
        router.push("/")
        return
      }

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/employee/attendance/paged?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        },
      )

      if (!response.ok) {
        if (response.status === 401) {
          router.push("/")
          return
        }
        throw new Error("Failed to fetch attendance records")
      }

      const data = await response.json()
      setRecords(data.content || [])
      setTotalPages(data.totalPages || 0)
      setTotalRecords(data.totalElements || 0)
    } catch (err) {
      setError(err instanceof Error ? err.message : "An error occurred")
      setRecords([])
      setTotalPages(0)
      setTotalRecords(0)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchRecords()
  }, [page, size, sortBy, direction])

  const handleSort = (column: string) => {
    if (sortBy === column) {
      setDirection(direction === "asc" ? "desc" : "asc")
    } else {
      setSortBy(column)
      setDirection("asc")
    }
  }

  const filteredRecords = (records || []).filter((record) => {
    // Apply search filter
    const matchesSearch = record.remarks?.toLowerCase().includes(searchTerm.toLowerCase())

    // Apply status filter
    const matchesStatus =
      statusFilter === "all" ||
      (statusFilter === "present" && record.remarks?.toLowerCase() === "present") ||
      (statusFilter === "absent" && record.remarks?.toLowerCase() === "absent") ||
      (statusFilter === "late" && record.remarks?.toLowerCase() === "late") ||
      (statusFilter === "leave" && record.remarks?.toLowerCase() === "leave") ||
      (statusFilter === "half day" && record.remarks?.toLowerCase() === "half day")

    return matchesSearch && matchesStatus
  })

  const getStatusColor = (remarks: string | null) => {
    switch (remarks?.toLowerCase()) {
      case "present":
        return "bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-400"
      case "absent":
        return "bg-rose-100 text-rose-800 dark:bg-rose-900/30 dark:text-rose-400"
      case "late":
        return "bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400"
      case "half day":
        return "bg-sky-100 text-sky-800 dark:bg-sky-900/30 dark:text-sky-400"
      case "leave":
        return "bg-violet-100 text-violet-800 dark:bg-violet-900/30 dark:text-violet-400"
      default:
        return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-400"
    }
  }

  const getStatusIcon = (remarks: string | null) => {
    switch (remarks?.toLowerCase()) {
      case "present":
        return <CheckCircle className="h-3.5 w-3.5 mr-1" />
      case "absent":
        return <XCircle className="h-3.5 w-3.5 mr-1" />
      case "late":
        return <Clock className="h-3.5 w-3.5 mr-1" />
      case "leave":
        return <Coffee className="h-3.5 w-3.5 mr-1" />
      case "half day":
        return <Calendar className="h-3.5 w-3.5 mr-1" />
      default:
        return null
    }
  }

  // Add this new helper function
  const formatTimeFromMinutes = (minutes: number | null) => {
    if (minutes === null || minutes === 0) return "-"
    
    const hours = Math.floor(minutes / 60)
    const remainingMinutes = Math.floor(minutes % 60)
    const seconds = Math.floor((minutes % 1) * 60)
    
    const parts = []
    if (hours > 0) parts.push(`${hours}h`)
    if (remainingMinutes > 0) parts.push(`${remainingMinutes}m`)
    if (seconds > 0) parts.push(`${seconds}s`)
    
    return parts.join(" ") || "0m"
  }

  // Calculate statistics
  const getPresentCount = () => {
    return (records || []).filter((record) => record.remarks?.toLowerCase() === "present").length
  }

  const getAbsentCount = () => {
    return (records || []).filter((record) => record.remarks?.toLowerCase() === "absent").length
  }

  const getLateCount = () => {
    return (records || []).filter((record) => record.remarks?.toLowerCase() === "late").length
  }

  const getPresentPercentage = () => {
    if (!records || records.length === 0) return 0
    return Math.round((getPresentCount() / records.length) * 100)
  }

  const getPaginatedRecords = () => {
    const startIndex = 0
    const endIndex = filteredRecords.length
    return filteredRecords.slice(startIndex, endIndex)
  }

  // Group records by month for calendar view
  const getMonthlyRecords = () => {
    const monthlyData: Record<string, AttendanceRecord[]> = {}

    filteredRecords.forEach((record) => {
      const monthYear = format(new Date(record.date), "MMMM yyyy")
      if (!monthlyData[monthYear]) {
        monthlyData[monthYear] = []
      }
      monthlyData[monthYear].push(record)
    })

    return monthlyData
  }

  // Generate calendar days for a given month
  const generateCalendarDays = (monthYear: string) => {
    const [month, year] = monthYear.split(" ")
    const date = new Date(`${month} 1, ${year}`)
    const firstDay = date.getDay() // 0 = Sunday, 1 = Monday, etc.
    const daysInMonth = new Date(parseInt(year), date.getMonth() + 1, 0).getDate()
    
    // Get previous month's days to fill the first week
    const prevMonth = new Date(date)
    prevMonth.setMonth(prevMonth.getMonth() - 1)
    const daysInPrevMonth = new Date(prevMonth.getFullYear(), prevMonth.getMonth() + 1, 0).getDate()
    
    const days = []
    
    // Add previous month's days
    for (let i = firstDay - 1; i >= 0; i--) {
      const day = daysInPrevMonth - i
      const prevDate = new Date(prevMonth.getFullYear(), prevMonth.getMonth(), day)
      days.push({
        date: prevDate,
        isCurrentMonth: false,
        record: null
      })
    }
    
    // Add current month's days
    for (let i = 1; i <= daysInMonth; i++) {
      const currentDate = new Date(date.getFullYear(), date.getMonth(), i)
      const record = monthlyRecords[monthYear]?.find(r => 
        format(new Date(r.date), "yyyy-MM-dd") === format(currentDate, "yyyy-MM-dd")
      )
      days.push({
        date: currentDate,
        isCurrentMonth: true,
        record
      })
    }
    
    // Add next month's days to complete the last week
    const remainingDays = 42 - days.length // 6 rows * 7 days = 42
    for (let i = 1; i <= remainingDays; i++) {
      const nextDate = new Date(date.getFullYear(), date.getMonth() + 1, i)
      days.push({
        date: nextDate,
        isCurrentMonth: false,
        record: null
      })
    }
    
    return days
  }

  const monthlyRecords = getMonthlyRecords()

  // Get streak data
  const getCurrentStreak = () => {
    if (!records.length) return 0

    let streak = 0
    const sortedRecords = [...records].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())

    for (const record of sortedRecords) {
      if (record.remarks?.toLowerCase() === "present") {
        streak++
      } else {
        break
      }
    }

    return streak
  }

  const getWeekdayDistribution = () => {
    const distribution = {
      Monday: 0,
      Tuesday: 0,
      Wednesday: 0,
      Thursday: 0,
      Friday: 0,
      Saturday: 0,
      Sunday: 0,
    }

    records.forEach((record) => {
      const day = format(new Date(record.date), "EEEE")
      if (record.remarks?.toLowerCase() === "present") {
        distribution[day as keyof typeof distribution]++
      }
    })

    return distribution
  }

  const weekdayDistribution = getWeekdayDistribution()
  const bestDay = Object.entries(weekdayDistribution).sort((a, b) => b[1] - a[1])[0]

  return (
    <div className="min-h-screen bg-gradient-to-br from-teal-50 via-cyan-50 to-sky-50 dark:from-slate-900 dark:via-teal-950 dark:to-slate-950 p-4 md:p-6">
      <Toaster
        position="top-right"
        richColors
        className="mt-24"
        style={{
          top: "6rem",
          right: "1rem",
        }}
      />
      <div className="w-full max-w-6xl mx-auto space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-2">
              <div className="h-12 w-12 bg-gradient-to-br from-teal-400 to-cyan-500 rounded-full flex items-center justify-center mr-2 shadow-lg shadow-teal-200 dark:shadow-teal-900/30">
                <User className="h-6 w-6 text-white" />
              </div>
              My Attendance Journey
            </h1>
            <p className="text-slate-600 dark:text-slate-300 mt-1 text-lg">
              Track your work rhythm and attendance patterns
            </p>
          </div>
          <Button
            onClick={fetchRecords}
            className="bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-600 hover:to-cyan-600 text-white transition-all duration-200 shadow-md hover:shadow-lg rounded-full px-6"
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh
          </Button>
        </div>

        {/* Attendance Progress Card */}
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
                {getPresentPercentage()}% Present Rate
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
                    {loading ? "..." : getPresentCount()}
                  </span>
                </div>
                <div className="h-3 w-full bg-slate-100 dark:bg-slate-700 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-gradient-to-r from-emerald-400 to-teal-500 rounded-full transition-all duration-1000 ease-out"
                    style={{ width: `${loading ? 0 : (getPresentCount() / totalRecords) * 100}%` }}
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
                    {loading ? "..." : getAbsentCount()}
                  </span>
                </div>
                <div className="h-3 w-full bg-slate-100 dark:bg-slate-700 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-gradient-to-r from-rose-400 to-pink-500 rounded-full transition-all duration-1000 ease-out"
                    style={{ width: `${loading ? 0 : (getAbsentCount() / totalRecords) * 100}%` }}
                  ></div>
                </div>
              </div>

              <div className="space-y-3">
                <div className="flex justify-between mb-1">
                  <span className="text-sm font-medium text-slate-600 dark:text-slate-300 flex items-center">
                    <Clock className="h-4 w-4 mr-1.5 text-amber-500" />
                    Late
                  </span>
                  <span className="text-sm font-medium text-amber-600 dark:text-amber-400">
                    {loading ? "..." : getLateCount()}
                  </span>
                </div>
                <div className="h-3 w-full bg-slate-100 dark:bg-slate-700 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-gradient-to-r from-amber-400 to-orange-500 rounded-full transition-all duration-1000 ease-out"
                    style={{ width: `${loading ? 0 : (getLateCount() / totalRecords) * 100}%` }}
                  ></div>
                </div>
              </div>
            </div>

            <div className="mt-8 pt-6 border-t border-slate-100 dark:border-slate-700">
              <div className="flex flex-wrap gap-3">
                <Badge className="bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-400 py-1.5 px-3">
                  <CheckCircle className="h-3.5 w-3.5 mr-1.5" />
                  {getPresentCount()} Present Days
                </Badge>
                <Badge className="bg-rose-100 text-rose-800 dark:bg-rose-900/30 dark:text-rose-400 py-1.5 px-3">
                  <XCircle className="h-3.5 w-3.5 mr-1.5" />
                  {getAbsentCount()} Absent Days
                </Badge>
                <Badge className="bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400 py-1.5 px-3">
                  <Clock className="h-3.5 w-3.5 mr-1.5" />
                  {getLateCount()} Late Days
                </Badge>
                {records.some((r) => r.overtimeHours) && (
                  <Badge className="bg-sky-100 text-sky-800 dark:bg-sky-900/30 dark:text-sky-400 py-1.5 px-3">
                    <Zap className="h-3.5 w-3.5 mr-1.5" />
                    Overtime Hours
                  </Badge>
                )}
              </div>
            </div>
          </CardContent>
        </Card>

        {/* View Tabs */}
        <div className="flex overflow-x-auto space-x-2 pb-2">
          <Button
            variant={activeTab === "calendar" ? "default" : "outline"}
            className={cn(
              "rounded-full px-6",
              activeTab === "calendar"
                ? "bg-gradient-to-r from-teal-500 to-cyan-500 text-white"
                : "border-slate-200 dark:border-slate-700",
            )}
            onClick={() => setActiveTab("calendar")}
          >
            <Calendar className="h-4 w-4 mr-2" />
            Calendar View
          </Button>
          <Button
            variant={activeTab === "table" ? "default" : "outline"}
            className={cn(
              "rounded-full px-6",
              activeTab === "table"
                ? "bg-gradient-to-r from-teal-500 to-cyan-500 text-white"
                : "border-slate-200 dark:border-slate-700",
            )}
            onClick={() => setActiveTab("table")}
          >
            <CalendarDays className="h-4 w-4 mr-2" />
            Table View
          </Button>
        </div>

        {/* Main Attendance Records Card */}
        <Card className="border-none shadow-xl overflow-hidden bg-white dark:bg-slate-800 rounded-xl">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-teal-400 to-cyan-500"></div>
          <CardHeader className="bg-slate-50 dark:bg-slate-900 border-b border-slate-100 dark:border-slate-700">
            <div className="flex flex-col md:flex-row justify-between md:items-center gap-4">
              <div>
                <CardTitle className="text-xl text-slate-800 dark:text-white flex items-center gap-2">
                  <CalendarDays className="h-5 w-5 text-teal-500 dark:text-teal-400" />
                  My Attendance History
                </CardTitle>
                <CardDescription className="text-slate-500 dark:text-slate-400 mt-1">
                  {activeTab === "calendar"
                    ? "Monthly calendar view of your attendance"
                    : "Detailed view of your attendance records"}
                </CardDescription>
              </div>
              <div className="flex items-center gap-2">
                <Badge
                  variant="outline"
                  className="bg-teal-50 text-teal-600 border-teal-200 dark:bg-teal-900/30 dark:text-teal-400 dark:border-teal-800 px-3 py-1.5"
                >
                  {filteredRecords.length} records found
                </Badge>
              </div>
            </div>
          </CardHeader>
          <CardContent className="p-6">
            <div className="flex flex-col md:flex-row gap-4 mb-6">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-slate-500 dark:text-slate-400" />
                <Input
                  placeholder="Search by status..."
                  className="pl-10 border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 focus-visible:ring-teal-500 focus-visible:border-teal-500"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              <div className="flex gap-2">
                <Select value={statusFilter} onValueChange={setStatusFilter}>
                  <SelectTrigger className="w-[180px] border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 focus:ring-teal-500">
                    <div className="flex items-center gap-2">
                      <Filter className="h-4 w-4 text-slate-500 dark:text-slate-400" />
                      <SelectValue placeholder="Filter by status" />
                    </div>
                  </SelectTrigger>
                  <SelectContent className="border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800">
                    <SelectItem value="all">All Status</SelectItem>
                    <SelectItem value="present">Present</SelectItem>
                    <SelectItem value="absent">Absent</SelectItem>
                    <SelectItem value="late">Late</SelectItem>
                    <SelectItem value="leave">Leave</SelectItem>
                    <SelectItem value="half day">Half Day</SelectItem>
                  </SelectContent>
                </Select>
                <Select value={sortBy} onValueChange={(value) => handleSort(value)}>
                  <SelectTrigger className="w-[180px] border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 focus:ring-teal-500">
                    <div className="flex items-center gap-2">
                      <Filter className="h-4 w-4 text-slate-500 dark:text-slate-400" />
                      <SelectValue placeholder="Sort by" />
                    </div>
                  </SelectTrigger>
                  <SelectContent className="border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800">
                    <SelectItem value="date">Date</SelectItem>
                    <SelectItem value="status">Status</SelectItem>
                    <SelectItem value="totalHours">Total Hours</SelectItem>
                  </SelectContent>
                </Select>
                <Button
                  variant="outline"
                  size="icon"
                  onClick={() => setDirection(direction === "asc" ? "desc" : "asc")}
                  className="border-slate-200 dark:border-slate-700 w-10 h-10"
                >
                  {direction === "asc" ? "↑" : "↓"}
                </Button>
              </div>
            </div>

            {error && (
              <div className="mb-6 p-4 border border-rose-200 dark:border-rose-800 rounded-md bg-rose-50 dark:bg-rose-900/20 text-rose-600 dark:text-rose-400">
                <p className="font-medium">Error</p>
                <p className="text-sm">{error}</p>
              </div>
            )}

            {loading ? (
              <div className="space-y-4">
                {Array.from({ length: 5 }).map((_, index) => (
                  <div key={index} className="flex items-center space-x-4">
                    <Skeleton className="h-12 w-full rounded-md" />
                  </div>
                ))}
              </div>
            ) : filteredRecords.length === 0 ? (
              <div className="text-center py-12 border border-dashed border-slate-200 dark:border-slate-700 rounded-lg bg-slate-50 dark:bg-slate-900/50">
                <div className="relative w-20 h-20 mx-auto mb-4">
                  <div className="absolute inset-0 rounded-full bg-gradient-to-r from-teal-400 to-cyan-500 opacity-20 animate-pulse"></div>
                  <div className="absolute inset-2 bg-white dark:bg-slate-800 rounded-full flex items-center justify-center">
                    <AlertCircle className="h-10 w-10 text-slate-500 dark:text-slate-400" />
                  </div>
                </div>
                <h3 className="text-xl font-medium text-slate-800 dark:text-white mb-2">No records found</h3>
                <p className="text-slate-500 dark:text-slate-400 max-w-md mx-auto mb-6">
                  We couldn't find any attendance records matching your current filters. Try adjusting your search
                  criteria.
                </p>
                <Button
                  className="bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-600 hover:to-cyan-600 text-white shadow-md rounded-full px-6"
                  onClick={() => {
                    setSearchTerm("")
                    setStatusFilter("all")
                  }}
                >
                  <RefreshCw className="h-4 w-4 mr-2" />
                  Reset Filters
                </Button>
              </div>
            ) : activeTab === "calendar" ? (
              <div className="space-y-8">
                {Object.entries(monthlyRecords).map(([monthYear, monthRecords]) => {
                  const calendarDays = generateCalendarDays(monthYear)
                  return (
                    <div key={monthYear} className="space-y-4">
                      <h3 className="text-lg font-semibold text-slate-800 dark:text-white flex items-center gap-2">
                        <Calendar className="h-5 w-5 text-teal-500" />
                        {monthYear}
                      </h3>
                      <div className="grid grid-cols-7 gap-2">
                        {["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"].map((day) => (
                          <div
                            key={day}
                            className="text-center text-sm font-medium text-slate-500 dark:text-slate-400 py-2"
                          >
                            {day}
                          </div>
                        ))}
                        {calendarDays.map((day, index) => (
                          <div
                            key={index}
                            className={cn(
                              "p-2 rounded-lg border border-slate-100 dark:border-slate-700 min-h-[100px]",
                              !day.isCurrentMonth && "opacity-50",
                              day.record && "shadow-sm hover:shadow-md transition-shadow duration-200",
                              day.record?.remarks?.toLowerCase() === "present"
                                ? "bg-emerald-50 dark:bg-emerald-900/20"
                                : day.record?.remarks?.toLowerCase() === "absent"
                                  ? "bg-rose-50 dark:bg-rose-900/20"
                                  : day.record?.remarks?.toLowerCase() === "late"
                                    ? "bg-amber-50 dark:bg-amber-900/20"
                                    : day.record?.remarks?.toLowerCase() === "leave"
                                      ? "bg-violet-50 dark:bg-violet-900/20"
                                      : day.record?.remarks?.toLowerCase() === "half day"
                                        ? "bg-sky-50 dark:bg-sky-900/20"
                                        : "bg-white dark:bg-slate-800"
                            )}
                          >
                            <div className="flex flex-col h-full">
                              <div className="text-center mb-2">
                                <span className="text-sm font-medium text-slate-500 dark:text-slate-400">
                                  {format(day.date, "EEE")}
                                </span>
                                <h4 className={cn(
                                  "text-2xl font-bold",
                                  day.isCurrentMonth 
                                    ? "text-slate-800 dark:text-white"
                                    : "text-slate-400 dark:text-slate-600"
                                )}>
                                  {format(day.date, "d")}
                                </h4>
                              </div>

                              {day.record && (
                                <div className="flex-grow flex flex-col items-center justify-center gap-2 mt-2">
                                  <Badge
                                    variant="outline"
                                    className={cn(
                                      "border-2 flex items-center justify-center w-full py-1",
                                      getStatusColor(day.record.remarks)
                                    )}
                                  >
                                    {getStatusIcon(day.record.remarks)}
                                    <span>{day.record.remarks || "-"}</span>
                                  </Badge>

                                  {day.record.clockInTime && (
                                    <div className="text-xs text-slate-600 dark:text-slate-300 flex items-center gap-1">
                                      <Clock className="h-3 w-3" />
                                      {format(new Date(`2000-01-01T${day.record.clockInTime}`), "h:mm a")}
                                    </div>
                                  )}

                                  {day.record.totalHours !== null && (
                                    <div className="text-xs font-medium text-slate-700 dark:text-slate-200 mt-1">
                                      {day.record.totalHours.toFixed(1)} hrs
                                    </div>
                                  )}
                                </div>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )
                })}
              </div>
            ) : (
              <div className="rounded-lg border border-slate-200 dark:border-slate-700 overflow-hidden">
                <Table>
                  <TableHeader className="bg-slate-50 dark:bg-slate-900">
                    <TableRow className="hover:bg-slate-100 dark:hover:bg-slate-800 border-b border-slate-200 dark:border-slate-700">
                      <TableHead className="text-slate-600 dark:text-slate-300 font-medium">Date</TableHead>
                      <TableHead className="text-slate-600 dark:text-slate-300 font-medium">Clock In</TableHead>
                      <TableHead className="text-slate-600 dark:text-slate-300 font-medium">Clock Out</TableHead>
                      <TableHead className="text-slate-600 dark:text-slate-300 font-medium">Total Hours</TableHead>
                      <TableHead className="text-slate-600 dark:text-slate-300 font-medium">Status</TableHead>
                      <TableHead className="text-slate-600 dark:text-slate-300 font-medium">Remarks</TableHead>
                      <TableHead className="text-slate-600 dark:text-slate-300 font-medium">Overtime</TableHead>
                      <TableHead className="text-slate-600 dark:text-slate-300 font-medium">Tardiness</TableHead>
                      <TableHead className="text-slate-600 dark:text-slate-300 font-medium">Under Time</TableHead>
                      <TableHead className="text-slate-600 dark:text-slate-300 font-medium">Approved</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {getPaginatedRecords().map((record, index) => (
                      <TableRow
                        key={record.attendanceId}
                        className={cn(
                          "hover:bg-slate-50 dark:hover:bg-slate-900 border-b border-slate-200 dark:border-slate-700 group transition-colors",
                          index % 2 === 0 ? "bg-white dark:bg-slate-800" : "bg-slate-50/50 dark:bg-slate-900/30",
                        )}
                      >
                        <TableCell className="text-slate-600 dark:text-slate-300 font-medium">
                          {format(new Date(record.date), "MMM dd, yyyy")}
                        </TableCell>
                        <TableCell className="text-slate-600 dark:text-slate-300">
                          {record.clockInTime ? (
                            <Badge
                              variant="outline"
                              className="bg-cyan-50 text-cyan-700 border-cyan-200 dark:bg-cyan-900/30 dark:text-cyan-400 dark:border-cyan-800"
                            >
                              {format(new Date(`2000-01-01T${record.clockInTime}`), "hh:mm a")}
                            </Badge>
                          ) : (
                            "-"
                          )}
                        </TableCell>
                        <TableCell className="text-slate-600 dark:text-slate-300">
                          {record.clockOutTime ? (
                            <Badge
                              variant="outline"
                              className="bg-cyan-50 text-cyan-700 border-cyan-200 dark:bg-cyan-900/30 dark:text-cyan-400 dark:border-cyan-800"
                            >
                              {format(new Date(`2000-01-01T${record.clockOutTime}`), "hh:mm a")}
                            </Badge>
                          ) : (
                            "-"
                          )}
                        </TableCell>
                        <TableCell className="text-slate-600 dark:text-slate-300 font-medium">
                          {record.totalHours?.toFixed(2) || "-"}
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant="outline"
                            className={cn("border-2 flex items-center", getStatusColor(record.status))}
                          >
                            {getStatusIcon(record.status)}
                            <span>{record.status || "-"}</span>
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant="outline"
                            className={cn("border-2 flex items-center", getStatusColor(record.remarks))}
                          >
                            {getStatusIcon(record.remarks)}
                            <span>{record.remarks || "-"}</span>
                          </Badge>
                        </TableCell>
                        <TableCell className="text-slate-600 dark:text-slate-300">
                          {record.overtimeHours ? (
                            <Badge className="bg-teal-100 text-teal-800 dark:bg-teal-900/30 dark:text-teal-400">
                              {record.overtimeHours.toFixed(2)}
                            </Badge>
                          ) : (
                            "-"
                          )}
                        </TableCell>
                        <TableCell className="text-slate-600 dark:text-slate-300 font-medium">
                          {record.tardinessMinutes ? (
                            <Badge className="bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400">
                              {formatTimeFromMinutes(record.tardinessMinutes)}
                            </Badge>
                          ) : (
                            "-"
                          )}
                        </TableCell>
                        <TableCell className="text-slate-600 dark:text-slate-300 font-medium">
                          {record.undertimeMinutes ? (
                            <Badge className="bg-sky-100 text-sky-800 dark:bg-sky-900/30 dark:text-sky-400">
                              {formatTimeFromMinutes(record.undertimeMinutes)}
                            </Badge>
                          ) : (
                            "-"
                          )}
                        </TableCell>
                        <TableCell>
                          {record.approvedByManager ? (
                            <Badge className="bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-400 font-normal">
                              Yes
                            </Badge>
                          ) : (
                            <Badge className="bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400 font-normal">
                              Pending
                            </Badge>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}

            {totalPages > 1 && (
              <div className="mt-6 flex justify-center">
                <Pagination>
                  <PaginationContent>
                    <PaginationItem>
                      <PaginationPrevious
                        onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                        className={cn(
                          "border border-slate-200 dark:border-slate-700",
                          page === 0
                            ? "pointer-events-none opacity-50"
                            : "hover:border-teal-500 dark:hover:border-teal-500 text-slate-600 dark:text-slate-300",
                        )}
                      />
                    </PaginationItem>

                    {Array.from({ length: totalPages }, (_, i) => i).map((pageNum) => (
                      <PaginationItem key={pageNum}>
                        <PaginationLink
                          onClick={() => setPage(pageNum)}
                          isActive={page === pageNum}
                          className={
                            page === pageNum
                              ? "bg-gradient-to-r from-teal-500 to-cyan-500 text-white border-transparent hover:from-teal-600 hover:to-cyan-600"
                              : "text-slate-600 dark:text-slate-300 border border-slate-200 dark:border-slate-700 hover:border-teal-500 dark:hover:border-teal-500"
                          }
                        >
                          {pageNum + 1}
                        </PaginationLink>
                      </PaginationItem>
                    ))}

                    <PaginationItem>
                      <PaginationNext
                        onClick={() => setPage((prev) => Math.min(prev + 1, totalPages - 1))}
                        className={cn(
                          "border border-slate-200 dark:border-slate-700",
                          page === totalPages - 1
                            ? "pointer-events-none opacity-50"
                            : "hover:border-teal-500 dark:hover:border-teal-500 text-slate-600 dark:text-slate-300",
                        )}
                      />
                    </PaginationItem>
                  </PaginationContent>
                </Pagination>
              </div>
            )}

            <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mt-6">
              <div className="flex items-center gap-2">
                <span className="text-sm text-slate-500 dark:text-slate-400">Show</span>
                <Select value={size.toString()} onValueChange={(value) => setSize(Number(value))}>
                  <SelectTrigger className="w-[80px] h-9 border-slate-200 dark:border-slate-700">
                    <SelectValue placeholder="10" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="5">5</SelectItem>
                    <SelectItem value="10">10</SelectItem>
                    <SelectItem value="20">20</SelectItem>
                    <SelectItem value="50">50</SelectItem>
                  </SelectContent>
                </Select>
                <span className="text-sm text-slate-500 dark:text-slate-400">entries</span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
