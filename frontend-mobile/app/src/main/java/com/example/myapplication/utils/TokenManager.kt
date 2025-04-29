package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.data.models.AuthResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Class responsible for managing authentication tokens
 */
class TokenManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        Constants.PREF_NAME, 
        Context.MODE_PRIVATE
    )
    
    /**
     * Saves authentication response to SharedPreferences
     */
    fun saveAuthData(authResponse: AuthResponse) {
        val editor = sharedPreferences.edit()
        editor.putString(Constants.KEY_ACCESS_TOKEN, authResponse.accessToken)
        editor.putString(Constants.KEY_REFRESH_TOKEN, authResponse.refreshToken)
        editor.putString(Constants.KEY_USER_ID, authResponse.userId)
        editor.putString(Constants.KEY_USERNAME, authResponse.username)
        editor.putString(Constants.KEY_ROLES, Gson().toJson(authResponse.roles))
        
        // Calculate token expiration time
        val expiresAt = System.currentTimeMillis() + (authResponse.expiresIn * 1000)
        editor.putLong(Constants.KEY_EXPIRES_AT, expiresAt)
        
        editor.apply()
    }
    
    /**
     * Gets the current access token
     */
    fun getAccessToken(): String? {
        return sharedPreferences.getString(Constants.KEY_ACCESS_TOKEN, null)
    }
    
    /**
     * Gets the refresh token
     */
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(Constants.KEY_REFRESH_TOKEN, null)
    }
    
    /**
     * Gets the user ID
     */
    fun getUserId(): String? {
        return sharedPreferences.getString(Constants.KEY_USER_ID, null)
    }
    
    /**
     * Gets the username (email)
     */
    fun getUsername(): String? {
        return sharedPreferences.getString(Constants.KEY_USERNAME, null)
    }
    
    /**
     * Gets the user roles
     */
    fun getRoles(): List<String> {
        val rolesJson = sharedPreferences.getString(Constants.KEY_ROLES, null)
        return if (rolesJson != null) {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(rolesJson, type)
        } else {
            emptyList()
        }
    }
    
    /**
     * Checks if the current token is expired
     */
    fun isTokenExpired(): Boolean {
        val expiresAt = sharedPreferences.getLong(Constants.KEY_EXPIRES_AT, 0)
        return System.currentTimeMillis() >= expiresAt
    }
    
    /**
     * Checks if the user is logged in
     */
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null && !isTokenExpired()
    }
    
    /**
     * Clears all authentication data
     */
    fun clearAuthData() {
        sharedPreferences.edit().clear().apply()
    }
} 