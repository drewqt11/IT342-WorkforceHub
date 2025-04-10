"use client";

import {
  Bell,
  User,
  ChevronDown,
  Calendar,
  Moon,
  Sun,
  LogOut,
} from "lucide-react";
import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { authService } from "@/lib/auth";

export function Navbar() {
  const router = useRouter();
  const [notifications, setNotifications] = useState(3);
  const [currentTime, setCurrentTime] = useState<string>("");
  const [currentDate, setCurrentDate] = useState<string>("");
  const [isDarkMode, setIsDarkMode] = useState(false);
  const [userInfo, setUserInfo] = useState({
    firstName: "",
    lastName: "",
    email: "",
    role: "",
    idNumber: "",
  });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchUserProfile = async () => {
      try {
        setIsLoading(true);
        // Fetch user profile from the backend API
        const profile = await authService.getEmployeeProfile();

        setUserInfo({
          firstName: profile.firstName || "",
          lastName: profile.lastName || "",
          email: profile.email || "",
          role: profile.role || "",
          idNumber: profile.idNumber || "",
        });
      } catch (error) {
        console.error("Error fetching user profile:", error);
        // Fallback to cookies if API call fails
        const getCookie = (name: string) => {
          const value = `; ${document.cookie}`;
          const parts = value.split(`; ${name}=`);
          if (parts.length === 2) return parts.pop()?.split(';').shift();
          return null;
        };

        const firstName = getCookie('firstName') || "";
        const lastName = getCookie('lastName') || "";
        const email = getCookie('email') || "";
        const role = getCookie('role') || "";
        const idNumber = getCookie('idNumber') || "";

        setUserInfo({
          firstName,
          lastName,
          email,
          role,
          idNumber,
        });
      } finally {
        setIsLoading(false);
      }
    };

    fetchUserProfile();
  }, []);

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      setCurrentTime(
        now.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
      );
      setCurrentDate(
        now.toLocaleDateString([], {
          weekday: "short",
          month: "short",
          day: "numeric",
        })
      );
    };

    updateTime();
    const timer = setInterval(updateTime, 60000);

    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    // Check if dark mode is enabled
    const isDark = document.documentElement.classList.contains("dark");
    setIsDarkMode(isDark);
  }, []);

  const toggleDarkMode = () => {
    document.documentElement.classList.toggle("dark");
    setIsDarkMode(!isDarkMode);
  };

  const handleLogout = async () => {
    try {
      // Call the authService logout method which handles the backend API call
      await authService.logout();
      // The authService.logout() method already redirects to '/', but we'll keep this for clarity
      router.push('/');
    } catch (error) {
      console.error('Logout error:', error);
      // Even if there's an error, clear tokens and redirect
      authService.clearTokens();
      router.push('/');
    }
  };

  return (
    <div className="h-20 border-b border-[#E5E7EB] bg-white/90 backdrop-blur-md dark:bg-[#1F2937]/90 flex items-center justify-between px-8 sticky top-0 z-10">
      <div className="flex items-center">
        <div className="text-xl font-bold text-[#3B82F6] mr-6 whitespace-nowrap">
          DASHBOARD
        </div>

        <div className="hidden lg:flex items-center text-[#6B7280] text-sm whitespace-nowrap">
          <Calendar className="h-4 w-4 mr-2 flex-shrink-0" />
          <span className="truncate">{currentDate}</span>
          <div className="mx-2 h-4 w-px bg-[#E5E7EB] dark:bg-[#4B5563] flex-shrink-0"></div>
          <span className="truncate">{currentTime}</span>
        </div>
      </div>

      <div className="flex items-center gap-4">
        <Button
          variant="ghost"
          size="icon"
          onClick={toggleDarkMode}
          className="relative bg-[#F9FAFB] rounded-full h-10 w-10 dark:bg-[#374151] hover:bg-[#E5E7EB] dark:hover:bg-[#4B5563] flex-shrink-0"
        >
          {isDarkMode ? (
            <Sun className="h-5 w-5 text-[#6B7280]" />
          ) : (
            <Moon className="h-5 w-5 text-[#6B7280]" />
          )}
        </Button>

        <div className="relative flex-shrink-0">
          <Button
            variant="ghost"
            size="icon"
            className="relative bg-[#F9FAFB] rounded-full h-10 w-10 dark:bg-[#374151] hover:bg-[#E5E7EB] dark:hover:bg-[#4B5563] transition-all duration-300"
          >
            <Bell className="h-5 w-5 text-[#6B7280]" />
            {notifications > 0 && (
              <span className="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full bg-[#3B82F6] text-xs text-white shadow-lg animate-pulse">
                {notifications}
              </span>
            )}
          </Button>
        </div>

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="ghost"
              className="flex items-center gap-3 pl-3 pr-4 rounded-full bg-[#F9FAFB] dark:bg-[#374151] hover:bg-[#E5E7EB] dark:hover:bg-[#4B5563] transition-all duration-300 flex-shrink-0"
            >
              <div className="h-9 w-9 rounded-full bg-[#3B82F6] flex items-center justify-center text-white shadow-lg shadow-[#3B82F6]/20 ring-2 ring-white dark:ring-[#1F2937] flex-shrink-0">
                {isLoading ? (
                  <div className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></div>
                ) : userInfo.firstName && userInfo.lastName ? (
                  <span className="text-sm font-medium">
                    {userInfo.firstName.charAt(0)}{userInfo.lastName.charAt(0)}
                  </span>
                ) : (
                  <User className="h-4 w-4" />
                )}
              </div>
              <div className="flex flex-col items-start">
                <span className="font-medium text-sm text-[#1F2937] dark:text-white whitespace-nowrap">
                  {isLoading ? (
                    <div className="h-4 w-24 animate-pulse bg-gray-200 dark:bg-gray-700 rounded"></div>
                  ) : userInfo.firstName && userInfo.lastName
                    ? `${userInfo.firstName} ${userInfo.lastName}`
                    : "User"}
                </span>
                <span className="text-xs text-[#6B7280] whitespace-nowrap">
                  {isLoading ? (
                    <div className="h-3 w-16 animate-pulse bg-gray-200 dark:bg-gray-700 rounded"></div>
                  ) : userInfo.role || "Employee"}
                </span>
              </div>
              <ChevronDown className="h-4 w-4 text-[#6B7280] ml-1 flex-shrink-0" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-56 mt-1 rounded-xl p-2">
            <DropdownMenuLabel className="font-normal">
              <div className="flex flex-col space-y-1">
                <p className="text-sm font-medium text-[#1F2937] dark:text-white">
                  {isLoading ? (
                    <div className="h-4 w-32 animate-pulse bg-gray-200 dark:bg-gray-700 rounded"></div>
                  ) : userInfo.firstName && userInfo.lastName
                    ? `${userInfo.firstName} ${userInfo.lastName}`
                    : "User"}
                </p>
                <p className="text-xs text-[#6B7280]">
                  {isLoading ? (
                    <div className="h-3 w-40 animate-pulse bg-gray-200 dark:bg-gray-700 rounded"></div>
                  ) : userInfo.email || "user@example.com"}
                </p>
                {userInfo.idNumber && (
                  <p className="text-xs text-[#6B7280]">
                    ID Number: {userInfo.idNumber}
                  </p>
                )}
              </div>
            </DropdownMenuLabel>
            <DropdownMenuSeparator className="bg-[#E5E7EB]" />
            <DropdownMenuItem className="cursor-pointer rounded-lg focus:bg-[#F9FAFB] dark:focus:bg-[#374151] text-[#1F2937] dark:text-white">
              <User className="mr-2 h-4 w-4" />
              <span>Profile</span>
            </DropdownMenuItem>
            <DropdownMenuItem className="cursor-pointer rounded-lg focus:bg-[#F9FAFB] dark:focus:bg-[#374151] text-[#1F2937] dark:text-white">
              <Bell className="mr-2 h-4 w-4" />
              <span>Notifications</span>
              <span className="ml-auto flex h-5 w-5 items-center justify-center rounded-full bg-[#3B82F6]/10 text-xs font-medium text-[#3B82F6]">
                {notifications}
              </span>
            </DropdownMenuItem>
            <DropdownMenuSeparator className="bg-[#E5E7EB]" />
            <DropdownMenuItem
              className="cursor-pointer rounded-lg text-red-600 focus:bg-red-50 dark:focus:bg-red-950"
              onClick={handleLogout}
            >
              <LogOut className="mr-2 h-4 w-4" />
              <span>Logout</span>
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>
  );
}
