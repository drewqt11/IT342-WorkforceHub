import { NextRequest, NextResponse } from "next/server"

/**
 * GET /api/hr/departments
 * 
 * Fetches all departments from the backend.
 * 
 * @returns {Promise<NextResponse>} A JSON response containing the list of departments
 * @throws {401} If authorization header is missing
 * @throws {500} If there's an internal server error
 */
export async function GET(request: NextRequest) {
  try {
    const authHeader = request.headers.get("authorization")
    
    if (!authHeader) {
      return NextResponse.json(
        { error: "Authorization header is required" },
        { status: 401 }
      )
    }
    
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/departments`, {
      headers: {
        Authorization: authHeader,
      },
    })
    
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      return NextResponse.json(
        { error: "Failed to fetch departments", details: errorData },
        { status: response.status }
      )
    }
    
    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error("Error in departments GET route:", error)
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    )
  }
}

/**
 * POST /api/hr/departments
 * 
 * Creates a new department in the system.
 * 
 * @param {NextRequest} request - The request object containing the department name and description in FormData
 * @returns {Promise<NextResponse>} A JSON response containing the created department
 * @throws {401} If authorization header is missing
 * @throws {400} If department name is missing
 * @throws {500} If there's an internal server error
 */
export async function POST(request: NextRequest) {
  try {
    const authHeader = request.headers.get("authorization")
    
    if (!authHeader) {
      return NextResponse.json(
        { error: "Authorization header is required" },
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
    let url = `${process.env.NEXT_PUBLIC_API_URL}/hr/departments?departmentName=${encodeURIComponent(departmentName.toString())}`
    
    // Add description if provided
    if (description) {
      url += `&description=${encodeURIComponent(description.toString())}`
    }
    
    const response = await fetch(url, {
      method: "POST",
      headers: {
        Authorization: authHeader,
      },
    })
    
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      return NextResponse.json(
        { error: "Failed to create department", details: errorData },
        { status: response.status }
      )
    }
    
    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error("Error in departments POST route:", error)
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    )
  }
} 