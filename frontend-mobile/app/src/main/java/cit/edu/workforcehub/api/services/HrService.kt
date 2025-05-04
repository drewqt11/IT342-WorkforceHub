package cit.edu.workforcehub.api.services

import cit.edu.workforcehub.api.ApiEndpoints
import cit.edu.workforcehub.api.models.Department
import cit.edu.workforcehub.api.models.Employee
import cit.edu.workforcehub.api.models.JobTitle
import cit.edu.workforcehub.api.models.UserAccount
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit service interface for HR-related API endpoints.
 */
interface HrService {
    /**
     * Get all departments.
     */
    @GET(ApiEndpoints.DEPARTMENTS)
    suspend fun getDepartments(): Response<List<Department>>
    
    /**
     * Get a department by ID.
     */
    @GET("${ApiEndpoints.DEPARTMENTS}/{id}")
    suspend fun getDepartment(@Path("id") id: String): Response<Department>
    
    /**
     * Create a new department.
     */
    @POST(ApiEndpoints.DEPARTMENTS)
    suspend fun createDepartment(
        @Query("departmentName") departmentName: String,
        @Query("description") description: String?
    ): Response<Department>
    
    /**
     * Update a department.
     */
    @PUT("${ApiEndpoints.DEPARTMENTS}/{id}")
    suspend fun updateDepartment(
        @Path("id") id: String,
        @Query("departmentName") departmentName: String,
        @Query("description") description: String?
    ): Response<Department>
    
    /**
     * Delete a department.
     */
    @DELETE("${ApiEndpoints.DEPARTMENTS}/{id}")
    suspend fun deleteDepartment(@Path("id") id: String): Response<Unit>
    
    /**
     * Get all job titles.
     */
    @GET(ApiEndpoints.JOB_TITLES)
    suspend fun getJobTitles(): Response<List<JobTitle>>
    
    /**
     * Get a job title by ID.
     */
    @GET("${ApiEndpoints.JOB_TITLES}/{id}")
    suspend fun getJobTitle(@Path("id") id: String): Response<JobTitle>
    
    /**
     * Get all employees.
     */
    @GET(ApiEndpoints.EMPLOYEES)
    suspend fun getEmployees(): Response<List<Employee>>
    
    /**
     * Get an employee by ID.
     */
    @GET("${ApiEndpoints.EMPLOYEES}/{id}")
    suspend fun getEmployee(@Path("id") id: String): Response<Employee>
    
    /**
     * Activate an employee account.
     */
    @POST("${ApiEndpoints.EMPLOYEES}/{id}/activate")
    suspend fun activateEmployee(@Path("id") id: String): Response<Employee>
    
    /**
     * Deactivate an employee account.
     */
    @POST("${ApiEndpoints.EMPLOYEES}/{id}/deactivate")
    suspend fun deactivateEmployee(@Path("id") id: String): Response<Employee>
    
    /**
     * Get a user account by email.
     */
    @GET("/api/hr/user-accounts/{email}")
    suspend fun getUserAccountByEmail(@Path("email") email: String): Response<UserAccount>
} 