import { NextRequest, NextResponse } from 'next/server'

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

/**
 * PUT /api/hr/user-accounts/[email]/account/[action]
 * 
 * Activates or deactivates a user account.
 * 
 * @param {NextRequest} request - The request object
 * @param {Object} params - Route parameters containing the email and action
 * @returns {Promise<NextResponse>} A JSON response indicating success or failure
 * @throws {401} If authorization header is missing
 * @throws {400} If email or action is missing
 * @throws {404} If user with the given email is not found
 * @throws {500} If there's an internal server error
 */
export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ email: string; action: string }> }
) {
  try {
    // Get the authorization header from the request
    const authHeader = request.headers.get('Authorization')
    
    if (!authHeader) {
      return NextResponse.json({ error: 'Authorization header is required' }, { status: 401 })
    }

    // Get the email and action from the URL parameters
    const resolvedParams = await params
    const { email, action } = resolvedParams
    
    if (!email || !action) {
      return NextResponse.json({ error: 'Email and action are required' }, { status: 400 })
    }

    if (action !== 'activate' && action !== 'deactivate') {
      return NextResponse.json({ error: 'Invalid action. Must be either activate or deactivate' }, { status: 400 })
    }

    // Forward the request to the backend
    const response = await fetch(`${API_URL}/hr/user-accounts/${email}/account/${action}`, {
      method: 'PUT',
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json',
      },
    })

    // Check if the response is not ok
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ error: 'Failed to parse error response' }))
      return NextResponse.json(errorData, { status: response.status })
    }

    // Get the response data
    const data = await response.json()
    
    // Return the response with the same status code
    return NextResponse.json(data, { status: response.status })
  } catch (error) {
    console.error('Error in user account action:', error)
    return NextResponse.json(
      { error: 'Failed to process account action', details: error instanceof Error ? error.message : 'Unknown error' },
      { status: 500 }
    )
  }
} 