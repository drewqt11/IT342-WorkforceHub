# WorkforceHub Frontend

This is the frontend for the WorkforceHub application, a Human Resource Management System built with React, TypeScript, and Tailwind CSS.

## Features

### Module 1: Authentication and User Management

- User login with email/password and Microsoft OAuth
- Self-enrollment signup process
- Profile management
- Responsive design for all devices

### Module 2: Employee Data Management

- Employee records management (view, create, update, delete)
- Role, job title, and department assignment
- Certification and training document tracking
- HR Admin and Employee views

## Technology Stack

- React 19 with TypeScript
- React Router for routing
- Tailwind CSS for styling
- React Hook Form with Zod for form validation
- Axios for API communication

## Project Structure

```
src/
├── assets/          # Static assets like images, icons
├── components/      # Reusable UI components
│   ├── layouts/     # Layout components like DashboardLayout
│   └── ui/          # UI components like Button, Input
├── context/         # React context providers
├── hooks/           # Custom React hooks
├── pages/           # Page components
│   └── employee/    # Employee-related pages
├── services/        # API services
└── utils/           # Utility functions
```

## Getting Started

### Prerequisites

- Node.js 18+ and npm

### Installation

1. Clone the repository
2. Navigate to the frontend directory:
   ```
   cd frontend-web/frontend-website
   ```
3. Install dependencies:
   ```
   npm install
   ```
4. Start the development server:
   ```
   npm run dev
   ```
5. Open your browser and visit http://localhost:5173

## API Connection

The frontend connects to the Spring Boot backend running on http://localhost:8080. Make sure the backend server is running before using the application.

## Authentication Flow

1. Users can login with email/password or Microsoft OAuth
2. Upon successful authentication, a JWT token is stored securely
3. All API requests include the JWT token for authorization
4. Token refresh is handled automatically when needed

## Features by User Role

### Employee
- View and update personal profile
- Upload and view certifications and training documents

### HR Admin
- Manage all employee records
- Assign roles, job titles, and departments
- Review and approve certifications and documents
- Manage departments and job titles

## Development

### Adding New Components

1. Create component in the appropriate directory
2. Use existing UI components for consistency
3. Follow the established patterns for state management

### Building for Production

Run:
```
npm run build
```

The build artifacts will be stored in the `dist/` directory.
