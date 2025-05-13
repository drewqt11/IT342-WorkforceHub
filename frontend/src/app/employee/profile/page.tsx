"use client"

import type React from "react"

import { useEffect, useState, useCallback, useMemo, useRef } from "react"
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
  Clock,
  Building2,
  Award,
  AlertCircle,
  Shield,
  LogIn,
  RefreshCw,
  Clock10,
  Pencil,
  FileText,
  Upload,
  File,
  FolderOpen,
  Download,
  Eye,
  X,
  ZoomIn,
  ZoomOut,
  RotateCw,
  Maximize2,
  Minimize2,
  ExternalLink,
} from "lucide-react"
import { toast, Toaster } from "sonner"
import { Dialog, DialogContent, DialogClose, DialogTitle, DialogHeader } from "@/components/ui/dialog"

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
  lastLogin: string
  isActive: boolean
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
  type: "text" | "date" | "select"
  options?: string[]
  readOnly?: boolean
  noFormat?: boolean
}

interface DocumentViewerProps {
  documentId: string
  documentType: string
  isOpen: boolean
  onClose: () => void
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
    const cleaned = text.replace(/\D/g, "")
    if (cleaned.length === 10) {
      return `(${cleaned.slice(0, 3)}) ${cleaned.slice(3, 6)}-${cleaned.slice(6)}`
    }
    return text
  }

  // Split by spaces, underscores, and hyphens
  return text
    .split(/[\s_-]+/)
    .map((word) => {
      // Handle special cases for words like "ID", "HR", etc.
      if (word.toUpperCase() === word && word.length <= 2) {
        return word
      }
      return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
    })
    .join(" ")
}

const formatTime = (time: string | null | undefined): string => {
  if (!time || time === "NULL") return "Not Provided"
  try {
    const [hours, minutes] = time.split(":")
    const hourNum = Number.parseInt(hours)
    const minNum = Number.parseInt(minutes)

    // Validate time components
    if (isNaN(hourNum) || isNaN(minNum) || hourNum < 0 || hourNum > 23 || minNum < 0 || minNum > 59) {
      return "Not Provided"
    }

    const period = hourNum >= 12 ? "PM" : "AM"
    const standardHour = hourNum % 12 || 12 // Convert 0 to 12 for 12 AM
    const formattedMinutes = minNum.toString().padStart(2, "0") // Ensure two digits for minutes

    return `${standardHour}:${formattedMinutes} ${period}`
  } catch {
    return "Not Provided"
  }
}

const formatDisplayValue = (
  value: string | null | undefined,
  type: "text" | "date" | "time" | "datetime" = "text",
): string => {
  if (!value || value === "NULL") return "Not Provided"

  switch (type) {
    case "date":
      return format(new Date(value), "MMMM d, yyyy")
    case "time":
      return formatTime(value)
    case "datetime":
      return format(new Date(value), "MMMM d, yyyy 'at' h:mm a")
    default:
      return formatText(value)
  }
}

const DocumentViewer = ({ documentId, documentType, isOpen, onClose }: DocumentViewerProps) => {
  const [documentUrl, setDocumentUrl] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [zoom, setZoom] = useState(1)
  const [rotation, setRotation] = useState(0)
  const [fileType, setFileType] = useState<string | null>(null)
  const [isFullscreen, setIsFullscreen] = useState(false)
  const contentRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const fetchDocument = async () => {
      if (!documentId || !isOpen) return

      try {
        setLoading(true)
        const token = authService.getToken()
        if (!token) {
          throw new Error("Authentication required")
        }

        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/documents/${documentId}/view`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        })

        if (!response.ok) {
          throw new Error("Failed to fetch document")
        }

        const blob = await response.blob()
        const contentType = blob.type
        setFileType(contentType)
        const url = window.URL.createObjectURL(blob)
        setDocumentUrl(url)
      } catch (error) {
        console.error("Error fetching document:", error)
        setError(error instanceof Error ? error.message : "Failed to load document")
      } finally {
        setLoading(false)
      }
    }

    fetchDocument()

    return () => {
      if (documentUrl) {
        window.URL.revokeObjectURL(documentUrl)
      }
    }
  }, [documentId, isOpen])

  const handleZoomIn = () => {
    setZoom((prev) => Math.min(prev + 0.25, 3))
  }

  const handleZoomOut = () => {
    setZoom((prev) => Math.max(prev - 0.25, 0.5))
  }

  const handleRotate = () => {
    setRotation((prev) => (prev + 90) % 360)
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

  const renderContent = () => {
    if (loading) {
      return (
        <div className="flex items-center justify-center h-full">
          <div className="h-8 w-8 rounded-full border-4 border-[#3B82F6] border-t-transparent animate-spin"></div>
        </div>
      )
    }

    if (error) {
      return (
        <div className="flex flex-col items-center justify-center h-full text-center">
          <AlertCircle className="h-12 w-12 text-red-500 mb-4" />
          <h3 className="text-lg font-semibold text-[#1F2937] mb-2">Error Loading Document</h3>
          <p className="text-[#6B7280]">{error}</p>
        </div>
      )
    }

    if (!documentUrl) {
      return (
        <div className="flex flex-col items-center justify-center h-full text-center">
          <File className="h-12 w-12 text-[#6B7280] mb-4" />
          <h3 className="text-lg font-semibold text-[#1F2937] mb-2">No Document Available</h3>
          <p className="text-[#6B7280]">The document could not be loaded.</p>
        </div>
      )
    }

    if (fileType?.startsWith("image/")) {
      return (
        <div className="flex justify-center items-center h-full">
          <img
            src={documentUrl || "/placeholder.svg"}
            alt={documentType}
            className="max-w-full max-h-full object-contain"
            style={{
              transform: `scale(${zoom}) rotate(${rotation}deg)`,
              transition: "transform 0.3s ease",
            }}
          />
        </div>
      )
    } else if (fileType?.includes("pdf")) {
      return (
        <div
          className="h-full"
          style={{
            transform: `scale(${zoom})`,
            transformOrigin: "center center",
            transition: "transform 0.3s ease",
          }}
        >
          <iframe
            src={`${documentUrl}#view=FitH`}
            className="w-full h-full"
            title={documentType}
          />
        </div>
      )
    } else {
      return (
        <div className="flex flex-col items-center justify-center h-full text-center">
          <FileText className="h-12 w-12 text-[#3B82F6] mb-4" />
          <h3 className="text-lg font-semibold text-[#1F2937] mb-2">Document Preview Not Available</h3>
          <p className="text-[#6B7280] mb-4">This file type cannot be previewed directly.</p>
          <Button
            onClick={() => documentUrl && window.open(documentUrl, "_blank")}
            className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white"
          >
            <Download className="h-4 w-4 mr-2" />
            Download to View
          </Button>
        </div>
      )
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent fullWidth className="w-[90vw] h-[90vh] p-0 overflow-hidden rounded-2xl border-none">
        <DialogHeader className="flex items-center justify-between bg-white p-4 border-b">
        <div className="flex items-center justify-between w-full">
          <DialogTitle className="text-xl font-semibold text-[#1F2937]">{formatText(documentType)}</DialogTitle>
          <div className="grid grid-cols-5 items-center gap-2 mr-10">
            <Button variant="outline" size="sm" onClick={handleZoomIn} title="Zoom In">
              <ZoomIn className="h-4 w-4" />
            </Button>
            <Button variant="outline" size="sm" onClick={handleZoomOut} title="Zoom Out">
              <ZoomOut className="h-4 w-4" />
            </Button>
            <Button variant="outline" size="sm" onClick={handleRotate} title="Rotate">
              <RotateCw className="h-4 w-4" />
            </Button>
            <Button variant="outline" size="sm" onClick={toggleFullscreen} title={isFullscreen ? "Exit Fullscreen" : "View in Fullscreen"}>
              {isFullscreen ? <Minimize2 className="h-4 w-4" /> : <Maximize2 className="h-4 w-4" />}
            </Button>
            <Button 
              variant="outline" 
              size="sm" 
              onClick={() => documentUrl && window.open(documentUrl, '_blank')} 
              title="Open in New Tab"
              disabled={!documentUrl}
            >
              <ExternalLink className="h-4 w-4" />
            </Button>
          </div>
        </div>
        </DialogHeader>
        <div 
          ref={contentRef} 
          className="flex-1 overflow-auto bg-[#F9FAFB]"
          style={{ height: isFullscreen ? 'calc(100vh - 73px)' : 'calc(90vh - 73px)' }}
        >
          {renderContent()}
        </div>
      </DialogContent>
    </Dialog>
  )
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

const Header = ({
  isEditing,
  isUpdating,
  onEditToggle,
}: { isEditing: boolean; isUpdating: boolean; onEditToggle: () => void }) => (
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
            <div className="h-4 w-4 rounded-full border-2 border-current border-t-transparent animate-spin"></div>
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
              <div className="mt-1 text-xs text-[#1F2937]">{formatDisplayValue(profile?.employmentStatus)}</div>
            </div>

            <div className="text-center">
              <div className="h-10 w-10 rounded-full bg-[#EFF6FF] flex items-center justify-center mx-auto mb-2">
                <Award className="h-5 w-5 text-[#3B82F6]" />
              </div>
              <div className="text-xs text-[#6B7280]">Role</div>
              <div className="mt-1 text-xs text-[#1F2937]">{formatDisplayValue(profile?.roleName)}</div>
            </div>

            <div className="text-center">
              <div className="h-10 w-10 rounded-full bg-[#EFF6FF] flex items-center justify-center mx-auto mb-2">
                <Clock10 className="h-5 w-5 text-[#3B82F6]" />
              </div>
              <div className="text-xs text-[#6B7280]">Work Schedule</div>
              <div className="mt-1 text-xs text-[#1F2937]">
                {formatDisplayValue(profile?.workTimeInSched, "time")} -{" "}
                {formatDisplayValue(profile?.workTimeOutSched, "time")}
              </div>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
)

const AccountInfoCard = ({ profile }: { profile: EmployeeProfile | null }) => (
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
            <span
              className={`px-2 py-1 rounded-full text-xs ${
                profile?.isActive
                  ? "bg-green-100 text-green-800 border border-green-200"
                  : "bg-red-100 text-red-800 border border-red-200"
              }`}
            >
              {profile?.isActive ? "Active" : "Inactive"}
            </span>
          </div>
        </div>

        <div className="grid grid-cols-2 border-b border-dashed border-[#E5E7EB] pb-3">
          <div className="text-sm text-[#6B7280]">Account Created</div>
          <div className="text-sm text-[#6B7280] flex items-center">
            <Calendar className="h-3.5 w-3.5 mr-1.5 text-[#3B82F6]" />
            {formatDisplayValue(profile?.createdAt, "date")}
          </div>
        </div>

        <div className="grid grid-cols-2 border-b border-dashed border-[#E5E7EB] pb-3">
          <div className="text-sm text-[#6B7280]">Last Login</div>
          <div className="text-sm text-[#6B7280] flex items-center">
            <LogIn className="h-3.5 w-3.5 mr-1.5 text-[#3B82F6]" />
            {formatDisplayValue(profile?.lastLogin, "datetime")}
          </div>
        </div>
      </div>
    </CardContent>
  </Card>
)

const DetailedInfoCards = ({
  profile,
  editedProfile,
  isEditing,
  onInputChange,
  profileFields,
}: {
  profile: EmployeeProfile | null
  editedProfile: EmployeeProfile | null
  isEditing: boolean
  onInputChange: (field: keyof EmployeeProfile, value: string) => void
  profileFields: ProfileField[]
}) => {
  // Filter out employment-related fields from profileFields
  const personalFields = profileFields.filter(
    (field) => !["hireDate", "employmentStatus", "departmentName", "jobName", "roleName"].includes(field.field),
  )

  return (
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
              {personalFields.map((field) => (
                <div key={field.field} className="grid grid-cols-2 border-b border-dashed border-[#E5E7EB] pb-3">
                  <div className="text-sm text-[#6B7280]">{field.label}</div>
                  <div className="text-sm text-[#6B7280]">
                    {isEditing && !field.readOnly ? (
                      field.type === "select" ? (
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
                      ) : field.type === "date" ? (
                        <Input
                          type="date"
                          value={editedProfile?.[field.field]?.toString().split("T")[0] || ""}
                          onChange={(e) => onInputChange(field.field, e.target.value)}
                          className="text-sm text-[#6B7280]"
                        />
                      ) : field.field === "address" ? (
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
                        {field.noFormat
                          ? profile?.[field.field]?.toString()
                          : formatDisplayValue(
                              profile?.[field.field]?.toString(),
                              field.type === "date" ? "date" : "text",
                            )}
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
                <div className="text-sm text-[#6B7280]">{formatDisplayValue(profile?.employmentStatus)}</div>
              </div>

              <div className="grid grid-cols-2 border-b border-dashed border-[#E5E7EB] pb-3">
                <div className="text-sm text-[#6B7280]">Hire Date</div>
                <div className="text-sm text-[#6B7280] flex items-center">
                  <Calendar className="h-3.5 w-3.5 mr-1.5 text-[#14B8A6]" />
                  {formatDisplayValue(profile?.hireDate, "date")}
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

              <div className="grid grid-cols-2 border-b border-dashed border-[#E5E7EB] pb-3">
                <div className="text-sm text-[#6B7280]">Work Schedule</div>
                <div className="text-sm text-[#6B7280] flex items-center">
                  <Clock className="h-3.5 w-3.5 mr-1.5 text-[#14B8A6]" />
                  {formatDisplayValue(profile?.workTimeInSched, "time")} -{" "}
                  {formatDisplayValue(profile?.workTimeOutSched, "time")}
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

const DocumentCard = ({
  title,
  isUploading,
  onUpload,
  documentId,
  onDownload,
  onView,
  onReplace,
}: {
  title: string
  isUploading: boolean
  onUpload: () => void
  documentId?: string
  onDownload?: () => void
  onView?: () => void
  onReplace?: () => void
}) => {
  const [isHovered, setIsHovered] = useState(false)

  return (
    <div
      className={`relative overflow-hidden rounded-lg border ${isHovered ? "border-[#3B82F6]" : "border-[#E5E7EB]"} transition-all duration-200 shadow-sm hover:shadow-md`}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div className="flex items-center p-4">
        <div
          className={`h-10 w-10 rounded-lg ${documentId ? "bg-green-100" : "bg-[#F3F4F6]"} flex items-center justify-center mr-3 transition-colors duration-300`}
        >
          {documentId ? (
            <FolderOpen
              className={`h-5 w-5 ${isHovered ? "text-[#3B82F6]" : "text-green-600"} transition-colors duration-300`}
            />
          ) : (
            <File
              className={`h-5 w-5 ${isHovered ? "text-[#3B82F6]" : "text-[#6B7280]"} transition-colors duration-300`}
            />
          )}
        </div>

        <div className="flex-1 min-w-0">
          <h4 className="text-sm font-medium text-[#1F2937] truncate">{title}</h4>
          <p className="text-xs text-[#6B7280]">{documentId ? "Uploaded" : "Not uploaded yet"}</p>
        </div>

        {documentId ? (
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={onView}
              className="transition-all duration-300 border-[#3B82F6] text-[#3B82F6] hover:bg-[#EFF6FF]"
            >
              <div className="flex items-center gap-1">
                <Eye className="h-3 w-3" />
                View
              </div>
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={onReplace}
              className="transition-all duration-300 border-[#3B82F6] text-[#3B82F6] hover:bg-[#EFF6FF]"
            >
              <div className="flex items-center gap-1">
                <Upload className="h-3 w-3" />
                Replace
              </div>
            </Button>
            <Button
              variant="default"
              size="sm"
              onClick={onDownload}
              className="transition-all duration-300 bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white"
            >
              <div className="flex items-center gap-1">
                <Download className="h-3 w-3" />
              </div>
            </Button>
          </div>
        ) : (
          <Button
            variant="default"
            size="sm"
            onClick={onUpload}
            disabled={isUploading}
            className="ml-2 transition-all duration-300 bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white"
          >
            {isUploading ? (
              <div className="flex items-center gap-1">
                <div className="h-3 w-3 rounded-full border-2 border-current border-t-transparent animate-spin"></div>
                <span>Uploading...</span>
              </div>
            ) : (
              <div className="flex items-center gap-1">
                <Upload className="h-3 w-3" />
                Upload
              </div>
            )}
          </Button>
        )}
      </div>
    </div>
  )
}

const DocumentsCard = ({ profile }: { profile: EmployeeProfile | null }) => {
  const [uploadingDoc, setUploadingDoc] = useState<string | null>(null)
  const [documents, setDocuments] = useState<Record<string, string>>({})
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [viewingDocument, setViewingDocument] = useState<{ id: string; type: string } | null>(null)

  // Fetch existing documents
  useEffect(() => {
    const fetchDocuments = async () => {
      if (!profile?.employeeId) return

      try {
        const token = authService.getToken()
        if (!token) return

        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/employees/${profile.employeeId}/documents`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        })

        if (!response.ok) {
          throw new Error("Failed to fetch documents")
        }

        const data = await response.json()
        const documentMap = data.reduce((acc: Record<string, string>, doc: any) => {
          acc[doc.documentType] = doc.documentId
          return acc
        }, {})
        setDocuments(documentMap)
      } catch (error) {
        console.error("Error fetching documents:", error)
        toast.error("Failed to fetch documents")
      }
    }

    fetchDocuments()
  }, [profile?.employeeId])

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    try {
      setUploadingDoc(uploadingDoc)
      const formData = new FormData()
      formData.append("file", file)
      formData.append("documentType", uploadingDoc || "")
      formData.append("employeeId", profile?.employeeId || "")

      const token = authService.getToken()
      if (!token) {
        toast.error("Authentication required. Please log in.", { id: "upload" })
        setUploadingDoc(null)
        return
      }

      // If we're replacing a document, use PUT endpoint
      if (documents[uploadingDoc || ""]) {
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/documents/${documents[uploadingDoc || ""]}`, {
          method: "PUT",
          headers: {
            Authorization: `Bearer ${token}`,
          },
          body: formData,
        })

        if (!response.ok) {
          const errorData = await response.json()
          throw new Error(errorData.message || "Failed to replace document")
        }

        const data = await response.json()
        toast.success(`${uploadingDoc} replaced successfully`, { id: "upload" })
      } else {
        // Otherwise use POST for new upload
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/employees/${profile?.employeeId}/documents`, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
          body: formData,
        })

        if (!response.ok) {
          const errorData = await response.json()
          throw new Error(errorData.message || "Failed to upload document")
        }

        const data = await response.json()
        setDocuments((prev) => ({
          ...prev,
          [uploadingDoc || ""]: data.documentId,
        }))
        toast.success(`${uploadingDoc} uploaded successfully`, { id: "upload" })
      }

      if (fileInputRef.current) {
        fileInputRef.current.value = ""
      }
    } catch (error) {
      console.error("Error handling document:", error)
      toast.error(error instanceof Error ? error.message : "Failed to handle document", { id: "upload" })
    } finally {
      setUploadingDoc(null)
    }
  }

  const handleReplace = (documentId: string, documentType: string) => {
    setUploadingDoc(documentType)
    fileInputRef.current?.click()
  }

  const handleFileUpload = (type: string) => {
    setUploadingDoc(type)
    fileInputRef.current?.click()
  }

  const handleView = (documentId: string, documentType: string) => {
    setViewingDocument({ id: documentId, type: documentType })
  }

  const handleDownload = async (documentId: string, documentType: string) => {
    try {
      const token = authService.getToken()
      if (!token) {
        toast.error("Authentication required. Please log in.")
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/documents/${documentId}/download`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        throw new Error("Failed to download document")
      }

      const blob = await response.blob()
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement("a")
      a.href = url
      
      // Get file extension from blob type
      const fileExtension = blob.type.split("/")[1] || "pdf"
      
      // Format the filename: "Employee FirstName LastName - Document Type"
      const employeeName = profile ? `${profile.firstName} ${profile.lastName}` : "Employee"
      const fileName = `${employeeName} - ${documentType}.${fileExtension}`
      
      a.download = fileName
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
      
      toast.success("Document downloaded successfully")
    } catch (error) {
      console.error("Error downloading document:", error)
      toast.error("Failed to download document")
    }
  }

  const documentCategories = [
    {
      title: "Personal",
      icon: <User className="h-4 w-4 mr-2 text-[#3B82F6]" />,
      documents: ["Resume/Curriculum Vitae", "Birth Certificate", "Government Issue ID"],
    },
    {
      title: "Government Related",
      icon: <Shield className="h-4 w-4 mr-2 text-[#3B82F6]" />,
      sections: [
        {
          subtitle: "BIR",
          documents: ["BIR Form 1902", "BIR TAX Identification Number", "BIR Form 2316"],
        },
        {
          documents: ["SSS ID", "Philhealth ID", "PAG-IBIG Membership ID"],
        },
      ],
    },
    {
      title: "Company",
      icon: <Building2 className="h-4 w-4 mr-2 text-[#3B82F6]" />,
      documents: ["Confidentiality Agreement", "Employment Contract"],
    },
  ]

  return (
    <div className="mt-6">
      <Card className="border border-[#E5E7EB] shadow-md bg-white hover:shadow-lg transition-shadow duration-300">
        <CardHeader className="pb-2 border-b border-[#E5E7EB]">
          <CardTitle className="flex items-center text-[#1F2937]">
            <FileText className="h-5 w-5 mr-2 text-[#3B82F6]" />
            Documents
          </CardTitle>
        </CardHeader>
        <CardContent className="pt-4">
          <input
            type="file"
            ref={fileInputRef}
            className="hidden"
            onChange={handleFileChange}
            accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
          />

          <div className="space-y-8">
            {documentCategories.map((category, idx) => (
              <div key={idx} className="animate-in fade-in duration-500" style={{ animationDelay: `${idx * 100}ms` }}>
                <h3 className="text-sm font-semibold text-[#1F2937] mb-4 flex items-center">
                  {category.icon}
                  {category.title}
                </h3>

                {category.sections ? (
                  <>
                    {category.sections.map((section, sectionIdx) => (
                      <div key={sectionIdx} className="mb-4">
                        {section.subtitle && (
                          <h4 className="text-xs font-medium text-[#6B7280] mb-2">{section.subtitle}</h4>
                        )}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                          {section.documents.map((doc) => (
                            <DocumentCard
                              key={doc}
                              title={doc}
                              isUploading={uploadingDoc === doc}
                              onUpload={() => handleFileUpload(doc)}
                              documentId={documents[doc]}
                              onDownload={documents[doc] ? () => handleDownload(documents[doc], doc) : undefined}
                              onView={documents[doc] ? () => handleView(documents[doc], doc) : undefined}
                              onReplace={documents[doc] ? () => handleReplace(documents[doc], doc) : undefined}
                            />
                          ))}
                        </div>
                      </div>
                    ))}
                  </>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    {category.documents.map((doc) => (
                      <DocumentCard
                        key={doc}
                        title={doc}
                        isUploading={uploadingDoc === doc}
                        onUpload={() => handleFileUpload(doc)}
                        documentId={documents[doc]}
                        onDownload={documents[doc] ? () => handleDownload(documents[doc], doc) : undefined}
                        onView={documents[doc] ? () => handleView(documents[doc], doc) : undefined}
                        onReplace={documents[doc] ? () => handleReplace(documents[doc], doc) : undefined}
                      />
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>

          {viewingDocument && (
            <DocumentViewer
              documentId={viewingDocument.id}
              documentType={viewingDocument.type}
              isOpen={!!viewingDocument}
              onClose={() => setViewingDocument(null)}
            />
          )}
        </CardContent>
      </Card>
    </div>
  )
}

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

      if (data.employmentStatus?.toLowerCase() === "inactive") {
        router.push("/employee/dashboard")
        return
      }

      await fetchUserAccount(data)
    } catch (err) {
      console.error("Error fetching profile:", err)
      if (err instanceof Error) {
        if (err.message.includes("Network error")) {
          setError("Unable to connect to the server. Please check your internet connection and try again.")
        } else if (err.message.includes("Session expired")) {
          setError("Your session has expired. Please log in again.")
        } else {
          setError(`Failed to load profile data: ${err.message}`)
        }
      } else {
        setError("An unexpected error occurred while loading your profile.")
      }
    } finally {
      setLoading(false)
    }
  }, [router, fetchUserAccount])

  useEffect(() => {
    fetchProfile()
  }, [fetchProfile])

  const handleInputChange = useCallback(
    (field: keyof EmployeeProfile, value: string) => {
      if (editedProfile) {
        let formattedValue = value

        // Format phone number on input
        if (field === "phoneNumber" && value) {
          // Remove all non-digit characters and limit to 11 digits
          const cleaned = value.replace(/\D/g, "").slice(0, 11)

          // Format as #### ### ####
          if (cleaned.length <= 4) {
            formattedValue = cleaned
          } else if (cleaned.length <= 7) {
            formattedValue = `${cleaned.slice(0, 4)} ${cleaned.slice(4)}`
          } else {
            formattedValue = `${cleaned.slice(0, 4)} ${cleaned.slice(4, 7)} ${cleaned.slice(7)}`
          }
        }

        // Format address on input - keep as is, no uppercase conversion
        if (field === "address" && value) {
          formattedValue = value
        } else if (value) {
          // Convert other fields to uppercase for storage
          formattedValue = value.toUpperCase()
        }

        setEditedProfile((prev) => ({
          ...prev!,
          [field]: formattedValue,
        }))
      }
    },
    [editedProfile],
  )

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
        ? new Date(editedProfile.dateOfBirth).toISOString().split("T")[0]
        : null

      const formattedGender = editedProfile.gender?.toUpperCase() || null

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/employee/${editedProfile.employeeId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          employeeId: profile?.employeeId,
          firstName: profile?.firstName,
          lastName: profile?.lastName,
          gender: formattedGender,
          dateOfBirth: formattedDateOfBirth,
          address: editedProfile.address,
          phoneNumber: editedProfile.phoneNumber,
          maritalStatus: editedProfile.maritalStatus,
          idNumber: profile?.idNumber,
          email: profile?.email,
          hireDate: profile?.hireDate,
          status: profile?.status,
          employmentStatus: profile?.employmentStatus,
          departmentId: profile?.departmentId,
          departmentName: profile?.departmentName,
          createdAt: profile?.createdAt,
          workTimeInSched: profile?.workTimeInSched,
          workTimeOutSched: profile?.workTimeOutSched,
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
    setIsEditing((prev) => !prev)
  }, [isEditing, handleUpdateProfile])

  const profileFields = useMemo(
    () => [
      { label: "Employee ID", field: "employeeId" as keyof EmployeeProfile, type: "text", readOnly: true },
      { label: "ID Number", field: "idNumber" as keyof EmployeeProfile, type: "text", readOnly: true },
      { label: "Email", field: "email" as keyof EmployeeProfile, type: "text", readOnly: true, noFormat: true },
      { label: "First Name", field: "firstName" as keyof EmployeeProfile, type: "text" , readOnly: true},
      { label: "Last Name", field: "lastName" as keyof EmployeeProfile, type: "text" , readOnly: true},
      {
        label: "Gender",
        field: "gender" as keyof EmployeeProfile,
        type: "select",
        options: ["MALE", "FEMALE", "OTHER"],
      },
      { label: "Date of Birth", field: "dateOfBirth" as keyof EmployeeProfile, type: "date" },
      { label: "Address", field: "address" as keyof EmployeeProfile, type: "text" },
      { label: "Phone Number", field: "phoneNumber" as keyof EmployeeProfile, type: "text" },
      {
        label: "Marital Status",
        field: "maritalStatus" as keyof EmployeeProfile,
        type: "select",
        options: ["SINGLE", "MARRIED", "DIVORCED", "WIDOWED"],
      },
      { label: "Hire Date", field: "hireDate" as keyof EmployeeProfile, type: "date" },
      {
        label: "Employment Status",
        field: "employmentStatus" as keyof EmployeeProfile,
        type: "select",
        options: ["ACTIVE", "INACTIVE"],
      },
      { label: "Department", field: "departmentName" as keyof EmployeeProfile, type: "text" },
      { label: "Job Title", field: "jobName" as keyof EmployeeProfile, type: "text" },
      { label: "Role", field: "roleName" as keyof EmployeeProfile, type: "text" },
    ],
    [],
  ) as ProfileField[]

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
            marginTop: "5rem",
          },
        }}
      />
      <div className="max-w-6xl mx-auto">
        <Header isEditing={isEditing} isUpdating={isUpdating} onEditToggle={handleEditToggle} />

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="space-y-6">
            <ProfileSummaryCard profile={profile} />
            <AccountInfoCard profile={profile} />
          </div>
          <div className="lg:col-span-2">
            <DetailedInfoCards
              profile={profile}
              editedProfile={editedProfile}
              isEditing={isEditing}
              onInputChange={handleInputChange}
              profileFields={profileFields}
            />
          </div>
        </div>

        <div className="mt-6">
          <DocumentsCard profile={profile} />
        </div>
      </div>
    </div>
  )
}
