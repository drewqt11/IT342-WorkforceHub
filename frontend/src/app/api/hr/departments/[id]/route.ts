import { NextRequest, NextResponse } from "next/server"

/**
 * GET /api/hr/departments/{id}
 * 
 * Retrieves a specific department by ID.
 * 
 * @param {NextRequest} request - The request object
 * @param {Object} params - Route parameters containing the department ID
 * @returns {Promise<NextResponse>} A JSON response containing the department details
 * @throws {401} If authorization header is missing
 * @throws {404} If department with the given ID is not found
 * @throws {500} If there's an internal server error
 */
export async function GET(
  request: NextRequest,
  context: { params: { id: string } }
) {
  try {
    const { id } = context.params
    const token = request.headers.get("authorization")
    
    if (!token) {
      return NextResponse.json(
        { error: "Authorization token is required" },
        { status: 401 }
      )
    }
    
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/departments/${id}`, {
      headers: {
        Authorization: token,
      },
    })
    
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      return NextResponse.json(
        { error: "Failed to fetch department", details: errorData },
        { status: response.status }
      )
    }
    
    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error("Error in department GET route:", error)
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    )
  }
}

/**
 * PUT /api/hr/departments/{id}
 * 
 * Updates an existing department by ID.
 * 
 * @param {NextRequest} request - The request object containing the updated department name and description in FormData
 * @param {Object} params - Route parameters containing the department ID
 * @returns {Promise<NextResponse>} A JSON response containing the updated department
 * @throws {401} If authorization header is missing
 * @throws {400} If department name is missing
 * @throws {404} If department with the given ID is not found
 * @throws {500} If there's an internal server error
 */
export async function PUT(
  request: NextRequest,
  context: { params: { id: string } }
) {
  try {
    const { id } = context.params
    const token = request.headers.get("authorization")
    
    if (!token) {
      return NextResponse.json(
        { error: "Authorization token is required" },
        { status: 401 }
      )
    }
    
    const formData = await request.formData()
    const departmentName = formData.get("departmentName")
    const description = formData.get("description")
    
    if (!departmentName) {
      return NextResponse.json(
        { error: "Department name is required" },
        { status: 400 }
      )
    }
    
    // Build the URL with query parameters
    let url = `${process.env.NEXT_PUBLIC_API_URL}/hr/departments/${id}?departmentName=${encodeURIComponent(departmentName.toString())}`
    
    // Add description if provided
    if (description) {
      url += `&description=${encodeURIComponent(description.toString())}`
    }
    
    const response = await fetch(url, {
      method: "PUT",
      headers: {
        Authorization: token,
      },
    })
    
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      if (response.status === 404) {
        return NextResponse.json(
          { error: "Department not found" },
          { status: 404 }
        )
      } else if (response.status === 403) {
        return NextResponse.json(
          { error: "You don't have permission to update this department" },
          { status: 403 }
        )
      }
      return NextResponse.json(
        { error: "Failed to update department", details: errorData },
        { status: response.status }
      )
    }
    
    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error("Error in department PUT route:", error)
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    )
  }
}

/**
 * DELETE /api/hr/departments/{id}
 * 
 * Deletes a department by ID.
 * 
 * @param {NextRequest} request - The request object
 * @param {Object} params - Route parameters containing the department ID
 * @returns {Promise<NextResponse>} A response indicating success or failure
 * @throws {401} If authorization header is missing
 * @throws {404} If department with the given ID is not found
 * @throws {500} If there's an internal server error
 */
export async function DELETE(
  request: NextRequest,
  context: { params: { id: string } }
) {
  try {
    const { id } = context.params
    const token = request.headers.get("authorization")
    
    if (!token) {
      return NextResponse.json(
        { error: "Authorization token is required" },
        { status: 401 }
      )
    }
    
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/departments/${id}`, {
      method: "DELETE",
      headers: {
        Authorization: token,
      },
    })
    
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      if (response.status === 403) {
        return NextResponse.json(
          { error: "You don't have permission to delete this department" },
          { status: 403 }
        )
      } else if (response.status === 404) {
        return NextResponse.json(
          { error: "Department not found" },
          { status: 404 }
        )
      }
      return NextResponse.json(
        { error: "Failed to delete department", details: errorData },
        { status: response.status }
      )
    }
    
    return new NextResponse(null, { status: 204 })
  } catch (error) {
    console.error("Error in department DELETE route:", error)
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    )
  }
} 