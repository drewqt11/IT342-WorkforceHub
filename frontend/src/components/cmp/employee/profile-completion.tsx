"use client"

import type React from "react"
import { useRouter } from "next/navigation"
import { User, ChevronRight, Shield, Award, Briefcase, Phone, Building2, Calendar, Heart, X, CheckCircle } from "lucide-react"
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
  const [profile, setProfile] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [lastSaved, setLastSaved] = useState<Date | null>(null)
  const [isRefreshing, setIsRefreshing] = useState(false)

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        setIsRefreshing(true)
        const data = await authService.getEmployeeProfile()
        setProfile(data)
        setLastSaved(new Date())
      } catch (err) {
        console.error("Error fetching profile:", err)
      } finally {
        setIsRefreshing(false)
        setLoading(false)
      }
    }

    fetchProfile()

    // Set up periodic refresh every 30 seconds
    const interval = setInterval(fetchProfile, 30000)
    return () => clearInterval(interval)
  }, [])

  const checkProfileStatus = (field: string) => {
    if (!profile) return "incomplete"
    
    // Check for draft data
    const savedDraft = localStorage.getItem('enrollmentDraft')
    let draftData = null
    if (savedDraft) {
      try {
        draftData = JSON.parse(savedDraft)
      } catch (e) {
        console.error('Error parsing draft data:', e)
      }
    }
    
    // Debug logging
    console.log(`Checking status for ${field}:`, {
      profileValue: profile[field],
      draftValue: draftData?.formData?.[field],
      hasDraft: !!draftData
    })
    
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
    
    // Check both profile and draft data
    const profileValue = profile[field]
    const draftValue = draftData?.formData?.[field]
    
    // Consider a field complete if it has a value in either profile or draft
    const isComplete = (profileValue !== null && profileValue !== undefined && profileValue !== "" && profileValue !== false) ||
                      (draftValue !== null && draftValue !== undefined && draftValue !== "" && draftValue !== false)
    
    return isComplete ? "complete" : "incomplete"
  }

  // Get missing fields with more detailed information
  const getMissingFields = () => {
    if (!profile) return []
    
    const fieldsToCheck = [
      { key: 'firstName', label: 'First Name', section: 'Personal Information' },
      { key: 'lastName', label: 'Last Name', section: 'Personal Information' },
      { key: 'email', label: 'Email', section: 'Contact Details' },
      { key: 'phoneNumber', label: 'Phone Number', section: 'Contact Details' },
      { key: 'gender', label: 'Gender', section: 'Demographics' },
      { key: 'dateOfBirth', label: 'Date of Birth', section: 'Demographics' },
      { key: 'address', label: 'Address', section: 'Address Information' },
      { key: 'maritalStatus', label: 'Marital Status', section: 'Demographics' }
    ]
    
    return fieldsToCheck
      .filter(({ key }) => checkProfileStatus(key) === "incomplete")
      .map(({ label, section }) => ({ label, section }))
  }

  const missingFields = getMissingFields()

  // Calculate completion percentage with weighted fields
  const calculateCompletionPercentage = () => {
    if (!profile) return 0
    
    // Check for draft data
    const savedDraft = localStorage.getItem('enrollmentDraft')
    let draftData = null
    if (savedDraft) {
      try {
        draftData = JSON.parse(savedDraft)
      } catch (e) {
        console.error('Error parsing draft data:', e)
      }
    }
    
    const fieldsToCheck = [
      { key: 'firstName', weight: 1 },
      { key: 'lastName', weight: 1 },
      { key: 'email', weight: 1 },
      { key: 'phoneNumber', weight: 1 },
      { key: 'gender', weight: 0.5 },
      { key: 'dateOfBirth', weight: 0.5 },
      { key: 'address', weight: 1 },
      { key: 'maritalStatus', weight: 0.5 }
    ]
    
    const totalWeight = fieldsToCheck.reduce((sum, field) => sum + field.weight, 0)
    const completedWeight = fieldsToCheck.reduce((sum, field) => {
      const profileValue = profile[field.key]
      const draftValue = draftData?.formData?.[field.key]
      
      // Consider a field complete if it has a value in either profile or draft
      const isComplete = (profileValue !== null && profileValue !== undefined && profileValue !== "" && profileValue !== false) ||
                        (draftValue !== null && draftValue !== undefined && draftValue !== "" && draftValue !== false)
      
      return sum + (isComplete ? field.weight : 0)
    }, 0)
    
    const percentage = Math.round((completedWeight / totalWeight) * 100)
    console.log('Completion percentage calculation:', {
      completedWeight,
      totalWeight,
      percentage
    })
    
    return percentage
  }

  const completionPercentage = calculateCompletionPercentage()

  useEffect(() => {
    const timer = setTimeout(() => {
      setProgress(completionPercentage)
    }, 500)
    return () => clearTimeout(timer)
  }, [completionPercentage])

  // Track profile completion changes with more detailed logging
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
          return value !== null && value !== undefined && value !== "" && value !== false
        })

      console.log('Profile Completion Update:', {
        completionPercentage,
        completedFields,
        totalFields: fieldsToCheck.length,
        lastSaved: lastSaved?.toISOString(),
        missingFields: missingFields.map(f => ({ field: f.label, section: f.section })),
      })
    }
  }, [profile, completionPercentage, lastSaved, missingFields])

  const handleCompleteProfile = () => {
    router.push("/employee/enrollment")
  }

  if (loading) {
    return (
      <Card
        className="w-full h-full overflow-hidden border border-[#E5E7EB] shadow-xl rounded-2xl bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] relative flex flex-col"
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
      className="w-full h-full overflow-hidden border border-[#E5E7EB] shadow-xl rounded-2xl bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] relative flex flex-col"
      role="region"
      aria-label="Profile Completion Status"
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
        <div className="mb-6">
          <div className="flex justify-between mb-2">
            <span className="text-sm text-[#6B7280]">Progress</span>
            <div className="flex items-center gap-2">
              {isRefreshing && (
                <div className="h-3 w-3 rounded-full border-2 border-[#3B82F6] border-t-transparent animate-spin"></div>
              )}
              <span className="font-bold text-[#3B82F6]">{completionPercentage}%</span>
            </div>
          </div>
          <Progress
            value={progress}
            className="h-2 bg-[#E5E7EB] dark:bg-[#374151]"
            indicatorClassName="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6]"
          />
        </div>

        <div className="space-y-4 flex-1 overflow-auto" role="list" aria-label="Profile completion items">
          {missingFields.length > 0 ? (
            <div className="p-4 rounded-xl border border-amber-100 bg-amber-50 dark:border-amber-900/30 dark:bg-amber-900/10">
              <h4 className="font-medium text-[#1F2937] dark:text-white mb-3">Missing Information</h4>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {missingFields.map((field) => (
                  <div
                    key={field.label}
                    className="flex items-center gap-2 p-2 rounded-lg bg-white dark:bg-[#1F2937]/50"
                    role="listitem"
                    aria-label={`${field.label}: Incomplete`}
                  >
                    <div className="h-8 w-8 rounded-full flex items-center justify-center bg-amber-100 text-amber-600 dark:bg-amber-900/30 dark:text-amber-400">
                      <User className="h-4 w-4" />
                    </div>
                    <div className="flex-1">
                      <p className="text-sm font-medium text-[#1F2937] dark:text-white">{field.label}</p>
                      <p className="text-xs text-[#6B7280]">{field.section}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="p-4 rounded-xl border border-[#14B8A6]/20 bg-[#F0FDFA] dark:border-[#14B8A6]/20 dark:bg-[#134E4A]/20">
              <div className="flex items-center gap-3">
                <div className="h-10 w-10 rounded-full bg-[#14B8A6]/20 text-[#14B8A6] dark:bg-[#14B8A6]/30 dark:text-[#14B8A6] flex items-center justify-center">
                  <CheckCircle className="h-5 w-5" />
                </div>
                <div>
                  <h4 className="font-medium text-[#1F2937] dark:text-white">All Required Information Complete</h4>
                  <p className="text-sm text-[#6B7280]">Your profile is completed and submitted.</p>
                </div>
              </div>
            </div>
          )}
        </div>

        <motion.div
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
          transition={{ duration: 0.2 }}
          className="mt-6"
        >
          {missingFields.length === 0 ? (
            <div className="text-center">
              <Button
                className="w-full rounded-xl py-6 bg-gradient-to-r from-[#14B8A6] to-[#3B82F6] opacity-50 cursor-not-allowed"
                disabled
                aria-label="Profile completed"
              >
                <span className="text-lg font-medium">
                  Profile Completed
                </span>
                <CheckCircle className="h-5 w-5 ml-2" />
              </Button>
              <p className="mt-4 text-sm text-[#6B7280] dark:text-[#9CA3AF]">
                Please wait for account activation after your profile information is checked.
              </p>
            </div>
          ) : (
            <motion.div
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              transition={{ duration: 0.2 }}
            >
              <Button
                className="w-full rounded-xl py-6 bg-gradient-to-r from-[#14B8A6] to-[#3B82F6] hover:from-[#0D9488] hover:to-[#2563EB] hover:shadow-lg hover:shadow-[#14B8A6]/20 transition-all duration-300 group"
                aria-label="Complete your profile"
                onClick={handleCompleteProfile}
              >
                <span className="text-lg font-medium group-hover:tracking-wide transition-all duration-300">
                  Complete Your Profile
                </span>
                <ChevronRight className="h-5 w-5 ml-2 transition-transform duration-300 group-hover:translate-x-1" />
              </Button>
            </motion.div>
          )}
        </motion.div>
      </CardContent>
    </Card>
  )
}
