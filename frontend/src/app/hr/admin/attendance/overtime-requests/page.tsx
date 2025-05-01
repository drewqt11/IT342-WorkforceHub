"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { authService } from "@/lib/auth"

export default function OvertimeRequestsPage() {
  const router = useRouter()
  const [overtimeRequests, setOvertimeRequests] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchOvertimeRequests = async () => {
      try {
        const token = authService.getToken()
        if (!token) {
          router.push("/login")
          return
        }

        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/overtime-requests`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        })

        if (!response.ok) {
          throw new Error("Failed to fetch overtime requests")
        }

        const data = await response.json()
        setOvertimeRequests(data)
      } catch (err) {
        setError(err instanceof Error ? err.message : "An error occurred")
      } finally {
        setLoading(false)
      }
    }

    fetchOvertimeRequests()
  }, [router])

  const handleApprove = async (requestId: string) => {
    try {
      const token = authService.getToken()
      if (!token) {
        router.push("/login")
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/overtime-requests/${requestId}/approve`, {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        throw new Error("Failed to approve overtime request")
      }

      // Refresh the list
      const updatedResponse = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/overtime-requests`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      const updatedData = await updatedResponse.json()
      setOvertimeRequests(updatedData)
    } catch (err) {
      setError(err instanceof Error ? err.message : "An error occurred")
    }
  }

  const handleReject = async (requestId: string) => {
    try {
      const token = authService.getToken()
      if (!token) {
        router.push("/login")
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/overtime-requests/${requestId}/reject`, {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        throw new Error("Failed to reject overtime request")
      }

      // Refresh the list
      const updatedResponse = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/overtime-requests`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      const updatedData = await updatedResponse.json()
      setOvertimeRequests(updatedData)
    } catch (err) {
      setError(err instanceof Error ? err.message : "An error occurred")
    }
  }

  if (loading) {
    return <div>Loading...</div>
  }

  if (error) {
    return <div>Error: {error}</div>
  }

  return (
    <div className="container mx-auto py-8">
      <h1 className="text-3xl font-bold mb-8">Overtime Requests</h1>
      
      <div className="grid gap-6">
        {overtimeRequests.map((request) => (
          <Card key={request.id}>
            <CardHeader>
              <CardTitle>Overtime Request from {request.employeeName}</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <p><strong>Date:</strong> {new Date(request.date).toLocaleDateString()}</p>
                <p><strong>Hours:</strong> {request.hours}</p>
                <p><strong>Reason:</strong> {request.reason}</p>
                <p><strong>Status:</strong> {request.status}</p>
                
                {request.status === "PENDING" && (
                  <div className="flex gap-4 mt-4">
                    <Button onClick={() => handleApprove(request.id)} variant="default">
                      Approve
                    </Button>
                    <Button onClick={() => handleReject(request.id)} variant="destructive">
                      Reject
                    </Button>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        ))}
        
        {overtimeRequests.length === 0 && (
          <p>No overtime requests found</p>
        )}
      </div>
    </div>
  )
}
