"use client";

import { useState, useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { toast } from "sonner";
import { authService } from "@/lib/auth";
import { UserCog, RefreshCw, Search, Filter } from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";
import { Toaster } from "@/components/ui/sonner";

interface Employee {
  employeeId: string;
  firstName: string;
  lastName: string;
  email: string;
  departmentId: string;
  departmentName: string;
  jobId: string;
  jobName: string;
  jobTitle: string;
  role: string;
  status: boolean;
}

export default function SettingsPage() {
  const router = useRouter();
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [isUpdateDialogOpen, setIsUpdateDialogOpen] = useState(false);
  const [selectedEmployee, setSelectedEmployee] = useState<Employee | null>(
    null
  );
  const [selectedRole, setSelectedRole] = useState("");
  const [processingEmployee, setProcessingEmployee] = useState<string | null>(
    null
  );
  const [searchTerm, setSearchTerm] = useState("");
  const [departmentFilter, setDepartmentFilter] = useState("all");
  const [departments, setDepartments] = useState<string[]>([]);
  const [roleFilter, setRoleFilter] = useState("all");
  const [roles] = useState([
    { value: "ROLE_EMPLOYEE", label: "Employee" },
    { value: "ROLE_HR", label: "HR Admin" },
  ]);
  const [currentUserRole, setCurrentUserRole] = useState<string | null>(null);
  const logoutTriggered = useRef(false);

  useEffect(() => {
    fetchEmployees();
    fetchDepartments();
  }, []);

  // Check for role changes every 10 seconds
  useEffect(() => {
    let interval: NodeJS.Timeout;

    const checkRole = async () => {
      try {
        const profile = await authService.getEmployeeProfile();
        if (!currentUserRole) {
          setCurrentUserRole(profile.role);
        } else if (
          profile.role !== currentUserRole &&
          !logoutTriggered.current
        ) {
          logoutTriggered.current = true;
          toast.error(
            "Your role has changed. You will be logged out for security reasons."
          );
          setTimeout(() => {
            authService.logout();
          }, 2000);
        }
      } catch (err) {
        // Optionally handle error
      }
    };

    checkRole();
    interval = setInterval(checkRole, 10000); // 10 seconds

    return () => clearInterval(interval);
  }, [currentUserRole]);

  const fetchEmployees = async () => {
    try {
      setLoading(true);
      const token = authService.getToken();

      if (!token) {
        router.push("/");
        toast.error("Authentication required. Please log in.");
        return;
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/employees`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error("Failed to fetch employees");
      }

      const data = await response.json();

      // Check if data is an array, if not, try to access the correct property
      const employeesData = Array.isArray(data)
        ? data
        : data.employees || data.content || [];

      // Ensure each employee has all required fields with proper defaults
      const processedEmployees = employeesData
        .filter((emp: any) => emp.status === true) // Only keep active employees
        .map((emp: any) => ({
          employeeId: emp.employeeId || "",
          firstName: emp.firstName || "",
          lastName: emp.lastName || "",
          email: emp.email || "",
          departmentId: emp.departmentId || "",
          departmentName: emp.departmentName || "Unassigned",
          jobId: emp.jobId || "",
          jobName: emp.jobName || "",
          jobTitle: emp.jobTitle || emp.jobName || "",
          role: emp.roleId || emp.role || "ROLE_EMPLOYEE",
          status: emp.status || true,
        }));

      setEmployees(processedEmployees as Employee[]);
    } catch (error) {
      console.error("Error fetching employees:", error);
      toast.error("Failed to load employees. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const fetchDepartments = async () => {
    try {
      const token = authService.getToken();

      if (!token) {
        return;
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/departments`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error("Failed to fetch departments");
      }

      const data = await response.json();
      const departmentsData = Array.isArray(data)
        ? data
        : data.departments || data.content || [];

      // Extract unique department names and sort them alphabetically
      const uniqueDepartments = [
        ...new Set(
          departmentsData.map(
            (dept: any) => dept.departmentName || dept.name || ""
          )
        ),
      ]
        .filter((dept): dept is string => Boolean(dept))
        .sort((a, b) => a.localeCompare(b));

      setDepartments(uniqueDepartments);
    } catch (error) {
      console.error("Error fetching departments:", error);
      toast.error("Failed to load departments. Please try again.");
    }
  };

  const handleUpdateRole = async () => {
    if (!selectedEmployee || !selectedRole) {
      toast.error("Role is required");
      return;
    }

    try {
      setProcessingEmployee(selectedEmployee.employeeId);
      const token = authService.getToken();

      if (!token) {
        router.push("/");
        toast.error("Authentication required. Please log in.");
        return;
      }

      const roleToSend = selectedRole.toUpperCase();
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/hr/employees/${selectedEmployee.employeeId}/assign-role`,
        {
          method: "PUT",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ roleId: roleToSend }),
        }
      );

      const responseData = await response.json();

      if (!response.ok) {
        // Log the full error for debugging
        console.error(
          "Failed to update role:",
          responseData,
          "Status:",
          response.status
        );
        const errorMessage =
          responseData.message || responseData.error || "Failed to update role";
        toast.error(errorMessage);
        throw new Error(errorMessage);
      }

      if (
        typeof responseData.email === "string" &&
        typeof getCookie("email") === "string" &&
        responseData.email.toLowerCase() ===
          (getCookie("email") as string).toLowerCase()
      ) {
        toast.error("Your role was changed. You will be logged out.");
        setTimeout(() => {
          // Remove all cookies
          if (typeof document !== "undefined" && document.cookie) {
            document.cookie.split(";").forEach(function (c) {
              document.cookie =
                c.trim().split("=")[0] +
                "=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/";
            });
          }
          router.push("/");
        }, 1500);
        return;
      }

      // Update the employee in the local state with the response data
      setEmployees((prevEmployees) =>
        prevEmployees.map((emp) =>
          emp.employeeId === selectedEmployee.employeeId
            ? { ...emp, role: responseData.role || roleToSend }
            : emp
        )
      );

      toast.success("Employee role updated successfully");

      setIsUpdateDialogOpen(false);
      setSelectedRole("");
      setSelectedEmployee(null);
    } catch (error) {
      console.error("Error updating role:", error);
      // Only show error toast if it hasn't been shown already
      if (
        !(error instanceof Error && error.message === "Failed to update role")
      ) {
        toast.error(
          error instanceof Error ? error.message : "Failed to update role"
        );
      }
    } finally {
      setProcessingEmployee(null);
    }
  };

  const openUpdateDialog = (employee: Employee) => {
    setSelectedEmployee(employee);
    setSelectedRole(employee.role);
    setIsUpdateDialogOpen(true);
  };

  const filteredEmployees = employees.filter((employee) => {
    // Only show active employees
    if (!employee.status) return false;

    const matchesSearch =
      employee.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      employee.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      employee.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      employee.employeeId.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesDepartment =
      departmentFilter === "all" ||
      employee.departmentName === departmentFilter;
    const matchesRole = roleFilter === "all" || employee.role === roleFilter;

    return matchesSearch && matchesDepartment && matchesRole;
  });

  // Helper function to get role label
  const getRoleLabel = (roleValue: string) => {
    if (!roleValue) return "No Role";
    // Normalize the role value to ensure consistent format
    const normalizedRole = roleValue.toUpperCase();
    const role = roles.find((r) => r.value === normalizedRole);
    return role ? role.label : normalizedRole; // Fallback to the role value if no label found
  };

  // Helper function to get role badge style
  const getRoleBadgeStyle = (role: string) => {
    if (!role)
      return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300";

    const normalizedRole = role.toUpperCase();
    switch (normalizedRole) {
      case "ROLE_HR":
        return "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300";
      case "ROLE_EMPLOYEE":
        return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300";
      default:
        return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300";
    }
  };

  // Get the email from cookies
  function getCookie(name: string): string | null {
    if (typeof document === "undefined" || !document.cookie) return null;
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
      const part = parts.pop();
      if (part) return part.split(";").shift() || null;
    }
    return null;
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
                <UserCog className="h-5 w-5 text-white" />
              </div>
              Role Management
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
              Manage employee roles and permissions
            </p>
          </div>
          <div className="flex gap-2">
            <Button
              onClick={fetchEmployees}
              className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-md hover:shadow-lg"
            >
              <RefreshCw className="h-4 w-4 mr-2" />
              Refresh
            </Button>
          </div>
        </div>

        {/* Filters */}
        <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-md overflow-hidden bg-white dark:bg-[#1F2937]">
          <CardHeader className="bg-[#F9FAFB] dark:bg-[#111827] border-b border-[#E5E7EB] dark:border-[#374151] py-4">
            <div className="flex flex-col md:flex-row gap-4">
              <div className="flex-1">
                <div className="relative">
                  <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-[#9CA3AF]" />
                  <Input
                    type="text"
                    placeholder="Search employees..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="pl-8 bg-white dark:bg-[#1F2937] border-[#E5E7EB] dark:border-[#374151]"
                  />
                </div>
              </div>
              <div className="flex flex-col md:flex-row gap-2">
                <div className="flex items-center gap-2">
                  <Filter className="h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF]" />
                  <Select
                    value={departmentFilter}
                    onValueChange={setDepartmentFilter}
                  >
                    <SelectTrigger className="w-[180px] bg-white dark:bg-[#1F2937] border-[#E5E7EB] dark:border-[#374151]">
                      <SelectValue placeholder="Department" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Departments</SelectItem>
                      {departments.map((department) => (
                        <SelectItem
                          key={`dept-${department}`}
                          value={department}
                        >
                          {department}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="flex items-center gap-2">
                  <Filter className="h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF]" />
                  <Select value={roleFilter} onValueChange={setRoleFilter}>
                    <SelectTrigger className="w-[180px] bg-white dark:bg-[#1F2937] border-[#E5E7EB] dark:border-[#374151]">
                      <SelectValue placeholder="Role" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Roles</SelectItem>
                      {roles.map((role) => (
                        <SelectItem
                          key={`role-${role.value}`}
                          value={role.value}
                        >
                          {role.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </div>
          </CardHeader>
        </Card>

        {/* Main Employee Table Card */}
        <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-xl overflow-hidden bg-white dark:bg-[#1F2937]">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
          <CardHeader className="bg-[#F9FAFB] dark:bg-[#111827] border-b border-[#E5E7EB] dark:border-[#374151]">
            <div className="flex flex-col md:flex-row justify-between md:items-center gap-4">
              <div>
                <CardTitle className="text-xl text-[#1F2937] dark:text-white flex items-center gap-2">
                  <UserCog className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
                  Employee Roles
                </CardTitle>
                <CardDescription className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
                  View and manage employee roles
                </CardDescription>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                  {filteredEmployees.length} employees
                </span>
              </div>
            </div>
          </CardHeader>
          <CardContent className="p-6">
            {loading ? (
              <div className="space-y-4">
                {Array.from({ length: 5 }).map((_, index) => (
                  <div
                    key={`skeleton-${index}`}
                    className="flex items-center space-x-4"
                  >
                    <Skeleton className="h-12 w-full rounded-md" />
                  </div>
                ))}
              </div>
            ) : filteredEmployees.length === 0 ? (
              <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
                <div className="relative w-16 h-16 mx-auto mb-4">
                  <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] opacity-20 animate-pulse"></div>
                  <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                    <UserCog className="h-8 w-8 text-[#6B7280] dark:text-[#9CA3AF]" />
                  </div>
                </div>
                <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">
                  No employees found
                </h3>
                <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto mb-6">
                  {searchTerm || departmentFilter || roleFilter
                    ? "No employees match your search criteria. Try adjusting your filters."
                    : "There are no employees in the system yet."}
                </p>
                {(searchTerm || departmentFilter || roleFilter) && (
                  <Button
                    className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white shadow-md"
                    onClick={() => {
                      setSearchTerm("");
                      setDepartmentFilter("all");
                      setRoleFilter("all");
                    }}
                  >
                    Clear Filters
                  </Button>
                )}
              </div>
            ) : (
              <div className="rounded-lg border border-[#E5E7EB] dark:border-[#374151] overflow-hidden">
                <Table>
                  <TableHeader className="bg-[#F9FAFB] dark:bg-[#111827]">
                    <TableRow className="hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151]">
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                        Employee ID
                      </TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                        Name
                      </TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                        Email
                      </TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                        Department
                      </TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                        Job Title
                      </TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                        Role
                      </TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                        Actions
                      </TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredEmployees.map((employee, index) => (
                      <TableRow
                        key={employee.employeeId}
                        className={cn(
                          "hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151] group transition-colors",
                          index % 2 === 0
                            ? "bg-[#F9FAFB] dark:bg-[#111827]/50"
                            : ""
                        )}
                      >
                        <TableCell className="font-medium text-[#1F2937] dark:text-white">
                          <div className="flex items-center gap-2">
                            <div className="h-6 w-1 rounded-full bg-gradient-to-b from-[#3B82F6] to-[#14B8A6] transition-all duration-300 group-hover:h-full"></div>
                            {employee.employeeId}
                          </div>
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          {employee.firstName} {employee.lastName}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          {employee.email}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          {employee.departmentName}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          {employee.jobName}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          <span
                            className={cn(
                              "px-2 py-1 rounded-full text-xs font-medium",
                              getRoleBadgeStyle(employee.role)
                            )}
                          >
                            {getRoleLabel(employee.role)}
                          </span>
                        </TableCell>
                        <TableCell>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => openUpdateDialog(employee)}
                            disabled={
                              processingEmployee === employee.employeeId
                            }
                            className="border-[#BFDBFE] text-[#3B82F6] hover:bg-[#EFF6FF] dark:border-[#1E3A8A] dark:text-[#3B82F6] dark:hover:bg-[#1E3A8A]/30"
                          >
                            {processingEmployee === employee.employeeId ? (
                              <div className="flex items-center gap-1">
                                <div className="h-3 w-3 rounded-full border-2 border-[#3B82F6] border-t-transparent animate-spin"></div>
                                <span>Processing...</span>
                              </div>
                            ) : (
                              <div className="flex items-center gap-1">
                                <UserCog className="h-3.5 w-3.5" />
                                <span>Update</span>
                              </div>
                            )}
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
      </div>

      {/* Update Role Dialog */}
      <Dialog open={isUpdateDialogOpen} onOpenChange={setIsUpdateDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Update Employee Role</DialogTitle>
            <DialogDescription>
              Change the role for {selectedEmployee?.firstName}{" "}
              {selectedEmployee?.lastName}
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="role" className="text-right">
                Role
              </Label>
              <Select value={selectedRole} onValueChange={setSelectedRole}>
                <SelectTrigger className="col-span-3">
                  <SelectValue placeholder="Select a role" />
                </SelectTrigger>
                <SelectContent>
                  {roles.map((role) => (
                    <SelectItem key={`role-${role.value}`} value={role.value}>
                      {role.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsUpdateDialogOpen(false)}
              className="border-[#E5E7EB] text-[#4B5563] hover:bg-[#F3F4F6] dark:border-[#374151] dark:text-[#D1D5DB] dark:hover:bg-[#1F2937]"
            >
              Cancel
            </Button>
            <Button
              onClick={handleUpdateRole}
              disabled={processingEmployee === selectedEmployee?.employeeId}
              className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white"
            >
              {processingEmployee === selectedEmployee?.employeeId ? (
                <div className="flex items-center gap-1">
                  <div className="h-3 w-3 rounded-full border-2 border-white border-t-transparent animate-spin"></div>
                  <span>Updating...</span>
                </div>
              ) : (
                "Update Role"
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
