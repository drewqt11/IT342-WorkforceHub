package cit.edu.workforcehub.api.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * Model for employee profile data.
 */
@JsonClass(generateAdapter = true)
data class EmployeeProfile(
    @Json(name = "employeeId") val employeeId: String,
    @Json(name = "idNumber") val idNumber: String?,
    @Json(name = "firstName") val firstName: String,
    @Json(name = "lastName") val lastName: String,
    @Json(name = "email") val email: String,
    @Json(name = "gender") val gender: String? = null,
    @Json(name = "hireDate") val hireDate: String? = null,
    @Json(name = "dateOfBirth") val dateOfBirth: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "phoneNumber") val phoneNumber: String? = null,
    @Json(name = "maritalStatus") val maritalStatus: String? = null,
    @Json(name = "status") val status: Boolean = false,
    @Json(name = "employmentStatus") val employmentStatus: String? = null,
    @Json(name = "departmentId") val departmentId: String? = null,
    @Json(name = "departmentName") val departmentName: String? = null,
    @Json(name = "jobId") val jobId: String? = null,
    @Json(name = "jobName") val jobName: String? = null,
    @Json(name = "roleId") val roleId: String? = null,
    @Json(name = "roleName") val roleName: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "userId") val userId: String? = null,
    @Json(name = "lastLogin") val lastLogin: String? = null,
    @Json(name = "isActive") val isActive: Boolean? = null
)

/**
 * Simplified employee model used in lists.
 */
@JsonClass(generateAdapter = true)
data class Employee(
    @Json(name = "employeeId") val employeeId: String,
    @Json(name = "firstName") val firstName: String,
    @Json(name = "lastName") val lastName: String,
    @Json(name = "email") val email: String,
    @Json(name = "departmentName") val departmentName: String? = null,
    @Json(name = "jobTitleName") val jobTitleName: String? = null,
    @Json(name = "isActive") val isActive: Boolean = true
)

/**
 * Model for clock in/out requests.
 */
@JsonClass(generateAdapter = true)
data class ClockInRequest(
    @Json(name = "employeeId") val employeeId: String,
    @Json(name = "remarks") val remarks: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null
)

/**
 * Model for attendance records.
 */
@JsonClass(generateAdapter = true)
data class AttendanceRecord(
    @Json(name = "attendanceId") val recordId: String,
    @Json(name = "employeeId") val employeeId: String,
    @Json(name = "date") val date: String,
    @Json(name = "clockInTime") val clockInTime: String,
    @Json(name = "clockOutTime") val clockOutTime: String? = null,
    @Json(name = "totalHours") val totalHours: Double? = null,
    @Json(name = "remarks") val remarks: String? = null,
    @Json(name = "status") val status: String,
    @Json(name = "overtimeHours") val overtimeHours: Double? = null,
    @Json(name = "tardinessMinutes") val tardinessMinutes: Int? = null,
    @Json(name = "undertimeMinutes") val undertimeMinutes: Int? = null,
    @Json(name = "reasonForAbsence") val reasonForAbsence: String? = null,
    @Json(name = "approvedByManager") val approvedByManager: Boolean = false
)

/**
 * Model for leave request.
 */
@JsonClass(generateAdapter = true)
data class LeaveRequest(
    @Json(name = "leaveRequestId") val leaveRequestId: String? = null,
    @Json(name = "employeeId") val employeeId: String,
    @Json(name = "startDate") val startDate: String,
    @Json(name = "endDate") val endDate: String,
    @Json(name = "leaveType") val leaveType: String,
    @Json(name = "reason") val reason: String,
    @Json(name = "status") val status: String? = "PENDING",
    @Json(name = "approvedByManagerId") val approvedByManagerId: String? = null,
    @Json(name = "approvedDate") val approvedDate: String? = null,
    @Json(name = "submissionDate") val submissionDate: String? = null,
    @Json(name = "remarks") val remarks: String? = null,
    @Json(name = "attachmentUrl") val attachmentUrl: String? = null
)

/**
 * Model for overtime request.
 */
@JsonClass(generateAdapter = true)
data class OvertimeRequest(
    @Json(name = "otRequestId") val overtimeRequestId: String? = null,
    @Json(name = "employeeId") val employeeId: String? = null,
    @Json(name = "employeeName") val employeeName: String? = null,
    @Json(name = "date") val date: String,
    @Json(name = "startTime") val startTime: String,
    @Json(name = "endTime") val endTime: String,
    @Json(name = "totalHours") val totalHours: Double,
    @Json(name = "reason") val reason: String,
    @Json(name = "status") val status: String? = "PENDING",
    @Json(name = "reviewedBy") val reviewedBy: String? = null,
    @Json(name = "reviewedAt") val reviewedAt: String? = null
)

/**
 * Model for reimbursement request.
 */
@JsonClass(generateAdapter = true)
data class ReimbursementRequest(
    @Json(name = "reimbursementRequestId") val reimbursementRequestId: String? = null,
    @Json(name = "employeeId") val employeeId: String,
    @Json(name = "expenseDate") val expenseDate: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "category") val category: String,
    @Json(name = "description") val description: String,
    @Json(name = "status") val status: String? = "PENDING",
    @Json(name = "approvedByManagerId") val approvedByManagerId: String? = null,
    @Json(name = "approvedDate") val approvedDate: String? = null,
    @Json(name = "submissionDate") val submissionDate: String? = null,
    @Json(name = "receiptUrl") val receiptUrl: String? = null
) 