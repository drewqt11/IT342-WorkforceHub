package com.example.myapplication.data.api

import com.example.myapplication.data.models.AuthResponse
import com.example.myapplication.data.models.TokenRefreshRequest
import com.example.myapplication.data.models.TokenRefreshResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Interface defining the API endpoints for the WorkforceHub backend
 */
interface WorkforceApi {
    
    /**
     * Get user token info after OAuth2 authentication
     */
    @GET("api/auth/oauth2/token-info/{email}")
    suspend fun getOAuth2TokenInfo(@Path("email") email: String): Response<AuthResponse>
    
    /**
     * Refresh an authentication token
     */
    @POST("api/auth/refresh-token")
    suspend fun refreshToken(@Body request: TokenRefreshRequest): Response<TokenRefreshResponse>
    
    /**
     * Get employee dashboard
     */
    @GET("api/auth/dashboard/employee")
    suspend fun getEmployeeDashboard(): Response<String>
} 