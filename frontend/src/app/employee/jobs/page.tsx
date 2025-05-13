"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import {
  Briefcase,
  Calendar,
  Clock,
  MapPin,
  Building2,
  Filter,
  Search,
  Grid3X3,
  ChevronLeft,
  ChevronRight,
  Share2,
  CheckCircle2,
  AlertCircle,
} from "lucide-react"
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
import { toast } from "sonner"
import { Toaster } from "sonner"
import { useUser } from "@/contexts/UserContext"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { ScrollArea } from "@/components/ui/scroll-area"
import Image from "next/image"
import React from "react"

interface JobListing {
  jobId: string
  title: string
  description: string
  departmentId: string
  departmentName: string
  jobDescription: string
  qualifications: string
  employmentType: string
  jobType: string
  applicationDeadline: string
  isActive: boolean
  datePosted: string
}

interface ApplicationStatus {
  [key: string]: boolean
}

// Helper function to get image based on job title or type
const getImageUrl = (job: JobListing) => {
  const title = job.title.toLowerCase();
  const type = job.jobType.toLowerCase();

  if (title.includes("software") || title.includes("developer") || title.includes("engineer")) return "/tech-job.png";
  if (title.includes("manager") || title.includes("lead")) return "/management-job.png";
  if (title.includes("design") || title.includes("creative")) return "/design-job.png";
  if (title.includes("marketing") || title.includes("sales")) return "/marketing-job.png";
  if (title.includes("hr") || title.includes("human resources")) return "/hr-job.png";
  if (type.includes("internal")) return "/internal-job.png";

  return "/general-job.png";
};

// Helper function to get icon based on employment type
const getEmploymentTypeIcon = (type: string) => {
  const lowerType = type.toLowerCase()

  if (lowerType.includes("full")) return Briefcase
  if (lowerType.includes("part")) return Clock
  if (lowerType.includes("contract")) return Calendar

  return Briefcase
}

// Helper function to safely format dates
const formatDate = (dateString: string | undefined | null) => {
  if (!dateString) return "N/A";
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return "Invalid Date";
    return format(date, "MMM d, yyyy");
  } catch (error) {
    console.error("Error formatting date:", error);
    return "Invalid Date";
  }
};

// Helper function to get background color based on job type
const getHeaderColor = (job: JobListing) => {
  const title = job.title.toLowerCase();
  const type = job.jobType.toLowerCase();

  if (title.includes("software") || title.includes("developer") || title.includes("engineer")) return "bg-blue-500";
  if (title.includes("manager") || title.includes("lead")) return "bg-purple-500";
  if (title.includes("design") || title.includes("creative")) return "bg-pink-500";
  if (title.includes("marketing") || title.includes("sales")) return "bg-green-500";
  if (title.includes("hr") || title.includes("human resources")) return "bg-orange-500";
  if (type.includes("internal")) return "bg-indigo-500";

  return "bg-gray-500";
};

export default function JobsPage() {
  const { user } = useUser()
  const [jobs, setJobs] = useState<JobListing[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedJob, setSelectedJob] = useState<JobListing | null>(null)
  const [isApplyDialogOpen, setIsApplyDialogOpen] = useState(false)
  const [isApplying, setIsApplying] = useState(false)
  const [appliedJobs, setAppliedJobs] = useState<ApplicationStatus>({})
  const [viewMode, setViewMode] = useState<"grid" | "list">("grid")
  const [searchTerm, setSearchTerm] = useState("")
  const [activeTab, setActiveTab] = useState("all")
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false)
  const [activeSharePopup, setActiveSharePopup] = useState<string | null>(null)

  useEffect(() => {
    const fetchJobs = async () => {
      try {
        setLoading(true);
        const token = authService.getToken();

        if (!token) {
          throw new Error("No authentication token found");
        }

        // Fetch all jobs
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/jobs`, {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });

        if (!response.ok) {
          throw new Error("Failed to fetch jobs");
        }

        const jobsData = await response.json();
        setJobs(jobsData.content);

        // Get applicant profile and check applications
        const applicantResponse = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/applicants/me`, {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });

        if (applicantResponse.ok && applicantResponse.status !== 204) {
          const applicantData = await applicantResponse.json();
          
          // Fetch applications for this applicant
          const applicationsResponse = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/applications/applicant/${applicantData.applicantId}`,
            {
              headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json",
              },
            }
          );

          if (applicationsResponse.ok) {
            const applications = await applicationsResponse.json();
            const applicationStatus: ApplicationStatus = {};
            applications.content.forEach((application: any) => {
              applicationStatus[application.jobId] = true;
            });
            setAppliedJobs(applicationStatus);
          }
        }
      } catch (err) {
        console.error("Error fetching data:", err);
        setError(err instanceof Error ? err.message : "Failed to fetch data");
      } finally {
        setLoading(false);
      }
    };

    fetchJobs();
  }, []);

  const handleApplyClick = (job: JobListing) => {
    setSelectedJob(job)
    setIsApplyDialogOpen(true)
  }

  const handleDetailsClick = (job: JobListing) => {
    setSelectedJob(job)
    setDetailsDialogOpen(true)
  }

  const handleApply = async () => {
    if (!selectedJob || !user) return;

    try {
      setIsApplying(true);
      const token = authService.getToken();

      if (!token) {
        throw new Error("No authentication token found");
      }

      const profile = await authService.getEmployeeProfile();

      // First check if user already has an applicant profile
      const existingApplicantResponse = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/applicants/me`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      let applicantId;

      if (existingApplicantResponse.ok && existingApplicantResponse.status !== 204) {
        // Use existing applicant profile
        const existingApplicant = await existingApplicantResponse.json();
        applicantId = existingApplicant.applicantId;
      } else if (existingApplicantResponse.status === 204) {
        // No existing profile, create new one
        const applicantResponse = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/applicants/internal`, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            userId: profile.userId,
            fullName: profile.fullName,
            email: profile.emailAddress,
            phoneNumber: profile.phoneNumber,
            isInternal: true
          }),
        });

        if (!applicantResponse.ok) {
          const errorData = await applicantResponse.json().catch(() => ({ message: "Failed to create applicant profile" }));
          throw new Error(errorData.message || "Failed to create applicant profile");
        }

        const applicantData = await applicantResponse.json();
        applicantId = applicantData.applicantId;
      } else {
        throw new Error("Failed to check applicant profile");
      }

      // Apply for the job
      const applicationResponse = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/applications?applicantId=${applicantId}&jobId=${selectedJob.jobId}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      if (!applicationResponse.ok) {
        const errorData = await applicationResponse.json().catch(() => ({ message: "Failed to apply for job" }));
        throw new Error(errorData.message || "Failed to apply for job");
      }

      // Update applied jobs state
      setAppliedJobs(prev => ({ ...prev, [selectedJob.jobId]: true }));
      
      toast.success("Application submitted successfully!", {
        description: "Your application has been submitted and is pending review.",
        duration: 5000,
      });
      setIsApplyDialogOpen(false);
    } catch (err) {
      console.error("Error applying for job:", err);
      toast.error("Failed to apply for job", {
        description: err instanceof Error ? err.message : "An unexpected error occurred",
        duration: 5000,
      });
    } finally {
      setIsApplying(false);
    }
  };

  const handleShareJob = (job: JobListing, event: React.MouseEvent<HTMLButtonElement>) => {
    event.stopPropagation();
    const shareUrl = `${window.location.origin}/jobs/${job.jobId}`;
    
    if (activeSharePopup === job.jobId) {
      setActiveSharePopup(null);
      return;
    }

    setActiveSharePopup(job.jobId);

    // Close popup when clicking outside
    const handleClickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (!target.closest('.share-popup') && !target.closest('.share-button')) {
        setActiveSharePopup(null);
      }
    };

    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  };

  const handleSocialShare = (type: 'facebook' | 'linkedin' | 'copy', job: JobListing) => {
    const shareUrl = `${window.location.origin}/jobs/${job.jobId}`;
    
    switch (type) {
      case 'facebook':
        window.open(`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(shareUrl)}`, '_blank');
        break;
      case 'linkedin':
        window.open(`https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(shareUrl)}`, '_blank');
        break;
      case 'copy':
        navigator.clipboard.writeText(shareUrl);
        toast.success('Link copied to clipboard!');
        break;
    }
    
    setActiveSharePopup(null);
  };

  const filterJobs = (jobs: JobListing[]) => {
    return jobs.filter(job => {
      const matchesSearch = job.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        job.description.toLowerCase().includes(searchTerm.toLowerCase())
      
      const matchesTab = activeTab === "all" ||
        (activeTab === "internal" && job.jobType.toLowerCase() === "internal") ||
        (activeTab === "external" && job.jobType.toLowerCase() === "external") ||
        (activeTab === "applied" && appliedJobs[job.jobId])

      return matchesSearch && matchesTab
    })
  }

  if (loading) {
    return (
      <div className="container mx-auto p-6">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[...Array(6)].map((_, i) => (
            <Card key={i} className="overflow-hidden">
              <Skeleton className="h-48 w-full" />
              <CardHeader>
                <Skeleton className="h-6 w-3/4" />
                <Skeleton className="h-4 w-1/2" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-4 w-full mb-2" />
                <Skeleton className="h-4 w-2/3" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
            <h2 className="text-2xl font-bold mb-2">Error Loading Jobs</h2>
            <p className="text-gray-600">{error}</p>
          </div>
        </div>
      </div>
    )
  }

  const filteredJobs = filterJobs(jobs)

  return (
    <div className="container mx-auto p-6">
      <Toaster 
        position="top-right"
        className="y-10"
        expand={true}
        richColors
        closeButton
      />
      
      {/* Header Section */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
        <div>
          <h1 className="text-3xl font-bold mb-2">Job Listings</h1>
          <p className="text-gray-600">Find and apply for opportunities within the organization</p>
        </div>
        
        <div className="flex items-center gap-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <Input
              placeholder="Search jobs..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10 w-[300px]"
            />
          </div>
          
          <Button
            variant="outline"
            onClick={() => setViewMode(viewMode === "grid" ? "list" : "grid")}
          >
            <Grid3X3 className="h-4 w-4 mr-2" />
            {viewMode === "grid" ? "List View" : "Grid View"}
          </Button>
        </div>
      </div>

      {/* Filter Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="mb-6">
        <TabsList>
          <TabsTrigger value="all">All Jobs</TabsTrigger>
          <TabsTrigger value="internal">Internal</TabsTrigger>
          <TabsTrigger value="external">External</TabsTrigger>
          <TabsTrigger value="applied">Applied</TabsTrigger>
        </TabsList>
      </Tabs>

      {/* Jobs Grid */}
      <div className={cn(
        "gap-6",
        viewMode === "grid" ? "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3" : "flex flex-col"
      )}>
        {filteredJobs.map((job) => (
          <Card key={job.jobId} className="overflow-hidden p-0">
            <div className={cn("relative h-32", getHeaderColor(job))}>
              <div className="absolute inset-0 flex items-center justify-center">
                <Briefcase className="h-12 w-12 text-white" />
              </div>
              <div className="absolute top-4 right-4">
                <Badge variant="default" className="bg-green-500 text-white">
                 Active
                </Badge>
              </div>
            </div>
            
            <CardHeader className="pt-6">
              <CardTitle className="line-clamp-2">{job.title}</CardTitle>
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <Building2 className="h-4 w-4" />
                <span>{job.departmentName}</span>
              </div>
            </CardHeader>
            
            <CardContent>
              <p className="text-gray-600 line-clamp-3 mb-4">{job.description}</p>
              
              <div className="flex flex-wrap gap-2 mb-4">
                <Badge variant="outline" className="flex items-center gap-1">
                  {React.createElement(getEmploymentTypeIcon(job.employmentType), { className: "h-3 w-3" })}
                  {job.employmentType}
                </Badge>
                <Badge variant="outline" className="flex items-center gap-1">
                  <Clock className="h-3 w-3" />
                  {formatDate(job.applicationDeadline)}
                </Badge>
              </div>
            </CardContent>
            
            <CardFooter className="flex justify-between pb-6">
              <Button
                variant="outline"
                onClick={() => handleDetailsClick(job)}
                className="mb-2"
              >
                View Details
              </Button>
              
              {job.jobType.toLowerCase() === "internal" ? (
                <Button
                  onClick={() => handleApplyClick(job)}
                  disabled={appliedJobs[job.jobId]}
                  className="mb-2"
                >
                  {appliedJobs[job.jobId] ? (
                    <>
                      <CheckCircle2 className="h-4 w-4 mr-2" />
                      Applied
                    </>
                  ) : (
                    "Apply Now"
                  )}
                </Button>
              ) : (
                <div className="relative">
                  {activeSharePopup === job.jobId && (
                    <div className="share-popup absolute bottom-full right-0 mb-2 bg-white rounded-lg shadow-lg p-2 flex gap-2 border">
                      <button
                        onClick={() => handleSocialShare('facebook', job)}
                        className="p-2 hover:bg-gray-100 rounded-full transition-colors"
                        title="Share on Facebook"
                      >
                        <svg className="w-5 h-5 text-[#1877F2]" fill="currentColor" viewBox="0 0 24 24">
                          <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
                        </svg>
                      </button>
                      <button
                        onClick={() => handleSocialShare('linkedin', job)}
                        className="p-2 hover:bg-gray-100 rounded-full transition-colors"
                        title="Share on LinkedIn"
                      >
                        <svg className="w-5 h-5 text-[#0A66C2]" fill="currentColor" viewBox="0 0 24 24">
                          <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"/>
                        </svg>
                      </button>
                      <button
                        onClick={() => handleSocialShare('copy', job)}
                        className="p-2 hover:bg-gray-100 rounded-full transition-colors"
                        title="Copy Link"
                      >
                        <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2m0 0h2a2 2 0 012 2v3m2 4H10m0 0l3-3m-3 3l3 3"/>
                        </svg>
                      </button>
                    </div>
                  )}
                  <Button
                    variant="secondary"
                    onClick={(e) => handleShareJob(job, e)}
                    className="mb-2 share-button"
                  >
                    <Share2 className="h-4 w-4 mr-2" />
                    Share
                  </Button>
                </div>
              )}
            </CardFooter>
          </Card>
        ))}
      </div>

      {/* Apply Dialog */}
      <Dialog open={isApplyDialogOpen} onOpenChange={setIsApplyDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Apply for {selectedJob?.title}</DialogTitle>
            <DialogDescription>
              Are you sure you want to apply for this position? Your profile will be submitted for review.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsApplyDialogOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleApply} disabled={isApplying}>
              {isApplying ? "Applying..." : "Confirm Application"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Job Details Dialog */}
      <Dialog open={detailsDialogOpen} onOpenChange={setDetailsDialogOpen}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>{selectedJob?.title}</DialogTitle>
            <DialogDescription>
              {selectedJob?.departmentName}
            </DialogDescription>
          </DialogHeader>
          
          <ScrollArea className="max-h-[60vh]">
            <div className="space-y-6">
              <div>
                <h3 className="font-semibold mb-2">Job Description</h3>
                <p className="text-gray-600 whitespace-pre-wrap">{selectedJob?.jobDescription}</p>
              </div>
              
              <div>
                <h3 className="font-semibold mb-2">Qualifications</h3>
                <p className="text-gray-600 whitespace-pre-wrap">{selectedJob?.qualifications}</p>
              </div>
              
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <h3 className="font-semibold mb-2">Employment Type</h3>
                  <p className="text-gray-600">{selectedJob?.employmentType}</p>
                </div>
                <div>
                  <h3 className="font-semibold mb-2">Job Type</h3>
                  <p className="text-gray-600">{selectedJob?.jobType}</p>
                </div>
                <div>
                  <h3 className="font-semibold mb-2">Application Deadline</h3>
                  <p className="text-gray-600">
                    {formatDate(selectedJob?.applicationDeadline)}
                  </p>
                </div>
                <div>
                  <h3 className="font-semibold mb-2">Posted Date</h3>
                  <p className="text-gray-600">
                    {formatDate(selectedJob?.datePosted)}
                  </p>
                </div>
              </div>
            </div>
          </ScrollArea>
          
          <DialogFooter>
            <Button variant="outline" onClick={() => setDetailsDialogOpen(false)}>
              Close
            </Button>
            {selectedJob?.jobType.toLowerCase() === "internal" ? (
              <Button
                onClick={() => {
                  setDetailsDialogOpen(false)
                  handleApplyClick(selectedJob)
                }}
                disabled={appliedJobs[selectedJob.jobId]}
              >
                {appliedJobs[selectedJob.jobId] ? (
                  <>
                    <CheckCircle2 className="h-4 w-4 mr-2" />
                    Applied
                  </>
                ) : (
                  "Apply Now"
                )}
              </Button>
            ) : (
              <Button
                variant="secondary"
                onClick={() => {
                  setDetailsDialogOpen(false);
                  if (selectedJob) {
                    setActiveSharePopup(selectedJob.jobId);
                  }
                }}
              >
                <Share2 className="h-4 w-4 mr-2" />
                Share
              </Button>
            )}
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
