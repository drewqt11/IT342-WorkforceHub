import { NextRequest, NextResponse } from 'next/server';

export async function GET(request: NextRequest) {
    const searchParams = request.nextUrl.searchParams;

    // Get all parameters from the URL
    const token = searchParams.get('token');
    const userId = searchParams.get('userId');
    const email = searchParams.get('email');
    const role = searchParams.get('role');
    const employeeId = searchParams.get('employeeId');
    const firstName = searchParams.get('firstName');
    const lastName = searchParams.get('lastName');

    if (!token || !email) {
        // Create a response that redirects to the home page
        const response = NextResponse.redirect(new URL('/', request.url));

        // Clear all cookies
        const cookies = request.cookies.getAll();
        cookies.forEach(cookie => {
            response.cookies.delete(cookie.name);
        });

        return response;
    }

    // Determine the redirect URL based on role
    let redirectUrl = '/employee/dashboard';
    if (role?.toLowerCase() === 'hr administrator') {
        redirectUrl = '/hr/admin/dashboard';
    }

    // Create a response with HTML that sets localStorage and then redirects
    const html = `
        <!DOCTYPE html>
        <html>
        <head>
            <title>Redirecting...</title>
            <script>
                // Only store token in localStorage
                localStorage.setItem('token', '${token}');
                
                // Redirect after setting localStorage
                window.location.href = '${redirectUrl}';
            </script>
        </head>
        <body>
            <p>Redirecting...</p>
        </body>
        </html>
    `;

    // Return HTML response
    return new NextResponse(html, {
        headers: {
            'Content-Type': 'text/html',
        },
    });
} 