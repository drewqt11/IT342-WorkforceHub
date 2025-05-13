"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Heart, Shield, DollarSign, Calendar, FileText, AlertCircle, PhilippinePeso, Plus, X, CheckCircle2 } from "lucide-react"
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
import { toast } from "sonner"
import { Toaster } from "sonner"

interface HealthBenefit {
  planId: string
  planName: string
  description: string
  provider: string
  eligibility: string
  planType: string
  maxCoverage: number
  createdAt: string
  isActive: boolean
}

interface Dependent {
  name: string
  relationship: string
  birthdate: string
}

interface EnrollmentStatus {
  [key: string]: boolean
}

export default function HealthBenefitsPage() {
  const [benefits, setBenefits] = useState<HealthBenefit[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedPlan, setSelectedPlan] = useState<HealthBenefit | null>(null)
  const [isEnrollDialogOpen, setIsEnrollDialogOpen] = useState(false)
  const [dependents, setDependents] = useState<Dependent[]>([{ name: "", relationship: "", birthdate: "" }])
  const [isEnrolling, setIsEnrolling] = useState(false)
  const [enrolledPlans, setEnrolledPlans] = useState<EnrollmentStatus>({})

  useEffect(() => {
    const fetchHealthBenefits = async () => {
      try {
        setLoading(true)
        const token = authService.getToken()

        if (!token) {
          throw new Error("No authentication token found")
        }

        // Fetch health benefits
        const response = await fetch("/api/benefit-plans/type/Health", {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        })

        if (!response.ok) {
          throw new Error("Failed to fetch health benefits")
        }

        const data = await response.json()
        setBenefits(data)

        // Fetch current enrollments
        const enrollmentsResponse = await fetch("/api/employee/benefit-enrollments", {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        })

        if (enrollmentsResponse.ok) {
          const enrollments = await enrollmentsResponse.json()
          const enrollmentStatus: EnrollmentStatus = {}
          enrollments.forEach((enrollment: any) => {
            enrollmentStatus[enrollment.planId] = true
          })
          setEnrolledPlans(enrollmentStatus)
        }
      } catch (err) {
        console.error("Error fetching data:", err)
        setError(err instanceof Error ? err.message : "Failed to fetch data")
      } finally {
        setLoading(false)
      }
    }

    fetchHealthBenefits()
  }, [])

  const getStatusColor = (isActive: boolean) => {
    return isActive ? "bg-[#10B981] text-white" : "bg-[#6B7280] text-white"
  }

  const handleEnrollClick = (benefit: HealthBenefit) => {
    setSelectedPlan(benefit)
    setIsEnrollDialogOpen(true)
  }

  const addDependent = () => {
    setDependents([...dependents, { name: "", relationship: "", birthdate: "" }])
  }

  const removeDependent = (index: number) => {
    setDependents(dependents.filter((_, i) => i !== index))
  }

  const updateDependent = (index: number, field: keyof Dependent, value: string) => {
    const newDependents = [...dependents]
    newDependents[index] = { ...newDependents[index], [field]: value }
    setDependents(newDependents)
  }

  const handleEnroll = async () => {
    if (!selectedPlan) return

    try {
      setIsEnrolling(true)
      const token = authService.getToken()

      if (!token) {
        throw new Error("No authentication token found")
      }

      // First, enroll in the benefit plan
      const enrollResponse = await fetch(`/api/employee/benefit-enrollments?planId=${selectedPlan.planId}`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      if (!enrollResponse.ok) {
        const errorData = await enrollResponse.json().catch(() => null)
        throw new Error(errorData?.message || "Failed to enroll in benefit plan")
      }

      const enrollment = await enrollResponse.json()

      // Then, add dependents if any
      const validDependents = dependents.filter(d => d.name && d.relationship)
      if (validDependents.length > 0) {
        const dependentPromises = validDependents.map(dependent =>
          fetch(`/api/benefit-enrollments/${enrollment.enrollmentId}/dependents`, {
            method: "POST",
            headers: {
              Authorization: `Bearer ${token}`,
              "Content-Type": "application/json",
            },
            body: JSON.stringify(dependent),
          })
        )

        await Promise.all(dependentPromises)
      }

      // Update enrollment status
      setEnrolledPlans(prev => ({
        ...prev,
        [selectedPlan.planId]: true
      }))

      toast.success("Successfully enrolled in benefit plan", {
        description: validDependents.length > 0 
          ? `Added ${validDependents.length} dependent${validDependents.length > 1 ? 's' : ''}`
          : undefined
      })
      
      setIsEnrollDialogOpen(false)
      setDependents([{ name: "", relationship: "", birthdate: "" }])
      setSelectedPlan(null)
    } catch (err) {
      console.error("Error enrolling in benefit plan:", err)
      toast.error("Failed to enroll in benefit plan", {
        description: err instanceof Error ? err.message : "An unexpected error occurred"
      })
    } finally {
      setIsEnrolling(false)
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
                  <Heart className="h-5 w-5 text-white" />
                </div>
                Health Benefits
              </h1>
              <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">View your health benefit plans</p>
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
            <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">Error Loading Benefits</h3>
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
                <Heart className="h-5 w-5 text-white" />
              </div>
              Health Benefits
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">View your health benefit plans</p>
          </div>
          <Badge
            variant="outline"
            className="bg-[#F0FDFA] text-[#14B8A6] border-[#99F6E4] dark:bg-[#134E4A]/30 dark:text-[#14B8A6] dark:border-[#134E4A] px-3 py-1.5"
          >
            {benefits.length} plans available
          </Badge>
        </div>

        {benefits.length === 0 ? (
          <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
            <div className="relative w-16 h-16 mx-auto mb-4">
              <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] opacity-20 animate-pulse"></div>
              <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                <Heart className="h-8 w-8 text-[#6B7280] dark:text-[#9CA3AF]" />
              </div>
            </div>
            <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">No Health Benefits Found</h3>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto">
              There are no health benefit plans available at the moment.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {benefits.map((benefit) => (
              <div key={benefit.planId} className="relative h-full">
                <div className="h-2 w-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] rounded-t-lg absolute top-1.5 left-0"></div>
                <Card className="overflow-hidden border border-[#E5E7EB] dark:border-[#374151] shadow-md hover:shadow-lg transition-all duration-200 flex flex-col h-full mt-1.5">
                  <CardHeader className="p-4 pb-2 flex flex-col space-y-2">
                    <div className="flex items-center justify-between w-full -mt-5">
                      <div className="w-10 h-10 rounded-full bg-[#EFF6FF] dark:bg-[#1E3A8A]/30 flex items-center justify-center">
                        <Heart className="h-5 w-5 text-[#3B82F6]" />
                      </div>
                      <Badge className="bg-green-600">
                       Active
                      </Badge>
                    </div>
                    <CardTitle className="text-lg font-semibold text-[#3B82F6]">
                      {benefit.planName}
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="p-4 pt-0 flex-grow flex flex-col justify-between">
                    <div className="space-y-3">
                      <div className="flex items-center text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                        <Shield className="h-4 w-4 min-w-4 mr-1.5" />
                        <span>{benefit.provider}</span>
                      </div>

                      <div className="flex items-center text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                        <span>Max Coverage: ₱ {benefit.maxCoverage.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
                      </div>

                      {benefit.description && (
                        <div className="mt-2">
                          <p className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">Description:</p>
                          <p className="text-sm text-[#4B5563] dark:text-[#D1D5DB] break-words whitespace-pre-wrap">
                            {benefit.description}
                          </p>
                        </div>
                      )}

                      {benefit.eligibility && (
                        <div className="space-y-2">
                          <p className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">Eligibility:</p>
                          <p className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                            {benefit.eligibility}
                          </p>
                        </div>
                      )}

                    
                    </div>

                    <div className="mt-4 pt-3 border-t border-[#E5E7EB] dark:border-[#374151] text-xs text-[#6B7280] dark:text-[#9CA3AF]">
                      <div className="flex justify-between items-center mb-3">
                        <span>ID: {benefit.planId}</span>
                        <div className="flex items-center">
                          <Calendar className="h-3 w-3 mr-1" />
                          <span>
                            Created: {format(new Date(benefit.createdAt), "MMM d, yyyy")}
                          </span>
                        </div>
                      </div>
                      <Button 
                        className={cn(
                          "w-full transition-all duration-200",
                          enrolledPlans[benefit.planId]
                            ? "bg-green-600 hover:bg-green-700 text-white"
                            : "bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white"
                        )}
                        onClick={() => !enrolledPlans[benefit.planId] && handleEnrollClick(benefit)}
                        disabled={enrolledPlans[benefit.planId]}
                      >
                        {enrolledPlans[benefit.planId] ? (
                          <span className="flex items-center gap-2">
                            <CheckCircle2 className="h-4 w-4" />
                            Enrolled
                          </span>
                        ) : (
                          "Enroll Now"
                        )}
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              </div>
            ))}
          </div>
        )}
      </div>

      <Dialog open={isEnrollDialogOpen} onOpenChange={setIsEnrollDialogOpen}>
        <DialogContent className="sm:max-w-[500px] max-h-[90vh] flex flex-col">
          <DialogHeader>
            <DialogTitle>Enroll in {selectedPlan?.planName}</DialogTitle>
            <DialogDescription>
              Please review the plan details and add any dependents you want to include in your enrollment.
            </DialogDescription>
          </DialogHeader>

          <div className="flex-1 overflow-y-auto space-y-4 py-4">
            <div className="space-y-2">
              <h4 className="font-medium">Plan Details</h4>
              <div className="text-sm text-[#6B7280]">
                <p>Provider: {selectedPlan?.provider}</p>
                <p>Max Coverage: ₱ {selectedPlan?.maxCoverage.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
                {selectedPlan?.eligibility && <p>Eligibility: {selectedPlan.eligibility}</p>}
              </div>
            </div>

            <div className="space-y-4">
              <div className="flex items-center justify-between bg-white dark:bg-[#1F2937] py-2 sticky -top-7 z-10 pt-5 pb-5">
                <h4 className="font-medium">Dependents</h4>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={addDependent}
                  className="flex items-center gap-1"
                >
                  <Plus className="h-4 w-4" />
                  Add Dependent
                </Button>
              </div>

              <div className="space-y-3">
                {dependents.map((dependent, index) => (
                  <div key={index} className="space-y-3 p-3 border rounded-lg relative">
                    {index > 0 && (
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="absolute top-2 right-2"
                        onClick={() => removeDependent(index)}
                      >
                        <X className="h-4 w-4" />
                      </Button>
                    )}
                    <div className="grid gap-3">
                      <div className="grid gap-2">
                        <Label htmlFor={`name-${index}`}>Name</Label>
                        <Input
                          id={`name-${index}`}
                          value={dependent.name}
                          onChange={(e) => updateDependent(index, "name", e.target.value)}
                          placeholder="Enter dependent's name"
                        />
                      </div>
                      <div className="grid gap-2">
                        <Label htmlFor={`relationship-${index}`}>Relationship</Label>
                        <Input
                          id={`relationship-${index}`}
                          value={dependent.relationship}
                          onChange={(e) => updateDependent(index, "relationship", e.target.value)}
                          placeholder="Enter relationship"
                        />
                      </div>
                      <div className="grid gap-2">
                        <Label htmlFor={`birthdate-${index}`}>Birthdate</Label>
                        <Input
                          id={`birthdate-${index}`}
                          type="date"
                          value={dependent.birthdate}
                          onChange={(e) => updateDependent(index, "birthdate", e.target.value)}
                        />
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <DialogFooter className="mt-4">
            <Button
              variant="outline"
              onClick={() => {
                setIsEnrollDialogOpen(false)
                setDependents([{ name: "", relationship: "", birthdate: "" }])
              }}
            >
              Cancel
            </Button>
            <Button
              onClick={handleEnroll}
              disabled={isEnrolling}
              className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488]"
            >
              {isEnrolling ? "Enrolling..." : "Confirm Enrollment"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
