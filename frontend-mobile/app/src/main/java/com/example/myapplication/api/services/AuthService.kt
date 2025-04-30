package com.example.myapplication.api.services

import com.example.myapplication.api.ApiEndpoints
import com.example.myapplication.api.models.AuthRequest
import com.example.myapplication.api.models.AuthResponse
import com.example.myapplication.api.models.RefreshTokenRequest
import com.example.myapplication.api.models.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface for authentication-related API endpoints.
 */
interface AuthService {
    /**
     * Log in a user with email and password.
     */
    @POST(ApiEndpoints.LOGIN)
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>
    
    /**
     * Register a new user.
     */
    @POST(ApiEndpoints.REGISTER)
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    /**
     * Refresh an access token using a refresh token.
     */
    @POST(ApiEndpoints.REFRESH_TOKEN)
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>
    
    /**
     * Log out a user and invalidate their tokens.
     */
    @POST(ApiEndpoints.LOGOUT)
    suspend fun logout(@Query("userId") userId: String): Response<Unit>
    
    /**
     * Get token information after OAuth2 authentication.
     * Used for Microsoft OAuth flow.
     */
    @GET("auth/oauth2/token-info/{email}")
    suspend fun getOAuth2TokenInfo(@Path("email") email: String): Response<AuthResponse>
} 