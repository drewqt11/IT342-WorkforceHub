package cit.edu.workforcehub.auth

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar

/**
 * Custom WebViewClient that intercepts OAuth redirects and extracts token data
 */
class OAuthWebViewClient(
    private val onTokenReceived: (token: String) -> Unit,
    private val progressBar: ProgressBar? = null
) : WebViewClient() {
    
    companion object {
        private const val TAG = "OAuthWebViewClient"
        // The redirect URL pattern to look for
        private const val OAUTH_REDIRECT_PATH = "/oauth2/redirect"
    }
    
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        // Show progress bar when page starts loading
        progressBar?.visibility = View.VISIBLE
    }
    
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        // Hide progress bar when page is loaded
        progressBar?.visibility = View.GONE
    }
    
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url ?: return false
        val urlString = url.toString()
        
        Log.d(TAG, "Intercepted URL: $urlString")
        
        // Check if this is our OAuth redirect URL
        if (urlString.contains(OAUTH_REDIRECT_PATH)) {
            Log.d(TAG, "Detected OAuth redirect: $urlString")
            
            // Extract token from the URL
            url.getQueryParameter("token")?.let { token ->
                Log.d(TAG, "Extracted token from URL")
                onTokenReceived(token)
                return true // Prevent WebView from loading the redirect URL
            }
        }
        
        // Allow WebView to handle other URLs normally
        return false
    }
} 