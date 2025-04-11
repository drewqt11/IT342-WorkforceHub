"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { authService } from "@/lib/auth";

interface UserContextType {
  userStatus: string;
  isLoading: boolean;
  error: string | null;
  refreshUserStatus: () => Promise<void>;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export function UserProvider({ children }: { children: React.ReactNode }) {
  const [userStatus, setUserStatus] = useState<string>("");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchUserStatus = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const profile = await authService.getEmployeeProfile();
      setUserStatus(profile.status === true ? "Active" : "Inactive");
    } catch (err) {
      setError("Failed to fetch user status");
      console.error("Error fetching user status:", err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUserStatus();
  }, []);

  const refreshUserStatus = async () => {
    await fetchUserStatus();
  };

  return (
    <UserContext.Provider
      value={{
        userStatus,
        isLoading,
        error,
        refreshUserStatus,
      }}
    >
      {children}
    </UserContext.Provider>
  );
}

export function useUser() {
  const context = useContext(UserContext);
  if (context === undefined) {
    throw new Error("useUser must be used within a UserProvider");
  }
  return context;
} 