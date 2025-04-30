package com.example.myapplication.api.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * Request model for login.
 */
@JsonClass(generateAdapter = true)
data class AuthRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

/**
 * Request model for user registration.
 */
@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "firstName") val firstName: String,
    @Json(name = "lastName") val lastName: String,
    @Json(name = "phoneNumber") val phoneNumber: String? = null,
    @Json(name = "dateOfBirth") val dateOfBirth: String? = null
)

/**
 * Request model for refreshing an access token.
 */
@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    @Json(name = "refreshToken") val refreshToken: String
)

/**
 * Response model for authentication operations.
 */
@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "token") val token: String,
    @Json(name = "refreshToken") val refreshToken: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "email") val email: String,
    @Json(name = "role") val role: String,
    @Json(name = "employeeId") val employeeId: String,
    @Json(name = "firstName") val firstName: String,
    @Json(name = "lastName") val lastName: String,
    @Json(name = "createdAt") val createdAt: Date
) 