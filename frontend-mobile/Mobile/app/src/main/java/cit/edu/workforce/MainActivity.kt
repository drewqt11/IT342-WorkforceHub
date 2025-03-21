package cit.edu.workforce

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cit.edu.workforce.ui.theme.WorkforceHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkforceHubTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login_signup"
    ) {
        composable("login_signup") {
            LoginSignupScreen(
                isLogin = true,
                onNavigateToSignup = { navController.navigate("sign_up") },
                onNavigateToLogin = { navController.navigate("sign_in") }
            )
        }
        composable("sign_in") {
            SignInScreen(
                onSignUpClick = { navController.navigate("sign_up") },
                onForgotPasswordClick = { /* Handle later */ }
            )
        }
        composable("sign_up") {
            SignUpScreen(
                onNavigateBack = { navController.navigateUp() },
                onSignUpClick = { firstName, lastName, email, password ->
                    // Handle sign up logic here
                    navController.navigate("sign_in")
                }
            )
        }
    }
}