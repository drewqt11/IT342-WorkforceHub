"use client"

import { useEffect, useState, useCallback, useMemo } from "react"
import { authService } from "@/lib/auth"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { format } from "date-fns"
import { useRouter } from "next/navigation"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"
import {
  ArrowLeft,
  User,
  Briefcase,
  Calendar,
  Mail,
  Phone,
  MapPin,
  Heart,
  Clock,
  Building2,
  Award,
  AlertCircle,
  Shield,
  LogIn,
  RefreshCw,
  Clock10,
  Pencil,
} from "lucide-react"
import { toast, Toaster } from "sonner"

interface EmployeeProfile {
  employeeId: string
  idNumber: string
  firstName: string
  lastName: string
  gender: string
  dateOfBirth: string
  address: string
  phoneNumber: string
  maritalStatus: string
  email: string
  hireDate: string
  status: boolean
  employmentStatus: string
  departmentId: string
  departmentName: string
  jobId: string
  jobName: string
  roleId: string
  roleName: string
  createdAt: string
  workTimeInSched: string
  workTimeOutSched: string
  userId?: string
}

interface UserAccountInfo {
  userId: string
  email: string
  createdAt: string
  lastLogin: string
  isActive: boolean
}

interface ProfileField {
  label: string
  field: keyof EmployeeProfile
  type: 'text' | 'date' | 'select'
  options?: string[]
}

const formatText = (text: string | null | undefined): string => {
  if (!text || text === "NULL") return "Not Provided"
  
  // Handle special cases
  if (text.toLowerCase() === "active" || text.toLowerCase() === "inactive") {
    return text.charAt(0).toUpperCase() + text.slice(1).toLowerCase()
  }
  
  // Handle marital status and other enum values
  if (["SINGLE", "MARRIED", "DIVORCED", "WIDOWED", "MALE", "FEMALE", "OTHER"].includes(text.toUpperCase())) {
    return text.charAt(0).toUpperCase() + text.slice(1).toLowerCase()
  }
  
  // Handle phone number format
  if (/^\+?[\d\s-]{10,}$/.test(text)) {
    // Format as (XXX) XXX-XXXX or similar
    const cleaned = text.replace(/\D/g, '')
    if (cleaned.length === 10) {
      return `(${cleaned.slice(0, 3)}) ${cleaned.slice(3, 6)}-${cleaned.slice(6)}`
    }
    return text
  }
  
  // Split by spaces, underscores, and hyphens
  return text
    .split(/[\s_-]+/)
    .map(word => {
      // Handle special cases for words like "ID", "HR", etc.
      if (word.toUpperCase() === word && word.length <= 2) {
        return word
      }
      return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
    })
    .join(' ')
}

const formatTime = (time: string | null | undefined): string => {
  if (!time || time === "NULL") return "Not Provided"
  try {
    const [hours, minutes] = time.split(':')
    const hourNum = parseInt(hours)
    const minNum = parseInt(minutes)
    
    // Validate time components
    if (isNaN(hourNum) || isNaN(minNum) || hourNum < 0 || hourNum > 23 || minNum < 0 || minNum > 59) {
      return "Not Provided"
    }
    
    const period = hourNum >= 12 ? 'PM' : 'AM'
    const standardHour = hourNum % 12 || 12 // Convert 0 to 12 for 12 AM
    const formattedMinutes = minNum.toString().padStart(2, '0') // Ensure two digits for minutes
    
    return `${standardHour}:${formattedMinutes} ${period}`
  } catch {
    return "Not Provided"
  }
}

const formatDisplayValue = (value: string | null | undefined, type: 'text' | 'date' | 'time' = 'text'): string => {
  if (!value || value === "NULL") return "Not Provided"
  
  switch (type) {
    case 'date':
      return format(new Date(value), "MMMM d, yyyy")
    case 'time':
      return formatTime(value)
    default:
      return formatText(value)
  }
}

const LoadingState = () => (
  <div className="min-h-screen bg-gradient-to-b from-[#F9FAFB] to-[#EFF6FF] p-6">
    <div className="max-w-6xl mx-auto">
      <div className="flex items-center justify-between mb-8">
        <div className="flex items-center">
          <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-4 shadow-md">
            <User className="h-5 w-5 text-white" />
          </div>
          <h1 className="text-3xl font-bold text-[#1F2937]">My Profile</h1>
        </div>
        <Button
          variant="outline"
          onClick={() => window.history.back()}
          className="border-[#3B82F6] text-[#3B82F6] hover:bg-[#EFF6FF] transition-all duration-200 flex items-center"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back
        </Button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1">
          <Card className="border border-[#E5E7EB] shadow-md overflow-hidden bg-white">
            <div className="flex justify-center -mt-24">
              <div className="-mt-8 h-32 w-32 rounded-full border-4 border-white bg-[#F9FAFB] flex items-center justify-center">
                <Skeleton className="h-full w-full rounded-full" />
              </div>
            </div>
            <CardContent className="pt-4 pb-6 px-6">
              <div className="text-center mb-6">
                <Skeleton className="h-6 w-3/4 mx-auto mb-2" />
                <Skeleton className="h-4 w-1/2 mx-auto" />
              </div>
              <div className="space-y-4">
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-full" />
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="lg:col-span-2">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Card className="border border-[#E5E7EB] shadow-md bg-white">
              <CardHeader className="pb-2">
                <CardTitle className="flex items-center text-[#1F2937]">
                  <User className="h-5 w-5 mr-2 text-[#3B82F6]" />
                  Personal Information
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-4 w-full" />
                </div>
              </CardContent>
            </Card>

            <Card className="border border-[#E5E7EB] shadow-md bg-white">
              <CardHeader className="pb-2">
                <CardTitle className="flex items-center text-[#1F2937]">
                  <Briefcase className="h-5 w-5 mr-2 text-[#14B8A6]" />
                  Employment Information
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-4 w-full" />
                </div>
              </CardContent>
            </Card>
          </div>

          <div className="mt-6">
            <Card className="border border-[#E5E7EB] shadow-md bg-white">
              <CardHeader className="pb-2">
                <CardTitle className="flex items-center text-[#1F2937]">
                  <Shield className="h-5 w-5 mr-2 text-[#3B82F6]" />
                  Account Information
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-4 w-full" />
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  </div>
)

const ErrorState = ({ error }: { error: string }) => (
  <div className="min-h-screen bg-gradient-to-b from-[#F9FAFB] to-[#EFF6FF] p-6">
    <div className="max-w-6xl mx-auto">
      <div className="flex items-center justify-between mb-8">
        <div className="flex items-center">
          <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-4 shadow-md">
            <User className="h-5 w-5 text-white" />
          </div>
          <h1 className="text-3xl font-bold text-[#1F2937]">My Profile</h1>
        </div>
        <Button
          variant="outline"
          onClick={() => window.history.back()}
          className="border-[#3B82F6] text-[#3B82F6] hover:bg-[#EFF6FF] transition-all duration-200 flex items-center"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back
        </Button>
      </div>

      <Card className="border border-[#E5E7EB] shadow-md bg-white">
        <CardContent className="p-6">
          <div className="flex items-center mb-4">
            <div className="h-10 w-10 rounded-full bg-red-100 flex items-center justify-center mr-4">
              <AlertCircle className="h-5 w-5 text-red-500" />
            </div>
            <h2 className="text-xl font-semibold text-[#1F2937]">Error Loading Profile</h2>
          </div>
          <p className="text-[#6B7280] mb-6">{error}</p>
          <div className="flex justify-end">
            <Button
              variant="outline"
              onClick={() => window.location.reload()}
              className="border-[#3B82F6] text-[#3B82F6] hover:bg-[#EFF6FF] transition-all duration-200 flex items-center"
            >
              <RefreshCw className="h-4 w-4 mr-2" />
              Try Again
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
)

const Header = ({ isEditing, isUpdating, onEditToggle }: { isEditing: boolean, isUpdating: boolean, onEditToggle: () => void }) => (
  <div className="flex items-center justify-between mb-8">
    <div className="flex items-center">
      <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-4 shadow-md">
        <User className="h-5 w-5 text-white" />
      </div>
      <h1 className="text-3xl font-bold text-[#1F2937]">My Profile</h1>
    </div>
    <div className="flex items-center gap-4">
      <Button
        onClick={onEditToggle}
        disabled={isUpdating}
        className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white transition-all duration-200 shadow-md hover:shadow-lg flex items-center"
      >
        {isUpdating ? (
          <div className="flex items-center gap-2">
            <div className="h-4 w-4 rounded-full border-2 border-white border-t-transparent animate-spin"></div>
            <span>Updating...</span>
          </div>
        ) : (
          <>
            <Pencil className="h-4 w-4 mr-2" />
            {isEditing ? "Update Profile" : "Edit Profile"}
          </>
        )}
      </Button>
      <Button
        variant="outline"
        onClick={() => window.history.back()}
        className="border-[#3B82F6] text-[#3B82F6] hover:bg-[#EFF6FF] transition-all duration-200 flex items-center"
      >
        <ArrowLeft className="h-4 w-4 mr-2" />
        Back
      </Button>
    </div>
  </div>
)

const ProfileSummaryCard = ({ profile }: { profile: EmployeeProfile | null }) => (
  <div className="lg:col-span-1">
    <Card className="border border-[#E5E7EB] shadow-md overflow-hidden bg-white hover:shadow-lg transition-shadow duration-300">
      <div className="flex justify-center">
        <div className="h-32 w-32 rounded-full border-4 border-white bg-[#F9FAFB] flex items-center justify-center shadow-md overflow-hidden">
          <div className="bg-gradient-to-br from-[#3B82F6]/20 to-[#14B8A6]/20 h-full w-full flex items-center justify-center">
            <User className="h-16 w-16 text-[#3B82F6]" />
          </div>
        </div>
      </div>
      <CardContent className="pt-4 pb-6 px-6">
        <div className="text-center mb-6">
          <h2 className="text-xl text-[#1F2937]">
            {formatDisplayValue(profile?.firstName)} {formatDisplayValue(profile?.lastName)}
          </h2>
          <p className="text-[#6B7280]">{formatDisplayValue(profile?.jobName)}</p>
        </div>

        <div className="space-y-4">
          <div className="flex items-center">
            <div className="h-8 w-8 rounded-full bg-[#EFF6FF] flex items-center justify-center flex-shrink-0 mr-3">
              <Mail className="h-4 w-4 text-[#3B82F6]" />
            </div>
            <div className="text-sm text-[#6B7280] break-words">{formatDisplayValue(profile?.email)}</div>
          </div>

          <div className="flex items-center">
            <div className="h-8 w-8 rounded-full bg-[#F0FDFA] flex items-center justify-center flex-shrink-0 mr-3">
              <Phone className="h-4 w-4 text-[#14B8A6]" />
            </div>
            <div className="text-sm text-[#6B7280]">{formatDisplayValue(profile?.phoneNumber)}</div>
          </div>

          <div className="flex items-start">
            <div className="h-8 w-8 rounded-full bg-[#EFF6FF] flex items-center justify-center flex-shrink-0 mr-3 mt-1">
              <MapPin className="h-4 w-4 text-[#3B82F6]" />
            </div>
            <div className="text-sm text-[#6B7280] break-words flex-1">{formatDisplayValue(profile?.address)}</div>
          </div>

          <div className="flex items-center">
            <div className="h-8 w-8 rounded-full bg-[#F0FDFA] flex items-center justify-center flex-shrink-0 mr-3">
              <Building2 className="h-4 w-4 text-[#14B8A6]" />
            </div>
            <div className="text-sm text-[#6B7280]">{formatDisplayValue(profile?.departmentName)}</div>
          </div>
        </div>

        <div className="mt-6 pt-6 border-t border-[#E5E7EB]">
          <div className="grid grid-cols-3 gap-2">
            <div className="text-center">
              <div className="h-10 w-10 rounded-full bg-[#F0FDFA] flex items-center justify-center mx-auto mb-2">
                <Clock className="h-5 w-5 text-[#14B8A6]" />
              </div>
              <div className="text-xs text-[#6B7280]">Employment</div>
              <div className="mt-1 text-xs text-[#1F2937]">
                {formatDisplayValue(profile?.employmentStatus)}
              </div>
            </div>

            <div className="text-center">
              <div className="h-10 w-10 rounded-full bg-[#EFF6FF] flex items-center justify-center mx-auto mb-2">
                <Award className="h-5 w-5 text-[#3B82F6]" />
              </div>
              <div className="text-xs text-[#6B7280]">Role</div>
              <div className="mt-1 text-xs text-[#1F2937]">
                {formatDisplayValue(profile?.roleName)}
              </div>
            </div>

            <div className="text-center">
              <div className="h-10 w-10 rounded-full bg-[#EFF6FF] flex items-center justify-center mx-auto mb-2">
                <Clock10 className="h-5 w-5 text-[#3B82F6]" />
              </div>
              <div className="text-xs text-[#6B7280]">Work Schedule</div>
              <div className="mt-1 text-xs text-[#1F2937]">
                {formatDisplayValue(profile?.workTimeInSched, 'time')} - {formatDisplayValue(profile?.workTimeOutSched, 'time')}
              </div>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
)

const DetailedInfoCards = ({ 
  profile, 
  editedProfile, 
  isEditing, 
  onInputChange,
  profileFields 
}: { 
  profile: EmployeeProfile | null
  editedProfile: EmployeeProfile | null
  isEditing: boolean
  onInputChange: (field: keyof EmployeeProfile, value: string) => void
  profileFields: ProfileField[]
}) => (
  <div className="lg:col-span-2">
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      <Card className="border border-[#E5E7EB] shadow-md bg-white hover:shadow-lg transition-shadow duration-300">
        <CardHeader className="pb-2 border-b border-[#E5E7EB]">
          <CardTitle className="flex items-center text-[#1F2937]">
            <User className="h-5 w-5 mr-2 text-[#3B82F6]" />
            Personal Information
          </CardTitle>
        </CardHeader>
        <CardContent className="pt-4">
          <div className="space-y-3">
            {profileFields.map((field) => (
              <div key={field.field} className="grid grid-cols-2 border-b border-dashed border-[#E5E7EB] pb-3">
                <div className="text-sm text-[#6B7280]">{field.label}</div>
                <div className="text-sm text-[#6B7280]">
                  {isEditing ? (
                    field.type === 'select' ? (
                      <Select
                        value={editedProfile?.[field.field]?.toString() || ""}
                        onValueChange={(value) => onInputChange(field.field, value)}
                      >
                        <SelectTrigger className="text-sm text-[#6B7280]">
                          <SelectValue placeholder={`Select ${field.label.toLowerCase()}`} />
                        </SelectTrigger>
                        <SelectContent>
                          {field.options?.map((option) => (
                            <SelectItem key={option} value={option}>
                              {option}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    ) : field.type === 'date' ? (
                      <Input
                        type="date"
                        value={editedProfile?.[field.field]?.toString().split('T')[0] || ""}
                        onChange={(e) => onInputChange(field.field, e.target.value)}
                        className="text-sm text-[#6B7280]"
                      />
                    ) : field.field === 'address' ? (
                      <Textarea
                        value={editedProfile?.[field.field]?.toString() || ""}
                        onChange={(e) => onInputChange(field.field, e.target.value)}
                        className="text-sm text-[#6B7280] min-h-[100px]"
                      />
                    ) : (
                      <Input
                        value={editedProfile?.[field.field]?.toString() || ""}
                        onChange={(e) => onInputChange(field.field, e.target.value)}
                        className="text-sm text-[#6B7280]"
                      />
                    )
                  ) : (
                    <div className="break-words">
                      {formatDisplayValue(profile?.[field.field]?.toString(), field.type === 'date' ? 'date' : 'text')}
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      <Card className="border border-[#E5E7EB] shadow-md bg-white hover:shadow-lg transition-shadow duration-300">
        <CardHeader className="pb-2 border-b border-[#E5E7EB]">
          <CardTitle className="flex items-center text-[#1F2937]">
            <Briefcase className="h-5 w-5 mr-2 text-[#14B8A6]" />
            Employment Information
          </CardTitle>
        </CardHeader>
        <CardContent className="pt-4">
          <div className="space-y-3">
            <div className="grid grid-cols-2 border-b border-dashed border-[#E5E7EB] pb-3">
              <div className="text-sm text-[#6B7280]">Status</div>
              <div className="text-sm text-[#6B7280]">
                <span
                  className={`px-2 py-1 rounded-full text-xs ${
                    profile?.status === true
                      ? "bg-green-100 text-green-800 border border-green-200"
                      : "bg-red-100 text-red-800 border border-red-200"
                  }`}
                >
                  {profile?.status === true ? "Active" : "Inactive"}
                </span>
              </div>
            </div>

            <div className="grid grid-cols-2 border-b border-dashed border-[#E5E7EB] pb-3">
              <div className="text-sm text-[#6B7280]">Employment Status</div>
              <div className="text-sm text-[#6B7280]">
                {formatDisplayValue(profile?.employmentStatus)}
              </div>
            </div>

            <div className="grid grid-cols-2 border-b border-dashed border-[#E5E7EB] pb-3">
              <div className="text-sm text-[#6B7280]">Hire Date</div>
              <div className="text-sm text-[#6B7280] flex items-center">
                <Calendar className="h-3.5 w-3.5 mr-1.5 text-[#14B8A6]" />
                {formatDisplayValue(profile?.hireDate, 'date')}
              </div>
            </div>

            <div className="grid grid-cols-2 border-b border-dashed border-[#E5E7EB] pb-3">
              <div className="text-sm text-[#6B7280]">Department</div>
              <div className="text-sm text-[#6B7280] flex items-center">
                <Building2 className="h-3.5 w-3.5 mr-1.5 text-[#14B8A6]" />
                {formatDisplayValue(profile?.departmentName)}
              </div>
            </div>

            <div className="grid grid-cols-2 border-b border-dashed border-[#E5E7EB] pb-3">
              <div className="text-sm text-[#6B7280]">Job Title</div>
              <div className="text-sm text-[#6B7280] flex items-center">
                <Briefcase className="h-3.5 w-3.5 mr-1.5 text-[#14B8A6]" />
                {formatDisplayValue(profile?.jobName)}
              </div>
            </div>

            <div className="grid grid-cols-2 border-b border-dashed border-[#E5E7EB] pb-3">
              <div className="text-sm text-[#6B7280]">Role</div>
              <div className="text-sm text-[#6B7280] flex items-center">
                <Award className="h-3.5 w-3.5 mr-1.5 text-[#14B8A6]" />
                {formatDisplayValue(profile?.roleName)}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <div className="mt-6">
      <Card className="border border-[#E5E7EB] shadow-md bg-white hover:shadow-lg transition-shadow duration-300">
        <CardHeader className="pb-2 border-b border-[#E5E7EB]">
          <CardTitle className="flex items-center text-[#1F2937]">
            <Shield className="h-5 w-5 mr-2 text-[#3B82F6]" />
            Account Information
          </CardTitle>
        </CardHeader>
        <CardContent className="pt-4">
          <div className="space-y-3">
            <div className="grid grid-cols-2 border-b border-dashed border-[#E5E7EB] pb-3">
              <div className="text-sm text-[#6B7280]">Account Status</div>
              <div className="text-sm text-[#6B7280]">
                <span className="px-2 py-1 rounded-full text-xs bg-green-100 text-green-800 border border-green-200">
                  Active
                </span>
              </div>
            </div>

            <div className="grid grid-cols-2 border-b border-dashed border-[#E5E7EB] pb-3">
              <div className="text-sm text-[#6B7280]">Account Created</div>
              <div className="text-sm text-[#6B7280] flex items-center">
                <Calendar className="h-3.5 w-3.5 mr-1.5 text-[#3B82F6]" />
                {formatDisplayValue(profile?.createdAt, 'date')}
              </div>
            </div>

            <div className="grid grid-cols-2 border-b border-dashed border-[#E5E7EB] pb-3">
              <div className="text-sm text-[#6B7280]">Last Login</div>
              <div className="text-sm text-[#6B7280] flex items-center">
                <LogIn className="h-3.5 w-3.5 mr-1.5 text-[#3B82F6]" />
                {format(new Date(), "MMMM d, yyyy 'at' h:mm a")}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
)

export default function EmployeeProfile() {
  const router = useRouter()
  const [profile, setProfile] = useState<EmployeeProfile | null>(null)
  const [userAccount, setUserAccount] = useState<UserAccountInfo | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isUpdating, setIsUpdating] = useState(false)
  const [editedProfile, setEditedProfile] = useState<EmployeeProfile | null>(null)
  const [isEditing, setIsEditing] = useState(false)

  const fetchUserAccount = useCallback(async (profileData: EmployeeProfile) => {
    try {
      const userData = await authService.getOAuth2UserInfo()
      setUserAccount({
        userId: userData.userId || "Not Available",
        email: userData.email || profileData.email,
        createdAt: profileData.createdAt || new Date().toISOString(),
        lastLogin: new Date().toISOString(),
        isActive: true,
      })
    } catch (userErr) {
      console.error("Error fetching OAuth2 user info:", userErr)
      setUserAccount({
        userId: profileData.userId || "Not Available",
        email: profileData.email,
        createdAt: profileData.createdAt || new Date().toISOString(),
        lastLogin: new Date().toISOString(),
        isActive: true,
      })
    }
  }, [])

  const fetchProfile = useCallback(async () => {
    try {
      const data = await authService.getEmployeeProfile()
      setProfile(data)
      setEditedProfile(data)

      if (data.employmentStatus?.toLowerCase() === 'inactive') {
        router.push('/employee/dashboard')
        return
      }

      await fetchUserAccount(data)
    } catch (err) {
      console.error("Error fetching profile:", err)
      if (err instanceof Error) {
        if (err.message.includes('Network error')) {
          setError('Unable to connect to the server. Please check your internet connection and try again.')
        } else if (err.message.includes('Session expired')) {
          setError('Your session has expired. Please log in again.')
        } else {
          setError(`Failed to load profile data: ${err.message}`)
        }
      } else {
        setError('An unexpected error occurred while loading your profile.')
      }
    } finally {
      setLoading(false)
    }
  }, [router, fetchUserAccount])

  useEffect(() => {
    fetchProfile()
  }, [fetchProfile])

  const handleInputChange = useCallback((field: keyof EmployeeProfile, value: string) => {
    if (editedProfile) {
      let formattedValue = value;
      
      // Format phone number on input
      if (field === 'phoneNumber' && value) {
        // Remove all non-digit characters and limit to 11 digits
        const cleaned = value.replace(/\D/g, '').slice(0, 11);
        
        // Format as #### ### ####
        if (cleaned.length <= 4) {
          formattedValue = cleaned;
        } else if (cleaned.length <= 7) {
          formattedValue = `${cleaned.slice(0, 4)} ${cleaned.slice(4)}`;
        } else {
          formattedValue = `${cleaned.slice(0, 4)} ${cleaned.slice(4, 7)} ${cleaned.slice(7)}`;
        }
      }
      
      // Format address on input - keep as is, no uppercase conversion
      if (field === 'address' && value) {
        formattedValue = value;
      } else if (value) {
        // Convert other fields to uppercase for storage
        formattedValue = value.toUpperCase();
      }
      
      setEditedProfile(prev => ({
        ...prev!,
        [field]: formattedValue
      }))
    }
  }, [editedProfile])

  const handleUpdateProfile = useCallback(async () => {
    if (!editedProfile) return

    try {
      setIsUpdating(true)
      const token = authService.getToken()

      if (!token) {
        toast.error("Authentication required. Please log in.")
        return
      }

      const formattedDateOfBirth = editedProfile.dateOfBirth 
        ? new Date(editedProfile.dateOfBirth).toISOString().split('T')[0]
        : null

      const formattedGender = editedProfile.gender?.toUpperCase() || null

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/employee/profile`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          employeeId: editedProfile.employeeId,
          firstName: editedProfile.firstName,
          lastName: editedProfile.lastName,
          gender: formattedGender,
          dateOfBirth: formattedDateOfBirth,
          address: editedProfile.address,
          phoneNumber: editedProfile.phoneNumber,
          maritalStatus: editedProfile.maritalStatus,
          idNumber: profile?.idNumber || "NULL",
          email: profile?.email || "NULL",
          hireDate: profile?.hireDate || "NULL",
          status: profile?.status || false,
          employmentStatus: profile?.employmentStatus || "NULL",
          departmentId: profile?.departmentId || "NULL",
          departmentName: profile?.departmentName || "NULL",
          jobId: profile?.jobId || "NULL",
          jobName: profile?.jobName || "NULL",
          roleId: profile?.roleId || "NULL",
          roleName: profile?.roleName || "NULL",
          createdAt: profile?.createdAt || "NULL",
          workTimeInSched: profile?.workTimeInSched || "00:00:00",
          workTimeOutSched: profile?.workTimeOutSched || "00:00:00"
        }),
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.message || "Failed to update profile")
      }

      setProfile(editedProfile)
      toast.success("Profile updated successfully")
      setIsEditing(false)
    } catch (error) {
      console.error("Error updating profile:", error)
      toast.error(error instanceof Error ? error.message : "Failed to update profile")
    } finally {
      setIsUpdating(false)
    }
  }, [editedProfile, profile])

  const handleEditToggle = useCallback(() => {
    if (isEditing) {
      handleUpdateProfile()
    }
    setIsEditing(prev => !prev)
  }, [isEditing, handleUpdateProfile])

  const profileFields = useMemo(() => [
    { label: "Employee ID", field: "employeeId" as keyof EmployeeProfile, type: "text" },
    { label: "ID Number", field: "idNumber" as keyof EmployeeProfile, type: "text" },
    { label: "First Name", field: "firstName" as keyof EmployeeProfile, type: "text" },
    { label: "Last Name", field: "lastName" as keyof EmployeeProfile, type: "text" },
    { label: "Gender", field: "gender" as keyof EmployeeProfile, type: "select", options: ["MALE", "FEMALE", "OTHER"] },
    { label: "Date of Birth", field: "dateOfBirth" as keyof EmployeeProfile, type: "date" },
    { label: "Address", field: "address" as keyof EmployeeProfile, type: "text" },
    { label: "Phone Number", field: "phoneNumber" as keyof EmployeeProfile, type: "text" },
    { label: "Marital Status", field: "maritalStatus" as keyof EmployeeProfile, type: "select", options: ["SINGLE", "MARRIED", "DIVORCED", "WIDOWED"] },
    { label: "Email", field: "email" as keyof EmployeeProfile, type: "text" },
    { label: "Hire Date", field: "hireDate" as keyof EmployeeProfile, type: "date" },
    { label: "Employment Status", field: "employmentStatus" as keyof EmployeeProfile, type: "select", options: ["ACTIVE", "INACTIVE"] },
    { label: "Department", field: "departmentName" as keyof EmployeeProfile, type: "text" },
    { label: "Job Title", field: "jobName" as keyof EmployeeProfile, type: "text" },
    { label: "Role", field: "roleName" as keyof EmployeeProfile, type: "text" },
    { label: "Work Time In", field: "workTimeInSched" as keyof EmployeeProfile, type: "time" },
    { label: "Work Time Out", field: "workTimeOutSched" as keyof EmployeeProfile, type: "time" }
  ], []) as ProfileField[]

  if (loading) {
    return <LoadingState />
  }

  if (error) {
    return <ErrorState error={error} />
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-[#F9FAFB] to-[#EFF6FF] p-6">
      <Toaster 
        position="top-right" 
        richColors 
        className="mt-18" 
        toastOptions={{
          style: {
            marginTop: '5rem',
          },
        }}
      />
      <div className="max-w-6xl mx-auto">
        <Header 
          isEditing={isEditing} 
          isUpdating={isUpdating} 
          onEditToggle={handleEditToggle} 
        />
        
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <ProfileSummaryCard profile={profile} />
          <DetailedInfoCards 
            profile={profile} 
            editedProfile={editedProfile} 
            isEditing={isEditing} 
            onInputChange={handleInputChange} 
            profileFields={profileFields}
          />
        </div>
      </div>
    </div>
  )
}
