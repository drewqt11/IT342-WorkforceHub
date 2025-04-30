"use client"

import type React from "react"
import { useState } from "react"
import Link from "next/link"
import { usePathname } from "next/navigation"
import { cn } from "@/lib/utils"
import {
  LayoutDashboard,
  FileText,
  Calendar,
  Settings,
  Building2,
  Heart,
  GraduationCap,
  ChevronDown,
  ChevronRight,
  Users,
  Briefcase,
  BarChart,
  LogOut,
  X,
  Clock8,
} from "lucide-react"
import { Button } from "@/components/ui/button"

interface SubMenuItem {
  title: string
  href: string
  disabled?: boolean
}

interface MenuItem {
  title: string
  href: string
  icon: React.ElementType
  disabled?: boolean
  subItems?: SubMenuItem[]
  isDivider?: boolean
}

interface AppSidebarProps {
  isMobile?: boolean
  onClose?: () => void
}

export function AppSidebar({ isMobile, onClose }: AppSidebarProps) {
  const pathname = usePathname()
  const [openSubMenu, setOpenSubMenu] = useState<string | null>(null)

  const toggleSubMenu = (title: string) => {
    setOpenSubMenu(openSubMenu === title ? null : title)
  }

  const menuItems: MenuItem[] = [
    {
      title: "Dashboard",
      href: "/hr/admin/dashboard",
      icon: LayoutDashboard,
    },
    {
      title: "Main",
      href: "#main-divider",
      icon: LayoutDashboard,
      isDivider: true,
    },
    {
      title: "Departments",
      href: "/hr/admin/departments",
      icon: Building2,
    },
    {
      title: "Employees",
      href: "/hr/admin/employees",
      icon: Users,
      subItems: [
        {
          title: "All Employees",
          href: "/hr/admin/employees/all",
        },
        {
          title: "Pending Activation",
          href: "/hr/admin/employees/activate",
        },
      ],
    },
    {
      title: "Management",
      href: "#management-divider",
      icon: LayoutDashboard,
      isDivider: true,
    },
    {
      title: "Time & Attendance",
      href: "/hr/admin/attendance/records",
      icon: Clock8,
      subItems: [
        {
          title: "Clock In/Out Records",
          href: "/hr/admin/attendance/clock-in-out/records",
        },
        {
          title: "Attendance Logs",
          href: "/hr/admin/attendance/logs",
        },
        {
          title: "Overtime Requests",
          href: "/hr/admin/attendance/overtime-requests",
        },
        {
          title: "Leave Requests",
          href: "/hr/admin/attendance/leave-requests",
        },
        {
          title: "Attendance Reports",
          href: "/hr/admin/attendance/reports",
        },
      ],
    },
    /*{
      title: "Recruitment",
      href: "/hr/admin/recruitment",
      icon: Briefcase,
      subItems: [
        {
          title: "Job Postings",
          href: "/hr/admin/recruitment/jobs",
        },
        {
          title: "Applications",
          href: "/hr/admin/recruitment/applications",
        },
        {
          title: "Interviews",
          href: "/hr/admin/recruitment/interviews",
        },
      ],
    },
    {
      title: "Benefits",
      href: "/hr/admin/benefits",
      icon: Heart,
      subItems: [
        {
          title: "Health Insurance",
          href: "/hr/admin/benefits/health",
        },
        {
          title: "Retirement",
          href: "/hr/admin/benefits/retirement",
        },
        {
          title: "Other Benefits",
          href: "/hr/admin/benefits/other",
        },
      ],
    },
    {
      title: "Career",
      href: "#career-divider",
      icon: LayoutDashboard,
      isDivider: true,
    },
    {
      title: "Training",
      href: "/hr/admin/training",
      icon: GraduationCap,
    },
    {
      title: "Documents",
      href: "/hr/admin/documents",
      icon: FileText,
    },
    {
      title: "Schedule",
      href: "/hr/admin/schedule",
      icon: Calendar,
    },
    {
      title: "Reports",
      href: "/hr/admin/reports",
      icon: BarChart,
    },
    /*{
      title: "Settings",
      href: "/hr/admin/settings",
      icon: Settings,
    },*/
  ]

  const settingsItem: MenuItem = {
    title: "Settings",
    href: "/hr/admin/settings",
    icon: Settings,
    subItems: [
      {
        title: "General Settings",
        href: "/hr/admin/settings",
      },
      {
        title: "Deactivated Accounts",
        href: "/hr/admin/settings/deactivated",
      },
    ],
  }

  return (
    <div className="flex h-full flex-col border-r border-blue-100 bg-blue-50/90 backdrop-blur-md dark:bg-blue-950/90 dark:border-blue-900">
      {/* Logo and company name - matching navbar height */}
      <div className="flex h-20 items-center justify-between border-b border-blue-100 dark:border-blue-900 px-6">
        <Link href="/hr/admin/dashboard" className="flex items-center gap-3">
          <div className="h-10 w-10 bg-white rounded-lg flex items-center justify-center shadow-md overflow-hidden">
            <img 
              src="/Logo with no Text.png" 
                alt="Workforce Hub Logo"
              className="h-8 w-8 object-contain"
              />
          </div>
          <div className="flex flex-col">
            <span className="font-bold text-blue-900 dark:text-blue-100">Workforce Hub</span>
            <span className="text-xs text-blue-600 dark:text-blue-300">HR Admin Portal</span>
          </div>
        </Link>
        </div>

      {/* Navigation menu */}
      <div className="flex-1 overflow-auto py-4 scrollbar-hide">
        <nav className="grid items-start px-4 text-sm font-medium gap-1">
          {menuItems.map((item) => (
            <div key={item.href} className="flex flex-col">
              {item.isDivider ? (
                <div className="px-3 py-2 text-xs font-medium text-neutral-600 dark:text-blue-300 uppercase tracking-wider">
                  {item.title}
                </div>
              ) : (
                <>
                  {/* Main menu item */}
                  {item.subItems ? (
                    <button
                      onClick={() => toggleSubMenu(item.title)}
                      className={cn(
                        "flex items-center justify-between rounded-lg px-3 py-2.5 transition-all",
                        item.disabled
                          ? "text-blue-300 cursor-not-allowed opacity-60"
                          : pathname === item.href || pathname.startsWith(`${item.href}/`) || openSubMenu === item.title
                            ? "bg-white dark:bg-blue-900 text-blue-600 dark:text-blue-300 shadow-sm"
                            : "text-blue-800 dark:text-blue-200 hover:bg-white hover:text-blue-600 dark:hover:bg-blue-900 dark:hover:text-blue-300",
                      )}
                      disabled={item.disabled}
                    >
                      <div className="flex items-center gap-3">
                        <item.icon className="h-5 w-5" />
                        <span>{item.title}</span>
                      </div>
                      {openSubMenu === item.title ? (
                        <ChevronDown className="h-4 w-4" />
                      ) : (
                        <ChevronRight className="h-4 w-4" />
                      )}
                    </button>
                  ) : (
                    <Link
                      href={item.disabled ? "#" : item.href}
                      className={cn(
                        "flex items-center gap-3 rounded-lg px-3 py-2.5 transition-all",
                        item.disabled
                          ? "text-blue-300 cursor-not-allowed opacity-60"
                          : pathname === item.href
                            ? "bg-white dark:bg-blue-900 text-blue-600 dark:text-blue-300 shadow-sm"
                            : "text-blue-800 dark:text-blue-200 hover:bg-white hover:text-blue-600 dark:hover:bg-blue-900 dark:hover:text-blue-300",
                      )}
                      onClick={(e) => {
                        if (item.disabled) e.preventDefault()
                        if (isMobile && onClose) onClose()
                      }}
                    >
                      <item.icon className="h-5 w-5" />
                      <span>{item.title}</span>
                    </Link>
                  )}

                  {/* Sub menu items */}
                  {item.subItems && openSubMenu === item.title && (
                    <div className="ml-3 mt-1 space-y-1 border-l-2 border-blue-200 dark:border-blue-800 pl-2">
                      {item.subItems.map((subItem) => (
                        <Link
                          key={subItem.href}
                          href={subItem.disabled ? "#" : subItem.href}
                          className={cn(
                            "block rounded-lg px-3 py-2 text-sm transition-all",
                            subItem.disabled
                              ? "text-blue-300 cursor-not-allowed opacity-60"
                              : pathname === subItem.href
                                ? "bg-white dark:bg-blue-900 text-blue-600 dark:text-blue-300 shadow-sm"
                                : "text-blue-800 dark:text-blue-200 hover:bg-white hover:text-blue-600 dark:hover:bg-blue-900 dark:hover:text-blue-300",
                          )}
                          onClick={(e) => {
                            if (subItem.disabled) e.preventDefault()
                            if (isMobile && onClose) onClose()
                          }}
                        >
                          {subItem.title}
                        </Link>
                      ))}
                    </div>
                  )}
                </>
              )}
            </div>
          ))}
        </nav>
          </div>

      {/* Footer section with Settings */}
      <div className="border-t border-blue-100 dark:border-blue-900 p-4 space-y-4">
        <div className="flex flex-col">
          <button
            onClick={() => toggleSubMenu(settingsItem.title)}
            className={cn(
              "flex items-center justify-between rounded-lg px-3 py-2.5 transition-all",
              pathname === settingsItem.href || pathname.startsWith(`${settingsItem.href}/`) || openSubMenu === settingsItem.title
                ? "bg-white dark:bg-blue-900 text-blue-600 dark:text-blue-300 shadow-sm"
                : "text-blue-800 dark:text-blue-200 hover:bg-white hover:text-blue-600 dark:hover:bg-blue-900 dark:hover:text-blue-300",
            )}
          >
            <div className="flex items-center gap-3">
              <settingsItem.icon className="h-5 w-5" />
              <span>{settingsItem.title}</span>
            </div>
            {openSubMenu === settingsItem.title ? (
              <ChevronDown className="h-4 w-4" />
            ) : (
              <ChevronRight className="h-4 w-4" />
            )}
          </button>

          {openSubMenu === settingsItem.title && (
            <div className="ml-3 mt-1 space-y-1 border-l-2 border-blue-200 dark:border-blue-800 pl-2">
              {settingsItem.subItems?.map((subItem) => (
                <Link
                  key={subItem.href}
                  href={subItem.href}
                  className={cn(
                    "block rounded-lg px-3 py-2 text-sm transition-all",
                    pathname === subItem.href
                      ? "bg-white dark:bg-blue-900 text-blue-600 dark:text-blue-300 shadow-sm"
                      : "text-blue-800 dark:text-blue-200 hover:bg-white hover:text-blue-600 dark:hover:bg-blue-900 dark:hover:text-blue-300",
                  )}
                  onClick={() => isMobile && onClose && onClose()}
                >
                  {subItem.title}
                </Link>
              ))}
            </div>
          )}
        </div>

        <div className="flex items-center gap-2 text-xs text-blue-600 dark:text-blue-300">
          <div className="h-2 w-2 rounded-full bg-green-500" />
          <span>System Status: Online</span>
        </div>
      </div>
    </div>
  )
}
