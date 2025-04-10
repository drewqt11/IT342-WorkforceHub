"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowRight, Shield } from "lucide-react";

import { Button } from "@/components/ui/button";
import { authService } from "@/lib/auth";

export default function MicrosoftLoginForm() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);

  const handleMicrosoftLogin = async () => {
    setIsLoading(true);
    try {
      // Redirect to Microsoft login
      await authService.loginWithMicrosoft();
      // No need to redirect to dashboard here as the OAuth2 flow will handle that
    } catch (error) {
      console.error("Authentication error:", error);
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-[#F9FAFB] p-4">
      <div className="relative mx-auto w-full max-w-md">
        {/* Decorative elements */}
        <div className="absolute -left-10 -top-10 h-20 w-20 rounded-full bg-gradient-to-br from-[#3B82F6] to-[#93C5FD] opacity-70 blur-lg"></div>
        <div className="absolute -bottom-8 -right-8 h-16 w-16 rounded-full bg-gradient-to-br from-[#14B8A6] to-[#5EEAD4] opacity-70 blur-lg"></div>

        {/* Logo and Header */}
        <div className="relative mb-8 text-center">
          <div className="mx-auto mb-3 h-20 w-20 overflow-hidden rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] p-[3px] shadow-lg">
            <div className="flex h-full w-full items-center justify-center rounded-full bg-white">
              <img
                src="/Logo with no Text.png/?height=80&width=80"
                alt="Workforce Hub Logo"
                className="h-16 w-16 align-middle"
              />
            </div>
          </div>
          <h1 className="text-2xl font-bold uppercase tracking-wider text-[#1F2937]">
            Workforce Hub
          </h1>
          <p className="text-xs uppercase tracking-widest text-[#6B7280] pt-2">
            Enterprise Portal
          </p>
        </div>

        {/* Auth Card */}
        <div className="overflow-hidden rounded-xl shadow-lg">
          {/* Card Header with gradient */}
          <div className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] px-6 py-3 text-center">
            <p className="font-medium text-white">Enterprise Authentication</p>
          </div>

          {/* Card Body */}
          <div className="relative bg-white p-8">
            {/* Subtle background pattern */}
            <div className="absolute inset-0 opacity-5">
              <div className="absolute -right-4 -top-4 h-32 w-32 rounded-full border-8 border-[#14B8A6]"></div>
              <div className="absolute -bottom-4 -left-4 h-24 w-24 rounded-full border-8 border-[#3B82F6]"></div>
            </div>

            <div className="relative mb-6 text-center">
              <h2 className="mb-1 text-2xl font-semibold text-[#1F2937]">
                Welcome to Workforce Hub
              </h2>
              <p className="text-sm text-[#6B7280]">
                Access your enterprise dashboard securely with Microsoft
              </p>
            </div>

            <div className="space-y-6">
              {/* Microsoft Sign In Card */}
              <div className="rounded-lg border border-[#E5E7EB] bg-white p-5 shadow-sm">
                <div className="mb-4 flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="flex h-10 w-10 items-center justify-center rounded-md bg-[#f3f4f6]">
                      <svg
                        className="h-6 w-6"
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 23 23"
                      >
                        <path fill="#f35325" d="M1 1h10v10H1z" />
                        <path fill="#81bc06" d="M12 1h10v10H12z" />
                        <path fill="#05a6f0" d="M1 12h10v10H1z" />
                        <path fill="#ffba08" d="M12 12h10v10H12z" />
                      </svg>
                    </div>
                    <div>
                      <h3 className="font-medium text-[#1F2937]">
                        Microsoft Authentication
                      </h3>
                      <p className="text-xs text-[#6B7280]">
                        Secure single sign-on
                      </p>
                    </div>
                  </div>
                  <Shield className="h-5 w-5 text-[#3B82F6]" />
                </div>

                <p className="mb-4 text-sm text-[#6B7280]">
                  Sign in with your organization's Microsoft account to access
                  all enterprise resources securely.
                </p>

                {/* Microsoft Button - Highlighted */}
                <div className="relative mt-2">
                  {/* Animated glow effect */}
                  <div className="absolute -inset-0.5 animate-pulse rounded-lg bg-gradient-to-r from-[#0078D4] via-[#50a4e6] to-[#0078D4] opacity-75 blur-sm transition duration-1000"></div>

                  <Button
                    type="button"
                    onClick={handleMicrosoftLogin}
                    className="relative w-full border-0 bg-[#0078D4] hover:bg-[#106EBE] py-6 font-medium text-white"
                    disabled={isLoading}
                  >
                    <div className="flex w-full items-center justify-between">
                      <div className="flex items-center">
                        <div className="mr-3 flex h-8 w-8 items-center justify-center bg-white rounded-sm">
                          <svg
                            className="h-6 w-6"
                            xmlns="http://www.w3.org/2000/svg"
                            viewBox="0 0 23 23"
                          >
                            <path fill="#f35325" d="M1 1h10v10H1z" />
                            <path fill="#81bc06" d="M12 1h10v10H12z" />
                            <path fill="#05a6f0" d="M1 12h10v10H1z" />
                            <path fill="#ffba08" d="M12 12h10v10H12z" />
                          </svg>
                        </div>
                        <span className="text-base">
                          {isLoading
                            ? "Authenticating..."
                            : "Continue with Microsoft Account"}
                        </span>
                      </div>
                      <ArrowRight className="ml-3 h-5 w-5 mr-4 text-white" />
                    </div>
                  </Button>
                </div>
              </div>

              {/* Benefits Section */}
              <div className="rounded-lg border border-[#E5E7EB] bg-[#F9FAFB] p-4">
                <h4 className="mb-3 font-medium text-[#1F2937]">
                  Enterprise Benefits:
                </h4>
                <ul className="space-y-2 text-sm">
                  <li className="flex items-start space-x-2">
                    <div className="mt-0.5 h-4 w-4 flex-shrink-0 rounded-full bg-[#3B82F6] text-white flex items-center justify-center">
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="10"
                        height="10"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="3"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                      >
                        <polyline points="20 6 9 17 4 12"></polyline>
                      </svg>
                    </div>
                    <span className="text-[#4B5563]">
                      Single sign-on across all enterprise applications
                    </span>
                  </li>
                  <li className="flex items-start space-x-2">
                    <div className="mt-0.5 h-4 w-4 flex-shrink-0 rounded-full bg-[#3B82F6] text-white flex items-center justify-center">
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="10"
                        height="10"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="3"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                      >
                        <polyline points="20 6 9 17 4 12"></polyline>
                      </svg>
                    </div>
                    <span className="text-[#4B5563]">
                      Enhanced security with your organization's policies
                    </span>
                  </li>
                  <li className="flex items-start space-x-2">
                    <div className="mt-0.5 h-4 w-4 flex-shrink-0 rounded-full bg-[#3B82F6] text-white flex items-center justify-center">
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="10"
                        height="10"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="3"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                      >
                        <polyline points="20 6 9 17 4 12"></polyline>
                      </svg>
                    </div>
                    <span className="text-[#4B5563]">
                      Seamless access to all company resources
                    </span>
                  </li>
                </ul>
              </div>
            </div>
          </div>

          {/* Card Footer */}
          <div className="border-t border-[#E5E7EB] bg-gradient-to-r from-[#F9FAFB] to-white px-6 py-3 text-center">
            <p className="text-sm text-[#6B7280]">
              Contact your IT administrator for support
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
