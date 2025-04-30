package com.example.myapplication.api.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * Model for employee profile data.
 */
@JsonClass(generateAdapter = true)
data class EmployeeProfile(
    @Json(name = "employeeId") val employeeId: String,
    @Json(name = "firstName") val firstName: String,
    @Json(name = "lastName") val lastName: String,
    @Json(name = "email") val email: String,
    @Json(name = "phoneNumber") val phoneNumber: String? = null,
    @Json(name = "dateOfBirth") val dateOfBirth: Date? = null,
    @Json(name = "hireDate") val hireDate: Date? = null,
    @Json(name = "departmentId") val departmentId: String? = null,
    @Json(name = "departmentName") val departmentName: String? = null,
    @Json(name = "jobTitleId") val jobTitleId: String? = null,
    @Json(name = "jobTitleName") val jobTitleName: String? = null,
    @Json(name = "role") val role: String? = null,
    @Json(name = "isActive") val isActive: Boolean = true
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
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null
)

/**
 * Model for attendance records.
 */
@JsonClass(generateAdapter = true)
data class AttendanceRecord(
    @Json(name = "recordId") val recordId: String,
    @Json(name = "employeeId") val employeeId: String,
    @Json(name = "date") val date: Date,
    @Json(name = "clockInTime") val clockInTime: Date,
    @Json(name = "clockOutTime") val clockOutTime: Date? = null,
    @Json(name = "totalHours") val totalHours: Double? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "status") val status: String
) 