"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { authService } from "@/lib/auth"
import { CalendarDays, Clock, AlertCircle } from "lucide-react"
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

interface OvertimeRequestForm {
  date: Date | undefined
  startTime: string
  endTime: string
  reason: string
}

export default function OvertimeRequestPage() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)
  const [formData, setFormData] = useState<OvertimeRequestForm>({
    date: undefined,
    startTime: "",
    endTime: "",
    reason: "",
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!formData.date || !formData.startTime || !formData.endTime || !formData.reason) {
      toast.error("Please fill in all required fields")
      return
    }

    try {
      setLoading(true)
      const token = authService.getToken()
      if (!token) {
        router.push("/")
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/overtime/request`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          date: formData.date.toISOString().split('T')[0],
          startTime: formData.startTime,
          endTime: formData.endTime,
          reason: formData.reason,
        }),
      })

      if (!response.ok) {
        throw new Error("Failed to submit overtime request")
      }

      toast.success("Overtime request submitted successfully")
      setFormData({
        date: undefined,
        startTime: "",
        endTime: "",
        reason: "",
      })
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to submit overtime request")
    } finally {
      setLoading(false)
    }
  }

  const generateTimeOptions = () => {
    const times = []
    for (let hour = 0; hour < 24; hour++) {
      for (let minute = 0; minute < 60; minute += 30) {
        const time = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
        times.push(time)
      }
    }
    return times
  }

  const timeOptions = generateTimeOptions()

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
      <Toaster position="top-right" richColors className="mt-24" style={{ top: "6rem", right: "1rem" }} />
      <div className="w-full max-w-2xl mx-auto space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white flex items-center gap-2">
              <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-1 shadow-md">
                <Clock className="h-5 w-5 text-white" />
              </div>
              Request Overtime
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
              Submit a new overtime request
            </p>
          </div>
        </div>

        <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-xl overflow-hidden bg-white dark:bg-[#1F2937]">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
          <CardHeader className="bg-[#F9FAFB] dark:bg-[#111827] border-b border-[#E5E7EB] dark:border-[#374151]">
            <CardTitle className="text-xl text-[#1F2937] dark:text-white flex items-center gap-2">
              <CalendarDays className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
              Overtime Request Form
            </CardTitle>
            <CardDescription className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
              Fill in the details for your overtime request
            </CardDescription>
          </CardHeader>
          <CardContent className="p-6">
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="space-y-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">
                    Date <span className="text-red-500">*</span>
                  </label>
                  <Popover>
                    <PopoverTrigger asChild>
                      <Button
                        variant="outline"
                        className={cn(
                          "w-full justify-start text-left font-normal border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827]",
                          !formData.date && "text-[#6B7280] dark:text-[#9CA3AF]"
                        )}
                      >
                        <CalendarDays className="mr-2 h-4 w-4" />
                        {formData.date ? format(formData.date, "PPP") : "Pick a date"}
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-auto p-0" align="start">
                      <Calendar
                        mode="single"
                        selected={formData.date}
                        onSelect={(date) => setFormData({ ...formData, date })}
                        initialFocus
                        disabled={(date) => date < new Date()}
                      />
                    </PopoverContent>
                  </Popover>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">
                      Start Time <span className="text-red-500">*</span>
                    </label>
                    <Select
                      value={formData.startTime}
                      onValueChange={(value) => setFormData({ ...formData, startTime: value })}
                    >
                      <SelectTrigger className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827]">
                        <SelectValue placeholder="Select start time" />
                      </SelectTrigger>
                      <SelectContent className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#1F2937]">
                        {timeOptions.map((time) => (
                          <SelectItem key={time} value={time}>
                            {format(new Date(`2000-01-01T${time}`), "hh:mm a")}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">
                      End Time <span className="text-red-500">*</span>
                    </label>
                    <Select
                      value={formData.endTime}
                      onValueChange={(value) => setFormData({ ...formData, endTime: value })}
                    >
                      <SelectTrigger className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827]">
                        <SelectValue placeholder="Select end time" />
                      </SelectTrigger>
                      <SelectContent className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#1F2937]">
                        {timeOptions.map((time) => (
                          <SelectItem key={time} value={time}>
                            {format(new Date(`2000-01-01T${time}`), "hh:mm a")}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="space-y-2">
                  <label className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">
                    Reason <span className="text-red-500">*</span>
                  </label>
                  <Textarea
                    placeholder="Enter the reason for overtime"
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
