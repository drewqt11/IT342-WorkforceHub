'use client';

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useRouter } from "next/navigation";

export default function TermsPage() {
  const router = useRouter();

  return (
    <div className="container mx-auto py-8 px-4">
      <Card className="max-w-4xl mx-auto">
        <CardHeader>
          <CardTitle className="text-2xl font-bold">Terms and Conditions</CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="prose max-w-none">
            <h2 className="text-xl font-semibold mb-4">1. Acceptance of Terms</h2>
            <p className="mb-4">
              By accessing and using Workforce Hub, you accept and agree to be bound by the terms and provision of this agreement.
            </p>

            <h2 className="text-xl font-semibold mb-4">2. Use License</h2>
            <p className="mb-4">
              Permission is granted to temporarily access Workforce Hub for personal, non-commercial transitory viewing only. This is the grant of a license, not a transfer of title.
            </p>

            <h2 className="text-xl font-semibold mb-4">3. User Account</h2>
            <p className="mb-4">
              You are responsible for maintaining the confidentiality of your account and password. You agree to accept responsibility for all activities that occur under your account.
            </p>

            <h2 className="text-xl font-semibold mb-4">4. Privacy Policy</h2>
            <p className="mb-4">
              Your use of Workforce Hub is also governed by our Privacy Policy. Please review our Privacy Policy, which also governs the Site and informs users of our data collection practices.
            </p>

            <h2 className="text-xl font-semibold mb-4">5. Disclaimer</h2>
            <p className="mb-4">
              The materials on Workforce Hub are provided on an 'as is' basis. Workforce Hub makes no warranties, expressed or implied, and hereby disclaims and negates all other warranties including, without limitation, implied warranties or conditions of merchantability, fitness for a particular purpose, or non-infringement of intellectual property or other violation of rights.
            </p>

            <h2 className="text-xl font-semibold mb-4">6. Limitations</h2>
            <p className="mb-4">
              In no event shall Workforce Hub or its suppliers be liable for any damages (including, without limitation, damages for loss of data or profit, or due to business interruption) arising out of the use or inability to use the materials on Workforce Hub.
            </p>

            <h2 className="text-xl font-semibold mb-4">7. Revisions and Errata</h2>
            <p className="mb-4">
              The materials appearing on Workforce Hub could include technical, typographical, or photographic errors. Workforce Hub does not warrant that any of the materials on its website are accurate, complete, or current.
            </p>

            <h2 className="text-xl font-semibold mb-4">8. Links</h2>
            <p className="mb-4">
              Workforce Hub has not reviewed all of the sites linked to its website and is not responsible for the contents of any such linked site. The inclusion of any link does not imply endorsement by Workforce Hub of the site.
            </p>

            <h2 className="text-xl font-semibold mb-4">9. Governing Law</h2>
            <p className="mb-4">
              These terms and conditions are governed by and construed in accordance with the laws of your jurisdiction and you irrevocably submit to the exclusive jurisdiction of the courts in that location.
            </p>
          </div>

          <div className="flex justify-end mt-8">
            <Button onClick={() => router.back()}>
              Back to Login
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
} 