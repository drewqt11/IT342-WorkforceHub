package cit.edu.workforcehub.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Interceptor that logs API requests and responses in a clean, informative way.
 * 
 * @param tag The tag to use for logging (default: "ApiInterceptor")
 * @param level The level of detail to include in logs
 */
class LoggingInterceptor(
    private val tag: String = "ApiInterceptor",
    private val level: Level = Level.BODY
) : Interceptor {

    enum class Level {
        NONE,      // Don't log anything
        BASIC,     // Log only the request method and URL, and the response status code
        HEADERS,   // Log request and response headers
        BODY       // Log request and response headers and bodies
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        
        if (level == Level.NONE) {
            return chain.proceed(requestBuilder.build())
        }

        // Log request
        val requestStartMessage = StringBuilder("┌────── Request ──────────────────────────────────────────────")
        Log.d(tag, requestStartMessage.toString())

        val method = request.method
        val url = request.url

        Log.d(tag, "│ $method $url")
        
        if (level.ordinal >= Level.HEADERS.ordinal) {
            val headers = request.headers
            for (i in 0 until headers.size) {
                Log.d(tag, "│ ${headers.name(i)}: ${headers.value(i)}")
            }
            
            if (level.ordinal >= Level.BODY.ordinal) {
                val requestBody = request.body
                if (requestBody != null) {
                    val buffer = Buffer()
                    requestBody.writeTo(buffer)
                    val charset = requestBody.contentType()?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
                    val bodyString = buffer.readString(charset)
                    Log.d(tag, "│")
                    Log.d(tag, "│ Body:")
                    Log.d(tag, "│ $bodyString")
                }
            }
        }
        
        Log.d(tag, "└─────────────────────────────────────────────────────────────")

        // Proceed with the request and time it
        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Log.e(tag, "┌────── Network Error ──────────────────────────────────────")
            Log.e(tag, "│ ${e.javaClass.simpleName}: ${e.message}")
            Log.e(tag, "└─────────────────────────────────────────────────────────")
            throw e
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        // Log response
        val responseStartMessage = StringBuilder("┌────── Response [${response.code}] (${tookMs}ms) ────────────────────")
        Log.d(tag, responseStartMessage.toString())
        
        if (level.ordinal >= Level.HEADERS.ordinal) {
            val headers = response.headers
            for (i in 0 until headers.size) {
                Log.d(tag, "│ ${headers.name(i)}: ${headers.value(i)}")
            }
        }
        
        if (level.ordinal >= Level.BODY.ordinal) {
            val responseBody = response.body
            if (responseBody != null) {
                val contentType = responseBody.contentType()
                val contentLength = responseBody.contentLength()
                val bodyString = responseBody.string()
                
                Log.d(tag, "│")
                Log.d(tag, "│ Body:")
                Log.d(tag, "│ $bodyString")
                
                // Since we've consumed the response body, we need to create a new one
                val newResponseBody = bodyString.toResponseBody(contentType)
                
                Log.d(tag, "└─────────────────────────────────────────────────────────")
                
                return response.newBuilder()
                    .body(newResponseBody)
                    .build()
            }
        }
        
        Log.d(tag, "└─────────────────────────────────────────────────────────")
        return response
    }
} 