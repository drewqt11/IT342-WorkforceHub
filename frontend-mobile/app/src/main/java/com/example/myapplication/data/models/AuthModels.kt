package com.example.myapplication.data.models

import com.google.gson.annotations.SerializedName

/**
 * Data class representing the authentication response from the backend
 */
data class AuthResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("tokenType") val tokenType: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("username") val username: String,
    @SerializedName("roles") val roles: List<String>,
    @SerializedName("expiresIn") val expiresIn: Long
)

/**
 * Data class representing a request to refresh an authentication token
 */
data class TokenRefreshRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

/**
 * Data class representing the response to a token refresh request
 */
data class TokenRefreshResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("tokenType") val tokenType: String,
    @SerializedName("expiresIn") val expiresIn: Long
)

/**
 * Data class representing the user profile
 */
data class UserProfile(
    @SerializedName("userId") val userId: String,
    @SerializedName("email") val email: String,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("employeeId") val employeeId: String? = null,
    @SerializedName("isActive") val isActive: Boolean = false
) 