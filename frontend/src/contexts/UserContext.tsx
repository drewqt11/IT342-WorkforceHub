"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { authService } from "@/lib/auth";

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  status: boolean;
  idNumber?: string;
}

interface UserContextType {
  user: User | null;
  setUser: (user: User | null) => void;
  loading: boolean;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export function UserProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUser = async () => {
      try {
        // Check if we have an authentication token first
        const token = authService.getToken();
        if (!token) {
          setUser(null);
          setLoading(false);
          return;
        }

        const profile = await authService.getEmployeeProfile();
        setUser({
          id: profile.id,
          email: profile.email,
          firstName: profile.firstName,
          lastName: profile.lastName,
          role: profile.role,
          status: profile.status,
          idNumber: profile.idNumber,
        });
      } catch (error) {
        console.error("Error fetching user:", error);
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, []);

  return (
    <UserContext.Provider value={{ user, setUser, loading }}>
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