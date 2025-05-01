import { NextRequest, NextResponse } from "next/server"

/**
 * GET /api/hr/departments/{id}/job-titles
 * 
 * Fetches all job titles for a specific department.
 * 
 * @param {NextRequest} request - The request object
 * @param {Object} params - The route parameters containing the department ID
 * @returns {Promise<NextResponse>} A JSON response containing the list of job titles
 * @throws {401} If authorization header is missing
 * @throws {500} If there's an internal server error
 */
export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id: departmentId } = await params;
    const authHeader = request.headers.get("authorization")
    
    if (!authHeader) {
      return NextResponse.json(
        { error: "Authorization header is required" },
        { status: 401 }
      )
    }

    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/job-titles/department/${departmentId}`, {
      headers: {
        Authorization: authHeader,
      },
    })
    
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      return NextResponse.json(
        { error: "Failed to fetch job titles", details: errorData },
        { status: response.status }
      )
    }
    
    const data = await response.json()
    
    // Transform the data to break circular references
    const transformedData = data.map((job: any) => ({
      jobId: job.jobId,
      jobName: job.jobName,
      jobDescription: job.jobDescription,
      payGrade: job.payGrade,
      departmentId: job.departmentId
    }))
    
    return NextResponse.json(transformedData)
  } catch (error) {
    console.error("Error in job titles GET route:", error)
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    )
  }
}

/**
 * POST /api/hr/departments/{id}/job-titles
 * 
 * Creates a new job title for a specific department.
 * 
 * @param {NextRequest} request - The request object containing the job title data in FormData
 * @param {Object} params - The route parameters containing the department ID
 * @returns {Promise<NextResponse>} A JSON response containing the created job title
 * @throws {401} If authorization header is missing
 * @throws {400} If required fields are missing
 * @throws {500} If there's an internal server error
 */
export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id: departmentId } = await params;
    const authHeader = request.headers.get("authorization")
    
    if (!authHeader) {
      return NextResponse.json(
        { error: "Authorization header is required" },
        { status: 401 }
      )
    }
    
    const formData = await request.formData()
    const jobName = formData.get("jobName")
    const jobDescription = formData.get("jobDescription")
    const payGrade = formData.get("payGrade")
    
    if (!jobName) {
      return NextResponse.json(
        { error: "Job name is required" },
        { status: 400 }
      )
    }
    
    // Build the URL with query parameters
    let url = `${process.env.NEXT_PUBLIC_API_URL}/hr/job-titles?jobName=${encodeURIComponent(jobName.toString())}&departmentId=${departmentId}`
    
    // Add optional parameters if provided
    if (jobDescription) {
      url += `?jobDescription=${encodeURIComponent(jobDescription.toString())}`
    }
    if (payGrade) {
      url += `&payGrade=${encodeURIComponent(payGrade.toString())}`
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
        { error: "Failed to create job title", details: errorData },
        { status: response.status }
      )
    }
    
    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error("Error in job titles POST route:", error)
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    )
  }
} 