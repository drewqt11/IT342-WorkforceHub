"use client"

import { Bell, User, ChevronDown, Calendar, Moon, Sun, LogOut, Settings, HelpCircle, Menu } from "lucide-react"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Button } from "@/components/ui/button"
import { authService } from "@/lib/auth"
import { cn } from "@/lib/utils"

interface NavbarProps {
  onMobileMenuToggle?: () => void
}

export function Navbar({ onMobileMenuToggle }: NavbarProps) {
  const router = useRouter()
  const [notifications, setNotifications] = useState(3)
  const [currentTime, setCurrentTime] = useState<string>("")
  const [currentDate, setCurrentDate] = useState<string>("")
  const [isDarkMode, setIsDarkMode] = useState(false)
  const [profile, setProfile] = useState<any>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchUserProfile = async () => {
      try {
        setIsLoading(true)
        const profile = await authService.getEmployeeProfile()
        setProfile(profile)
      } catch (error) {
        console.error("Error fetching user profile:", error)
      } finally {
        setIsLoading(false)
      }
    }

    fetchUserProfile()
  }, [])

  useEffect(() => {
    const updateTime = () => {
      const now = new Date()
      setCurrentTime(now.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }))
      setCurrentDate(
        now.toLocaleDateString([], {
          weekday: "short",
          month: "short",
          day: "numeric",
        }),
      )
    }

    updateTime()
    const timer = setInterval(updateTime, 60000)

    return () => clearInterval(timer)
  }, [])

  useEffect(() => {
    // Check if dark mode is enabled
    const isDark = document.documentElement.classList.contains("dark")
    setIsDarkMode(isDark)

    // Add event listener for theme changes
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        if (
          mutation.type === "attributes" &&
          mutation.attributeName === "class" &&
          mutation.target === document.documentElement
        ) {
          setIsDarkMode(document.documentElement.classList.contains("dark"))
        }
      })
    })

    observer.observe(document.documentElement, { attributes: true })

    return () => observer.disconnect()
  }, [])

  const toggleDarkMode = () => {
    document.documentElement.classList.toggle("dark")
    setIsDarkMode(!isDarkMode)
  }

  const handleLogout = async () => {
    try {
      await authService.logout()
      // Remove all cookies
      if (typeof document !== 'undefined' && document.cookie) {
        document.cookie.split(';').forEach(function(c) {
          document.cookie = c.trim().split('=')[0] + '=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/';
        });
      }
      router.push("/")
    } catch (error) {
      console.error("Logout error:", error)
      authService.clearTokens()
      // Remove all cookies
      if (typeof document !== 'undefined' && document.cookie) {
        document.cookie.split(';').forEach(function(c) {
          document.cookie = c.trim().split('=')[0] + '=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/';
        });
      }
      router.push("/")
    }
  }

  // Format the role display
  const formatRole = (role: string) => {
    if (!role) return "HR Admin"

    if (role === "HR ADMINISTRATOR") {
      return "HR Admin"
    } else if (role === "EMPLOYEE") {
      return "Employee"
    }

    // For any other role, return the original role
    return role
  }

  return (
    <div className="h-20 border-b border-blue-100 bg-blue-50/90 backdrop-blur-md dark:bg-blue-950/90 dark:border-blue-900 flex items-center justify-between px-4 md:px-8 sticky top-0 z-10 shadow-sm">
      <div className="flex items-center">
        {/* Mobile menu button - only visible on small screens */}
        <Button
          variant="ghost"
          size="icon"
          onClick={onMobileMenuToggle}
          className="md:hidden mr-2 rounded-full h-10 w-10 bg-white dark:bg-blue-900 text-blue-600 dark:text-blue-300 hover:bg-blue-100 dark:hover:bg-blue-800"
        >
          <Menu className="h-5 w-5" />
          <span className="sr-only">Toggle menu</span>
        </Button>

        <div className="relative mr-3 md:mr-6 whitespace-nowrap">
          <h1 className="text-lg md:text-xl font-bold bg-gradient-to-r from-blue-600 to-blue-500 bg-clip-text text-transparent">
            HR DASHBOARD
          </h1>
          <div className="absolute -bottom-1 left-0 w-1/3 h-0.5 bg-gradient-to-r from-blue-600 to-blue-400"></div>
        </div>

        <div className="hidden lg:flex items-center text-blue-700 text-sm whitespace-nowrap bg-white/80 dark:bg-blue-900/60 px-4 py-2 rounded-full shadow-sm">
          <Calendar className="h-4 w-4 mr-2 flex-shrink-0 text-blue-500" />
          <span className="truncate font-medium">{currentDate}</span>
          <div className="mx-2 h-4 w-px bg-blue-100 dark:bg-blue-800 flex-shrink-0"></div>
          <span className="truncate">{currentTime}</span>
        </div>
      </div>

      <div className="flex items-center gap-2 md:gap-3">
        <Button
          variant="ghost"
          size="icon"
          onClick={toggleDarkMode}
          className={cn(
            "relative rounded-full h-9 w-9 md:h-10 md:w-10 flex-shrink-0 transition-all duration-300",
            isDarkMode
              ? "bg-blue-900 hover:bg-blue-800 active:bg-blue-700 text-blue-300"
              : "bg-white hover:bg-blue-100 hover:text-blue-600 active:bg-blue-200 text-blue-500",
          )}
        >
          {isDarkMode ? <Sun className="h-4 w-4 md:h-5 md:w-5" /> : <Moon className="h-4 w-4 md:h-5 md:w-5" />}
        </Button>

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="ghost"
              className={cn(
                "flex items-center gap-2 md:gap-3 pl-2 pr-3 md:pl-3 md:pr-4 rounded-full transition-all duration-300 flex-shrink-0",
                isDarkMode
                  ? "bg-blue-900 hover:bg-blue-800 active:bg-blue-700"
                  : "bg-white hover:bg-blue-100 active:bg-blue-200",
              )}
            >
              <div className="h-7 w-7 md:h-9 md:w-9 rounded-full bg-gradient-to-r from-blue-600 to-blue-400 flex items-center justify-center text-white shadow-md ring-2 ring-white dark:ring-blue-950 flex-shrink-0">
                {isLoading ? (
                  <div className="h-3 w-3 md:h-4 md:w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></div>
                ) : profile?.firstName && profile?.lastName ? (
                  <span className="text-xs md:text-sm font-medium">
                    {profile.firstName.charAt(0)}
                    {profile.lastName.charAt(0)}
                  </span>
                ) : (
                  <User className="h-3 w-3 md:h-4 md:w-4" />
                )}
              </div>
              <div className="flex flex-col items-start">
                <span className="font-medium text-xs md:text-sm text-blue-900 dark:text-blue-100 whitespace-nowrap">
                  {isLoading ? (
                    <div className="h-3 md:h-4 w-16 md:w-24 animate-pulse bg-blue-100 dark:bg-blue-800 rounded"></div>
                  ) : profile?.firstName && profile?.lastName ? (
                    `${profile.firstName} ${profile.lastName}`
                  ) : (
                    "User"
                  )}
                </span>
                <span className="text-[10px] md:text-xs text-blue-600 dark:text-blue-300 whitespace-nowrap">
                  {isLoading ? (
                    <div className="h-2 md:h-3 w-12 md:w-16 animate-pulse bg-blue-100 dark:bg-blue-800 rounded"></div>
                  ) : (
                    formatRole(profile?.role)
                  )}
                </span>
              </div>
              <ChevronDown className="h-3 w-3 md:h-4 md:w-4 text-blue-500 dark:text-blue-300 ml-0 md:ml-1 flex-shrink-0" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent
            align="end"
            className="w-56 mt-1 rounded-xl p-2 border border-blue-100 dark:border-blue-800 bg-white dark:bg-blue-950 shadow-lg"
          >
            <DropdownMenuLabel className="font-normal">
              <div className="flex flex-col space-y-1">
                <div className="text-sm font-medium text-gray-900 dark:text-gray-100">
                  {isLoading ? (
                    <div className="h-4 w-32 animate-pulse bg-blue-100 dark:bg-blue-800 rounded"></div>
                  ) : profile?.firstName && profile?.lastName ? (
                    `${profile.firstName} ${profile.lastName}`
                  ) : (
                    "User"
                  )}
                </div>
                <div className="text-xs text-gray-600 dark:text-gray-400">
                  {isLoading ? (
                    <div className="h-3 w-40 animate-pulse bg-blue-100 dark:bg-blue-800 rounded"></div>
                  ) : (
                    profile?.email || "user@example.com"
                  )}
                </div>
                {profile?.idNumber && (
                  <div className="text-xs text-gray-600 dark:text-gray-400">ID Number: {profile.idNumber}</div>
                )}
                <div className="text-xs text-gray-600 dark:text-gray-400">
                  Status:{" "}
                  <span
                    className={cn(
                      "px-1.5 py-0.5 rounded-full text-xs",
                      profile?.status === true
                        ? "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400"
                        : "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400",
                    )}
                  >
                    {profile?.status === true ? "Active" : "Inactive"}
                  </span>
                </div>
              </div>
            </DropdownMenuLabel>
            <DropdownMenuSeparator className="bg-gray-200 dark:bg-gray-700 my-2" />
            <DropdownMenuItem
              className="cursor-pointer rounded-lg px-3 py-2 hover:bg-blue-50 dark:hover:bg-blue-900/50 text-gray-900 dark:text-gray-100 focus:bg-blue-100 dark:focus:bg-blue-800 focus:text-gray-900 dark:focus:text-white transition-colors"
              onClick={() => router.push("/hr/admin/profile")}
            >
              <User className="mr-2 h-4 w-4" />
              <span>Profile</span>
            </DropdownMenuItem>
            <DropdownMenuItem className="cursor-pointer rounded-lg px-3 py-2 hover:bg-blue-50 dark:hover:bg-blue-900/50 text-gray-900 dark:text-gray-100 focus:bg-blue-100 dark:focus:bg-blue-800 focus:text-gray-900 dark:focus:text-white transition-colors">
              <HelpCircle className="mr-2 h-4 w-4" />
              <span>Help & Support</span>
            </DropdownMenuItem>
            <DropdownMenuSeparator className="bg-gray-200 dark:bg-gray-700 my-2" />
            <DropdownMenuItem
              className="cursor-pointer rounded-lg px-3 py-2 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/30 focus:bg-red-100 dark:focus:bg-red-900/50 transition-colors"
              onClick={handleLogout}
            >
              <LogOut className="mr-2 h-4 w-4" />
              <span>Logout</span>
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>
  )
}
