package com.example.myapplication.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.api.ApiEndpoints
import com.example.myapplication.api.ApiHelper
import com.example.myapplication.api.models.AuthResponse
import com.example.myapplication.utils.WebViewUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Activity that handles OAuth authentication using a WebView
 */
class OAuthWebViewActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "OAuthWebViewActivity"
        private const val OAUTH_PATH = "/oauth2/authorization/microsoft"
        
        // Intent extra keys
        const val EXTRA_AUTH_SUCCESS = "auth_success"
        const val EXTRA_AUTH_TOKEN = "auth_token"
        const val EXTRA_AUTH_EMAIL = "auth_email"
        
        /**
         * Start this activity from any context
         */
        fun startForResult(activity: Activity, requestCode: Int) {
            val intent = Intent(activity, OAuthWebViewActivity::class.java)
            activity.startActivityForResult(intent, requestCode)
        }
    }
    
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var isAuthSuccessful = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth_webview)
        
        // Initialize views
        webView = findViewById(R.id.oauth_webview)
        progressBar = findViewById(R.id.progress_bar)
        
        // Configure WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        
        // Set custom WebViewClient to intercept redirects
        webView.webViewClient = OAuthWebViewClient(
            onTokenReceived = { token ->
                // This is called when the token is extracted from the redirect URL
                isAuthSuccessful = true
                handleOAuthSuccess(token)
            },
            progressBar = progressBar
        )
        
        // Start the OAuth flow
        startOAuthFlow()
    }
    
    private fun startOAuthFlow() {
        // Show loading indicator for initial load
        progressBar.visibility = View.VISIBLE
        
        // Load the OAuth URL
        val authUrl = ApiEndpoints.BASE_URL + OAUTH_PATH
        Log.d(TAG, "Loading OAuth URL: $authUrl")
        webView.loadUrl(authUrl)
    }
    
    private fun handleOAuthSuccess(token: String) {
        Log.d(TAG, "OAuth token received")
        
        // Hide the WebView and show loading indicator
        webView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        
        // Process the token in a background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get user info using the token
                val response = ApiHelper.getAuthService().validateToken("Bearer $token")
                
                if (response.isSuccessful && response.body() != null) {
                    val userInfo = response.body()!!
                    
                    // Create the auth response
                    val authResponse = AuthResponse(
                        token = token,
                        refreshToken = "", // The server might not provide this
                        userId = userInfo.userId ?: "",
                        email = userInfo.email ?: "",
                        role = userInfo.role ?: "",
                        employeeId = userInfo.employeeId ?: "",
                        firstName = userInfo.firstName ?: "",
                        lastName = userInfo.lastName ?: "",
                        createdAt = Date()
                    )
                    
                    // Save the auth data
                    ApiHelper.saveAuthData(authResponse)
                    
                    // Return success to the calling activity
                    val resultIntent = Intent().apply {
                        putExtra(EXTRA_AUTH_SUCCESS, true)
                        putExtra(EXTRA_AUTH_TOKEN, token)
                        putExtra(EXTRA_AUTH_EMAIL, userInfo.email)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    // Handle error
                    Log.e(TAG, "Failed to validate token: ${response.code()} ${response.message()}")
                    handleAuthFailure()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing OAuth token", e)
                handleAuthFailure()
            }
        }
    }
    
    private fun handleAuthFailure() {
        // Clean up any WebView data since auth failed
        WebViewUtil.clearWebViewData(this)
        setResult(RESULT_CANCELED)
        finish()
    }
    
    override fun onDestroy() {
        // If authentication wasn't successful, clear WebView data
        if (!isAuthSuccessful) {
            WebViewUtil.clearWebViewData(this)
        }
        super.onDestroy()
    }
    
    override fun onBackPressed() {
        // User canceled the authentication, clear WebView data
        WebViewUtil.clearWebViewData(this)
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
} 