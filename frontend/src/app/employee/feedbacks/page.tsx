"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { authService } from "@/lib/auth"
import { MessageSquare, AlertCircle } from "lucide-react"
import { Toaster, toast } from "sonner"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

interface FeedbackForm {
  category: string
  subject: string
  description: string
}

export default function FeedbackPage() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)
  const [formData, setFormData] = useState<FeedbackForm>({
    category: "",
    subject: "",
    description: "",
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!formData.category || !formData.subject || !formData.description) {
      toast.error("Please fill in all required fields")
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

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/employee/feedback`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(formData),
      })

      if (!response.ok) {
        const errorData = await response.json()
        
        // Handle specific backend validation errors
        switch (errorData.message) {
          case "Category is required":
            toast.error("Please select a category")
            break
          case "Subject is required":
            toast.error("Please enter a subject")
            break
          case "Description is required":
            toast.error("Please enter a description")
            break
          default:
            if (response.status === 401) {
              toast.error("Session expired. Please login again.")
              router.push("/login")
            } else if (response.status === 403) {
              toast.error("You don't have permission to submit feedback")
            } else {
              toast.error(errorData.message || "Failed to submit feedback")
            }
        }
        return
      }

      toast.success("Feedback submitted successfully")
      setFormData({
        category: "",
        subject: "",
        description: "",
      })
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to submit feedback")
    } finally {
      setLoading(false)
    }
  }

  const categories = [
    { value: "Feedback", label: "Feedback" },
    { value: "Complaint", label: "Complaint" },
    { value: "Concern", label: "Concern" }
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
      <Toaster position="top-right" richColors className="mt-24" style={{ top: "6rem", right: "1rem" }} />
      <div className="w-full max-w-2xl mx-auto space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white flex items-center gap-2">
              <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-1 shadow-md">
                <MessageSquare className="h-5 w-5 text-white" />
              </div>
              Submit
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
              Share your feedback, complaints, or concerns
            </p>
          </div>
        </div>

        <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-xl overflow-hidden bg-white dark:bg-[#1F2937]">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
          <CardHeader className="bg-[#F9FAFB] dark:bg-[#111827] border-b border-[#E5E7EB] dark:border-[#374151]">
            <CardTitle className="text-xl text-[#1F2937] dark:text-white flex items-center gap-2">
              <MessageSquare className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
              Feedback Form
            </CardTitle>
            <CardDescription className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
              Fill in the details for your feedback
            </CardDescription>
          </CardHeader>
          <CardContent className="p-6">
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="space-y-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">
                    Category <span className="text-red-500">*</span>
                  </label>
                  <Select
                    value={formData.category}
                    onValueChange={(value) => setFormData({ ...formData, category: value })}
                  >
                    <SelectTrigger className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827]">
                      <SelectValue placeholder="Select category" />
                    </SelectTrigger>
                    <SelectContent className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#1F2937]">
                      {categories.map((category) => (
                        <SelectItem key={category.value} value={category.value}>
                          {category.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <label className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">
                    Subject <span className="text-red-500">*</span>
                  </label>
                  <Input
                    placeholder="Enter a brief subject"
                    className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827] focus-visible:ring-[#3B82F6] focus-visible:border-[#3B82F6]"
                    value={formData.subject}
                    onChange={(e) => setFormData({ ...formData, subject: e.target.value })}
                  />
                </div>

                <div className="space-y-2">
                  <label className="text-sm font-medium text-[#4B5563] dark:text-[#D1D5DB]">
                    Description <span className="text-red-500">*</span>
                  </label>
                  <Textarea
                    placeholder="Provide detailed feedback, complaint, or concern"
                    className="min-h-[150px] border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827] focus-visible:ring-[#3B82F6] focus-visible:border-[#3B82F6]"
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
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
                {loading ? "Submitting..." : "Submit Feedback"}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}