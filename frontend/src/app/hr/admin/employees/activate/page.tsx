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
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
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
import { profile } from "console"
import { Toaster } from "@/components/ui/sonner"

interface Employee {
  employeeId: string
  firstName: string
  lastName: string
  email: string
  phoneNumber: string
  gender: string
  dateOfBirth: string
  address: string
  maritalStatus: string
  departmentId: string
  departmentName: string
  jobId: string
  jobName: string
  role: string
  status: boolean
  employmentStatus: string
  isActive: boolean
  createdAt?: string
  idNumber?: string
  hireDate?: string
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
}

interface UserAccountInfo {
  userId: string
  email: string
  createdAt: string
  lastLogin: string
  isActive: boolean
}

interface JobTitle {
  jobId: string
  jobName: string
  departmentId: string
  employeeId: string
}

export default function ActivateEmployeesPage() {
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
  const [profile, setProfile] = useState<EmployeeProfile | null>(null)
  const [userAccount, setUserAccount] = useState<UserAccountInfo | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [processingDepartment, setProcessingDepartment] = useState<string | null>(null)
  const [isUpdateJobTitleDialogOpen, setIsUpdateJobTitleDialogOpen] = useState(false)
  const [selectedJobTitleForUpdate, setSelectedJobTitleForUpdate] = useState<JobTitle | null>(null)
  const [updatingJobTitle, setUpdatingJobTitle] = useState<string | null>(null)
  const [jobTitles, setJobTitles] = useState<JobTitle[]>([])
  const [selectedJobTitleId, setSelectedJobTitleId] = useState<string>("")
  const [loadingJobTitles, setLoadingJobTitles] = useState(false)
  const [jobTitleError, setJobTitleError] = useState<string | null>(null)

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await authService.getEmployeeProfile()
        setProfile(data)

        // Fetch user account info using the OAuth2 user info endpoint
        try {
          const userData = await authService.getOAuth2UserInfo()

          // Map the OAuth2 user info to UserAccountInfo
          setUserAccount({
            userId: userData.userId || "N/A",
            email: userData.email || data.email,
            createdAt: data.createdAt || new Date().toISOString(), // Use actual createdAt from profile
            lastLogin: new Date().toISOString(), // Default value since not in OAuth2 user info
            isActive: true, // Default value since not in OAuth2 user info
          })
        } catch (userErr) {
          console.error("Error fetching OAuth2 user info:", userErr)
          // Fallback to creating UserAccountInfo from profile data
          if (data) {
            setUserAccount({
              userId: data.userId || "N/A",
              email: data.email,
              createdAt: data.createdAt || new Date().toISOString(), // Use actual createdAt from profile
              lastLogin: new Date().toISOString(), // Default value
              isActive: true, // Default value
            })
          }
        }
      } catch (err) {
        console.error("Error fetching profile:", err)
        if (err instanceof Error) {
          if (err.message.includes('Network error')) {
            setError('Unable to connect to the server. Please check your internet connection and try again.');
          } else if (err.message.includes('Session expired')) {
            setError('Your session has expired. Please log in again.');
          } else {
            setError(`Failed to load profile data: ${err.message}`);
          }
        } else {
          setError('An unexpected error occurred while loading your profile.');
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

  useEffect(() => {
    const fetchJobTitles = async () => {
      if (selectedDepartmentId) {
        try {
          setLoadingJobTitles(true)
          setJobTitleError(null)
          const token = authService.getToken()
          if (!token) return

          const response = await fetch(`/api/hr/departments/${selectedDepartmentId}/job-titles`, {
            headers: { Authorization: `Bearer ${token}` },
          })

          if (!response.ok) {
            throw new Error("Failed to load job titles")
          }

          const data = await response.json()
          setJobTitles(data)
        } catch (error) {
          console.error("Error fetching job titles:", error)
          setJobTitleError("Failed to load job titles. Please try again.")
          toast.error("Failed to load job titles")
        } finally {
          setLoadingJobTitles(false)
        }
      } else {
        setJobTitles([])
      }
    }

    fetchJobTitles()
  }, [selectedDepartmentId])

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
      
      // Check if data is an array, if not, try to access the correct property
      const employeesData = Array.isArray(data) ? data : data.employees || data.content || []
      
      // Process employees and check their active status
      const processedEmployees = await Promise.all(
        employeesData.map(async (emp: any) => {
          try {
            // Check if the employee's user account is active
            const isActive = await authService.getActiveStatus(emp.email)
            
            return {
        employeeId: emp.employeeId || "",
        firstName: emp.firstName || "",
        lastName: emp.lastName || "",
        email: emp.email || "",
        phoneNumber: emp.phoneNumber || "Not provided",
        gender: emp.gender || "Not specified",
        dateOfBirth: emp.dateOfBirth ? new Date(emp.dateOfBirth).toLocaleDateString() : "Not provided",
        address: emp.address || "Not provided",
        maritalStatus: emp.maritalStatus || "Not specified",
        departmentId: emp.departmentId || "",
        departmentName: emp.departmentName || "Unassigned",
        jobId: emp.jobId || "",
        jobName: emp.jobName || "",
        role: emp.roleId || emp.role || "ROLE_EMPLOYEE",
        status: emp.status || false,
              employmentStatus: emp.employmentStatus || "INACTIVE",
              isActive: isActive // Add the active status from user account
            }
          } catch (error) {
            console.error(`Error checking active status for employee ${emp.email}:`, error)
            return null // Return null for employees we couldn't check
          }
        })
      )

      // Filter out null values and inactive employees
      const validEmployees = processedEmployees.filter(emp => emp !== null && emp.isActive) as Employee[]
      
      setEmployees(validEmployees)
      setTotalPages(Math.ceil(validEmployees.length / itemsPerPage))
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
      const departmentsData = Array.isArray(data) ? data : data.departments || data.content || []
      
      // Ensure each department has a unique ID and name
      const uniqueDepartments = departmentsData.reduce((acc: Department[], dept: any) => {
        if (dept.departmentId && dept.departmentName && !acc.find(d => d.departmentId === dept.departmentId)) {
          acc.push({
            departmentId: dept.departmentId,
            departmentName: dept.departmentName,
            description: dept.description
          })
        }
        return acc
      }, [])
      
      setDepartments(uniqueDepartments)
    } catch (error) {
      console.error("Error fetching departments:", error)
      toast.error("Failed to load departments. Please try again.")
    }
  }

  const filterEmployees = () => {
    let filtered = employees.filter(employee => !employee.status); // Only show inactive employees
    
    if (searchTerm) {
      filtered = filtered.filter(employee => 
        employee.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        employee.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        employee.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
        employee.employeeId.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }
    
    if (departmentFilter !== "all") {
      filtered = filtered.filter(employee => employee.departmentName === departmentFilter);
    }
    
    setFilteredEmployees(filtered);
    setTotalPages(Math.ceil(filtered.length / itemsPerPage));
    setCurrentPage(1);
  };

  const handleActivate = async (employeeId: string) => {
    try {
      setProcessingEmployee(employeeId)
      const token = authService.getToken()

      if (!token) {
        router.push("/")
        toast.error("Authentication required. Please log in.")
        return
      }

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/hr/employees/${employeeId}/activate`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }
      )

      if (!response.ok) {
        throw new Error("Failed to activate employee")
      }

      toast.success("Employee activated successfully")
      // Refresh the employee list
      await fetchEmployees()
    } catch (error) {
      console.error("Error activating employee:", error)
      toast.error("Failed to activate employee. Please try again.")
    } finally {
      setProcessingEmployee(null)
    }
  }

  const handleDeactivate = async (employeeId: string) => {
    try {
      setProcessingEmployee(employeeId)
      const token = authService.getToken()

      if (!token) {
        router.push("/")
        toast.error("Authentication required. Please log in.")
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/employees/${employeeId}/deactivate`, {
        method: "PATCH",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        throw new Error("Failed to deactivate employee")
      }

      toast.success("Employee deactivated successfully")
      // Refresh the employee list
      await fetchEmployees()
    } catch (error) {
      console.error("Error deactivating employee:", error)
      toast.error("Failed to deactivate employee. Please try again.")
    } finally {
      setProcessingEmployee(null)
    }
  }

  const handleUpdateDepartment = async (employeeId: string, departmentId: string) => {
    try {
      setProcessingDepartment(employeeId);
      const token = authService.getToken();
      
      if (!token) {
        router.push("/");
        toast.error("Authentication required. Please log in.");
        return;
      }
      
      const response = await fetch(`/api/hr/employees/${employeeId}/assign-department`, {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ 
          departmentId,
          jobId: null // Set jobId to null when department changes
        })
      });

      const responseData = await response.json();

      if (!response.ok) {
        const errorMessage = responseData.error || "Failed to update department";
        toast.error(errorMessage);
        throw new Error(errorMessage);
      }

      toast.success("Department updated successfully");
      
      // Refresh the employee list
      await fetchEmployees();
      
      // Close department dialog
      setIsDepartmentDialogOpen(false);
      setSelectedDepartmentId(departmentId);
      
      // Automatically open job title dialog
      if (selectedEmployeeForDepartment) {
        setSelectedJobTitleForUpdate({
          jobId: "",
          jobName: "",
          departmentId: departmentId,
          employeeId: employeeId
        });
        setSelectedJobTitleId("");
        setIsUpdateJobTitleDialogOpen(true);
      }
    } catch (error) {
      console.error("Error updating department:", error);
      toast.error(error instanceof Error ? error.message : "Failed to update department");
    } finally {
      setProcessingDepartment(null);
    }
  };

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
    if (employees.length === 0) return 0
    return Math.round((getActiveCount() / employees.length) * 100)
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

      // Fetch last login time and active status
      const [lastLoginTime, isActive] = await Promise.all([
        authService.getLastLogin(employee.email),
        authService.getActiveStatus(employee.email)
      ]);
      
      // Update user account with the last login time and active status
      setUserAccount({
        userId: employee.employeeId,
        email: employee.email,
        createdAt: employee.createdAt || new Date().toISOString(),
        lastLogin: lastLoginTime,
        isActive: isActive
      });
    } catch (error) {
      console.error("Error fetching profile:", error);
      toast.error("Failed to load profile data");
    } finally {
      setLoadingProfile(false);
    }
  };

  // Add a new function to handle profile updates
  const handleProfileUpdate = async (employeeId: string, action: 'activate' | 'deactivate') => {
    try {
      const token = authService.getToken();
      if (!token) {
        toast.error("Authentication required");
        return;
      }

      const response = await fetch(
        `/api/hr/user-accounts/${selectedEmployeeProfile?.email}/account/${action}`,
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
      setUserAccount(prev => prev ? { ...prev, isActive: !prev.isActive } : null);
      toast.success(`Account ${action}d successfully`);
      
      // Refresh the employee list after the change
      await fetchEmployees();
    } catch (error) {
      console.error(`Error ${action}ing account:`, error);
      toast.error(error instanceof Error ? error.message : `Failed to ${action} account`);
    }
  };

  // Update the profile dialog content to use the new handleProfileUpdate function
  const renderProfileDialog = () => (
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
                      {loadingProfile ? (
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
                        {loadingProfile ? (
                          <div className="flex items-center gap-2">
                            <div className="h-3 w-3 rounded-full border-2 border-[#3B82F6] border-t-transparent animate-spin"></div>
                            <p className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">Loading...</p>
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
                            </div>
                          ) : (
                            <div className="flex items-center gap-1">
                              <Building2 className="h-3.5 w-3.5" />
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
  );

  const handleUpdateJobTitle = async (jobId: string, departmentId: string) => {
    try {
      setUpdatingJobTitle(jobId);
      const token = authService.getToken();
      
      if (!token) {
        router.push("/");
        toast.error("Authentication required. Please log in.");
        return;
      }
      
      if (!selectedJobTitleForUpdate?.employeeId) {
        throw new Error("Employee ID is required");
      }
      
      const response = await fetch(`/api/hr/employees/${selectedJobTitleForUpdate.employeeId}/job-title`, {
        method: "PATCH",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ jobId: selectedJobTitleId })
      });

      const responseData = await response.json();

      if (!response.ok) {
        const errorMessage = responseData.error || responseData.message || "Failed to update job title";
        toast.error(errorMessage);
        throw new Error(errorMessage);
      }

      toast.success("Job title updated successfully");
      
      // Refresh the employee list to show updated job titles
      await fetchEmployees();
      
      // Close the dialog and reset states
      setIsUpdateJobTitleDialogOpen(false);
      setSelectedJobTitleForUpdate(null);
      setSelectedJobTitleId("");
      setSelectedEmployeeForDepartment(null);
    } catch (error) {
      console.error("Error updating job title:", error);
      toast.error(error instanceof Error ? error.message : "Failed to update job title");
    } finally {
      setUpdatingJobTitle(null);
    }
  };

  const handleUpdateJobTitleDialogOpen = (employee: Employee) => {
    setSelectedJobTitleForUpdate({
      jobId: employee.jobId,
      jobName: employee.jobName,
      departmentId: employee.departmentId,
      employeeId: employee.employeeId
    });
    setSelectedDepartmentId(employee.departmentId);
    setSelectedJobTitleId(employee.jobId);
    setIsUpdateJobTitleDialogOpen(true);
  };

  const renderUpdateJobTitleDialog = () => (
    <Dialog 
      open={isUpdateJobTitleDialogOpen} 
      onOpenChange={(open) => {
        if (!open && !selectedJobTitleId && selectedEmployeeForDepartment) {
          toast.warning("Need to assign job-title to proceed");
          return;
        }
        setIsUpdateJobTitleDialogOpen(open);
      }}
    >
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Update Job Title</DialogTitle>
          <DialogDescription>
            {selectedEmployeeForDepartment ? 
              `Assign Job Title for ${selectedEmployeeForDepartment.firstName} ${selectedEmployeeForDepartment.lastName}` :
              `Select a new job title for ${selectedEmployeeProfile?.firstName} ${selectedEmployeeProfile?.lastName}`
            }
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 py-4">
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="department" className="text-right">
              Department
            </Label>
            <div className="col-span-3 flex items-center gap-2">

              <span className="font-medium text-[#1F2937] dark:text-white">
                {departments.find(d => d.departmentId === selectedDepartmentId)?.departmentName || "Not assigned"}
              </span>
            </div>
          </div>
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="jobTitle" className="text-right">
              Job Title
            </Label>
            <div className="col-span-3">
              {loadingJobTitles ? (
                <div className="flex items-center gap-2">
                  <div className="h-4 w-4 rounded-full border-2 border-[#3B82F6] border-t-transparent animate-spin" />
                  <span className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">Loading job titles...</span>
                </div>
              ) : jobTitleError ? (
                <div className="flex items-center gap-2 text-red-500">
                  <AlertCircle className="h-4 w-4" />
                  <span className="text-sm">{jobTitleError}</span>
                </div>
              ) : (
                <Select
                  value={selectedJobTitleId}
                  onValueChange={setSelectedJobTitleId}
                >
                  <SelectTrigger className="w-full">
                    <SelectValue placeholder="Select a job title" />
                  </SelectTrigger>
                  <SelectContent>
                    {jobTitles.length > 0 ? (
                      jobTitles.map((job) => (
                        <SelectItem key={job.jobId} value={job.jobId}>
                          {job.jobName}
                        </SelectItem>
                      ))
                    ) : (
                      <SelectItem value="no-jobs" disabled>
                        No job titles available for this department
                      </SelectItem>
                    )}
                  </SelectContent>
                </Select>
              )}
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button
            variant="outline"
            onClick={() => {
              if (!selectedJobTitleId && selectedEmployeeForDepartment) {
                toast.warning("Need to assign job-title to proceed");
                return;
              }
              setIsUpdateJobTitleDialogOpen(false);
              setSelectedJobTitleId("");
            }}
            disabled={updatingJobTitle === selectedJobTitleForUpdate?.jobId || loadingJobTitles || (!selectedJobTitleId && Boolean(selectedEmployeeForDepartment))}
          >
            Cancel
          </Button>
          <Button
            onClick={() => handleUpdateJobTitle(selectedJobTitleForUpdate?.jobId || "", selectedDepartmentId)}
            disabled={updatingJobTitle === selectedJobTitleForUpdate?.jobId || !selectedJobTitleId || loadingJobTitles}
            className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white"
          >
            {updatingJobTitle === selectedJobTitleForUpdate?.jobId ? (
              <div className="flex items-center gap-1">
                <div className="h-3 w-3 rounded-full border-2 border-white border-t-transparent animate-spin" />
                <span>Updating...</span>
              </div>
            ) : (
              "Update"
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );

  const renderJobTitleCell = (employee: Employee) => (
    <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
      <div className="flex items-center justify-between">
        <span>{employee.jobName || "Not Assigned"}</span>
        <Button
          variant="outline"
          size="sm"
          onClick={() => handleUpdateJobTitleDialogOpen(employee)}
          disabled={updatingJobTitle === employee.jobId || !employee.departmentId}
          className={cn(
            "ml-2 px-2 py-1 h-7 text-xs border-[#BFDBFE] text-[#3B82F6] hover:bg-[#EFF6FF] dark:border-[#1E3A8A] dark:text-[#3B82F6] dark:hover:bg-[#1E3A8A]/30",
            !employee.departmentId && "opacity-50 cursor-not-allowed"
          )}
        >
          {updatingJobTitle === employee.jobId ? (
            <div className="flex items-center gap-1">
              <div className="h-2.5 w-2.5 rounded-full border-2 border-[#3B82F6] border-t-transparent animate-spin" />
            </div>
          ) : (
            <div className="flex items-center gap-1">
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
                className="lucide lucide-briefcase"
              >
                <rect width="20" height="14" x="2" y="7" rx="2" ry="2"></rect>
                <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
              </svg>
            </div>
          )}
        </Button>
      </div>
    </TableCell>
  );

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
      <Toaster 
        position="top-right" 
        richColors 
        className="mt-24" 
        style={{
          top: "6rem",
          right: "1rem"
        }}
      />
      <div className="w-full max-w-6xl mx-auto space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white flex items-center gap-2">
              <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-1 shadow-md">
                <Shield className="h-5 w-5 text-white" />
              </div>
              Employee Activation
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
              Manage employee activation status and system access
            </p>
          </div>
          <Button
            onClick={fetchEmployees}
            className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-md hover:shadow-lg"
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh List
          </Button>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
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
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#14B8A6] via-[#0EA5E9] to-[#3B82F6]"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">Active Employees</p>
                  <h3 className="text-3xl font-bold text-[#1F2937] dark:text-white mt-1">
                    {loading ? <Skeleton className="h-9 w-16" /> : getActiveCount()}
                  </h3>
                </div>
                <div className="h-12 w-12 bg-[#F0FDFA] dark:bg-[#134E4A]/30 rounded-full flex items-center justify-center">
                  <UserCheck className="h-6 w-6 text-[#14B8A6] dark:text-[#14B8A6]" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-md overflow-hidden bg-white dark:bg-[#1F2937] hover:shadow-lg transition-shadow duration-200">
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#F59E0B] to-[#EF4444]"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">Inactive Employees</p>
                  <h3 className="text-3xl font-bold text-[#1F2937] dark:text-white mt-1">
                    {loading ? <Skeleton className="h-9 w-16" /> : getInactiveCount()}
                  </h3>
                </div>
                <div className="h-12 w-12 bg-[#FEF3C7] dark:bg-[#78350F]/30 rounded-full flex items-center justify-center">
                  <UserX className="h-6 w-6 text-[#F59E0B] dark:text-[#F59E0B]" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Status Overview Card */}
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
                {getActivePercentage()}% Active
              </Badge>
            </div>

            <div className="space-y-3">
              <div className="flex justify-between mb-1">
                <span className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">Active Employees</span>
                <span className="text-sm font-medium text-[#14B8A6] dark:text-[#14B8A6]">
                  {loading ? "..." : `${getActivePercentage()}%`}
                </span>
              </div>
              <div className="h-3 w-full bg-[#F3F4F6] dark:bg-[#374151] rounded-full overflow-hidden">
                <div
                  className="h-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] rounded-full transition-all duration-1000 ease-out"
                  style={{ width: `${loading ? 0 : getActivePercentage()}%` }}
                ></div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Main Employee Table Card */}
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
                  View and manage employee activation status
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
                    setStatusFilter("all")
                    setDepartmentFilter("all")
                  }}
                >
                  <RefreshCw className="h-4 w-4 mr-2" />
                  Reset Filters
                </Button>
              </div>
            ) : (
              <>
                <div className="rounded-lg border border-[#E5E7EB] dark:border-[#374151] overflow-hidden">
                  <Table>
                    <TableHeader className="bg-[#F9FAFB] dark:bg-[#111827]">
                      <TableRow className="hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151]">
                        <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Employee ID</TableHead>
                        <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Name</TableHead>
                        <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Email</TableHead>
                        <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Department</TableHead>
                        <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Job Title</TableHead>
                        <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Status</TableHead>
                        <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Profile</TableHead>
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
                            {employee.firstName} {employee.lastName}
                          </TableCell>
                          <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                            {employee.email}
                          </TableCell>
                          <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                            <div className="flex items-center justify-between">
                              <span>{employee.departmentName}</span>
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => handleDepartmentDialogOpen(employee)}
                                disabled={updatingDepartment === employee.employeeId}
                                className="ml-2 px-2 py-1 h-7 text-xs border-[#BFDBFE] text-[#3B82F6] hover:bg-[#EFF6FF] dark:border-[#1E3A8A] dark:text-[#3B82F6] dark:hover:bg-[#1E3A8A]/30"
                              >
                                {updatingDepartment === employee.employeeId ? (
                                  <div className="flex items-center gap-1">
                                    <div className="h-2.5 w-2.5 rounded-full border-2 border-[#3B82F6] border-t-transparent animate-spin" />
                                  </div>
                                ) : (
                                  <div className="flex items-center gap-1">
                                    <Building2 className="h-3.5 w-3.5" />
                                  </div>
                                )}
                              </Button>
                            </div>
                          </TableCell>
                          <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                            <div className="flex items-center justify-between">
                              <span>{employee.jobName || "Not Assigned"}</span>
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => handleUpdateJobTitleDialogOpen(employee)}
                                disabled={updatingJobTitle === employee.jobId || !employee.departmentId}
                                className={cn(
                                  "ml-2 px-2 py-1 h-7 text-xs border-[#BFDBFE] text-[#3B82F6] hover:bg-[#EFF6FF] dark:border-[#1E3A8A] dark:text-[#3B82F6] dark:hover:bg-[#1E3A8A]/30",
                                  !employee.departmentId && "opacity-50 cursor-not-allowed"
                                )}
                              >
                                {updatingJobTitle === employee.jobId ? (
                                  <div className="flex items-center gap-1">
                                    <div className="h-2.5 w-2.5 rounded-full border-2 border-[#3B82F6] border-t-transparent animate-spin" />
                                  </div>
                                ) : (
                                  <div className="flex items-center gap-1">
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
                                      className="lucide lucide-briefcase"
                                    >
                                      <rect width="20" height="14" x="2" y="7" rx="2" ry="2"></rect>
                                      <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                                    </svg>
                                  </div>
                                )}
                              </Button>
                            </div>
                          </TableCell>
                          <TableCell>
                            <Badge
                              variant="outline"
                              className={cn(
                                "border-2",
                                employee.status
                                  ? "bg-[#F0FDFA] text-[#14B8A6] border-[#99F6E4] dark:bg-[#134E4A]/30 dark:text-[#14B8A6] dark:border-[#134E4A]"
                                  : "bg-[#FEF2F2] text-[#EF4444] border-[#FECACA] dark:bg-[#7F1D1D]/30 dark:text-[#EF4444] dark:border-[#7F1D1D]"
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
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleProfileView(employee)}
                            >
                              View Profile
                            </Button>
                          </TableCell>
                          <TableCell>
                            <div className="flex items-center gap-2">
                              {employee.status ? (
                                <Button
                                  variant="outline"
                                  size="sm"
                                  onClick={() => handleDeactivate(employee.employeeId)}
                                  disabled={processingEmployee === employee.employeeId}
                                  className="border-[#FED7AA] text-[#F59E0B] hover:bg-[#FEF3C7] dark:border-[#78350F] dark:text-[#F59E0B] dark:hover:bg-[#78350F]/30"
                                >
                                  {processingEmployee === employee.employeeId ? (
                                    <div className="flex items-center gap-1">
                                      <div className="h-3 w-3 rounded-full border-2 border-[#F59E0B] border-t-transparent animate-spin"></div>
                                      <span>Processing...</span>
                                    </div>
                                  ) : (
                                    <div className="flex items-center gap-1">
                                      <UserX className="h-3.5 w-3.5" />
                                      <span>Deactivate</span>
                                    </div>
                                  )}
                                </Button>
                              ) : (
                                <Button
                                  variant="outline"
                                  size="sm"
                                  onClick={() => handleActivate(employee.employeeId)}
                                  disabled={processingEmployee === employee.employeeId}
                                  className="border-[#BFDBFE] text-[#3B82F6] hover:bg-[#EFF6FF] dark:border-[#1E3A8A] dark:text-[#3B82F6] dark:hover:bg-[#1E3A8A]/30"
                                >
                                  {processingEmployee === employee.employeeId ? (
                                    <div className="flex items-center gap-1">
                                      <div className="h-3 w-3 rounded-full border-2 border-[#3B82F6] border-t-transparent animate-spin"></div>
                                      <span>Processing...</span>
                                    </div>
                                  ) : (
                                    <div className="flex items-center gap-1">
                                      <UserCheck className="h-3.5 w-3.5" />
                                      <span>Activate</span>
                                    </div>
                                  )}
                                </Button>
                              )}
                            </div>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>

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
              </>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Department Dialog */}
      <Dialog open={isDepartmentDialogOpen} onOpenChange={setIsDepartmentDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Update Department</DialogTitle>
            <DialogDescription>
              Select a new department for {selectedEmployeeForDepartment?.firstName} {selectedEmployeeForDepartment?.lastName}
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="department" className="text-right">
                Department
              </Label>
              <Select
                value={selectedDepartmentId}
                onValueChange={setSelectedDepartmentId}
              >
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
              disabled={updatingDepartment === selectedEmployeeForDepartment?.employeeId}
            >
              Cancel
            </Button>
            <Button
              onClick={() => handleUpdateDepartment(selectedEmployeeForDepartment?.employeeId || "", selectedDepartmentId)}
              disabled={updatingDepartment === selectedEmployeeForDepartment?.employeeId}
              className="bg-[#3B82F6] text-white hover:bg-[#2563EB] dark:bg-[#1E40AF] dark:hover:bg-[#1E3A8A]"
            >
              {updatingDepartment === selectedEmployeeForDepartment?.employeeId ? (
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

      {renderUpdateJobTitleDialog()}
      {renderProfileDialog()}
    </div>
  )
}
