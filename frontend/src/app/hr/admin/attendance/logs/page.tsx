"use client"

import { useEffect, useState } from "react"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { format } from "date-fns"
import { Search, Clock, RefreshCw, CalendarDays, Filter, Users, CheckCircle, XCircle, AlertCircle } from "lucide-react"
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
  approvedByManager: boolean
}

interface EmployeeProfile {
  employeeId: string
  idNumber: string
  firstName: string
  lastName: string
  email: string
  gender: string
  hireDate: string
  dateOfBirth: string
  address: string
  phoneNumber: string
  maritalStatus: string
  status: boolean
  employmentStatus: string
  departmentId: string
  departmentName: string
  jobId: string
  jobName: string
  roleId: string
  roleName: string
  createdAt: string
}

export default function AttendanceRecordsPage() {
  const router = useRouter()
  const [records, setRecords] = useState<AttendanceRecord[]>([])
  const [employeeProfiles, setEmployeeProfiles] = useState<Record<string, EmployeeProfile>>({})
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

  const getEmployeeProfile = async (employeeId: string) => {
    try {
      const data = await authService.getEmployeeProfile()
      if (!data) {
        router.push("/")
        return null
      }
      return data
    } catch (err) {
      console.error("Error fetching employee profile:", err)
      if (err instanceof Error) {
        if (err.message.includes("Network error")) {
          setError("Unable to connect to the server. Please check your internet connection and try again.")
        } else if (err.message.includes("Session expired")) {
          setError("Your session has expired. Please log in again.")
        } else {
          setError(`Failed to load profile data: ${err.message}`)
          authService.logout()
          window.location.href = '/'
        }
      } else {
        setError("An unexpected error occurred while loading your profile.")
      }
      return null
    }
  }

  const fetchRecords = async () => {
    try {
      setLoading(true)

      const token = authService.getToken()
      if (!token) {
        router.push("/")
        return
      }

      const response = await fetch(
        `/api/hr/attendance/all?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`,
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
      setRecords(data.content)
      setTotalPages(data.totalPages)
      setTotalRecords(data.totalElements)

      // Fetch employee profiles for all records
      const profiles: Record<string, EmployeeProfile> = {}
      for (const record of data.content) {
        if (!profiles[record.employeeId]) {
          const profile = await getEmployeeProfile(record.employeeId)
          if (profile) {
            profiles[record.employeeId] = profile
          }
        }
      }
      setEmployeeProfiles(profiles)
    } catch (err) {
      setError(err instanceof Error ? err.message : "An error occurred")
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

  const filteredRecords = records.filter((record) => {
    // Apply search filter
    const matchesSearch =
      record.employeeName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      record.remarks?.toLowerCase().includes(searchTerm.toLowerCase())

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
        return "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400"
      case "absent":
        return "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400"
      case "late":
        return "bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400"
      case "half day":
        return "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400"
      case "leave":
        return "bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400"
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
      default:
        return null
    }
  }

  // Calculate statistics
  const getPresentCount = () => {
    return records.filter((record) => record.remarks?.toLowerCase() === "present").length
  }

  const getAbsentCount = () => {
    return records.filter((record) => record.remarks?.toLowerCase() === "absent").length
  }

  const getLateCount = () => {
    return records.filter((record) => record.remarks?.toLowerCase() === "late").length
  }

  const getPresentPercentage = () => {
    if (records.length === 0) return 0
    return Math.round((getPresentCount() / records.length) * 100)
  }

  const getPaginatedRecords = () => {
    const startIndex = 0
    const endIndex = filteredRecords.length
    return filteredRecords.slice(startIndex, endIndex)
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
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
            <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white flex items-center gap-2">
              <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-1 shadow-md">
                <Clock className="h-5 w-5 text-white" />
              </div>
              Attendance Logs
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">View and manage employee attendance logs</p>
          </div>
          <Button
            onClick={fetchRecords}
            className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-md hover:shadow-lg"
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh List
          </Button>
        </div>

        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-md overflow-hidden bg-white dark:bg-[#1F2937] hover:shadow-lg transition-shadow duration-200">
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">Total Records</p>
                  <h3 className="text-3xl font-bold text-[#1F2937] dark:text-white mt-1">
                    {loading ? <Skeleton className="h-9 w-16" /> : totalRecords}
                  </h3>
                </div>
                <div className="h-12 w-12 bg-[#EFF6FF] dark:bg-[#1E3A8A]/30 rounded-full flex items-center justify-center">
                  <CalendarDays className="h-6 w-6 text-[#3B82F6] dark:text-[#3B82F6]" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-md overflow-hidden bg-white dark:bg-[#1F2937] hover:shadow-lg transition-shadow duration-200">
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">Present</p>
                  <h3 className="text-3xl font-bold text-[#1F2937] dark:text-white mt-1">
                    {loading ? <Skeleton className="h-9 w-16" /> : getPresentCount()}
                  </h3>
                </div>
                <div className="h-12 w-12 bg-[#F0FDF4] dark:bg-[#166534]/30 rounded-full flex items-center justify-center">
                  <CheckCircle className="h-6 w-6 text-[#22C55E] dark:text-[#22C55E]" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-md overflow-hidden bg-white dark:bg-[#1F2937] hover:shadow-lg transition-shadow duration-200">
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">Absent</p>
                  <h3 className="text-3xl font-bold text-[#1F2937] dark:text-white mt-1">
                    {loading ? <Skeleton className="h-9 w-16" /> : getAbsentCount()}
                  </h3>
                </div>
                <div className="h-12 w-12 bg-[#FEF2F2] dark:bg-[#991B1B]/30 rounded-full flex items-center justify-center">
                  <XCircle className="h-6 w-6 text-[#EF4444] dark:text-[#EF4444]" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Attendance Rate Card */}
        <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-md overflow-hidden bg-white dark:bg-[#1F2937] hover:shadow-lg transition-shadow duration-200">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
          <CardContent className="p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <Users className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
                <h3 className="text-lg font-semibold text-[#1F2937] dark:text-white">Attendance Rate</h3>
              </div>
              <Badge
                variant="outline"
                className="bg-[#EFF6FF] text-[#3B82F6] border-[#BFDBFE] dark:bg-[#1E3A8A]/30 dark:text-[#3B82F6] dark:border-[#1E3A8A]"
              >
                <CheckCircle className="h-3.5 w-3.5 mr-1.5" />
                {getPresentPercentage()}% Present
              </Badge>
            </div>

            <div className="space-y-3">
              <div className="flex justify-between mb-1">
                <span className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">Present Rate</span>
                <span className="text-sm font-medium text-[#14B8A6] dark:text-[#14B8A6]">
                  {loading ? "..." : `${getPresentPercentage()}%`}
                </span>
              </div>
              <div className="h-3 w-full bg-[#F3F4F6] dark:bg-[#374151] rounded-full overflow-hidden">
                <div
                  className="h-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] rounded-full transition-all duration-1000 ease-out"
                  style={{ width: `${loading ? 0 : getPresentPercentage()}%` }}
                ></div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Main Attendance Records Card */}
        <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-xl overflow-hidden bg-white dark:bg-[#1F2937]">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
          <CardHeader className="bg-[#F9FAFB] dark:bg-[#111827] border-b border-[#E5E7EB] dark:border-[#374151]">
            <div className="flex flex-col md:flex-row justify-between md:items-center gap-4">
              <div>
                <CardTitle className="text-xl text-[#1F2937] dark:text-white flex items-center gap-2">
                  <CalendarDays className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
                  Attendance Directory
                </CardTitle>
                <CardDescription className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
                  Track employee attendance, hours, and status
                </CardDescription>
              </div>
              <div className="flex items-center gap-2">
                <Badge
                  variant="outline"
                  className="bg-[#F0FDFA] text-[#14B8A6] border-[#99F6E4] dark:bg-[#134E4A]/30 dark:text-[#14B8A6] dark:border-[#134E4A] px-3 py-1.5"
                >
                  {filteredRecords.length} records found
                </Badge>
              </div>
            </div>
          </CardHeader>
          <CardContent className="p-6">
            <div className="flex flex-col md:flex-row gap-4 mb-6">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF]" />
                <Input
                  placeholder="Search by employee name or status..."
                  className="pl-10 border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827] focus-visible:ring-[#3B82F6] focus-visible:border-[#3B82F6]"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              <div className="flex gap-2">
                <Select value={statusFilter} onValueChange={setStatusFilter}>
                  <SelectTrigger className="w-[180px] border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827] focus:ring-[#3B82F6]">
                    <div className="flex items-center gap-2">
                      <Filter className="h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF]" />
                      <SelectValue placeholder="Filter by status" />
                    </div>
                  </SelectTrigger>
                  <SelectContent className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#1F2937]">
                    <SelectItem value="all">All Status</SelectItem>
                    <SelectItem value="present">Present</SelectItem>
                    <SelectItem value="absent">Absent</SelectItem>
                    <SelectItem value="late">Late</SelectItem>
                    <SelectItem value="leave">Leave</SelectItem>
                    <SelectItem value="half day">Half Day</SelectItem>
                  </SelectContent>
                </Select>
                <Select value={sortBy} onValueChange={(value) => handleSort(value)}>
                  <SelectTrigger className="w-[180px] border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827] focus:ring-[#3B82F6]">
                    <div className="flex items-center gap-2">
                      <Filter className="h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF]" />
                      <SelectValue placeholder="Sort by" />
                    </div>
                  </SelectTrigger>
                  <SelectContent className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#1F2937]">
                    <SelectItem value="employeeName">Employee Name</SelectItem>
                    <SelectItem value="date">Date</SelectItem>
                    <SelectItem value="status">Status</SelectItem>
                    <SelectItem value="totalHours">Total Hours</SelectItem>
                  </SelectContent>
                </Select>
                <Button
                  variant="outline"
                  size="icon"
                  onClick={() => setDirection(direction === "asc" ? "desc" : "asc")}
                  className="border-[#E5E7EB] dark:border-[#374151] w-10 h-10"
                >
                  {direction === "asc" ? "↑" : "↓"}
                </Button>
              </div>
            </div>

            {error && (
              <div className="mb-6 p-4 border border-red-200 dark:border-red-800 rounded-md bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400">
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
              <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
                <div className="relative w-16 h-16 mx-auto mb-4">
                  <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] opacity-20 animate-pulse"></div>
                  <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                    <AlertCircle className="h-8 w-8 text-[#6B7280] dark:text-[#9CA3AF]" />
                  </div>
                </div>
                <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">No records found</h3>
                <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto mb-6">
                  We couldn't find any attendance records matching your current filters. Try adjusting your search
                  criteria.
                </p>
                <Button
                  className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white shadow-md"
                  onClick={() => {
                    setSearchTerm("")
                    setStatusFilter("all")
                  }}
                >
                  <RefreshCw className="h-4 w-4 mr-2" />
                  Reset Filters
                </Button>
              </div>
            ) : (
              <div className="rounded-lg border border-[#E5E7EB] dark:border-[#374151] overflow-hidden">
                <Table>
                  <TableHeader className="bg-[#F9FAFB] dark:bg-[#111827]">
                    <TableRow className="hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151]">
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Employee</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Date</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Clock In</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Clock Out</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Total Hours</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Status</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Remarks</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Overtime</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Approved</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {getPaginatedRecords().map((record, index) => (
                      <TableRow
                        key={record.attendanceId}
                        className={cn(
                          "hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151] group transition-colors",
                          index % 2 === 0 ? "bg-[#F9FAFB] dark:bg-[#111827]/50" : "",
                        )}
                      >
                        <TableCell className="font-medium text-[#1F2937] dark:text-white">
                          <div className="flex items-center gap-2">
                            <div className="h-6 w-1 rounded-full bg-gradient-to-b from-[#3B82F6] to-[#14B8A6] transition-all duration-300 group-hover:h-full"></div>
                            <div>
                              <div className="font-medium">{record.employeeName}</div>
                              <div className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                                {employeeProfiles[record.employeeId]?.email || record.employeeEmail}
                              </div>
                            </div>
                          </div>
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          {format(new Date(record.date), "MMM dd, yyyy")}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          {record.clockInTime ? (
                            <Badge
                              variant="outline"
                              className="bg-[#e8f3fa] text-[#148ab8] border-[#99e3f6] dark:bg-[#134E4A]/30 dark:text-[#14B8A6] dark:border-[#134E4A]"
                            >
                              {format(new Date(`2000-01-01T${record.clockInTime}`), "hh:mm a")}
                            </Badge>
                          ) : (
                            "-"
                          )}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          {record.clockOutTime ? (
                            <Badge
                              variant="outline"
                              className="bg-[#e8f3fa] text-[#148ab8] border-[#99e3f6] dark:bg-[#134E4A]/30 dark:text-[#14B8A6] dark:border-[#134E4A]"
                            >
                              {format(new Date(`2000-01-01T${record.clockOutTime}`), "hh:mm a")}
                            </Badge>
                          ) : (
                            "-"
                          )}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
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
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                        {record.overtimeHours?.toFixed(2) || "-"}
                        </TableCell>
                        <TableCell>
                          {record.approvedByManager ? (
                            <Badge className="bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400 font-normal">
                              Yes
                            </Badge>
                          ) : (
                            <Badge className="bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-400 font-normal">
                              No
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
                          "border border-[#E5E7EB] dark:border-[#374151]",
                          page === 0
                            ? "pointer-events-none opacity-50"
                            : "hover:border-[#3B82F6] dark:hover:border-[#3B82F6] text-[#4B5563] dark:text-[#D1D5DB]",
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
                              ? "bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] text-white border-transparent hover:from-[#2563EB] hover:to-[#0D9488]"
                              : "text-[#4B5563] dark:text-[#D1D5DB] border border-[#E5E7EB] dark:border-[#374151] hover:border-[#3B82F6] dark:hover:border-[#3B82F6]"
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
                          "border border-[#E5E7EB] dark:border-[#374151]",
                          page === totalPages - 1
                            ? "pointer-events-none opacity-50"
                            : "hover:border-[#3B82F6] dark:hover:border-[#3B82F6] text-[#4B5563] dark:text-[#D1D5DB]",
                        )}
                      />
                    </PaginationItem>
                  </PaginationContent>
                </Pagination>
              </div>
            )}

            <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mt-6">
              <div className="flex items-center gap-2">
                <span className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">Show</span>
                <Select value={size.toString()} onValueChange={(value) => setSize(Number(value))}>
                  <SelectTrigger className="w-[80px] h-9 border-[#E5E7EB] dark:border-[#374151]">
                    <SelectValue placeholder="10" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="5">5</SelectItem>
                    <SelectItem value="10">10</SelectItem>
                    <SelectItem value="20">20</SelectItem>
                    <SelectItem value="50">50</SelectItem>
                  </SelectContent>
                </Select>
                <span className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">entries</span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
