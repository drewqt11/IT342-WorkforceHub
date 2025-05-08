package cit.edu.workforcehub.api.services

import cit.edu.workforcehub.api.ApiEndpoints
import cit.edu.workforcehub.api.models.AttendanceRecord
import cit.edu.workforcehub.api.models.ClockInRequest
import cit.edu.workforcehub.api.models.EmployeeProfile
import cit.edu.workforcehub.api.models.LeaveRequest
import cit.edu.workforcehub.api.models.OvertimeRequest
import cit.edu.workforcehub.api.models.ReimbursementRequest
import cit.edu.workforcehub.presentation.screens.forms.LeaveRequestData
import cit.edu.workforcehub.presentation.screens.forms.OvertimeRequestData
import retrofit2.Response
import retrofit2.http.*
import java.net.URLEncoder

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
    
    /**
     * Get all attendance records for the authenticated employee.
     */
    @GET(ApiEndpoints.EMPLOYEE_ATTENDANCE)
    suspend fun getAllAttendanceRecords(): Response<List<AttendanceRecord>>
    
    /**
     * Submit a leave request.
     */
    @POST("/api/employee/leave-requests")
    @Headers("Content-Type: application/json")
    suspend fun submitLeaveRequest(@Body request: LeaveRequest): Response<LeaveRequest>
    
    /**
     * Create a leave request with simplified data
     */
    @POST("/api/employee/leave-requests")
    @Headers("Content-Type: application/json")
    suspend fun createLeaveRequest(@Body request: LeaveRequestData): Response<LeaveRequest>
    
    /**
     * Get all leave requests for the authenticated employee.
     */
    @GET("/api/employee/leave-requests")
    suspend fun getLeaveRequests(): Response<List<LeaveRequest>>

    /**
     * Submit an overtime request.
     */
    @POST("/api/employee/overtime-requests")
    @Headers("Content-Type: application/json")
    suspend fun submitOvertimeRequest(@Body request: OvertimeRequest): Response<OvertimeRequest>
    
    /**
     * Create an overtime request with simplified data.
     */
    @POST("/api/overtime/request")
    @Headers("Content-Type: application/json")
    suspend fun createOvertimeRequest(@Body request: OvertimeRequestData): Response<OvertimeRequest>
    
    /**
     * Get all overtime requests for the authenticated employee.
     */
    @GET("/api/overtime/my-requests")
    suspend fun getOvertimeRequests(): Response<List<OvertimeRequest>>

    /**
     * Submit a reimbursement request.
     */
    @POST("/api/employee/reimbursement-requests")
    @Headers("Content-Type: application/json")
    suspend fun submitReimbursementRequest(@Body request: ReimbursementRequest): Response<ReimbursementRequest>
    
    /**
     * Get all reimbursement requests for the authenticated employee.
     */
    @GET("/api/employee/reimbursement-requests")
    suspend fun getReimbursementRequests(): Response<List<ReimbursementRequest>>
    
    /**
     * Cancel an overtime request.
     */
    @PATCH("/api/overtime/request/{otRequestId}/cancel")
    suspend fun cancelOvertimeRequest(@Path("otRequestId") requestId: String): Response<Void>
    
    /**
     * Cancel a leave request.
     * The ID parameter is the unique identifier for the leave request.
     */
    @PATCH("/api/employee/leave-requests/{id}/cancel")
    suspend fun cancelLeaveRequest(@Path("id") id: String): Response<Void>
} 