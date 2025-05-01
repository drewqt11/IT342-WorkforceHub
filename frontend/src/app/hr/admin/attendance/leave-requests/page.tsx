"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { authService } from "@/lib/auth"

export default function LeaveRequestsPage() {
  const router = useRouter()
  const [leaveRequests, setLeaveRequests] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchLeaveRequests = async () => {
      try {
        const token = authService.getToken()
        if (!token) {
          router.push("/login")
          return
        }

        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/leave-requests`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        })

        if (!response.ok) {
          throw new Error("Failed to fetch leave requests")
        }

        const data = await response.json()
        setLeaveRequests(data)
      } catch (err) {
        setError(err instanceof Error ? err.message : "An error occurred")
      } finally {
        setLoading(false)
      }
    }

    fetchLeaveRequests()
  }, [router])

  const handleApprove = async (requestId: string) => {
    try {
      const token = authService.getToken()
      if (!token) {
        router.push("/login")
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/leave-requests/${requestId}/approve`, {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        throw new Error("Failed to approve leave request")
      }

      // Refresh the list
      const updatedResponse = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/leave-requests`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      const updatedData = await updatedResponse.json()
      setLeaveRequests(updatedData)
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

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/leave-requests/${requestId}/reject`, {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        throw new Error("Failed to reject leave request")
      }

      // Refresh the list
      const updatedResponse = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/leave-requests`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      const updatedData = await updatedResponse.json()
      setLeaveRequests(updatedData)
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
      <h1 className="text-3xl font-bold mb-8">Leave Requests</h1>
      
      <div className="grid gap-6">
        {leaveRequests.map((request) => (
          <Card key={request.id}>
            <CardHeader>
              <CardTitle>Leave Request from {request.employeeName}</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <p><strong>Type:</strong> {request.type}</p>
                <p><strong>Start Date:</strong> {new Date(request.startDate).toLocaleDateString()}</p>
                <p><strong>End Date:</strong> {new Date(request.endDate).toLocaleDateString()}</p>
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
        
        {leaveRequests.length === 0 && (
          <p>No leave requests found</p>
        )}
      </div>
    </div>
  )
}
