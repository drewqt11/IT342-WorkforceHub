package cit.edu.workforcehub

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import cit.edu.workforcehub.api.ApiHelper
import cit.edu.workforcehub.auth.AuthManager
import cit.edu.workforcehub.auth.OAuthWebViewActivity
import cit.edu.workforcehub.presentation.components.AppScreen
import cit.edu.workforcehub.presentation.screens.DashboardScreen
import cit.edu.workforcehub.presentation.screens.MainScreen
import cit.edu.workforcehub.presentation.screens.ProfileScreen
import cit.edu.workforcehub.presentation.screens.TimeAttendanceScreen
import cit.edu.workforcehub.presentation.screens.LeaveRequestScreen
import cit.edu.workforcehub.presentation.screens.PerformanceScreen
import cit.edu.workforcehub.presentation.screens.TrainingScreen
import cit.edu.workforcehub.presentation.theme.AppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    // State to track login status
    private val isLoggedInState = mutableStateOf(false)
    private val currentScreenState = mutableStateOf(AppScreen.DASHBOARD)
    
    // Authentication manager
    private lateinit var authManager: AuthManager
    
    // Request code for OAuth activity
    private val OAUTH_REQUEST_CODE = 1001
    
    // Activity result launcher for OAuth
    private val oauthActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val success = data?.getBooleanExtra(OAuthWebViewActivity.EXTRA_AUTH_SUCCESS, false) ?: false
            
            if (success) {
                // Update UI state to reflect successful login
                isLoggedInState.value = true
                currentScreenState.value = AppScreen.DASHBOARD
                Toast.makeText(this, "Authentication successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Authentication cancelled", Toast.LENGTH_SHORT).show()
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize API Helper
        ApiHelper.init(applicationContext)
        
        // Initialize Auth Manager
        authManager = AuthManager.getInstance(applicationContext)
        
        // Check if user is already authenticated
        isLoggedInState.value = authManager.isAuthenticated()
        
        setContent {
            AppTheme {
                WorkforceHubApp(
                    isLoggedIn = isLoggedInState.value,
                    currentScreen = currentScreenState.value,
                    onMicrosoftLoginClick = { startWebViewOAuth() },
                    onLogout = { handleLogout() },
                    onScreenChange = { screen -> 
                        currentScreenState.value = screen 
                    }
                )
            }
        }
        
        // Check if this activity was started from a deep link
        handleIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent) {
        // Check if this intent has data (from a deep link)
        val data: Uri? = intent.data
        if (data != null) {
            lifecycleScope.launch {
                val success = authManager.handleOAuthRedirect(data)
                if (success) {
                    // Update UI state to reflect successful login
                    isLoggedInState.value = true
                    currentScreenState.value = AppScreen.DASHBOARD
                    Toast.makeText(this@MainActivity, "Authentication successful!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    /**
     * Start OAuth flow using WebView-based approach
     */
    private fun startWebViewOAuth() {
        val intent = Intent(this, OAuthWebViewActivity::class.java)
        oauthActivityResultLauncher.launch(intent)
    }
    
    private fun handleLogout() {
        lifecycleScope.launch {
            authManager.logout()
            isLoggedInState.value = false
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == OAUTH_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val success = data?.getBooleanExtra(OAuthWebViewActivity.EXTRA_AUTH_SUCCESS, false) ?: false
                
                if (success) {
                    // Update UI state to reflect successful login
                    isLoggedInState.value = true
                    currentScreenState.value = AppScreen.DASHBOARD
                    Toast.makeText(this, "Authentication successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Authentication cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun WorkforceHubApp(
    isLoggedIn: Boolean,
    currentScreen: AppScreen,
    onMicrosoftLoginClick: () -> Unit,
    onLogout: () -> Unit,
    onScreenChange: (AppScreen) -> Unit
) {
    val context = LocalContext.current
    
    if (isLoggedIn) {
        when (currentScreen) {
            AppScreen.DASHBOARD -> {
                // Show dashboard when logged in
                DashboardScreen(
                    onLogout = onLogout,
                    onNavigateToAttendance = {
                        // Navigate to Time & Attendance screen
                        onScreenChange(AppScreen.TIME_ATTENDANCE)
                    },
                    onNavigateToLeaveRequests = {
                        onScreenChange(AppScreen.LEAVE_REQUESTS)
                    },
                    onNavigateToPerformance = {
                        onScreenChange(AppScreen.PERFORMANCE)
                    },
                    onNavigateToTraining = {
                        onScreenChange(AppScreen.TRAINING)
                    },
                    onNavigateToProfile = {
                        onScreenChange(AppScreen.PROFILE)
                    }
                )
            }
            AppScreen.TIME_ATTENDANCE -> {
                TimeAttendanceScreen(
                    onNavigateToDashboard = {
                        onScreenChange(AppScreen.DASHBOARD)
                    },
                    onLogout = onLogout,
                    onNavigateToLeaveRequests = {
                        onScreenChange(AppScreen.LEAVE_REQUESTS)
                    },
                    onNavigateToPerformance = {
                        onScreenChange(AppScreen.PERFORMANCE)
                    },
                    onNavigateToTraining = {
                        onScreenChange(AppScreen.TRAINING)
                    },
                    onNavigateToProfile = {
                        onScreenChange(AppScreen.PROFILE)
                    }
                )
            }
            AppScreen.LEAVE_REQUESTS -> {
                LeaveRequestScreen(
                    onLogout = onLogout,
                    onNavigateToDashboard = {
                        onScreenChange(AppScreen.DASHBOARD)
                    },
                    onNavigateToAttendance = {
                        onScreenChange(AppScreen.TIME_ATTENDANCE)
                    },
                    onNavigateToPerformance = {
                        onScreenChange(AppScreen.PERFORMANCE)
                    },
                    onNavigateToTraining = {
                        onScreenChange(AppScreen.TRAINING)
                    },
                    onNavigateToProfile = {
                        onScreenChange(AppScreen.PROFILE)
                    }
                )
            }
            AppScreen.PERFORMANCE -> {
                PerformanceScreen(
                    onLogout = onLogout,
                    onNavigateToDashboard = {
                        onScreenChange(AppScreen.DASHBOARD)
                    },
                    onNavigateToAttendance = {
                        onScreenChange(AppScreen.TIME_ATTENDANCE)
                    },
                    onNavigateToLeaveRequests = {
                        onScreenChange(AppScreen.LEAVE_REQUESTS)
                    },
                    onNavigateToTraining = {
                        onScreenChange(AppScreen.TRAINING)
                    },
                    onNavigateToProfile = {
                        onScreenChange(AppScreen.PROFILE)
                    }
                )
            }
            AppScreen.TRAINING -> {
                TrainingScreen(
                    onLogout = onLogout,
                    onNavigateToDashboard = {
                        onScreenChange(AppScreen.DASHBOARD)
                    },
                    onNavigateToAttendance = {
                        onScreenChange(AppScreen.TIME_ATTENDANCE)
                    },
                    onNavigateToLeaveRequests = {
                        onScreenChange(AppScreen.LEAVE_REQUESTS)
                    },
                    onNavigateToPerformance = {
                        onScreenChange(AppScreen.PERFORMANCE)
                    },
                    onNavigateToProfile = {
                        onScreenChange(AppScreen.PROFILE)
                    }
                )
            }
            AppScreen.PROFILE -> {
                ProfileScreen(
                    onLogout = onLogout,
                    onNavigateToDashboard = {
                        onScreenChange(AppScreen.DASHBOARD)
                    },
                    onNavigateToAttendance = {
                        onScreenChange(AppScreen.TIME_ATTENDANCE)
                    },
                    onNavigateToLeaveRequests = {
                        onScreenChange(AppScreen.LEAVE_REQUESTS)
                    },
                    onNavigateToPerformance = {
                        onScreenChange(AppScreen.PERFORMANCE)
                    },
                    onNavigateToTraining = {
                        onScreenChange(AppScreen.TRAINING)
                    }
                )
            }
            // For now, other screens can just redirect to Dashboard
            else -> {
                // If we get an unimplemented screen, navigate to Dashboard as a fallback
                DashboardScreen(
                    onLogout = onLogout,
                    onNavigateToAttendance = {
                        onScreenChange(AppScreen.TIME_ATTENDANCE)
                    },
                    onNavigateToLeaveRequests = {
                        onScreenChange(AppScreen.LEAVE_REQUESTS)
                    },
                    onNavigateToPerformance = {
                        onScreenChange(AppScreen.PERFORMANCE)
                    },
                    onNavigateToTraining = {
                        onScreenChange(AppScreen.TRAINING)
                    },
                    onNavigateToProfile = {
                        onScreenChange(AppScreen.PROFILE)
                    }
                )
            }
        }
    } else {
        // Show login screen when not logged in
        MainScreen(
            onMicrosoftLoginClick = onMicrosoftLoginClick
        )
    }
}