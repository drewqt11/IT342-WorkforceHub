"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Heart, Shield, DollarSign, Calendar, FileText, AlertCircle, PhilippinePeso, Users, X, CheckCircle2, User, Filter } from "lucide-react"
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
import { toast } from "sonner"
import { Toaster } from "sonner"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"

interface BenefitEnrollment {
  enrollmentId: string
  employeeId: string
  employeeName: string
  planId: string
  planName: string
  planType: string
  enrollmentDate: string
  status: string
  cancellationReason: string | null
  dependents: BenefitDependent[]
  dependentCount: number
}

interface BenefitDependent {
  dependentId: string
  enrollmentId: string
  name: string
  relationship: string
  birthdate: string
}

type FilterStatus = "all" | "active" | "cancelled"

export default function EnrolledBenefitsPage() {
  const [enrollments, setEnrollments] = useState<BenefitEnrollment[]>([])
  const [filteredEnrollments, setFilteredEnrollments] = useState<BenefitEnrollment[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedEnrollment, setSelectedEnrollment] = useState<BenefitEnrollment | null>(null)
  const [isCancelDialogOpen, setIsCancelDialogOpen] = useState(false)
  const [isDependentsDialogOpen, setIsDependentsDialogOpen] = useState(false)
  const [cancellationReason, setCancellationReason] = useState("")
  const [isCancelling, setIsCancelling] = useState(false)
  const [activeFilter, setActiveFilter] = useState<FilterStatus>("active")

  useEffect(() => {
    const fetchEnrollments = async () => {
      try {
        setLoading(true)
        const token = authService.getToken()

        if (!token) {
          throw new Error("No authentication token found")
        }

        const response = await fetch("/api/employee/benefit-enrollments", {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        })

        if (!response.ok) {
          throw new Error("Failed to fetch enrollments")
        }

        const data = await response.json()
        setEnrollments(data)
        setFilteredEnrollments(data)
      } catch (err) {
        console.error("Error fetching data:", err)
        setError(err instanceof Error ? err.message : "Failed to fetch data")
      } finally {
        setLoading(false)
      }
    }

    fetchEnrollments()
  }, [])

  useEffect(() => {
    // Filter enrollments based on active filter
    if (activeFilter === "all") {
      setFilteredEnrollments(enrollments)
    } else {
      setFilteredEnrollments(
        enrollments.filter(enrollment => 
          enrollment.status.toLowerCase() === activeFilter
        )
      )
    }
  }, [activeFilter, enrollments])

  const handleCancelClick = (enrollment: BenefitEnrollment) => {
    setSelectedEnrollment(enrollment)
    setIsCancelDialogOpen(true)
  }

  const handleCancel = async () => {
    if (!selectedEnrollment) return

    try {
      setIsCancelling(true)
      const token = authService.getToken()

      if (!token) {
        throw new Error("No authentication token found")
      }

      const response = await fetch(`/api/benefit-enrollments/${selectedEnrollment.enrollmentId}/cancel`, {
        method: "PATCH",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ cancellationReason }),
      })

      if (!response.ok) {
        throw new Error("Failed to cancel enrollment")
      }

      // Update the enrollment status in the local state
      setEnrollments(prevEnrollments =>
        prevEnrollments.map(enrollment =>
          enrollment.enrollmentId === selectedEnrollment.enrollmentId
            ? { ...enrollment, status: "Cancelled", cancellationReason }
            : enrollment
        )
      )

      toast.success("Successfully cancelled enrollment")
      setIsCancelDialogOpen(false)
      setCancellationReason("")
      setSelectedEnrollment(null)
    } catch (err) {
      console.error("Error cancelling enrollment:", err)
      toast.error("Failed to cancel enrollment", {
        description: err instanceof Error ? err.message : "An unexpected error occurred"
      })
    } finally {
      setIsCancelling(false)
    }
  }

  const handleViewDependents = (enrollment: BenefitEnrollment) => {
    setSelectedEnrollment(enrollment)
    setIsDependentsDialogOpen(true)
  }

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case "active":
        return "bg-[#10B981] text-white"
      case "cancelled":
        return "bg-[#EF4444] text-white"
      default:
        return "bg-[#6B7280] text-white"
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
                  <FileText className="h-5 w-5 text-white" />
                </div>
                Enrolled Benefits
              </h1>
              <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">View your enrolled benefit plans</p>
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
            <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">Error Loading Enrollments</h3>
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
                <FileText className="h-5 w-5 text-white" />
              </div>
              Enrolled Benefits
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">View your enrolled benefit plans</p>
          </div>
          <div className="flex flex-col items-end gap-3">
            <div className="flex items-center gap-2">
              <Filter className="h-4 w-4 text-[#6B7280]" />
              <span className="text-sm text-[#6B7280]">Filter by:</span>
            </div>
            <div className="flex gap-2">
              <Button
                variant={activeFilter === "active" ? "default" : "outline"}
                size="sm"
                onClick={() => setActiveFilter("active")}
                className={cn(
                  "transition-all duration-200",
                  activeFilter === "active" 
                    ? "bg-[#10B981] text-white hover:bg-[#059669]" 
                    : "border-[#10B981] text-[#10B981] hover:bg-[#10B981] hover:text-white"
                )}
              >
                Active
              </Button>
              <Button
                variant={activeFilter === "cancelled" ? "default" : "outline"}
                size="sm"
                onClick={() => setActiveFilter("cancelled")}
                className={cn(
                  "transition-all duration-200",
                  activeFilter === "cancelled" 
                    ? "bg-[#EF4444] text-white hover:bg-[#DC2626]" 
                    : "border-[#EF4444] text-[#EF4444] hover:bg-[#EF4444] hover:text-white"
                )}
              >
                Cancelled
              </Button>
              <Button
                variant={activeFilter === "all" ? "default" : "outline"}
                size="sm"
                onClick={() => setActiveFilter("all")}
                className={cn(
                  "transition-all duration-200",
                  activeFilter === "all" 
                    ? "bg-[#3B82F6] text-white hover:bg-[#2563EB]" 
                    : "border-[#3B82F6] text-[#3B82F6] hover:bg-[#3B82F6] hover:text-white"
                )}
              >
                All
              </Button>
            </div>
          </div>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {Array.from({ length: 6 }).map((_, index) => (
              <div key={index} className="flex flex-col h-full">
                <Skeleton className="h-[300px] w-full rounded-lg" />
              </div>
            ))}
          </div>
        ) : error ? (
          <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
            <div className="relative w-16 h-16 mx-auto mb-4">
              <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] opacity-20 animate-pulse"></div>
              <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                <AlertCircle className="h-8 w-8 text-[#EF4444] dark:text-[#EF4444]" />
              </div>
            </div>
            <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">Error Loading Enrollments</h3>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto mb-6">{error}</p>
          </div>
        ) : filteredEnrollments.length === 0 ? (
          <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
            <div className="relative w-16 h-16 mx-auto mb-4">
              <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] opacity-20 animate-pulse"></div>
              <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                <FileText className="h-8 w-8 text-[#6B7280] dark:text-[#9CA3AF]" />
              </div>
            </div>
            <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">No Enrollments Found</h3>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto">
              {activeFilter === "all" 
                ? "You haven't enrolled in any benefit plans yet."
                : `No ${activeFilter} enrollments found.`}
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredEnrollments.map((enrollment) => (
              <div key={enrollment.enrollmentId} className="relative h-full">
                <div className="h-2 w-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] rounded-t-lg absolute top-1.5 left-0"></div>
                <Card className="overflow-hidden border border-[#E5E7EB] dark:border-[#374151] shadow-md hover:shadow-lg transition-all duration-200 flex flex-col h-full mt-1.5">
                  <CardHeader className="p-4 pb-2 flex flex-col space-y-2">
                    <div className="flex items-center justify-between w-full -mt-5">
                      <div className="w-10 h-10 rounded-full bg-[#EFF6FF] dark:bg-[#1E3A8A]/30 flex items-center justify-center">
                        <FileText className="h-5 w-5 text-[#3B82F6]" />
                      </div>
                      <Badge className={getStatusColor(enrollment.status)}>
                        {enrollment.status}
                      </Badge>
                    </div>
                    <CardTitle className="text-lg font-semibold text-[#3B82F6]">
                      {enrollment.planName}
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="p-4 pt-0 flex-grow flex flex-col justify-between">
                    <div className="space-y-3">
                      <div className="flex items-center text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                        <Shield className="h-4 w-4 min-w-4 mr-1.5" />
                        <span>Plan Type: {enrollment.planType}</span>
                      </div>

                      <div className="flex items-center text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                        <Calendar className="h-4 w-4 min-w-4 mr-1.5" />
                        <span>Enrolled: {format(new Date(enrollment.enrollmentDate), "MMM d, yyyy")}</span>
                      </div>

                      {enrollment.dependentCount > 0 && (
                        <div className="flex items-center text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                          <Users className="h-4 w-4 min-w-4 mr-1.5" />
                          <span>{enrollment.dependentCount} Dependent{enrollment.dependentCount > 1 ? 's' : ''}</span>
                        </div>
                      )}

                      {enrollment.cancellationReason && (
                        <div className="mt-2">
                          <p className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">Cancellation Reason:</p>
                          <p className="text-sm text-[#4B5563] dark:text-[#D1D5DB] break-words whitespace-pre-wrap">
                            {enrollment.cancellationReason}
                          </p>
                        </div>
                      )}
                    </div>

                    <div className="mt-4 pt-3 border-t border-[#E5E7EB] dark:border-[#374151] text-xs text-[#6B7280] dark:text-[#9CA3AF]">
                      <div className="flex justify-between items-center mb-3">
                        <span>ID: {enrollment.enrollmentId}</span>
                      </div>
                      <div className="space-y-2">
                        {enrollment.dependentCount > 0 && (
                          <Button 
                            variant="outline"
                            className="w-full border-[#3B82F6] text-[#3B82F6] hover:bg-[#3B82F6] hover:text-white transition-all duration-200"
                            onClick={() => handleViewDependents(enrollment)}
                          >
                            <Users className="h-4 w-4 mr-2" />
                            View Dependents
                          </Button>
                        )}
                        {enrollment.status === "Active" && (
                          <Button 
                            className="w-full bg-red-600 hover:bg-red-700 text-white transition-all duration-200"
                            onClick={() => handleCancelClick(enrollment)}
                          >
                            Cancel Enrollment
                          </Button>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>
            ))}
          </div>
        )}
      </div>

      <Dialog open={isCancelDialogOpen} onOpenChange={setIsCancelDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Cancel Enrollment</DialogTitle>
            <DialogDescription>
              Are you sure you want to cancel your enrollment in {selectedEnrollment?.planName}? Please provide a reason for cancellation.
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="cancellationReason">Cancellation Reason</Label>
              <Textarea
                id="cancellationReason"
                value={cancellationReason}
                onChange={(e) => setCancellationReason(e.target.value)}
                placeholder="Enter your reason for cancellation"
                className="min-h-[100px]"
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setIsCancelDialogOpen(false)
                setCancellationReason("")
              }}
            >
              Cancel
            </Button>
            <Button
              onClick={handleCancel}
              disabled={isCancelling || !cancellationReason.trim()}
              className="bg-red-600 hover:bg-red-700"
            >
              {isCancelling ? "Cancelling..." : "Confirm Cancellation"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={isDependentsDialogOpen} onOpenChange={setIsDependentsDialogOpen}>
        <DialogContent className="sm:max-w-[600px]">
          <DialogHeader>
            <DialogTitle>Dependents for {selectedEnrollment?.planName}</DialogTitle>
            <DialogDescription>
              View the list of dependents enrolled in this benefit plan.
            </DialogDescription>
          </DialogHeader>

          <div className="py-4">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Relationship</TableHead>
                  <TableHead>Birthdate</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {selectedEnrollment?.dependents.map((dependent) => (
                  <TableRow key={dependent.dependentId}>
                    <TableCell className="font-medium">{dependent.name}</TableCell>
                    <TableCell>{dependent.relationship}</TableCell>
                    <TableCell>{format(new Date(dependent.birthdate), "MMM d, yyyy")}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsDependentsDialogOpen(false)}
            >
              Close
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
