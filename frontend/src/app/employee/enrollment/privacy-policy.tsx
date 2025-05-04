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
import { LockKeyhole, AlertCircle, Calendar } from 'lucide-react'
import { toast } from "sonner"

interface PrivacyPolicyPopupProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onAccept?: () => void
}

export default function PrivacyPolicyPopup({ open, onOpenChange, onAccept }: PrivacyPolicyPopupProps) {
  const [hasAgreed, setHasAgreed] = useState(false)
  const effectiveDate = new Date().toLocaleDateString("en-US", {
    year: "numeric",
    month: "long",
    day: "numeric",
  })

  const handleOpenChange = (newOpen: boolean) => {
    if (!hasAgreed && newOpen === false) {
      toast.error("Please accept the Privacy Policy before proceeding", {
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
        <div className="absolute top-0 left-0 right-0 h-1.5 bg-gradient-to-r from-purple-500 to-blue-400"></div>

        <DialogHeader className="p-8 pb-4 border-b border-gray-100 dark:border-gray-800">
          <div className="flex items-center gap-3 mb-3">
            <div className="h-10 w-10 rounded-full bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center">
              <LockKeyhole className="h-5 w-5 text-purple-600 dark:text-purple-400" />
            </div>
            <DialogTitle className="text-2xl font-semibold">Privacy Policy</DialogTitle>
          </div>
          <DialogDescription className="text-base text-muted-foreground">
            Workforce Hub HR Information System
          </DialogDescription>
        </DialogHeader>

        <div className="px-8 py-4">
          <div className="bg-muted/40 rounded-md p-4 mb-4 flex items-start gap-3 border border-purple-100 dark:border-purple-900/30">
            <AlertCircle className="h-5 w-5 text-purple-500 mt-0.5 flex-shrink-0" />
            <div className="space-y-1">
              <p className="text-sm font-medium text-purple-700 dark:text-purple-400">Important Notice</p>
              <p className="text-sm text-muted-foreground">
                This Privacy Policy explains how we collect, use, store, and protect your personal information when you use our system. 
                By using the System, you consent to the collection and use of your personal data as described in this Privacy Policy.
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
              <div className="h-6 w-6 rounded-full bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-purple-600 dark:text-purple-400">1</span>
              </div>
              <h3 className="text-lg font-semibold">Introduction</h3>
            </div>
            <p className="text-sm text-muted-foreground pl-8 leading-relaxed">
              Workforce Hub HR Information System (hereinafter referred to as "the System") is committed to protecting your privacy and ensuring the confidentiality of your personal data. This Privacy Policy explains how we collect, use, store, and protect your personal information when you use our system. By using the System, you consent to the collection and use of your personal data as described in this Privacy Policy.
            </p>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-purple-600 dark:text-purple-400">2</span>
              </div>
              <h3 className="text-lg font-semibold">Information We Collect</h3>
            </div>
            <div className="pl-8 space-y-2">
              <p className="text-sm text-muted-foreground leading-relaxed">
                We collect personal data in the following ways:
              </p>
              <ul className="list-disc pl-5 text-sm text-muted-foreground space-y-2 leading-relaxed">
                <li>
                  <span className="font-medium">Personal Information:</span> When you register for the System, we collect details such as your name, job title, email address, phone number, and other relevant information necessary to create and manage your user account.
                </li>
                <li>
                  <span className="font-medium">Employee Data:</span> HR staff may manage employee profiles, which may include sensitive information such as job titles, performance records, training certifications, leave balances, and other HR-related details.
                </li>
                <li>
                  <span className="font-medium">Attendance and Leave Records:</span> The System tracks your attendance, leave requests, and approvals as part of time and attendance management.
                </li>
                <li>
                  <span className="font-medium">Communication and Feedback:</span> We may collect responses to surveys, feedback forms, and communication sent to HR regarding your workplace experience, performance, or other matters.
                </li>
                <li>
                  <span className="font-medium">Device and Usage Data:</span> The System collects data related to your device, browser type, operating system, and your interactions with the System, such as login times and system usage patterns.
                </li>
              </ul>
            </div>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-purple-600 dark:text-purple-400">3</span>
              </div>
              <h3 className="text-lg font-semibold">How We Use Your Information</h3>
            </div>
            <div className="pl-8 space-y-2">
              <p className="text-sm text-muted-foreground leading-relaxed">
                We use your personal data for the following purposes:
              </p>
              <ul className="list-disc pl-5 text-sm text-muted-foreground space-y-2 leading-relaxed">
                <li>
                  <span className="font-medium">Account Management:</span> To create, update, and manage your user account within the System.
                </li>
                <li>
                  <span className="font-medium">HR Management:</span> To facilitate HR-related processes such as employee data management, leave management, performance tracking, training, and attendance records.
                </li>
                <li>
                  <span className="font-medium">System Improvement:</span> To monitor usage patterns and make improvements to the system's performance, security, and features.
                </li>
                <li>
                  <span className="font-medium">Communication:</span> To send you notifications about important updates, such as leave request approvals, training schedules, or system alerts.
                </li>
                <li>
                  <span className="font-medium">Compliance and Legal Obligations:</span> To comply with applicable laws, such as data protection and labor laws, and to ensure the accuracy of data stored within the System.
                </li>
              </ul>
            </div>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-purple-600 dark:text-purple-400">4</span>
              </div>
              <h3 className="text-lg font-semibold">Data Sharing and Disclosure</h3>
            </div>
            <div className="pl-8 space-y-2">
              <p className="text-sm text-muted-foreground leading-relaxed">
                We do not sell, rent, or share your personal data with third parties for marketing purposes. However, we may share your data under the following circumstances:
              </p>
              <ul className="list-disc pl-5 text-sm text-muted-foreground space-y-2 leading-relaxed">
                <li>
                  <span className="font-medium">With Your Employer:</span> As the System is a tool for HR management, your employer (or HR department) may access and manage your personal data, attendance, leave records, and other HR-related information.
                </li>
                <li>
                  <span className="font-medium">Service Providers:</span> We may share data with trusted third-party service providers who assist us in operating the System, such as cloud hosting, data storage, and communication services. These third parties are obligated to protect your data in accordance with this Privacy Policy.
                </li>
                <li>
                  <span className="font-medium">Legal Compliance:</span> We may disclose your personal information if required by law, such as in response to subpoenas, legal processes, or to comply with regulatory requirements.
                </li>
              </ul>
            </div>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-purple-600 dark:text-purple-400">5</span>
              </div>
              <h3 className="text-lg font-semibold">Data Security</h3>
            </div>
            <div className="pl-8 space-y-2">
              <p className="text-sm text-muted-foreground leading-relaxed">
                We implement a range of security measures to protect your personal information from unauthorized access, alteration, disclosure, or destruction, including:
              </p>
              <ul className="list-disc pl-5 text-sm text-muted-foreground space-y-2 leading-relaxed">
                <li>
                  <span className="font-medium">Encryption:</span> Your personal data is encrypted both during transmission (via HTTPS) and while stored within the System.
                </li>
                <li>
                  <span className="font-medium">Access Control:</span> The System employs role-based access control (RBAC) to ensure that only authorized personnel can access sensitive data.
                </li>
                <li>
                  <span className="font-medium">Two-Factor Authentication:</span> For added security, we recommend using two-factor authentication (2FA) for logging into the System.
                </li>
                <li>
                  <span className="font-medium">Regular Security Audits:</span> We conduct regular security audits to ensure that our systems are secure and up-to-date with industry best practices.
                </li>
              </ul>
            </div>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-purple-600 dark:text-purple-400">6</span>
              </div>
              <h3 className="text-lg font-semibold">Retention of Data</h3>
            </div>
            <p className="text-sm text-muted-foreground pl-8 leading-relaxed">
              We retain your personal data for as long as it is necessary to fulfill the purposes outlined in this Privacy Policy, or as required by law. If your personal data is no longer needed for the purposes for which it was collected, we will securely delete or anonymize the data.
            </p>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-purple-600 dark:text-purple-400">7</span>
              </div>
              <h3 className="text-lg font-semibold">Your Rights</h3>
            </div>
            <div className="pl-8 space-y-2">
              <p className="text-sm text-muted-foreground leading-relaxed">
                As a user of the System, you have the following rights regarding your personal data:
              </p>
              <ul className="list-disc pl-5 text-sm text-muted-foreground space-y-2 leading-relaxed">
                <li>
                  <span className="font-medium">Access:</span> You may request access to the personal data we hold about you.
                </li>
                <li>
                  <span className="font-medium">Correction:</span> You may request that we correct any inaccuracies in your personal data.
                </li>
                <li>
                  <span className="font-medium">Deletion:</span> You may request the deletion of your personal data, subject to certain legal and contractual obligations.
                </li>
                <li>
                  <span className="font-medium">Data Portability:</span> You may request to receive a copy of your personal data in a structured, commonly used, and machine-readable format.
                </li>
                <li>
                  <span className="font-medium">Withdrawal of Consent:</span> If you have provided consent for data processing, you may withdraw it at any time by contacting us.
                </li>
              </ul>
              <p className="text-sm text-muted-foreground leading-relaxed mt-2">
                To exercise your rights, please contact us using the information provided below.
              </p>
            </div>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-purple-600 dark:text-purple-400">8</span>
              </div>
              <h3 className="text-lg font-semibold">Third-Party Websites and Services</h3>
            </div>
            <p className="text-sm text-muted-foreground pl-8 leading-relaxed">
              The System may contain links to third-party websites or services. We are not responsible for the privacy practices or content of these external sites. We encourage you to read the privacy policies of any third-party websites you visit.
            </p>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-purple-600 dark:text-purple-400">9</span>
              </div>
              <h3 className="text-lg font-semibold">Changes to this Privacy Policy</h3>
            </div>
            <p className="text-sm text-muted-foreground pl-8 leading-relaxed">
              We reserve the right to update or modify this Privacy Policy at any time. Any changes will be communicated to you via the System or by email, and the updated Privacy Policy will be posted on this page with an updated "Effective Date." Your continued use of the System after such changes signifies your acceptance of the updated policy.
            </p>
          </section>

          <section>
            <div className="flex items-center gap-2 mb-3">
              <div className="h-6 w-6 rounded-full bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center">
                <span className="text-sm font-semibold text-purple-600 dark:text-purple-400">10</span>
              </div>
              <h3 className="text-lg font-semibold">Contact Information</h3>
            </div>
            <div className="pl-8 space-y-2">
              <p className="text-sm text-muted-foreground leading-relaxed">
                If you have any questions or concerns regarding this Privacy Policy or how we handle your personal data, please contact us at:
              </p>
              <p className="text-sm text-muted-foreground leading-relaxed">Email: privacy@workforcehub.com</p>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Address: 123 Corporate Plaza, Makati City, Philippines
              </p>
            </div>
          </section>
        </div>

        <div className="p-8 pt-6 bg-gray-50 dark:bg-gray-900/50">
          <div className="flex items-start space-x-3 mb-6">
            <Checkbox
              id="privacy"
              checked={hasAgreed}
              onCheckedChange={(checked) => setHasAgreed(checked === true)}
              className="mt-1"
            />
            <div>
              <label
                htmlFor="privacy"
                className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
              >
                I have read and agree to the Privacy Policy
              </label>
              <p className="text-xs text-muted-foreground mt-1">
                By checking this box, you acknowledge that you have read, understood, and agreed to this Privacy Policy.
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
              className="sm:flex-1 bg-gradient-to-r from-purple-500 to-blue-400 hover:from-purple-600 hover:to-blue-500 text-white"
            >
              Accept Policy
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  )
} 