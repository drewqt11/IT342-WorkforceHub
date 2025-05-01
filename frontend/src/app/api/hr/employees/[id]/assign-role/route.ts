import { NextRequest, NextResponse } from "next/server"

/**
 * PUT /api/hr/employees/[id]/assign-role
 * 
 * Assigns a role to an employee.
 * 
 * @param {NextRequest} request - The request object containing the role ID in the request body
 * @param {Object} params - Route parameters containing the employee ID
 * @returns {Promise<NextResponse>} A JSON response containing the updated employee
 * @throws {401} If authorization header is missing
 * @throws {400} If role ID is missing
 * @throws {404} If employee with the given ID is not found
 * @throws {500} If there's an internal server error
 */

function getEmployeeIdFromToken(token: string): string | null {
  try {
    // Remove 'Bearer ' if present
    const jwt = token.replace(/^Bearer /, "");
    const payload = JSON.parse(Buffer.from(jwt.split('.')[1], 'base64').toString());
    console.log('Decoded JWT payload:', payload); // Debug log
    return payload.employeeId || null;
  } catch (e) {
    console.error('Failed to decode JWT:', e);
    return null;
  }
}

export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params
    const token = request.headers.get("authorization")
    const body = await request.json()
    const { roleId } = body

    if (!token) {
      return NextResponse.json(
        { error: "Authorization token is required" },
        { status: 401 }
      )
    }

    if (!roleId) {
      return NextResponse.json(
        { error: "Role ID is required" },
        { status: 400 }
      )
    }

    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/hr/employees/${id}/role`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: token,
        },
        body: JSON.stringify({ roleId }),
      }
    )

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      if (response.status === 404) {
        return NextResponse.json(
          { error: "Employee or role not found" },
          { status: 404 }
        )
      } else if (response.status === 403) {
        return NextResponse.json(
          { error: "You don't have permission to assign roles" },
          { status: 403 }
        )
      }
      return NextResponse.json(
        { error: "Failed to assign role", details: errorData },
        { status: response.status }
      )
    }

    const data = await response.json()
    // Check if the updated employee is the current user
    const currentEmployeeId = getEmployeeIdFromToken(token || "")
    console.log('API Route: id param:', id, 'currentEmployeeId from JWT:', currentEmployeeId); // Debug log
    const shouldLogout = currentEmployeeId && currentEmployeeId === id
    // Ensure the response includes the updated employee's email
    return NextResponse.json({ ...data, shouldLogout, email: data.email })
  } catch (error) {
    console.error("Error in assign-role route:", error)
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    )
  }
} 