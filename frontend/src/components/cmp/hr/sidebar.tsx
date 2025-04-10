"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  Home,
  DollarSign,
  GraduationCap,
  ChevronRight,
  Heart,
  HandHeart,
  Wallet,
  Clock,
} from "lucide-react";

import {
  Sidebar,
  SidebarContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarMenuSub,
  SidebarMenuSubButton,
  SidebarMenuSubItem,
} from "@/components/ui/sidebar";

export function AppSidebar() {
  const pathname = usePathname();
  const [benefitsOpen, setBenefitsOpen] = useState(
    pathname.startsWith("/benefits")
  );

  const isActive = (path: string) => {
    return pathname === path;
  };

  const isBenefitsActive = pathname.startsWith("/benefits");

  return (
    <Sidebar
      className="border-r border-[#E5E7EB] bg-white dark:bg-[#1F2937] relative overflow-hidden"
      collapsible="none" // This ensures the sidebar cannot be collapsed
    >
      {/* Decorative elements */}
      <div className="absolute top-0 left-0 w-full h-full opacity-5 pointer-events-none">
        <div className="absolute top-0 right-0 w-40 h-40 bg-[#3B82F6] rounded-full blur-3xl -translate-y-1/2 translate-x-1/2"></div>
        <div className="absolute bottom-0 left-0 w-40 h-40 bg-[#14B8A6] rounded-full blur-3xl translate-y-1/2 -translate-x-1/2"></div>
      </div>

      <SidebarHeader className="h-20 border-b border-[#E5E7EB] flex justify-between items-center pt-4">
        <div className="flex items-center gap-3">
          <div className="h-12 w-12 rounded-xl overflow-hidden bg-[#3B82F6] p-[2px] shadow-lg shadow-[#3B82F6]/20">
            <div className="flex h-full w-full items-center justify-center rounded-[10px] bg-white dark:bg-[#1F2937]">
              <img
                src="/Logo with no Text.png?height=32&width=32"
                alt="Workforce Hub Logo"
                className="h-10 w-10"
              />
            </div>
          </div>
          <div>
            <span className="font-bold text-lg text-[#3B82F6]">
              Workforce Hub
            </span>
          </div>
        </div>
      </SidebarHeader>

      <SidebarContent className="px-5 py-6">
        <SidebarMenu className="space-y-2">
          {/* Dashboard Menu Item */}
          <SidebarMenuItem>
            <SidebarMenuButton
              asChild
              isActive={isActive("/hr/admin/dashboard")}
              className="rounded-xl transition-all duration-300 hover:bg-[#F9FAFB] dark:hover:bg-[#374151] group w-full"
            >
              <Link href="/hr/admin/dashboard" className="py-2.5">
                <div className="flex items-center gap-4 w-full">
                  <div
                    className={`h-9 w-9 rounded-lg flex items-center justify-center transition-all duration-300 ${isActive("/hr/admin/dashboard")
                      ? "bg-[#3B82F6] text-white shadow-lg shadow-[#3B82F6]/20"
                      : "bg-[#F9FAFB] text-[#6B7280] dark:bg-[#374151] group-hover:bg-[#EFF6FF] group-hover:text-[#3B82F6] dark:group-hover:bg-[#1E3A8A]/20 dark:group-hover:text-[#60A5FA]"
                      }`}
                  >
                    <Home className="h-5 w-5" />
                  </div>
                  <span
                    className={`font-medium transition-colors duration-300 ${isActive("/hr/admin/dashboard")
                      ? "text-[#1F2937] dark:text-white"
                      : "text-[#6B7280] group-hover:text-[#3B82F6] dark:group-hover:text-[#60A5FA]"
                      }`}
                  >
                    Dashboard
                  </span>
                </div>
              </Link>
            </SidebarMenuButton>
          </SidebarMenuItem>

          {/* Benefits Menu Item with Dropdown */}
          {/* <SidebarMenuItem>
            <SidebarMenuButton
              isActive={isBenefitsActive}
              className="rounded-xl transition-all duration-300 hover:bg-[#F9FAFB] dark:hover:bg-[#374151] group w-full"
              onClick={() => setBenefitsOpen(!benefitsOpen)}
            >
              <div className="flex items-center gap-4 w-full py-2.5">
                <div
                  className={`h-9 w-9 rounded-lg flex items-center justify-center transition-all duration-300 ${
                    isBenefitsActive
                      ? "bg-[#3B82F6] text-white shadow-lg shadow-[#3B82F6]/20"
                      : "bg-[#F9FAFB] text-[#6B7280] dark:bg-[#374151] group-hover:bg-[#EFF6FF] group-hover:text-[#3B82F6] dark:group-hover:bg-[#1E3A8A]/20 dark:group-hover:text-[#60A5FA]"
                  }`}
                >
                  <HandHeart className="h-5 w-5" />
                </div>
                <span
                  className={`font-medium transition-colors duration-300 ${
                    isBenefitsActive
                      ? "text-[#1F2937] dark:text-white"
                      : "text-[#6B7280] group-hover:text-[#3B82F6] dark:group-hover:text-[#60A5FA]"
                  }`}
                >
                  Benefits
                </span>
                <ChevronRight
                  className={`ml-auto h-4 w-4 transition-transform duration-300 ${
                    benefitsOpen ? "rotate-90" : ""
                  } text-[#6B7280]`}
                />
              </div>
            </SidebarMenuButton>

            {benefitsOpen && (
              <SidebarMenuSub className="mt-1 ml-4 pl-4 border-l border-[#E5E7EB] dark:border-[#4B5563]">
                <SidebarMenuSubItem>
                  <SidebarMenuSubButton
                    asChild
                    isActive={isActive("/benefits/health")}
                    className="py-2 px-3 rounded-lg"
                  >
                    <Link
                      href="/benefits/health"
                      className="flex items-center gap-3"
                    >
                      <span>Health Insurance</span>
                    </Link>
                  </SidebarMenuSubButton>
                </SidebarMenuSubItem>

                <SidebarMenuSubItem>
                  <SidebarMenuSubButton
                    asChild
                    isActive={isActive("/benefits/financial")}
                    className="py-2 px-3 rounded-lg"
                  >
                    <Link
                      href="/benefits/financial"
                      className="flex items-center gap-3"
                    >
                      <span>Financial Benefits</span>
                    </Link>
                  </SidebarMenuSubButton>
                </SidebarMenuSubItem>

                <SidebarMenuSubItem>
                  <SidebarMenuSubButton
                    asChild
                    isActive={isActive("/benefits/time-off")}
                    className="py-2 px-3 rounded-lg"
                  >
                    <Link
                      href="/benefits/time-off"
                      className="flex items-center gap-3"
                    >
                      <span>Time Off</span>
                    </Link>
                  </SidebarMenuSubButton>
                </SidebarMenuSubItem>
              </SidebarMenuSub>
            )}
          </SidebarMenuItem>*/}

          {/*<SidebarMenuItem>
            <SidebarMenuButton
              asChild
              isActive={pathname.startsWith("/career")}
              className="rounded-xl transition-all duration-300 hover:bg-[#F9FAFB] dark:hover:bg-[#374151] group w-full"
            >
              <Link href="/career" className="py-2.5">
                <div className="flex items-center gap-4 w-full">
                  <div
                    className={`h-9 w-9 rounded-lg flex items-center justify-center transition-all duration-300 ${
                      pathname.startsWith("/career")
                        ? "bg-[#3B82F6] text-white shadow-lg shadow-[#3B82F6]/20"
                        : "bg-[#F9FAFB] text-[#6B7280] dark:bg-[#374151] group-hover:bg-[#EFF6FF] group-hover:text-[#3B82F6] dark:group-hover:bg-[#1E3A8A]/20 dark:group-hover:text-[#60A5FA]"
                    }`}
                  >
                    <GraduationCap className="h-5 w-5" />
                  </div>
                  <span
                    className={`font-medium transition-colors duration-300 ${
                      pathname.startsWith("/career")
                        ? "text-[#1F2937] dark:text-white"
                        : "text-[#6B7280] group-hover:text-[#3B82F6] dark:group-hover:text-[#60A5FA]"
                    }`}
                  >
                    Career
                  </span>
                </div>
              </Link>
            </SidebarMenuButton>
          </SidebarMenuItem>*/}
        </SidebarMenu>

        {/* Decorative element at the bottom */}
        <div className="mt-12 mx-auto w-3/4 h-px bg-gradient-to-r from-transparent via-[#E5E7EB] to-transparent opacity-70"></div>
        <div className="mt-6 p-4 rounded-xl bg-[#F9FAFB] dark:bg-[#374151] border border-[#E5E7EB] dark:border-[#4B5563]">
          <div className="text-xs text-[#6B7280] mb-2">
            Admin Workforce Hub v1.2.0
          </div>
          <div className="text-xs text-[#6B7280] flex items-center">
            <div className="h-2 w-2 rounded-full bg-[#10B981] mr-2"></div>
            <span>All systems operational</span>
          </div>
        </div>
      </SidebarContent>
    </Sidebar>
  );
}
