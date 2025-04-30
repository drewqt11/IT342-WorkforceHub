package com.example.myapplication.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Singleton class that provides a configured Retrofit instance for API calls.
 * 
 * This class handles the setup of:
 * - OkHttp client with interceptors, timeouts, etc.
 * - Moshi for JSON serialization
 * - Retrofit for API communication
 */
object ApiClient {
    private const val CONNECT_TIMEOUT = 15L // seconds
    private const val READ_TIMEOUT = 30L // seconds
    private const val WRITE_TIMEOUT = 15L // seconds

    /**
     * Create and configure the OkHttpClient with logging interceptor
     */
    private fun createOkHttpClient(authToken: String? = null): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            // Add the logging interceptor
            .addInterceptor(LoggingInterceptor())
        
        // Add auth token if provided
        authToken?.let { token ->
            builder.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .method(original.method, original.body)
                chain.proceed(requestBuilder.build())
            }
        }
        
        return builder.build()
    }

    /**
     * Configure Moshi for JSON serialization/deserialization
     */
    private fun createMoshi(): Moshi {
        return Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    /**
     * Create a Retrofit instance with the given base URL and OkHttp client
     */
    private fun createRetrofit(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    /**
     * Create an API service with the given token (optional)
     */
    fun <T> createService(serviceClass: Class<T>, authToken: String? = null): T {
        val okHttpClient = createOkHttpClient(authToken)
        val moshi = createMoshi()
        val retrofit = createRetrofit(ApiEndpoints.BASE_URL, okHttpClient, moshi)
        
        return retrofit.create(serviceClass)
    }

    /**
     * Create an API service with a specific base URL and token (optional)
     */
    fun <T> createService(serviceClass: Class<T>, baseUrl: String, authToken: String? = null): T {
        val okHttpClient = createOkHttpClient(authToken)
        val moshi = createMoshi()
        val retrofit = createRetrofit(baseUrl, okHttpClient, moshi)
        
        return retrofit.create(serviceClass)
    }
} 