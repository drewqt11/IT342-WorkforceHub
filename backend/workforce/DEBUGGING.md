# Debugging the 403 Forbidden Error

If you're encountering a 403 Forbidden error when trying to create an employee, follow these specific steps to troubleshoot and fix the issue:

## Key Changes Made

1. **Added User with ID 5**: We've added a test user with ID 5 in the `data.sql` file, so you can use `userId: 5` in your employee creation request.

2. **Modified EmployeeController**: The `createEmployee` method now expects the `userId` in the request body rather than as a query parameter.

3. **Added userId Field to EmployeeDTO**: We've added a `userId` field to the `EmployeeDTO` class to support this change.

## Steps to Test Employee Creation

1. **Login First to Get a JWT Token**:
   - **URL**: `http://localhost:8080/api/auth/login`
   - **Method**: POST
   - **Headers**: `Content-Type: application/json`
   - **Body**:
     ```json
     {
       "username": "admin",
       "password": "password123"
     }
     ```
   - Copy the JWT token from the response (it's under `data.token`).

2. **Create an Employee with the JWT Token**:
   - **URL**: `http://localhost:8080/api/employees`
   - **Method**: POST
   - **Headers**: 
     - `Content-Type: application/json`
     - `Authorization: Bearer YOUR_JWT_TOKEN`
   - **Body**:
     ```json
     {
       "userId": 5,
       "firstName": "Jane",
       "lastName": "Smith",
       "email": "jane.smith@example.com",
       "phoneNumber": "555-123-4567",
       "dateOfBirth": "1992-05-15",
       "hireDate": "2023-03-01",
       "department": "Marketing",
       "position": "Marketing Specialist"
     }
     ```

## Common Issues and Solutions

1. **Wrong JWT Token Format**:
   - Make sure your Authorization header is exactly: `Bearer YOUR_JWT_TOKEN`
   - There should be a space between "Bearer" and the token
   - The token should not have any extra spaces or quotes

2. **Expired Token**:
   - Tokens expire after 24 hours (by default)
   - If you get a 401 error, try logging in again to get a fresh token

3. **User Role Issues**:
   - Only users with the `ADMIN` or `HR_STAFF` role can create employees
   - Make sure you're logged in as "admin" or "testuser", which have these roles

4. **User ID Doesn't Exist**:
   - If the user ID in your request doesn't exist in the database, you'll get an error
   - We've added user ID 5, so `"userId": 5` should work

5. **Database Connection Issues**:
   - Make sure PostgreSQL is running
   - Check the database connection details in `application.properties`

## Testing with curl

If Postman isn't working, try using curl:

```bash
# Step 1: Login to get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'

# Step 2: Create employee using token (replace YOUR_TOKEN with actual token)
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"userId":5,"firstName":"Jane","lastName":"Smith","email":"jane.smith@example.com","phoneNumber":"555-123-4567","dateOfBirth":"1992-05-15","hireDate":"2023-03-01","department":"Marketing","position":"Marketing Specialist"}'
``` 