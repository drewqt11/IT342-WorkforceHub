package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.presentation.components.AppScreen
import com.example.myapplication.presentation.screens.DashboardScreen
import com.example.myapplication.presentation.screens.MainScreen
import com.example.myapplication.presentation.screens.TimeAttendanceScreen
import com.example.myapplication.presentation.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                WorkforceHubApp()
            }
        }
    }
}

@Composable
fun WorkforceHubApp() {
    val context = LocalContext.current
    // State to control whether to show login screen or dashboard
    var isLoggedIn by remember { mutableStateOf(false) }
    // State to track current screen using AppScreen enum instead of strings
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    
    if (isLoggedIn) {
        when (currentScreen) {
            AppScreen.DASHBOARD -> {
                // Show dashboard when logged in
                DashboardScreen(
                    onLogout = {
                        // Handle logout - set isLoggedIn to false to return to login screen
                        isLoggedIn = false
                    },
                    onNavigateToAttendance = {
                        // Navigate to Time & Attendance screen
                        currentScreen = AppScreen.TIME_ATTENDANCE
                    },
                    onNavigateToLeaveRequests = {
                        currentScreen = AppScreen.LEAVE_REQUESTS
                    },
                    onNavigateToPerformance = {
                        currentScreen = AppScreen.PERFORMANCE
                    },
                    onNavigateToTraining = {
                        currentScreen = AppScreen.TRAINING
                    },
                    onNavigateToProfile = {
                        currentScreen = AppScreen.PROFILE
                    }
                )
            }
            AppScreen.TIME_ATTENDANCE -> {
                TimeAttendanceScreen(
                    onBack = {
                        // Navigate back to dashboard
                        currentScreen = AppScreen.DASHBOARD
                    },
                    onLogout = {
                        // Handle logout
                        isLoggedIn = false
                    },
                    onNavigateToLeaveRequests = {
                        currentScreen = AppScreen.LEAVE_REQUESTS
                    },
                    onNavigateToPerformance = {
                        currentScreen = AppScreen.PERFORMANCE
                    },
                    onNavigateToTraining = {
                        currentScreen = AppScreen.TRAINING
                    },
                    onNavigateToProfile = {
                        currentScreen = AppScreen.PROFILE
                    }
                )
            }
            // For now, other screens can just redirect to Dashboard
            else -> {
                // Placeholder - in the future, you'll implement these screens
                currentScreen = AppScreen.DASHBOARD
            }
        }
    } else {
        // Show login screen when not logged in
        MainScreen(
            onMicrosoftLoginClick = {
                // Handle Microsoft login click
                Toast.makeText(
                    context,
                    "Microsoft Login Initiated",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Here you would implement the actual authentication logic
                // This simulates a successful login after a delay
                Handler(context.mainLooper).postDelayed({
                    Toast.makeText(
                        context,
                        "Authentication successful!",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Set logged in to true to navigate to dashboard
                    isLoggedIn = true
                    currentScreen = AppScreen.DASHBOARD
                }, 1500)
            }
        )
    }
}