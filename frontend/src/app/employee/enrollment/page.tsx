"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import {
  CalendarIcon,
  User,
  MapPin,
  CheckCircle,
  Save,
  ChevronRight,
  ChevronLeft,
  Sparkles,
  Building2,
  Phone,
  Mail,
  Shield,
  AlertCircle,
} from "lucide-react"
import { useRouter } from "next/navigation"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { format } from "date-fns"
import { Progress } from "@/components/ui/progress"
import { cn } from "@/lib/utils"
import { authService } from "@/lib/auth"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog"

export default function EnrollmentForm() {
  const router = useRouter()
  const [activeTab, setActiveTab] = useState("personal")
  const [formSubmitted, setFormSubmitted] = useState(false)
  const [date, setDate] = useState<Date>()
  const [startDate, setStartDate] = useState<Date>()
  const [animateProgress, setAnimateProgress] = useState(false)
  const [profile, setProfile] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    gender: "",
    dateOfBirth: "",
    address: "",
    maritalStatus: "",
    employmentStatus: "FULL_TIME",
    status: true,
    buildingNo: "",
    street: "",
    barangay: "",
    city: "",
    province: "",
    zipCode: "",
    country: "Philippines",
  })
  const [showErrorDialog, setShowErrorDialog] = useState(false)
  const [errorDetails, setErrorDetails] = useState({
    title: "",
    message: "",
  })

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await authService.getEmployeeProfile()
        setProfile(data)
        // Set initial values for non-null fields
        if (data) {
          if (data.dateOfBirth) {
            const dob = new Date(data.dateOfBirth)
            setDate(dob)
            setFormData(prev => ({
              ...prev,
              dateOfBirth: format(dob, 'yyyy-MM-dd')
            }))
          }
          if (data.hireDate) setStartDate(new Date(data.hireDate))
          
          // Split address string if it exists
          let buildingNo = ""
          let street = ""
          let barangay = ""
          let city = ""
          let province = ""
          let zipCode = ""
          
          if (data.address) {
            const addressParts = data.address.split(',')
            if (addressParts.length >= 1) buildingNo = addressParts[0].trim()
            if (addressParts.length >= 2) street = addressParts[1].trim()
            if (addressParts.length >= 3) barangay = addressParts[2].trim()
            if (addressParts.length >= 4) city = addressParts[3].trim()
            if (addressParts.length >= 5) province = addressParts[4].trim()
            if (addressParts.length >= 6) zipCode = addressParts[5].trim()
          }

          // Initialize form data with existing profile data
          setFormData(prev => ({
            ...prev,
            firstName: data.firstName || "",
            lastName: data.lastName || "",
            email: data.email || "",
            phoneNumber: data.phoneNumber || "",
            gender: data.gender || "",
            address: data.address || "",
            maritalStatus: data.maritalStatus || "",
            employmentStatus: data.employmentStatus || "FULL_TIME",
            status: data.status !== undefined ? data.status : true,
            buildingNo: buildingNo,
            street: street,
            barangay: barangay,
            city: city,
            province: province,
            zipCode: zipCode,
            country: "Philippines",
          }))
        }
      } catch (err) {
        console.error("Error fetching profile:", err)
        setError("Failed to fetch profile data")
      } finally {
        setLoading(false)
      }
    }

    fetchProfile()
  }, [])

  // Progress tracking
  const tabOrder = ["personal", "address", "account"]
  const currentTabIndex = tabOrder.indexOf(activeTab)
  const progressPercentage = ((currentTabIndex + 1) / tabOrder.length) * 100

  useEffect(() => {
    // Trigger progress animation when tab changes
    setAnimateProgress(true)
    const timer = setTimeout(() => setAnimateProgress(false), 1000)
    return () => clearTimeout(timer)
  }, [activeTab])

  const handleInputChange = (field: string, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!profile?.employeeId) {
      setErrorDetails({
        title: "Missing Employee ID",
        message: "Your employee ID could not be found. Please try logging out and logging in again.",
      })
      setShowErrorDialog(true)
      return
    }

    // Validate date of birth
    if (date) {
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      const selectedDate = new Date(date)
      selectedDate.setHours(0, 0, 0, 0)
      
      if (selectedDate >= today) {
        setErrorDetails({
          title: "Invalid Date of Birth",
          message: "Date of birth cannot be today or a future date.",
        })
        setShowErrorDialog(true)
        return
      }
    }

    try {
      // Concatenate address components
      const fullAddress = [
        formData.buildingNo,
        formData.street,
        formData.barangay,
        formData.city,
        formData.province,
        formData.zipCode,
        formData.country
      ].filter(Boolean).join(', ')

      const payload = {
        ...formData,
        address: fullAddress,
        dateOfBirth: date ? format(date, 'yyyy-MM-dd') : null,
        hireDate: startDate ? format(startDate, 'yyyy-MM-dd') : null,
        departmentId: profile.departmentId,
        jobId: profile.jobId,
        roleId: profile.roleId,
      }

      console.log('Submitting payload:', payload)

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/employees/${profile.employeeId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify(payload),
      })

      const responseText = await response.text()
      console.log('Raw response:', responseText)

      let errorData
      try {
        errorData = responseText ? JSON.parse(responseText) : {}
      } catch (e) {
        console.error('Failed to parse response as JSON:', e)
        errorData = { message: 'Invalid response from server' }
      }

      if (!response.ok) {
        console.error('Error response:', {
          status: response.status,
          statusText: response.statusText,
          data: errorData
        })
        throw new Error(errorData.message || `Server error: ${response.status} ${response.statusText}`)
      }

      console.log('Success response:', errorData)
      setFormSubmitted(true)
    } catch (err) {
      console.error('Error updating employee:', err)
      
      let errorMessage = 'An unexpected error occurred while updating your information.'
      let errorTitle = 'Update Failed'
      
      if (err instanceof Error) {
        if (err.message.includes('Failed to fetch')) {
          errorTitle = 'Connection Error'
          errorMessage = 'Unable to connect to the server. Please check if:\n\n' +
            '• The backend server is running at http://localhost:8080\n' +
            '• Your internet connection is working\n' +
            '• The API URL is correctly configured'
        } else {
          errorMessage = err.message
        }
      }
      
      setErrorDetails({
        title: errorTitle,
        message: errorMessage,
      })
      setShowErrorDialog(true)
    }
  }

  const handleSaveAsDraft = async () => {
    if (!profile?.employeeId) {
      setErrorDetails({
        title: "Missing Employee ID",
        message: "Your employee ID could not be found. Please try logging out and logging in again.",
      })
      setShowErrorDialog(true)
      return
    }

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/employees/${profile.employeeId}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify(formData),
      })

      if (!response.ok) {
        throw new Error('Failed to save draft')
      }

      setErrorDetails({
        title: "Success",
        message: "Your form has been saved as a draft successfully.",
      })
      setShowErrorDialog(true)
    } catch (err) {
      console.error('Error saving draft:', err)
      
      let errorMessage = 'An unexpected error occurred while saving your draft.'
      let errorTitle = 'Save Failed'
      
      if (err instanceof Error) {
        if (err.message.includes('Failed to fetch')) {
          errorTitle = 'Connection Error'
          errorMessage = 'Unable to connect to the server. Please check if:\n\n' +
            '• The backend server is running at http://localhost:8080\n' +
            '• Your internet connection is working\n' +
            '• The API URL is correctly configured'
        } else {
          errorMessage = err.message
        }
      }
      
      setErrorDetails({
        title: errorTitle,
        message: errorMessage,
      })
      setShowErrorDialog(true)
    }
  }

  const handleNextTab = () => {
    const currentIndex = tabOrder.indexOf(activeTab)
    if (currentIndex < tabOrder.length - 1) {
      setActiveTab(tabOrder[currentIndex + 1])
    }
  }

  const handlePreviousTab = () => {
    const currentIndex = tabOrder.indexOf(activeTab)
    if (currentIndex > 0) {
      setActiveTab(tabOrder[currentIndex - 1])
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A]">
        <div className="relative h-16 w-16">
          <div className="absolute inset-0 rounded-full border-2 border-[#E5E7EB] dark:border-[#374151] opacity-30"></div>
          <div className="absolute inset-0 rounded-full border-t-2 border-[#3B82F6] dark:border-[#3B82F6] animate-spin"></div>
          <div className="absolute inset-3 rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] opacity-20 animate-pulse"></div>
        </div>
      </div>
    )
  }

  if (formSubmitted) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4">
        <div className="w-full max-w-3xl mx-auto">
          <div className="flex flex-col items-center justify-center mb-12 relative">
            {/* Decorative elements */}
            <div className="absolute -top-10 -left-10 w-20 h-20 bg-[#3B82F6] opacity-10 rounded-full blur-xl"></div>
            <div className="absolute -bottom-10 -right-10 w-20 h-20 bg-[#14B8A6] opacity-10 rounded-full blur-xl"></div>

            <div className="relative h-24 w-24 mb-6">
              <div className="absolute inset-0 bg-gradient-to-br from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] rounded-full animate-pulse"></div>
              <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                <CheckCircle className="h-12 w-12 text-[#3B82F6] dark:text-[#3B82F6]" />
              </div>
            </div>

            <h1 className="text-3xl font-bold text-center text-[#1F2937] dark:text-white tracking-tight">
              WORKFORCE HUB
            </h1>
            <div className="h-1 w-20 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] rounded-full my-2"></div>
            <p className="text-sm text-[#6B7280] dark:text-[#9CA3AF] uppercase tracking-wider">ENTERPRISE PORTAL</p>
          </div>

          <div className="text-center p-8 bg-white dark:bg-[#1F2937] rounded-xl shadow-lg border border-[#E5E7EB] dark:border-[#374151] relative overflow-hidden">
            {/* Background pattern */}
            <div className="absolute top-0 right-0 w-40 h-40 bg-gradient-to-br from-[#3B82F6]/5 to-[#14B8A6]/5 dark:from-[#3B82F6]/10 dark:to-[#14B8A6]/10 rounded-bl-full -z-10"></div>
            <div className="absolute bottom-0 left-0 w-40 h-40 bg-gradient-to-tr from-[#3B82F6]/5 to-[#14B8A6]/5 dark:from-[#3B82F6]/10 dark:to-[#14B8A6]/10 rounded-tr-full -z-10"></div>

            <Sparkles className="h-8 w-8 text-[#3B82F6] dark:text-[#3B82F6] mx-auto mb-4" />
            <h2 className="text-3xl font-bold mb-4 text-[#1F2937] dark:text-white tracking-tight">
              Successfully Submitted!
            </h2>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mb-8 max-w-md mx-auto">
              Your submission is being reviewed for approval. Just login with your account after 24 hours.
            </p>
            
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-8">
      <div className="w-full max-w-4xl mx-auto">
        <div className="mb-8 flex flex-col md:flex-row justify-between items-center">
          <div className="flex items-center mb-4 md:mb-0">
            <div className="h-20 w-20 bg-white dark:bg-[#1F2937] mr-4 rounded-lg flex items-center justify-center shadow-md overflow-hidden">
              <img src="/Logo with no Text.png" alt="Workforce Hub Logo" className="h-15 w-15 object-contain" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white tracking-tight">WORKFORCE HUB</h1>
              <div className="h-0.5 w-12 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] rounded-full my-1"></div>
              <p className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">ENTERPRISE PORTAL</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-sm font-medium text-[#6B7280] dark:text-[#9CA3AF]">Completion:</span>
            <div className="w-32 md:w-48 relative">
              <Progress
                value={progressPercentage}
                className="h-2 bg-[#E5E7EB] dark:bg-[#374151] rounded-full overflow-hidden"
                indicatorClassName={cn(
                  "bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] transition-all duration-700 ease-in-out",
                  animateProgress && "animate-pulse",
                )}
              />
              <div
                className="absolute -bottom-1 left-0 h-4 w-4 rounded-full bg-white dark:bg-[#1F2937] border-2 border-[#3B82F6] dark:border-[#3B82F6] transition-all duration-700"
                style={{ left: `${progressPercentage}%`, transform: "translateX(-50%)" }}
              ></div>
            </div>
            <span className="text-sm font-medium text-[#3B82F6] dark:text-[#3B82F6]">
              {Math.round(progressPercentage)}%
            </span>
          </div>
        </div>

        <div className="bg-white dark:bg-[#1F2937] rounded-xl shadow-lg overflow-hidden border border-[#E5E7EB] dark:border-[#374151] relative">
          {/* Top border accent */}
          <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>

          {/* Background pattern */}
          <div className="absolute top-0 right-0 w-40 h-40 bg-gradient-to-br from-[#3B82F6]/5 to-[#14B8A6]/5 dark:from-[#3B82F6]/10 dark:to-[#14B8A6]/10 rounded-bl-full -z-10"></div>
          <div className="absolute bottom-0 left-0 w-40 h-40 bg-gradient-to-tr from-[#3B82F6]/5 to-[#14B8A6]/5 dark:from-[#3B82F6]/10 dark:to-[#14B8A6]/10 rounded-tr-full -z-10"></div>

          <div className="p-6 md:p-8 border-b border-[#E5E7EB] dark:border-[#374151]">
            <h2 className="text-2xl font-bold text-[#1F2937] dark:text-white tracking-tight">
              Employee Enrollment Form
            </h2>
            <p className="text-[#6B7280] dark:text-[#9CA3AF]">
              Complete all required information to finalize your registration
            </p>
          </div>

          <Tabs defaultValue="personal" value={activeTab} onValueChange={setActiveTab} className="w-full">
            <div className="border-b border-[#E5E7EB] dark:border-[#374151]">
              <TabsList className="flex w-full rounded-none bg-transparent h-auto p-0">
                {tabOrder.map((tab, index) => (
                  <TabsTrigger
                    key={tab}
                    value={tab}
                    className={cn(
                      "flex-1 py-4 rounded-none border-b-2 border-transparent data-[state=active]:border-[#3B82F6] data-[state=active]:text-[#3B82F6] dark:data-[state=active]:text-[#3B82F6] data-[state=active]:bg-transparent transition-all duration-300",
                      index < currentTabIndex && "text-[#14B8A6] dark:text-[#14B8A6]",
                    )}
                  >
                    <div className="flex flex-col items-center gap-1 md:flex-row md:gap-2">
                      <div
                        className={cn(
                          "h-8 w-8 rounded-full flex items-center justify-center text-xs font-medium transition-all duration-300",
                          index < currentTabIndex
                            ? "bg-gradient-to-br from-[#14B8A6] via-[#0EA5E9] to-[#0D9488] text-white shadow-md"
                            : index === currentTabIndex
                              ? "bg-gradient-to-br from-[#3B82F6] via-[#0EA5E9] to-[#2563EB] text-white shadow-md"
                              : "bg-[#E5E7EB] dark:bg-[#374151] text-[#6B7280] dark:text-[#9CA3AF]",
                        )}
                      >
                        {index < currentTabIndex ? <CheckCircle className="h-4 w-4" /> : index + 1}
                      </div>
                      <span className="hidden md:inline capitalize font-medium">{tab}</span>
                      {tab === "personal" && <User className="h-4 w-4 md:hidden" />}
                      {tab === "address" && <MapPin className="h-4 w-4 md:hidden" />}
                      {tab === "account" && <Shield className="h-4 w-4 md:hidden" />}
                    </div>
                  </TabsTrigger>
                ))}
              </TabsList>
            </div>

            <form onSubmit={handleSubmit}>
              <TabsContent value="personal" className="p-6 md:p-8 animate-in fade-in-50 duration-300">
                <div className="mb-6 border-l-4 border-[#3B82F6] pl-4">
                  <h3 className="text-xl font-semibold text-[#1F2937] dark:text-white mb-2 tracking-tight">
                    Personal Information
                  </h3>
                  <p className="text-[#6B7280] dark:text-[#9CA3AF]">Please provide your basic personal details</p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2 group">
                    <Label
                      htmlFor="firstName"
                      className="text-[#1F2937] dark:text-white group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200"
                    >
                      First Name <span className="text-red-500 dark:text-red-400">*</span>
                    </Label>
                    <div className="relative">
                      <Input
                        id="firstName"
                        placeholder="Enter your first name"
                        value={formData.firstName}
                        onChange={(e) => handleInputChange('firstName', e.target.value)}
                        disabled={!!profile?.firstName}
                        className={cn(
                          "border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 focus:border-[#3B82F6] dark:focus:border-[#3B82F6] focus:border-2 focus:border-solid focus:ring-0 dark:focus:ring-0 pl-10 transition-all duration-200 bg-white dark:bg-[#1F2937]/60 rounded-md shadow-sm",
                          profile?.firstName && "bg-gray-50 dark:bg-gray-900/50"
                        )}
                        required
                      />
                      <div className="absolute inset-y-0 left-0 w-0.5 rounded-l-md bg-gradient-to-b from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] opacity-0 group-focus-within:opacity-100 transition-opacity duration-200"></div>
                      <User className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF] group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200" />
                    </div>
                  </div>
                  <div className="space-y-2 group">
                    <Label
                      htmlFor="lastName"
                      className="text-[#1F2937] dark:text-white group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200"
                    >
                      Last Name <span className="text-red-500 dark:text-red-400">*</span>
                    </Label>
                    <div className="relative">
                      <Input
                        id="lastName"
                        placeholder="Enter your last name"
                        value={formData.lastName}
                        onChange={(e) => handleInputChange('lastName', e.target.value)}
                        disabled={!!profile?.lastName}
                        className={cn(
                          "border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 focus:border-[#3B82F6] dark:focus:border-[#3B82F6] focus:border-2 focus:border-solid focus:ring-0 dark:focus:ring-0 pl-10 transition-all duration-200 bg-white dark:bg-[#1F2937]/60 rounded-md shadow-sm",
                          profile?.lastName && "bg-gray-50 dark:bg-gray-900/50"
                        )}
                        required
                      />
                      <div className="absolute inset-y-0 left-0 w-0.5 rounded-l-md bg-gradient-to-b from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] opacity-0 group-focus-within:opacity-100 transition-opacity duration-200"></div>
                      <User className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF] group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200" />
                    </div>
                  </div>
                  <div className="space-y-2 group">
                    <Label
                      htmlFor="dob"
                      className="text-[#1F2937] dark:text-white group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200"
                    >
                      Date of Birth <span className="text-red-500 dark:text-red-400">*</span>
                    </Label>
                    <Popover>
                      <PopoverTrigger asChild>
                        <Button
                          variant="outline"
                          className={cn(
                            "w-full justify-start text-left font-normal border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 focus:border-[#3B82F6] dark:focus:border-[#3B82F6] focus:border-2 focus:border-solid focus:ring-0 dark:focus:ring-0 transition-all duration-200 bg-white dark:bg-[#1F2937]/60 rounded-md shadow-sm",
                            !date && "text-muted-foreground",
                            profile?.dateOfBirth && "bg-gray-50 dark:bg-gray-900/50",
                          )}
                          disabled={!!profile?.dateOfBirth}
                        >
                          <CalendarIcon className="mr-2 h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF]" />
                          {date ? format(date, 'yyyy-MM-dd') : <span>Pick a date</span>}
                        </Button>
                      </PopoverTrigger>
                      <PopoverContent className="w-auto p-0 border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 bg-white dark:bg-[#1F2937] rounded-md shadow-md">
                        <Calendar
                          mode="single"
                          selected={date}
                          onSelect={(selectedDate) => {
                            setDate(selectedDate)
                            setFormData(prev => ({
                              ...prev,
                              dateOfBirth: selectedDate ? format(selectedDate, 'yyyy-MM-dd') : ""
                            }))
                          }}
                          disabled={!!profile?.dateOfBirth}
                          initialFocus
                          className="rounded-md"
                        />
                      </PopoverContent>
                    </Popover>
                  </div>
                  <div className="space-y-2 group">
                    <Label
                      htmlFor="maritalStatus"
                      className="text-[#1F2937] dark:text-white group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200"
                    >
                      Marital Status <span className="text-red-500 dark:text-red-400">*</span>
                    </Label>
                    <Select
                      value={formData.maritalStatus}
                      onValueChange={(value) => handleInputChange('maritalStatus', value)}
                      disabled={!!profile?.maritalStatus}
                    >
                      <SelectTrigger className="w-full border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 focus:border-[#3B82F6] dark:focus:border-[#3B82F6] focus:border-2 focus:border-solid focus:ring-0 dark:focus:ring-0 transition-all duration-200 bg-white dark:bg-[#1F2937]/60 rounded-md shadow-sm">
                        <SelectValue placeholder="Select marital status" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="SINGLE">Single</SelectItem>
                        <SelectItem value="MARRIED">Married</SelectItem>
                        <SelectItem value="DIVORCED">Divorced</SelectItem>
                        <SelectItem value="WIDOWED">Widowed</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2 group">
                    <Label
                      htmlFor="phone"
                      className="text-[#1F2937] dark:text-white group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200"
                    >
                      Phone Number <span className="text-red-500 dark:text-red-400">*</span>
                    </Label>
                    <div className="relative">
                      <Input
                        id="phone"
                        type="tel"
                        placeholder="(555) 555-5555"
                        value={formData.phoneNumber}
                        onChange={(e) => handleInputChange('phoneNumber', e.target.value)}
                        disabled={!!profile?.phoneNumber}
                        className={cn(
                          "border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 focus:border-[#3B82F6] dark:focus:border-[#3B82F6] focus:border-2 focus:border-solid focus:ring-0 dark:focus:ring-0 pl-10 transition-all duration-200 bg-white dark:bg-[#1F2937]/60 rounded-md shadow-sm",
                          profile?.phoneNumber && "bg-gray-50 dark:bg-gray-900/50"
                        )}
                        required
                      />
                      <div className="absolute inset-y-0 left-0 w-0.5 rounded-l-md bg-gradient-to-b from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] opacity-0 group-focus-within:opacity-100 transition-opacity duration-200"></div>
                      <Phone className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF] group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200" />
                    </div>
                  </div>
                  <div className="space-y-2 group">
                    <Label
                      htmlFor="email"
                      className="text-[#1F2937] dark:text-white group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200"
                    >
                      Email Address <span className="text-red-500 dark:text-red-400">*</span>
                    </Label>
                    <div className="relative">
                      <Input
                        id="email"
                        type="email"
                        placeholder="your.email@example.com"
                        value={formData.email}
                        onChange={(e) => handleInputChange('email', e.target.value)}
                        disabled={!!profile?.email}
                        className={cn(
                          "border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 focus:border-[#3B82F6] dark:focus:border-[#3B82F6] focus:border-2 focus:border-solid focus:ring-0 dark:focus:ring-0 pl-10 transition-all duration-200 bg-white dark:bg-[#1F2937]/60 rounded-md shadow-sm",
                          profile?.email && "bg-gray-50 dark:bg-gray-900/50"
                        )}
                        required
                      />
                      <div className="absolute inset-y-0 left-0 w-0.5 rounded-l-md bg-gradient-to-b from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] opacity-0 group-focus-within:opacity-100 transition-opacity duration-200"></div>
                      <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF] group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200" />
                    </div>
                  </div>
                  <div className="space-y-2 col-span-2">
                    <Label className="text-[#1F2937] dark:text-white">
                      Gender <span className="text-red-500 dark:text-red-400">*</span>
                    </Label>
                    <RadioGroup
                      value={formData.gender}
                      onValueChange={(value) => handleInputChange('gender', value)}
                      disabled={!!profile?.gender}
                      className={cn(
                        "grid grid-cols-1 sm:grid-cols-3 gap-2",
                        profile?.gender && "bg-gray-50 dark:bg-gray-900/50 p-2 rounded-md"
                      )}
                    >
                      <div className="flex items-center space-x-2 border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 rounded-md p-3 hover:border-[#3B82F6] dark:hover:border-[#3B82F6] hover:bg-[#F9FAFB] dark:hover:bg-[#1F2937]/80 transition-all duration-200">
                        <RadioGroupItem value="male" id="male" className="text-[#3B82F6] dark:text-[#3B82F6]" />
                        <Label htmlFor="male" className="cursor-pointer w-full text-[#1F2937] dark:text-white">
                          Male
                        </Label>
                      </div>
                      <div className="flex items-center space-x-2 border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 rounded-md p-3 hover:border-[#3B82F6] dark:hover:border-[#3B82F6] hover:bg-[#F9FAFB] dark:hover:bg-[#1F2937]/80 transition-all duration-200">
                        <RadioGroupItem value="female" id="female" className="text-[#3B82F6] dark:text-[#3B82F6]" />
                        <Label htmlFor="female" className="cursor-pointer w-full text-[#1F2937] dark:text-white">
                          Female
                        </Label>
                      </div>
                      <div className="flex items-center space-x-2 border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 rounded-md p-3 hover:border-[#3B82F6] dark:hover:border-[#3B82F6] hover:bg-[#F9FAFB] dark:hover:bg-[#1F2937]/80 transition-all duration-200">
                        <RadioGroupItem value="other" id="other" className="text-[#3B82F6] dark:text-[#3B82F6]" />
                        <Label htmlFor="other" className="cursor-pointer w-full text-[#1F2937] dark:text-white">
                          Other
                        </Label>
                      </div>
                    </RadioGroup>
                  </div>
                </div>
              </TabsContent>

              <TabsContent value="address" className="p-6 md:p-8 animate-in fade-in-50 duration-300">
                <div className="mb-6 border-l-4 border-[#3B82F6] pl-4">
                  <h3 className="text-xl font-semibold text-[#1F2937] dark:text-white mb-2 tracking-tight">
                    Address Information
                  </h3>
                  <p className="text-[#6B7280] dark:text-[#9CA3AF]">Your residential address details</p>
                </div>

                <div className="p-4 mb-6 bg-gradient-to-r from-[#EFF6FF] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1E3A8A]/20 dark:via-[#134E4A]/20 dark:to-[#0C4A6E]/20 border border-[#E5E7EB]/60 dark:border-[#374151]/60 rounded-lg relative overflow-hidden">
                  <div className="absolute top-0 right-0 w-20 h-20 bg-gradient-to-br from-[#3B82F6]/10 to-[#14B8A6]/10 dark:from-[#3B82F6]/20 dark:to-[#14B8A6]/20 rounded-bl-full"></div>
                  <h4 className="font-medium text-[#1F2937] dark:text-white mb-2 flex items-center">
                    <div className="h-5 w-5 rounded-full bg-gradient-to-br from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] flex items-center justify-center text-white text-xs mr-2">
                      <CheckCircle className="h-3 w-3" />
                    </div>
                    Primary Address
                  </h4>
                  <p className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                    This address will be used for official communications
                  </p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2 group">
                    <Label
                      htmlFor="buildingNo"
                      className="text-[#1F2937] dark:text-white group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200"
                    >
                      Building No./Room <span className="text-red-500 dark:text-red-400">*</span>
                    </Label>
                    <div className="relative">
                      <Input
                        id="buildingNo"
                        placeholder="Enter building number or room"
                        value={formData.buildingNo}
                        onChange={(e) => handleInputChange('buildingNo', e.target.value)}
                        disabled={!!profile?.address}
                        className={cn(
                          "border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 focus:border-[#3B82F6] dark:focus:border-[#3B82F6] focus:border-2 focus:border-solid focus:ring-0 dark:focus:ring-0 pl-10 transition-all duration-200 bg-white dark:bg-[#1F2937]/60 rounded-md shadow-sm",
                          profile?.address && "bg-gray-50 dark:bg-gray-900/50"
                        )}
                        required
                      />
                      <div className="absolute inset-y-0 left-0 w-0.5 rounded-l-md bg-gradient-to-b from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] opacity-0 group-focus-within:opacity-100 transition-opacity duration-200"></div>
                      <Building2 className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF] group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200" />
                    </div>
                  </div>

                  <div className="space-y-2 group">
                    <Label
                      htmlFor="street"
                      className="text-[#1F2937] dark:text-white group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200"
                    >
                      Street <span className="text-red-500 dark:text-red-400">*</span>
                    </Label>
                    <div className="relative">
                      <Input
                        id="street"
                        placeholder="Enter street name"
                        value={formData.street}
                        onChange={(e) => handleInputChange('street', e.target.value)}
                        disabled={!!profile?.address}
                        className={cn(
                          "border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 focus:border-[#3B82F6] dark:focus:border-[#3B82F6] focus:border-2 focus:border-solid focus:ring-0 dark:focus:ring-0 pl-10 transition-all duration-200 bg-white dark:bg-[#1F2937]/60 rounded-md shadow-sm",
                          profile?.address && "bg-gray-50 dark:bg-gray-900/50"
                        )}
                        required
                      />
                      <div className="absolute inset-y-0 left-0 w-0.5 rounded-l-md bg-gradient-to-b from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] opacity-0 group-focus-within:opacity-100 transition-opacity duration-200"></div>
                      <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF] group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200" />
                    </div>
                  </div>

                  <div className="space-y-2 group">
                    <Label
                      htmlFor="barangay"
                      className="text-[#1F2937] dark:text-white group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200"
                    >
                      Barangay <span className="text-red-500 dark:text-red-400">*</span>
                    </Label>
                    <div className="relative">
                      <Input
                        id="barangay"
                        placeholder="Enter barangay"
                        value={formData.barangay}
                        onChange={(e) => handleInputChange('barangay', e.target.value)}
                        disabled={!!profile?.address}
                        className={cn(
                          "border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 focus:border-[#3B82F6] dark:focus:border-[#3B82F6] focus:border-2 focus:border-solid focus:ring-0 dark:focus:ring-0 pl-10 transition-all duration-200 bg-white dark:bg-[#1F2937]/60 rounded-md shadow-sm",
                          profile?.address && "bg-gray-50 dark:bg-gray-900/50"
                        )}
                        required
                      />
                      <div className="absolute inset-y-0 left-0 w-0.5 rounded-l-md bg-gradient-to-b from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] opacity-0 group-focus-within:opacity-100 transition-opacity duration-200"></div>
                      <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF] group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200" />
                    </div>
                  </div>

                  <div className="space-y-2 group">
                    <Label
                      htmlFor="city"
                      className="text-[#1F2937] dark:text-white group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200"
                    >
                      City <span className="text-red-500 dark:text-red-400">*</span>
                    </Label>
                    <div className="relative">
                      <Input
                        id="city"
                        placeholder="Enter city"
                        value={formData.city}
                        onChange={(e) => handleInputChange('city', e.target.value)}
                        disabled={!!profile?.address}
                        className={cn(
                          "border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 focus:border-[#3B82F6] dark:focus:border-[#3B82F6] focus:border-2 focus:border-solid focus:ring-0 dark:focus:ring-0 pl-10 transition-all duration-200 bg-white dark:bg-[#1F2937]/60 rounded-md shadow-sm",
                          profile?.address && "bg-gray-50 dark:bg-gray-900/50"
                        )}
                        required
                      />
                      <div className="absolute inset-y-0 left-0 w-0.5 rounded-l-md bg-gradient-to-b from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] opacity-0 group-focus-within:opacity-100 transition-opacity duration-200"></div>
                      <Building2 className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF] group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200" />
                    </div>
                  </div>

                  <div className="space-y-2 group">
                    <Label
                      htmlFor="province"
                      className="text-[#1F2937] dark:text-white group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200"
                    >
                      Province <span className="text-red-500 dark:text-red-400">*</span>
                    </Label>
                    <div className="relative">
                      <Input
                        id="province"
                        placeholder="Enter province"
                        value={formData.province}
                        onChange={(e) => handleInputChange('province', e.target.value)}
                        disabled={!!profile?.address}
                        className={cn(
                          "border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 focus:border-[#3B82F6] dark:focus:border-[#3B82F6] focus:border-2 focus:border-solid focus:ring-0 dark:focus:ring-0 pl-10 transition-all duration-200 bg-white dark:bg-[#1F2937]/60 rounded-md shadow-sm",
                          profile?.address && "bg-gray-50 dark:bg-gray-900/50"
                        )}
                        required
                      />
                      <div className="absolute inset-y-0 left-0 w-0.5 rounded-l-md bg-gradient-to-b from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] opacity-0 group-focus-within:opacity-100 transition-opacity duration-200"></div>
                      <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF] group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200" />
                    </div>
                  </div>

                  <div className="space-y-2 group">
                    <Label
                      htmlFor="zipCode"
                      className="text-[#1F2937] dark:text-white group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200"
                    >
                      ZIP Code <span className="text-red-500 dark:text-red-400">*</span>
                    </Label>
                    <div className="relative">
                      <Input
                        id="zipCode"
                        placeholder="Enter ZIP code"
                        value={formData.zipCode}
                        onChange={(e) => handleInputChange('zipCode', e.target.value)}
                        disabled={!!profile?.address}
                        className={cn(
                          "border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 focus:border-[#3B82F6] dark:focus:border-[#3B82F6] focus:border-2 focus:border-solid focus:ring-0 dark:focus:ring-0 pl-10 transition-all duration-200 bg-white dark:bg-[#1F2937]/60 rounded-md shadow-sm",
                          profile?.address && "bg-gray-50 dark:bg-gray-900/50"
                        )}
                        required
                      />
                      <div className="absolute inset-y-0 left-0 w-0.5 rounded-l-md bg-gradient-to-b from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] opacity-0 group-focus-within:opacity-100 transition-opacity duration-200"></div>
                      <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF] group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200" />
                    </div>
                  </div>

                  <div className="space-y-2 group">
                    <Label
                      htmlFor="country"
                      className="text-[#1F2937] dark:text-white group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200"
                    >
                      Country <span className="text-red-500 dark:text-red-400">*</span>
                    </Label>
                    <div className="relative">
                      <Input
                        id="country"
                        value="Philippines"
                        disabled
                        className="border border-[#3B82F6]/70 dark:border-[#3B82F6]/70 focus:border-[#3B82F6] dark:focus:border-[#3B82F6] focus:border-2 focus:border-solid focus:ring-0 dark:focus:ring-0 pl-10 transition-all duration-200 bg-gray-50 dark:bg-gray-900/50 rounded-md shadow-sm"
                      />
                      <div className="absolute inset-y-0 left-0 w-0.5 rounded-l-md bg-gradient-to-b from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] opacity-0 group-focus-within:opacity-100 transition-opacity duration-200"></div>
                      <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[#6B7280] dark:text-[#9CA3AF] group-focus-within:text-[#3B82F6] dark:group-focus-within:text-[#3B82F6] transition-colors duration-200" />
                    </div>
                  </div>
                </div>
              </TabsContent>

              <TabsContent value="account" className="p-6 md:p-8 animate-in fade-in-50 duration-300">
                <div className="mb-6 border-l-4 border-[#3B82F6] pl-4">
                  <h3 className="text-xl font-semibold text-[#1F2937] dark:text-white mb-2 tracking-tight">
                    Account Details
                  </h3>
                  <p className="text-[#6B7280] dark:text-[#9CA3AF]">Set up your account preferences</p>
                </div>

                <div className="p-4 mb-6 bg-gradient-to-r from-[#EFF6FF] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1E3A8A]/20 dark:via-[#134E4A]/20 dark:to-[#0C4A6E]/20 border border-[#E5E7EB]/60 dark:border-[#374151]/60 rounded-lg relative overflow-hidden">
                  <div className="absolute top-0 right-0 w-20 h-20 bg-gradient-to-br from-[#3B82F6]/10 to-[#14B8A6]/10 dark:from-[#3B82F6]/20 dark:to-[#14B8A6]/20 rounded-bl-full"></div>
                  <h4 className="font-medium text-[#1F2937] dark:text-white mb-2 flex items-center">
                    <div className="h-5 w-5 rounded-full bg-gradient-to-br from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] flex items-center justify-center text-white text-xs mr-2">
                      <Shield className="h-3 w-3" />
                    </div>
                    Account Security
                  </h4>
                  <p className="text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                    Your account will be secured with Microsoft authentication
                  </p>
                </div>

                <div className="space-y-6">
                  <div className="p-4 rounded-lg border border-[#E5E7EB]/60 dark:border-[#374151]/60 bg-white dark:bg-[#1F2937]/60 shadow-sm">
                    <h4 className="font-medium text-[#1F2937] dark:text-white mb-2">Terms and Conditions</h4>
                    <div className="space-y-3">
                      <div className="flex items-start">
                        <input
                          type="checkbox"
                          id="termsAgree"
                          className="h-4 w-4 mt-1 rounded border-[#3B82F6]/70 dark:border-[#3B82F6]/70 text-[#3B82F6] dark:text-[#3B82F6] focus:ring-1 focus:ring-[#3B82F6] dark:focus:ring-[#3B82F6]"
                          required
                        />
                        <label htmlFor="termsAgree" className="ml-2 text-sm text-[#1F2937] dark:text-white">
                          I agree to the{" "}
                          <a href="#" className="text-[#3B82F6] dark:text-[#3B82F6] hover:underline">
                            Terms of Service
                          </a>{" "}
                          and{" "}
                          <a href="#" className="text-[#3B82F6] dark:text-[#3B82F6] hover:underline">
                            Privacy Policy
                          </a>
                        </label>
                      </div>
                      <div className="flex items-start">
                        <input
                          type="checkbox"
                          id="dataConsent"
                          className="h-4 w-4 mt-1 rounded border-[#3B82F6]/70 dark:border-[#3B82F6]/70 text-[#3B82F6] dark:text-[#3B82F6] focus:ring-1 focus:ring-[#3B82F6] dark:focus:ring-[#3B82F6]"
                          required
                        />
                        <label htmlFor="dataConsent" className="ml-2 text-sm text-[#1F2937] dark:text-white">
                          I consent to the collection and processing of my personal information as described in the
                          Privacy Policy
                        </label>
                      </div>
                    </div>
                  </div>
                </div>
              </TabsContent>

              <div className="flex justify-between p-6 border-t border-[#E5E7EB] dark:border-[#374151] bg-gradient-to-r from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937]/80 dark:via-[#134E4A]/80 dark:to-[#0F172A]/80">
                <div className="flex gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={handleSaveAsDraft}
                    className="border-[#6B7280] text-[#6B7280] dark:border-[#6B7280] dark:text-[#9CA3AF] hover:bg-gray-50 dark:hover:bg-[#1F2937]/80 transition-all duration-200"
                  >
                    <Save className="h-4 w-4 mr-2" />
                    Save as Draft
                  </Button>
                  {currentTabIndex > 0 && (
                    <Button
                      type="button"
                      variant="outline"
                      onClick={handlePreviousTab}
                      className="border-[#3B82F6] text-[#3B82F6] dark:border-[#3B82F6] dark:text-[#3B82F6] hover:bg-[#EFF6FF] dark:hover:bg-[#1E3A8A]/20 transition-all duration-200"
                    >
                      <ChevronLeft className="h-4 w-4 mr-1" />
                      Previous
                    </Button>
                  )}
                </div>
                <div>
                  {currentTabIndex < tabOrder.length - 1 ? (
                    <Button
                      type="button"
                      onClick={handleNextTab}
                      className="bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#2563EB] hover:from-[#2563EB] hover:via-[#0284C7] hover:to-[#1D4ED8] text-white shadow-md transition-all duration-300 hover:shadow-lg"
                    >
                      Next
                      <ChevronRight className="h-4 w-4 ml-1" />
                    </Button>
                  ) : (
                    <Button
                      type="submit"
                      className="bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6] hover:from-[#2563EB] hover:via-[#0284C7] hover:to-[#0D9488] text-white shadow-md transition-all duration-300 hover:shadow-lg"
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

        <div className="mt-6 text-center text-sm text-[#6B7280] dark:text-[#9CA3AF]">
          <p>© 2023 Workforce Hub. All rights reserved.</p>
          <div className="mt-2 flex justify-center gap-4">
            <a href="#" className="text-[#3B82F6] dark:text-[#3B82F6] hover:underline transition-all duration-200">
              Privacy Policy
            </a>
            <a href="#" className="text-[#3B82F6] dark:text-[#3B82F6] hover:underline transition-all duration-200">
              Terms of Service
            </a>
            <a href="#" className="text-[#3B82F6] dark:text-[#3B82F6] hover:underline transition-all duration-200">
              Help Center
            </a>
          </div>
        </div>
      </div>

      <Dialog open={showErrorDialog} onOpenChange={setShowErrorDialog}>
        <DialogContent className="sm:max-w-[425px] bg-white dark:bg-[#1F2937] text-[#1F2937] dark:text-white">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <AlertCircle className={cn(
                "h-5 w-5",
                errorDetails.title.toLowerCase() === "success" 
                  ? "text-green-500" 
                  : "text-red-500"
              )} />
              {errorDetails.title}
            </DialogTitle>
            <DialogDescription className="text-[#6B7280] dark:text-[#9CA3AF] whitespace-pre-line">
              {errorDetails.message}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              onClick={() => setShowErrorDialog(false)}
              className={cn(
                "w-full sm:w-auto",
                errorDetails.title.toLowerCase() === "success"
                  ? "bg-green-500 hover:bg-green-600"
                  : "bg-[#3B82F6] hover:bg-[#2563EB]",
                "text-white"
              )}
            >
              {errorDetails.title.toLowerCase() === "success" ? "Continue" : "Close"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}

