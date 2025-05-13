"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { authService } from "@/lib/auth"
import { Users, UserPlus, CalendarDays, AlertCircle } from "lucide-react"
import { Toaster, toast } from "sonner"
import { format } from "date-fns"
import { Skeleton } from "@/components/ui/skeleton"
import { cn } from "@/lib/utils"

interface BenefitDependent {
  dependentId: string
  name: string
  relationship: string
  birthdate: string
}

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

export default function BenefitEnrollmentPage() {
  const router = useRouter()
  const [loading, setLoading] = useState(true)
  const [enrollments, setEnrollments] = useState<BenefitEnrollment[]>([])
  const [selectedEnrollment, setSelectedEnrollment] = useState<BenefitEnrollment | null>(null)

  useEffect(() => {
    fetchEnrollments()
  }, [])

  const fetchEnrollments = async () => {
    try {
      setLoading(true)
      const token = authService.getToken()
      if (!token) {
        router.push("/")
        return
      }

      // Fetch all benefit plans first
      const plansResponse = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/benefit-plans`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!plansResponse.ok) {
        throw new Error("Failed to fetch benefit plans")
      }

      const plans = await plansResponse.json()
      
      // Fetch enrollments for each plan
      const allEnrollments: BenefitEnrollment[] = []
      for (const plan of plans) {
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/benefit-plans/${plan.planId}/enrollments`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        })

        if (!response.ok) {
          console.error(`Failed to fetch enrollments for plan ${plan.planId}`)
          continue
        }

        const planEnrollments = await response.json()
        allEnrollments.push(...planEnrollments)
      }

      setEnrollments(allEnrollments)
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to fetch enrollments")
    } finally {
      setLoading(false)
    }
  }

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case "active":
        return "bg-green-500"
      case "cancelled":
        return "bg-red-500"
      case "expired":
        return "bg-yellow-500"
      default:
        return "bg-gray-500"
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
      <Toaster position="top-right" richColors className="mt-24" style={{ top: "6rem", right: "1rem" }} />
      <div className="w-full max-w-7xl mx-auto space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white flex items-center gap-2">
              <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-1 shadow-md">
                <Users className="h-5 w-5 text-white" />
              </div>
              Benefit Enrollments
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
              View and manage benefit plan enrollments
            </p>
          </div>
        </div>

        <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-xl overflow-hidden bg-white dark:bg-[#1F2937]">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
          <CardHeader className="bg-[#F9FAFB] dark:bg-[#111827] border-b border-[#E5E7EB] dark:border-[#374151]">
            <div className="flex flex-col md:flex-row justify-between md:items-center gap-4">
              <div>
                <CardTitle className="text-xl text-[#1F2937] dark:text-white flex items-center gap-2">
                  <CalendarDays className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
                  Enrollment List
                </CardTitle>
                <CardDescription className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
                  View all benefit plan enrollments and their dependents
                </CardDescription>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">{enrollments.length} enrollments</span>
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
            ) : enrollments.length === 0 ? (
              <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
                <div className="relative w-16 h-16 mx-auto mb-4">
                  <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] opacity-20 animate-pulse"></div>
                  <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                    <Users className="h-8 w-8 text-[#6B7280] dark:text-[#9CA3AF]" />
                  </div>
                </div>
                <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">No enrollments found</h3>
                <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto mb-6">
                  There are no benefit plan enrollments in the system yet.
                </p>
              </div>
            ) : (
              <div className="rounded-lg border border-[#E5E7EB] dark:border-[#374151] overflow-hidden">
                <Table>
                  <TableHeader className="bg-[#F9FAFB] dark:bg-[#111827]">
                    <TableRow className="hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151]">
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium w-12 text-center">No.</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Employee</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Plan</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Type</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Enrollment Date</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Status</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Dependents</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium pl-16">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {enrollments.map((enrollment, index) => (
                      <TableRow
                        key={enrollment.enrollmentId}
                        className={cn(
                          "hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151] group transition-colors",
                          index % 2 === 0 ? "bg-[#F9FAFB] dark:bg-[#111827]/50" : "",
                        )}
                      >
                        <TableCell className="text-center text-[#4B5563] dark:text-[#D1D5DB] font-medium">{index + 1}</TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">{enrollment.employeeName}</TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">{enrollment.planName}</TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">{enrollment.planType}</TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">{format(new Date(enrollment.enrollmentDate), "MMM dd, yyyy")}</TableCell>
                        <TableCell>
                          <Badge className={getStatusColor(enrollment.status)}>
                            {enrollment.status}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">{enrollment.dependentCount}</TableCell>
                        <TableCell className="text-left">
                          <div className="flex justify-end gap-4">
                            <Dialog>
                              <DialogTrigger asChild>
                                <Button
                                  variant="outline"
                                  size="sm"
                                  onClick={() => setSelectedEnrollment(enrollment)}
                                  className="flex items-center gap-2 border-[#BFDBFE] text-[#3B82F6] hover:bg-[#EFF6FF] dark:border-[#1E3A8A] dark:text-[#3B82F6] dark:hover:bg-[#1E3A8A]/30"
                                >
                                  <UserPlus className="h-4 w-4" />
                                  View Dependents
                                </Button>
                              </DialogTrigger>
                              <DialogContent className="max-w-2xl">
                                <DialogHeader>
                                  <DialogTitle>Dependent Information</DialogTitle>
                                </DialogHeader>
                                <div className="space-y-4">
                                  <div className="grid grid-cols-2 gap-4">
                                    <div>
                                      <h3 className="font-semibold text-sm text-[#6B7280] dark:text-[#9CA3AF]">Employee</h3>
                                      <p className="text-[#1F2937] dark:text-white">{enrollment.employeeName}</p>
                                    </div>
                                    <div>
                                      <h3 className="font-semibold text-sm text-[#6B7280] dark:text-[#9CA3AF]">Plan</h3>
                                      <p className="text-[#1F2937] dark:text-white">{enrollment.planName}</p>
                                    </div>
                                  </div>
                                  <div className="border-t border-[#E5E7EB] dark:border-[#374151] pt-4">
                                    <h3 className="font-semibold mb-2">Dependents</h3>
                                    {enrollment.dependents.length > 0 ? (
                                      <div className="space-y-4">
                                        {enrollment.dependents.map((dependent) => (
                                          <div
                                            key={dependent.dependentId}
                                            className="p-4 rounded-lg border border-[#E5E7EB] dark:border-[#374151] bg-[#F9FAFB] dark:bg-[#111827]"
                                          >
                                            <div className="grid grid-cols-2 gap-4">
                                              <div>
                                                <h4 className="font-semibold text-sm text-[#6B7280] dark:text-[#9CA3AF]">Name</h4>
                                                <p className="text-[#1F2937] dark:text-white">{dependent.name}</p>
                                              </div>
                                              <div>
                                                <h4 className="font-semibold text-sm text-[#6B7280] dark:text-[#9CA3AF]">Relationship</h4>
                                                <p className="text-[#1F2937] dark:text-white">{dependent.relationship}</p>
                                              </div>
                                              <div>
                                                <h4 className="font-semibold text-sm text-[#6B7280] dark:text-[#9CA3AF]">Birthdate</h4>
                                                <p className="text-[#1F2937] dark:text-white">
                                                  {format(new Date(dependent.birthdate), "MMM dd, yyyy")}
                                                </p>
                                              </div>
                                            </div>
                                          </div>
                                        ))}
                                      </div>
                                    ) : (
                                      <div className="flex items-center gap-2 text-[#6B7280] dark:text-[#9CA3AF]">
                                        <AlertCircle className="h-4 w-4" />
                                        <span>No dependents found</span>
                                      </div>
                                    )}
                                  </div>
                                </div>
                              </DialogContent>
                            </Dialog>
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
    </div>
  )
}
