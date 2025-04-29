package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.presentation.components.AppScreen
import com.example.myapplication.presentation.screens.DashboardScreen
import com.example.myapplication.presentation.screens.LoginScreen
import com.example.myapplication.presentation.screens.TimeAttendanceScreen
import com.example.myapplication.presentation.theme.AppTheme
import com.example.myapplication.presentation.viewmodels.AuthViewModel

class MainActivity : ComponentActivity() {
    
    private lateinit var authViewModel: AuthViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewModel
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        setContent {
            AppTheme {
                WorkforceHubApp(authViewModel)
            }
        }
    }
}

@Composable
fun WorkforceHubApp(authViewModel: AuthViewModel) {
    val context = LocalContext.current
    
    // Observe authentication state
    val isLoggedIn by authViewModel.isLoggedIn.observeAsState(false)
    val isLoading by authViewModel.isLoading.observeAsState(false)
    val errorMessage by authViewModel.errorMessage.observeAsState()
    
    // Show error message if any
    errorMessage?.let { error ->
        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        // Clear error after showing
        authViewModel.clearError()
    }
    
    // State to track current screen
    val currentScreen = if (isLoggedIn) AppScreen.DASHBOARD else AppScreen.LOGIN
    
    when (currentScreen) {
        AppScreen.LOGIN -> {
            LoginScreen(
                isLoading = isLoading,
                onMicrosoftLoginClick = {
                    authViewModel.signInWithMicrosoft()
                }
            )
        }
        AppScreen.DASHBOARD -> {
            DashboardScreen(
                onLogout = {
                    authViewModel.signOut()
                },
                onNavigateToAttendance = {
                    // Navigate to Time & Attendance screen
                },
                onNavigateToLeaveRequests = {
                    // Navigate to Leave Requests screen
                },
                onNavigateToPerformance = {
                    // Navigate to Performance screen
                },
                onNavigateToTraining = {
                    // Navigate to Training screen
                },
                onNavigateToProfile = {
                    // Navigate to Profile screen
                }
            )
        }
        AppScreen.TIME_ATTENDANCE -> {
            TimeAttendanceScreen(
                onBack = {
                    // Navigate back to dashboard
                },
                onLogout = {
                    authViewModel.signOut()
                },
                onNavigateToLeaveRequests = {
                    // Navigate to Leave Requests screen
                },
                onNavigateToPerformance = {
                    // Navigate to Performance screen
                },
                onNavigateToTraining = {
                    // Navigate to Training screen
                },
                onNavigateToProfile = {
                    // Navigate to Profile screen
                }
            )
        }
        else -> {
            // For other screens, show dashboard for now
            DashboardScreen(
                onLogout = {
                    authViewModel.signOut()
                },
                onNavigateToAttendance = { },
                onNavigateToLeaveRequests = { },
                onNavigateToPerformance = { },
                onNavigateToTraining = { },
                onNavigateToProfile = { }
            )
        }
    }
}