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
  userId?: string
  lastLogin?: string
  workTimeIn?: string
  workTimeOut?: string
}

interface Department {
  departmentId: string
  departmentName: string
  description?: string
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
  workTimeIn: string
  workTimeOut: string
  userId: string
  lastLogin: string
  isActive: boolean
}

interface UserAccountInfo {
  userId: string
  email: string
  createdAt: string
  lastLogin: string
}

export default function DeactivatedAccountsPage() {
  const router = useRouter()
  const [employees, setEmployees] = useState<Employee[]>([])
  const [loading, setLoading] = useState(true)
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
  const [userAccount, setUserAccount] = useState<UserAccountInfo | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [processingDepartment, setProcessingDepartment] = useState<string | null>(null)

  useEffect(() => {
    fetchEmployees()
    fetchDepartments()
  }, [])

  const fetchEmployees = async () => {
    try {
      setLoading(true)
      const token = authService.getToken()

      if (!token) {
        router.push("/")
        toast.error("Authentication required. Please log in.")
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/employees/accounts/deactivated`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        throw new Error("Failed to fetch employees")
      }

      const data = await response.json()
      const employeesData = Array.isArray(data) ? data : data.employees || data.content || []

      // Process employees
      const processedEmployees = employeesData.map((emp: any) => ({
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
        userId: emp.userId || "",
        lastLogin: emp.lastLogin || "",
        workTimeIn: emp.workTimeIn || "",
        workTimeOut: emp.workTimeOut || ""
      }))

      setEmployees(processedEmployees as Employee[])
      setTotalPages(Math.ceil(processedEmployees.length / itemsPerPage))
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

  const handleDepartmentDialogOpen = (employee: Employee) => {
    setSelectedEmployeeForDepartment(employee)
    setSelectedDepartmentId(employee.departmentId)
    setIsDepartmentDialogOpen(true)
  }

  const handleProfileView = async (employee: Employee) => {
    setSelectedEmployeeProfile(employee);
    setIsProfileDialogOpen(true);
    setLoadingProfile(true);

    try {
      // Fetch employee profile
      const profileData = await authService.getEmployeeProfile();
      setProfile(profileData);

      // Update user account with the last login time
      setUserAccount({
        userId: employee.employeeId,
        email: employee.email,
        createdAt: employee.createdAt || new Date().toISOString(),
        lastLogin: employee.lastLogin || new Date().toISOString()
      });
    } catch (error) {
      console.error("Error fetching profile:", error);
      toast.error("Failed to load profile data");
    } finally {
      setLoadingProfile(false);
    }
  };

  const handleProfileUpdate = async (employeeId: string, action: 'activate' | 'deactivate') => {
    try {
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
      
      // Update employee status when deactivating
      if (action === 'deactivate') {
        setSelectedEmployeeProfile(prev => {
          if (!prev) return prev;
          return {
            ...prev,
            status: false
          };
        });
      }

      toast.success(`Account ${action}d successfully`);
      
      // Refresh the employee list after the change
      await fetchEmployees();
    } catch (error) {
      console.error(`Error ${action}ing account:`, error);
      toast.error(error instanceof Error ? error.message : `Failed to ${action} account`);
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
              Deactivated Accounts
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">View and manage deactivated employee accounts</p>
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
                  <UserX className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
                  Deactivated Accounts List
                </CardTitle>
                <CardDescription className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
                  View and manage deactivated employee accounts
                </CardDescription>
              </div>
              <div className="flex items-center gap-2">
                <Badge
                  variant="outline"
                  className="bg-[#F0FDFA] text-[#14B8A6] border-[#99F6E4] dark:bg-[#134E4A]/30 dark:text-[#14B8A6] dark:border-[#134E4A] px-3 py-1.5"
                >
                  {employees.length} accounts found
                </Badge>
              </div>
            </div>
          </CardHeader>
          <CardContent className="p-6">
            {loading ? (
              <div className="space-y-4">
                {Array.from({ length: 5 }).map((_, index) => (
                  <div key={index} className="flex items-center space-x-4">
                    <Skeleton className="h-12 w-full rounded-md" />
                  </div>
                ))}
              </div>
            ) : employees.length === 0 ? (
              <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
                <div className="relative w-16 h-16 mx-auto mb-4">
                  <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] opacity-20 animate-pulse"></div>
                  <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                    <AlertCircle className="h-8 w-8 text-[#6B7280] dark:text-[#9CA3AF]" />
                  </div>
                </div>
                <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">No deactivated accounts found</h3>
                <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto mb-6">
                  There are currently no deactivated accounts in the system.
                </p>
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
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Employee Status</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Actions</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Account Status</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {employees.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage).map((employee, index) => (
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
                          <Badge
                            variant="outline"
                            className="bg-[#F0FDFA] text-[#14B8A6] border-[#99F6E4] dark:bg-[#134E4A]/30 dark:text-[#14B8A6] dark:border-[#134E4A]"
                          >
                            {employee.departmentName}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant="outline"
                            className="bg-[#FEF2F2] text-[#EF4444] border-[#FECACA] dark:bg-[#7F1D1D]/30 dark:text-[#EF4444] dark:border-[#7F1D1D]"
                          >
                            <div className="flex items-center gap-1">
                              <XCircle className="h-3.5 w-3.5" />
                              <span>Inactive</span>
                            </div>
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
                        <TableCell>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleProfileUpdate(employee.employeeId, 'activate')}
                            className="border-[#BFDBFE] text-[#04c807] hover:bg-green-600 hover:text-white dark:border-[#1E3A8A] dark:text-[#3B82F6] dark:hover:bg-[#1E3A8A]/30"
                          >
                            <UserCheck className="h-3.5 w-3.5 mr-1" />
                            Activate Account
                          </Button>
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
                      <p className="text-xs text-white/60 uppercase">Employee ID</p>
                      <p className="text-sm font-medium">{selectedEmployeeProfile.employeeId}</p>
                    </div>
                    <div>
                      <p className="text-xs text-white/60 uppercase">ID Number</p>
                      <p className="text-sm font-medium">{selectedEmployeeProfile.idNumber || "Not provided"}</p>
                    </div>
                    <div>
                      <p className="text-xs text-white/60 uppercase">Employment Status</p>
                      <p className="text-sm font-medium">{selectedEmployeeProfile.employmentStatus}</p>
                    </div>
                    <div>
                      <p className="text-xs text-white/60 uppercase">Hire Date</p>
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
                  <DialogTitle className="text-2xl">Employee Profile</DialogTitle>
                  <DialogDescription>Detailed information about this employee</DialogDescription>
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
                        <p className="text-sm font-medium text-[#1F2937] dark:text-white">
                          {userAccount?.lastLogin ? new Date(userAccount.lastLogin).toLocaleString() : "Not available"}
                        </p>
                      </div>
                      <div>
                        <p className="text-xs text-[#6B7280] dark:text-[#9CA3AF]">Account Status</p>
                        <div className="flex items-center gap-2">
                          <p className="text-sm font-medium text-[#1F2937] dark:text-white">
                            {userAccount ? "Active" : "Inactive"}
                          </p>
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
    </div>
  )
} 