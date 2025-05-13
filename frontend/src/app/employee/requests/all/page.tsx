"use client"

import type React from "react"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { format } from "date-fns"
import { authService } from "@/lib/auth"
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination"
import {
  Clock,
  Calendar,
  DollarSign,
  CheckCircle,
  XCircle,
  Clock3,
  AlertCircle,
  FileText,
  Shield,
  Filter,
  Search,
  Briefcase,
  Coffee,
  Receipt,
  Zap,
  RotateCcw,
  ZoomIn,
  ZoomOut,
  X,
  RefreshCw,
} from "lucide-react"
import { cn } from "@/lib/utils"
import { Skeleton } from "@/components/ui/skeleton"
import { Button } from "@/components/ui/button"
import { toast } from "sonner"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"

interface Request {
  id: string
  type: "OVERTIME" | "LEAVE" | "REIMBURSEMENT"
  status: string
  date: string
  description?: string
  startDate?: string
  endDate?: string
  amount?: number
  leaveType?: string
  // Overtime specific fields
  startTime?: string
  endTime?: string
  totalHours?: number
  reason?: string
  // Reimbursement specific fields
  receiptImage1?: string
  receiptImage2?: string
}

export default function AllRequestsPage() {
  const [requests, setRequests] = useState<Request[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [currentPage, setCurrentPage] = useState(1)
  const [activeTab, setActiveTab] = useState("all")
  const [statusFilter, setStatusFilter] = useState("all")
  const [searchTerm, setSearchTerm] = useState("")
  const [selectedImages, setSelectedImages] = useState<{ image1?: string; image2?: string } | null>(null)
  const [selectedImage, setSelectedImage] = useState<string | null>(null)
  const [zoom, setZoom] = useState(1)
  const [rotation, setRotation] = useState(0)
  const [position, setPosition] = useState({ x: 0, y: 0 })
  const [isDragging, setIsDragging] = useState(false)
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 })
  const itemsPerPage = 8 // Show 8 cards per page (2 rows of 4 cards)
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false)
  const [requestToCancel, setRequestToCancel] = useState<{ id: string; type: string } | null>(null)

  useEffect(() => {
    const fetchAllRequests = async () => {
      try {
        setLoading(true)
        setError(null)

        const token = authService.getToken()

        if (!token) {
          throw new Error("No authentication token found")
        }

        const headers = {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        }

        const [overtimeRes, leaveRes, reimbursementRes] = await Promise.all([
          fetch("/api/overtime/my-requests", { headers }),
          fetch("/api/employee/leave-requests", { headers }),
          fetch("/api/employee/reimbursement-requests", { headers }),
        ])

        if (!overtimeRes.ok || !leaveRes.ok || !reimbursementRes.ok) {
          throw new Error("Failed to fetch requests")
        }

        const overtimeData = await overtimeRes.json()
        const leaveData = await leaveRes.json()
        const reimbursementData = await reimbursementRes.json()

        // Transform the data to a common format with validation
        const transformedRequests: Request[] = [
          ...overtimeData.map((req: any, index: number) => ({
            id: req.otRequestId || `overtime-${index}`,
            type: "OVERTIME",
            status: req.status || "PENDING",
            date: req.date || new Date().toISOString(),
            startTime: req.startTime,
            endTime: req.endTime,
            totalHours: req.totalHours,
            reason: req.reason,
            description: req.reason,
          })),
          ...leaveData.map((req: any, index: number) => ({
            id: req.leaveId || `leave-${index}`,
            type: "LEAVE",
            status: req.status || "PENDING",
            startDate: req.startDate,
            endDate: req.endDate,
            leaveType: req.leaveType || "Unspecified",
            description: req.reason,
          })),
          ...reimbursementData.map((req: any, index: number) => ({
            id: req.id || `reimbursement-${index}`,
            type: "REIMBURSEMENT",
            status: req.status || "PENDING",
            date: req.requestDate || new Date().toISOString(),
            amount: req.amountRequested,
            description: req.reason,
            receiptImage1: req.receiptImage1,
            receiptImage2: req.receiptImage2,
          })),
        ]

        // Sort by date (most recent first)
        transformedRequests.sort(
          (a, b) => new Date(b.date || b.startDate || "").getTime() - new Date(a.date || a.startDate || "").getTime(),
        )

        setRequests(transformedRequests)
      } catch (err) {
        console.error("Error fetching requests:", err)
        setError(err instanceof Error ? err.message : "Failed to fetch requests")
      } finally {
        setLoading(false)
      }
    }

    fetchAllRequests()
  }, [])

  // Reset to first page when changing tabs
  useEffect(() => {
    setCurrentPage(1)
  }, [activeTab])

  const getStatusColor = (status: string) => {
    switch (status.toUpperCase()) {
      case "APPROVED":
        return "bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-400"
      case "PENDING":
        return "bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400"
      case "REJECTED":
        return "bg-rose-100 text-rose-800 dark:bg-rose-900/30 dark:text-rose-400"
      case "CANCELED":
        return "bg-slate-100 text-slate-800 dark:bg-slate-800/50 dark:text-slate-400"
      default:
        return "bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-400"
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status.toUpperCase()) {
      case "APPROVED":
        return <CheckCircle className="h-3.5 w-3.5 mr-1" />
      case "PENDING":
        return <Clock3 className="h-3.5 w-3.5 mr-1" />
      case "REJECTED":
        return <XCircle className="h-3.5 w-3.5 mr-1" />
      case "CANCELED":
        return <AlertCircle className="h-3.5 w-3.5 mr-1" />
      default:
        return <AlertCircle className="h-3.5 w-3.5 mr-1" />
    }
  }

  const getTypeIcon = (type: string) => {
    switch (type) {
      case "OVERTIME":
        return <Clock className="h-5 w-5 text-teal-500" />
      case "LEAVE":
        return <Coffee className="h-5 w-5 text-violet-500" />
      case "REIMBURSEMENT":
        return <Receipt className="h-5 w-5 text-amber-500" />
      default:
        return <FileText className="h-5 w-5 text-slate-500" />
    }
  }

  const getTypeColor = (type: string) => {
    switch (type) {
      case "OVERTIME":
        return "from-teal-400 to-cyan-500"
      case "LEAVE":
        return "from-violet-400 to-purple-500"
      case "REIMBURSEMENT":
        return "from-amber-400 to-orange-500"
      default:
        return "from-slate-400 to-slate-500"
    }
  }

  const getTypeTextColor = (type: string) => {
    switch (type) {
      case "OVERTIME":
        return "text-teal-600 dark:text-teal-400"
      case "LEAVE":
        return "text-violet-600 dark:text-violet-400"
      case "REIMBURSEMENT":
        return "text-amber-600 dark:text-amber-400"
      default:
        return "text-slate-600 dark:text-slate-400"
    }
  }

  const getTypeBgColor = (type: string) => {
    switch (type) {
      case "OVERTIME":
        return "bg-teal-50 dark:bg-teal-900/30"
      case "LEAVE":
        return "bg-violet-50 dark:bg-violet-900/30"
      case "REIMBURSEMENT":
        return "bg-amber-50 dark:bg-amber-900/30"
      default:
        return "bg-slate-50 dark:bg-slate-800/30"
    }
  }

  // Filter requests based on active tab, status filter, and search term
  const filteredRequests = requests.filter((request) => {
    const typeMatch = activeTab === "all" || request.type.toLowerCase() === activeTab.toLowerCase()
    const statusMatch = statusFilter === "all" || request.status.toLowerCase() === statusFilter.toLowerCase()
    const searchMatch =
      searchTerm === "" ||
      (request.description && request.description.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (request.leaveType && request.leaveType.toLowerCase().includes(searchTerm.toLowerCase()))

    return typeMatch && statusMatch && searchMatch
  })

  // Calculate total pages
  const totalPages = Math.ceil(filteredRequests.length / itemsPerPage)

  // Get current page items
  const getCurrentPageItems = () => {
    const startIndex = (currentPage - 1) * itemsPerPage
    const endIndex = startIndex + itemsPerPage
    return filteredRequests.slice(startIndex, endIndex)
  }

  // Add this function to handle viewing receipts
  const handleViewReceipts = (image1?: string, image2?: string) => {
    setSelectedImages({ image1, image2 })
  }

  // Add this function to close the modal
  const handleCloseModal = () => {
    setSelectedImages(null)
  }

  // Add this function to handle viewing individual images
  const handleViewImage = (image: string) => {
    setSelectedImage(image)
  }

  // Add these functions for image manipulation
  const handleZoomIn = () => {
    setZoom((prev) => Math.min(prev + 0.25, 3))
  }

  const handleZoomOut = () => {
    setZoom((prev) => Math.max(prev - 0.25, 0.5))
  }

  const handleRotate = () => {
    setRotation((prev) => (prev + 90) % 360)
  }

  // Add these functions for panning
  const handleMouseDown = (e: React.MouseEvent) => {
    if (zoom > 1) {
      setIsDragging(true)
      setDragStart({ x: e.clientX - position.x, y: e.clientY - position.y })
    }
  }

  const handleMouseMove = (e: React.MouseEvent) => {
    if (isDragging && zoom > 1) {
      const newX = e.clientX - dragStart.x
      const newY = e.clientY - dragStart.y
      setPosition({ x: newX, y: newY })
    }
  }

  const handleMouseUp = () => {
    setIsDragging(false)
  }

  const handleTouchStart = (e: React.TouchEvent) => {
    if (zoom > 1 && e.touches.length === 1) {
      setIsDragging(true)
      setDragStart({
        x: e.touches[0].clientX - position.x,
        y: e.touches[0].clientY - position.y,
      })
    }
  }

  const handleTouchMove = (e: React.TouchEvent) => {
    if (isDragging && zoom > 1 && e.touches.length === 1) {
      const newX = e.touches[0].clientX - dragStart.x
      const newY = e.touches[0].clientY - dragStart.y
      setPosition({ x: newX, y: newY })
    }
  }

  const handleTouchEnd = () => {
    setIsDragging(false)
  }

  // Update handleReset to also reset position
  const handleReset = () => {
    setZoom(1)
    setRotation(0)
    setPosition({ x: 0, y: 0 })
  }

  // Update handleCloseImageModal to reset position
  const handleCloseImageModal = () => {
    setSelectedImage(null)
    setZoom(1)
    setRotation(0)
    setPosition({ x: 0, y: 0 })
  }

  // Get request counts by type and status
  const getRequestCounts = () => {
    const counts = {
      total: requests.length,
      pending: requests.filter((r) => r.status.toUpperCase() === "PENDING").length,
      approved: requests.filter((r) => r.status.toUpperCase() === "APPROVED").length,
      rejected: requests.filter((r) => r.status.toUpperCase() === "REJECTED").length,
      overtime: requests.filter((r) => r.type === "OVERTIME").length,
      leave: requests.filter((r) => r.type === "LEAVE").length,
      reimbursement: requests.filter((r) => r.type === "REIMBURSEMENT").length,
    }
    return counts
  }

  const requestCounts = getRequestCounts()

  // Update the ImageModal component
  const ImageModal = () => {
    if (!selectedImage) return null

    return (
      <div
        className="fixed inset-0 bg-black/90 flex items-center justify-center z-[60] p-4"
        onClick={handleCloseImageModal}
      >
        <div className="relative max-w-5xl w-full max-h-[90vh]">
          <div className="absolute -top-16 right-0 flex items-center gap-2">
            <Button
              variant="ghost"
              size="icon"
              onClick={(e) => {
                e.stopPropagation()
                handleZoomOut()
              }}
              className="text-white hover:text-slate-300 transition-colors bg-black/50 hover:bg-black/70 rounded-full"
              title="Zoom Out"
            >
              <ZoomOut className="h-5 w-5" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              onClick={(e) => {
                e.stopPropagation()
                handleZoomIn()
              }}
              className="text-white hover:text-slate-300 transition-colors bg-black/50 hover:bg-black/70 rounded-full"
              title="Zoom In"
            >
              <ZoomIn className="h-5 w-5" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              onClick={(e) => {
                e.stopPropagation()
                handleRotate()
              }}
              className="text-white hover:text-slate-300 transition-colors bg-black/50 hover:bg-black/70 rounded-full"
              title="Rotate"
            >
              <RotateCcw className="h-5 w-5" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              onClick={(e) => {
                e.stopPropagation()
                handleReset()
              }}
              className="text-white hover:text-slate-300 transition-colors bg-black/50 hover:bg-black/70 rounded-full"
              title="Reset"
            >
              <Zap className="h-5 w-5" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              onClick={handleCloseImageModal}
              className="text-white hover:text-slate-300 transition-colors bg-black/50 hover:bg-black/70 rounded-full"
              title="Close"
            >
              <X className="h-5 w-5" />
            </Button>
          </div>
          <div className="relative overflow-auto max-h-[80vh]" onClick={(e) => e.stopPropagation()}>
            <div
              className="relative"
              onMouseDown={handleMouseDown}
              onMouseMove={handleMouseMove}
              onMouseUp={handleMouseUp}
              onMouseLeave={handleMouseUp}
              onTouchStart={handleTouchStart}
              onTouchMove={handleTouchMove}
              onTouchEnd={handleTouchEnd}
              style={{
                cursor: zoom > 1 ? (isDragging ? "grabbing" : "grab") : "default",
                touchAction: "none",
              }}
            >
              <img
                src={`data:image/jpeg;base64,${selectedImage}`}
                alt="Receipt"
                className="w-full h-auto rounded-lg shadow-2xl transition-all duration-200"
                style={{
                  transform: `scale(${zoom}) rotate(${rotation}deg)`,
                  transformOrigin: "center",
                  translate: `${position.x}px ${position.y}px`,
                }}
                draggable={false}
              />
            </div>
          </div>
          <div className="absolute bottom-4 left-1/2 -translate-x-1/2 text-white text-sm bg-black/50 px-3 py-1 rounded-full">
            Zoom: {Math.round(zoom * 100)}% | Rotation: {rotation}°
          </div>
        </div>
      </div>
    )
  }

  const ReceiptModal = () => {
    if (!selectedImages) return null

    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="bg-white dark:bg-slate-800 rounded-xl p-6 max-w-4xl w-full max-h-[90vh] overflow-y-auto shadow-2xl">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-xl font-semibold text-slate-800 dark:text-white">Receipt Images</h3>
            <Button
              variant="ghost"
              size="icon"
              onClick={handleCloseModal}
              className="text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-white rounded-full"
            >
              <X className="h-5 w-5" />
            </Button>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {selectedImages.image1 && (
              <div className="space-y-3">
                <h4 className="text-sm font-medium text-slate-600 dark:text-slate-300">Receipt 1</h4>
                <div
                  className="relative group cursor-pointer overflow-hidden rounded-xl"
                  onClick={() => handleViewImage(selectedImages.image1!)}
                >
                  <img
                    src={`data:image/jpeg;base64,${selectedImages.image1}`}
                    alt="Receipt 1"
                    className="w-full h-auto rounded-xl border border-slate-200 dark:border-slate-700 transition-transform duration-300 group-hover:scale-[1.03]"
                  />
                  <div className="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition-colors rounded-xl flex items-center justify-center">
                    <div className="opacity-0 group-hover:opacity-100 transition-opacity transform translate-y-4 group-hover:translate-y-0 duration-300">
                      <div className="bg-white/90 dark:bg-slate-800/90 rounded-full p-3">
                        <ZoomIn className="w-6 h-6 text-teal-600 dark:text-teal-400" />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}
            {selectedImages.image2 && (
              <div className="space-y-3">
                <h4 className="text-sm font-medium text-slate-600 dark:text-slate-300">Receipt 2</h4>
                <div
                  className="relative group cursor-pointer overflow-hidden rounded-xl"
                  onClick={() => handleViewImage(selectedImages.image2!)}
                >
                  <img
                    src={`data:image/jpeg;base64,${selectedImages.image2}`}
                    alt="Receipt 2"
                    className="w-full h-auto rounded-xl border border-slate-200 dark:border-slate-700 transition-transform duration-300 group-hover:scale-[1.03]"
                  />
                  <div className="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition-colors rounded-xl flex items-center justify-center">
                    <div className="opacity-0 group-hover:opacity-100 transition-opacity transform translate-y-4 group-hover:translate-y-0 duration-300">
                      <div className="bg-white/90 dark:bg-slate-800/90 rounded-full p-3">
                        <ZoomIn className="w-6 h-6 text-teal-600 dark:text-teal-400" />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    )
  }

  const handleCancelRequest = async (requestId: string, type: string) => {
    try {
      const token = authService.getToken()
      if (!token) {
        throw new Error("No authentication token found")
      }

      const headers = {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      }

      let endpoint = ""
      if (type === "LEAVE") {
        endpoint = `/api/employee/leave-requests/${requestId}/cancel`
      } else if (type === "OVERTIME") {
        endpoint = `/api/overtime/${requestId}/cancel`
      } else if (type === "REIMBURSEMENT") {
        endpoint = `/api/employee/reimbursement-requests/${requestId}/cancel`
      }

      const response = await fetch(endpoint, {
        method: "PATCH",
        headers,
      })

      if (!response.ok) {
        throw new Error("Failed to cancel request")
      }

      // Update the request status in the local state
      setRequests((prevRequests) =>
        prevRequests.map((req) => (req.id === requestId ? { ...req, status: "CANCELED" } : req)),
      )

      toast.success("Request cancelled successfully")
      setCancelDialogOpen(false)
      setRequestToCancel(null)
    } catch (error) {
      console.error("Error cancelling request:", error)
      toast.error("Failed to cancel request")
    }
  }

  const openCancelDialog = (requestId: string, type: string) => {
    setRequestToCancel({ id: requestId, type })
    setCancelDialogOpen(true)
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-teal-50 via-cyan-50 to-sky-50 dark:from-slate-900 dark:via-teal-950 dark:to-slate-950 p-4 md:p-6">
        <div className="w-full max-w-6xl mx-auto space-y-8">
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
            <div>
              <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-2">
                <div className="h-12 w-12 bg-gradient-to-br from-teal-400 to-cyan-500 rounded-full flex items-center justify-center mr-2 shadow-lg shadow-teal-200 dark:shadow-teal-900/30">
                  <Briefcase className="h-6 w-6 text-white" />
                </div>
                My Requests
              </h1>
              <p className="text-slate-600 dark:text-slate-300 mt-1 text-lg">
                Track and manage all your workplace requests
              </p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            {Array.from({ length: 4 }).map((_, index) => (
              <div key={index} className="flex flex-col h-full">
                <Skeleton className="h-[180px] w-full rounded-xl" />
              </div>
            ))}
          </div>

          <Skeleton className="h-12 w-full max-w-md rounded-full" />

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {Array.from({ length: 8 }).map((_, index) => (
              <div key={`card-${index}`} className="flex flex-col h-full">
                <Skeleton className="h-[220px] w-full rounded-xl" />
              </div>
            ))}
          </div>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-teal-50 via-cyan-50 to-sky-50 dark:from-slate-900 dark:via-teal-950 dark:to-slate-950 p-4 md:p-6">
        <div className="w-full max-w-6xl mx-auto">
          <div className="text-center py-12 border border-dashed border-slate-200 dark:border-slate-700 rounded-xl bg-white/50 dark:bg-slate-800/20 shadow-xl">
            <div className="relative w-20 h-20 mx-auto mb-4">
              <div className="absolute inset-0 rounded-full bg-gradient-to-r from-rose-400 to-red-500 opacity-20 animate-pulse"></div>
              <div className="absolute inset-2 bg-white dark:bg-slate-800 rounded-full flex items-center justify-center">
                <AlertCircle className="h-10 w-10 text-rose-500 dark:text-rose-400" />
              </div>
            </div>
            <h3 className="text-2xl font-bold text-slate-800 dark:text-white mb-2">Error Loading Requests</h3>
            <p className="text-slate-600 dark:text-slate-300 max-w-md mx-auto mb-6">{error}</p>
            <Button
              className="bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-600 hover:to-cyan-600 text-white shadow-md rounded-full px-6"
              onClick={() => window.location.reload()}
            >
              <RefreshCw className="h-4 w-4 mr-2" />
              Try Again
            </Button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-teal-50 via-cyan-50 to-sky-50 dark:from-slate-900 dark:via-teal-950 dark:to-slate-950 p-4 md:p-6">
      <ReceiptModal />
      <ImageModal />
      <Dialog open={cancelDialogOpen} onOpenChange={setCancelDialogOpen}>
        <DialogContent className="sm:max-w-[425px] bg-white dark:bg-slate-800 border-none shadow-xl">
          <DialogHeader>
            <DialogTitle className="text-xl text-slate-800 dark:text-white">Cancel Request</DialogTitle>
            <DialogDescription className="text-slate-600 dark:text-slate-300">
              Are you sure you want to cancel this request? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter className="flex gap-2 sm:gap-0">
            <Button
              variant="outline"
              onClick={() => {
                setCancelDialogOpen(false)
                setRequestToCancel(null)
              }}
              className="border-slate-200 dark:border-slate-700"
            >
              No, keep request
            </Button>
            <Button
              variant="destructive"
              onClick={() => {
                if (requestToCancel) {
                  handleCancelRequest(requestToCancel.id, requestToCancel.type)
                }
              }}
              className="bg-rose-500 hover:bg-rose-600"
            >
              Yes, cancel request
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      <div className="w-full max-w-6xl mx-auto space-y-8">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-2">
              <div className="h-12 w-12 bg-gradient-to-br from-teal-400 to-cyan-500 rounded-full flex items-center justify-center mr-2 shadow-lg shadow-teal-200 dark:shadow-teal-900/30">
                <Briefcase className="h-6 w-6 text-white" />
              </div>
              My Requests
            </h1>
            <p className="text-slate-600 dark:text-slate-300 mt-1 text-lg">
              Track and manage all your workplace requests
            </p>
          </div>
        </div>

        {/* Request Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <Card className="border-none shadow-xl overflow-hidden bg-white dark:bg-slate-800 rounded-xl hover:shadow-2xl transition-shadow duration-200 group">
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-teal-400 to-cyan-500"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-slate-500 dark:text-slate-400">Total Requests</p>
                  <h3 className="text-3xl font-bold text-slate-800 dark:text-white mt-1">{requestCounts.total}</h3>
                </div>
                <div className="h-14 w-14 bg-teal-50 dark:bg-teal-900/30 rounded-full flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
                  <Briefcase className="h-7 w-7 text-teal-500 dark:text-teal-400" />
                </div>
              </div>
              <div className="mt-4 pt-4 border-t border-slate-100 dark:border-slate-700">
                <div className="flex justify-between text-xs text-slate-500 dark:text-slate-400">
                  <span>Pending: {requestCounts.pending}</span>
                  <span>Approved: {requestCounts.approved}</span>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="border-none shadow-xl overflow-hidden bg-white dark:bg-slate-800 rounded-xl hover:shadow-2xl transition-shadow duration-200 group">
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-teal-400 to-cyan-500"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-slate-500 dark:text-slate-400">Overtime</p>
                  <h3 className="text-3xl font-bold text-slate-800 dark:text-white mt-1">{requestCounts.overtime}</h3>
                </div>
                <div className="h-14 w-14 bg-teal-50 dark:bg-teal-900/30 rounded-full flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
                  <Clock className="h-7 w-7 text-teal-500 dark:text-teal-400" />
                </div>
              </div>
              <div className="mt-4 pt-4 border-t border-slate-100 dark:border-slate-700">
                <p className="text-xs text-slate-500 dark:text-slate-400">
                  {requestCounts.overtime > 0 ? "Track your extra hours and compensation" : "No overtime requests yet"}
                </p>
              </div>
            </CardContent>
          </Card>

          <Card className="border-none shadow-xl overflow-hidden bg-white dark:bg-slate-800 rounded-xl hover:shadow-2xl transition-shadow duration-200 group">
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-teal-400 to-cyan-500"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-slate-500 dark:text-slate-400">Leave</p>
                  <h3 className="text-3xl font-bold text-slate-800 dark:text-white mt-1">{requestCounts.leave}</h3>
                </div>
                <div className="h-14 w-14 bg-violet-50 dark:bg-violet-900/30 rounded-full flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
                  <Coffee className="h-7 w-7 text-violet-500 dark:text-violet-400" />
                </div>
              </div>
              <div className="mt-4 pt-4 border-t border-slate-100 dark:border-slate-700">
                <p className="text-xs text-slate-500 dark:text-slate-400">
                  {requestCounts.leave > 0 ? "Manage your time off and vacations" : "No leave requests yet"}
                </p>
              </div>
            </CardContent>
          </Card>

          <Card className="border-none shadow-xl overflow-hidden bg-white dark:bg-slate-800 rounded-xl hover:shadow-2xl transition-shadow duration-200 group">
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-teal-400 to-cyan-500"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-slate-500 dark:text-slate-400">Reimbursement</p>
                  <h3 className="text-3xl font-bold text-slate-800 dark:text-white mt-1">
                    {requestCounts.reimbursement}
                  </h3>
                </div>
                <div className="h-14 w-14 bg-amber-50 dark:bg-amber-900/30 rounded-full flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
                  <Receipt className="h-7 w-7 text-amber-500 dark:text-amber-400" />
                </div>
              </div>
              <div className="mt-4 pt-4 border-t border-slate-100 dark:border-slate-700">
                <p className="text-xs text-slate-500 dark:text-slate-400">
                  {requestCounts.reimbursement > 0
                    ? "Track your expense reimbursements"
                    : "No reimbursement requests yet"}
                </p>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Search and Filter */}
        <div className="flex flex-col md:flex-row gap-4 items-center">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-slate-500 dark:text-slate-400" />
            <Input
              placeholder="Search requests..."
              className="pl-10 border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 focus-visible:ring-teal-500 focus-visible:border-teal-500 rounded-full"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-2 bg-white dark:bg-slate-800 px-4 py-2 rounded-full shadow-md border border-slate-200 dark:border-slate-700">
              <Filter className="h-4 w-4 text-slate-500 dark:text-slate-400" />
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="bg-transparent border-none text-slate-700 dark:text-slate-300 focus:outline-none text-sm"
              >
                <option value="all">All Status</option>
                <option value="pending">Pending</option>
                <option value="approved">Approved</option>
                <option value="rejected">Rejected</option>
                <option value="canceled">Canceled</option>
              </select>
            </div>
            <Badge
              variant="outline"
              className="bg-teal-50 text-teal-600 border-teal-200 dark:bg-teal-900/30 dark:text-teal-400 dark:border-teal-800 px-3 py-1.5"
            >
              {filteredRequests.length} requests found
            </Badge>
          </div>
        </div>

        <Tabs defaultValue="all" className="w-6xl p-1.5" onValueChange={(value) => setActiveTab(value)}>
          <TabsList className="mb-6 bg-white dark:bg-slate-800 p-1.5 pb-1 rounded-full shadow-md border border-slate-200 dark:border-slate-700 w-full mx-auto flex justify-between">
            <TabsTrigger
              value="all"
              className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-teal-500 data-[state=active]:to-cyan-500 data-[state=active]:text-white rounded-full transition-all px-6 py-2"
            >
              <Shield className="h-4 w-4 mr-2" />
              All Requests
            </TabsTrigger>
            <TabsTrigger
              value="overtime"
              className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-teal-500 data-[state=active]:to-cyan-500 data-[state=active]:text-white rounded-full transition-all px-6 py-2"
            >
              <Clock className="h-4 w-4 mr-2" />
              Overtime
            </TabsTrigger>
            <TabsTrigger
              value="leave"
              className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-teal-500 data-[state=active]:to-cyan-500 data-[state=active]:text-white rounded-full transition-all px-6 py-2"
            >
              <Coffee className="h-4 w-4 mr-2" />
              Leave
            </TabsTrigger>
            <TabsTrigger
              value="reimbursement"
              className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-teal-500 data-[state=active]:to-cyan-500 data-[state=active]:text-white rounded-full transition-all px-6 py-2"
            >
              <Receipt className="h-4 w-4 mr-2 ml-4" />
              Reimbursement
            </TabsTrigger>
          </TabsList>

          {["all", "overtime", "leave", "reimbursement"].map((tabValue) => (
            <TabsContent key={tabValue} value={tabValue} className="space-y-6">
              {filteredRequests.length === 0 ? (
                <div className="text-center py-12 border border-dashed border-slate-200 dark:border-slate-700 rounded-xl bg-white/50 dark:bg-slate-800/20 shadow-lg">
                  <div className="relative w-20 h-20 mx-auto mb-4">
                    <div className="absolute inset-0 rounded-full bg-gradient-to-r from-teal-400 to-cyan-500 opacity-20 animate-pulse"></div>
                    <div className="absolute inset-2 bg-white dark:bg-slate-800 rounded-full flex items-center justify-center">
                      <FileText className="h-10 w-10 text-slate-500 dark:text-slate-400" />
                    </div>
                  </div>
                  <h3 className="text-2xl font-bold text-slate-800 dark:text-white mb-2">No requests found</h3>
                  <p className="text-slate-600 dark:text-slate-300 max-w-md mx-auto mb-6">
                    We couldn't find any {tabValue !== "all" ? tabValue : ""} requests matching your filters.
                  </p>
                  <Button
                    className="bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-600 hover:to-cyan-600 text-white shadow-md rounded-full px-6"
                    onClick={() => {
                      setSearchTerm("")
                      setStatusFilter("all")
                    }}
                  >
                    <RefreshCw className="h-4 w-4 mr-2" />
                    Reset Filters
                  </Button>
                </div>
              ) : (
                <>
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    {getCurrentPageItems().map((request, index) => (
                      <div key={`${request.type}-${request.id}-${index}`} className="relative h-full">
                        <Card className="overflow-hidden border-none shadow-xl hover:shadow-2xl transition-all duration-200 flex flex-col h-full rounded-xl bg-white dark:bg-slate-800 group">
                          <div
                            className={`h-2 w-full bg-gradient-to-r ${getTypeColor(
                              request.type,
                            )} absolute top-0 left-0 z-10`}
                          ></div>
                          <CardHeader className="p-5 pb-2 flex flex-col space-y-2">
                            <div className="flex items-center justify-between w-full">
                              <div
                                className={`w-12 h-12 rounded-full ${getTypeBgColor(
                                  request.type,
                                )} flex items-center justify-center group-hover:scale-110 transition-transform duration-300`}
                              >
                                {getTypeIcon(request.type)}
                              </div>
                              <Badge className={`${getStatusColor(request.status)} ml-auto px-3 py-1`}>
                                <div className="flex items-center">
                                  {getStatusIcon(request.status)}
                                  <span>{request.status}</span>
                                </div>
                              </Badge>
                            </div>
                            <CardTitle className={`text-lg font-bold ${getTypeTextColor(request.type)} mt-2`}>
                              {request.type === "LEAVE" ? (
                                <span>{request.leaveType}</span>
                              ) : request.type === "OVERTIME" ? (
                                <span>Overtime Request</span>
                              ) : (
                                <span>Reimbursement</span>
                              )}
                            </CardTitle>
                          </CardHeader>
                          <CardContent className="p-5 pt-0 flex-grow flex flex-col justify-between">
                            <div className="space-y-3">
                              {request.type === "LEAVE" && request.startDate && request.endDate && (
                                <div className="flex items-center text-sm text-slate-600 dark:text-slate-300">
                                  <Calendar className="h-4 w-4 min-w-4 mr-1.5 text-violet-500" />
                                  <span>
                                    {format(new Date(request.startDate), "MMM d, yyyy")} -{" "}
                                    {format(new Date(request.endDate), "MMM d, yyyy")}
                                  </span>
                                </div>
                              )}

                              {request.type === "OVERTIME" && request.startTime && request.endTime && (
                                <div className="flex items-center text-sm text-slate-600 dark:text-slate-300">
                                  <Clock className="h-4 w-4 min-w-4 mr-1.5 text-teal-500" />
                                  <span>
                                    {format(new Date(request.date + "T" + request.startTime), "MMM d, yyyy | h:mm a")} -{" "}
                                    {format(new Date(request.date + "T" + request.endTime), "h:mm a")}
                                  </span>
                                </div>
                              )}

                              {request.type === "OVERTIME" && request.totalHours && (
                                <div className="flex items-center text-sm text-slate-600 dark:text-slate-300">
                                  <Zap className="h-4 w-4 min-w-4 mr-1.5 text-teal-500" />
                                  <span>Duration: {request.totalHours} hour(s)</span>
                                </div>
                              )}

                              {request.type === "REIMBURSEMENT" && request.amount && (
                                <div className="flex items-center text-sm text-slate-600 dark:text-slate-300">
                                  <DollarSign className="h-4 w-4 min-w-4 mr-1.5 text-amber-500" />
                                  <span>${request.amount.toFixed(2)}</span>
                                </div>
                              )}

                              {request.type === "REIMBURSEMENT" && (
                                <>
                                  {(request.receiptImage1 || request.receiptImage2) && (
                                    <Button
                                      variant="outline"
                                      size="sm"
                                      className="mt-2 w-full border-slate-200 dark:border-slate-700 hover:border-amber-500 dark:hover:border-amber-500 text-slate-600 dark:text-slate-300 rounded-full"
                                      onClick={() => handleViewReceipts(request.receiptImage1, request.receiptImage2)}
                                    >
                                      <FileText className="h-4 w-4 mr-2 text-amber-500" />
                                      View Receipts
                                    </Button>
                                  )}
                                </>
                              )}

                              {request.description && (
                                <div className="mt-3 bg-slate-50 dark:bg-slate-900/50 p-3 rounded-lg">
                                  <p className="text-xs font-medium text-slate-500 dark:text-slate-400 mb-1">Reason:</p>
                                  <p className="text-sm text-slate-700 dark:text-slate-300 break-words whitespace-pre-wrap">
                                    {request.description}
                                  </p>
                                </div>
                              )}

                              {request.status === "PENDING" && (
                                <Button
                                  variant="destructive"
                                  size="sm"
                                  className="mt-3 w-full bg-rose-100 hover:bg-rose-200 text-rose-700 dark:bg-rose-900/30 dark:hover:bg-rose-900/50 dark:text-rose-400 rounded-full border border-rose-200 dark:border-rose-800"
                                  onClick={() => openCancelDialog(request.id, request.type)}
                                >
                                  <X className="h-4 w-4 mr-2" />
                                  Cancel Request
                                </Button>
                              )}
                            </div>

                            <div className="mt-4 pt-3 border-t border-slate-100 dark:border-slate-700 text-xs text-slate-500 dark:text-slate-400">
                              <div className="flex justify-between items-center">
                                <span className="truncate max-w-[100px]" title={request.id}>
                                  ID: {request.id.substring(0, 8)}...
                                </span>
                                <span>{format(new Date(request.date || request.startDate || ""), "MMM d, yyyy")}</span>
                              </div>
                            </div>
                          </CardContent>
                        </Card>
                      </div>
                    ))}
                  </div>

                  {totalPages > 1 && (
                    <div className="flex justify-center mt-8">
                      <Pagination>
                        <PaginationContent>
                          <PaginationItem>
                            <PaginationPrevious
                              onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
                              className={cn(
                                "border border-slate-200 dark:border-slate-700 rounded-full",
                                currentPage === 1
                                  ? "pointer-events-none opacity-50"
                                  : "hover:border-teal-500 dark:hover:border-teal-500 text-slate-600 dark:text-slate-300",
                              )}
                            />
                          </PaginationItem>

                          {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
                            <PaginationItem key={page}>
                              <PaginationLink
                                onClick={() => setCurrentPage(page)}
                                isActive={currentPage === page}
                                className={
                                  currentPage === page
                                    ? "bg-gradient-to-r from-teal-500 to-cyan-500 text-white border-transparent hover:from-teal-600 hover:to-cyan-600 rounded-full"
                                    : "text-slate-600 dark:text-slate-300 border border-slate-200 dark:border-slate-700 hover:border-teal-500 dark:hover:border-teal-500 rounded-full"
                                }
                              >
                                {page}
                              </PaginationLink>
                            </PaginationItem>
                          ))}

                          <PaginationItem>
                            <PaginationNext
                              onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))}
                              className={cn(
                                "border border-slate-200 dark:border-slate-700 rounded-full",
                                currentPage === totalPages
                                  ? "pointer-events-none opacity-50"
                                  : "hover:border-teal-500 dark:hover:border-teal-500 text-slate-600 dark:text-slate-300",
                              )}
                            />
                          </PaginationItem>
                        </PaginationContent>
                      </Pagination>
                    </div>
                  )}
                </>
              )}
            </TabsContent>
          ))}
        </Tabs>
      </div>
    </div>
  )
}
