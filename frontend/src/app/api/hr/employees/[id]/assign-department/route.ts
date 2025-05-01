import { NextRequest, NextResponse } from "next/server"

/**
 * PUT /api/hr/employees/{id}/assign-department
 * 
 * Assigns a department to an employee.
 * 
 * @param {NextRequest} request - The request object containing the department ID in FormData
 * @param {Object} params - Route parameters containing the employee ID
 * @returns {Promise<NextResponse>} A JSON response containing the updated employee
 * @throws {401} If authorization header is missing
 * @throws {400} If department ID is missing
 * @throws {404} If employee or department is not found
 * @throws {500} If there's an internal server error
 */
export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params
    const token = request.headers.get("authorization")
    
    if (!token) {
      return NextResponse.json(
        { error: "Authorization token is required" },
        { status: 401 }
      )
    }
    
    const body = await request.json()
    const { departmentId } = body
    
    if (!departmentId) {
      return NextResponse.json(
        { error: "Department ID is required" },
        { status: 400 }
      )
    }
    
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/employees/${id}/department?departmentId=${encodeURIComponent(departmentId)}`, {
      method: "PUT",
      headers: {
        Authorization: token,
      },
    })
    
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      if (response.status === 404) {
        return NextResponse.json(
          { error: "Employee or department not found" },
          { status: 404 }
        )
      } else if (response.status === 403) {
        return NextResponse.json(
          { error: "You don't have permission to assign departments" },
          { status: 403 }
        )
      }
      return NextResponse.json(
        { error: "Failed to assign department", details: errorData },
        { status: response.status }
      )
    }
    
    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error("Error in assign department route:", error)
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    )
  }
} 