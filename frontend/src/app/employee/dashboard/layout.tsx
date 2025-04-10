import type React from "react";
import { AppSidebar } from "@/components/cmp/employee/sidebar";
import { Navbar } from "@/components/cmp/employee/navbar";
import { SidebarProvider } from "@/components/ui/sidebar";

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <SidebarProvider defaultOpen={true}>
      <div className="flex h-screen w-full overflow-hidden">
        <AppSidebar />
        <div className="flex-1 flex flex-col overflow-hidden relative w-full">
          {/* Background with the specified light gray and pattern */}
          <div className="absolute inset-0 bg-[#F9FAFB] dark:bg-[#111827] pointer-events-none">
            <div className="absolute inset-0 bg-grid-slate-100 dark:bg-grid-slate-700/20 bg-[center_top_-1px] [mask-image:linear-gradient(0deg,transparent,black)]"></div>
          </div>

          <Navbar />
          <main className="flex-1 overflow-auto relative w-full">
            {children}
          </main>
        </div>
      </div>
    </SidebarProvider>
  );
}
