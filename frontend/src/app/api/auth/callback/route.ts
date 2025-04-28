import { NextRequest, NextResponse } from 'next/server';

export async function GET(request: NextRequest) {
    const searchParams = request.nextUrl.searchParams;

    // Get all parameters from the URL
    const token = searchParams.get('token');
    const userId = searchParams.get('userId');
    const email = searchParams.get('email');
    const role = searchParams.get('role');
    const isActive = searchParams.get('isActive');
    const employeeId = searchParams.get('employeeId');
    const firstName = searchParams.get('firstName');
    const lastName = searchParams.get('lastName');

    if (!token || !email) {
        return NextResponse.redirect(new URL('/login?error=missing_params', request.url));
    }

    // Determine the redirect URL based on role
    let redirectUrl = '/employee/dashboard';
    if (role?.toLowerCase() === 'hr administrator') {
        redirectUrl = '/hr/admin/dashboard';
    }

    // Create a cookie with the token
    const response = NextResponse.redirect(
        new URL(redirectUrl, request.url)
    );

    // Set the token in a secure HTTP-only cookie
    response.cookies.set({
        name: 'token',
        value: token,
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'strict',
        path: '/',
    });

    // Set user info in cookies if needed
    if (userId) {
        response.cookies.set({
            name: 'userId',
            value: userId,
            httpOnly: true,
            secure: process.env.NODE_ENV === 'production',
            sameSite: 'strict',
            path: '/',
        });
    }

    if (email) {
        response.cookies.set({
            name: 'email',
            value: email,
            httpOnly: true,
            secure: process.env.NODE_ENV === 'production',
            sameSite: 'strict',
            path: '/',
        });
    }

    if (role) {
        response.cookies.set({
            name: 'role',
            value: role,
            httpOnly: true,
            secure: process.env.NODE_ENV === 'production',
            sameSite: 'strict',
            path: '/',
        });
    }

    if (isActive) {
        response.cookies.set({
            name: 'isActive',
            value: isActive,
            path: '/',
        });
    }

    return response;
} 