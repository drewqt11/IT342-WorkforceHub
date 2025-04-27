package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
            android.os.Handler(context.mainLooper).postDelayed({
                Toast.makeText(
                    context,
                    "Authentication successful!",
                    Toast.LENGTH_SHORT
                ).show()
            }, 1500)
        }
    )
}