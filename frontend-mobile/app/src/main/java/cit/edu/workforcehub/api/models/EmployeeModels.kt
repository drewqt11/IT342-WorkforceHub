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