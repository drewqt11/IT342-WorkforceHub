"use client"

import { useState } from "react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Checkbox } from "@/components/ui/checkbox"
import { Shield, AlertCircle, Calendar } from "lucide-react"
import { toast } from "sonner"

interface TermsOfServicePopupProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onAccept?: () => void
}

export default function TermsOfServicePopup({ open, onOpenChange, onAccept }: TermsOfServicePopupProps) {
  const [hasAgreed, setHasAgreed] = useState(false)
  const effectiveDate = new Date().toLocaleDateString("en-US", {
    year: "numeric",
    month: "long",
    day: "numeric",
  })

  const handleOpenChange = (newOpen: boolean) => {
    if (!hasAgreed && newOpen === false) {
      toast.error("Please accept the Terms of Service before proceeding", {
        duration: 3000,
        position: "top-center",
        style: {
          background: "#FEF2F2",
          color: "#DC2626",
          border: "1px solid #FECACA",
        },
      });
      return;
    }
    onOpenChange(newOpen);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-[912px] max-h-[90vh] p-0 overflow-y-auto">
        <div className="absolute top-0 left-0 right-0 h-1.5 bg-gradient-to-r from-blue-500 to-teal-400"></div>

        <DialogHeader className="p-8 pb-4 border-b border-gray-100 dark:border-gray-800">
          <div className="flex items-center gap-3 mb-3">
            <div className="h-10 w-10 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
              <Shield className="h-5 w-5 text-blue-600 dark:text-blue-400" />
            </div>
            <DialogTitle className="text-2xl font-semibold">Terms of Service</DialogTitle>
          </div>
          <DialogDescription className="text-base text-muted-foreground">
            Workforce Hub HR Information System
          </DialogDescription>
        </DialogHeader>

        <div className="px-8 py-4">
          <div className="bg-muted/40 rounded-md p-4 mb-4 flex items-start gap-3 border border-blue-100 dark:border-blue-900/30">
            <AlertCircle className="h-5 w-5 text-blue-500 mt-0.5 flex-shrink-0" />
            <div className="space-y-1">
              <p className="text-sm font-medium text-blue-700 dark:text-blue-400">Important Notice</p>
              <p className="text-sm text-muted-foreground">
                Please review these terms carefully before proceeding. By using the Workforce Hub HR Information System,
                you agree to comply with and be bound by the following Terms of Service.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 text-sm text-muted-foreground mb-2">
            <Calendar className="h-4 w-4" />
            <span>Effective Date: {effectiveDate}</span>
          </div>
        </div>

        <div className="space-y-8 p-8">
          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-blue-600 dark:text-blue-400">1</span>
              </div>
              <h3 className="text-lg font-semibold">Acceptance of Terms</h3>
            </div>
            <p className="text-sm text-muted-foreground pl-8 leading-relaxed">
              By accessing or using the Workforce Hub HR Information System (hereinafter referred to as "the System"),
              you agree to comply with and be bound by the following Terms of Service. If you do not agree to these
              terms, please refrain from using the System. These Terms of Service constitute a legally binding
              agreement between you and the administrators of the Workforce Hub HR Information System.
            </p>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-blue-600 dark:text-blue-400">2</span>
              </div>
              <h3 className="text-lg font-semibold">User Responsibilities</h3>
            </div>
            <div className="pl-8 space-y-2">
              <p className="text-sm text-muted-foreground leading-relaxed">
                As a user of the System, you are responsible for:
              </p>
              <ul className="list-disc pl-5 text-sm text-muted-foreground space-y-1 leading-relaxed">
                <li>Providing accurate, up-to-date information when creating or updating your account.</li>
                <li>Keeping your login credentials and other sensitive information secure and confidential.</li>
                <li>Not sharing or disclosing your login credentials to third parties.</li>
                <li>Complying with all applicable laws and regulations regarding data privacy and security.</li>
              </ul>
            </div>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-blue-600 dark:text-blue-400">3</span>
              </div>
              <h3 className="text-lg font-semibold">System Usage</h3>
            </div>
            <div className="pl-8 space-y-3">
              <p className="text-sm text-muted-foreground leading-relaxed">
                The System provides functionalities related to employee data management, attendance tracking, leave
                requests, recruitment, training, and other HR-related processes. You may access and use the System
                based on your role and permissions granted by your organization.
              </p>
              <div className="space-y-2">
                <p className="text-sm font-medium">User Roles:</p>
                <ul className="list-disc pl-5 text-sm text-muted-foreground space-y-1 leading-relaxed">
                  <li>
                    <span className="font-medium">Employees:</span> Can view and update their personal profile,
                    request time off, track attendance and training progress, and respond to feedback surveys.
                  </li>
                  <li>
                    <span className="font-medium">HR Admins:</span> Can manage employee records, approve or deny leave
                    requests, handle recruitment processes, and monitor training progress.
                  </li>
                  <li>
                    <span className="font-medium">System Admins:</span> Have full access to all features and settings
                    within the System to ensure its smooth operation.
                  </li>
                </ul>
              </div>
            </div>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-blue-600 dark:text-blue-400">4</span>
              </div>
              <h3 className="text-lg font-semibold">User Accounts and Security</h3>
            </div>
            <div className="pl-8 space-y-2">
              <p className="text-sm text-muted-foreground leading-relaxed">
                <span className="font-medium">Account Registration:</span> Users are required to create an account to
                access the System. You agree to provide accurate and complete information during registration.
              </p>
              <p className="text-sm text-muted-foreground leading-relaxed">
                <span className="font-medium">Security:</span> The System implements two-factor authentication (2FA)
                and OAuth for secure login. You are responsible for maintaining the confidentiality of your account
                credentials.
              </p>
              <p className="text-sm text-muted-foreground leading-relaxed">
                <span className="font-medium">Account Termination:</span> The administrators reserve the right to
                suspend or terminate user accounts if they violate these Terms of Service or for any other legitimate
                business purpose.
              </p>
            </div>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-blue-600 dark:text-blue-400">5</span>
              </div>
              <h3 className="text-lg font-semibold">Data Privacy and Confidentiality</h3>
            </div>
            <div className="pl-8 space-y-2">
              <p className="text-sm text-muted-foreground leading-relaxed">
                We take your privacy seriously. The System stores and processes personal data in compliance with
                applicable data privacy laws, including the Data Privacy Act of 2012 (Republic Act No. 10173) and the
                General Data Protection Regulation (GDPR), as applicable.
              </p>
              <p className="text-sm text-muted-foreground leading-relaxed">
                The System will not share your personal data with third parties without your consent unless required
                by law or for system functionality purposes (e.g., payroll processing, email notifications).
              </p>
              <p className="text-sm text-muted-foreground leading-relaxed">
                You have the right to request access to, correction, or deletion of your personal data in accordance
                with our data privacy policy.
              </p>
            </div>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-blue-600 dark:text-blue-400">6</span>
              </div>
              <h3 className="text-lg font-semibold">Acceptable Use</h3>
            </div>
            <div className="pl-8">
              <p className="text-sm text-muted-foreground mb-2 leading-relaxed">You agree not to:</p>
              <ul className="list-disc pl-5 text-sm text-muted-foreground space-y-1 leading-relaxed">
                <li>Use the System for any unlawful purpose.</li>
                <li>Access or attempt to access data not meant for you or any unauthorized systems or networks.</li>
                <li>
                  Upload, share, or transmit any harmful or malicious content (e.g., viruses, malware, or any other
                  harmful code).
                </li>
              </ul>
            </div>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-blue-600 dark:text-blue-400">7</span>
              </div>
              <h3 className="text-lg font-semibold">System Availability and Maintenance</h3>
            </div>
            <div className="pl-8 space-y-2">
              <p className="text-sm text-muted-foreground leading-relaxed">
                The System will be available 24/7 with minimal downtime for maintenance. However, we do not guarantee
                that the System will be free from interruptions or errors at all times.
              </p>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Regular maintenance will be communicated to users in advance, but emergency downtime may occur without
                prior notice.
              </p>
              <p className="text-sm text-muted-foreground leading-relaxed">
                We are not liable for any data loss, disruptions, or damages resulting from downtime, errors, or
                system unavailability.
              </p>
            </div>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-blue-600 dark:text-blue-400">8</span>
              </div>
              <h3 className="text-lg font-semibold">Limitation of Liability</h3>
            </div>
            <div className="pl-8 space-y-2">
              <p className="text-sm text-muted-foreground leading-relaxed">
                The System is provided "as-is" and "as available," and we do not warrant that the System will meet
                your requirements or be error-free.
              </p>
              <p className="text-sm text-muted-foreground leading-relaxed">
                We are not liable for any direct, indirect, incidental, special, or consequential damages arising out
                of the use or inability to use the System.
              </p>
              <p className="text-sm text-muted-foreground leading-relaxed">
                We are not responsible for third-party integrations or any other external services that may be linked
                to or used with the System.
              </p>
            </div>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-blue-600 dark:text-blue-400">9</span>
              </div>
              <h3 className="text-lg font-semibold">Modifications to the Terms of Service</h3>
            </div>
            <p className="text-sm text-muted-foreground pl-8 leading-relaxed">
              We reserve the right to update or modify these Terms of Service at any time. Any changes will be
              communicated to users, and continued use of the System after such changes signifies your acceptance of
              the updated terms.
            </p>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-blue-600 dark:text-blue-400">10</span>
              </div>
              <h3 className="text-lg font-semibold">Termination</h3>
            </div>
            <p className="text-sm text-muted-foreground pl-8 leading-relaxed">
              We may suspend or terminate your access to the System at any time, with or without cause, if we believe
              you have violated these Terms of Service.
            </p>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-blue-600 dark:text-blue-400">11</span>
              </div>
              <h3 className="text-lg font-semibold">Governing Law</h3>
            </div>
            <p className="text-sm text-muted-foreground pl-8 leading-relaxed">
              These Terms of Service are governed by the laws of the Philippines. Any disputes arising from the use of
              the System shall be resolved in the competent courts located in the Philippines.
            </p>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-blue-600 dark:text-blue-400">12</span>
              </div>
              <h3 className="text-lg font-semibold">Contact Information</h3>
            </div>
            <div className="pl-8 space-y-2">
              <p className="text-sm text-muted-foreground leading-relaxed">
                If you have any questions regarding these Terms of Service, please contact us at:
              </p>
              <p className="text-sm text-muted-foreground leading-relaxed">Email: support@workforcehub.com</p>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Address: 123 Corporate Plaza, Makati City, Philippines
              </p>
            </div>
          </section>
        </div>

        <div className="p-8 pt-6 bg-gray-50 dark:bg-gray-900/50">
          <div className="flex items-start space-x-3 mb-6">
            <Checkbox
              id="terms"
              checked={hasAgreed}
              onCheckedChange={(checked) => setHasAgreed(checked === true)}
              className="mt-1"
            />
            <div>
              <label
                htmlFor="terms"
                className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
              >
                I have read and agree to the Terms of Service
              </label>
              <p className="text-xs text-muted-foreground mt-1">
                By checking this box, you acknowledge that you have read, understood, and agreed to these Terms of
                Service.
              </p>
            </div>
          </div>

          <DialogFooter className="flex flex-col sm:flex-row gap-2">
            <Button 
              variant="outline" 
              onClick={() => {
                if (hasAgreed) {
                  onOpenChange(false);
                }
              }} 
              className="sm:flex-1"
              disabled={!hasAgreed}
            >
              Decline
            </Button>
            <Button
              onClick={() => {
                onOpenChange(false);
                onAccept?.();
              }}
              disabled={!hasAgreed}
              className="sm:flex-1 bg-gradient-to-r from-blue-500 to-teal-400 hover:from-blue-600 hover:to-teal-500 text-white"
            >
              Accept Terms
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  )
} 