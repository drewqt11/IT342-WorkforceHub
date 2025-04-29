package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.api.WorkforceApi
import com.example.myapplication.data.models.AuthResponse
import com.example.myapplication.data.models.TokenRefreshRequest
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.TokenManager
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Repository that handles all authentication related operations
 */
class AuthRepository(private val context: Context) {
    
    private val tokenManager = TokenManager(context)
    private val publicApi = RetrofitClient.createPublicClient()
    
    private var msalClient: ISingleAccountPublicClientApplication? = null
    
    /**
     * Initialize the MSAL client
     */
    suspend fun initializeMsal(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (msalClient == null) {
                    msalClient = PublicClientApplication.createSingleAccountPublicClientApplication(
                        context,
                        Constants.AUTH_CONFIG
                    )
                }
                true
            } catch (e: Exception) {
                Log.e("AuthRepository", "Failed to initialize MSAL", e)
                false
            }
        }
    }
    
    /**
     * Sign in with Microsoft account
     */
    suspend fun signInWithMicrosoft(): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Ensure MSAL is initialized
                if (msalClient == null && !initializeMsal()) {
                    return@withContext Result.failure(Exception("Failed to initialize MSAL"))
                }
                
                // Get current account if it exists
                val currentAccount = msalClient?.currentAccount?.currentAccount
                
                // Get auth result either by silent auth or interactive auth
                val authResult = if (currentAccount != null) {
                    acquireTokenSilently(currentAccount)
                } else {
                    acquireTokenInteractively()
                }
                
                // Get user info from token
                val email = authResult.account?.username ?: 
                    throw Exception("Failed to get email from Microsoft authentication")
                
                // Exchange token with backend
                Log.d("AuthRepository", "Exchanging token with backend for email: $email")
                val response = publicApi.getOAuth2TokenInfo(email)
                
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    tokenManager.saveAuthData(authResponse)
                    Result.success(authResponse)
                } else {
                    Result.failure(Exception("Failed to exchange token: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Microsoft sign-in failed", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Acquire token silently (without UI)
     */
    private suspend fun acquireTokenSilently(account: IAccount): IAuthenticationResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                msalClient?.acquireTokenSilentAsync(
                    Constants.SCOPES,
                    account.authority,
                    object : AuthenticationCallback {
                        override fun onSuccess(authenticationResult: IAuthenticationResult) {
                            continuation.resume(authenticationResult)
                        }
                        
                        override fun onCancel() {
                            continuation.resumeWithException(Exception("Authentication cancelled"))
                        }
                        
                        override fun onError(exception: MsalException) {
                            continuation.resumeWithException(exception)
                        }
                    }
                )
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
    
    /**
     * Acquire token interactively (with UI)
     */
    private suspend fun acquireTokenInteractively(): IAuthenticationResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                msalClient?.acquireToken(
                    Constants.SCOPES,
                    object : AuthenticationCallback {
                        override fun onSuccess(authenticationResult: IAuthenticationResult) {
                            continuation.resume(authenticationResult)
                        }
                        
                        override fun onCancel() {
                            continuation.resumeWithException(Exception("Authentication cancelled"))
                        }
                        
                        override fun onError(exception: MsalException) {
                            continuation.resumeWithException(exception)
                        }
                    }
                )
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
    
    /**
     * Refresh token when it's expired
     */
    suspend fun refreshToken(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val refreshToken = tokenManager.getRefreshToken() ?: 
                    return@withContext Result.failure(Exception("No refresh token available"))
                
                val response = publicApi.refreshToken(TokenRefreshRequest(refreshToken))
                
                if (response.isSuccessful && response.body() != null) {
                    val tokenResponse = response.body()!!
                    // Create AuthResponse from TokenRefreshResponse
                    val authResponse = AuthResponse(
                        accessToken = tokenResponse.accessToken,
                        refreshToken = tokenResponse.refreshToken,
                        tokenType = tokenResponse.tokenType,
                        userId = tokenManager.getUserId() ?: "",
                        username = tokenManager.getUsername() ?: "",
                        roles = tokenManager.getRoles(),
                        expiresIn = tokenResponse.expiresIn
                    )
                    tokenManager.saveAuthData(authResponse)
                    Result.success(true)
                } else {
                    Result.failure(Exception("Failed to refresh token: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Token refresh failed", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Sign out user
     */
    suspend fun signOut(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Sign out from MSAL
                msalClient?.signOut()
                
                // Clear local token storage
                tokenManager.clearAuthData()
                
                Result.success(true)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Sign out failed", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
    
    /**
     * Get authenticated API client
     */
    fun getAuthenticatedApi(): Result<WorkforceApi> {
        return try {
            val api = RetrofitClient.createAuthenticatedClient(tokenManager)
            Result.success(api)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 