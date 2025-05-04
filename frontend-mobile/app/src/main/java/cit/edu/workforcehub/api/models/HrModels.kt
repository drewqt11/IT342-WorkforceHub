package cit.edu.workforcehub.api.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Model for department data.
 */
@JsonClass(generateAdapter = true)
data class Department(
    @Json(name = "departmentId") val departmentId: String,
    @Json(name = "departmentName") val departmentName: String,
    @Json(name = "description") val description: String? = null
)

/**
 * Model for job title data.
 */
@JsonClass(generateAdapter = true)
data class JobTitle(
    @Json(name = "jobTitleId") val jobTitleId: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "departmentId") val departmentId: String? = null,
    @Json(name = "departmentName") val departmentName: String? = null,
    @Json(name = "minimumSalary") val minimumSalary: Double? = null,
    @Json(name = "maximumSalary") val maximumSalary: Double? = null
)

/**
 * Model for user account data.
 */
@JsonClass(generateAdapter = true)
data class UserAccount(
    @Json(name = "userId") val userId: String,
    @Json(name = "emailAddress") val emailAddress: String,
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "lastLogin") val lastLogin: String? = null,
    @Json(name = "active") val active: Boolean = false
) 