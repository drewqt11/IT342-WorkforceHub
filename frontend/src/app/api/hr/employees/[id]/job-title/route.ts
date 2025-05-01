import { NextRequest, NextResponse } from "next/server"

/**
 * PATCH /api/hr/employees/{employeeId}/job-title
 * 
 * Updates an employee's job title.
 * 
 * @param {NextRequest} request - The request object containing the job title ID
 * @param {Object} params - Route parameters containing the employee ID
 * @returns {Promise<NextResponse>} A success response
 * @throws {401} If authorization header is missing
 * @throws {400} If job title ID is missing
 * @throws {500} If there's an internal server error
 */
export async function PATCH(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const authHeader = request.headers.get("authorization")
    
    if (!authHeader) {
      return NextResponse.json(
        { error: "Authorization header is required" },
        { status: 401 }
      )
    }

    const { id: jobId } = await request.json()
    
    if (!jobId) {
      return NextResponse.json(
        { error: "Job title ID is required" },
        { status: 400 }
      )
    }

    const { id } = await params;
    if (!id) {
      return NextResponse.json(
        { error: "Employee ID is required" },
        { status: 400 }
      )
    }

    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/hr/employees/${id}/job-title`,
      {
        method: "PATCH",
        headers: {
          Authorization: authHeader,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ jobId })
      }
    )

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      return NextResponse.json(
        { error: "Failed to update employee job title", details: errorData },
        { status: response.status }
      )
    }

    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error("Error in employee job title PATCH route:", error)
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    )
  }
} 