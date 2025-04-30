import { NextRequest, NextResponse } from "next/server"

/**
 * GET /api/hr/attendance/all
 * 
 * Fetches attendance logs with pagination, sorting, and filtering.
 * 
 * @param {NextRequest} request - The request object containing query parameters
 * @returns {Promise<NextResponse>} A JSON response containing the paginated attendance logs
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

    // Get query parameters
    const { searchParams } = new URL(request.url)
    const page = searchParams.get("page") || "0"
    const size = searchParams.get("size") || "10"
    const sortBy = searchParams.get("sortBy") || "timestamp"
    const direction = searchParams.get("direction") || "desc"

    // Build the URL with query parameters
    const url = new URL(`${process.env.NEXT_PUBLIC_API_URL}/hr/attendance/all`)
    url.searchParams.append("page", page)
    url.searchParams.append("size", size)
    url.searchParams.append("sortBy", sortBy)
    url.searchParams.append("direction", direction)

    const response = await fetch(url.toString(), {
      headers: {
        Authorization: authHeader,
      },
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      return NextResponse.json(
        { error: "Failed to fetch attendance logs", details: errorData },
        { status: response.status }
      )
    }

    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error("Error in attendance logs GET route:", error)
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    )
  }
} 