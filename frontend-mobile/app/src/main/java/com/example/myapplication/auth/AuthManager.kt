package com.example.myapplication.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.example.myapplication.api.ApiEndpoints
import com.example.myapplication.api.ApiHelper
import com.example.myapplication.api.models.AuthResponse
import com.example.myapplication.utils.WebViewUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Manages authentication with the backend, including OAuth flows and token handling.
 */
class AuthManager(private val context: Context) {
    companion object {
        private const val TAG = "AuthManager"
        
        // OAuth constants
        private const val OAUTH_PATH = "/login/oauth2/authorization/microsoft"
        private const val OAUTH_CALLBACK_PATH = "/oauth2/redirect"
        
        // App scheme for redirect URI
        private const val APP_SCHEME = "workforcehub://oauth2redirect"
        
        // Key for checking if we're in the middle of authentication
        private const val OAUTH_STATE_PREFS = "oauth_state_prefs"
        private const val KEY_AUTH_IN_PROGRESS = "auth_in_progress"
        private const val KEY_AUTH_EMAIL = "auth_email"
        
        // Initialize singleton instance
        @Volatile
        private var instance: AuthManager? = null
        
        fun getInstance(context: Context): AuthManager {
            return instance ?: synchronized(this) {
                instance ?: AuthManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Start the Microsoft OAuth authentication flow
     */
    fun startMicrosoftOAuth() {
        try {
            // Mark that auth is in progress
            setAuthInProgress(true)
            
            // Build the OAuth URL
            val authUrl = ApiEndpoints.BASE_URL + OAUTH_PATH
            
            // Open the URL in Custom Tabs
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            
            // Create an intent to launch the URL
            val intent = customTabsIntent.intent
            intent.data = Uri.parse(authUrl)
            
            // Add FLAG_ACTIVITY_NEW_TASK when launching from a non-Activity context
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            // Launch the custom tab
            context.startActivity(intent)
            
            Log.d(TAG, "Launched Microsoft OAuth flow at URL: $authUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting OAuth flow", e)
            setAuthInProgress(false)
        }
    }
    
    /**
     * Alternative method to start OAuth flow with an Activity context
     * Use this method when you have an Activity context available
     */
    fun startMicrosoftOAuth(activity: Activity) {
        try {
            // Mark that auth is in progress
            setAuthInProgress(true)
            
            // Build the OAuth URL
            val authUrl = ApiEndpoints.BASE_URL + OAUTH_PATH
            
            // Open the URL in Custom Tabs
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            
            // Launch with activity context
            customTabsIntent.launchUrl(activity, Uri.parse(authUrl))
            
            Log.d(TAG, "Launched Microsoft OAuth flow at URL: $authUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting OAuth flow", e)
            setAuthInProgress(false)
        }
    }
    
    /**
     * Handle the OAuth redirect when returning from the browser
     * 
     * @param uri The redirect URI that was captured
     * @return True if this was an OAuth redirect that was handled
     */
    suspend fun handleOAuthRedirect(uri: Uri): Boolean {
        // Check if we're in the middle of authentication
        if (!isAuthInProgress()) {
            return false
        }
        
        Log.d(TAG, "Handling OAuth redirect: $uri")
        
        // Check if this is our app scheme redirect
        if (uri.scheme == "workforcehub" && uri.host == "oauth2redirect") {
            // Extract email from query parameters
            val email = uri.getQueryParameter("email")
            
            if (email != null) {
                // Save email for token exchange
                saveAuthEmail(email)
                
                // Exchange email for tokens
                val result = exchangeOAuthTokenInfo(email)
                
                // Reset the auth in progress flag
                setAuthInProgress(false)
                
                if (result.isSuccess) {
                    Log.d(TAG, "OAuth authentication successful for user: $email")
                    return true
                } else {
                    Log.e(TAG, "Failed to exchange OAuth token: ${result.exceptionOrNull()?.message}")
                }
            } else {
                Log.e(TAG, "OAuth redirect missing email parameter")
                setAuthInProgress(false)
            }
            return false
        }
        
        // If it's a backend redirect with token directly
        // This is used when testing with browser redirects from backend
        if (uri.toString().contains(OAUTH_CALLBACK_PATH)) {
            try {
                // Extract token and other parameters from the URI
                val token = uri.getQueryParameter("token")
                val email = uri.getQueryParameter("email")
                val userId = uri.getQueryParameter("userId") 
                val role = uri.getQueryParameter("role")
                val employeeId = uri.getQueryParameter("employeeId")
                val firstName = uri.getQueryParameter("firstName")
                val lastName = uri.getQueryParameter("lastName")
                
                if (token == null || email == null) {
                    Log.e(TAG, "OAuth redirect missing token or email parameters")
                    setAuthInProgress(false)
                    return false
                }
                
                // Create an auth response object from the URI parameters
                val authResponse = AuthResponse(
                    token = token,
                    refreshToken = "", // OAuth flow doesn't return refresh token directly
                    userId = userId ?: "",
                    email = email,
                    role = role ?: "",
                    employeeId = employeeId ?: "",
                    firstName = firstName ?: "",
                    lastName = lastName ?: "",
                    createdAt = Date()
                )
                
                // Store the authentication data securely
                ApiHelper.saveAuthData(authResponse)
                
                // Reset the auth in progress flag
                setAuthInProgress(false)
                
                // If we made it this far, authentication was successful
                Log.d(TAG, "OAuth authentication successful for user: $email")
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Error handling OAuth redirect", e)
                setAuthInProgress(false)
                return false
            }
        }
        
        return false
    }
    
    /**
     * Exchange OAuth token info using the email
     */
    suspend fun exchangeOAuthTokenInfo(email: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Exchanging OAuth token info for email: $email")
            val response = ApiHelper.getAuthService().getOAuth2TokenInfo(email)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                ApiHelper.saveAuthData(authResponse)
                return@withContext Result.success(authResponse)
            } else {
                return@withContext Result.failure(Exception("Failed to get token info: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error exchanging OAuth token info", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Check if we're in the middle of authentication
     */
    private fun isAuthInProgress(): Boolean {
        val prefs = context.getSharedPreferences(OAUTH_STATE_PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTH_IN_PROGRESS, false)
    }
    
    /**
     * Set whether authentication is in progress
     */
    private fun setAuthInProgress(inProgress: Boolean) {
        val prefs = context.getSharedPreferences(OAUTH_STATE_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTH_IN_PROGRESS, inProgress).apply()
    }
    
    /**
     * Save the email from OAuth for token exchange
     */
    private fun saveAuthEmail(email: String) {
        val prefs = context.getSharedPreferences(OAUTH_STATE_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_AUTH_EMAIL, email).apply()
    }
    
    /**
     * Get the saved auth email
     */
    private fun getAuthEmail(): String? {
        val prefs = context.getSharedPreferences(OAUTH_STATE_PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_AUTH_EMAIL, null)
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return ApiHelper.isAuthenticated()
    }
    
    /**
     * Logout the current user
     * Clears tokens, cookies, and all WebView data
     */
    suspend fun logout(): Result<Unit> {
        Log.d(TAG, "Logging out user and clearing session data")
        
        // Clear WebView cookies and session data
        WebViewUtil.clearWebViewData(context)
        
        // Call API logout endpoint and clear stored token
        return ApiHelper.logout()
    }
} 