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

export default function AllEmployeesPage() {
  const router = useRouter()
  const [employees, setEmployees] = useState<Employee[]>([])
  const [filteredEmployees, setFilteredEmployees] = useState<Employee[]>([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [departmentFilter, setDepartmentFilter] = useState<string>("all")
  const [currentPage, setCurrentPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const [processingEmployee, setProcessingEmployee] = useState<string | null>(null)
  const itemsPerPage = 10
  const [departments, setDepartments] = useState<Department[]>([])
  const [selectedDepartment, setSelectedDepartment] = useState<string>("")
  const [updatingDepartment, setUpdatingDepartment] = useState<string | null>(null)
  const [isDepartmentDialogOpen, setIsDepartmentDialogOpen] = useState(false)
  const [selectedEmployeeForDepartment, setSelectedEmployeeForDepartment] = useState<Employee | null>(null)
  const [selectedDepartmentId, setSelectedDepartmentId] = useState<string>("")
  const [isProfileDialogOpen, setIsProfileDialogOpen] = useState(false)
  const [selectedEmployeeProfile, setSelectedEmployeeProfile] = useState<Employee | null>(null)
  const [loadingProfile, setLoadingProfile] = useState(false)
  const [profile, setProfile] = useState<Employee | null>(null)
  const [userAccount, setUserAccount] = useState<Employee | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [processingDepartment, setProcessingDepartment] = useState<string | null>(null)
  const [percentages, setPercentages] = useState({
    active: 0,
    inactive: 0,
    total: 0
  })
  const [isConfirmDialogOpen, setIsConfirmDialogOpen] = useState(false)
  const [pendingAction, setPendingAction] = useState<{ employeeId: string; action: 'activate' | 'deactivate' } | null>(null)
  const [isStatusUpdating, setIsStatusUpdating] = useState(false)
  const [isProfileLoading, setIsProfileLoading] = useState(false)
  const [lastLoginLoading, setLastLoginLoading] = useState(false)

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await authService.getEmployeeProfile()
        setProfile(data)

        try {
          const userData = await authService.getOAuth2UserInfo()
          console.log('OAuth2 User Info:', userData)
          setUserAccount({
            ...data,
            userId: userData.userId || "N/A",
            emailAddress: userData.email || data.email,
            createdAt: userData.createdAt ? new Date(userData.createdAt).toLocaleString() : "Not available",
            lastLogin: data.lastLogin ? new Date(data.lastLogin).toLocaleString() : "Not available",
            isActive: data.status
          })
        } catch (userErr) {
          console.error("Error fetching OAuth2 user info:", userErr)
          if (data) {
            setUserAccount({
              ...data,
              userId: data.userId || "N/A",
              emailAddress: data.email,
              createdAt: data.createdAt ? new Date(data.createdAt).toLocaleString() : "Not available",
              lastLogin: data.lastLogin ? new Date(data.lastLogin).toLocaleString() : "Not available",
              isActive: data.status
            })
          }
        }
      } catch (err) {
        console.error("Error fetching profile:", err)
        if (err instanceof Error) {
          if (err.message.includes("Network error")) {
            setError("Unable to connect to the server. Please check your internet connection and try again.")
          } else if (err.message.includes("Session expired")) {
            setError("Your session has expired. Please log in again.")
          } else {
            setError(`Failed to load profile data: ${err.message}`)
            authService.logout();
            router.push('/');
          }
        } else {
          setError("An unexpected error occurred while loading your profile.")
        }
      } finally {
        setLoading(false)
      }
    }

    fetchProfile()
  }, [])

  useEffect(() => {
    fetchEmployees()
    fetchDepartments()
  }, [])

  useEffect(() => {
    filterEmployees()
  }, [employees, searchTerm, statusFilter, departmentFilter])

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
            userId: emp.userId || emp.employeeId
          }
        })
      )

      // Filter out null values but keep all employees
      const validEmployees = processedEmployees.filter(emp => emp !== null) as Employee[]
      
      console.log('Processed Employees:', validEmployees);
      console.log('Total Employees:', validEmployees.length);
      console.log('Active Employees:', validEmployees.filter(emp => emp.status).length);
      console.log('Inactive Employees:', validEmployees.filter(emp => !emp.status).length);
      
      // Set employees for display (active only)
      const activeEmployeesForDisplay = validEmployees.filter(emp => emp.status)
      setEmployees(activeEmployeesForDisplay)
      setTotalPages(Math.ceil(activeEmployeesForDisplay.length / itemsPerPage))

      // Calculate total employees and active employees for percentage
      const totalEmployees = validEmployees.length
      const activeEmployees = validEmployees.filter(emp => emp.status).length
      const inactiveEmployees = totalEmployees - activeEmployees

      // Update the percentage state
      setPercentages({
        active: Math.round((activeEmployees / totalEmployees) * 100),
        inactive: Math.round((inactiveEmployees / totalEmployees) * 100),
        total: activeEmployeesForDisplay.length
      })
    } catch (error) {
      console.error("Error fetching employees:", error)
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
      console.error("Error fetching departments:", error)
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

    if (statusFilter !== "all") {
      filtered = filtered.filter((emp) => {
        if (statusFilter === "active") {
          return emp.status;
        } else if (statusFilter === "inactive") {
          return !emp.status;
        }
        return true;
      });
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

  const getActiveCount = () => {
    return employees.filter((emp) => emp.status).length
  }

  const getInactiveCount = () => {
    return employees.filter((emp) => !emp.status).length
  }

  const getActivePercentage = () => {
    // Get all employees including inactive ones
    const allEmployees = employees
    if (allEmployees.length === 0) return 0
    const activeEmployees = allEmployees.filter(emp => emp.status).length
    return Math.round((activeEmployees / allEmployees.length) * 100)
  }

  const handleDepartmentDialogOpen = (employee: Employee) => {
    setSelectedEmployeeForDepartment(employee)
    setSelectedDepartmentId(employee.departmentId)
    setIsDepartmentDialogOpen(true)
  }

  const handleProfileView = async (employee: Employee) => {
    setSelectedEmployeeProfile(employee);
    setIsProfileDialogOpen(true);
    setIsProfileLoading(true);
    setLastLoginLoading(true);

    try {
      // Fetch employee profile
      const profileData = await authService.getEmployeeProfile();
      setProfile(profileData);
      
      // Use existing employee data for status and last login
      setUserAccount({
        ...employee,
        userId: employee.employeeId,
        lastLogin: employee.lastLogin ? new Date(employee.lastLogin).toLocaleString() : "Not available",
        isActive: employee.status
      });
    } catch (error) {
      console.error("Error fetching profile:", error);
      toast.error("Failed to load profile data");
    } finally {
      setIsProfileLoading(false);
      setLastLoginLoading(false);
    }
  };

  const handleProfileUpdate = async (employeeId: string, action: 'activate' | 'deactivate') => {
    if (action === 'deactivate') {
      setPendingAction({ employeeId, action });
      setIsConfirmDialogOpen(true);
      return;
    }

    // Proceed with activation without confirmation
    await performProfileUpdate(employeeId, action);
  };

  const performProfileUpdate = async (employeeId: string, action: 'activate' | 'deactivate') => {
    try {
      setIsStatusUpdating(true);
      const token = authService.getToken();
      if (!token) {
        toast.error("Authentication required");
        return;
      }

      // Find the employee by ID to get their email
      const employee = employees.find(emp => emp.employeeId === employeeId);
      if (!employee) {
        toast.error("Employee not found");
        return;
      }

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/hr/user-accounts/${employee.email}/account/${action}`,
        {
          method: "PUT",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      if (!response.ok) {
        const errorData = await response.json();
        if (response.status === 404) {
          toast.error("User account not found");
        } else {
          throw new Error(errorData.error || `Failed to ${action} account`);
        }
        return;
      }

      const data = await response.json();
      const newStatus = action === 'activate';
      
      // Update user account state
      setUserAccount(prev => prev ? { ...prev, isActive: newStatus } : null);
      
      // Only update employee status when deactivating
      if (action === 'deactivate') {
        setSelectedEmployeeProfile(prev => {
          if (!prev) return prev;
          return {
            ...prev,
            status: false
          };
        });
        // Close the profile dialog after deactivation
        setIsProfileDialogOpen(false);
      }

      toast.success(`Account ${action}d successfully`);
      
      // Refresh the employee list after the change
      await fetchEmployees();
    } catch (error) {
      console.error(`Error ${action}ing account:`, error);
      toast.error(error instanceof Error ? error.message : `Failed to ${action} account`);
    } finally {
      setIsStatusUpdating(false);
    }
  };

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
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">Manage and view all employees in the system</p>
          </div>
          <Button
            onClick={fetchEmployees}
            className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-md hover:shadow-lg"
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh List
          </Button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-md overflow-hidden bg-white dark:bg-[#1F2937] hover:shadow-lg transition-shadow duration-200">
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">Total Employees</p>
                  <h3 className="text-3xl font-bold text-[#1F2937] dark:text-white mt-1">
                    {loading ? <Skeleton className="h-9 w-16" /> : employees.length}
                  </h3>
                </div>
                <div className="h-12 w-12 bg-[#EFF6FF] dark:bg-[#1E3A8A]/30 rounded-full flex items-center justify-center">
                  <Users className="h-6 w-6 text-[#3B82F6] dark:text-[#3B82F6]" />
                </div>
              </div>
            </CardContent>
          </Card>


          <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-md overflow-hidden bg-white dark:bg-[#1F2937] hover:shadow-lg transition-shadow duration-200">
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">Departments</p>
                  <h3 className="text-3xl font-bold text-[#1F2937] dark:text-white mt-1">
                    {loading ? <Skeleton className="h-9 w-16" /> : departments.length}
                  </h3>
                </div>
                <div className="h-12 w-12 bg-[#EFF6FF] dark:bg-[#1E3A8A]/30 rounded-full flex items-center justify-center">
                  <Building2 className="h-6 w-6 text-[#3B82F6] dark:text-[#3B82F6]" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-md overflow-hidden bg-white dark:bg-[#1F2937] hover:shadow-lg transition-shadow duration-200">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
          <CardContent className="p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <Clock className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
                <h3 className="text-lg font-semibold text-[#1F2937] dark:text-white">Activation Status</h3>
              </div>
              <Badge
                variant="outline"
                className="bg-[#EFF6FF] text-[#3B82F6] border-[#BFDBFE] dark:bg-[#1E3A8A]/30 dark:text-[#3B82F6] dark:border-[#1E3A8A]"
              >
                <Sparkles className="h-3.5 w-3.5 mr-1.5" />
                {percentages.active}% Active
              </Badge>
            </div>

            <div className="space-y-3">
              <div className="flex justify-between mb-1">
                <span className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">Active Employees</span>
                <span className="text-sm font-medium text-[#14B8A6] dark:text-[#14B8A6]">
                  {loading ? "..." : `${percentages.active}%`}
                </span>
              </div>
              <div className="h-3 w-full bg-[#F3F4F6] dark:bg-[#374151] rounded-full overflow-hidden">
                <div
                  className="h-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] rounded-full transition-all duration-1000 ease-out"
                  style={{ width: `${loading ? 0 : percentages.active}%` }}
                ></div>
              </div>
            </div>
          </CardContent>
        </Card>

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
                  View and manage all active employees
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
                <Select value={statusFilter} onValueChange={setStatusFilter}>
                  <SelectTrigger className="w-[180px] border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827] focus:ring-[#3B82F6]">
                    <div className="flex items-center gap-2">
                      <Filter className="h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF]" />
                      <SelectValue placeholder="Filter by status" />
                    </div>
                  </SelectTrigger>
                  <SelectContent className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#1F2937]">
                    <SelectItem value="all">All Status</SelectItem>
                    <SelectItem value="active">Active</SelectItem>
                    <SelectItem value="inactive">Inactive</SelectItem>
                  </SelectContent>
                </Select>
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
                    setStatusFilter("all")
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
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Gender</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Contact Number</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Department</TableHead>
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
                            {employee.firstName} {employee.lastName}
                          </div>
                          <div className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">{employee.email}</div>
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          {employee.gender || "Not Specified"}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          {employee.phoneNumber || "Not Provided"}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          <Badge
                            variant="outline"
                            className="bg-[#e8f3fa] text-[#148ab8] border-[#99e3f6] dark:bg-[#134E4A]/30 dark:text-[#14B8A6] dark:border-[#134E4A]"
                          >
                            {employee.departmentName}
                          </Badge>
                          
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
                              onClick={() => handleProfileView(employee)}
                              className="border-[#BFDBFE] text-[#3B82F6] hover:bg-[#EFF6FF] dark:border-[#1E3A8A] dark:text-[#3B82F6] dark:hover:bg-[#1E3A8A]/30"
                            >
                              <UserCheck className="h-3.5 w-3.5 mr-1" />
                              View Profile
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

      <Dialog open={isDepartmentDialogOpen} onOpenChange={setIsDepartmentDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Update Department</DialogTitle>
            <DialogDescription>
              Select a new department for {selectedEmployeeForDepartment?.firstName}{" "}
              {selectedEmployeeForDepartment?.lastName}
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="department" className="text-right">
                Department
              </Label>
              <Select value={selectedDepartmentId} onValueChange={setSelectedDepartmentId}>
                <SelectTrigger className="col-span-3">
                  <SelectValue placeholder="Select a department" />
                </SelectTrigger>
                <SelectContent>
                  {departments.map((department) => (
                    <SelectItem key={department.departmentId} value={department.departmentId}>
                      {department.departmentName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsDepartmentDialogOpen(false)}
              disabled={processingDepartment !== null}
            >
              Cancel
            </Button>
            <Button
              onClick={async () => {
                if (selectedEmployeeForDepartment && selectedDepartmentId) {
                  try {
                    setProcessingDepartment(selectedEmployeeForDepartment.employeeId)
                    const token = authService.getToken()
                    if (!token) {
                      toast.error("Authentication required")
                      return
                    }

                    const response = await fetch(
                      `${process.env.NEXT_PUBLIC_API_URL}/hr/employees/${selectedEmployeeForDepartment.employeeId}/assign-department`,
                      {
                        method: "PUT",
                        headers: {
                          Authorization: `Bearer ${token}`,
                          "Content-Type": "application/json",
                        },
                        body: JSON.stringify({ departmentId: selectedDepartmentId })
                      },
                    )

                    const responseData = await response.json()

                    if (!response.ok) {
                      const errorMessage = responseData.error || "Failed to update department"
                      toast.error(errorMessage)
                      throw new Error(errorMessage)
                    }

                    // Update the employee in the local state
                    setEmployees(prevEmployees => 
                      prevEmployees.map(emp => 
                        emp.employeeId === selectedEmployeeForDepartment.employeeId 
                          ? { 
                              ...emp, 
                              departmentId: responseData.departmentId, 
                              departmentName: responseData.departmentName || departments.find(d => d.departmentId === selectedDepartmentId)?.departmentName || "Unknown Department"
                            }
                          : emp
                      )
                    )

                    // Update the selected employee profile if it's the same employee
                    if (selectedEmployeeProfile?.employeeId === selectedEmployeeForDepartment.employeeId) {
                      setSelectedEmployeeProfile(prev => {
                        if (!prev) return prev;
                        return {
                          ...prev,
                          departmentId: responseData.departmentId,
                          departmentName: responseData.departmentName || departments.find(d => d.departmentId === selectedDepartmentId)?.departmentName || "Unknown Department"
                        }
                      });
                    }

                    toast.success("Department updated successfully")
                    setIsDepartmentDialogOpen(false)
                    setSelectedDepartmentId("")
                    setSelectedEmployeeForDepartment(null)
                  } catch (error) {
                    console.error("Error updating department:", error)
                    toast.error(error instanceof Error ? error.message : "Failed to update department")
                  } finally {
                    setProcessingDepartment(null)
                  }
                }
              }}
              disabled={!selectedDepartmentId || processingDepartment !== null}
              className="bg-[#3B82F6] text-white hover:bg-[#2563EB] dark:bg-[#1E40AF] dark:hover:bg-[#1E3A8A]"
            >
              {processingDepartment ? (
                <div className="flex items-center gap-1">
                  <div className="h-3 w-3 rounded-full border-2 border-white border-t-transparent animate-spin"></div>
                  <span>Updating...</span>
                </div>
              ) : (
                "Update"
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={isProfileDialogOpen} onOpenChange={setIsProfileDialogOpen}>
        <DialogContent className="max-w-[90vw] md:max-w-[85vw] lg:max-w-[1000px] p-0 overflow-hidden h-[90vh] max-h-[800px]">
          <div className="flex flex-col md:flex-row h-full">
            {/* Left sidebar with employee photo and basic info */}
            <div className="w-full md:w-1/3 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] p-6 text-white overflow-y-auto">
              <div className="flex flex-col items-center mb-6">
                <div className="relative group">
                  <div className="w-28 h-28 rounded-full bg-white/20 mb-4 flex items-center justify-center overflow-hidden border-4 border-white/30">
                    <Users className="h-14 w-14 text-white" />
                  </div>
                  <div className="absolute inset-0 rounded-full flex items-center justify-center bg-black/0 group-hover:bg-black/30 opacity-0 group-hover:opacity-100 transition-all duration-200">
                    <Button variant="ghost" size="sm" className="text-white h-8 w-8 rounded-full p-0">
                      <RefreshCw className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
                {selectedEmployeeProfile && (
                  <>
                    <h3 className="text-xl font-bold text-center">
                      {selectedEmployeeProfile.firstName} {selectedEmployeeProfile.lastName}
                    </h3>
                    <p className="text-sm text-white/80 text-center">
                      {selectedEmployeeProfile.jobName || "No Job Title"} at {selectedEmployeeProfile.departmentName}
                    </p>

                    <div className="mt-4 w-full">
                      <Badge
                        className={cn(
                          "w-full justify-center py-1.5 text-sm font-medium",
                          selectedEmployeeProfile.status
                            ? "bg-green-400 text-white"
                            : "bg-red-500 text-white",
                        )}
                      >
                        {selectedEmployeeProfile.status ? (
                          <div className="flex items-center gap-1.5">
                            <CheckCircle className="h-4 w-4" />
                            <span>Employee Active</span>
                          </div>
                        ) : (
                          <div className="flex items-center gap-1.5">
                            <XCircle className="h-4 w-4" />
                            <span>Employee Inactive</span>
                          </div>
                        )}
                      </Badge>
                    </div>
                  </>
                )}
              </div>

              {selectedEmployeeProfile && (
                <div className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <p className="text-xs text-white/60">Employee ID</p>
                      <p className="text-sm font-medium">{selectedEmployeeProfile.employeeId}</p>
                    </div>
                    <div>
                      <p className="text-xs text-white/60">ID Number</p>
                      <p className="text-sm font-medium">{selectedEmployeeProfile.idNumber || "Not provided"}</p>
                    </div>
                    <div>
                      <p className="text-xs text-white/60">Employment Status</p>
                      <p className="text-sm font-medium">{selectedEmployeeProfile.employmentStatus}</p>
                    </div>
                    <div>
                      <p className="text-xs text-white/60">Hire Date</p>
                      <p className="text-sm font-medium text-white dark:text-white">
                        {selectedEmployeeProfile.hireDate || "Not provided"}
                      </p>
                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* Right side with detailed information */}
            <div className="w-full md:w-2/3 p-6 overflow-y-auto max-h-[calc(90vh-2rem)]">
              <DialogHeader className="mb-6 text-left">
                <div>
                  <DialogTitle className="text-2xl font-bold text-[#1F2937] dark:text-white">Employee Profile</DialogTitle>
                  <DialogDescription className="text-[#6B7280] dark:text-[#9CA3AF]">Detailed information about this employee</DialogDescription>
                </div>
              </DialogHeader>

              {selectedEmployeeProfile && (
                <div className="space-y-6">
                  <div className="bg-[#F9FAFB] dark:bg-[#1F2937] rounded-lg p-4 border border-[#E5E7EB] dark:border-[#374151]">
                    <h3 className="text-sm font-medium text-[#3B82F6] dark:text-[#60A5FA] mb-3 flex items-center gap-2">
                      <Clock className="h-4 w-4" /> Account Information
                    </h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <p className="text-xs text-[#6B7280] dark:text-[#9CA3AF]">Created At</p>
                        <p className="text-sm font-medium text-[#1F2937] dark:text-white">
                          {selectedEmployeeProfile.createdAt
                            ? new Date(selectedEmployeeProfile.createdAt).toLocaleString()
                            : "Not provided"}
                        </p>
                      </div>
                      <div>
                        <p className="text-xs text-[#6B7280] dark:text-[#9CA3AF]">Last Login</p>
                        {lastLoginLoading ? (
                          <div className="flex items-center gap-2">
                            <div className="h-3 w-3 rounded-full border-2 border-[#3B82F6] border-t-transparent animate-spin"></div>
                            <p className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">Loading...</p>
                          </div>
                        ) : (
                          <p className="text-sm font-medium text-[#1F2937] dark:text-white">
                            {userAccount?.lastLogin ? new Date(userAccount.lastLogin).toLocaleString() : "Not available"}
                          </p>
                        )}
                      </div>
                      <div>
                        <p className="text-xs text-[#6B7280] dark:text-[#9CA3AF]">Status</p>
                        <div className="flex items-center gap-2">
                          {isProfileLoading ? (
                            <div className="flex items-center gap-2">
                              <div className="h-3 w-3 rounded-full border-2 border-[#3B82F6] border-t-transparent animate-spin"></div>
                              <p className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">Loading...</p>
                            </div>
                          ) : isStatusUpdating ? (
                            <div className="flex items-center gap-2">
                              <div className="h-3 w-3 rounded-full border-2 border-[#3B82F6] border-t-transparent animate-spin"></div>
                              <p className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">Updating...</p>
                            </div>
                          ) : (
                            <div className="flex items-center gap-2">
                              <p className={cn(
                                "text-sm font-medium",
                                userAccount?.isActive 
                                  ? "text-green-600 dark:text-green-400" 
                                  : "text-red-600 dark:text-red-400"
                              )}>
                                {userAccount?.isActive ? "Active" : "Inactive"}
                              </p>
                              <TooltipProvider>
                                <Tooltip>
                                  <TooltipTrigger asChild>
                                    <Button
                                      variant="ghost"
                                      size="icon"
                                      onClick={() => handleProfileUpdate(selectedEmployeeProfile.employeeId, userAccount?.isActive ? 'deactivate' : 'activate')}
                                      className={cn(
                                        "h-8 w-8 p-0",
                                        userAccount?.isActive
                                          ? "text-red-600 hover:bg-red-50 dark:hover:bg-red-900/30"
                                          : "text-green-600 hover:bg-green-50 dark:hover:bg-green-900/30"
                                      )}
                                    >
                                      {userAccount?.isActive ? (
                                        <UserX className="h-4 w-4" />
                                      ) : (
                                        <UserCheck className="h-4 w-4" />
                                      )}
                                    </Button>
                                  </TooltipTrigger>
                                  <TooltipContent>
                                    <p>{userAccount?.isActive ? "Deactivate Account" : "Activate Account"}</p>
                                  </TooltipContent>
                                </Tooltip>
                              </TooltipProvider>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="bg-[#F9FAFB] dark:bg-[#1F2937] rounded-lg p-4 border border-[#E5E7EB] dark:border-[#374151]">
                    <h3 className="text-sm font-medium text-[#3B82F6] dark:text-[#60A5FA] mb-3 flex items-center gap-2">
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="16"
                        height="16"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        className="lucide lucide-mail"
                      >
                        <rect width="20" height="16" x="2" y="4" rx="2"></rect>
                        <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"></path>
                      </svg>{" "}
                      Contact Information
                    </h3>
                    <div className="grid grid-cols-1 gap-4">
                      <div>
                        <p className="text-xs text-[#6B7280] dark:text-[#9CA3AF]">Email</p>
                        <p className="text-sm font-medium text-[#1F2937] dark:text-white flex items-center gap-2">
                          {selectedEmployeeProfile.email}
                          <Button variant="ghost" size="sm" className="h-6 w-6 p-0 rounded-full">
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              width="14"
                              height="14"
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              strokeWidth="2"
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              className="lucide lucide-copy"
                            >
                              <rect width="14" height="14" x="8" y="8" rx="2" ry="2"></rect>
                              <path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2"></path>
                            </svg>
                          </Button>
                        </p>
                      </div>
                      <div>
                        <p className="text-xs text-[#6B7280] dark:text-[#9CA3AF]">Phone Number</p>
                        <p className="text-sm font-medium text-[#1F2937] dark:text-white flex items-center gap-2">
                          {selectedEmployeeProfile.phoneNumber || "Not provided"}
                          {selectedEmployeeProfile.phoneNumber && (
                            <Button variant="ghost" size="sm" className="h-6 w-6 p-0 rounded-full">
                              <svg
                                xmlns="http://www.w3.org/2000/svg"
                                width="14"
                                height="14"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="2"
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                className="lucide lucide-copy"
                              >
                                <rect width="14" height="14" x="8" y="8" rx="2" ry="2"></rect>
                                <path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2"></path>
                              </svg>
                            </Button>
                          )}
                        </p>
                      </div>
                      <div>
                        <p className="text-xs text-[#6B7280] dark:text-[#9CA3AF]">Address</p>
                        <p className="text-sm font-medium text-[#1F2937] dark:text-white">
                          {selectedEmployeeProfile.address || "Not provided"}
                        </p>
                      </div>
                      <div>
                        <p className="text-xs text-[#6B7280] dark:text-[#9CA3AF]">Emergency Contact</p>
                        <p className="text-sm font-medium text-[#1F2937] dark:text-white">Not provided</p>
                      </div>
                    </div>
                  </div>

                  <div className="bg-[#F9FAFB] dark:bg-[#1F2937] rounded-lg p-4 border border-[#E5E7EB] dark:border-[#374151]">
                    <h3 className="text-sm font-medium text-[#3B82F6] dark:text-[#60A5FA] mb-3 flex items-center gap-2">
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="16"
                        height="16"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        className="lucide lucide-briefcase"
                      >
                        <rect width="20" height="14" x="2" y="7" rx="2" ry="2"></rect>
                        <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                      </svg>{" "}
                      Employment Information
                    </h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <p className="text-xs text-[#6B7280] dark:text-[#9CA3AF]">Department</p>
                        <div className="flex items-center gap-2">
                        <p className="text-sm font-medium text-[#1F2937] dark:text-white">
                          {selectedEmployeeProfile.departmentName}
                        </p>
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleDepartmentDialogOpen(selectedEmployeeProfile)}
                            disabled={updatingDepartment === selectedEmployeeProfile.employeeId}
                            className="ml-2 px-2 py-1 h-7 text-xs border-[#BFDBFE] text-[#3B82F6] hover:bg-[#EFF6FF] dark:border-[#1E3A8A] dark:text-[#3B82F6] dark:hover:bg-[#1E3A8A]/30"
                          >
                            {updatingDepartment === selectedEmployeeProfile.employeeId ? (
                              <div className="flex items-center gap-1">
                                <div className="h-2.5 w-2.5 rounded-full border-2 border-[#3B82F6] border-t-transparent animate-spin"></div>
                                <span className="text-xs">Processing...</span>
                              </div>
                            ) : (
                              <div className="flex items-center gap-1">
                                <span className="text-xs">Update</span>
                              </div>
                            )}
                          </Button>
                          </div>
                      </div>
                      <div>
                        <p className="text-xs text-[#6B7280] dark:text-[#9CA3AF]">Job Title</p>
                        <p className="text-sm font-medium text-[#1F2937] dark:text-white">
                          {selectedEmployeeProfile.jobName || "Not assigned"}
                        </p>
                      </div>
                      <div>
                        <p className="text-xs text-[#6B7280] dark:text-[#9CA3AF]">Hire Date</p>
                        <p className="text-sm font-medium text-[#1F2937] dark:text-white">
                          {selectedEmployeeProfile.hireDate || "Not provided"}
                        </p>
                      </div>
                      <div>
                        <p className="text-xs text-[#6B7280] dark:text-[#9CA3AF]">Employment Type</p>
                        <p className="text-sm font-medium text-[#1F2937] dark:text-white">
                          {selectedEmployeeProfile.employmentStatus}
                        </p>
                      </div>
                      <div>
                        <p className="text-xs text-[#6B7280] dark:text-[#9CA3AF]">Work Experience</p>
                        <p className="text-sm font-medium text-[#1F2937] dark:text-white">
                          {selectedEmployeeProfile.hireDate
                            ? Math.floor(
                                (new Date().getTime() - new Date(selectedEmployeeProfile.hireDate).getTime()) /
                                  (365.25 * 24 * 60 * 60 * 1000),
                              ) + " years"
                            : "Not available"}
                        </p>
                      </div>
                      <div>
                        <p className="text-xs text-[#6B7280] dark:text-[#9CA3AF]">Manager</p>
                        <p className="text-sm font-medium text-[#1F2937] dark:text-white">Not assigned</p>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              <DialogFooter className="mt-6 flex justify-start">
                <Button
                  onClick={() => setIsProfileDialogOpen(false)}
                  className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white"
                >
                  Close
                </Button>
              </DialogFooter>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      <Dialog open={isConfirmDialogOpen} onOpenChange={setIsConfirmDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Confirm Account Deactivation</DialogTitle>
            <DialogDescription>
              Are you sure you want to deactivate this account? All deactivated accounts will not be able to access the system.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsConfirmDialogOpen(false)}
              disabled={isStatusUpdating}
            >
              Cancel
            </Button>
            <Button
              variant="destructive"
              onClick={async () => {
                if (pendingAction) {
                  await performProfileUpdate(pendingAction.employeeId, pendingAction.action);
                  setIsConfirmDialogOpen(false);
                  setPendingAction(null);
                }
              }}
              disabled={isStatusUpdating}
            >
              {isStatusUpdating ? (
                <div className="flex items-center gap-2">
                  <div className="h-4 w-4 rounded-full border-2 border-white border-t-transparent animate-spin"></div>
                  <span>Deactivating...</span>
                </div>
              ) : (
                "Confirm Deactivation"
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
