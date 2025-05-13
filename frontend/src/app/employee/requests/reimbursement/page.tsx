"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { authService } from "@/lib/auth"
import { CalendarDays, Receipt, AlertCircle, Upload } from "lucide-react"
import { Toaster, toast } from "sonner"
import { cn } from "@/lib/utils"
import { format } from "date-fns"
import { Calendar } from "@/components/ui/calendar"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import Image from "next/image"

interface ReimbursementRequestForm {
  expenseDate: Date | undefined
  amountRequested: string
  reason: string
  receiptImage1: File | null
  receiptImage2: File | null
}

export default function ReimbursementRequestPage() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)
  const [formData, setFormData] = useState<ReimbursementRequestForm>({
    expenseDate: undefined,
    amountRequested: "",
    reason: "",
    receiptImage1: null,
    receiptImage2: null,
  })
  const [previewImage1, setPreviewImage1] = useState<string | null>(null)
  const [previewImage2, setPreviewImage2] = useState<string | null>(null)

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>, imageNumber: 1 | 2) => {
    const file = e.target.files?.[0]
    if (file) {
      if (file.size > 5 * 1024 * 1024) { // 5MB limit
        toast.error("Image size should be less than 5MB")
        return
      }
      if (!file.type.startsWith("image/")) {
        toast.error("Please upload an image file")
        return
      }

      if (imageNumber === 1) {
        setFormData({ ...formData, receiptImage1: file })
        setPreviewImage1(URL.createObjectURL(file))
      } else {
        setFormData({ ...formData, receiptImage2: file })
        setPreviewImage2(URL.createObjectURL(file))
      }
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!formData.expenseDate || !formData.amountRequested || !formData.reason || !formData.receiptImage1) {
      toast.error("Please fill in all required fields and upload at least one receipt image")
      return
    }

    try {
      setLoading(true)
      const token = authService.getToken()
      if (!token) {
        router.push("/")
        return
      }

      const formDataToSend = new FormData()
      formDataToSend.append("expenseDate", formData.expenseDate.toISOString().split('T')[0])
      formDataToSend.append("amountRequested", formData.amountRequested)
      formDataToSend.append("reason", formData.reason)
      if (formData.receiptImage1) {
        formDataToSend.append("receiptImage1", formData.receiptImage1)
      }
      if (formData.receiptImage2) {
        formDataToSend.append("receiptImage2", formData.receiptImage2)
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/employee/reimbursement-requests`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formDataToSend,
      })

      if (!response.ok) {
        throw new Error("Failed to submit reimbursement request")
      }

      toast.success("Reimbursement request submitted successfully")
      setFormData({
        expenseDate: undefined,
        amountRequested: "",
        reason: "",
        receiptImage1: null,
        receiptImage2: null,
      })
      setPreviewImage1(null)
      setPreviewImage2(null)
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to submit reimbursement request")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
      <Toaster position="top-right" richColors className="mt-24" style={{ top: "6rem", right: "1rem" }} />
      <div className="w-full max-w-2xl mx-auto space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white flex items-center gap-2">
              <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-1 shadow-md">
                <Receipt className="h-5 w-5 text-white" />
              </div>
              Request Reimbursement
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
              Submit a new reimbursement request
            </p>
          </div>
        </div>

        <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-xl overflow-hidden bg-white dark:bg-[#1F2937]">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
          <CardHeader className="bg-[#F9FAFB] dark:bg-[#111827] border-b border-[#E5E7EB] dark:border-[#374151]">
            <CardTitle className="text-xl text-[#1F2937] dark:text-white flex items-center gap-2">
              <CalendarDays className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
              Reimbursement Request Form
            </CardTitle>
            <CardDescription className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
              Fill in the details for your reimbursement request
            </CardDescription>
          </CardHeader>
          <CardContent className="p-6">
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="space-y-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">
                    Expense Date <span className="text-red-500">*</span>
                  </label>
                  <Popover>
                    <PopoverTrigger asChild>
                      <Button
                        variant="outline"
                        className={cn(
                          "w-full justify-start text-left font-normal border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827]",
                          !formData.expenseDate && "text-[#6B7280] dark:text-[#9CA3AF]"
                        )}
                      >
                        <CalendarDays className="mr-2 h-4 w-4" />
                        {formData.expenseDate ? format(formData.expenseDate, "PPP") : "Pick a date"}
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-auto p-0" align="start">
                      <Calendar
                        mode="single"
                        selected={formData.expenseDate}
                        onSelect={(date) => setFormData({ ...formData, expenseDate: date })}
                        initialFocus
                        disabled={(date) => date > new Date()}
                      />
                    </PopoverContent>
                  </Popover>
                </div>

                <div className="space-y-2">
                  <label className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">
                    Amount Requested <span className="text-red-500">*</span>
                  </label>
                  <Input
                    type="number"
                    step="0.01"
                    min="0"
                    placeholder="Enter amount"
                    className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827]"
                    value={formData.amountRequested}
                    onChange={(e) => setFormData({ ...formData, amountRequested: e.target.value })}
                  />
                </div>

                <div className="space-y-2">
                  <label className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">
                    Reason <span className="text-red-500">*</span>
                  </label>
                  <Textarea
                    placeholder="Enter the reason for reimbursement"
                    className="min-h-[100px] border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827] focus-visible:ring-[#3B82F6] focus-visible:border-[#3B82F6]"
                    value={formData.reason}
                    onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
                  />
                </div>

                <div className="space-y-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">
                      Receipt Image 1 <span className="text-red-500">*</span>
                    </label>
                    <div className="flex items-center gap-4">
                      <Input
                        type="file"
                        accept="image/*"
                        className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827]"
                        onChange={(e) => handleImageChange(e, 1)}
                      />
                      {previewImage1 && (
                        <div className="relative w-20 h-20">
                          <Image
                            src={previewImage1}
                            alt="Receipt preview 1"
                            fill
                            className="object-cover rounded-md"
                          />
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">
                      Receipt Image 2 (Optional)
                    </label>
                    <div className="flex items-center gap-4">
                      <Input
                        type="file"
                        accept="image/*"
                        className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827]"
                        onChange={(e) => handleImageChange(e, 2)}
                      />
                      {previewImage2 && (
                        <div className="relative w-20 h-20">
                          <Image
                            src={previewImage2}
                            alt="Receipt preview 2"
                            fill
                            className="object-cover rounded-md"
                          />
                        </div>
                      )}
                    </div>
                  </div>
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
