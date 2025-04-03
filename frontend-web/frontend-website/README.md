# WorkforceHub Frontend

This is a React-based frontend for the WorkforceHub application, designed to interact with the Spring Boot backend.

## Features

- User authentication (login/register)
- Employee dashboard
- Department management
- Job title management
- User profile management

## Prerequisites

- Node.js (v16+)
- npm or yarn
- WorkforceHub backend running on http://localhost:8080

## Getting Started

### Installation

1. Clone the repository or navigate to the frontend-website directory
2. Install dependencies:

```bash
npm install
```

or if you use yarn:

```bash
yarn
```

### Running the Development Server

Start the development server:

```bash
npm run dev
```

or with yarn:

```bash
yarn dev
```

The application will be available at http://localhost:5173 (or a different port if 5173 is in use).

## API Integration

This frontend is configured to work with the WorkforceHub backend API at `http://localhost:8080/api`. If your backend is running on a different URL, you'll need to update the `API_URL` constant in `src/services/api.ts`.

## Testing the Application

1. Start your Spring Boot backend server
2. Start the frontend development server
3. Open your browser and navigate to http://localhost:5173
4. You can register a new account or login with existing credentials

### Test Users (if available in your backend)

- Regular Employee:
  - Email: employee@example.com
  - Password: password

- HR Admin:
  - Email: hr.admin@example.com
  - Password: password

## Folder Structure

- `/src/components` - Reusable UI components
- `/src/pages` - Page components corresponding to routes
- `/src/services` - API service functions
- `/src/context` - React context for state management
- `/src/types` - TypeScript type definitions

## Development

### Adding New Features

1. Define any new types in `/src/types/index.ts`
2. Add API service functions in `/src/services/api.ts`
3. Create new page components in `/src/pages/`
4. Add routes to `/src/App.tsx`

### Building for Production

```bash
npm run build
```

The build artifacts will be located in the `dist/` directory.

## Note for Developers

This is a basic frontend implementation for testing purposes. For a production application, you would want to consider:

- More robust error handling
- Better state management (e.g., Redux)
- Unit and integration tests
- More complete form validation
- Accessibility improvements
- Performance optimizations
