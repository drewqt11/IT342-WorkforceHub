"use client";

import type React from "react";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  CalendarIcon,
  User,
  Building2,
  MapPin,
  FileText,
  Lock,
  CheckCircle,
  ArrowRight,
  Save,
  Upload,
} from "lucide-react";
import { useRouter } from "next/navigation";
import { Calendar } from "@/components/ui/calendar";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { format } from "date-fns";
import { Progress } from "@/components/ui/progress";
import { cn } from "@/lib/utils";

export default function EnrollmentForm() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState("personal");
  const [formSubmitted, setFormSubmitted] = useState(false);
  const [date, setDate] = useState<Date>();
  const [startDate, setStartDate] = useState<Date>();

  // Progress tracking
  const tabOrder = ["personal", "address", "account"];
  const currentTabIndex = tabOrder.indexOf(activeTab);
  const progressPercentage = ((currentTabIndex + 1) / tabOrder.length) * 100;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setFormSubmitted(true);
  };

  const handleSaveAsDraft = () => {
    // Logic to save as draft
    alert("Form saved as draft");
  };

  const handleNextTab = () => {
    const currentIndex = tabOrder.indexOf(activeTab);
    if (currentIndex < tabOrder.length - 1) {
      setActiveTab(tabOrder[currentIndex + 1]);
    }
  };

  const handlePreviousTab = () => {
    const currentIndex = tabOrder.indexOf(activeTab);
    if (currentIndex > 0) {
      setActiveTab(tabOrder[currentIndex - 1]);
    }
  };

  if (formSubmitted) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-[#F9FAFB] p-4">
        <div className="w-full max-w-3xl mx-auto">
          <div className="flex flex-col items-center justify-center mb-12">
            <div className="h-20 w-20 bg-[#3B82F6] rounded-full flex items-center justify-center mb-6">
              <CheckCircle className="h-10 w-10 text-white" />
            </div>
            <h1 className="text-3xl font-bold text-center text-[#1F2937]">
              WORKFORCE HUB
            </h1>
            <p className="text-sm text-[#6B7280] uppercase tracking-wider">
              ENTERPRISE PORTAL
            </p>
          </div>

          <div className="text-center p-8 bg-white rounded-lg shadow-lg border border-[#E5E7EB]">
            <h2 className="text-3xl font-bold mb-4 text-[#1F2937]">
              Enrollment Successfully Submitted!
            </h2>
            <p className="text-[#6B7280] mb-8">
              Your submission is being reviewed for approval. You will receive a
              confirmation email shortly.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Button
                onClick={() => router.push("/")}
                className="bg-[#3B82F6] hover:bg-[#2563EB] text-white"
              >
                Sign In to Your Account
              </Button>
              <Button
                variant="outline"
                onClick={() => setFormSubmitted(false)}
                className="border-[#3B82F6] text-[#3B82F6]"
              >
                Return to Form
              </Button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#F9FAFB] p-4 md:p-8">
      <div className="w-full max-w-4xl mx-auto">
        <div className="mb-8 flex flex-col md:flex-row justify-between items-center">
          <div className="flex items-center mb-4 md:mb-0">
            <div className="h-12 w-12 bg-[#3B82F6] rounded-lg flex items-center justify-center mr-4">
              <img
                src="https://hebbkx1anhila5yf.public.blob.vercel-storage.com/Workforce_Hub-gziX2Ift38tJLqQr5cNyIfEhauLgcf.png"
                alt="Company Logo"
                className="h-8 w-8"
              />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-[#1F2937]">
                WORKFORCE HUB
              </h1>
              <p className="text-sm text-[#6B7280]">ENTERPRISE PORTAL</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-sm font-medium text-[#6B7280]">
              Completion:
            </span>
            <div className="w-32 md:w-48">
              <Progress
                value={progressPercentage}
                className="h-2 bg-[#E5E7EB]"
                indicatorClassName="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6]"
              />
            </div>
            <span className="text-sm font-medium text-[#3B82F6]">
              {Math.round(progressPercentage)}%
            </span>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-lg overflow-hidden border border-[#E5E7EB]">
          <div className="p-6 md:p-8 border-b border-[#E5E7EB]">
            <h2 className="text-2xl font-bold text-[#1F2937]">
              Employee Enrollment Form
            </h2>
            <p className="text-[#6B7280]">
              Complete all required information to finalize your registration
            </p>
          </div>

          <Tabs
            defaultValue="personal"
            value={activeTab}
            onValueChange={setActiveTab}
            className="w-full"
          >
            <div className="border-b border-[#E5E7EB]">
              <TabsList className="flex w-full rounded-none bg-transparent h-auto p-0">
                {tabOrder.map((tab, index) => (
                  <TabsTrigger
                    key={tab}
                    value={tab}
                    className={cn(
                      "flex-1 py-4 rounded-none border-b-2 border-transparent data-[state=active]:border-[#3B82F6] data-[state=active]:text-[#3B82F6] data-[state=active]:bg-transparent",
                      index < currentTabIndex && "text-[#14B8A6]"
                    )}
                  >
                    <div className="flex flex-col items-center gap-1 md:flex-row md:gap-2">
                      <div
                        className={cn(
                          "h-6 w-6 rounded-full flex items-center justify-center text-xs font-medium",
                          index < currentTabIndex
                            ? "bg-[#14B8A6] text-white"
                            : index === currentTabIndex
                            ? "bg-[#3B82F6] text-white"
                            : "bg-[#E5E7EB] text-[#6B7280]"
                        )}
                      >
                        {index < currentTabIndex ? (
                          <CheckCircle className="h-3 w-3" />
                        ) : (
                          index + 1
                        )}
                      </div>
                      <span className="hidden md:inline capitalize">{tab}</span>
                      {tab === "personal" && (
                        <User className="h-4 w-4 md:hidden" />
                      )}
                      {tab === "employment" && (
                        <Building2 className="h-4 w-4 md:hidden" />
                      )}
                      {tab === "address" && (
                        <MapPin className="h-4 w-4 md:hidden" />
                      )}
                      {tab === "account" && (
                        <Lock className="h-4 w-4 md:hidden" />
                      )}
                      {tab === "documents" && (
                        <FileText className="h-4 w-4 md:hidden" />
                      )}
                    </div>
                  </TabsTrigger>
                ))}
              </TabsList>
            </div>

            <form onSubmit={handleSubmit}>
              <TabsContent value="personal" className="p-6 md:p-8">
                <div className="mb-6">
                  <h3 className="text-xl font-semibold text-[#1F2937] mb-2">
                    Personal Information
                  </h3>
                  <p className="text-[#6B7280]">
                    Please provide your basic personal details
                  </p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="firstName" className="text-[#1F2937]">
                      First Name <span className="text-red-500">*</span>
                    </Label>
                    <Input
                      id="firstName"
                      placeholder="Enter your first name"
                      className="border-[#E5E7EB] focus:border-[#3B82F6] focus:ring-[#3B82F6]"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="lastName" className="text-[#1F2937]">
                      Last Name <span className="text-red-500">*</span>
                    </Label>
                    <Input
                      id="lastName"
                      placeholder="Enter your last name"
                      className="border-[#E5E7EB] focus:border-[#3B82F6] focus:ring-[#3B82F6]"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="middleName" className="text-[#1F2937]">
                      Middle Name (Optional)
                    </Label>
                    <Input
                      id="middleName"
                      placeholder="Enter your middle name"
                      className="border-[#E5E7EB] focus:border-[#3B82F6] focus:ring-[#3B82F6]"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="dob" className="text-[#1F2937]">
                      Date of Birth <span className="text-red-500">*</span>
                    </Label>
                    <Popover>
                      <PopoverTrigger asChild>
                        <Button
                          variant="outline"
                          className="w-full justify-start text-left font-normal border-[#E5E7EB] focus:border-[#3B82F6] focus:ring-[#3B82F6]"
                        >
                          <CalendarIcon className="mr-2 h-4 w-4 text-[#6B7280]" />
                          {date ? format(date, "PPP") : "Select date of birth"}
                        </Button>
                      </PopoverTrigger>
                      <PopoverContent className="w-auto p-0">
                        <Calendar
                          mode="single"
                          selected={date}
                          onSelect={setDate}
                          initialFocus
                          className="rounded-md border border-[#E5E7EB]"
                        />
                      </PopoverContent>
                    </Popover>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="phone" className="text-[#1F2937]">
                      Phone Number <span className="text-red-500">*</span>
                    </Label>
                    <Input
                      id="phone"
                      placeholder="(555) 555-5555"
                      className="border-[#E5E7EB] focus:border-[#3B82F6] focus:ring-[#3B82F6]"
                      required
                    />
                  </div>
                  <div className="space-y-2 col-span-2">
                    <Label className="text-[#1F2937]">
                      Gender <span className="text-red-500">*</span>
                    </Label>
                    <RadioGroup
                      defaultValue="male"
                      className="grid grid-cols-1 sm:grid-cols-3 gap-2"
                    >
                      <div className="flex items-center space-x-2 border border-[#E5E7EB] rounded-md p-3 hover:border-[#3B82F6] transition-colors">
                        <RadioGroupItem
                          value="male"
                          id="male"
                          className="text-[#3B82F6]"
                        />
                        <Label htmlFor="male" className="cursor-pointer w-full">
                          Male
                        </Label>
                      </div>
                      <div className="flex items-center space-x-2 border border-[#E5E7EB] rounded-md p-3 hover:border-[#3B82F6] transition-colors">
                        <RadioGroupItem
                          value="female"
                          id="female"
                          className="text-[#3B82F6]"
                        />
                        <Label
                          htmlFor="female"
                          className="cursor-pointer w-full"
                        >
                          Female
                        </Label>
                      </div>
                      <div className="flex items-center space-x-2 border border-[#E5E7EB] rounded-md p-3 hover:border-[#3B82F6] transition-colors">
                        <RadioGroupItem
                          value="prefer-not-to-say"
                          id="prefer-not-to-say"
                          className="text-[#3B82F6]"
                        />
                        <Label
                          htmlFor="prefer-not-to-say"
                          className="cursor-pointer w-full"
                        >
                          Prefer not to say
                        </Label>
                      </div>
                    </RadioGroup>
                  </div>
                  <div className="col-span-2 mt-4 pt-4 border-t border-[#E5E7EB]">
                    <h3 className="text-lg font-medium mb-4 text-[#1F2937]">
                      Emergency Contact
                    </h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-2">
                        <Label htmlFor="contactName" className="text-[#1F2937]">
                          Contact Name <span className="text-red-500">*</span>
                        </Label>
                        <Input
                          id="contactName"
                          placeholder="Full name"
                          className="border-[#E5E7EB] focus:border-[#3B82F6] focus:ring-[#3B82F6]"
                          required
                        />
                      </div>
                      <div className="space-y-2">
                        <Label
                          htmlFor="contactPhone"
                          className="text-[#1F2937]"
                        >
                          Phone Number <span className="text-red-500">*</span>
                        </Label>
                        <Input
                          id="contactPhone"
                          placeholder="(555) 555-5555"
                          className="border-[#E5E7EB] focus:border-[#3B82F6] focus:ring-[#3B82F6]"
                          required
                        />
                      </div>
                      <div className="space-y-2 col-span-2">
                        <Label
                          htmlFor="contactRelationship"
                          className="text-[#1F2937]"
                        >
                          Relationship <span className="text-red-500">*</span>
                        </Label>
                        <Select required>
                          <SelectTrigger className="border-[#E5E7EB] focus:border-[#3B82F6] focus:ring-[#3B82F6]">
                            <SelectValue placeholder="Select relationship" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="spouse">Spouse</SelectItem>
                            <SelectItem value="parent">Parent</SelectItem>
                            <SelectItem value="sibling">Sibling</SelectItem>
                            <SelectItem value="friend">Friend</SelectItem>
                            <SelectItem value="guardian">Guardian</SelectItem>
                            <SelectItem value="other">Other</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                    </div>
                  </div>
                </div>
              </TabsContent>

              <TabsContent value="address" className="p-6 md:p-8">
                <div className="mb-6">
                  <h3 className="text-xl font-semibold text-[#1F2937] mb-2">
                    Address Information
                  </h3>
                  <p className="text-[#6B7280]">
                    Your residential address details
                  </p>
                </div>

                <div className="p-4 mb-6 bg-gradient-to-r from-[#EFF6FF] to-[#F0FDFA] border border-[#E5E7EB] rounded-lg">
                  <h4 className="font-medium text-[#1F2937] mb-2">
                    Primary Address
                  </h4>
                  <p className="text-sm text-[#6B7280]">
                    This address will be used for official communications
                  </p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="streetAddress" className="text-[#1F2937]">
                      Street Address <span className="text-red-500">*</span>
                    </Label>
                    <Input
                      id="streetAddress"
                      placeholder="Enter your street address"
                      className="border-[#E5E7EB] focus:border-[#3B82F6] focus:ring-[#3B82F6]"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="aptSuite" className="text-[#1F2937]">
                      Subdivision/Apt/Suite (Optional)
                    </Label>
                    <Input
                      id="aptSuite"
                      placeholder="Apartment, suite, unit, etc."
                      className="border-[#E5E7EB] focus:border-[#3B82F6] focus:ring-[#3B82F6]"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="city" className="text-[#1F2937]">
                      City <span className="text-red-500">*</span>
                    </Label>
                    <Input
                      id="city"
                      placeholder="Enter your city"
                      className="border-[#E5E7EB] focus:border-[#3B82F6] focus:ring-[#3B82F6]"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="stateProvince" className="text-[#1F2937]">
                      State/Province <span className="text-red-500">*</span>
                    </Label>
                    <Input
                      id="stateProvince"
                      placeholder="Enter your state or province"
                      className="border-[#E5E7EB] focus:border-[#3B82F6] focus:ring-[#3B82F6]"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="zipCode" className="text-[#1F2937]">
                      Zip/Postal Code <span className="text-red-500">*</span>
                    </Label>
                    <Input
                      id="zipCode"
                      placeholder="Enter your zip code"
                      className="border-[#E5E7EB] focus:border-[#3B82F6] focus:ring-[#3B82F6]"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="stateProvince" className="text-[#1F2937]">
                      Country <span className="text-red-500">*</span>
                    </Label>
                    <Input
                      id="stateProvince"
                      value="Philippines"
                      className="border-[#E5E7EB] focus:border-[#3B82F6] focus:ring-[#3B82F6]"
                      disabled
                    />
                  </div>
                </div>
              </TabsContent>

              <TabsContent value="account" className="p-6 md:p-8">
                <div className="mb-6">
                  <h3 className="text-xl font-semibold text-[#1F2937] mb-2">
                    Account Security
                  </h3>
                  <p className="text-[#6B7280]">
                    Set up your login credentials
                  </p>
                </div>

                <div className="grid grid-cols-1 gap-6 max-w-md mx-auto">
                  <div className="space-y-2">
                    <Label htmlFor="email" className="text-[#1F2937]">
                      Email Address
                    </Label>
                    <Input
                      id="email"
                      type="email"
                      placeholder="your.email@company.com"
                      disabled
                      className="bg-gray-50 border-[#E5E7EB] text-[#6B7280]"
                    />
                    <p className="text-xs text-[#6B7280]">
                      Your email address cannot be changed
                    </p>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="password" className="text-[#1F2937]">
                      Password <span className="text-red-500">*</span>
                    </Label>
                    <Input
                      id="password"
                      type="password"
                      placeholder="••••••••"
                      className="border-[#E5E7EB] focus:border-[#3B82F6] focus:ring-[#3B82F6]"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="confirmPassword" className="text-[#1F2937]">
                      Confirm Password <span className="text-red-500">*</span>
                    </Label>
                    <Input
                      id="confirmPassword"
                      type="password"
                      placeholder="••••••••"
                      className="border-[#E5E7EB] focus:border-[#3B82F6] focus:ring-[#3B82F6]"
                      required
                    />
                  </div>
                  <div className="bg-gradient-to-r from-[#EFF6FF] to-[#F0FDFA] p-4 rounded-lg border border-[#E5E7EB] mt-2">
                    <h4 className="font-medium text-[#1F2937] mb-2 flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-[#14B8A6]" />
                      Password Requirements
                    </h4>
                    <ul className="text-sm text-[#6B7280] space-y-2">
                      <li className="flex items-center gap-2">
                        <div className="h-1.5 w-1.5 rounded-full bg-[#3B82F6]"></div>
                        <span>At least 8 characters long</span>
                      </li>
                      <li className="flex items-center gap-2">
                        <div className="h-1.5 w-1.5 rounded-full bg-[#3B82F6]"></div>
                        <span>Contains at least one uppercase letter</span>
                      </li>
                      <li className="flex items-center gap-2">
                        <div className="h-1.5 w-1.5 rounded-full bg-[#3B82F6]"></div>
                        <span>Contains at least one number</span>
                      </li>
                      <li className="flex items-center gap-2">
                        <div className="h-1.5 w-1.5 rounded-full bg-[#3B82F6]"></div>
                        <span>Contains at least one special character</span>
                      </li>
                    </ul>
                  </div>
                  <div className="flex items-center space-x-2 mt-4">
                    <input
                      type="checkbox"
                      id="termsAgree"
                      className="h-4 w-4 rounded border-[#E5E7EB] text-[#3B82F6] focus:ring-[#3B82F6]"
                      required
                    />
                    <Label
                      htmlFor="termsAgree"
                      className="text-[#6B7280] text-sm"
                    >
                      I agree to the{" "}
                      <a href="#" className="text-[#3B82F6] hover:underline">
                        Terms of Service
                      </a>{" "}
                      and{" "}
                      <a href="#" className="text-[#3B82F6] hover:underline">
                        Privacy Policy
                      </a>
                    </Label>
                  </div>
                </div>
              </TabsContent>

              <div className="flex justify-between p-6 border-t border-[#E5E7EB] bg-[#F9FAFB]">
                <div className="flex gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={handleSaveAsDraft}
                    className="border-[#6B7280] text-[#6B7280] hover:bg-gray-50"
                  >
                    <Save className="h-4 w-4 mr-2" />
                    Save as Draft
                  </Button>
                  {currentTabIndex > 0 && (
                    <Button
                      type="button"
                      variant="outline"
                      onClick={handlePreviousTab}
                      className="border-[#3B82F6] text-[#3B82F6] hover:bg-[#EFF6FF]"
                    >
                      Previous
                    </Button>
                  )}
                </div>
                <div>
                  {currentTabIndex < tabOrder.length - 1 ? (
                    <Button
                      type="button"
                      onClick={handleNextTab}
                      className="bg-[#3B82F6] hover:bg-[#2563EB] text-white"
                    >
                      Next
                      <ArrowRight className="h-4 w-4 ml-2" />
                    </Button>
                  ) : (
                    <Button
                      type="submit"
                      className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white"
                    >
                      Complete Enrollment
                      <CheckCircle className="h-4 w-4 ml-2" />
                    </Button>
                  )}
                </div>
              </div>
            </form>
          </Tabs>
        </div>

        <div className="mt-6 text-center text-sm text-[#6B7280]">
          <p>© 2023 Workforce Hub. All rights reserved.</p>
          <div className="mt-2 flex justify-center gap-4">
            <a href="#" className="text-[#3B82F6] hover:underline">
              Privacy Policy
            </a>
            <a href="#" className="text-[#3B82F6] hover:underline">
              Terms of Service
            </a>
            <a href="#" className="text-[#3B82F6] hover:underline">
              Help Center
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
