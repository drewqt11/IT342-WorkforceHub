import { NextRequest, NextResponse } from 'next/server';

export async function POST(request: NextRequest) {
  try {
    const authHeader = request.headers.get("authorization")

   
    if (!authHeader) {
      return NextResponse.json(
        { error: "Authorization header is required" },
        { status: 401 }
      )
    }

    const body = await request.json();
    const { employeeId, remarks } = body;

    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/employee/attendance/clock-in`, {
      method: 'POST',
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ employeeId, remarks })
    });

    if (!response.ok) {
      const error = await response.json();
      return NextResponse.json({ error: error.message || 'Failed to clock in' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Error in clock-in route:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
} 