"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { authService } from "@/lib/auth"

export default function CareerPage() {
  const router = useRouter()
  const [careerData, setCareerData] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchCareerData = async () => {
      try {
        const token = authService.getToken()
        if (!token) {
          router.push("/")
          return
        }

        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/employee/career`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        })

        if (!response.ok) {
          throw new Error("Failed to fetch career data")
        }

        const data = await response.json()
        setCareerData(data)
      } catch (err) {
        setError(err instanceof Error ? err.message : "An error occurred")
      } finally {
        setLoading(false)
      }
    }

    fetchCareerData()
  }, [router])

  if (loading) {
    return <div>Loading...</div>
  }

  if (error) {
    return <div>Error: {error}</div>
  }

  return (
    <div className="container mx-auto py-8">
      <h1 className="text-3xl font-bold mb-8">My Career</h1>
      
      <div className="grid gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Current Position</CardTitle>
          </CardHeader>
          <CardContent>
            {careerData?.currentPosition ? (
              <div>
                <p><strong>Job Title:</strong> {careerData.currentPosition.jobTitle}</p>
                <p><strong>Department:</strong> {careerData.currentPosition.department}</p>
                <p><strong>Start Date:</strong> {new Date(careerData.currentPosition.startDate).toLocaleDateString()}</p>
              </div>
            ) : (
              <p>No current position data available</p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Career History</CardTitle>
          </CardHeader>
          <CardContent>
            {careerData?.history?.length > 0 ? (
              <div className="space-y-4">
                {careerData.history.map((item: any, index: number) => (
                  <div key={index} className="border-b pb-4">
                    <p><strong>Position:</strong> {item.jobTitle}</p>
                    <p><strong>Department:</strong> {item.department}</p>
                    <p><strong>Duration:</strong> {item.duration}</p>
                  </div>
                ))}
              </div>
            ) : (
              <p>No career history available</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
} 