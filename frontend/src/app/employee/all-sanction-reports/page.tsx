"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { MessageSquare, AlertCircle, CheckCircle2, Clock, Filter } from "lucide-react"
import { format } from "date-fns"
import { formatInTimeZone } from 'date-fns-tz'
import { authService } from "@/lib/auth"
import { Skeleton } from "@/components/ui/skeleton"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { toast } from "sonner"
import { Toaster } from "sonner"

interface FeedbackComplaint {
  feedbackId: string
  employeeId: string
  employeeName: string
  category: string
  subject: string
  description: string
  submittedAt: string
  status: string
  resolutionNotes?: string
  resolvedAt?: string
  resolverName?: string
}

type FilterStatus = "all" | "Open" | "In Review" | "Resolved" | "Closed"

export default function FeedbackPage() {
  const [feedbacks, setFeedbacks] = useState<FeedbackComplaint[]>([])
  const [filteredFeedbacks, setFilteredFeedbacks] = useState<FeedbackComplaint[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedFeedback, setSelectedFeedback] = useState<FeedbackComplaint | null>(null)
  const [isDetailsDialogOpen, setIsDetailsDialogOpen] = useState(false)
  const [activeFilter, setActiveFilter] = useState<FilterStatus>("all")

  const fetchFeedbacks = async () => {
    try {
      setLoading(true)
      const token = authService.getToken()

      if (!token) {
        throw new Error("No authentication token found")
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/employee/feedback`, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      if (!response.ok) {
        throw new Error("Failed to fetch feedback")
      }

      const data = await response.json()
      setFeedbacks(data)
      setFilteredFeedbacks(data)
    } catch (err) {
      console.error("Error fetching data:", err)
      setError(err instanceof Error ? err.message : "Failed to fetch data")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchFeedbacks()
  }, [])

  const handleFilterChange = async (filter: FilterStatus) => {
    setActiveFilter(filter)
    await fetchFeedbacks() // Refresh data when filter changes
  }

  useEffect(() => {
    if (activeFilter === "all") {
      setFilteredFeedbacks(feedbacks)
    } else {
      setFilteredFeedbacks(
        feedbacks.filter(feedback => 
          feedback.status === activeFilter
        )
      )
    }
  }, [activeFilter, feedbacks])

  const handleViewDetails = (feedback: FeedbackComplaint) => {
    setSelectedFeedback(feedback)
    setIsDetailsDialogOpen(true)
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "Open":
        return "bg-blue-500 text-white"
      case "In Review":
        return "bg-yellow-500 text-white"
      case "Resolved":
        return "bg-green-500 text-white"
      case "Closed":
        return "bg-gray-500 text-white"
      default:
        return "bg-gray-500 text-white"
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "Open":
        return <AlertCircle className="h-4 w-4" />
      case "In Review":
        return <Clock className="h-4 w-4" />
      case "Resolved":
        return <CheckCircle2 className="h-4 w-4" />
      case "Closed":
        return <CheckCircle2 className="h-4 w-4" />
      default:
        return <AlertCircle className="h-4 w-4" />
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
                  <MessageSquare className="h-5 w-5 text-white" />
                </div>
                My Feedback & Complaints
              </h1>
              <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">View your submitted feedback and complaints</p>
            </div>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {[1, 2, 3].map((i) => (
              <Skeleton key={`skeleton-${i}`} className="h-[200px] w-full rounded-lg" />
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
          <div className="bg-red-50 dark:bg-red-900/20 p-4 rounded-lg">
            <p className="text-red-600 dark:text-red-400">{error}</p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
      <Toaster />
      <div className="w-full max-w-6xl mx-auto space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white flex items-center gap-2">
              <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-1 shadow-md">
                <MessageSquare className="h-5 w-5 text-white" />
              </div>
              My Feedback & Complaints
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">View your submitted feedback and complaints</p>
          </div>
          <div className="flex gap-2">
            <Button
              variant={activeFilter === "all" ? "default" : "outline"}
              onClick={() => handleFilterChange("all")}
              className="flex items-center gap-2"
            >
              <Filter className="h-4 w-4" />
              All
            </Button>
            <Button
              variant={activeFilter === "Open" ? "default" : "outline"}
              onClick={() => handleFilterChange("Open")}
              className="flex items-center gap-2"
            >
              <AlertCircle className="h-4 w-4" />
              Open
            </Button>
            <Button
              variant={activeFilter === "In Review" ? "default" : "outline"}
              onClick={() => handleFilterChange("In Review")}
              className="flex items-center gap-2"
            >
              <Clock className="h-4 w-4" />
              In Review
            </Button>
            <Button
              variant={activeFilter === "Resolved" ? "default" : "outline"}
              onClick={() => handleFilterChange("Resolved")}
              className="flex items-center gap-2"
            >
              <CheckCircle2 className="h-4 w-4" />
              Resolved
            </Button>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredFeedbacks.map((feedback) => (
            <Card key={feedback.feedbackId} className="overflow-hidden hover:shadow-lg transition-shadow">
              <CardHeader className="pb-2">
                <div className="flex justify-between items-start">
                  <CardTitle className="text-lg font-semibold">
                    {feedback.category}
                  </CardTitle>
                  <Badge className={cn("flex items-center gap-1", getStatusColor(feedback.status))}>
                    {getStatusIcon(feedback.status)}
                    {feedback.status}
                  </Badge>
                </div>
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  {format(new Date(feedback.submittedAt), "MMM d, yyyy")}
                </p>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-gray-600 dark:text-gray-300 line-clamp-2 mb-4">
                  {feedback.description}
                </p>
                <Button
                  variant="outline"
                  className="w-full"
                  onClick={() => handleViewDetails(feedback)}
                >
                  View Details
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>

        <Dialog open={isDetailsDialogOpen} onOpenChange={setIsDetailsDialogOpen}>
          <DialogContent className="sm:max-w-[500px]">
            <DialogHeader>
              <DialogTitle>{selectedFeedback?.category}</DialogTitle>
              <DialogDescription>
                Submitted on {selectedFeedback && format(new Date(selectedFeedback.submittedAt), "MMMM d, yyyy")}
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div>
                <h3 className="font-medium mb-1">Subject</h3>
                <p className="text-sm text-gray-600 dark:text-gray-300">{selectedFeedback?.subject}</p>
              </div>
              <div>
                <h3 className="font-medium mb-1">Description</h3>
                <p className="text-sm text-gray-600 dark:text-gray-300">{selectedFeedback?.description}</p>
              </div>
              <div>
                <h3 className="font-medium mb-1">Submitted On</h3>
                <p className="text-sm text-gray-600 dark:text-gray-300">
                  {selectedFeedback && formatInTimeZone(new Date(selectedFeedback.submittedAt), 'Asia/Manila', 'MMMM d, yyyy hh:mm a')}
                </p>
              </div>
              {selectedFeedback?.status === "Resolved" && (
                <>
                  <div>
                    <h3 className="font-medium mb-1">Resolved By</h3>
                    <p className="text-sm text-gray-600 dark:text-gray-300">
                    HR  {selectedFeedback.resolverName || "Not specified"}
                    </p>
                  </div>
                  <div>
                    <h3 className="font-medium mb-1">Resolved On</h3>
                    <p className="text-sm text-gray-600 dark:text-gray-300">
                      {selectedFeedback.resolvedAt && formatInTimeZone(
                        new Date(selectedFeedback.resolvedAt),
                        'Asia/Manila',
                        'MMMM d, yyyy hh:mm a'
                      )}
                    </p>
                  </div>
                  <div>
                    <h3 className="font-medium mb-1">Resolution Notes</h3>
                    <div className="bg-gray-50 dark:bg-gray-900/50 rounded-lg p-4 border border-gray-200 dark:border-gray-800">
                      <p className="text-sm text-gray-600 dark:text-gray-300 break-words">
                        {selectedFeedback.resolutionNotes || "No resolution notes provided"}
                      </p>
                    </div>
                  </div>
                </>
              )}
              <div className="flex items-center gap-2">
                <Badge className={cn("flex items-center gap-1", getStatusColor(selectedFeedback?.status || ""))}>
                  {getStatusIcon(selectedFeedback?.status || "")}
                  {selectedFeedback?.status}
                </Badge>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </div>
    </div>
  )
} 