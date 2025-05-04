package cit.edu.workforcehub.api.services

import cit.edu.workforcehub.api.ApiEndpoints
import cit.edu.workforcehub.api.models.AttendanceRecord
import cit.edu.workforcehub.api.models.ClockInRequest
import cit.edu.workforcehub.api.models.EmployeeProfile
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit service interface for employee-related API endpoints.
 */
interface EmployeeService {
    /**
     * Get the profile of the current authenticated employee.
     */
    @GET(ApiEndpoints.EMPLOYEE_PROFILE)
    suspend fun getProfile(): Response<EmployeeProfile>
    
    /**
     * Update the profile of the current authenticated employee.
     */
    @PUT(ApiEndpoints.EMPLOYEE_PROFILE)
    @Headers("Content-Type: application/json")
    suspend fun updateProfile(@Body profile: EmployeeProfile): Response<EmployeeProfile>
    
    /**
     * Partially update the profile of the current authenticated employee.
     */
    @PATCH(ApiEndpoints.EMPLOYEE_PROFILE)
    @Headers("Content-Type: application/json")
    suspend fun patchProfile(@Body profile: EmployeeProfile): Response<EmployeeProfile>
    
    /**
     * Clock in for the current day.
     */
    @POST("${ApiEndpoints.EMPLOYEE_ATTENDANCE}/clock-in")
    suspend fun clockIn(@Body request: ClockInRequest): Response<AttendanceRecord>
    
    /**
     * Clock out for the current day.
     */
    @POST("${ApiEndpoints.EMPLOYEE_ATTENDANCE}/clock-out")
    suspend fun clockOut(@Body request: ClockInRequest): Response<AttendanceRecord>
    
    /**
     * Get today's attendance record for the current employee.
     */
    @GET("${ApiEndpoints.EMPLOYEE_ATTENDANCE}/today")
    suspend fun getTodayAttendance(): Response<AttendanceRecord>
    
    /**
     * Get attendance records for the authenticated employee.
     */
    @GET("${ApiEndpoints.EMPLOYEE_ATTENDANCE}/records")
    suspend fun getAttendanceRecords(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<List<AttendanceRecord>>
} 