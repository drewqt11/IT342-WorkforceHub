package com.example.myapplication.utils

/**
 * Object containing application-wide constants
 */
object Constants {
    // Backend URL - change this to your actual server IP/domain when testing
    // For local development, use 10.0.2.2 which is the special IP for the host from Android emulator
    const val BASE_URL = "http://10.0.2.2:8080/"

    // Auth related constants
    const val PREF_NAME = "WorkforceHubPrefs"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USERNAME = "username"
    const val KEY_ROLES = "roles"
    const val KEY_EXPIRES_AT = "expires_at"
    
    // Microsoft OAuth related constants
    val SCOPES = arrayOf("User.Read", "profile", "email", "openid")
    
    // MSAL Auth Config
    const val AUTH_CONFIG = "auth_config.json"
} 