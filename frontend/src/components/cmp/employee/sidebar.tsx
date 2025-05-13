"use client"

import type React from "react"
import { useState } from "react"
import Link from "next/link"
import { usePathname } from "next/navigation"
import { cn } from "@/lib/utils"
import { useUser } from "@/contexts/UserContext"
import {
  LayoutDashboard,
  User,
  FileText,
  Calendar,
  Settings,
  Building2,
  Heart,
  GraduationCap,
  ChevronDown,
  ChevronRight,
  Clock8,
  CalendarClock,
  CalendarClockIcon,
  Briefcase,
  PenLine,
  MessageSquare,
  BarChart2,
  Target,
  AlertTriangle,
} from "lucide-react"

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

export function AppSidebar() {
  const pathname = usePathname()
  const { user } = useUser()
  const isInactive = user?.status === false
  const isActive = user?.status === true
  const [openSubMenu, setOpenSubMenu] = useState<string | null>(null)

  const toggleSubMenu = (title: string) => {
    setOpenSubMenu(openSubMenu === title ? null : title)
  }

  const menuItems: MenuItem[] = [
    {
      title: "Dashboard",
      href: "/employee/dashboard",
      icon: LayoutDashboard,
    },
    ...(isActive
      ? [
        {
          title: "Main",
          href: "#main-divider",
          icon: LayoutDashboard,
          isDivider: true,
        },
          {
            title: "My Profile",
            href: "/employee/profile",
            icon: User,
          },
          {
            title: "Benefits",
            href: "/employee/benefits",
            icon: Heart,
            subItems: [
              {
                title: "Health Insurance",
                href: "/employee/benefits/health",
              },
              {
                title: "Retirement",
                href: "/employee/benefits/retirement",
              },
              {
                title: "Other Benefits",
                href: "/employee/benefits/other",
              },
            ],
          },
          {
            title: "My Applications",
            href: "/employee/applications",
            icon: PenLine,
            subItems: [
              {
                title: "Benefits",
                href: "/employee/applications/benefits",
              },
            ],
          },
          {
            title: "Work & Schedule",
            href: "#work-divider",
            icon: LayoutDashboard,
            isDivider: true,
          },
          {
            title: "Attendance Logs",
            href: "/employee/attendance-logs",
            icon: Clock8,
          },
          {
            title: "Requests",
            href: "/employee/requests",
            icon: CalendarClockIcon,
            subItems: [
              {
                title: "All Requests",
                href: "/employee/requests/all",
              },
              {
                title: "Leave",
                href: "/employee/requests/leave",
              },
              {
                title: "Overtime",
                href: "/employee/requests/overtime",
              },
              {
                title: "Reimbursement",
                href: "/employee/requests/reimbursement",
              },
            ],
          },
          {
            title: "Career & Development",
            href: "#career-divider",
            icon: LayoutDashboard,
            isDivider: true,
          },
          {
            title: "Job Listings",
            href: "/employee/jobs",
            icon: Briefcase,
          },
          
          {
            title: "Training & Events",
            href: "/employee/training-events",
            icon: GraduationCap,
          },
          {
            title: "Feedback & Complaints",
            href: "#feedback-divider",
            icon: LayoutDashboard,
            isDivider: true,
          },
          {
            title: "Feedbacks & Complaints",
            href: "/employee/feedbacks",
            icon: MessageSquare,
          },
          {
            title: "All Sanction Reports",
            href: "/employee/all-sanction-reports",
            icon: AlertTriangle,
          },
        ]
      : []),
  ]

  return (
    <div className="flex h-full flex-col border-r border-blue-100 bg-blue-50/90 backdrop-blur-md dark:bg-blue-950/90 dark:border-blue-900">
      {/* Logo and company name - matching navbar height */}
      <div className="flex h-20 items-center justify-between border-b border-blue-100 dark:border-blue-900 px-6">
        <Link href="/employee/dashboard" className="flex items-center gap-3">
          <div className="h-10 w-10 bg-white rounded-lg flex items-center justify-center shadow-md overflow-hidden">
            <img 
              src="/Logo with no Text.png" 
                alt="Workforce Hub Logo"
              className="h-8 w-8 object-contain"
              />
          </div>
          <div className="flex flex-col">
            <span className="font-bold text-blue-900 dark:text-blue-100">Workforce Hub</span>
            <span className="text-xs text-blue-600 dark:text-blue-300">Enterprise Portal</span>
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
                      onClick={(e) => item.disabled && e.preventDefault()}
                    >
                      <item.icon className="h-5 w-5" />
                      <span>{item.title}</span>
                    </Link>
                  )}

                  {/* Sub menu items */}
                  {item.subItems && openSubMenu === item.title && (
                    <div className="ml-9 mt-1 space-y-1 border-l-2 border-blue-200 dark:border-blue-800 pl-2">
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
                          onClick={(e) => subItem.disabled && e.preventDefault()}
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

      {/* Footer section */}
      <div className="border-t border-blue-100 dark:border-blue-900 p-4">
        <div className="flex items-center gap-2 text-xs text-blue-600 dark:text-blue-300">
          <div className="h-2 w-2 rounded-full bg-green-500" />
          <span>System Status: Online</span>
        </div>
        <div className="mt-1 text-xs text-blue-600 dark:text-blue-300">Version 1.0.0</div>
      </div>
    </div>
  )
}
