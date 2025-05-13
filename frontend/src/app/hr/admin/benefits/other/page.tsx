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

interface BenefitPlan {
  planId: string;
  planName: string;
  description: string;
  provider: string;
  eligibility: string;
  planType: string;
  maxCoverage: number;
  createdAt: string;
  isActive: boolean;
  enrollmentCount: number;
}

const BENEFIT_TYPES = [
  "Preventive Dental Care",
  "Life Insurance",
  "Work-Life Balance Programs",
  "Pioneer/Top Employee Incentives",
  "Employee Discounts",
  "Gym/Fitness Membership",
  "Other"
];

export default function OtherBenefitsPage() {
  const [benefits, setBenefits] = useState<BenefitPlan[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [selectedBenefit, setSelectedBenefit] = useState<BenefitPlan | null>(null);
  const [formData, setFormData] = useState({
    planName: "",
    description: "",
    provider: "",
    eligibility: "",
    planType: "",
    customPlanType: "",
    maxCoverage: "",
  });

  useEffect(() => {
    fetchBenefits();
  }, []);

  const fetchBenefits = async () => {
    try {
      const token = authService.getToken();
      if (!token) {
        throw new Error("No authentication token found");
      }

      const response = await fetch("/api/benefit-plans", {
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });
      
      if (!response.ok) throw new Error("Failed to fetch benefits");
      const data = await response.json();
      // Filter out Health and Retirement plans
      const filteredBenefits = data.filter((benefit: BenefitPlan) => 
        benefit.planType !== "Health" && benefit.planType !== "Retirement"
      );
      setBenefits(filteredBenefits);
    } catch (error) {
      toast.error("Failed to load benefits");
    } finally {
      setIsLoading(false);
    }
  };

  const formatNumber = (value: string) => {
    const numbers = value.replace(/\D/g, "");
    return numbers.replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  };

  const handleMaxCoverageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formattedValue = formatNumber(e.target.value);
    setFormData({ ...formData, maxCoverage: formattedValue });
  };

  const handlePlanTypeChange = (value: string) => {
    setFormData({
      ...formData,
      planType: value,
      customPlanType: value === "Other" ? formData.customPlanType : "",
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const token = authService.getToken();
      if (!token) {
        throw new Error("No authentication token found");
      }

      const url = selectedBenefit
        ? `/api/hr/benefit-plans/${selectedBenefit.planId}`
        : "/api/hr/benefit-plans";
      
      const method = selectedBenefit ? "PUT" : "POST";
      
      const maxCoverageValue = formData.maxCoverage.replace(/,/g, "");
      const finalPlanType = formData.planType === "Other" ? formData.customPlanType : formData.planType;
      
      const response = await fetch(url, {
        method,
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          ...formData,
          planType: finalPlanType,
          maxCoverage: parseFloat(maxCoverageValue),
        }),
      });

      if (!response.ok) throw new Error("Failed to save benefit plan");
      
      toast.success(
        selectedBenefit
          ? "Benefit plan updated successfully"
          : "Benefit plan created successfully"
      );
      
      setIsDialogOpen(false);
      fetchBenefits();
      resetForm();
    } catch (error) {
      toast.error("Failed to save benefit plan");
    }
  };

  const handleDelete = async () => {
    if (!selectedBenefit) return;
    
    try {
      const token = authService.getToken();
      if (!token) {
        throw new Error("No authentication token found");
      }

      const response = await fetch(
        `/api/hr/benefit-plans/${selectedBenefit.planId}/deactivate`,
        {
          method: "PATCH",
          headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      if (!response.ok) throw new Error("Failed to deactivate benefit plan");
      
      toast.success("Benefit plan deactivated successfully");
      setIsDeleteDialogOpen(false);
      fetchBenefits();
    } catch (error) {
      toast.error("Failed to deactivate benefit plan");
    }
  };

  const resetForm = () => {
    setFormData({
      planName: "",
      description: "",
      provider: "",
      eligibility: "",
      planType: "Other",
      customPlanType: "",
      maxCoverage: "",
    });
    setSelectedBenefit(null);
  };

  const openEditDialog = (benefit: BenefitPlan) => {
    setSelectedBenefit(benefit);
    const isCustomType = !BENEFIT_TYPES.includes(benefit.planType);
    setFormData({
      planName: benefit.planName,
      description: benefit.description || "",
      provider: benefit.provider || "",
      eligibility: benefit.eligibility || "",
      planType: isCustomType ? "Other" : benefit.planType,
      customPlanType: isCustomType ? benefit.planType : "",
      maxCoverage: benefit.maxCoverage.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ","),
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
              Other Benefits Management
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">Create, update, and manage other benefit plans</p>
          </div>
          <div className="flex gap-2">
            <Button
              onClick={fetchBenefits}
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
                  <Plus className="h-4 w-4 mr-2" /> Add New Plan
                </Button>
              </DialogTrigger>
              <DialogContent className="sm:max-w-[700px] max-h-[600px] overflow-y-auto">
                <DialogHeader>
                  <DialogTitle>
                    {selectedBenefit ? "Edit Benefit Plan" : "Create Benefit Plan"}
                  </DialogTitle>
                  <DialogDescription>
                    {selectedBenefit
                      ? "Update the details of the benefit plan."
                      : "Fill in the details to create a new benefit plan."}
                  </DialogDescription>
                </DialogHeader>
                <form onSubmit={handleSubmit}>
                  <div className="grid gap-4 py-4">
                    <div className="grid gap-2">
                      <Label htmlFor="planName">Plan Name</Label>
                      <Input
                        id="planName"
                        value={formData.planName}
                        onChange={(e) =>
                          setFormData({ ...formData, planName: e.target.value })
                        }
                        required
                      />
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="description">Description</Label>
                      <Textarea
                        id="description"
                        value={formData.description}
                        onChange={(e) =>
                          setFormData({ ...formData, description: e.target.value })
                        }
                      />
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="provider">Provider</Label>
                      <Input
                        id="provider"
                        value={formData.provider}
                        onChange={(e) =>
                          setFormData({ ...formData, provider: e.target.value })
                        }
                      />
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="eligibility">Eligibility</Label>
                      <Input
                        id="eligibility"
                        value={formData.eligibility}
                        onChange={(e) =>
                          setFormData({ ...formData, eligibility: e.target.value })
                        }
                      />
                    </div>
                    <div className="grid grid-cols-2 gap-2">
                      <div className="w-full">
                        <Label htmlFor="planType">Plan Type</Label>
                        <Select
                          value={formData.planType}
                          onValueChange={handlePlanTypeChange}
                          required
                        >
                          <SelectTrigger className="w-full">
                            <SelectValue placeholder="Select a plan type" />
                          </SelectTrigger>
                          <SelectContent>
                            {BENEFIT_TYPES.map((type) => (
                              <SelectItem key={type} value={type}>
                                {type}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                      {formData.planType === "Other" && (
                        <div className="w-full">
                          <Label htmlFor="customPlanType">Custom Plan Type</Label>
                          <Input
                            id="customPlanType"
                            value={formData.customPlanType}
                            onChange={(e) =>
                              setFormData({ ...formData, customPlanType: e.target.value })
                            }
                            required
                            placeholder="Enter custom plan type"
                            className="w-full"
                          />
                        </div>
                      )}
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="maxCoverage">Maximum Coverage</Label>
                      <Input
                        id="maxCoverage"
                        type="text"
                        value={formData.maxCoverage}
                        onChange={handleMaxCoverageChange}
                        required
                        placeholder="Enter amount"
                      />
                    </div>
                  </div>
                  <DialogFooter>
                    <Button className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-md hover:shadow-lg" type="submit">
                      {selectedBenefit ? "Update Plan" : "Create Plan"}
                    </Button>
                  </DialogFooter>
                </form>
              </DialogContent>
            </Dialog>
          </div>
        </div>

        <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-xl overflow-hidden bg-white dark:bg-[#1F2937]">
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
          <CardHeader className=" bg-[#F9FAFB] dark:bg-[#111827] border-b border-[#E5E7EB] dark:border-[#374151]">
            <div className="flex flex-col md:flex-row justify-between md:items-center gap-4">
              <div>
                <CardTitle className="text-xl text-[#1F2937] dark:text-white flex items-center gap-2">
                  <Plus className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
                  Other Benefit Plans
                </CardTitle>
                <CardDescription className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">
                  View and manage other benefit plans
                </CardDescription>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">{benefits.length} plans</span>
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
            ) : benefits.length === 0 ? (
              <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
                <div className="relative w-16 h-16 mx-auto mb-4">
                  <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] opacity-20 animate-pulse"></div>
                  <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                    <Plus className="h-8 w-8 text-[#6B7280] dark:text-[#9CA3AF]" />
                  </div>
                </div>
                <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">No benefit plans found</h3>
                <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto mb-6">
                  There are no other benefit plans in the system yet. Create your first plan to get started.
                </p>
                <Button
                  className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white shadow-md"
                  onClick={() => setIsDialogOpen(true)}
                >
                  <Plus className="h-4 w-4 mr-2" />
                  Add Benefit Plan
                </Button>
              </div>
            ) : (
              <div className="rounded-lg border border-[#E5E7EB] dark:border-[#374151] overflow-hidden">
                <Table>
                  <TableHeader className="bg-[#F9FAFB] dark:bg-[#111827]">
                    <TableRow className="hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151]">
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium w-12 text-center">No.</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Plan Name</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Plan Type</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Provider</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Max Coverage</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium text-center">Enrollments</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Created At</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">Status</TableHead>
                      <TableHead className="text-[#4B5563] dark:text-[#D1D5DB] font-medium pl-16">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {benefits.map((benefit, index) => (
                      <TableRow
                        key={benefit.planId}
                        className={cn(
                          "hover:bg-[#F3F4F6] dark:hover:bg-[#1F2937] border-b border-[#E5E7EB] dark:border-[#374151] group transition-colors",
                          index % 2 === 0 ? "bg-[#F9FAFB] dark:bg-[#111827]/50" : "",
                        )}
                      >
                        <TableCell className="text-center text-[#4B5563] dark:text-[#D1D5DB] font-medium">{index + 1}</TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          <div className="flex items-center gap-2">
                            {benefit.planName}
                            <HoverCard>
                              <HoverCardTrigger asChild>
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  className="h-8 w-8 p-0 hover:bg-transparent ml-3"
                                >
                                  <AlertCircle className="h-4 w-4 text-[#6B7280] hover:text-[#3B82F6]" />
                                  View
                                </Button>
                              </HoverCardTrigger>
                              <HoverCardContent className="w-80">
                                <div className="space-y-4">
                                  <div>
                                    <h4 className="text-sm font-semibold text-[#1F2937] dark:text-white mb-1">Description</h4>
                                    <p className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                                      {benefit.description || "No description provided"}
                                    </p>
                                  </div>
                                  <div>
                                    <h4 className="text-sm font-semibold text-[#1F2937] dark:text-white mb-1">Eligibility</h4>
                                    <p className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                                      {benefit.eligibility || "No eligibility criteria specified"}
                                    </p>
                                  </div>
                                </div>
                              </HoverCardContent>
                            </HoverCard>
                          </div>
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          {benefit.planType}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          {benefit.provider || "Not specified"}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          {benefit.maxCoverage 
                            ? new Intl.NumberFormat('en-US', {
                                style: 'currency',
                                currency: 'PHP',
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2,
                              }).format(benefit.maxCoverage)
                            : "Not specified"
                          }
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium text-center">
                          {benefit.enrollmentCount || 0}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          {new Date(benefit.createdAt).toLocaleDateString('en-US', {
                            year: 'numeric',
                            month: 'short',
                            day: 'numeric',
                          })}
                        </TableCell>
                        <TableCell className="text-[#4B5563] dark:text-[#D1D5DB] font-medium">
                          <span className="px-2 py-1 rounded-full text-xs bg-green-100 text-green-800">
                            Active
                          </span>
                        </TableCell>
                        <TableCell className="text-left">
                          <div className="flex justify-end gap-4">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => openEditDialog(benefit)}
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
                                setSelectedBenefit(benefit);
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
              You can only deactivate the benefit plan if it is not currently being used by any employees. 
              This will deactivate the benefit plan. Employees will no longer be
              able to enroll in this plan.
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
