package cit.edu.workforce

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cit.edu.workforce.ui.theme.WorkforceHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkforceHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isLogin by remember { mutableStateOf(true) }
                    
                    LoginSignupScreen(
                        isLogin = isLogin,
                        onNavigateToSignup = { isLogin = false },
                        onNavigateToLogin = { isLogin = true }
                    )
                }
            }
        }
    }
}