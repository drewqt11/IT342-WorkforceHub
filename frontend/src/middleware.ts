import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

export function middleware(request: NextRequest) {
    const token = request.cookies.get('token')?.value
    const userRole = request.cookies.get('role')?.value

    // Public paths that don't require authentication
    const publicPaths = ['/', '/oauth2/redirect']
    if (publicPaths.includes(request.nextUrl.pathname)) {
        // If user is already authenticated, redirect to appropriate dashboard
        if (token) {
            if (userRole?.toLowerCase() === 'hr administrator' && request.nextUrl.pathname === '/') {
                return NextResponse.redirect(new URL('/hr/admin/dashboard', request.url))
            } else if (userRole?.toLowerCase() === 'employee' && request.nextUrl.pathname === '/') {
                return NextResponse.redirect(new URL('/employee/dashboard', request.url))
            }
        }
        return NextResponse.next()
    }

    // Check if user is authenticated
    if (!token) {
        return NextResponse.redirect(new URL('/', request.url))
    }

    // Role-based access control
    if (
        request.nextUrl.pathname.startsWith('/employee') &&
        userRole?.toLowerCase() !== 'employee' &&
        userRole?.toLowerCase() !== 'hr administrator'
    ) {
        return NextResponse.redirect(new URL('/', request.url))
    }

    if (request.nextUrl.pathname.startsWith('/hr/admin') && userRole?.toLowerCase() !== 'hr administrator') {
        return NextResponse.redirect(new URL('/', request.url))
    }

    return NextResponse.next()
}

export const config = {
    matcher: [
        '/employee/:path*',
        '/hr/admin/:path*',
        '/',
        '/oauth2/redirect',
    ],
} 