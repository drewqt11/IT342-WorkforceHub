"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { authService } from "@/lib/auth"
import { CalendarDays, AlertCircle } from "lucide-react"
import { Toaster, toast } from "sonner"
import { cn } from "@/lib/utils"
import { format } from "date-fns"
import { Calendar } from "@/components/ui/calendar"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

interface LeaveRequestForm {
  leaveType: string
  startDate: Date | undefined
  endDate: Date | undefined
  reason: string
}

export default function LeaveRequestPage() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)
  const [formData, setFormData] = useState<LeaveRequestForm>({
    leaveType: "",
    startDate: undefined,
    endDate: undefined,
    reason: "",
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!formData.leaveType || !formData.startDate || !formData.endDate || !formData.reason) {
      toast.error("Please fill in all required fields")
      return
    }

    if (formData.endDate < formData.startDate) {
      toast.error("End date must be after start date")
      return
    }

    const today = new Date()
    today.setHours(0, 0, 0, 0)
    if (formData.startDate < today) {
      toast.error("Cannot request leave for past dates")
      return
    }

    try {
      setLoading(true)
      const token = authService.getToken()
      if (!token) {
        toast.error("Authentication required. Please login again.")
        router.push("/login")
        return
      }

      const selectedLeaveType = leaveTypes.find(type => type.value === formData.leaveType)
      if (!selectedLeaveType) {
        toast.error("Invalid leave type selected")
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/employee/leave-requests`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          leaveType: selectedLeaveType.label,
          startDate: formData.startDate.toISOString().split('T')[0],
          endDate: formData.endDate.toISOString().split('T')[0],
          reason: formData.reason,
        }),
      })

      if (!response.ok) {
        const errorData = await response.json()
        
        // Handle specific backend validation errors
        switch (errorData.message) {
          case "Leave type is required":
            toast.error("Please select a leave type")
            break
          case "Start and end dates are required":
            toast.error("Please select both start and end dates")
            break
          case "Cannot request leave for past dates":
            toast.error("Cannot request leave for past dates")
            break
          case "End date must be after start date":
            toast.error("End date must be after start date")
            break
          case "You already have a leave request for this period":
            toast.error("You already have a leave request for this period")
            break
          default:
            if (response.status === 401) {
              toast.error("Session expired. Please login again.")
              router.push("/login")
            } else if (response.status === 403) {
              toast.error("You don't have permission to submit leave requests")
            } else {
              toast.error(errorData.message || "Failed to submit leave request")
            }
        }
        return
      }

      toast.success("Leave request submitted successfully")
      setFormData({
        leaveType: "",
        startDate: undefined,
        endDate: undefined,
        reason: "",
      })
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to submit leave request")
    } finally {
      setLoading(false)
    }
  }

  const leaveTypes = [
    { value: "SL", label: "Sick Leave (SL)" },
    { value: "VL", label: "Vacation Leave (VL)" },
    { value: "ML", label: "Maternity Leave" },
    { value: "PL", label: "Paternity Leave" },
    { value: "PSL", label: "Parental Leave for Solo Parents" },
    { value: "BL", label: "Bereavement Leave" },
    { value: "EL", label: "Emergency Leave" },
    { value: "SWL", label: "Special Leave for Women" },
    { value: "SDL", label: "Special Leave for the Disabled" },
    { value: "HL", label: "Public Holidays (Holiday Leave)" },
    { value: "OTHER", label: "Other" }
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
      <Toaster position="top-right" richColors className="mt-24" style={{ top: "6rem", right: "1rem" }} />
      <div className="w-full max-w-2xl mx-auto space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white flex items-center gap-2">
              <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-1 shadow-md">
                <CalendarDays className="h-5 w-5 text-white" />
              </div>
              Request Leave
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
              Submit a new leave request
            </p>
          </div>
        </div>

        <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-xl overflow-hidden bg-white dark:bg-[#1F2937]">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
          <CardHeader className="bg-[#F9FAFB] dark:bg-[#111827] border-b border-[#E5E7EB] dark:border-[#374151]">
            <CardTitle className="text-xl text-[#1F2937] dark:text-white flex items-center gap-2">
              <CalendarDays className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
              Leave Request Form
            </CardTitle>
            <CardDescription className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
              Fill in the details for your leave request
            </CardDescription>
          </CardHeader>
          <CardContent className="p-6">
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="space-y-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">
                    Leave Type <span className="text-red-500">*</span>
                  </label>
                  <Select
                    value={formData.leaveType}
                    onValueChange={(value) => setFormData({ ...formData, leaveType: value })}
                  >
                    <SelectTrigger className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827]">
                      <SelectValue placeholder="Select leave type" />
                    </SelectTrigger>
                    <SelectContent className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#1F2937]">
                      {leaveTypes.map((type) => (
                        <SelectItem key={type.value} value={type.value}>
                          {type.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">
                      Start Date <span className="text-red-500">*</span>
                    </label>
                    <Popover>
                      <PopoverTrigger asChild>
                        <Button
                          variant="outline"
                          className={cn(
                            "w-full justify-start text-left font-normal border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827]",
                            !formData.startDate && "text-[#6B7280] dark:text-[#9CA3AF]"
                          )}
                        >
                          <CalendarDays className="mr-2 h-4 w-4" />
                          {formData.startDate ? format(formData.startDate, "PPP") : "Pick a date"}
                        </Button>
                      </PopoverTrigger>
                      <PopoverContent className="w-auto p-0" align="start">
                        <Calendar
                          mode="single"
                          selected={formData.startDate}
                          onSelect={(date) => setFormData({ ...formData, startDate: date })}
                          initialFocus
                          disabled={(date) => date < new Date()}
                        />
                      </PopoverContent>
                    </Popover>
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">
                      End Date <span className="text-red-500">*</span>
                    </label>
                    <Popover>
                      <PopoverTrigger asChild>
                        <Button
                          variant="outline"
                          className={cn(
                            "w-full justify-start text-left font-normal border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827]",
                            !formData.endDate && "text-[#6B7280] dark:text-[#9CA3AF]"
                          )}
                        >
                          <CalendarDays className="mr-2 h-4 w-4" />
                          {formData.endDate ? format(formData.endDate, "PPP") : "Pick a date"}
                        </Button>
                      </PopoverTrigger>
                      <PopoverContent className="w-auto p-0" align="start">
                        <Calendar
                          mode="single"
                          selected={formData.endDate}
                          onSelect={(date) => setFormData({ ...formData, endDate: date })}
                          initialFocus
                          disabled={(date) => 
                            date < new Date() || 
                            (formData.startDate ? date < formData.startDate : false)
                          }
                        />
                      </PopoverContent>
                    </Popover>
                  </div>
                </div>

                <div className="space-y-2">
                  <label className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">
                    Reason <span className="text-red-500">*</span>
                  </label>
                  <Textarea
                    placeholder="Enter the reason for leave"
                    className="min-h-[100px] border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827] focus-visible:ring-[#3B82F6] focus-visible:border-[#3B82F6]"
                    value={formData.reason}
                    onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
                  />
                </div>
              </div>

              <div className="flex items-center gap-2 text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                <AlertCircle className="h-4 w-4" />
                <span>Fields marked with * are required</span>
              </div>

              <Button
                type="submit"
                className="w-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-md hover:shadow-lg"
                disabled={loading}
              >
                {loading ? "Submitting..." : "Submit Request"}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
