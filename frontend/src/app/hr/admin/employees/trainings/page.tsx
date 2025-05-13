"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { GraduationCap, Calendar, FileText, AlertCircle, Plus, X, CheckCircle2, Pencil, Trash2 } from "lucide-react"
import { format } from "date-fns"
import { authService } from "@/lib/auth"
import { Skeleton } from "@/components/ui/skeleton"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { toast } from "sonner"
import { Toaster } from "sonner"

interface TrainingProgram {
  trainingId: string
  title: string
  description: string
  provider: string
  startDate: string
  endDate: string
  trainingMode: string
  isActive: boolean
  createdById: string
  createdByName: string
  enrollmentCount: number
}

export default function TrainingProgramsPage() {
  const [programs, setPrograms] = useState<TrainingProgram[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedProgram, setSelectedProgram] = useState<TrainingProgram | null>(null)
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false)
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    provider: "",
    startDate: "",
    endDate: "",
    trainingMode: "",
    isActive: true
  })

  useEffect(() => {
    fetchTrainingPrograms()
  }, [])

  const fetchTrainingPrograms = async () => {
    try {
      setLoading(true)
      const token = authService.getToken()

      if (!token) {
        throw new Error("No authentication token found")
      }

      const response = await fetch("/api/training-programs", {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      if (!response.ok) {
        throw new Error("Failed to fetch training programs")
      }

      const data = await response.json()
      setPrograms(data)
    } catch (err) {
      console.error("Error fetching data:", err)
      setError(err instanceof Error ? err.message : "Failed to fetch data")
    } finally {
      setLoading(false)
    }
  }

  const handleCreateClick = () => {
    setSelectedProgram(null)
    setFormData({
      title: "",
      description: "",
      provider: "",
      startDate: "",
      endDate: "",
      trainingMode: "",
      isActive: true
    })
    setIsDialogOpen(true)
  }

  const handleEditClick = (program: TrainingProgram) => {
    setSelectedProgram(program)
    setFormData({
      title: program.title,
      description: program.description || "",
      provider: program.provider || "",
      startDate: format(new Date(program.startDate), "yyyy-MM-dd"),
      endDate: format(new Date(program.endDate), "yyyy-MM-dd"),
      trainingMode: program.trainingMode,
      isActive: program.isActive
    })
    setIsDialogOpen(true)
  }

  const handleDeleteClick = (program: TrainingProgram) => {
    setSelectedProgram(program)
    setIsDeleteDialogOpen(true)
  }

  const handleSubmit = async () => {
    try {
      setIsSubmitting(true)
      const token = authService.getToken()

      if (!token) {
        throw new Error("No authentication token found")
      }

      const url = selectedProgram 
        ? `/api/training-programs/${selectedProgram.trainingId}`
        : "/api/training-programs"
      
      const method = selectedProgram ? "PUT" : "POST"

      const response = await fetch(url, {
        method,
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(formData),
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => null)
        throw new Error(errorData?.message || "Failed to save training program")
      }

      toast.success(`Training program ${selectedProgram ? "updated" : "created"} successfully`)
      setIsDialogOpen(false)
      fetchTrainingPrograms()
    } catch (err) {
      console.error("Error saving training program:", err)
      toast.error("Failed to save training program", {
        description: err instanceof Error ? err.message : "An unexpected error occurred"
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleDelete = async () => {
    if (!selectedProgram) return

    try {
      setIsSubmitting(true)
      const token = authService.getToken()

      if (!token) {
        throw new Error("No authentication token found")
      }

      const response = await fetch(`/api/training-programs/${selectedProgram.trainingId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => null)
        throw new Error(errorData?.message || "Failed to delete training program")
      }

      toast.success("Training program deleted successfully")
      setIsDeleteDialogOpen(false)
      fetchTrainingPrograms()
    } catch (err) {
      console.error("Error deleting training program:", err)
      toast.error("Failed to delete training program", {
        description: err instanceof Error ? err.message : "An unexpected error occurred"
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
        <div className="w-full max-w-6xl mx-auto space-y-6">
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
            <div>
              <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white flex items-center gap-2">
                <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-1 shadow-md">
                  <GraduationCap className="h-5 w-5 text-white" />
                </div>
                Training Programs
              </h1>
              <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">Manage training programs</p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {Array.from({ length: 6 }).map((_, index) => (
              <div key={index} className="flex flex-col h-full">
                <Skeleton className="h-[300px] w-full rounded-lg" />
              </div>
            ))}
          </div>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
        <div className="w-full max-w-6xl mx-auto">
          <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
            <div className="relative w-16 h-16 mx-auto mb-4">
              <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#EF4444] to-[#B91C1C] opacity-20 animate-pulse"></div>
              <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                <AlertCircle className="h-8 w-8 text-[#EF4444] dark:text-[#EF4444]" />
              </div>
            </div>
            <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">Error Loading Programs</h3>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto mb-6">{error}</p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
      <Toaster richColors position="top-right" />
      <div className="w-full max-w-6xl mx-auto space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white flex items-center gap-2">
              <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-1 shadow-md">
                <GraduationCap className="h-5 w-5 text-white" />
              </div>
              Training Programs
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">Manage training programs</p>
          </div>
          <div className="flex items-center gap-4">
            <Badge
              variant="outline"
              className="bg-[#F0FDFA] text-[#14B8A6] border-[#99F6E4] dark:bg-[#134E4A]/30 dark:text-[#14B8A6] dark:border-[#134E4A] px-3 py-1.5"
            >
              {programs.length} programs
            </Badge>
            <Button
              onClick={handleCreateClick}
              className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white"
            >
              <Plus className="h-4 w-4 mr-2" />
              Create Program
            </Button>
          </div>
        </div>

        {programs.length === 0 ? (
          <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
            <div className="relative w-16 h-16 mx-auto mb-4">
              <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] opacity-20 animate-pulse"></div>
              <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                <GraduationCap className="h-8 w-8 text-[#6B7280] dark:text-[#9CA3AF]" />
              </div>
            </div>
            <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">No Training Programs Found</h3>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto">
              Get started by creating your first training program.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {programs.map((program) => (
              <div key={program.trainingId} className="relative h-full">
                <div className="h-2 w-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] rounded-t-lg absolute top-1.5 left-0"></div>
                <Card className="overflow-hidden border border-[#E5E7EB] dark:border-[#374151] shadow-md hover:shadow-lg transition-all duration-200 flex flex-col h-full mt-1.5">
                  <CardHeader className="p-4 pb-2 flex flex-col space-y-2">
                    <div className="flex items-center justify-between w-full -mt-5">
                      <div className="w-10 h-10 rounded-full bg-[#EFF6FF] dark:bg-[#1E3A8A]/30 flex items-center justify-center">
                        <GraduationCap className="h-5 w-5 text-[#3B82F6]" />
                      </div>
                      <div className="flex items-center gap-2">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleEditClick(program)}
                          className="h-8 w-8"
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleDeleteClick(program)}
                          className="h-8 w-8 text-red-500 hover:text-red-600 hover:bg-red-50"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                    <CardTitle className="text-lg font-semibold text-[#3B82F6]">
                      {program.title}
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="p-4 pt-0 flex-grow flex flex-col justify-between">
                    <div className="space-y-3">
                      {program.provider && (
                        <div className="flex items-center text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                          <span>Provider: {program.provider}</span>
                        </div>
                      )}

                      <div className="flex items-center text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                        <Calendar className="h-4 w-4 min-w-4 mr-1.5" />
                        <span>
                          {format(new Date(program.startDate), "MMM d, yyyy")} - {format(new Date(program.endDate), "MMM d, yyyy")}
                        </span>
                      </div>

                      {program.description && (
                        <div className="mt-2">
                          <p className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">Description:</p>
                          <p className="text-sm text-[#4B5563] dark:text-[#D1D5DB] break-words whitespace-pre-wrap">
                            {program.description}
                          </p>
                        </div>
                      )}

                      <div className="space-y-2">
                        <p className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">Training Mode:</p>
                        <p className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                          {program.trainingMode}
                        </p>
                      </div>

                      <div className="flex items-center gap-2">

                          <Badge className="bg-green-600">
                            Active
                          </Badge>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>
            ))}
          </div>
        )}
      </div>

      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>{selectedProgram ? "Edit Training Program" : "Create Training Program"}</DialogTitle>
            <DialogDescription>
              {selectedProgram ? "Update the training program details below." : "Fill in the details to create a new training program."}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="title">Title</Label>
              <Input
                id="title"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                placeholder="Enter program title"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Enter program description"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="provider">Provider</Label>
              <Input
                id="provider"
                value={formData.provider}
                onChange={(e) => setFormData({ ...formData, provider: e.target.value })}
                placeholder="Enter provider name"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="startDate">Start Date</Label>
                <Input
                  id="startDate"
                  type="date"
                  value={formData.startDate}
                  onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="endDate">End Date</Label>
                <Input
                  id="endDate"
                  type="date"
                  value={formData.endDate}
                  onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="trainingMode">Training Mode</Label>
              <Select
                value={formData.trainingMode}
                onValueChange={(value) => setFormData({ ...formData, trainingMode: value })}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select training mode" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Online">Online</SelectItem>
                  <SelectItem value="In-person">In-person</SelectItem>
                  <SelectItem value="Hybrid">Hybrid</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="flex items-center space-x-2">
              <input
                type="checkbox"
                id="isActive"
                checked={formData.isActive}
                onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                className="h-4 w-4 rounded border-gray-300"
              />
              <Label htmlFor="isActive">Active</Label>
            </div>
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsDialogOpen(false)}
            >
              Cancel
            </Button>
            <Button
              onClick={handleSubmit}
              disabled={isSubmitting}
              className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488]"
            >
              {isSubmitting ? "Saving..." : (selectedProgram ? "Update Program" : "Create Program")}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Delete Training Program</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete this training program? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsDeleteDialogOpen(false)}
            >
              Cancel
            </Button>
            <Button
              onClick={handleDelete}
              disabled={isSubmitting}
              className="bg-red-600 hover:bg-red-700 text-white"
            >
              {isSubmitting ? "Deleting..." : "Delete Program"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
