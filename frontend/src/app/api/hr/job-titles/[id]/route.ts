import { NextRequest, NextResponse } from "next/server";

/**
 * DELETE /api/hr/job-titles/{id}
 * Deletes a specific job title by ID.
 */
export async function DELETE(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  const { id: jobId } = await params;
  try {
    const authHeader = request.headers.get("authorization");
    if (!authHeader) {
      return NextResponse.json(
        { error: "Authorization header is required" },
        { status: 401 }
      );
    }
    if (!jobId) {
      return NextResponse.json(
        { error: "Job ID is required" },
        { status: 400 }
      );
    }
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/job-titles/${jobId}`, {
      method: "DELETE",
      headers: {
        Authorization: authHeader,
      },
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      return NextResponse.json(
        { error: "Failed to delete job title", details: errorData },
        { status: response.status }
      );
    }
    return NextResponse.json({ message: "Job title deleted successfully" });
  } catch (error) {
    console.error("Error in job-titles DELETE route:", error);
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    );
  }
}

/**
 * PUT /api/hr/job-titles/{id}
 * Updates a specific job title by ID.
 */
export async function PUT(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  const { id: jobId } = await params;
  try {
    const authHeader = request.headers.get("authorization");
    if (!authHeader) {
      return NextResponse.json({ error: "Authorization header is required" }, { status: 401 });
    }
    if (!jobId) {
      return NextResponse.json({ error: "Job ID is required" }, { status: 400 });
    }
    const body = await request.json();
    console.log('PUT job title body:', body);
    const { jobName, jobDescription, payGrade, departmentId } = body;
    if (!jobName || !departmentId) {
      return NextResponse.json({ error: "Job name and departmentId are required" }, { status: 400 });
    }
    // Build the URL with query parameters
    let updateUrl = `${process.env.NEXT_PUBLIC_API_URL}/hr/job-titles/${jobId}?jobName=${encodeURIComponent(jobName)}&departmentId=${encodeURIComponent(departmentId)}`;
    if (jobDescription) updateUrl += `&jobDescription=${encodeURIComponent(jobDescription)}`;
    if (payGrade) updateUrl += `&payGrade=${encodeURIComponent(payGrade)}`;
    console.log('PUT job title backend URL:', updateUrl);
    const response = await fetch(updateUrl, {
      method: "PUT",
      headers: { Authorization: authHeader },
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      return NextResponse.json({ error: "Failed to update job title", details: errorData }, { status: response.status });
    }
    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error("Error in job-titles PUT route:", error);
    return NextResponse.json({ error: "Internal server error" }, { status: 500 });
  }
} 