package com.example.myapplication.api

/**
 * Contains all the API endpoints used in the application.
 * 
 * This class centralizes all endpoint URLs, making them easy to update and maintain.
 */
object ApiEndpoints {
    // Base URLs
    const val BASE_URL = "http://10.0.2.2:8080/api/" // For Android Emulator pointing to localhost
    // const val BASE_URL = "https://api.workforcehub.com/api/" // Production
    
    // Auth endpoints
    const val LOGIN = "auth/login"
    const val REGISTER = "auth/register"
    const val REFRESH_TOKEN = "auth/refresh-token"
    const val LOGOUT = "auth/logout"
    
    // Employee endpoints
    const val EMPLOYEE_PROFILE = "employee/profile"
    const val EMPLOYEE_ATTENDANCE = "employee/attendance"
    
    // HR endpoints
    const val DEPARTMENTS = "hr/departments"
    const val JOB_TITLES = "hr/job-titles"
    const val EMPLOYEES = "hr/employees"
} 