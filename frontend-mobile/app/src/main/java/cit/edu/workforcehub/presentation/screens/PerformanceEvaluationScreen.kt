package cit.edu.workforcehub.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import cit.edu.workforcehub.api.ApiHelper
import cit.edu.workforcehub.api.models.EmployeeProfile
import cit.edu.workforcehub.presentation.components.AppHeader
import cit.edu.workforcehub.presentation.components.AppScreen
import cit.edu.workforcehub.presentation.components.LoadingComponent
import cit.edu.workforcehub.presentation.components.UniversalDrawer

@Composable
fun PerformanceScreen(
    onLogout: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToLeaveRequests: () -> Unit = {},
    onNavigateToPerformance: () -> Unit = {},
    onNavigateToTraining: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // State for drawer
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Scroll state for content
    val scrollState = rememberScrollState()

    // State for profile data
    var profileData by remember { mutableStateOf<EmployeeProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fetch profile data
    LaunchedEffect(key1 = true) {
        try {
            val employeeService = ApiHelper.getEmployeeService()
            val response = employeeService.getProfile()

            if (response.isSuccessful && response.body() != null) {
                profileData = response.body()
                isLoading = false
            } else {
                error = "Failed to load profile: ${response.message()}"
                isLoading = false
            }
        } catch (e: Exception) {
            error = "Error loading profile: ${e.message}"
            isLoading = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = cit.edu.workforcehub.presentation.theme.AppColors.gray50
    ) {
        // Using the UniversalDrawer
        UniversalDrawer(
            drawerState = drawerState,
            currentScreen = AppScreen.PERFORMANCE,
            onLogout = onLogout,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToAttendance = onNavigateToAttendance,
            onNavigateToLeaveRequests = onNavigateToLeaveRequests,
            onNavigateToPerformance = {}, // Already on performance, no need to navigate
            onNavigateToTraining = onNavigateToTraining,
            onNavigateToProfile = onNavigateToProfile
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Show loading component if data is still loading
                if (isLoading) {
                    LoadingComponent()
                } else {
                    // Content that scrolls underneath the header
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 350.dp)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Placeholder content
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp)
                                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = cit.edu.workforcehub.presentation.theme.AppColors.white),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(cit.edu.workforcehub.presentation.theme.AppColors.blue50, cit.edu.workforcehub.presentation.theme.AppColors.teal50),
                                            start = Offset(0f, 0f),
                                            end = Offset(1000f, 0f)
                                        )
                                    )
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    
                                    Text(
                                        text = "Under Development",
                                        color = cit.edu.workforcehub.presentation.theme.AppColors.gray800,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    
                                    Text(
                                        text = "We're working hard to bring you the best experience.",
                                        color = cit.edu.workforcehub.presentation.theme.AppColors.gray700,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(bottom = 16.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = cit.edu.workforcehub.presentation.theme.AppColors.teal500,
                                                shape = RoundedCornerShape(24.dp)
                                            )
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "Coming Soon!",
                                            color = cit.edu.workforcehub.presentation.theme.AppColors.white,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Fixed header on top (doesn't scroll)
                AppHeader(
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    modifier = Modifier.zIndex(1f), // Ensure header stays on top
                    onProfileClick = onNavigateToProfile,
                    forceAutoFetch = true // Let AppHeader handle profile data fetching
                )
            }
        }
    }
}