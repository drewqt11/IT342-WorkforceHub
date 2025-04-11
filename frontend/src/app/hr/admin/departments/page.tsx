"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { toast } from "sonner"
import { authService } from "@/lib/auth"
import { Plus, Pencil, Trash2, Building2, RefreshCw } from "lucide-react"
import { Skeleton } from "@/components/ui/skeleton"
import { cn } from "@/lib/utils"

interface Department {
  departmentId: string
  departmentName: string
  description?: string
}

export default function DepartmentsPage() {
  const router = useRouter()
  const [departments, setDepartments] = useState<Department[]>([])
  const [loading, setLoading] = useState(true)
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false)
  const [newDepartmentName, setNewDepartmentName] = useState("")
  const [newDepartmentDescription, setNewDepartmentDescription] = useState("")
  const [selectedDepartment, setSelectedDepartment] = useState<Department | null>(null)
  const [editDepartmentName, setEditDepartmentName] = useState("")
  const [editDepartmentDescription, setEditDepartmentDescription] = useState("")
  const [processingDepartment, setProcessingDepartment] = useState<string | null>(null)
  const [userRole, setUserRole] = useState<string | null>(null)

  useEffect(() => {
    fetchDepartments()
    getUserRole()
  }, [])

  const getUserRole = async () => {
    try {
      const token = authService.getToken()
      if (!token) {
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/oauth2/user-info`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (response.ok) {
        const data = await response.json()
        setUserRole(data.role)
      }
    } catch (error) {
      console.error("Error fetching user role:", error)
    }
  }

  const fetchDepartments = async () => {
    try {
      setLoading(true)
      const token = authService.getToken()
      
      if (!token) {
        router.push("/")
        toast.error("Authentication required. Please log in.")
        return
      }
      
      const response = await fetch("/api/hr/departments", {
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
    } finally {
      setLoading(false)
    }
  }

  const handleAddDepartment = async () => {
    if (!newDepartmentName.trim()) {
      toast.error("Department name is required")
      return
    }

    try {
      setProcessingDepartment("new")
      const token = authService.getToken()
      
      if (!token) {
        router.push("/")
        toast.error("Authentication required. Please log in.")
        return
      }
      
      const formData = new FormData()
      formData.append("departmentName", newDepartmentName)
      if (newDepartmentDescription.trim()) {
        formData.append("description", newDepartmentDescription)
      }
      
      const response = await fetch("/api/hr/departments", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formData,
      })

      if (!response.ok) {
        throw new Error("Failed to create department")
      }

      toast.success("Department created successfully")
      setIsAddDialogOpen(false)
      setNewDepartmentName("")
      setNewDepartmentDescription("")
      fetchDepartments()
    } catch (error) {
      console.error("Error creating department:", error)
      toast.error("Failed to create department. Please try again.")
    } finally {
      setProcessingDepartment(null)
    }
  }

  const handleEditDepartment = async () => {
    if (!selectedDepartment || !editDepartmentName.trim()) {
      toast.error("Department name is required")
      return
    }

    try {
      setProcessingDepartment(selectedDepartment.departmentId)
      const token = authService.getToken()
      
      if (!token) {
        router.push("/")
        toast.error("Authentication required. Please log in.")
        return
      }
      
      const formData = new FormData()
      formData.append("departmentName", editDepartmentName)
      if (editDepartmentDescription.trim()) {
        formData.append("description", editDepartmentDescription)
      }
      
      const response = await fetch(`/api/hr/departments/${selectedDepartment.departmentId}`, {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formData,
      })

      if (!response.ok) {
        throw new Error("Failed to update department")
      }

      toast.success("Department updated successfully")
      setIsEditDialogOpen(false)
      setEditDepartmentName("")
      setEditDepartmentDescription("")
      setSelectedDepartment(null)
      fetchDepartments()
    } catch (error) {
      console.error("Error updating department:", error)
      toast.error("Failed to update department. Please try again.")
    } finally {
      setProcessingDepartment(null)
    }
  }

  const handleDeleteDepartment = async (id: string) => {
    if (!id) {
      toast.error("No department selected")
      return
    }

    try {
      setProcessingDepartment(id)
      const token = authService.getToken()
      
      if (!token) {
        router.push("/")
        toast.error("Authentication required. Please log in.")
        return
      }

      const response = await fetch(`/api/hr/departments/${id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        
        if (response.status === 403) {
          toast.error("You don't have permission to delete departments")
          return
        } else if (response.status === 404) {
          toast.error("Department not found")
          return
        } else if (response.status === 409) {
          toast.error("Cannot delete department: There are employees associated with this department. Please reassign or remove these employees first.")
          return
        }
        
        throw new Error(errorData.error || "Failed to delete department")
      }

      toast.success("Department deleted successfully")
      setIsDeleteDialogOpen(false)
      setSelectedDepartment(null)
      fetchDepartments()
    } catch (error) {
      console.error("Error deleting department:", error)
      toast.error(error instanceof Error ? error.message : "Failed to delete department")
    } finally {
      setProcessingDepartment(null)
    }
  }

  const openEditDialog = (department: Department) => {
    setSelectedDepartment(department)
    setEditDepartmentName(department.departmentName)
    setEditDepartmentDescription(department.description || "")
    setIsEditDialogOpen(true)
  }

  const openDeleteDialog = (department: Department) => {
    setSelectedDepartment(department)
    setIsDeleteDialogOpen(true)
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
      <div className="w-full max-w-6xl mx-auto space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white flex items-center gap-2">
              <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-1 shadow-md">
                <Building2 className="h-5 w-5 text-white" />
              </div>
              Department Management
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
              Create, update, and manage company departments
            </p>
          </div>
          <div className="flex gap-2">
            <Button
              onClick={fetchDepartments}
              className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-md hover:shadow-lg"
            >
              <RefreshCw className="h-4 w-4 mr-2" />
              Refresh
            </Button>
            <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
              <DialogTrigger asChild>
                <Button className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-md hover:shadow-lg">
                  <Plus className="h-4 w-4 mr-2" />
                  Add Department
                </Button>
              </DialogTrigger>
              <DialogContent className="sm:max-w-[425px]">
                <DialogHeader>
                  <DialogTitle>Add New Department</DialogTitle>
                  <DialogDescription>
                    Enter the details for the new department.
                  </DialogDescription>
                </DialogHeader>
                <div className="grid gap-4 py-4">
                  <div className="grid grid-cols-4 items-center gap-4">
                    <Label htmlFor="name" className="text-right">
                      Name
                    </Label>
                    <Input
                      id="name"
                      value={newDepartmentName}
                      onChange={(e) => setNewDepartmentName(e.target.value)}
                      className="col-span-3"
                      placeholder="Department name"
                    />
                  </div>
                  <div className="grid grid-cols-4 items-center gap-4">
                    <Label htmlFor="description" className="text-right">
                      Description
                    </Label>
                    <Input
                      id="description"
                      value={newDepartmentDescription}
                      onChange={(e) => setNewDepartmentDescription(e.target.value)}
                      className="col-span-3"
                      placeholder="Department description (optional)"
                    />
                  </div>
                </div>
                <DialogFooter>
                  <Button 
                    type="submit" 
                    onClick={handleAddDepartment}
                    disabled={processingDepartment === "new"}
                    className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white"
                  >
                    {processingDepartment === "new" ? (
                      <div className="flex items-center gap-1">
                        <div className="h-3 w-3 rounded-full border-2 border-white border-t-transparent animate-spin"></div>
                        <span>Creating...</span>
                      </div>
                    ) : (
                      "Create Department"
                    )}
                  </Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>
          </div>
        </div>

        {/* Main Department Table Card */}
        <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-xl overflow-hidden bg-white dark:bg-[#1F2937]">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
          <CardHeader className="bg-[#F9FAFB] dark:bg-[#111827] border-b border-[#E5E7EB] dark:border-[#374151]">
            <div className="flex flex-col md:flex-row justify-between md:items-center gap-4">
              <div>
                <CardTitle className="text-xl text-[#1F2937] dark:text-white flex items-center gap-2">
                  <Building2 className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
                  Department Directory
                </CardTitle>
                <CardDescription className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
                  View and manage company departments
                </CardDescription>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                  {departments.length} departments
                </span>
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
            ) : departments.length === 0 ? (
              <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
                <div className="relative w-16 h-16 mx-auto mb-4">
                  <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] opacity-20 animate-pulse"></div>
                  <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                    <Building2 className="h-8 w-8 text-[#6B7280] dark:text-[#9CA3AF]" />
                  </div>
                </div>
                <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">No departments found</h3>
                <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto mb-6">
                  There are no departments in the system yet. Create your first department to get started.
                </p>
                <Button
                  className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white shadow-md"
                  onClick={() => setIsAddDialogOpen(true)}
                >
                  <Plus className="h-4 w-4 mr-2" />
                  Add Department
                </Button>
              </div>
            ) : (
              <div className="rounded-lg border border-[#E5E7EB] dark:border-[#374151] overflow-hidden">
                <Table>
                  <TableHeader className="bg-[#F9FAFB] dark:bg-[#111827]">
                    <TableRow className="hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151]">
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Department ID</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Department Name</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Description</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {departments.map((department, index) => (
                      <TableRow
                        key={department.departmentId}
                        className={cn(
                          "hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151] group transition-colors",
                          index % 2 === 0 ? "bg-[#F9FAFB] dark:bg-[#111827]/50" : "",
                        )}
                      >
                        <TableCell className="font-medium text-[#1F2937] dark:text-white">
                          <div className="flex items-center gap-2">
                            <div className="h-6 w-1 rounded-full bg-gradient-to-b from-[#3B82F6] to-[#14B8A6] transition-all duration-300 group-hover:h-full"></div>
                            {department.departmentId}
                          </div>
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          {department.departmentName}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          {department.description || "No description"}
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex justify-end gap-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => openEditDialog(department)}
                              disabled={processingDepartment === department.departmentId}
                              className="border-[#BFDBFE] text-[#3B82F6] hover:bg-[#EFF6FF] dark:border-[#1E3A8A] dark:text-[#3B82F6] dark:hover:bg-[#1E3A8A]/30"
                            >
                              {processingDepartment === department.departmentId ? (
                                <div className="flex items-center gap-1">
                                  <div className="h-3 w-3 rounded-full border-2 border-[#3B82F6] border-t-transparent animate-spin"></div>
                                  <span>Processing...</span>
                                </div>
                              ) : (
                                <div className="flex items-center gap-1">
                                  <Pencil className="h-3.5 w-3.5" />
                                  <span>Edit</span>
                                </div>
                              )}
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => openDeleteDialog(department)}
                              disabled={processingDepartment === department.departmentId}
                              className="border-[#FED7AA] text-[#F59E0B] hover:bg-[#FEF3C7] dark:border-[#78350F] dark:text-[#F59E0B] dark:hover:bg-[#78350F]/30"
                              title={userRole !== "ROLE_ADMIN" ? "Only administrators can delete departments" : ""}
                            >
                              {processingDepartment === department.departmentId ? (
                                <div className="flex items-center gap-1">
                                  <div className="h-3 w-3 rounded-full border-2 border-[#F59E0B] border-t-transparent animate-spin"></div>
                                  <span>Processing...</span>
                                </div>
                              ) : (
                                <div className="flex items-center gap-1">
                                  <Trash2 className="h-3.5 w-3.5" />
                                  <span>Delete</span>
                                </div>
                              )}
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
      </div>

      {/* Edit Department Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Edit Department</DialogTitle>
            <DialogDescription>
              Update the department details.
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="edit-name" className="text-right">
                Name
              </Label>
              <Input
                id="edit-name"
                value={editDepartmentName}
                onChange={(e) => setEditDepartmentName(e.target.value)}
                className="col-span-3"
                placeholder="Department name"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="edit-description" className="text-right">
                Description
              </Label>
              <Input
                id="edit-description"
                value={editDepartmentDescription}
                onChange={(e) => setEditDepartmentDescription(e.target.value)}
                className="col-span-3"
                placeholder="Department description (optional)"
              />
            </div>
          </div>
          <DialogFooter>
            <Button 
              type="submit" 
              onClick={handleEditDepartment}
              disabled={processingDepartment === selectedDepartment?.departmentId}
              className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white"
            >
              {processingDepartment === selectedDepartment?.departmentId ? (
                <div className="flex items-center gap-1">
                  <div className="h-3 w-3 rounded-full border-2 border-white border-t-transparent animate-spin"></div>
                  <span>Updating...</span>
                </div>
              ) : (
                "Update Department"
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Department Dialog */}
      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Delete Department</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete this department? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <div className="py-4">
            <p className="text-[#4B5563] dark:text-[#D1D5DB]">
              <span className="font-medium">{selectedDepartment?.departmentName}</span>
            </p>
          </div>
          <DialogFooter>
            <Button 
              variant="outline" 
              onClick={() => setIsDeleteDialogOpen(false)}
              className="border-[#E5E7EB] text-[#4B5563] hover:bg-[#F3F4F6] dark:border-[#374151] dark:text-[#D1D5DB] dark:hover:bg-[#1F2937]"
            >
              Cancel
            </Button>
            <Button 
              onClick={() => handleDeleteDepartment(selectedDepartment?.departmentId || "")}
              disabled={processingDepartment === selectedDepartment?.departmentId}
              className="bg-gradient-to-r from-[#EF4444] to-[#F59E0B] hover:from-[#DC2626] hover:to-[#D97706] text-white"
            >
              {processingDepartment === selectedDepartment?.departmentId ? (
                <div className="flex items-center gap-1">
                  <div className="h-3 w-3 rounded-full border-2 border-white border-t-transparent animate-spin"></div>
                  <span>Deleting...</span>
                </div>
              ) : (
                "Delete Department"
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
} 