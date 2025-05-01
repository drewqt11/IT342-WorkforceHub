import { NextResponse } from 'next/server';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * PATCH /api/hr/employees/[id]/activate
 * 
 * Activates an employee account.
 * 
 * @param {Request} request - The request object
 * @param {Object} context - Route parameters containing the employee ID
 * @returns {Promise<NextResponse>} A JSON response containing the activated employee
 * @throws {401} If authorization header is missing
 * @throws {400} If employee ID is missing
 * @throws {404} If employee with the given ID is not found
 * @throws {500} If there's an internal server error
 */
export async function PATCH(
  request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    // Get the authorization header from the request
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return NextResponse.json({ error: 'Authorization header is required' }, { status: 401 });
    }

    // Get the employee ID from the URL parameters
    const { id: employeeId } = await params;

    if (!employeeId) {
      return NextResponse.json({ error: 'Employee ID is required' }, { status: 400 });
    }

    // Forward the request to the backend
    const response = await fetch(`${API_URL}/api/hr/employees/${employeeId}/activate`, {
      method: 'PATCH',
      headers: {
        'Authorization': authHeader,
      },
    });

    // Get the response data
    const data = await response.json();

    // Return the response with the same status code
    return NextResponse.json(data, { status: response.status });
  } catch (error) {
    console.error('Error activating employee:', error);
    return NextResponse.json(
      { error: 'Failed to activate employee' },
      { status: 500 }
    );
  }
} 