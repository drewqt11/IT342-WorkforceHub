"use client"

import { useEffect, useState } from "react"
import { authService } from "@/lib/auth"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { format } from "date-fns"
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
  UserCheck,
  AlertCircle,
  Shield,
  KeyRound,
  LogIn,
  RefreshCw,
} from "lucide-react"

interface EmployeeProfile {
  employeeId: string
  idNumber: string
  firstName: string
  lastName: string
  email: string
  gender: string
  hireDate: string
  dateOfBirth: string
  address: string
  phoneNumber: string
  maritalStatus: string
  status: boolean
  employmentStatus: string
  departmentId: string
  departmentName: string
  jobId: string
  jobName: string
  roleId: string
  roleName: string
  createdAt: string
}

interface UserAccountInfo {
  userId: string
  email: string
  createdAt: string
  lastLogin: string
  isActive: boolean
}

export default function EmployeeProfile() {
  const [profile, setProfile] = useState<EmployeeProfile | null>(null)
  const [userAccount, setUserAccount] = useState<UserAccountInfo | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await authService.getEmployeeProfile()
        setProfile(data)

        // Fetch user account info using the OAuth2 user info endpoint
        try {
          const userData = await authService.getOAuth2UserInfo()

          // Map the OAuth2 user info to UserAccountInfo
          setUserAccount({
            userId: userData.userId || "N/A",
            email: userData.email || data.email,
            createdAt: data.createdAt || new Date().toISOString(), // Use actual createdAt from profile
            lastLogin: new Date().toISOString(), // Default value since not in OAuth2 user info
            isActive: true, // Default value since not in OAuth2 user info
          })
        } catch (userErr) {
          console.error("Error fetching OAuth2 user info:", userErr)
          // Fallback to creating UserAccountInfo from profile data
          if (data) {
            setUserAccount({
              userId: data.userId || "N/A",
              email: data.email,
              createdAt: data.createdAt || new Date().toISOString(), // Use actual createdAt from profile
              lastLogin: new Date().toISOString(), // Default value
              isActive: true, // Default value
            })
          }
        }
      } catch (err) {
        console.error("Error fetching profile:", err)
        if (err instanceof Error) {
          if (err.message.includes('Network error')) {
            setError('Unable to connect to the server. Please check your internet connection and try again.');
          } else if (err.message.includes('Session expired')) {
            setError('Your session has expired. Please log in again.');
          } else {
            setError(`Failed to load profile data: ${err.message}`);
          }
        } else {
          setError('An unexpected error occurred while loading your profile.');
        }
      } finally {
        setLoading(false)
      }
    }

    fetchProfile()
  }, [])

  if (loading) {
    return (
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
  }

  if (error) {
    return (
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
  }

  return (
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
          {/* Profile Summary Card */}
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
                  <h2 className="text-xl font-bold text-[#1F2937]">
                    {profile?.firstName} {profile?.lastName}
                  </h2>
                  <p className="text-[#6B7280]">{profile?.jobName || "N/A"}</p>
                </div>

                <div className="space-y-4">
                  <div className="flex items-center">
                    <div className="h-8 w-8 rounded-full bg-[#EFF6FF] flex items-center justify-center flex-shrink-0 mr-3">
                      <Mail className="h-4 w-4 text-[#3B82F6]" />
                    </div>
                    <div className="text-sm text-[#6B7280] truncate">{profile?.email}</div>
                  </div>

                  <div className="flex items-center">
                    <div className="h-8 w-8 rounded-full bg-[#F0FDFA] flex items-center justify-center flex-shrink-0 mr-3">
                      <Phone className="h-4 w-4 text-[#14B8A6]" />
                    </div>
                    <div className="text-sm text-[#6B7280]">{profile?.phoneNumber || "N/A"}</div>
                  </div>

                  <div className="flex items-center">
                    <div className="h-8 w-8 rounded-full bg-[#EFF6FF] flex items-center justify-center flex-shrink-0 mr-3">
                      <MapPin className="h-4 w-4 text-[#3B82F6]" />
                    </div>
                    <div className="text-sm text-[#6B7280] truncate">{profile?.address || "N/A"}</div>
                  </div>

                  <div className="flex items-center">
                    <div className="h-8 w-8 rounded-full bg-[#F0FDFA] flex items-center justify-center flex-shrink-0 mr-3">
                      <Building2 className="h-4 w-4 text-[#14B8A6]" />
                    </div>
                    <div className="text-sm text-[#6B7280]">{profile?.departmentName || "N/A"}</div>
                  </div>
                </div>

                <div className="mt-6 pt-6 border-t border-[#E5E7EB]">
                  <div className="grid grid-cols-3 gap-2">
                    <div className="text-center">
                      <div className="h-10 w-10 rounded-full bg-[#EFF6FF] flex items-center justify-center mx-auto mb-2">
                        <UserCheck className="h-5 w-5 text-[#3B82F6]" />
                      </div>
                      <div className="text-xs font-medium text-[#6B7280]">Status</div>
                      <div
                        className={`mt-1 text-xs font-semibold px-2 py-1 rounded-full inline-block ${
                          profile?.status === true ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800"
                        }`}
                      >
                        {profile?.status === true ? "Active" : "Inactive"}
                      </div>
                    </div>

                    <div className="text-center">
                      <div className="h-10 w-10 rounded-full bg-[#F0FDFA] flex items-center justify-center mx-auto mb-2">
                        <Clock className="h-5 w-5 text-[#14B8A6]" />
                      </div>
                      <div className="text-xs font-medium text-[#6B7280]">Employment</div>
                      <div className="mt-1 text-xs font-semibold text-[#1F2937]">
                        {profile?.employmentStatus || "N/A"}
                      </div>
                    </div>

                    <div className="text-center">
                      <div className="h-10 w-10 rounded-full bg-[#EFF6FF] flex items-center justify-center mx-auto mb-2">
                        <Award className="h-5 w-5 text-[#3B82F6]" />
                      </div>
                      <div className="text-xs font-medium text-[#6B7280]">Role</div>
                      <div className="mt-1 text-xs font-semibold text-[#1F2937]">{profile?.roleName || "N/A"}</div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Detailed Information Cards */}
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
                    <div className="grid grid-cols-3 border-b border-dashed border-[#E5E7EB] pb-3">
                      <div className="col-span-1 text-sm font-medium text-[#6B7280]">Employee ID</div>
                      <div className="col-span-2 text-sm text-[#1F2937] font-semibold">
                        {profile?.employeeId || "N/A"}
                      </div>
                    </div>

                    <div className="grid grid-cols-3 border-b border-dashed border-[#E5E7EB] pb-3">
                      <div className="col-span-1 text-sm font-medium text-[#6B7280]">ID Number</div>
                      <div className="col-span-2 text-sm text-[#1F2937] font-semibold">
                        {profile?.idNumber || "N/A"}
                      </div>
                    </div>

                    <div className="grid grid-cols-3 border-b border-dashed border-[#E5E7EB] pb-3">
                      <div className="col-span-1 text-sm font-medium text-[#6B7280]">Full Name</div>
                      <div className="col-span-2 text-sm text-[#1F2937] font-semibold">
                        {profile?.firstName} {profile?.lastName}
                      </div>
                    </div>

                    <div className="grid grid-cols-3 border-b border-dashed border-[#E5E7EB] pb-3">
                      <div className="col-span-1 text-sm font-medium text-[#6B7280]">Gender</div>
                      <div className="col-span-2 text-sm text-[#1F2937] font-semibold">{profile?.gender || "N/A"}</div>
                    </div>

                    <div className="grid grid-cols-3 border-b border-dashed border-[#E5E7EB] pb-3">
                      <div className="col-span-1 text-sm font-medium text-[#6B7280]">Date of Birth</div>
                      <div className="col-span-2 text-sm text-[#1F2937] font-semibold flex items-center">
                        <Calendar className="h-3.5 w-3.5 mr-1.5 text-[#3B82F6]" />
                        {profile?.dateOfBirth ? format(new Date(profile.dateOfBirth), "MMMM d, yyyy") : "N/A"}
                      </div>
                    </div>

                    <div className="grid grid-cols-3 border-b border-dashed border-[#E5E7EB] pb-3">
                      <div className="col-span-1 text-sm font-medium text-[#6B7280]">Marital Status</div>
                      <div className="col-span-2 text-sm text-[#1F2937] font-semibold flex items-center">
                        <Heart className="h-3.5 w-3.5 mr-1.5 text-[#3B82F6]" />
                        {profile?.maritalStatus || "N/A"}
                      </div>
                    </div>
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
                    <div className="grid grid-cols-3 border-b border-dashed border-[#E5E7EB] pb-3">
                      <div className="col-span-1 text-sm font-medium text-[#6B7280]">Status</div>
                      <div className="col-span-2">
                        <span
                          className={`px-2 py-1 rounded-full text-xs font-medium ${
                            profile?.status === true
                              ? "bg-green-100 text-green-800 border border-green-200"
                              : "bg-red-100 text-red-800 border border-red-200"
                          }`}
                        >
                          {profile?.status === true ? "Active" : "Inactive"}
                        </span>
                      </div>
                    </div>

                    <div className="grid grid-cols-3 border-b border-dashed border-[#E5E7EB] pb-3">
                      <div className="col-span-1 text-sm font-medium text-[#6B7280]">Employment</div>
                      <div className="col-span-2 text-sm text-[#1F2937] font-semibold">
                        {profile?.employmentStatus || "N/A"}
                      </div>
                    </div>

                    <div className="grid grid-cols-3 border-b border-dashed border-[#E5E7EB] pb-3">
                      <div className="col-span-1 text-sm font-medium text-[#6B7280]">Hire Date</div>
                      <div className="col-span-2 text-sm text-[#1F2937] font-semibold flex items-center">
                        <Calendar className="h-3.5 w-3.5 mr-1.5 text-[#14B8A6]" />
                        {profile?.hireDate ? format(new Date(profile.hireDate), "MMMM d, yyyy") : "N/A"}
                      </div>
                    </div>

                    <div className="grid grid-cols-3 border-b border-dashed border-[#E5E7EB] pb-3">
                      <div className="col-span-1 text-sm font-medium text-[#6B7280]">Department</div>
                      <div className="col-span-2 text-sm text-[#1F2937] font-semibold flex items-center">
                        <Building2 className="h-3.5 w-3.5 mr-1.5 text-[#14B8A6]" />
                        {profile?.departmentName || "N/A"}
                      </div>
                    </div>

                    <div className="grid grid-cols-3 border-b border-dashed border-[#E5E7EB] pb-3">
                      <div className="col-span-1 text-sm font-medium text-[#6B7280]">Job Title</div>
                      <div className="col-span-2 text-sm text-[#1F2937] font-semibold flex items-center">
                        <Briefcase className="h-3.5 w-3.5 mr-1.5 text-[#14B8A6]" />
                        {profile?.jobName || "N/A"}
                      </div>
                    </div>

                    <div className="grid grid-cols-3 border-b border-dashed border-[#E5E7EB] pb-3">
                      <div className="col-span-1 text-sm font-medium text-[#6B7280]">Role</div>
                      <div className="col-span-2 text-sm text-[#1F2937] font-semibold flex items-center">
                        <Award className="h-3.5 w-3.5 mr-1.5 text-[#14B8A6]" />
                        {profile?.roleName || "N/A"}
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Account Information Card */}
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
                    <div className="grid grid-cols-3 border-b border-dashed border-[#E5E7EB] pb-3">
                      <div className="col-span-1 text-sm font-medium text-[#6B7280]">User ID</div>
                      <div className="col-span-2 text-sm text-[#1F2937] font-semibold flex items-center">
                        <KeyRound className="h-3.5 w-3.5 mr-1.5 text-[#3B82F6]" />
                        {userAccount?.userId || "N/A"}
                      </div>
                    </div>

                    <div className="grid grid-cols-3 border-b border-dashed border-[#E5E7EB] pb-3">
                      <div className="col-span-1 text-sm font-medium text-[#6B7280]">Account Status</div>
                      <div className="col-span-2">
                        <span
                          className={`px-2 py-1 rounded-full text-xs font-medium ${
                            userAccount?.isActive
                              ? "bg-green-100 text-green-800 border border-green-200"
                              : "bg-red-100 text-red-800 border border-red-200"
                          }`}
                        >
                          {userAccount?.isActive ? "Active" : "Inactive"}
                        </span>
                      </div>
                    </div>

                    <div className="grid grid-cols-3 border-b border-dashed border-[#E5E7EB] pb-3">
                      <div className="col-span-1 text-sm font-medium text-[#6B7280]">Account Created</div>
                      <div className="col-span-2 text-sm text-[#1F2937] font-semibold flex items-center">
                        <Calendar className="h-3.5 w-3.5 mr-1.5 text-[#3B82F6]" />
                        {userAccount?.createdAt
                          ? format(new Date(userAccount.createdAt), "MMMM d, yyyy h:mm a")
                          : "N/A"}
                      </div>
                    </div>

                    <div className="grid grid-cols-3 border-b border-dashed border-[#E5E7EB] pb-3">
                      <div className="col-span-1 text-sm font-medium text-[#6B7280]">Last Login</div>
                      <div className="col-span-2 text-sm text-[#1F2937] font-semibold flex items-center">
                        <LogIn className="h-3.5 w-3.5 mr-1.5 text-[#3B82F6]" />
                        {userAccount?.lastLogin
                          ? format(new Date(userAccount.lastLogin), "MMMM d, yyyy h:mm a")
                          : "N/A"}
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
