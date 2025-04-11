import { NextRequest, NextResponse } from 'next/server';

/**
 * GET /api/hr/employees
 * 
 * Fetches all employees from the backend.
 * 
 * @param {NextRequest} request - The request object
 * @returns {Promise<NextResponse>} A JSON response containing the list of employees
 * @throws {401} If authorization header is missing
 * @throws {500} If there's an internal server error
 */
export async function GET(request: NextRequest) {
  try {
    // Get the authorization header from the request
    const authHeader = request.headers.get('Authorization');
    
    if (!authHeader) {
      return NextResponse.json({ error: 'Authorization header is required' }, { status: 401 });
    }

    // Forward the request to the backend
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hr/employees`, {
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json',
      },
    });

    // Get the response data
    const data = await response.json();
    
    // Return the response with the same status code
    return NextResponse.json(data, { status: response.status });
  } catch (error) {
    console.error('Error proxying request to backend:', error);
    return NextResponse.json({ error: 'Failed to fetch employees' }, { status: 500 });
  }
} 