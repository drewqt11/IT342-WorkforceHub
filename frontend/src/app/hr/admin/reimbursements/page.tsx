"use client"

import { useEffect, useState, useRef } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { authService } from "@/lib/auth"
import { format } from "date-fns"
import { Search, Calendar, RefreshCw, CalendarDays, Filter, Users, CheckCircle, XCircle, AlertCircle, Receipt, ZoomIn, ZoomOut, RotateCw, Minimize2, Maximize2, ExternalLink } from "lucide-react"
import { Skeleton } from "@/components/ui/skeleton"
import { cn } from "@/lib/utils"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Badge } from "@/components/ui/badge"
import { Toaster, toast } from "sonner"
import { Pagination, PaginationContent, PaginationItem, PaginationLink, PaginationNext, PaginationPrevious } from "@/components/ui/pagination"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"

interface ReimbursementRequest {
  reimbursementId: string
  employeeId: string
  employeeName: string
  requestDate: string
  expenseDate: string
  amountRequested: number
  receiptImage1: string
  receiptImage2: string | null
  reason: string
  status: string
  reviewedById: string | null
  reviewedByName: string | null
  reviewedAt: string | null
  remarks: string | null
}

export default function ReimbursementRequestsPage() {
  const router = useRouter()
  const [reimbursementRequests, setReimbursementRequests] = useState<ReimbursementRequest[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [totalPages, setTotalPages] = useState(0)
  const [searchTerm, setSearchTerm] = useState("")
  const [sortBy, setSortBy] = useState("requestDate")
  const [direction, setDirection] = useState("desc")
  const [totalRecords, setTotalRecords] = useState(0)
  const [statusFilter, setStatusFilter] = useState<string>("PENDING")
  const [counts, setCounts] = useState({
    total: 0,
    pending: 0,
    approved: 0,
    rejected: 0
  })
  const [selectedReceipt, setSelectedReceipt] = useState<string | null>(null)
  const [remarks, setRemarks] = useState("")
  const [zoom, setZoom] = useState(1)
  const [rotation, setRotation] = useState(0)
  const [isFullscreen, setIsFullscreen] = useState(false)
  const [position, setPosition] = useState({ x: 0, y: 0 })
  const [isDragging, setIsDragging] = useState(false)
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 })
  const contentRef = useRef<HTMLDivElement>(null)

  const fetchCounts = async () => {
    try {
      const token = authService.getToken()
      if (!token) {
        router.push("/")
        return
      }

      const [pendingRes, approvedRes, rejectedRes] = await Promise.all([
        fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/reimbursement-requests?status=PENDING&page=0&size=1`, {
          headers: { Authorization: `Bearer ${token}` }
        }),
        fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/reimbursement-requests?status=APPROVED&page=0&size=1`, {
          headers: { Authorization: `Bearer ${token}` }
        }),
        fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/reimbursement-requests?status=REJECTED&page=0&size=1`, {
          headers: { Authorization: `Bearer ${token}` }
        })
      ])

      if (!pendingRes.ok || !approvedRes.ok || !rejectedRes.ok) {
        throw new Error("Failed to fetch counts")
      }

      const [pendingData, approvedData, rejectedData] = await Promise.all([
        pendingRes.json(),
        approvedRes.json(),
        rejectedRes.json()
      ])

      const pendingCount = pendingData.totalElements || 0
      const approvedCount = approvedData.totalElements || 0
      const rejectedCount = rejectedData.totalElements || 0

      setCounts({
        total: pendingCount + approvedCount + rejectedCount,
        pending: pendingCount,
        approved: approvedCount,
        rejected: rejectedCount
      })
    } catch (err) {
      console.error("Error fetching counts:", err)
      setCounts({
        total: 0,
        pending: 0,
        approved: 0,
        rejected: 0
      })
    }
  }

  useEffect(() => {
    fetchCounts()
  }, [statusFilter])

  const fetchReimbursementRequests = async () => {
    try {
      setLoading(true)
      const token = authService.getToken()
      if (!token) {
        toast.error("Authentication required. Please login again.")
        router.push("/")
        return
      }

      const url = `${process.env.NEXT_PUBLIC_API_URL}/hr/reimbursement-requests?status=${statusFilter}&page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`

      const response = await fetch(url, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        const errorData = await response.json()
        if (response.status === 400) {
          toast.error(errorData.message || "Invalid request parameters")
        } else if (response.status === 401) {
          toast.error("Session expired. Please login again.")
          router.push("/")
        } else if (response.status === 403) {
          toast.error("You don't have permission to view reimbursement requests")
        } else {
          toast.error(errorData.message || "Failed to fetch reimbursement requests")
        }
        return
      }

      const data = await response.json()
      setReimbursementRequests(data.content)
      setTotalPages(data.totalPages)
      setTotalRecords(data.totalElements)
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "An error occurred while fetching reimbursement requests")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchReimbursementRequests()
  }, [page, size, sortBy, direction, statusFilter])

  const handleSort = (column: string) => {
    if (sortBy === column) {
      setDirection(direction === "asc" ? "desc" : "asc")
    } else {
      setSortBy(column)
      setDirection("asc")
    }
  }

  const handleApprove = async (requestId: string) => {
    try {
      const token = authService.getToken()
      if (!token) {
        toast.error("Authentication required. Please login again.")
        router.push("/")
        return
      }

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/hr/reimbursement-requests/${requestId}/approve`,
        {
          method: "PATCH",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ remarks }),
        }
      )

      if (!response.ok) {
        const errorData = await response.json()
        if (response.status === 400) {
          toast.error(errorData.message || "Invalid request data")
        } else if (response.status === 401) {
          toast.error("Session expired. Please login again.")
          router.push("/")
        } else if (response.status === 403) {
          toast.error("You don't have permission to approve reimbursement requests")
        } else if (response.status === 404) {
          toast.error("Reimbursement request not found")
        } else {
          toast.error(errorData.message || "Failed to approve reimbursement request")
        }
        return
      }

      toast.success("Reimbursement request approved successfully")
      fetchReimbursementRequests()
      fetchCounts()
      setRemarks("")
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to approve reimbursement request")
    }
  }

  const handleReject = async (requestId: string) => {
    try {
      const token = authService.getToken()
      if (!token) {
        toast.error("Authentication required. Please login again.")
        router.push("/")
        return
      }

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/hr/reimbursement-requests/${requestId}/reject`,
        {
          method: "PATCH",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ remarks }),
        }
      )

      if (!response.ok) {
        const errorData = await response.json()
        if (response.status === 400) {
          toast.error(errorData.message || "Invalid request data")
        } else if (response.status === 401) {
          toast.error("Session expired. Please login again.")
          router.push("/")
        } else if (response.status === 403) {
          toast.error("You don't have permission to reject reimbursement requests")
        } else if (response.status === 404) {
          toast.error("Reimbursement request not found")
        } else {
          toast.error(errorData.message || "Failed to reject reimbursement request")
        }
        return
      }

      toast.success("Reimbursement request rejected successfully")
      fetchReimbursementRequests()
      fetchCounts()
      setRemarks("")
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to reject reimbursement request")
    }
  }

  const filteredRequests = reimbursementRequests.filter((request) => {
    const matchesSearch = request.employeeName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      request.reason.toLowerCase().includes(searchTerm.toLowerCase())

    return matchesSearch
  })

  const getStatusColor = (status: string) => {
    switch (status) {
      case "PENDING":
        return "bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400"
      case "APPROVED":
        return "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400"
      case "REJECTED":
        return "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400"
      default:
        return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-400"
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "PENDING":
        return <Calendar className="h-3.5 w-3.5 mr-1" />
      case "APPROVED":
        return <CheckCircle className="h-3.5 w-3.5 mr-1" />
      case "REJECTED":
        return <XCircle className="h-3.5 w-3.5 mr-1" />
      default:
        return null
    }
  }

  const handleCardClick = (status: string) => {
    setStatusFilter(status)
    setPage(0)
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount)
  }

  const toggleFullscreen = async () => {
    if (!contentRef.current) return

    try {
      if (!isFullscreen) {
        if (contentRef.current.requestFullscreen) {
          await contentRef.current.requestFullscreen()
        }
      } else {
        if (document.exitFullscreen) {
          await document.exitFullscreen()
        }
      }
    } catch (error) {
      console.error("Error toggling fullscreen:", error)
    }
  }

  useEffect(() => {
    const handleFullscreenChange = () => {
      setIsFullscreen(!!document.fullscreenElement)
    }

    document.addEventListener("fullscreenchange", handleFullscreenChange)
    return () => {
      document.removeEventListener("fullscreenchange", handleFullscreenChange)
    }
  }, [])

  const handleMouseDown = (e: React.MouseEvent) => {
    if (zoom > 1) {
      setIsDragging(true)
      setDragStart({ x: e.clientX - position.x, y: e.clientY - position.y })
    }
  }

  const handleMouseMove = (e: React.MouseEvent) => {
    if (isDragging && zoom > 1) {
      setPosition({
        x: e.clientX - dragStart.x,
        y: e.clientY - dragStart.y
      })
    }
  }

  const handleMouseUp = () => {
    setIsDragging(false)
  }

  const handleZoom = (newZoom: number) => {
    setZoom(newZoom)
    if (newZoom <= 1) {
      setPosition({ x: 0, y: 0 })
    }
  }

  useEffect(() => {
    const handleMouseUp = () => {
      setIsDragging(false)
    }

    document.addEventListener('mouseup', handleMouseUp)
    return () => {
      document.removeEventListener('mouseup', handleMouseUp)
    }
  }, [])

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
      <Toaster position="top-right" richColors className="mt-24" style={{ top: "6rem", right: "1rem" }} />
      <div className="w-full max-w-6xl mx-auto space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white flex items-center gap-2">
              <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-1 shadow-md">
                <Receipt className="h-5 w-5 text-white" />
              </div>
              Reimbursement Requests
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
              Review and manage employee reimbursement requests
            </p>
          </div>
          <Button
            onClick={fetchReimbursementRequests}
            className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-md hover:shadow-lg"
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh List
          </Button>
        </div>

        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-md overflow-hidden bg-white dark:bg-[#1F2937] hover:shadow-lg transition-shadow duration-200">
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">
                    Total Requests
                  </p>
                  <h3 className="text-3xl font-bold text-[#1F2937] dark:text-white mt-1">
                    {loading ? <Skeleton className="h-9 w-16" /> : counts.total}
                  </h3>
                </div>
                <div className="h-12 w-12 bg-[#EFF6FF] dark:bg-[#1E3A8A]/30 rounded-full flex items-center justify-center">
                  <Receipt className="h-6 w-6 text-[#3B82F6] dark:text-[#3B82F6]" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card 
            className={cn(
              "border border-[#E5E7EB] dark:border-[#374151] shadow-md overflow-hidden bg-white dark:bg-[#1F2937] hover:shadow-lg transition-all duration-200 cursor-pointer",
              statusFilter === "PENDING" && "ring-2 ring-[#D97706] dark:ring-[#F59E0B]"
            )}
            onClick={() => handleCardClick("PENDING")}
          >
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">
                    Pending Requests
                  </p>
                  <h3 className="text-3xl font-bold text-[#1F2937] dark:text-white mt-1">
                    {loading ? <Skeleton className="h-9 w-16" /> : counts.pending}
                  </h3>
                </div>
                <div className="h-12 w-12 bg-[#FEF3C7] dark:bg-[#92400E]/30 rounded-full flex items-center justify-center">
                  <Calendar className="h-6 w-6 text-[#D97706] dark:text-[#F59E0B]" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card 
            className={cn(
              "border border-[#E5E7EB] dark:border-[#374151] shadow-md overflow-hidden bg-white dark:bg-[#1F2937] hover:shadow-lg transition-all duration-200 cursor-pointer",
              statusFilter === "APPROVED" && "ring-2 ring-[#22C55E] dark:ring-[#22C55E]"
            )}
            onClick={() => handleCardClick("APPROVED")}
          >
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">
                    Approved Requests
                  </p>
                  <h3 className="text-3xl font-bold text-[#1F2937] dark:text-white mt-1">
                    {loading ? <Skeleton className="h-9 w-16" /> : counts.approved}
                  </h3>
                </div>
                <div className="h-12 w-12 bg-[#F0FDF4] dark:bg-[#166534]/30 rounded-full flex items-center justify-center">
                  <CheckCircle className="h-6 w-6 text-[#22C55E] dark:text-[#22C55E]" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card 
            className={cn(
              "border border-[#E5E7EB] dark:border-[#374151] shadow-md overflow-hidden bg-white dark:bg-[#1F2937] hover:shadow-lg transition-all duration-200 cursor-pointer",
              statusFilter === "REJECTED" && "ring-2 ring-[#EF4444] dark:ring-[#EF4444]"
            )}
            onClick={() => handleCardClick("REJECTED")}
          >
            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
            <CardContent className="p-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">
                    Rejected Requests
                  </p>
                  <h3 className="text-3xl font-bold text-[#1F2937] dark:text-white mt-1">
                    {loading ? <Skeleton className="h-9 w-16" /> : counts.rejected}
                  </h3>
                </div>
                <div className="h-12 w-12 bg-[#FEF2F2] dark:bg-[#991B1B]/30 rounded-full flex items-center justify-center">
                  <XCircle className="h-6 w-6 text-[#EF4444] dark:text-[#EF4444]" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Main Reimbursement Requests Card */}
        <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-xl overflow-hidden bg-white dark:bg-[#1F2937]">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
          <CardHeader className="bg-[#F9FAFB] dark:bg-[#111827] border-b border-[#E5E7EB] dark:border-[#374151]">
            <div className="flex flex-col md:flex-row justify-between md:items-center gap-4">
              <div>
                <CardTitle className="text-xl text-[#1F2937] dark:text-white flex items-center gap-2">
                  <Receipt className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
                  Reimbursement Requests Directory
                </CardTitle>
                <CardDescription className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
                  Review and manage employee reimbursement requests
                </CardDescription>
              </div>
              <div className="flex items-center gap-2">
                <Badge
                  variant="outline"
                  className="bg-[#F0FDFA] text-[#14B8A6] border-[#99F6E4] dark:bg-[#134E4A]/30 dark:text-[#14B8A6] dark:border-[#134E4A] px-3 py-1.5"
                >
                  {filteredRequests.length} requests found
                </Badge>
              </div>
            </div>
          </CardHeader>
          <CardContent className="p-6">
            <div className="flex flex-col md:flex-row gap-4 mb-6">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF]" />
                <Input
                  placeholder="Search by employee name or reason..."
                  className="pl-10 border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827] focus-visible:ring-[#3B82F6] focus-visible:border-[#3B82F6]"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              <div className="flex gap-2">
                <Select value={statusFilter} onValueChange={setStatusFilter}>
                  <SelectTrigger className="w-[180px] border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827] focus:ring-[#3B82F6]">
                    <div className="flex items-center gap-2">
                      <Filter className="h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF]" />
                      <SelectValue placeholder="Filter by status" />
                    </div>
                  </SelectTrigger>
                  <SelectContent className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#1F2937]">
                    <SelectItem value="PENDING">Pending</SelectItem>
                    <SelectItem value="APPROVED">Approved</SelectItem>
                    <SelectItem value="REJECTED">Rejected</SelectItem>
                  </SelectContent>
                </Select>
                <Select value={sortBy} onValueChange={(value) => handleSort(value)}>
                  <SelectTrigger className="w-[180px] border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#111827] focus:ring-[#3B82F6]">
                    <div className="flex items-center gap-2">
                      <Filter className="h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF]" />
                      <SelectValue placeholder="Sort by" />
                    </div>
                  </SelectTrigger>
                  <SelectContent className="border-[#E5E7EB] dark:border-[#374151] bg-white dark:bg-[#1F2937]">
                    <SelectItem value="employeeName">Employee Name</SelectItem>
                    <SelectItem value="requestDate">Request Date</SelectItem>
                    <SelectItem value="expenseDate">Expense Date</SelectItem>
                    <SelectItem value="amountRequested">Amount</SelectItem>
                    <SelectItem value="status">Status</SelectItem>
                  </SelectContent>
                </Select>
                <Button
                  variant="outline"
                  size="icon"
                  onClick={() => setDirection(direction === "asc" ? "desc" : "asc")}
                  className="border-[#E5E7EB] dark:border-[#374151] w-10 h-10"
                >
                  {direction === "asc" ? "↑" : "↓"}
                </Button>
              </div>
            </div>

            {error && (
              <div className="mb-6 p-4 border border-red-200 dark:border-red-800 rounded-md bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400">
                <p className="font-medium">Error</p>
                <p className="text-sm">{error}</p>
              </div>
            )}

            {loading ? (
              <div className="space-y-4">
                {Array.from({ length: 5 }).map((_, index) => (
                  <div key={index} className="flex items-center space-x-4">
                    <Skeleton className="h-12 w-full rounded-md" />
                  </div>
                ))}
              </div>
            ) : filteredRequests.length === 0 ? (
              <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
                <div className="relative w-16 h-16 mx-auto mb-4">
                  <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] opacity-20 animate-pulse"></div>
                  <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                    <AlertCircle className="h-8 w-8 text-[#6B7280] dark:text-[#9CA3AF]" />
                  </div>
                </div>
                <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">
                  No requests found
                </h3>
                <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto mb-6">
                  We couldn't find any reimbursement requests matching your current filters. Try adjusting your search criteria.
                </p>
                <Button
                  className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white shadow-md"
                  onClick={() => {
                    setSearchTerm("")
                    setStatusFilter("PENDING")
                  }}
                >
                  <RefreshCw className="h-4 w-4 mr-2" />
                  Reset Filters
                </Button>
              </div>
            ) : (
              <div className="rounded-lg border border-[#E5E7EB] dark:border-[#374151] overflow-hidden">
                <Table>
                  <TableHeader className="bg-[#F9FAFB] dark:bg-[#111827]">
                    <TableRow className="hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151]">
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Employee</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Request Date</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Expense Date</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Amount</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Reason</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Receipts</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Status</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredRequests.map((request, index) => (
                      <TableRow
                        key={request.reimbursementId}
                        className={cn(
                          "hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151] group transition-colors",
                          index % 2 === 0 ? "bg-[#F9FAFB] dark:bg-[#111827]/50" : ""
                        )}
                      >
                        <TableCell className="font-medium text-[#1F2937] dark:text-white">
                          <div className="flex items-center gap-2">
                            <div className="h-6 w-1 rounded-full bg-gradient-to-b from-[#3B82F6] to-[#14B8A6] transition-all duration-300 group-hover:h-full"></div>
                            <div className="font-medium">{request.employeeName}</div>
                          </div>
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          {format(new Date(request.requestDate), "MMM dd, yyyy")}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                          {format(new Date(request.expenseDate), "MMM dd, yyyy")}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          {formatCurrency(request.amountRequested)}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] max-w-[300px]">
                          <div className="max-h-[7.5em] overflow-y-auto">
                            <p className="text-sm leading-tight break-words whitespace-normal">
                              {request.reason}
                            </p>
                          </div>
                        </TableCell>
                        <TableCell>
                          <div className="flex gap-2">
                            <Dialog>
                              <DialogTrigger asChild>
                                <Button
                                  variant="outline"
                                  size="sm"
                                  className="border-[#E5E7EB] dark:border-[#374151] hover:border-[#3B82F6] dark:hover:border-[#3B82F6]"
                                  onClick={() => setSelectedReceipt(request.receiptImage1)}
                                >
                                  Receipt 1
                                </Button>
                              </DialogTrigger>
                              <DialogContent fullWidth className="w-[90vw] h-[90vh] p-0 overflow-hidden rounded-2xl border-none">
                                <DialogHeader className="flex items-center justify-between bg-white p-4 border-b">
                                  <DialogTitle className="text-xl font-semibold text-[#1F2937]">Receipt Image</DialogTitle>
                                  <div className="grid grid-cols-4 items-center gap-2 mr-10">
                                    <Button variant="outline" size="sm" onClick={() => handleZoom(Math.min(zoom + 0.25, 3))} title="Zoom In">
                                      <ZoomIn className="h-4 w-4" />
                                    </Button>
                                    <Button variant="outline" size="sm" onClick={() => handleZoom(Math.max(zoom - 0.25, 0.5))} title="Zoom Out">
                                      <ZoomOut className="h-4 w-4" />
                                    </Button>
                                    <Button variant="outline" size="sm" onClick={() => setRotation(prev => (prev + 90) % 360)} title="Rotate">
                                      <RotateCw className="h-4 w-4" />
                                    </Button>
                                    <Button variant="outline" size="sm" onClick={toggleFullscreen} title={isFullscreen ? "Exit Fullscreen" : "View in Fullscreen"}>
                                      {isFullscreen ? <Minimize2 className="h-4 w-4" /> : <Maximize2 className="h-4 w-4" />}
                                    </Button>
                                  </div>
                                </DialogHeader>
                                <div 
                                  ref={contentRef}
                                  className="flex-1 overflow-hidden bg-[#F9FAFB]"
                                  style={{ height: isFullscreen ? 'calc(100vh - 73px)' : 'calc(90vh - 73px)' }}
                                >
                                  <div 
                                    className="flex justify-center items-center h-full w-full"
                                    onMouseDown={handleMouseDown}
                                    onMouseMove={handleMouseMove}
                                    onMouseUp={handleMouseUp}
                                    style={{ cursor: zoom > 1 ? (isDragging ? 'grabbing' : 'grab') : 'default' }}
                                  >
                                    <img
                                      src={`data:image/jpeg;base64,${request.receiptImage1}`}
                                      alt="Receipt 1"
                                      className="max-w-full max-h-full object-contain select-none"
                                      style={{
                                        transform: `scale(${zoom}) rotate(${rotation}deg) translate(${position.x}px, ${position.y}px)`,
                                        transition: isDragging ? 'none' : 'transform 0.3s ease',
                                      }}
                                    />
                                  </div>
                                </div>
                              </DialogContent>
                            </Dialog>
                            {request.receiptImage2 && (
                              <Dialog>
                                <DialogTrigger asChild>
                                  <Button
                                    variant="outline"
                                    size="sm"
                                    className="border-[#E5E7EB] dark:border-[#374151] hover:border-[#3B82F6] dark:hover:border-[#3B82F6]"
                                    onClick={() => setSelectedReceipt(request.receiptImage2)}
                                  >
                                    Receipt 2
                                  </Button>
                                </DialogTrigger>
                                <DialogContent fullWidth className="w-[90vw] h-[90vh] p-0 overflow-hidden rounded-2xl border-none">
                                  <DialogHeader className="flex items-center justify-between bg-white p-4 border-b">
                                    <DialogTitle className="text-xl font-semibold text-[#1F2937]">Receipt Image</DialogTitle>
                                    <div className="grid grid-cols-4 items-center gap-2 mr-10">
                                      <Button variant="outline" size="sm" onClick={() => handleZoom(Math.min(zoom + 0.25, 3))} title="Zoom In">
                                        <ZoomIn className="h-4 w-4" />
                                      </Button>
                                      <Button variant="outline" size="sm" onClick={() => handleZoom(Math.max(zoom - 0.25, 0.5))} title="Zoom Out">
                                        <ZoomOut className="h-4 w-4" />
                                      </Button>
                                      <Button variant="outline" size="sm" onClick={() => setRotation(prev => (prev + 90) % 360)} title="Rotate">
                                        <RotateCw className="h-4 w-4" />
                                      </Button>
                                      <Button variant="outline" size="sm" onClick={toggleFullscreen} title={isFullscreen ? "Exit Fullscreen" : "View in Fullscreen"}>
                                        {isFullscreen ? <Minimize2 className="h-4 w-4" /> : <Maximize2 className="h-4 w-4" />}
                                      </Button>
                                    </div>
                                  </DialogHeader>
                                  <div 
                                    ref={contentRef}
                                    className="flex-1 overflow-hidden bg-[#F9FAFB]"
                                    style={{ height: isFullscreen ? 'calc(100vh - 73px)' : 'calc(90vh - 73px)' }}
                                  >
                                    <div 
                                      className="absolute inset-0 flex items-center justify-center p-4"
                                      onMouseDown={handleMouseDown}
                                      onMouseMove={handleMouseMove}
                                      onMouseUp={handleMouseUp}
                                      style={{ cursor: zoom > 1 ? (isDragging ? 'grabbing' : 'grab') : 'default' }}
                                    >
                                      <img
                                        src={`data:image/jpeg;base64,${request.receiptImage2}`}
                                        alt="Receipt 2"
                                        className="max-w-full max-h-full object-contain select-none"
                                        style={{
                                          transform: `scale(${zoom}) rotate(${rotation}deg) translate(${position.x}px, ${position.y}px)`,
                                          transition: isDragging ? 'none' : 'transform 0.3s ease',
                                        }}
                                      />
                                    </div>
                                  </div>
                                </DialogContent>
                              </Dialog>
                            )}
                          </div>
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant="outline"
                            className={cn("border-2 flex items-center", getStatusColor(request.status))}
                          >
                            {getStatusIcon(request.status)}
                            <span>{request.status}</span>
                          </Badge>
                        </TableCell>
                        <TableCell>
                          {request.status === "PENDING" && (
                            <div className="flex gap-2">
                              <Dialog>
                                <DialogTrigger asChild>
                                  <Button
                                    variant="ghost"
                                    size="icon"
                                    className="h-8 w-8 hover:bg-green-100 dark:hover:bg-green-900/30"
                                  >
                                    <CheckCircle className="h-4 w-4 text-green-600 dark:text-green-400" />
                                  </Button>
                                </DialogTrigger>
                                <DialogContent>
                                  <DialogHeader>
                                    <DialogTitle>Approve Reimbursement Request</DialogTitle>
                                  </DialogHeader>
                                  <div className="space-y-4">
                                    <div>
                                      <Label htmlFor="remarks">Remarks (Optional)</Label>
                                      <Input
                                        id="remarks"
                                        value={remarks}
                                        onChange={(e) => setRemarks(e.target.value)}
                                        placeholder="Enter approval remarks..."
                                        className="mt-1"
                                      />
                                    </div>
                                    <div className="flex justify-end gap-2">
                                      <Button
                                        variant="outline"
                                        onClick={() => setRemarks("")}
                                      >
                                        Cancel
                                      </Button>
                                      <Button
                                        className="bg-green-600 hover:bg-green-700 text-white"
                                        onClick={() => handleApprove(request.reimbursementId)}
                                      >
                                        Approve
                                      </Button>
                                    </div>
                                  </div>
                                </DialogContent>
                              </Dialog>
                              <Dialog>
                                <DialogTrigger asChild>
                                  <Button
                                    variant="ghost"
                                    size="icon"
                                    className="h-8 w-8 hover:bg-red-100 dark:hover:bg-red-900/30"
                                  >
                                    <XCircle className="h-4 w-4 text-red-600 dark:text-red-400" />
                                  </Button>
                                </DialogTrigger>
                                <DialogContent>
                                  <DialogHeader>
                                    <DialogTitle>Reject Reimbursement Request</DialogTitle>
                                  </DialogHeader>
                                  <div className="space-y-4">
                                    <div>
                                      <Label htmlFor="rejectRemarks">Rejection Reason (Required)</Label>
                                      <Input
                                        id="rejectRemarks"
                                        value={remarks}
                                        onChange={(e) => setRemarks(e.target.value)}
                                        placeholder="Enter rejection reason..."
                                        className="mt-1"
                                      />
                                    </div>
                                    <div className="flex justify-end gap-2">
                                      <Button
                                        variant="outline"
                                        onClick={() => setRemarks("")}
                                      >
                                        Cancel
                                      </Button>
                                      <Button
                                        className="bg-red-600 hover:bg-red-700 text-white"
                                        onClick={() => handleReject(request.reimbursementId)}
                                        disabled={!remarks.trim()}
                                      >
                                        Reject
                                      </Button>
                                    </div>
                                  </div>
                                </DialogContent>
                              </Dialog>
                            </div>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}

            {totalPages > 1 && (
              <div className="mt-6 flex justify-center">
                <Pagination>
                  <PaginationContent>
                    <PaginationItem>
                      <PaginationPrevious
                        onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                        className={cn(
                          "border border-[#E5E7EB] dark:border-[#374151]",
                          page === 0
                            ? "pointer-events-none opacity-50"
                            : "hover:border-[#3B82F6] dark:hover:border-[#3B82F6] text-[#4B5563] dark:text-[#D1D5DB]"
                        )}
                      />
                    </PaginationItem>

                    {Array.from({ length: totalPages }, (_, i) => i).map((pageNum) => (
                      <PaginationItem key={pageNum}>
                        <PaginationLink
                          onClick={() => setPage(pageNum)}
                          isActive={page === pageNum}
                          className={
                            page === pageNum
                              ? "bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] text-white border-transparent hover:from-[#2563EB] hover:to-[#0D9488]"
                              : "text-[#4B5563] dark:text-[#D1D5DB] border border-[#E5E7EB] dark:border-[#374151] hover:border-[#3B82F6] dark:hover:border-[#3B82F6]"
                          }
                        >
                          {pageNum + 1}
                        </PaginationLink>
                      </PaginationItem>
                    ))}

                    <PaginationItem>
                      <PaginationNext
                        onClick={() => setPage((prev) => Math.min(prev + 1, totalPages - 1))}
                        className={cn(
                          "border border-[#E5E7EB] dark:border-[#374151]",
                          page === totalPages - 1
                            ? "pointer-events-none opacity-50"
                            : "hover:border-[#3B82F6] dark:hover:border-[#3B82F6] text-[#4B5563] dark:text-[#D1D5DB]"
                        )}
                      />
                    </PaginationItem>
                  </PaginationContent>
                </Pagination>
              </div>
            )}

            <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mt-6">
              <div className="flex items-center gap-2">
                <span className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">Show</span>
                <Select value={size.toString()} onValueChange={(value) => setSize(Number(value))}>
                  <SelectTrigger className="w-[80px] h-9 border-[#E5E7EB] dark:border-[#374151]">
                    <SelectValue placeholder="10" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="5">5</SelectItem>
                    <SelectItem value="10">10</SelectItem>
                    <SelectItem value="20">20</SelectItem>
                    <SelectItem value="50">50</SelectItem>
                  </SelectContent>
                </Select>
                <span className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">entries</span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
} 