"use client"

import type React from "react"
import { useRouter } from "next/navigation"
import { User, ChevronRight, Shield, Award, Briefcase, Phone, Building2, Calendar, Heart, X } from "lucide-react"
import { Card, CardContent, CardHeader } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Progress } from "@/components/ui/progress"
import { useState, useEffect } from "react"
import { motion } from "framer-motion"
import { authService } from "@/lib/auth"

type ProfileItem = {
  id: string
  name: string
  description: string
  status: "complete" | "incomplete" | "N/A"
  icon: React.ReactNode
}

export function ProfileCompletion() {
  const router = useRouter()
  const [progress, setProgress] = useState(0)
  const [hoveredItem, setHoveredItem] = useState<string | null>(null)
  const [selectedItem, setSelectedItem] = useState<string | null>(null)
  const [profile, setProfile] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [isFullScreen, setIsFullScreen] = useState(false)

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await authService.getEmployeeProfile()
        setProfile(data)
      } catch (err) {
        console.error("Error fetching profile:", err)
      } finally {
        setLoading(false)
      }
    }

    fetchProfile()
  }, [])

  const checkProfileStatus = (field: string) => {
    if (!profile) return "incomplete"
    
    // Only check these specific fields
    const fieldsToCheck = [
      'firstName',
      'lastName',
      'email',
      'phoneNumber',
      'gender',
      'dateOfBirth',
      'address',
      'maritalStatus'
    ]
    
    if (!fieldsToCheck.includes(field)) {
      return "N/A"
    }
    
    const value = profile[field]
    return value === null || value === undefined || value === "" ? "incomplete" : "complete"
  }

  // Get missing fields
  const getMissingFields = () => {
    if (!profile) return []
    
    const fieldsToCheck = [
      { key: 'firstName', label: 'First Name' },
      { key: 'lastName', label: 'Last Name' },
      { key: 'email', label: 'Email' },
      { key: 'phoneNumber', label: 'Phone Number' },
      { key: 'gender', label: 'Gender' },
      { key: 'dateOfBirth', label: 'Date of Birth' },
      { key: 'address', label: 'Address' },
      { key: 'maritalStatus', label: 'Marital Status' }
    ]
    
    return fieldsToCheck
      .filter(({ key }) => checkProfileStatus(key) === "incomplete")
      .map(({ label }) => label)
  }

  const missingFields = getMissingFields()

  const profileItems: ProfileItem[] = [
    {
      id: "personal",
      name: "Personal Information",
      description: missingFields.includes('First Name') || missingFields.includes('Last Name') 
        ? `Missing: ${missingFields.filter(f => ['First Name', 'Last Name'].includes(f)).join(', ')}`
        : "Basic details about yourself",
      status:
        checkProfileStatus("firstName") === "complete" && 
        checkProfileStatus("lastName") === "complete"
          ? "complete"
          : "incomplete",
      icon: <User className="h-5 w-5" />,
    },
    {
      id: "contact",
      name: "Contact Details",
      description: missingFields.includes('Email') || missingFields.includes('Phone Number')
        ? `Missing: ${missingFields.filter(f => ['Email', 'Phone Number'].includes(f)).join(', ')}`
        : "How to reach you",
      status:
        checkProfileStatus("email") === "complete" && 
        checkProfileStatus("phoneNumber") === "complete"
          ? "complete"
          : "incomplete",
      icon: <Phone className="h-5 w-5" />,
    },
    {
      id: "address",
      name: "Address Information",
      description: missingFields.includes('Address')
        ? "Missing: Address"
        : "Your residential address",
      status: checkProfileStatus("address"),
      icon: <Building2 className="h-5 w-5" />,
    },
    {
      id: "demographics",
      name: "Demographics",
      description: missingFields.some(f => ['Gender', 'Date of Birth', 'Marital Status'].includes(f))
        ? `Missing: ${missingFields.filter(f => ['Gender', 'Date of Birth', 'Marital Status'].includes(f)).join(', ')}`
        : "Gender, date of birth, marital status",
      status:
        checkProfileStatus("gender") === "complete" &&
        checkProfileStatus("dateOfBirth") === "complete" &&
        checkProfileStatus("maritalStatus") === "complete"
          ? "complete"
          : "incomplete",
      icon: <Heart className="h-5 w-5" />,
    }
  ]

  // Calculate completion percentage based on non-N/A items
  const completedItems = profileItems.filter((item) => item.status === "complete").length
  const totalItems = profileItems.filter((item) => item.status !== "N/A").length
  const completionPercentage = totalItems > 0 ? Math.round((completedItems / totalItems) * 100) : 0

  useEffect(() => {
    const timer = setTimeout(() => {
      setProgress(completionPercentage)
    }, 500)
    return () => clearTimeout(timer)
  }, [completionPercentage])

  // Track profile completion changes
  useEffect(() => {
    if (profile) {
      const fieldsToCheck = [
        'firstName',
        'lastName',
        'email',
        'phoneNumber',
        'gender',
        'dateOfBirth',
        'address',
        'maritalStatus'
      ]
      
      const completedFields = fieldsToCheck
        .filter(field => {
          const value = profile[field]
          return value !== null && value !== undefined && value !== ""
        })

      console.log('Profile Completion Update:', {
        completionPercentage,
        completedFields,
        totalFields: fieldsToCheck.length
      })
    }
  }, [profile, completionPercentage])

  const handleCompleteProfile = () => {
    router.push("/employee/enrollment")
  }

  const toggleFullScreen = () => {
    setIsFullScreen(!isFullScreen)
  }

  if (loading) {
    return (
      <Card
        className={`w-full h-full overflow-hidden border border-[#E5E7EB] shadow-xl rounded-2xl bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] relative flex flex-col ${isFullScreen ? "fixed inset-0 z-50 rounded-none" : ""}`}
      >
        <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>
        <CardHeader className="pt-6 pb-0">
          <div className="flex items-center gap-3">
            <div className="rounded-full bg-gradient-to-r from-[#14B8A6] to-[#3B82F6] p-[2px] shadow-lg shadow-[#14B8A6]/20">
              <div className="rounded-full bg-white p-2 dark:bg-[#1F2937]">
                <User className="h-8 w-8 text-[#14B8A6]" aria-hidden="true" />
              </div>
            </div>
            <h3 className="text-xl font-bold text-[#1F2937] dark:text-white">Profile Completion</h3>
          </div>
        </CardHeader>
        <CardContent className="pt-4 pb-6 flex-1 flex flex-col">
          <div className="flex items-center justify-center h-full">
            <p className="text-[#6B7280]">Loading profile data...</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card
      className={`w-full h-full overflow-hidden border border-[#E5E7EB] shadow-xl rounded-2xl bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] relative flex flex-col ${isFullScreen ? "fixed inset-0 z-50 rounded-none" : ""}`}
      role="region"
      aria-label="Profile Completion Status"
    >
      <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>

      <CardHeader className="pt-6 pb-0">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="rounded-full bg-gradient-to-r from-[#14B8A6] to-[#3B82F6] p-[2px] shadow-lg shadow-[#14B8A6]/20">
              <div className="rounded-full bg-white p-2 dark:bg-[#1F2937]">
                <User className="h-8 w-8 text-[#14B8A6]" aria-hidden="true" />
              </div>
            </div>
            <h3 className="text-xl font-bold text-[#1F2937] dark:text-white">Profile Completion</h3>
          </div>
          <Button
            variant="ghost"
            size="icon"
            onClick={toggleFullScreen}
            className="h-8 w-8 rounded-full hover:bg-[#F0FDFA] dark:hover:bg-[#134E4A]/50"
            aria-label={isFullScreen ? "Exit full screen" : "Enter full screen"}
          >
            {isFullScreen ? (
              <X className="h-5 w-5 text-[#6B7280]" />
            ) : (
              <div className="h-5 w-5 border-2 border-[#6B7280] rounded-sm" />
            )}
          </Button>
        </div>
      </CardHeader>
      <CardContent className="pt-4 pb-6 flex-1 flex flex-col">
        <div className="mb-6">
          <div className="flex justify-between mb-2">
            <span className="text-sm text-[#6B7280]">Progress</span>
            <span
              className={`font-bold ${progress < 50 ? "text-amber-500" : "text-[#14B8A6]"}`}
              role="status"
              aria-label={`Profile completion: ${completionPercentage}%`}
            >
              {completionPercentage}%
            </span>
          </div>

          <Progress
            value={progress}
            className="h-3 w-full bg-[#F9FAFB] dark:bg-[#374151]"
            indicatorClassName={`${
              progress < 50
                ? "bg-gradient-to-r from-amber-400 to-amber-500"
                : "bg-gradient-to-r from-[#14B8A6] to-[#3B82F6]"
            }`}
          />

          {missingFields.length > 0 && (
            <div className="mt-4 p-3 bg-amber-50 dark:bg-amber-900/10 rounded-lg">
              <p className="text-sm text-amber-700 dark:text-amber-400 font-medium mb-2">
                Missing Information:
              </p>
              <ul className="text-sm text-amber-600 dark:text-amber-500 list-disc list-inside">
                {missingFields.map((field) => (
                  <li key={field}>{field}</li>
                ))}
              </ul>
            </div>
          )}
        </div>

        <div className="space-y-3 flex-1 overflow-auto" role="list" aria-label="Profile completion items">
          {profileItems.map((item) => (
            <motion.div
              key={item.id}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3 }}
              className={`flex items-center gap-3 p-3 rounded-xl border transition-all duration-300 cursor-pointer ${
                item.status === "complete"
                  ? "border-[#14B8A6]/20 bg-[#F0FDFA] dark:border-[#14B8A6]/20 dark:bg-[#134E4A]/20"
                  : item.status === "N/A"
                    ? "border-gray-100 bg-gray-50 dark:border-gray-800 dark:bg-gray-900/10"
                    : "border-amber-100 bg-amber-50 dark:border-amber-900/30 dark:bg-amber-900/10"
              } ${hoveredItem === item.id ? "shadow-md transform -translate-y-0.5" : ""}`}
              onMouseEnter={() => setHoveredItem(item.id)}
              onMouseLeave={() => setHoveredItem(null)}
              onClick={() => setSelectedItem(selectedItem === item.id ? null : item.id)}
              role="listitem"
              aria-label={`${item.name}: ${
                item.status === "complete" ? "Completed" : item.status === "N/A" ? "Not Applicable" : "Incomplete"
              }`}
            >
              <div
                className={`h-10 w-10 rounded-lg flex items-center justify-center ${
                  item.status === "complete"
                    ? "bg-[#14B8A6]/20 text-[#14B8A6] dark:bg-[#14B8A6]/30 dark:text-[#14B8A6]"
                    : item.status === "N/A"
                      ? "bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400"
                      : "bg-amber-100 text-amber-600 dark:bg-amber-900/30 dark:text-amber-400"
                }`}
                aria-hidden="true"
              >
                {item.icon}
              </div>
              <div className="flex-1">
                <p className="font-medium text-[#1F2937] dark:text-white">{item.name}</p>
                <p className="text-xs text-[#6B7280]">{item.description}</p>
              </div>
              <div className="flex items-center">
                <span
                  className={`text-xs px-3 py-1 rounded-full font-medium ${
                    item.status === "complete"
                      ? "bg-[#14B8A6]/20 text-[#14B8A6] dark:bg-[#14B8A6]/30 dark:text-[#14B8A6]"
                      : item.status === "N/A"
                        ? "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-400"
                        : "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400"
                  }`}
                  role="status"
                  aria-label={`Status: ${
                    item.status === "complete" ? "Complete" : item.status === "N/A" ? "Not Applicable" : "Incomplete"
                  }`}
                >
                  {item.status === "complete" ? "Complete" : item.status === "N/A" ? "N/A" : "Incomplete"}
                </span>
                {(item.status === "incomplete" || item.status === "N/A") && (
                  <ChevronRight
                    className={`h-4 w-4 ml-1 text-[#6B7280] transition-transform duration-300 ${
                      hoveredItem === item.id ? "translate-x-1" : ""
                    } ${selectedItem === item.id ? "rotate-90" : ""}`}
                    aria-hidden="true"
                  />
                )}
              </div>
            </motion.div>
          ))}
        </div>

        <Button
          className="w-full rounded-xl py-6 mt-6 bg-gradient-to-r from-[#14B8A6] to-[#3B82F6] hover:from-[#0D9488] hover:to-[#2563EB] hover:shadow-lg hover:shadow-[#14B8A6]/20 transition-all duration-300 group"
          aria-label="Complete your profile"
          onClick={handleCompleteProfile}
        >
          <span className="text-lg font-medium group-hover:tracking-wide transition-all duration-300">
            Complete Your Profile
          </span>
        </Button>
      </CardContent>
    </Card>
  )
}
