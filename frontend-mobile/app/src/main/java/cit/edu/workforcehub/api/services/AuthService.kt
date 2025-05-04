package cit.edu.workforcehub.api.services

import cit.edu.workforcehub.api.ApiEndpoints
import cit.edu.workforcehub.api.models.AuthRequest
import cit.edu.workforcehub.api.models.AuthResponse
import cit.edu.workforcehub.api.models.RefreshTokenRequest
import cit.edu.workforcehub.api.models.RegisterRequest
import cit.edu.workforcehub.api.models.UserInfo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
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
    
    /**
     * Validate a token and get user information
     */
    @GET("api/auth/oauth2/user-info")
    suspend fun validateToken(@Header("Authorization") authHeader: String): Response<cit.edu.workforcehub.api.models.UserInfo>
} 