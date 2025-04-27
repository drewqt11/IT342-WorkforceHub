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
import com.example.myapplication.screens.DashboardScreen
import com.example.myapplication.screens.MainScreen
import com.example.myapplication.ui.theme.AppTheme

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
    
    if (isLoggedIn) {
        // Show dashboard when logged in
        DashboardScreen(
            onLogout = {
                // Handle logout - set isLoggedIn to false to return to login screen
                isLoggedIn = false
            }
        )
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
                }, 1500)
            }
        )
    }
}