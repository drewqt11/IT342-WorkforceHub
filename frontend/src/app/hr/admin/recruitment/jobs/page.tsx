"use client";

import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, AlertCircle, RefreshCw } from "lucide-react";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { authService } from "@/lib/auth";
import { cn } from "@/lib/utils";
import { Skeleton } from "@/components/ui/skeleton";
import {
  HoverCard,
  HoverCardContent,
  HoverCardTrigger,
} from "@/components/ui/hover-card";

interface JobListing {
  jobId: string;
  title: string;
  departmentId: string;
  departmentName: string;
  jobDescription: string;
  qualifications: string;
  employmentType: string;
  jobType: string;
  datePosted: string;
  applicationDeadline: string;
  isActive: boolean;
  totalApplications: number;
}

interface Department {
  departmentId: string;
  departmentName: string;
}

const EMPLOYMENT_TYPES = ["FULL_TIME", "PART_TIME", "CONTRACT"];
const JOB_TYPES = ["INTERNAL", "EXTERNAL", "BOTH"];

export default function JobListingsPage() {
  const [jobListings, setJobListings] = useState<JobListing[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [selectedJob, setSelectedJob] = useState<JobListing | null>(null);
  const [formData, setFormData] = useState({
    title: "",
    departmentId: "",
    jobDescription: "",
    qualifications: "",
    employmentType: "",
    jobType: "",
    applicationDeadline: "",
  });

  useEffect(() => {
    fetchJobListings();
    fetchDepartments();
  }, []);

  const fetchJobListings = async () => {
    try {
      const token = authService.getToken();
      if (!token) {
        throw new Error("No authentication token found");
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/jobs`, {
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });
      
      if (!response.ok) throw new Error("Failed to fetch job listings");
      const data = await response.json();
      setJobListings(data.content || data);
    } catch (error) {
      toast.error("Failed to load job listings");
    } finally {
      setIsLoading(false);
    }
  };

  const fetchDepartments = async () => {
    try {
      const token = authService.getToken();
      if (!token) {
        throw new Error("No authentication token found");
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/departments`, {
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });
      
      if (!response.ok) throw new Error("Failed to fetch departments");
      const data = await response.json();
      setDepartments(data);
    } catch (error) {
      toast.error("Failed to load departments");
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const token = authService.getToken();
      if (!token) {
        throw new Error("No authentication token found");
      }

      const url = selectedJob
        ? `${process.env.NEXT_PUBLIC_API_URL}/jobs/${selectedJob.jobId}`
        : `${process.env.NEXT_PUBLIC_API_URL}/jobs`;
      
      const method = selectedJob ? "PUT" : "POST";
      
      // Create FormData object
      const formDataToSend = new FormData();
      formDataToSend.append('title', formData.title);
      formDataToSend.append('departmentId', formData.departmentId);
      formDataToSend.append('jobDescription', formData.jobDescription);
      formDataToSend.append('qualifications', formData.qualifications);
      formDataToSend.append('employmentType', formData.employmentType);
      formDataToSend.append('jobType', formData.jobType);
      formDataToSend.append('applicationDeadline', new Date(formData.applicationDeadline).toISOString().split('T')[0]);

      const response = await fetch(url, {
        method,
        headers: {
          "Authorization": `Bearer ${token}`,
        },
        body: formDataToSend,
      });

      if (!response.ok) throw new Error("Failed to save job listing");
      
      toast.success(
        selectedJob
          ? "Job listing updated successfully"
          : "Job listing created successfully"
      );
      
      setIsDialogOpen(false);
      fetchJobListings();
      resetForm();
    } catch (error) {
      toast.error("Failed to save job listing");
    }
  };

  const handleDelete = async () => {
    if (!selectedJob) return;
    
    try {
      const token = authService.getToken();
      if (!token) {
        throw new Error("No authentication token found");
      }

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/jobs/${selectedJob.jobId}/deactivate`,
        {
          method: "PUT",
          headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      if (!response.ok) throw new Error("Failed to deactivate job listing");
      
      toast.success("Job listing deactivated successfully");
      setIsDeleteDialogOpen(false);
      fetchJobListings();
    } catch (error) {
      toast.error("Failed to deactivate job listing");
    }
  };

  const resetForm = () => {
    setFormData({
      title: "",
      departmentId: "",
      jobDescription: "",
      qualifications: "",
      employmentType: "",
      jobType: "",
      applicationDeadline: "",
    });
    setSelectedJob(null);
  };

  const openEditDialog = (job: JobListing) => {
    setSelectedJob(job);
    setFormData({
      title: job.title,
      departmentId: job.departmentId,
      jobDescription: job.jobDescription || "",
      qualifications: job.qualifications || "",
      employmentType: job.employmentType,
      jobType: job.jobType,
      applicationDeadline: new Date(job.applicationDeadline).toISOString().split('T')[0],
    });
    setIsDialogOpen(true);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
      <div className="w-full max-w-6xl mx-auto space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white flex items-center gap-2">
              <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-1 shadow-md">
                <Plus className="h-5 w-5 text-white" />
              </div>
              Job Listings Management
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">Create, update, and manage job listings</p>
          </div>
          
          <div className="flex gap-2">
            <Button
              onClick={fetchJobListings}
              className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-md hover:shadow-lg"
            >
              <RefreshCw className="h-4 w-4 mr-2" />
              Refresh
            </Button>
            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
              <DialogTrigger asChild>
                <Button 
                  onClick={() => resetForm()}
                  className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-md hover:shadow-lg"
                >
                  <Plus className="h-4 w-4 mr-2" /> Add New Job
                </Button>
              </DialogTrigger>
              
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>
                    {selectedJob ? "Edit Job Listing" : "Create Job Listing"}
                  </DialogTitle>
                  <DialogDescription>
                    {selectedJob
                      ? "Update the details of the job listing."
                      : "Fill in the details to create a new job listing."}
                  </DialogDescription>
                </DialogHeader>
                <form onSubmit={handleSubmit}>
                  <div className="grid gap-4 py-4">
                    <div className="grid gap-2">
                      <Label htmlFor="title">Job Title</Label>
                      <Input
                        id="title"
                        value={formData.title}
                        onChange={(e) =>
                          setFormData({ ...formData, title: e.target.value })
                        }
                        required
                      />
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="departmentId">Department</Label>
                      <Select
                        value={formData.departmentId}
                        onValueChange={(value) =>
                          setFormData({ ...formData, departmentId: value })
                        }
                        required
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="Select a department" />
                        </SelectTrigger>
                        <SelectContent>
                          {departments.map((dept) => (
                            <SelectItem key={dept.departmentId} value={dept.departmentId}>
                              {dept.departmentName}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="jobDescription">Job Description</Label>
                      <Textarea
                        id="jobDescription"
                        value={formData.jobDescription}
                        onChange={(e) =>
                          setFormData({ ...formData, jobDescription: e.target.value })
                        }
                        required
                      />
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="qualifications">Qualifications</Label>
                      <Textarea
                        id="qualifications"
                        value={formData.qualifications}
                        onChange={(e) =>
                          setFormData({ ...formData, qualifications: e.target.value })
                        }
                        required
                      />
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="employmentType">Employment Type</Label>
                      <Select
                        value={formData.employmentType}
                        onValueChange={(value) =>
                          setFormData({ ...formData, employmentType: value })
                        }
                        required
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="Select employment type" />
                        </SelectTrigger>
                        <SelectContent>
                          {EMPLOYMENT_TYPES.map((type) => (
                            <SelectItem key={type} value={type}>
                              {type.replace("_", " ")}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="jobType">Job Type</Label>
                      <Select
                        value={formData.jobType}
                        onValueChange={(value) =>
                          setFormData({ ...formData, jobType: value })
                        }
                        required
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="Select job type" />
                        </SelectTrigger>
                        <SelectContent>
                          {JOB_TYPES.map((type) => (
                            <SelectItem key={type} value={type}>
                              {type.replace("_", " ")}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="applicationDeadline">Application Deadline</Label>
                      <Input
                        id="applicationDeadline"
                        type="date"
                        value={formData.applicationDeadline}
                        onChange={(e) =>
                          setFormData({ ...formData, applicationDeadline: e.target.value })
                        }
                        required
                      />
                    </div>
                  </div>
                  <DialogFooter>
                    <Button className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-md hover:shadow-lg" type="submit">
                      {selectedJob ? "Update Job" : "Create Job"}
                    </Button>
                  </DialogFooter>
                </form>
              </DialogContent>
            </Dialog>
          </div>
        </div>

        <Card className="relative overflow-hidden">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
          <CardHeader className="bg-[#F9FAFB] dark:bg-[#111827] border-b border-[#E5E7EB] dark:border-[#374151]">
            <div className="flex flex-col md:flex-row justify-between md:items-center gap-4">
              <div>
                <CardTitle className="text-xl text-[#1F2937] dark:text-white flex items-center gap-2">
                  <Plus className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
                  Job Listings
                </CardTitle>
                <CardDescription className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
                  View and manage job listings
                </CardDescription>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">{jobListings.length} listings</span>
              </div>
            </div>
          </CardHeader>
          <CardContent className="p-6">
            {isLoading ? (
              <div className="space-y-4">
                {Array.from({ length: 5 }).map((_, index) => (
                  <div key={index} className="flex items-center space-x-4">
                    <Skeleton className="h-12 w-full rounded-md" />
                  </div>
                ))}
              </div>
            ) : jobListings.length === 0 ? (
              <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
                <div className="relative w-16 h-16 mx-auto mb-4">
                  <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] opacity-20 animate-pulse"></div>
                  <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                    <Plus className="h-8 w-8 text-[#6B7280] dark:text-[#9CA3AF]" />
                  </div>
                </div>
                <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">No job listings found</h3>
                <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto mb-6">
                  There are no job listings in the system yet. Create your first listing to get started.
                </p>
                <Button
                  className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white shadow-md"
                  onClick={() => setIsDialogOpen(true)}
                >
                  <Plus className="h-4 w-4 mr-2" />
                  Add Job Listing
                </Button>
              </div>
            ) : (
              <div className="rounded-lg border border-[#E5E7EB] dark:border-[#374151] overflow-hidden">
                <Table>
                  <TableHeader className="bg-[#F9FAFB] dark:bg-[#111827]">
                    <TableRow className="hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151]">
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium w-12 text-center">No.</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Job Title</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Department</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Employment Type</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Job Type</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium text-center">Applications</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Deadline</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Status</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium pl-16">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {jobListings.map((job, index) => (
                      <TableRow
                        key={job.jobId}
                        className={cn(
                          "hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151] group transition-colors",
                          index % 2 === 0 ? "bg-[#F9FAFB] dark:bg-[#111827]/50" : "",
                        )}
                      >
                        <TableCell className="text-center text-[#4B5563] dark:text-[#D1D5DB] font-medium">{index + 1}</TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          <div className="flex items-center gap-2">
                            {job.title}
                            <HoverCard>
                              <HoverCardTrigger asChild>
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  className="h-8 w-8 p-0 hover:bg-transparent ml-3"
                                >
                                  <AlertCircle className="h-4 w-4 text-[#6B7280] hover:text-[#3B82F6]" />
                                </Button>
                              </HoverCardTrigger>
                              <HoverCardContent className="w-80">
                                <div className="space-y-4">
                                  <div>
                                    <h4 className="text-sm font-semibold text-[#1F2937] dark:text-white mb-1">Description</h4>
                                    <p className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                                      {job.jobDescription || "No description provided"}
                                    </p>
                                  </div>
                                  <div>
                                    <h4 className="text-sm font-semibold text-[#1F2937] dark:text-white mb-1">Qualifications</h4>
                                    <p className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                                      {job.qualifications || "No qualifications specified"}
                                    </p>
                                  </div>
                                </div>
                              </HoverCardContent>
                            </HoverCard>
                          </div>
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          {job.departmentName}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          {job.employmentType.replace("_", " ")}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          {job.jobType.replace("_", " ")}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium text-center">
                          {job.totalApplications}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          {new Date(job.applicationDeadline).toLocaleDateString('en-US', {
                            year: 'numeric',
                            month: 'short',
                            day: 'numeric',
                          })}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          <span className="px-2 py-1 rounded-full text-xs bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400">
                            Active
                          </span>
                        </TableCell>
                        <TableCell>
                          <div className="flex justify-end gap-4">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => openEditDialog(job)}
                              className="border-[#BFDBFE] text-[#3B82F6] hover:bg-[#EFF6FF] dark:border-[#1E3A8A] dark:text-[#3B82F6] dark:hover:bg-[#1E3A8A]/30"
                            >
                              <div className="flex items-center gap-1">
                                <Pencil className="h-3.5 w-3.5" />
                                <span>Edit</span>
                              </div>
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => {
                                setSelectedJob(job);
                                setIsDeleteDialogOpen(true);
                              }}
                              className="border-[#FED7AA] text-[#F59E0B] hover:bg-[#FEF3C7] dark:border-[#78350F] dark:text-[#F59E0B] dark:hover:bg-[#78350F]/30"
                            >
                              <div className="flex items-center gap-1">
                                <Trash2 className="h-3.5 w-3.5" />
                                <span>Delete</span>
                              </div>
                            </Button>
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

      <AlertDialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>
              You can only deactivate the job listing if it is not currently being used by any applications. 
              This will deactivate the job listing. Candidates will no longer be
              able to apply for this position.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction className="bg-red-500 hover:bg-red-600" onClick={handleDelete}>Deactivate</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
} 