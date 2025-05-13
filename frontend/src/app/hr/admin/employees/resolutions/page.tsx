'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import {
  MessageSquare,
  Search,
  RefreshCw,
  AlertCircle,
  CheckCircle,
  Clock,
  Shield,
} from "lucide-react";
import { toast } from "sonner";
import { authService } from "@/lib/auth";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Toaster } from "@/components/ui/sonner";
import { format } from "date-fns";
import { formatInTimeZone } from 'date-fns-tz';

interface FeedbackComplaint {
  feedbackId: string;
  employeeId: string;
  employeeName: string;
  category: string;
  subject: string;
  description: string;
  submittedAt: string;
  status: string;
  resolutionNotes?: string;
  resolvedAt?: string;
  resolverName?: string;
}

export default function ResolutionsPage() {
  const router = useRouter();
  const [feedback, setFeedback] = useState<FeedbackComplaint[]>([]);
  const [filteredFeedback, setFilteredFeedback] = useState<FeedbackComplaint[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedFeedback, setSelectedFeedback] = useState<FeedbackComplaint | null>(null);
  const [resolutionNotes, setResolutionNotes] = useState('');
  const [status, setStatus] = useState('');
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    fetchFeedback();
  }, [currentPage, statusFilter]);

  useEffect(() => {
    filterFeedback();
  }, [searchTerm, statusFilter, feedback]);

  const fetchFeedback = async () => {
    try {
      setLoading(true);
      const token = authService.getToken();
      if (!token) {
        router.push('/');
        return;
      }

      // Use different endpoints based on status filter
      const endpoint = statusFilter === 'all' 
        ? `${process.env.NEXT_PUBLIC_API_URL}/hr/feedback/all`
        : `${process.env.NEXT_PUBLIC_API_URL}/hr/feedback`;

      const response = await fetch(
        `${endpoint}?${statusFilter !== 'all' ? `status=${statusFilter}&` : ''}page=${currentPage - 1}&size=${itemsPerPage}&sortBy=submittedAt&direction=desc`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        }
      );

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        throw new Error(errorData?.message || 'Failed to fetch feedback');
      }

      const data = await response.json();
      if (!data || !Array.isArray(data.content)) {
        throw new Error('Invalid response format');
      }

      setFeedback(data.content);
      setFilteredFeedback(data.content);
      setTotalPages(Math.ceil(data.totalElements / itemsPerPage));
    } catch (error) {
      console.error('Error fetching feedback:', error);
      toast.error(error instanceof Error ? error.message : 'Failed to load feedback');
    } finally {
      setLoading(false);
    }
  };

  const filterFeedback = () => {
    let filtered = [...feedback];

    if (searchTerm) {
      const searchLower = searchTerm.toLowerCase();
      filtered = filtered.filter(
        (item) =>
          item.employeeName.toLowerCase().includes(searchLower) ||
          item.subject.toLowerCase().includes(searchLower) ||
          item.feedbackId.toLowerCase().includes(searchLower)
      );
    }

    if (statusFilter !== 'all') {
      filtered = filtered.filter((item) => item.status === statusFilter);
    }

    setFilteredFeedback(filtered);
    setTotalPages(Math.ceil(filtered.length / itemsPerPage));
    setCurrentPage(1);
  };

  const getPaginatedFeedback = () => {
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    return filteredFeedback.slice(startIndex, endIndex);
  };

  const handleUpdateStatus = async () => {
    if (!selectedFeedback) return;

    try {
      const token = authService.getToken();
      if (!token) {
        router.push('/');
        return;
      }

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/hr/feedback/${selectedFeedback.feedbackId}/status`,
        {
          method: 'PATCH',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            status,
            resolutionNotes,
          }),
        }
      );

      if (!response.ok) throw new Error('Failed to update status');

      toast.success('Status updated successfully');
      setIsDialogOpen(false);
      fetchFeedback();
    } catch (error) {
      toast.error('Failed to update status');
      console.error(error);
    }
  };

  const openUpdateDialog = (feedback: FeedbackComplaint) => {
    setSelectedFeedback(feedback);
    setStatus(feedback.status);
    setResolutionNotes(feedback.resolutionNotes || '');
    setIsDialogOpen(true);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
      <Toaster
        position="top-right"
        richColors
        className="mt-24"
        style={{
          top: "6rem",
          right: "1rem",
        }}
      />
      <div className="w-full max-w-6xl mx-auto space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white flex items-center gap-2">
              <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-1 shadow-md">
                <MessageSquare className="h-5 w-5 text-white" />
              </div>
              Feedback & Complaints
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">Manage and resolve employee feedback and complaints</p>
          </div>
          <Button
            onClick={fetchFeedback}
            className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-md hover:shadow-lg"
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh List
          </Button>
        </div>

        <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-xl overflow-hidden bg-white dark:bg-[#1F2937]">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
          <CardHeader className="bg-[#F9FAFB] dark:bg-[#111827] border-b border-[#E5E7EB] dark:border-[#374151]">
            <div className="flex flex-col md:flex-row justify-between md:items-center gap-4">
              <div>
                <CardTitle className="text-xl text-[#1F2937] dark:text-white flex items-center gap-2">
                  <MessageSquare className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
                  Feedback Directory
                </CardTitle>
                <CardDescription className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
                  View and manage all feedback and complaints
                </CardDescription>
              </div>
              <div className="flex items-center gap-2">
                <Badge
                  variant="outline"
                  className="bg-[#F0FDFA] text-[#14B8A6] border-[#99F6E4] dark:bg-[#134E4A]/30 dark:text-[#14B8A6] dark:border-[#134E4A] px-3 py-1.5"
                >
                  {filteredFeedback.length} items found
                </Badge>
              </div>
            </div>
          </CardHeader>

          <CardContent className="p-6">
            <div className="flex flex-col md:flex-row gap-4 mb-6">
              <div className="flex-1">
                <Input
                  placeholder="Search by employee name, subject, or ID..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full"
                />
              </div>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className="w-[180px]">
                  <SelectValue placeholder="Filter by status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value="Open">Open</SelectItem>
                  <SelectItem value="In Review">In Review</SelectItem>
                  <SelectItem value="Resolved">Resolved</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {loading ? (
              <div className="space-y-4">
                {Array.from({ length: 5 }).map((_, index) => (
                  <div key={index} className="flex items-center space-x-4">
                    <Skeleton className="h-12 w-full rounded-md" />
                  </div>
                ))}
              </div>
            ) : filteredFeedback.length === 0 ? (
              <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
                <div className="relative w-16 h-16 mx-auto mb-4">
                  <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] opacity-20 animate-pulse"></div>
                  <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                    <AlertCircle className="h-8 w-8 text-[#6B7280] dark:text-[#9CA3AF]" />
                  </div>
                </div>
                <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">No feedback found</h3>
                <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto mb-6">
                  We couldn't find any feedback matching your current filters. Try adjusting your search criteria.
                </p>
                <Button
                  className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white shadow-md"
                  onClick={() => {
                    setSearchTerm('');
                    setStatusFilter('all');
                  }}
                >
                  <RefreshCw className="h-4 w-4 mr-2" />
                  Reset Filters
                </Button>
              </div>
            ) : (
              <>
                <div className="rounded-lg border border-[#E5E7EB] dark:border-[#374151] overflow-hidden">
                  <Table>
                    <TableHeader className="bg-[#F9FAFB] dark:bg-[#111827]">
                      <TableRow className="hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151]">
                        <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">ID</TableHead>
                        <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Employee</TableHead>
                        <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Category</TableHead>
                        <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Subject</TableHead>
                        <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Submitted</TableHead>
                        <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Status</TableHead>
                        {statusFilter !== "all" && (
                          <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Resolution Notes</TableHead>
                        )}
                        <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          {filteredFeedback.some(item => item.status !== "Resolved") && "Actions"}
                        </TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {getPaginatedFeedback().map((item, index) => (
                        <TableRow
                          key={item.feedbackId}
                          className={cn(
                            "hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151] group transition-colors",
                            index % 2 === 0 ? "bg-[#F9FAFB] dark:bg-[#111827]/50" : ""
                          )}
                        >
                          <TableCell className="font-medium text-[#1F2937] dark:text-white">
                            <div className="flex items-center gap-2">
                              <div className="h-6 w-1 rounded-full bg-gradient-to-b from-[#3B82F6] to-[#14B8A6] transition-all duration-300 group-hover:h-full"></div>
                              {item.feedbackId}
                            </div>
                          </TableCell>
                          <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                            {item.employeeName}
                          </TableCell>
                          <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                            <Badge
                              variant="outline"
                              className={cn(
                                "px-2 py-1",
                                item.category === "Feedback"
                                  ? "bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-900/30 dark:text-blue-300 dark:border-blue-800"
                                  : item.category === "Complaint"
                                  ? "bg-red-50 text-red-700 border-red-200 dark:bg-red-900/30 dark:text-red-300 dark:border-red-800"
                                  : "bg-yellow-50 text-yellow-700 border-yellow-200 dark:bg-yellow-900/30 dark:text-yellow-300 dark:border-yellow-800"
                              )}
                            >
                              {item.category}
                            </Badge>
                          </TableCell>
                          <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                            {item.subject}
                          </TableCell>
                          <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                            {formatInTimeZone(
                              new Date(item.submittedAt),
                              'Asia/Manila',
                              'MMM d, yyyy hh:mm a'
                            )}
                          </TableCell>
                          <TableCell className="text-[#4B5563] dark:text-[#D1D5DB]">
                            <Badge
                              variant="outline"
                              className={cn(
                                "px-2 py-1",
                                item.status === "Open"
                                  ? "bg-yellow-50 text-yellow-700 border-yellow-200 dark:bg-yellow-900/30 dark:text-yellow-300 dark:border-yellow-800"
                                  : item.status === "In Review"
                                  ? "bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-900/30 dark:text-blue-300 dark:border-blue-800"
                                  : item.status === "Resolved"
                                  ? "bg-green-50 text-green-700 border-green-200 dark:bg-green-900/30 dark:text-green-300 dark:border-green-800"
                                  : "bg-gray-50 text-gray-700 border-gray-200 dark:bg-gray-900/30 dark:text-gray-300 dark:border-gray-800"
                              )}
                            >
                              {item.status}
                            </Badge>
                          </TableCell>
                          {statusFilter !== "all" && (
                            <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] max-w-[300px]">
                              {item.status === "Resolved" ? (
                                <p className="whitespace-pre-wrap break-words text-sm">
                                  {item.resolutionNotes || "No resolution notes provided"}
                                </p>
                              ) : (
                                <span className="text-[#9CA3AF]">No resolution notes</span>
                              )}
                            </TableCell>
                          )}
                          <TableCell>
                            {statusFilter === "all" ? (
                              <Button
                                variant="outline"
                                onClick={() => openUpdateDialog(item)}
                                className="border-[#3B82F6] text-[#3B82F6] hover:bg-[#3B82F6] hover:text-white"
                              >
                                View Details
                              </Button>
                            ) : item.status !== "Resolved" && (
                              <Button
                                variant="outline"
                                onClick={() => openUpdateDialog(item)}
                                className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white border-none"
                              >
                                {item.status === "In Review" ? "Add Resolution" : "Update Status"}
                              </Button>
                            )}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>

                <div className="mt-4">
                  <Pagination>
                    <PaginationContent>
                      <PaginationItem>
                        <PaginationPrevious
                          onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
                          className={cn(
                            "border border-[#E5E7EB] dark:border-[#374151]",
                            currentPage === 1
                              ? "pointer-events-none opacity-50"
                              : "hover:border-[#3B82F6] dark:hover:border-[#3B82F6] text-[#4B5563] dark:text-[#D1D5DB]"
                          )}
                        />
                      </PaginationItem>
                      {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
                        <PaginationItem key={page}>
                          <PaginationLink
                            onClick={() => setCurrentPage(page)}
                            className={cn(
                              "border border-[#E5E7EB] dark:border-[#374151]",
                              currentPage === page
                                ? "bg-[#3B82F6] text-white border-[#3B82F6]"
                                : "hover:border-[#3B82F6] dark:hover:border-[#3B82F6] text-[#4B5563] dark:text-[#D1D5DB]"
                            )}
                          >
                            {page}
                          </PaginationLink>
                        </PaginationItem>
                      ))}
                      <PaginationItem>
                        <PaginationNext
                          onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))}
                          className={cn(
                            "border border-[#E5E7EB] dark:border-[#374151]",
                            currentPage === totalPages
                              ? "pointer-events-none opacity-50"
                              : "hover:border-[#3B82F6] dark:hover:border-[#3B82F6] text-[#4B5563] dark:text-[#D1D5DB]"
                          )}
                        />
                      </PaginationItem>
                    </PaginationContent>
                  </Pagination>
                </div>
              </>
            )}
          </CardContent>
        </Card>
      </div>

      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className={cn(
          "sm:max-w-[600px]",
          selectedFeedback?.status === "Resolved" && "sm:max-w-[700px]"
        )}>
          <DialogHeader>
            <DialogTitle>
              {selectedFeedback?.status === "Resolved" ? "Feedback Details" : "Update Feedback Status"}
            </DialogTitle>
            <DialogDescription>
              {selectedFeedback?.status === "Resolved" 
                ? "View feedback details and resolution information"
                : "Update the status and resolution for this feedback"
              }
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <h3 className="font-medium mb-1">Employee</h3>
              <p className="text-sm text-gray-600 dark:text-gray-300">{selectedFeedback?.employeeName}</p>
            </div>
            <div>
              <h3 className="font-medium mb-1">Category</h3>
              <p className="text-sm text-gray-600 dark:text-gray-300">{selectedFeedback?.category}</p>
            </div>
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
                {selectedFeedback && formatInTimeZone(
                  new Date(selectedFeedback.submittedAt),
                  'Asia/Manila',
                  'MMMM d, yyyy hh:mm a'
                )}
              </p>
            </div>

            {selectedFeedback?.status === "Resolved" && (
              <>
                <div>
                  <h3 className="font-medium mb-1">Resolved By</h3>
                  <p className="text-sm text-gray-600 dark:text-gray-300">
                    {selectedFeedback.resolverName || "Not specified"}
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

            {(selectedFeedback?.status === "Open" || selectedFeedback?.status === "In Review") && (
              <div className="space-y-2">
                <Label htmlFor="status">Status</Label>
                <Select value={status} onValueChange={setStatus}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select status" />
                  </SelectTrigger>
                  <SelectContent>
                    {selectedFeedback?.status === "Open" && (
                      <>
                        <SelectItem value="Open">Open</SelectItem>
                        <SelectItem value="In Review">In Review</SelectItem>
                      </>
                    )}
                    {selectedFeedback?.status === "In Review" && (
                      <>
                        <SelectItem value="Open">Open</SelectItem>
                        <SelectItem value="In Review">In Review</SelectItem>
                      </>
                    )}
                    <SelectItem value="Resolved">Resolved</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            )}

            {status === "In Review" && (
              <div className="space-y-2">
                <Label htmlFor="resolutionNotes">Resolution Notes</Label>
                <Textarea
                  id="resolutionNotes"
                  value={resolutionNotes}
                  onChange={(e) => setResolutionNotes(e.target.value)}
                  placeholder="Enter resolution notes..."
                  className="min-h-[100px]"
                />
              </div>
            )}

            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => setIsDialogOpen(false)}>
                Cancel
              </Button>
              <Button
                onClick={handleUpdateStatus}
                className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white"
              >
                Update Status
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
