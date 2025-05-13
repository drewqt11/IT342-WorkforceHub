"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination"
import {
  CheckCircle,
  XCircle,
  Search,
  RefreshCw,
  Users,
  Filter,
  AlertCircle,
  UserCheck,
  UserX,
  Sparkles,
  Clock,
  Shield,
  Building2,
} from "lucide-react"
import { toast } from "sonner"
import { authService } from "@/lib/auth"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"
import { cn } from "@/lib/utils"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Toaster } from "@/components/ui/sonner"
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip"

interface Employee {
  employeeId: string
  idNumber: string
  firstName: string
  lastName: string
  email: string
  phoneNumber: string
  gender: string
  dateOfBirth: string
  address: string
  maritalStatus: string
  hireDate: string
  departmentId: string
  departmentName: string
  jobId: string
  jobName: string
  role: string
  status: boolean
  employmentStatus: string
  createdAt: string
  isActive: boolean
  userId: string
  lastLogin: string
  workTimeIn: string
  workTimeOut: string
  roleId: string
  roleName: string
}

interface Department {
  departmentId: string
  departmentName: string
  description?: string
}

const formatDisplayText = (text: string): string => {
  if (!text) return "Not provided"
  return text
    .split(" ")
    .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join(" ")
}

const formatTimeToPST = (time: string): string => {
  if (!time) return "Not set"
  
  try {
    // Create a date object for today with the given time
    const [hours, minutes] = time.split(':')
    const date = new Date()
    date.setHours(parseInt(hours), parseInt(minutes))
    
    // Format to PST 12-hour time
    return date.toLocaleTimeString('en-US', {
      timeZone: 'America/Los_Angeles',
      hour: 'numeric',
      minute: '2-digit',
      hour12: true
    })
  } catch (error) {
    return "Not set"
  }
}

export default function AllEmployeesPage() {
  const router = useRouter()
  const [employees, setEmployees] = useState<Employee[]>([])
  const [filteredEmployees, setFilteredEmployees] = useState<Employee[]>([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState("")
  const [departmentFilter, setDepartmentFilter] = useState<string>("all")
  const [currentPage, setCurrentPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const [departments, setDepartments] = useState<Department[]>([])
  const [isScheduleDialogOpen, setIsScheduleDialogOpen] = useState(false)
  const [selectedEmployeeForSchedule, setSelectedEmployeeForSchedule] = useState<Employee | null>(null)
  const [workTimeIn, setWorkTimeIn] = useState("")
  const [workTimeOut, setWorkTimeOut] = useState("")
  const [isUpdatingSchedule, setIsUpdatingSchedule] = useState(false)
  const itemsPerPage = 10

  useEffect(() => {
    fetchEmployees()
    fetchDepartments()
  }, [])

  useEffect(() => {
    filterEmployees()
  }, [employees, searchTerm, departmentFilter])

  const fetchEmployees = async () => {
    try {
      setLoading(true)
      const token = authService.getToken()

      if (!token) {
        router.push("/")
        toast.error("Authentication required. Please log in.")
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/employees`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        throw new Error("Failed to fetch employees")
      }

      const data = await response.json()
      const employeesData = Array.isArray(data) ? data : data.employees || data.content || []

      // Process employees and check their active status
      const processedEmployees = await Promise.all(
        employeesData.map(async (emp: any) => {
          return {
            employeeId: emp.employeeId || "",
            idNumber: emp.idNumber || "Not provided",
            firstName: emp.firstName || "",
            lastName: emp.lastName || "",
            email: emp.email || "",
            phoneNumber: emp.phoneNumber || "Not provided",
            gender: emp.gender || "Not specified",
            dateOfBirth: emp.dateOfBirth ? new Date(emp.dateOfBirth).toLocaleDateString() : "Not provided",
            address: emp.address || "Not provided",
            maritalStatus: emp.maritalStatus || "Not specified",
            hireDate: emp.hireDate ? new Date(emp.hireDate).toLocaleDateString() : "Not provided",
            departmentId: emp.departmentId || "",
            departmentName: emp.departmentName || "Unassigned",
            jobId: emp.jobId || "",
            jobName: emp.jobName || "",
            role: emp.roleId || emp.role || "ROLE_EMPLOYEE",
            status: emp.status || false,
            employmentStatus: emp.employmentStatus || "INACTIVE",
            createdAt: emp.createdAt ? new Date(emp.createdAt).toLocaleString() : "Not provided",
            isActive: emp.status || false,
            lastLogin: emp.lastLogin ? new Date(emp.lastLogin).toLocaleString() : "Not available",
            userId: emp.userId || emp.employeeId,
            workTimeIn: emp.workTimeIn || "",
            workTimeOut: emp.workTimeOut || ""
          }
        })
      )

      // Filter out null values but keep all employees
      const validEmployees = processedEmployees.filter(emp => emp !== null) as Employee[]
      
      // Set employees for display (active only)
      const activeEmployeesForDisplay = validEmployees.filter(emp => emp.status)
      setEmployees(activeEmployeesForDisplay)
      setTotalPages(Math.ceil(activeEmployeesForDisplay.length / itemsPerPage))
    } catch (error) {
      toast.error("Failed to load employees. Please try again.")
    } finally {
      setLoading(false)
    }
  }

  const fetchDepartments = async () => {
    try {
      const token = authService.getToken()
      if (!token) {
        router.push("/")
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/departments`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        throw new Error("Failed to fetch departments")
      }

      const data = await response.json()
      setDepartments(data)
    } catch (error) {
      toast.error("Failed to load departments. Please try again.")
    }
  }

  const filterEmployees = () => {
    let filtered = [...employees]

    // Filter out inactive employees first
    filtered = filtered.filter(emp => emp.status)

    if (searchTerm) {
      const searchLower = searchTerm.toLowerCase()
      filtered = filtered.filter(
        (emp) =>
          emp.firstName.toLowerCase().includes(searchLower) ||
          emp.lastName.toLowerCase().includes(searchLower) ||
          emp.email.toLowerCase().includes(searchLower) ||
          emp.employeeId.toLowerCase().includes(searchLower),
      )
    }

    if (departmentFilter !== "all") {
      filtered = filtered.filter((emp) => emp.departmentId === departmentFilter)
    }

    setFilteredEmployees(filtered)
    setTotalPages(Math.ceil(filtered.length / itemsPerPage))
    setCurrentPage(1)
  }

  const getPaginatedEmployees = () => {
    const startIndex = (currentPage - 1) * itemsPerPage
    const endIndex = startIndex + itemsPerPage
    return filteredEmployees.slice(startIndex, endIndex)
  }

  const handleScheduleDialogOpen = (employee: Employee) => {
    setSelectedEmployeeForSchedule(employee)
    setWorkTimeIn(employee.workTimeIn || "")
    setWorkTimeOut(employee.workTimeOut || "")
    setIsScheduleDialogOpen(true)
  }

  const handleScheduleUpdate = async () => {
    if (!selectedEmployeeForSchedule) return

    try {
      setIsUpdatingSchedule(true)
      const token = authService.getToken()
      if (!token) {
        toast.error("Authentication required")
        return
      }

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/hr/employees/${selectedEmployeeForSchedule.employeeId}/work-schedule?workTimeIn=${workTimeIn}&workTimeOut=${workTimeOut}`,
        {
          method: "PATCH",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      )

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.error || "Failed to update work schedule")
      }

      // Update the employee in the local state
      setEmployees(prevEmployees =>
        prevEmployees.map(emp =>
          emp.employeeId === selectedEmployeeForSchedule.employeeId
            ? {
                ...emp,
                workTimeIn,
                workTimeOut,
              }
            : emp
        )
      )

      toast.success("Work schedule updated successfully")
      setIsScheduleDialogOpen(false)
      setSelectedEmployeeForSchedule(null)
      setWorkTimeIn("")
      setWorkTimeOut("")
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Failed to update work schedule")
    } finally {
      setIsUpdatingSchedule(false)
    }
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
                <Shield className="h-5 w-5 text-white" />
              </div>
              All Employees
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">Update employee work schedules</p>
          </div>
          <Button
            onClick={fetchEmployees}
            className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-md hover:shadow-lg"
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh List
          </Button>
        </div>


        <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-xl overflow-hidden bg-white dark:bg-[#1F2937]">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
          <CardHeader className="bg-[#F9FAFB] dark:bg-[#111827] border-b border-[#E5E7EB] dark:border-[#374151]">
            <div className="flex flex-col md:flex-row justify-between md:items-center gap-4">
              <div>
                <CardTitle className="text-xl text-[#1F2937] dark:text-white flex items-center gap-2">
                  <Users className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
                  Employee Directory
                </CardTitle>
                <CardDescription className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
                  Set and update employee work schedules
                </CardDescription>
              </div>
              <div className="flex items-center gap-2">
                <Badge
                  variant="outline"
                  className="bg-[#F0FDFA] text-[#14B8A6] border-[#99F6E4] dark:bg-[#134E4A]/30 dark:text-[#14B8A6] dark:border-[#134E4A] px-3 py-1.5"
                >
                  {filteredEmployees.length} employees found
                </Badge>
              </div>
            </div>
          </CardHeader>
          <CardContent className="p-6">
            <div className="flex flex-col md:flex-row gap-4 mb-6">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF]" />
                <Input
                  placeholder="Search by name, ID, or email..."
                  className="pl-10 border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827] focus-visible:ring-[#3B82F6] focus-visible:border-[#3B82F6]"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              <div className="flex gap-2">
                <Select value={departmentFilter} onValueChange={setDepartmentFilter}>
                  <SelectTrigger className="w-[180px] border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827] focus:ring-[#3B82F6]">
                    <div className="flex items-center gap-2">
                      <Building2 className="h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF]" />
                      <SelectValue placeholder="Filter by department" />
                    </div>
                  </SelectTrigger>
                  <SelectContent className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#1F2937]">
                    <SelectItem value="all">All Departments</SelectItem>
                    {departments.map((dept) => (
                      <SelectItem key={dept.departmentId} value={dept.departmentId}>
                        {dept.departmentName}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            {loading ? (
              <div className="space-y-4">
                {Array.from({ length: 5 }).map((_, index) => (
                  <div key={index} className="flex items-center space-x-4">
                    <Skeleton className="h-12 w-full rounded-md" />
                  </div>
                ))}
              </div>
            ) : filteredEmployees.length === 0 ? (
              <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
                <div className="relative w-16 h-16 mx-auto mb-4">
                  <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] opacity-20 animate-pulse"></div>
                  <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                    <AlertCircle className="h-8 w-8 text-[#6B7280] dark:text-[#9CA3AF]" />
                  </div>
                </div>
                <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">No employees found</h3>
                <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto mb-6">
                  We couldn't find any employees matching your current filters. Try adjusting your search criteria.
                </p>
                <Button
                  className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white shadow-md"
                  onClick={() => {
                    setSearchTerm("")
                    setDepartmentFilter("all")
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
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Employee ID</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">ID Number</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Name</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Department</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Job Title</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Work Time</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Status</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {getPaginatedEmployees().map((employee, index) => (
                      <TableRow
                        key={employee.employeeId}
                        className={cn(
                          "hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151] group transition-colors",
                          index % 2 === 0 ? "bg-[#F9FAFB] dark:bg-[#111827]/50" : "",
                        )}
                      >
                        <TableCell className="font-medium text-[#1F2937] dark:text-white">
                          <div className="flex items-center gap-2">
                            <div className="h-6 w-1 rounded-full bg-gradient-to-b from-[#3B82F6] to-[#14B8A6] transition-all duration-300 group-hover:h-full"></div>
                            {employee.employeeId}
                          </div>
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          {employee.idNumber || "Not provided"}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          <div className="font-medium">
                            {formatDisplayText(employee.firstName)} {formatDisplayText(employee.lastName)}
                          </div>
                          <div className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">{employee.email}</div>
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          <Badge
                            variant="outline"
                            className="bg-[#e8f3fa] text-[#148ab8] border-[#99e3f6] dark:bg-[#134E4A]/30 dark:text-[#14B8A6] dark:border-[#134E4A]"
                          >
                            {formatDisplayText(employee.departmentName)}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          {formatDisplayText(employee.jobName)}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          <div className="flex flex-col gap-1">
                            <div className="flex items-center gap-1">
                              <Clock className="h-3.5 w-3.5 text-[#3B82F6]" />
                              <span className="text-sm">
                                {formatTimeToPST(employee.workTimeIn)} - {formatTimeToPST(employee.workTimeOut)}
                              </span>
                            </div>
                          </div>
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant="outline"
                            className={cn(
                              "border-2",
                              employee.status
                                ? "bg-[#e1fff8] text-[#14b83a] border-[#02da51] dark:bg-[#134E4A]/30 dark:text-[#14B8A6] dark:border-[#134E4A]"
                                : "bg-[#FEF2F2] text-[#EF4444] border-[#FECACA] dark:bg-[#7F1D1D]/30 dark:text-[#EF4444] dark:border-[#7F1D1D]",
                            )}
                          >
                            {employee.status ? (
                              <div className="flex items-center gap-1">
                                <CheckCircle className="h-3.5 w-3.5" />
                                <span>Active</span>
                              </div>
                            ) : (
                              <div className="flex items-center gap-1">
                                <XCircle className="h-3.5 w-3.5" />
                                <span>Inactive</span>
                              </div>
                            )}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleScheduleDialogOpen(employee)}
                              className="border-[#BFDBFE] text-[#3B82F6] hover:bg-[#EFF6FF] dark:border-[#1E3A8A] dark:text-[#3B82F6] dark:hover:bg-[#1E3A8A]/30"
                            >
                              <Clock className="h-3.5 w-3.5 mr-1" />
                              Update Schedule
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>
        {totalPages > 1 && (
          <div className="mt-6 flex justify-center">
            <Pagination>
              <PaginationContent>
                <PaginationItem>
                  <PaginationPrevious
                    onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
                    className={cn(
                      "border border-[#E5E7EB] dark:border-[#374151]",
                      currentPage === 1
                        ? "pointer-events-none opacity-50"
                        : "hover:border-[#3B82F6] dark:hover:border-[#3B82F6] text-[#4B5563] dark:text-[#D1D5DB]",
                    )}
                  />
                </PaginationItem>

                {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
                  <PaginationItem key={page}>
                    <PaginationLink
                      onClick={() => setCurrentPage(page)}
                      isActive={currentPage === page}
                      className={
                        currentPage === page
                          ? "bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] text-white border-transparent hover:from-[#2563EB] hover:to-[#0D9488]"
                          : "text-[#4B5563] dark:text-[#D1D5DB] border border-[#E5E7EB] dark:border-[#374151] hover:border-[#3B82F6] dark:hover:border-[#3B82F6]"
                      }
                    >
                      {page}
                    </PaginationLink>
                  </PaginationItem>
                ))}

                <PaginationItem>
                  <PaginationNext
                    onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))}
                    className={cn(
                      "border border-[#E5E7EB] dark:border-[#374151]",
                      currentPage === totalPages
                        ? "pointer-events-none opacity-50"
                        : "hover:border-[#3B82F6] dark:hover:border-[#3B82F6] text-[#4B5563] dark:text-[#D1D5DB]",
                    )}
                  />
                </PaginationItem>
              </PaginationContent>
            </Pagination>
          </div>
        )}
      </div>

      <Dialog open={isScheduleDialogOpen} onOpenChange={setIsScheduleDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Update Work Schedule</DialogTitle>
            <DialogDescription>
              Set work schedule for {selectedEmployeeForSchedule ? `${formatDisplayText(selectedEmployeeForSchedule.firstName)} ${formatDisplayText(selectedEmployeeForSchedule.lastName)}` : ""}
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="workTimeIn" className="text-right">
                Work Time In
              </Label>
              <Input
                id="workTimeIn"
                type="time"
                value={workTimeIn}
                onChange={(e) => setWorkTimeIn(e.target.value)}
                className="col-span-3"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="workTimeOut" className="text-right">
                Work Time Out
              </Label>
              <Input
                id="workTimeOut"
                type="time"
                value={workTimeOut}
                onChange={(e) => setWorkTimeOut(e.target.value)}
                className="col-span-3"
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsScheduleDialogOpen(false)}
              disabled={isUpdatingSchedule}
            >
              Cancel
            </Button>
            <Button
              onClick={handleScheduleUpdate}
              disabled={!workTimeIn || !workTimeOut || isUpdatingSchedule}
              className="bg-[#3B82F6] text-white hover:bg-[#2563EB] dark:bg-[#1E40AF] dark:hover:bg-[#1E3A8A]"
            >
              {isUpdatingSchedule ? (
                <div className="flex items-center gap-1">
                  <div className="h-3 w-3 rounded-full border-2 border-white border-t-transparent animate-spin"></div>
                  <span>Updating...</span>
                </div>
              ) : (
                "Update Schedule"
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
