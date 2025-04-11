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
export async function PUT(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    const { id } = params
    const token = request.headers.get("authorization")
    const body = await request.json()
    const { roleId } = body

    if (!token) {
      return NextResponse.json({ message: "Authorization required" }, { status: 401 })
    }

    if (!roleId) {
      return NextResponse.json({ message: "Role ID is required" }, { status: 400 })
    }

    const normalizedRole = roleId.toUpperCase()
    
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/employees/${id}/role`, {
      method: "PUT",
      headers: {
        Authorization: token,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ roleId: normalizedRole })
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      if (response.status === 403) {
        return NextResponse.json(
          { message: "You don't have permission to update roles" },
          { status: 403 }
        )
      } else if (response.status === 404) {
        return NextResponse.json(
          { message: "Employee not found" },
          { status: 404 }
        )
      }
      return NextResponse.json(
        { message: errorData.message || "Failed to update role" },
        { status: response.status }
      )
    }

    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error("Error updating role:", error)
    return NextResponse.json(
      { message: "Internal server error" },
      { status: 500 }
    )
  }
} 