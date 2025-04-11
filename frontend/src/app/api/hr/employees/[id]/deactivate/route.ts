import { NextRequest, NextResponse } from 'next/server';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * PATCH /api/hr/employees/[id]/deactivate
 * 
 * Deactivates an employee account.
 * 
 * @param {NextRequest} request - The request object
 * @param {Object} params - Route parameters containing the employee ID
 * @returns {Promise<NextResponse>} A JSON response containing the deactivated employee
 * @throws {401} If authorization header is missing
 * @throws {400} If employee ID is missing
 * @throws {404} If employee with the given ID is not found
 * @throws {500} If there's an internal server error
 */
export async function PATCH(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    // Get the authorization header from the request
    const authHeader = request.headers.get('Authorization');
    
    if (!authHeader) {
      return NextResponse.json({ error: 'Authorization header is required' }, { status: 401 });
    }

    // Get the employee ID from the URL parameters
    const employeeId = params.id;
    
    if (!employeeId) {
      return NextResponse.json({ error: 'Employee ID is required' }, { status: 400 });
    }

    // Forward the request to the backend
    const response = await fetch(`${API_URL}/api/hr/employees/${employeeId}/deactivate`, {
      method: 'PATCH',
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ status: 'INACTIVE' }), // Explicitly set the status
    });

    // Get the response data
    const data = await response.json();
    
    // Return the response with the same status code
    return NextResponse.json(data, { status: response.status });
  } catch (error) {
    console.error('Error proxying request to backend:', error);
    return NextResponse.json({ error: 'Failed to deactivate employee' }, { status: 500 });
  }
} 