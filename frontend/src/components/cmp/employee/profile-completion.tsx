"use client";

import type React from "react";

import {
  User,
  ChevronRight,
  Shield,
  Award,
  Briefcase,
  Phone,
} from "lucide-react";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { useState, useEffect } from "react";
import { motion } from "framer-motion";

type ProfileItem = {
  id: string;
  name: string;
  description: string;
  status: "complete" | "incomplete";
  icon: React.ReactNode;
};

export function ProfileCompletion() {
  const [progress, setProgress] = useState(0);
  const [hoveredItem, setHoveredItem] = useState<string | null>(null);
  const [selectedItem, setSelectedItem] = useState<string | null>(null);

  const profileItems: ProfileItem[] = [
    {
      id: "personal",
      name: "Personal Information",
      description: "Basic details about yourself",
      status: "complete",
      icon: <User className="h-5 w-5" />,
    },
    {
      id: "contact",
      name: "Contact Details",
      description: "How to reach you",
      status: "complete",
      icon: <Phone className="h-5 w-5" />,
    },
    {
      id: "emergency",
      name: "Emergency Contacts",
      description: "People to contact in emergencies",
      status: "incomplete",
      icon: <Shield className="h-5 w-5" />,
    },
    {
      id: "skills",
      name: "Skills & Qualifications",
      description: "Your expertise and certifications",
      status: "incomplete",
      icon: <Award className="h-5 w-5" />,
    },
    {
      id: "experience",
      name: "Work Experience",
      description: "Your professional background",
      status: "incomplete",
      icon: <Briefcase className="h-5 w-5" />,
    },
  ];

  const completedItems = profileItems.filter(
    (item) => item.status === "complete"
  ).length;
  const completionPercentage = Math.round(
    (completedItems / profileItems.length) * 100
  );

  useEffect(() => {
    const timer = setTimeout(() => {
      setProgress(completionPercentage);
    }, 500);
    return () => clearTimeout(timer);
  }, [completionPercentage]);

  return (
    <Card
      className="w-full h-full overflow-hidden border border-[#E5E7EB] shadow-xl rounded-2xl bg-white dark:bg-[#1F2937] relative flex flex-col"
      role="region"
      aria-label="Profile Completion Status"
    >
      {" "}
      <CardHeader className="pt-6 pb-0">
        <div className="flex items-center gap-3">
          <div className="rounded-full bg-gradient-to-r from-[#14B8A6] to-[#3B82F6] p-[2px] shadow-lg shadow-[#14B8A6]/20">
            <div className="rounded-full bg-white p-2 dark:bg-[#1F2937]">
              <User className="h-8 w-8 text-[#14B8A6]" aria-hidden="true" />
            </div>
          </div>
          <h3 className="text-xl font-bold text-[#1F2937] dark:text-white">
            Profile Completion
          </h3>
        </div>
      </CardHeader>
      <CardContent className="pt-4 pb-6 flex-1 flex flex-col">
        <div className="mb-6">
          <div className="flex justify-between mb-2">
            <span className="text-sm text-[#6B7280]">Progress</span>
            <span
              className={`font-bold ${
                progress < 50 ? "text-amber-500" : "text-[#14B8A6]"
              }`}
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

          <div
            className="flex justify-between mt-1 text-xs text-[#6B7280]"
            aria-hidden="true"
          >
            <span>0%</span>
            <span>25%</span>
            <span>50%</span>
            <span>75%</span>
            <span>100%</span>
          </div>
        </div>

        <div
          className="space-y-3 flex-1 overflow-auto"
          role="list"
          aria-label="Profile completion items"
        >
          {profileItems.map((item) => (
            <motion.div
              key={item.id}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3 }}
              className={`flex items-center gap-3 p-3 rounded-xl border transition-all duration-300 cursor-pointer ${
                item.status === "complete"
                  ? "border-green-100 bg-green-50 dark:border-green-900/30 dark:bg-green-900/10"
                  : "border-amber-100 bg-amber-50 dark:border-amber-900/30 dark:bg-amber-900/10"
              } ${
                hoveredItem === item.id
                  ? "shadow-md transform -translate-y-0.5"
                  : ""
              }`}
              onMouseEnter={() => setHoveredItem(item.id)}
              onMouseLeave={() => setHoveredItem(null)}
              onClick={() =>
                setSelectedItem(selectedItem === item.id ? null : item.id)
              }
              role="listitem"
              aria-label={`${item.name}: ${
                item.status === "complete" ? "Completed" : "Incomplete"
              }`}
            >
              <div
                className={`h-10 w-10 rounded-lg flex items-center justify-center ${
                  item.status === "complete"
                    ? "bg-green-100 text-green-600 dark:bg-green-900/30 dark:text-green-400"
                    : "bg-amber-100 text-amber-600 dark:bg-amber-900/30 dark:text-amber-400"
                }`}
                aria-hidden="true"
              >
                {item.icon}
              </div>
              <div className="flex-1">
                <p className="font-medium text-[#1F2937] dark:text-white">
                  {item.name}
                </p>
                <p className="text-xs text-[#6B7280]">{item.description}</p>
              </div>
              <div className="flex items-center">
                <span
                  className={`text-xs px-3 py-1 rounded-full font-medium ${
                    item.status === "complete"
                      ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400"
                      : "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400"
                  }`}
                  role="status"
                  aria-label={`Status: ${
                    item.status === "complete" ? "Complete" : "Incomplete"
                  }`}
                >
                  {item.status === "complete" ? "Complete" : "Incomplete"}
                </span>
                {item.status === "incomplete" && (
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
          className="w-full rounded-xl py-6 mt-6 bg-gradient-to-r from-[#14B8A6] to-[#3B82F6] hover:shadow-lg hover:shadow-[#14B8A6]/20 transition-all duration-300 group"
          aria-label="Complete your profile"
        >
          <span className="text-lg font-medium group-hover:tracking-wide transition-all duration-300">
            Complete Your Profile
          </span>
        </Button>
      </CardContent>
    </Card>
  );
}
