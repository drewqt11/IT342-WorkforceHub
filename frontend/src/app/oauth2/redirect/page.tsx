"use client";

import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { authService } from "@/lib/auth";
import { Suspense } from "react";

function OAuth2RedirectContent() {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const handleRedirect = async () => {
      try {
        const token = searchParams.get("token");
        const userId = searchParams.get("userId");
        const email = searchParams.get("email");
        const role = searchParams.get("role");

        if (!token || !email || !role) {
          router.push("/");
          authService.logout();
        }

        document.cookie = `token=${token}; path=/; secure; samesite=strict`;

        if (userId) {
          document.cookie = `userId=${userId}; path=/; secure; samesite=strict`;
        }

        if (email) {
          document.cookie = `email=${email}; path=/; secure; samesite=strict`;
        }

        if (role) {
          document.cookie = `role=${role}; path=/; secure; samesite=strict`;
        }

        if (role?.toLowerCase() === "hr administrator") {
          router.push("/hr/admin/dashboard");
        } else if (role?.toLowerCase() === "employee") {
          router.push("/employee/dashboard");
        } else {
          router.push("/");
          authService.logout();
        }
      } catch (error) {
        router.push("/");
        authService.logout();
      }
    };

    handleRedirect();
  }, [router, searchParams]);

  return null; // Nothing visible; loading UI is in the parent
}

export default function OAuth2Redirect() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-900 dark:to-gray-800">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 dark:border-white mx-auto"></div>
        <p className="mt-4 text-gray-600 dark:text-gray-300">
          Completing authentication...
        </p>
      </div>
      <Suspense fallback={null}>
        <OAuth2RedirectContent />
      </Suspense>
    </div>
  );
}
