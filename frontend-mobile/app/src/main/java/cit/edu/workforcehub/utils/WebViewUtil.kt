package cit.edu.workforcehub.utils

import android.content.Context
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.util.Log

/**
 * Utility class for WebView related operations
 */
object WebViewUtil {
    private const val TAG = "WebViewUtil"
    
    /**
     * Clear all WebView cookies and session data
     * This should be called when logging out to ensure that the next login
     * doesn't automatically use the previous session
     */
    fun clearWebViewData(context: Context) {
        try {
            Log.d(cit.edu.workforcehub.utils.WebViewUtil.TAG, "Clearing WebView data and cookies")
            
            // Clear cookies
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookies(null)
            cookieManager.flush()
            
            // Clear cache
            WebView(context).clearCache(true)
            
            // Clear storage
            WebStorage.getInstance().deleteAllData()
            
            // On newer Android versions, more clearing options
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create a throwaway WebView to clear its data
                val webView = WebView(context)
                
                // Instead of setting null directly, create an empty WebViewClient
                webView.webViewClient = WebViewClient()
                
                // Clear form data
                webView.clearFormData()
                
                // Clear history
                webView.clearHistory()
                
                // Clear all data
                webView.clearSslPreferences()
                
                // Make sure to destroy the WebView to free resources
                webView.destroy()
            }
            
            Log.d(cit.edu.workforcehub.utils.WebViewUtil.TAG, "WebView data and cookies cleared successfully")
        } catch (e: Exception) {
            Log.e(cit.edu.workforcehub.utils.WebViewUtil.TAG, "Error clearing WebView data", e)
        }
    }
} 