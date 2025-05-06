"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { toast } from "sonner"
import { authService } from "@/lib/auth"
import { Plus, Pencil, Trash2, Building2, RefreshCw, BriefcaseBusiness } from "lucide-react"
import { Skeleton } from "@/components/ui/skeleton"
import { cn } from "@/lib/utils"
import { Toaster } from "sonner"
import { HoverCard, HoverCardContent, HoverCardTrigger } from "@/components/ui/hover-card"
import { Textarea } from "@/components/ui/textarea"

interface Department {
  departmentId: string
  departmentName: string
  description?: string
}

interface PayGrade {
  min: string
  max: string
}

interface JobTitle {
  jobId: string
  jobName: string
  jobDescription: string
  payGrade: string
  departmentId: string
}

export default function DepartmentsPage() {
  const router = useRouter()
  const [departments, setDepartments] = useState<Department[]>([])
  const [loading, setLoading] = useState(true)
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false)
  const [isAddJobTitlesDialogOpen, setIsAddJobTitlesDialogOpen] = useState(false)
  const [isViewJobsDialogOpen, setIsViewJobsDialogOpen] = useState(false)
  const [newDepartmentName, setNewDepartmentName] = useState("")
  const [newDepartmentDescription, setNewDepartmentDescription] = useState("")
  const [selectedDepartment, setSelectedDepartment] = useState<Department | null>(null)
  const [editDepartmentName, setEditDepartmentName] = useState("")
  const [editDepartmentDescription, setEditDepartmentDescription] = useState("")
  const [processingDepartment, setProcessingDepartment] = useState<string | null>(null)
  const [userRole, setUserRole] = useState<string | null>(null)
  const [selectedDepartmentForJobs, setSelectedDepartmentForJobs] = useState<Department | null>(null)
  const [jobTitles, setJobTitles] = useState<JobTitle[]>([])
  const [processing, setProcessing] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [loadingJobs, setLoadingJobs] = useState(false)
  const [addJobTitles, setAddJobTitles] = useState<JobTitle[]>([])
  const [isEditJobTitleDialogOpen, setIsEditJobTitleDialogOpen] = useState(false)
  const [selectedJobTitle, setSelectedJobTitle] = useState<JobTitle | null>(null)
  const [editJobTitleName, setEditJobTitleName] = useState("")
  const [editJobTitleDescription, setEditJobTitleDescription] = useState("")
  const [editJobTitlePayGrade, setEditJobTitlePayGrade] = useState("")
  const [isDeleteJobTitleDialogOpen, setIsDeleteJobTitleDialogOpen] = useState(false)
  const [jobTitleToDelete, setJobTitleToDelete] = useState<JobTitle | null>(null)
  const [isDeletingJobTitle, setIsDeletingJobTitle] = useState(false)
  const [payGradeMin, setPayGradeMin] = useState("");
  const [payGradeMax, setPayGradeMax] = useState("");
  const [payGradeError, setPayGradeError] = useState("");
  const [editPayGradeMin, setEditPayGradeMin] = useState("");
  const [editPayGradeMax, setEditPayGradeMax] = useState("");

  useEffect(() => {
    fetchDepartments()
    getUserRole()
  }, [])

  useEffect(() => {
    if (isEditJobTitleDialogOpen && editJobTitlePayGrade) {
      const match = editJobTitlePayGrade.match(/Php\s*([\d,]+(?:\.\d{1,2})?)\s*-\s*([\d,]+(?:\.\d{1,2})?)/);
      if (match) {
        setEditPayGradeMin(match[1]);
        setEditPayGradeMax(match[2]);
      } else {
        setEditPayGradeMin("");
        setEditPayGradeMax("");
      }
    }
  }, [isEditJobTitleDialogOpen, editJobTitlePayGrade]);

  // Clear pay grade min/max when Add Job Titles dialog is opened
  useEffect(() => {
    if (isAddJobTitlesDialogOpen) {
      setPayGradeMin("");
      setPayGradeMax("");
      setPayGradeError("");
    }
  }, [isAddJobTitlesDialogOpen]);

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
      toast.error("Failed to fetch user role. Please try again.")
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

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/departments`, {
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

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/departments/${selectedDepartment.departmentId}`, {
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

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/departments/${id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        throw new Error("Failed to delete department")
      }

      toast.success("Department deleted successfully")
      setIsDeleteDialogOpen(false)
      setSelectedDepartment(null)
      fetchDepartments()
    } catch (error) {
      toast.error("Failed to delete department. Please try again.")
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

  const handleViewJobs = async (department: Department) => {
    try {
      setLoadingJobs(true)
      setSelectedDepartmentForJobs(department)
      const token = authService.getToken()

      if (!token) {
        router.push("/")
        toast.error("Authentication required. Please log in.")
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/job-titles/department/${department.departmentId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        throw new Error("Failed to fetch job titles")
      }

      const data = await response.json()
      setJobTitles(data)
      setIsViewJobsDialogOpen(true)
    } catch (error) {
      console.error("Error fetching job titles:", error)
      toast.error("Failed to fetch job titles")
    } finally {
      setLoadingJobs(false)
    }
  }

  const handleOpenAddJobTitles = (department: Department) => {
    setSelectedDepartmentForJobs(department)
    setAddJobTitles([
      {
        jobId: `new-${Date.now()}`,
        jobName: "",
        jobDescription: "",
        payGrade: "",
        departmentId: department.departmentId,
      },
    ])
    setPayGradeMin("")
    setPayGradeMax("")
    setIsAddJobTitlesDialogOpen(true)
  }

  const handleAddJobTitle = () => {
    setAddJobTitles([
      ...addJobTitles,
      {
        jobId: `new-${Date.now()}`,
        jobName: "",
        jobDescription: "",
        payGrade: "",
        departmentId: selectedDepartmentForJobs?.departmentId || "",
      },
    ])
  }

  const handleSubmitJobTitles = async () => {
    if (!selectedDepartmentForJobs) return

    try {
      setProcessing(true)
      setError(null)
      const token = authService.getToken()

      if (!token) {
        router.push("/")
        toast.error("Authentication required. Please log in.")
        return
      }

      // Validate job titles
      if (addJobTitles.some((job) => !(job.jobName || '').trim())) {
        setError("Job name is required for all job titles")
        return
      }

      // Validate pay grade
      const payGradeError = validatePayGrade(payGradeMin, payGradeMax)
      if (payGradeError) {
        setError(payGradeError)
        return
      }

      // Submit each job title
      for (let i = 0; i < addJobTitles.length; i++) {
        const job = { ...addJobTitles[i] }
        // Only set pay grade if both min and max are provided and validation passed
        if (payGradeMin && payGradeMax) {
          const min = parseInt(payGradeMin.replace(/,/g, ''), 10)
          const max = parseInt(payGradeMax.replace(/,/g, ''), 10)
          job.payGrade = `Php ${min.toLocaleString("en-US")}.00 - ${max.toLocaleString("en-US")}.00`
        }

        const formData = new FormData()
        formData.append("jobName", job.jobName)
        formData.append("jobDescription", job.jobDescription || '')
        formData.append("payGrade", job.payGrade || '')
        formData.append("departmentId", selectedDepartmentForJobs.departmentId)

        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/job-titles`, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
          body: formData,
        })

        if (!response.ok) {
          let errorMessage = "Failed to create job title"
          try {
            const contentType = response.headers.get("content-type")
            if (contentType && contentType.includes("application/json")) {
              const errorData = await response.json()
              errorMessage = errorData.error || errorMessage
            } else {
              const text = await response.text()
              errorMessage = text || errorMessage
            }
          } catch (e) {
            console.error("Error parsing error response:", e)
          }
          throw new Error(errorMessage)
        }
      }

      toast.success("Job titles created successfully")
      setIsAddJobTitlesDialogOpen(false)
      setAddJobTitles([])
      // Refresh the job titles list
      if (selectedDepartmentForJobs) {
        handleViewJobs(selectedDepartmentForJobs)
      }
    } catch (error) {
      console.error("Error creating job titles:", error)
      setError(error instanceof Error ? error.message : "Failed to create job titles")
      toast.error(error instanceof Error ? error.message : "Failed to create job titles")
    } finally {
      setProcessing(false)
    }
  }

  const handleRemoveJobTitle = (index: number) => {
    setAddJobTitles(addJobTitles.filter((_, i) => i !== index))
  }

  const handleJobTitleChange = (index: number, field: keyof JobTitle, value: string) => {
    const updatedJobTitles = [...addJobTitles]
    updatedJobTitles[index] = { ...updatedJobTitles[index], [field]: value }
    setAddJobTitles(updatedJobTitles)
  }

  // Clear pay grade fields when opening Add Job Titles dialog
  useEffect(() => {
    if (isAddJobTitlesDialogOpen) {
      setPayGradeMin("")
      setPayGradeMax("")
      setPayGradeError("")
    }
  }, [isAddJobTitlesDialogOpen])

  // Clear pay grade fields when closing Add Job Titles dialog
  useEffect(() => {
    if (!isAddJobTitlesDialogOpen) {
      setPayGradeMin("")
      setPayGradeMax("")
      setPayGradeError("")
    }
  }, [isAddJobTitlesDialogOpen])

  const handleDeleteJobTitle = async (jobId: string) => {
    if (!selectedDepartmentForJobs) return

    try {
      setIsDeletingJobTitle(true)
      const token = authService.getToken()

      if (!token) {
        router.push("/")
        toast.error("Authentication required. Please log in.")
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/job-titles/${jobId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        throw new Error("Failed to delete job title")
      }

      toast.success("Job title deleted successfully")
      setIsDeleteJobTitleDialogOpen(false)
      setJobTitleToDelete(null)
      
      // Refresh the job titles list
      const jobsRes = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/job-titles/department/${selectedDepartmentForJobs.departmentId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      
      if (jobsRes.ok) {
        const updatedJobs = await jobsRes.json();
        setJobTitles(updatedJobs);
      } else {
        console.error("Failed to refresh job titles after deletion");
      }
    } catch (error) {
      toast.error("Failed to delete job title. Please try again.")
    } finally {
      setIsDeletingJobTitle(false)
    }
  }

  // Helper to format number with commas (no decimals)
  function formatPayGradeWhole(val: string) {
    let num = val.replace(/[^\d]/g, "");
    if (!num) return "";
    return parseInt(num, 10).toLocaleString("en-US");
  }

  // Helper to format pay grade display
  function formatPayGradeDisplay(payGrade: string) {
    if (!payGrade) return "No Grade"
    const match = payGrade.match(/Php\s*([\d,]+(?:\.\d{1,2})?)\s*-\s*([\d,]+(?:\.\d{1,2})?)/);
    if (match) {
      const min = parseInt(match[1].replace(/,/g, ''), 10).toLocaleString("en-US")
      const max = parseInt(match[2].replace(/,/g, ''), 10).toLocaleString("en-US")
      return `Php ${min}.00 - ${max}.00`
    }
    return payGrade
  }

  function validatePayGrade(min: string, max: string): string | null {
    if ((min && !max) || (!min && max)) {
      return "Both minimum and maximum pay grade are required"
    }
    
    if (min && max) {
      const minValue = parseInt(min.replace(/,/g, ''), 10)
      const maxValue = parseInt(max.replace(/,/g, ''), 10)
      
      if (isNaN(minValue) || isNaN(maxValue)) {
        return "Pay grade must be a valid number"
      }
      
      if (minValue >= maxValue) {
        return "Maximum pay grade must be greater than minimum pay grade"
      }
      
      if (minValue < 0 || maxValue < 0) {
        return "Pay grade cannot be negative"
      }
    }
    
    return null
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
                <Building2 className="h-5 w-5 text-white" />
              </div>
              Department Management
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">Create, update, and manage company departments</p>
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
                  <DialogDescription>Enter the details for the new department.</DialogDescription>
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
                  <div className="grid grid-cols-4 gap-4">
                    <Label htmlFor="description" className="text-right">
                      Description
                    </Label>
                    <Textarea
                      id="description"
                      value={newDepartmentDescription}
                      onChange={(e) => setNewDepartmentDescription(e.target.value)}
                      className="col-span-3 min-h-[40px] resize-y"
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
                <span className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">{departments.length} departments</span>
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
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium w-12 text-center">No.</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Department Name</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Description</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium pl-16">
                        Actions
                      </TableHead>
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
                        <TableCell className="text-center text-[#4B5563] dark:text-[#D1D5DB] font-medium">{index + 1}</TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          {department.departmentName}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          {department.description ? (
                            <HoverCard>
                              <HoverCardTrigger asChild>
                                <span className="select-none cursor-pointer">
                                  {department.description.length > 30
                                    ? `${department.description.substring(0, 60)}...`
                                    : department.description}
                                </span>
                              </HoverCardTrigger>
                              <HoverCardContent className="w-auto">
                                <div className="space-y-1">
                                  <h4 className="text-sm font-semibold">Description</h4>
                                  <p className="text-sm text-muted-foreground">{department.description}</p>
                                </div>
                              </HoverCardContent>
                            </HoverCard>
                          ) : (
                            "No description"
                          )}
                        </TableCell>
                        <TableCell className="text-left">
                          <div className="flex justify-end gap-4">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleViewJobs(department)}
                              className="flex items-center gap-2"
                              disabled={loadingJobs}
                            >
                              {loadingJobs && selectedDepartmentForJobs?.departmentId === department.departmentId ? (
                                <div className="flex items-center gap-2">
                                  <div className="h-3 w-3 rounded-full border-2 border-[#3B82F6] border-t-transparent animate-spin"></div>
                                  <span>Loading...</span>
                                </div>
                              ) : (
                                <>
                                  <BriefcaseBusiness className="h-4 w-4" />
                                  View Jobs
                                </>
                              )}
                            </Button>
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
            <DialogDescription>Update the department details.</DialogDescription>
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
              <Textarea
                id="edit-description"
                value={editDepartmentDescription}
                onChange={(e) => setEditDepartmentDescription(e.target.value)}
                className="col-span-3 min-h-[40px] resize-y"
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

      {/* View Jobs Dialog */}
      <Dialog open={isViewJobsDialogOpen} onOpenChange={setIsViewJobsDialogOpen}>
        <DialogContent className="sm:max-w-[1000px] max-h-[85vh] overflow-hidden flex flex-col p-0">
          <div className="sticky top-0 z-10 bg-white dark:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151]">
            <DialogHeader className="p-6 pb-4">
              <div className="flex items-center gap-2">
                <div className="h-8 w-8 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-md flex items-center justify-center shadow-sm">
                  <Building2 className="h-4 w-4 text-white" />
                </div>
                <DialogTitle className="text-xl">{selectedDepartmentForJobs?.departmentName} Jobs</DialogTitle>
              </div>
              <DialogDescription className="mt-1.5">View and manage job titles for this department</DialogDescription>
            </DialogHeader>
            <div className="flex justify-between items-center px-6 pb-4">
              <div className="text-sm text-muted-foreground">
                {jobTitles.length} {jobTitles.length === 1 ? "job" : "jobs"} found
              </div>
              <Button
                onClick={() => handleOpenAddJobTitles(selectedDepartmentForJobs!)}
                className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-sm hover:shadow-md"
              >
                <Plus className="h-4 w-4 mr-2" />
                Add Job Title
              </Button>
            </div>
          </div>

          <div className="overflow-y-auto flex-1 p-6 pt-2">
            {loadingJobs ? (
              <div className="space-y-4">
                {Array.from({ length: 3 }).map((_, index) => (
                  <Skeleton key={index} className="h-32 w-full rounded-lg" />
                ))}
              </div>
            ) : jobTitles.length === 0 ? (
              <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
                <div className="relative w-16 h-16 mx-auto mb-4">
                  <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] opacity-20 animate-pulse"></div>
                  <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                    <BriefcaseBusiness className="h-8 w-8 text-[#6B7280] dark:text-[#9CA3AF]" />
                  </div>
                </div>
                <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">No job titles found</h3>
                <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto mb-6">
                  There are no job titles defined for this department yet. Add your first job title to get started.
                </p>
                <Button
                  onClick={() => handleOpenAddJobTitles(selectedDepartmentForJobs!)}
                  className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white shadow-md"
                >
                  <Plus className="h-4 w-4 mr-2" />
                  Add Job Title
                </Button>
              </div>
            ) : (
              <div className="grid gap-4 md:grid-cols-3">
                {jobTitles.map((job, index) => (
                  <Card
                    key={job.jobId}
                    className="flex flex-col min-h-[auto] h-full overflow-hidden border border-[#E5E7EB] dark:border-[#374151] transition-all duration-200 hover:shadow-md relative"
                  >
                    <div className="absolute top-0 left-0 right-0 h-1.5 bg-gradient-to-r from-[#3B82F6] to-[#14B8A6]" />
                    <CardContent className="flex flex-1 flex-col p-5 pt-4">
                      <div className="flex-1 flex flex-col">
                        <div className="flex justify-between items-start mb-3">
                          <h3 className="font-semibold text-lg text-[#1F2937] dark:text-white truncate">
                            {job.jobName}
                          </h3>
                        </div>
                        <div className="mb-4">
                          <span className="block text-xs text-[#6B7280] dark:text-[#9CA3AF] font-medium">Job Description</span>
                          <p className="text-sm text-[#4B5563] dark:text-[#D1D5DB] whitespace-pre-wrap break-words">{job.jobDescription || "No description provided"}</p>
                        </div>
                        <div className="mb-6">
                          <span className="block text-xs text-[#6B7280] dark:text-[#9CA3AF] font-medium">Pay Grade</span>
                          <div className="mt-1 text-sm font-medium text-[#1E40AF] dark:text-[#93C5FD]">
                            {formatPayGradeDisplay(job.payGrade)}
                          </div>
                        </div>
                      </div>
                      <div className="flex justify-end gap-2 mt-auto pt-2">
                        <Button
                          variant="outline"
                          size="sm"
                          className="text-xs"
                          onClick={() => {
                            setSelectedJobTitle(job)
                            setEditJobTitleName(job.jobName)
                            setEditJobTitleDescription(job.jobDescription)
                            setEditJobTitlePayGrade(job.payGrade)
                            setIsEditJobTitleDialogOpen(true)
                          }}
                        >
                          <Pencil className="h-3 w-3 mr-1" />
                          Edit
                        </Button>
                        {!(job.jobId.startsWith && job.jobId.startsWith('new-')) && (
                          <Button
                            variant="destructive"
                            size="sm"
                            className="text-xs"
                            onClick={() => {
                              setJobTitleToDelete(job);
                              setIsDeleteJobTitleDialogOpen(true);
                            }}
                          >
                            <Trash2 className="h-3 w-3 mr-1" />
                            Delete
                          </Button>
                        )}
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </div>

          <div className="sticky bottom-0 z-10 bg-white dark:bg-[#1F2937] border-t border-[#E5E7EB] dark:border-[#374151] p-4 flex justify-end">
            <Button
              variant="outline"
              onClick={() => {
                setIsViewJobsDialogOpen(false)
                setJobTitles([])
                setSelectedDepartmentForJobs(null)
              }}
            >
              Close
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Add Job Titles Dialog */}
      <Dialog
        open={isAddJobTitlesDialogOpen}
        onOpenChange={async (open) => {
          if (!open && processing) return;
          setIsAddJobTitlesDialogOpen(open);
          if (!open) {
            setAddJobTitles([]);
            setError(null);
            // Always fetch from backend before opening View Jobs dialog
            if (selectedDepartmentForJobs) {
              const token = authService.getToken();
              if (token) {
                const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/departments/${selectedDepartmentForJobs.departmentId}/job-titles`, {
                  headers: { Authorization: `Bearer ${token}` },
                });
                if (response.ok) {
                  const jobs = await response.json();
                  setJobTitles(jobs);
                } else {
                  setJobTitles([]);
                }
              } else {
                setJobTitles([]);
              }
              setIsViewJobsDialogOpen(true);
            }
          }
        }}
      >
        <DialogContent className="sm:max-w-[700px] max-h-[85vh] overflow-hidden flex flex-col p-0">
          <div className="sticky top-0 z-10 bg-white dark:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151]">
            <DialogHeader className="p-6 pb-4">
              <div className="flex items-center gap-2">
                <div className="h-8 w-8 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-md flex items-center justify-center shadow-sm">
                  <BriefcaseBusiness className="h-4 w-4 text-white" />
                </div>
                <DialogTitle className="text-xl">Add Job Titles</DialogTitle>
              </div>
              <DialogDescription className="mt-1.5">
                Add job titles for {selectedDepartmentForJobs?.departmentName}
              </DialogDescription>
            </DialogHeader>
          </div>

          <div className="overflow-y-auto flex-1 p-6 pt-3">
            {error && (
              <div className="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-md text-red-600 dark:text-red-400 text-sm">
                <p className="font-medium mb-1">Error</p>
                <p>{error}</p>
              </div>
            )}

            <div className="space-y-6">
              {addJobTitles.map((job, index) => (
                <Card
                  key={job.jobId}
                  className="overflow-hidden border border-[#E5E7EB] dark:border-[#374151] relative"
                >
                  <div className="absolute top-0 left-0 right-0 h-1.5 bg-gradient-to-r from-[#3B82F6] to-[#14B8A6]"></div>
                  <CardHeader className="p-4 pb-2 pt-6 flex flex-row items-center justify-between">
                    <div className="flex items-center gap-2">
                      <div className="h-6 w-6 rounded-full bg-[#DBEAFE] dark:bg-[#1E3A8A]/30 flex items-center justify-center">
                        <span className="text-xs font-medium text-[#1E40AF] dark:text-[#93C5FD]">{index + 1}</span>
                      </div>
                      <h3 className="font-medium text-[#1F2937] dark:text-white">Job Title #{index + 1}</h3>
                    </div>
                    {index > 0 && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleRemoveJobTitle(index)}
                        className="h-8 w-8 p-0 text-red-500 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20"
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    )}
                  </CardHeader>
                  <CardContent className="p-4 pt-2 space-y-4">
                    <div className="grid gap-4 md:grid-cols-2">
                      <div>
                        <Label htmlFor={`jobName-${job.jobId}`} className="text-sm font-medium mb-1.5 block">
                          Job Name <span className="text-red-500">*</span>
                        </Label>
                        <Input
                          id={`jobName-${job.jobId}`}
                          value={job.jobName}
                          onChange={(e) => handleJobTitleChange(index, "jobName", e.target.value)}
                          placeholder="Enter job name"
                          className="border-[#E5E7EB] dark:border-[#374151] focus:ring-[#3B82F6] focus:border-[#3B82F6]"
                        />
                      </div>
                      <div>
                        <Label className="text-sm font-medium mb-1.5 block">Pay Grade</Label>
                        <div className="flex items-center gap-2">
                          <span>Php</span>
                          <Input
                            value={payGradeMin}
                            onChange={e => {
                              let val = e.target.value.replace(/,/g, '');
                              if (!/^\d{0,10}$/.test(val)) {
                                return;
                              }
                              setPayGradeError("");
                              setPayGradeMin(formatPayGradeWhole(val));
                            }}
                            placeholder="min"
                            className="w-40"
                            maxLength={10}
                          />
                          <span>-</span>
                          <Input
                            value={payGradeMax}
                            onChange={e => {
                              let val = e.target.value.replace(/,/g, '');
                              if (!/^\d{0,10}$/.test(val)) {
                                return;
                              }
                              setPayGradeError("");
                              setPayGradeMax(formatPayGradeWhole(val));
                            }}
                            placeholder="max"
                            className="w-40"
                            maxLength={10}
                          />
                        </div>
                        {payGradeError && <span className="text-xs text-red-500">{payGradeError}</span>}
                      </div>
                    </div>
                    <div>
                      <Label htmlFor={`jobDescription-${job.jobId}`} className="text-sm font-medium mb-1.5 block">
                        Job Description
                      </Label>
                      <Textarea
                        id={`jobDescription-${job.jobId}`}
                        value={job.jobDescription}
                        onChange={(e) => handleJobTitleChange(index, "jobDescription", e.target.value)}
                        placeholder="Enter job description"
                        className="min-h-[80px] border-[#E5E7EB] dark:border-[#374151] focus:ring-[#3B82F6] focus:border-[#3B82F6]"
                      />
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>

            <Button
              variant="outline"
              onClick={handleAddJobTitle}
              className="w-full mt-4 border-dashed border-[#E5E7EB] dark:border-[#374151] hover:border-[#3B82F6] hover:bg-[#F0F9FF] dark:hover:bg-[#1E3A8A]/20 transition-colors"
            >
              <Plus className="h-4 w-4 mr-2" />
              Add Another Job Title
            </Button>
          </div>

          <div className="sticky bottom-0 z-10 bg-white dark:bg-[#1F2937] border-t border-[#E5E7EB] dark:border-[#374151] p-4 flex justify-between">
            <Button
              variant="outline"
              onClick={async () => {
                setIsAddJobTitlesDialogOpen(false);
                setAddJobTitles([]);
                setError(null);
                if (selectedDepartmentForJobs) {
                  const token = authService.getToken();
                  if (token) {
                    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/departments/${selectedDepartmentForJobs.departmentId}/job-titles`, {
                      headers: { Authorization: `Bearer ${token}` },
                    });
                    if (response.ok) {
                      const jobs = await response.json();
                      setJobTitles(jobs);
                    } else {
                      setJobTitles([]);
                    }
                  } else {
                    setJobTitles([]);
                  }
                  setIsViewJobsDialogOpen(true);
                }
              }}
              disabled={processing}
            >
              Cancel
            </Button>

            <Button
              onClick={handleSubmitJobTitles}
              disabled={processing || addJobTitles.length === 0 || addJobTitles.some((job) => !job.jobName.trim())}
              className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-sm hover:shadow-md"
            >
              {processing ? (
                <div className="flex items-center gap-2">
                  <div className="h-4 w-4 rounded-full border-2 border-white border-t-transparent animate-spin"></div>
                  <span>Creating...</span>
                </div>
              ) : (
                <>
                  <Plus className="h-4 w-4 mr-2" />
                  Create Job Titles
                </>
              )}
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Edit Job Title Dialog */}
      <Dialog
        open={isEditJobTitleDialogOpen}
        onOpenChange={setIsEditJobTitleDialogOpen}
      >
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Edit Job Title</DialogTitle>
            <DialogDescription>Update the job title details.</DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="edit-job-name" className="text-right">
                Name
              </Label>
              <Input
                id="edit-job-name"
                value={editJobTitleName}
                onChange={(e) => setEditJobTitleName(e.target.value)}
                className="col-span-3"
                placeholder="Job name"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="edit-job-description" className="text-right">
                Description
              </Label>
              <Textarea
                id="edit-job-description"
                value={editJobTitleDescription}
                onChange={(e) => setEditJobTitleDescription(e.target.value)}
                className="col-span-3 min-h-[40px] resize-y"
                placeholder="Job description"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="edit-job-pay-grade" className="text-right">
                Pay Grade
              </Label>
              <div className="col-span-3 flex items-center gap-2">
                <span>Php</span>
                <Input
                  value={editPayGradeMin}
                  onChange={e => {
                    let val = e.target.value.replace(/,/g, '');
                    if (!/^\d{0,10}$/.test(val)) {
                      return;
                    }
                    setPayGradeError("");
                    setEditPayGradeMin(formatPayGradeWhole(val));
                  }}
                  placeholder="min"
                  className="w-40"
                  maxLength={10}
                />
                <span>-</span>
                <Input
                  value={editPayGradeMax}
                  onChange={e => {
                    let val = e.target.value.replace(/,/g, '');
                    if (!/^\d{0,10}$/.test(val)) {
                      return;
                    }
                    setPayGradeError("");
                    setEditPayGradeMax(formatPayGradeWhole(val));
                  }}
                  placeholder="max"
                  className="w-40"
                  maxLength={10}
                />
              </div>
              {payGradeError && <span className="text-xs text-red-500 col-span-4">{payGradeError}</span>}
            </div>
          </div>
          <DialogFooter>
            <Button
              type="submit"
              onClick={async () => {
                if (!editJobTitleName.trim()) {
                  toast.error("Job name is required");
                  return;
                }
                const departmentId = selectedDepartmentForJobs?.departmentId || selectedJobTitle?.departmentId;
                if (!selectedJobTitle || !departmentId) {
                  toast.error("Department ID is required for job title update.");
                  return;
                }
                setProcessing(true);
                try {
                  const token = authService.getToken();
                  if (!token) {
                    router.push("/");
                    toast.error("Authentication required. Please log in.");
                    return;
                  }
                  // Use departmentId from context or job title
                  const min = parseInt(editPayGradeMin.replace(/,/g, ''), 10);
                  const max = parseInt(editPayGradeMax.replace(/,/g, ''), 10);
                  const payGrade = `Php ${min.toLocaleString("en-US")}.00 - ${max.toLocaleString("en-US")}.00`;
                  const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/job-titles/${selectedJobTitle.jobId}`, {
                    method: "PUT",
                    headers: {
                      "Content-Type": "application/json",
                      Authorization: `Bearer ${token}`,
                    },
                    body: JSON.stringify({
                      jobName: editJobTitleName,
                      jobDescription: editJobTitleDescription,
                      payGrade,
                      departmentId,
                    }),
                  });
                  if (!response.ok) {
                    throw new Error("Failed to update job title");
                  }
                  toast.success("Job title updated successfully");
                  setIsEditJobTitleDialogOpen(false);
                  setSelectedJobTitle(null);
                  // Refresh job titles
                  if (selectedDepartmentForJobs) {
                    const jobsRes = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/job-titles/department/${selectedDepartmentForJobs.departmentId}`, {
                      headers: { Authorization: `Bearer ${token}` },
                    });
                    if (jobsRes.ok) {
                      setJobTitles(await jobsRes.json());
                    }
                  }
                } catch (error) {
                  toast.error("Failed to update job title. Please try again.");
                } finally {
                  setProcessing(false);
                }
              }}
              disabled={processing}
              className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white"
            >
              {processing ? (
                <div className="flex items-center gap-2">
                  <div className="h-4 w-4 rounded-full border-2 border-white border-t-transparent animate-spin"></div>
                  <span>Updating...</span>
                </div>
              ) : (
                "Update Job Title"
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Job Title Dialog */}
      <Dialog open={isDeleteJobTitleDialogOpen} onOpenChange={setIsDeleteJobTitleDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Delete Job Title</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete the job title{' '}
              <span className="font-semibold">{jobTitleToDelete?.jobName}</span>? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsDeleteJobTitleDialogOpen(false)}
              disabled={isDeletingJobTitle}
            >
              Cancel
            </Button>
            <Button
              variant="destructive"
              onClick={async () => {
                if (jobTitleToDelete) {
                  await handleDeleteJobTitle(jobTitleToDelete.jobId);
                  setIsDeleteJobTitleDialogOpen(false);
                  setJobTitleToDelete(null);
                }
              }}
              disabled={isDeletingJobTitle}
            >
              {isDeletingJobTitle ? (
                <div className="flex items-center gap-2">
                  <div className="h-4 w-4 rounded-full border-2 border-white border-t-transparent animate-spin"></div>
                  <span>Deleting...</span>
                </div>
              ) : (
                "Delete"
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
