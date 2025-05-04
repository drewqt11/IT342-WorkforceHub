package cit.edu.workforcehub.api

/**
 * Contains all the API endpoints used in the application.
 * 
 * This class centralizes all endpoint URLs, making them easy to update and maintain.
 */
object ApiEndpoints {
    // Base URLs
    //const val BASE_URL = "https://api-workforcehub.aetherrflare.org" // brian
    const val BASE_URL = "https://java-app-6sj5.onrender.com" // render
    
    // Auth endpoints
    const val LOGIN = "auth/login"
    const val REGISTER = "auth/register"
    const val REFRESH_TOKEN = "auth/refresh-token"
    const val LOGOUT = "auth/logout"
    
    // Employee endpoints
    const val EMPLOYEE_PROFILE = "/api/employee/profile"
    const val EMPLOYEE_ATTENDANCE = "/api/employee/attendance"
    
    // HR endpoints
    const val DEPARTMENTS = "hr/departments"
    const val JOB_TITLES = "hr/job-titles"
    const val EMPLOYEES = "hr/employees"
} 