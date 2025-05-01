package com.example.myapplication.api.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Model class for user information returned by token validation
 */
@JsonClass(generateAdapter = true)
data class UserInfo(
    @Json(name = "userId") val userId: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "role") val role: String? = null,
    @Json(name = "employeeId") val employeeId: String? = null,
    @Json(name = "firstName") val firstName: String? = null,
    @Json(name = "lastName") val lastName: String? = null,
    @Json(name = "isActive") val isActive: Boolean = true
) 