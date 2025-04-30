package com.example.myapplication.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.myapplication.api.models.AuthRequest
import com.example.myapplication.api.models.AuthResponse
import com.example.myapplication.api.models.RefreshTokenRequest
import com.example.myapplication.api.services.AuthService
import com.example.myapplication.api.services.EmployeeService
import com.example.myapplication.api.services.HrService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper class that provides easy access to API services and handles common API operations.
 */
object ApiHelper {
    private const val TAG = "ApiHelper"
    private const val PREF_NAME = "workforce_prefs"
    private const val KEY_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"

    private var authToken: String? = null
    private var refreshToken: String? = null
    private var userId: String? = null

    private lateinit var prefs: SharedPreferences

    /**
     * Initialize the ApiHelper with context for shared preferences access.
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        authToken = prefs.getString(KEY_TOKEN, null)
        refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
        userId = prefs.getString(KEY_USER_ID, null)
    }

    /**
     * Get the auth service.
     */
    fun getAuthService(): AuthService {
        return ApiClient.createService(AuthService::class.java)
    }

    /**
     * Get the employee service with authorization.
     */
    fun getEmployeeService(): EmployeeService {
        return ApiClient.createService(EmployeeService::class.java, authToken)
    }

    /**
     * Get the HR service with authorization.
     */
    fun getHrService(): HrService {
        return ApiClient.createService(HrService::class.java, authToken)
    }

    /**
     * Example of how to perform login.
     */
    suspend fun login(email: String, password: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = getAuthService().login(AuthRequest(email, password))
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                saveAuthData(authResponse)
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Login failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            Result.failure(e)
        }
    }

    /**
     * Attempt to refresh the token.
     */
    suspend fun refreshToken(): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val currentRefreshToken = refreshToken ?: return@withContext Result.failure(Exception("No refresh token available"))
            
            val response = getAuthService().refreshToken(RefreshTokenRequest(currentRefreshToken))
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                saveAuthData(authResponse)
                Result.success(authResponse)
            } else {
                // Clear tokens if refresh fails
                clearAuthData()
                Result.failure(Exception("Token refresh failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Token refresh error", e)
            clearAuthData()
            Result.failure(e)
        }
    }

    /**
     * Logout the current user.
     */
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = userId ?: return@withContext Result.failure(Exception("No user ID available"))
            
            val response = getAuthService().logout(currentUserId)
            
            // Clear auth data regardless of response
            clearAuthData()
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Logout failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Logout error", e)
            clearAuthData()
            Result.failure(e)
        }
    }

    /**
     * Save authentication data to shared preferences and memory.
     * This is now public to be used by the AuthManager.
     */
    fun saveAuthData(authResponse: AuthResponse) {
        authToken = authResponse.token
        refreshToken = authResponse.refreshToken
        userId = authResponse.userId
        
        prefs.edit().apply {
            putString(KEY_TOKEN, authToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putString(KEY_USER_ID, userId)
            apply()
        }
    }

    /**
     * Clear authentication data from shared preferences and memory.
     * This is now public to be used by the AuthManager.
     */
    fun clearAuthData() {
        authToken = null
        refreshToken = null
        userId = null
        
        prefs.edit().apply {
            remove(KEY_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_ID)
            apply()
        }
    }

    /**
     * Check if the user is authenticated.
     */
    fun isAuthenticated(): Boolean {
        return !authToken.isNullOrEmpty()
    }
} 