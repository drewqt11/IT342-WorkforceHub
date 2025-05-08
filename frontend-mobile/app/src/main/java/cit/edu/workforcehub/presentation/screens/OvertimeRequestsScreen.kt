package cit.edu.workforcehub.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import cit.edu.workforcehub.R
import cit.edu.workforcehub.api.ApiHelper
import cit.edu.workforcehub.api.models.EmployeeProfile
import cit.edu.workforcehub.api.models.OvertimeRequest
import cit.edu.workforcehub.presentation.components.AppHeader
import cit.edu.workforcehub.presentation.components.AppScreen
import cit.edu.workforcehub.presentation.components.LoadingComponent
import cit.edu.workforcehub.presentation.components.UniversalDrawer
import cit.edu.workforcehub.presentation.theme.AppColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

/**
 * Formats a date string from ISO format (YYYY-MM-DD) to a readable format (Month DD, YYYY)
 */
private fun formatDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString.split('T')[0])
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
        date.format(formatter)
    } catch (e: Exception) {
        // Return the original string if parsing fails
        dateString
    }
}

/**
 * Returns a color based on the status of a request
 */
private fun getStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "APPROVED" -> AppColors.green
        "PENDING" -> AppColors.amber
        "REJECTED" -> AppColors.red
        else -> AppColors.gray500
    }
}

/**
 * Returns a background color based on the status of a request
 */
private fun getStatusBackgroundColor(status: String): Color {
    return when (status.uppercase()) {
        "APPROVED" -> AppColors.greenLight
        "PENDING" -> AppColors.amberLight
        "REJECTED" -> AppColors.redLight
        else -> AppColors.gray100
    }
}

@Composable
fun OvertimeRequestsScreen(
    onLogout: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToLeaveRequests: () -> Unit = {},
    onNavigateToOvertimeRequests: () -> Unit = {},
    onNavigateToReimbursementRequests: () -> Unit = {},
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
    
    // State for overtime requests
    var overtimeRequests by remember { mutableStateOf<List<OvertimeRequest>>(emptyList()) }
    var isLoadingOvertimeRequests by remember { mutableStateOf(true) }
    var overtimeRequestsError by remember { mutableStateOf<String?>(null) }
    
    // State for navigation
    var shouldNavigateToSubmitOvertimeRequest by remember { mutableStateOf(false) }
    
    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle navigation
    LaunchedEffect(shouldNavigateToSubmitOvertimeRequest) {
        if (shouldNavigateToSubmitOvertimeRequest) {
            // In a real app, this would navigate to a overtime request submission screen
            // For now, just show a message
            snackbarHostState.showSnackbar("Navigating to Submit Overtime Request form")
            shouldNavigateToSubmitOvertimeRequest = false
        }
    }

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
    
    // Fetch overtime requests
    LaunchedEffect(key1 = true) {
        try {
            val employeeService = ApiHelper.getEmployeeService()
            val response = employeeService.getOvertimeRequests()
            
            if (response.isSuccessful && response.body() != null) {
                overtimeRequests = response.body()!!
                isLoadingOvertimeRequests = false
            } else {
                overtimeRequestsError = "Failed to load overtime requests: ${response.message()}"
                isLoadingOvertimeRequests = false
            }
        } catch (e: Exception) {
            overtimeRequestsError = "Error loading overtime requests: ${e.message}"
            isLoadingOvertimeRequests = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.gray100
    ) {
        // Using the UniversalDrawer
        UniversalDrawer(
            drawerState = drawerState,
            currentScreen = AppScreen.OVERTIME_REQUESTS,
            onLogout = onLogout,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToAttendance = onNavigateToAttendance,
            onNavigateToLeaveRequests = onNavigateToLeaveRequests,
            onNavigateToOvertimeRequests = {}, // Already on overtime requests, no need to navigate
            onNavigateToReimbursementRequests = onNavigateToReimbursementRequests,
            onNavigateToPerformance = onNavigateToPerformance,
            onNavigateToTraining = onNavigateToTraining,
            onNavigateToProfile = onNavigateToProfile
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Fixed header on top
                AppHeader(
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    modifier = Modifier.zIndex(1f),
                    onProfileClick = onNavigateToProfile,
                    forceAutoFetch = true, // Let AppHeader handle profile data fetching
                    onLogoutClick = onLogout
                )

                // Scrollable content below header
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 70.dp) // Adjusted for smaller header
                ) {
                    // Show loading component if data is still loading
                    if (isLoading) {
                        LoadingComponent()
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(
                                    state = scrollState,
                                    enabled = true
                                )
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Add Overtime Requests header at the top of the scrollable content
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 2.dp, top = 8.dp, bottom = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Icon in circular blue background
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .shadow(
                                            elevation = 2.dp,
                                            shape = RoundedCornerShape(8.dp),
                                            clip = false
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(AppColors.blue500, Color(0xFF36D1DC)),
                                                start = Offset(0f, 0f),
                                                end = Offset(40f, 40f)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Overtime Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                // Overtime Requests text
                                Text(
                                    text = "Overtime Requests",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.gray800
                                )
                            }
                            
                            // Main Overtime Requests Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = AppColors.white),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, AppColors.gray200)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(AppColors.blue500, AppColors.teal500)
                                            )
                                        )
                                )
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    // Submit Request Button positioned at the top right
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        contentAlignment = Alignment.TopEnd
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .shadow(
                                                    elevation = 2.dp,
                                                    shape = RoundedCornerShape(12.dp),
                                                    spotColor = AppColors.blue700.copy(alpha = 0.3f)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable { shouldNavigateToSubmitOvertimeRequest = true },
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.Transparent
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        brush = Brush.linearGradient(
                                                            colors = listOf(AppColors.blue500, AppColors.teal500),
                                                            start = Offset(0f, 0f),
                                                            end = Offset(250f, 0f)
                                                        )
                                                    )
                                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = "Add",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        text = "Submit Request",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    // Overtime Requests Directory header moved below the button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = "Overtime Requests Directory",
                                            tint = AppColors.blue500
                                        )
                                        Column {
                                            Text(
                                                text = "Overtime Requests Directory",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.gray800
                                            )
                                            Text(
                                                text = "Track hours, status, and approvals for overtime",
                                                fontSize = 14.sp,
                                                color = AppColors.gray500
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Display overtime requests
                                    if (isLoadingOvertimeRequests) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = AppColors.blue500,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    } else if (overtimeRequestsError != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AppColors.redLight)
                                                .padding(16.dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Error,
                                                    contentDescription = "Error",
                                                    tint = AppColors.red,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "Error Loading Requests",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AppColors.red
                                                )
                                                Text(
                                                    text = overtimeRequestsError ?: "Unknown error occurred",
                                                    fontSize = 14.sp,
                                                    color = AppColors.red.copy(alpha = 0.8f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    } else if (overtimeRequests.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = "No Requests",
                                                    tint = AppColors.gray400,
                                                    modifier = Modifier.size(48.dp)
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    text = "No Records Found",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AppColors.gray800
                                                )
                                                Text(
                                                    text = "You haven't submitted any overtime requests yet",
                                                    fontSize = 14.sp,
                                                    color = AppColors.gray500,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    } else {
                                        // Display overtime requests
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            overtimeRequests.forEachIndexed { index, request ->
                                                OvertimeRequestItem(
                                                    requestDate = request.requestDate,
                                                    hours = request.hours,
                                                    reason = request.reason,
                                                    status = request.status ?: "PENDING"
                                                )
                                                
                                                if (index < overtimeRequests.size - 1) {
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(vertical = 8.dp),
                                                        color = AppColors.gray200
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            // Spacer to ensure content doesn't get cut off at the bottom
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
                
                // Snackbar host for displaying messages
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    Snackbar(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        action = {
                            Text(
                                text = "OK",
                                color = AppColors.blue100,
                                modifier = Modifier.clickable { snackbarHostState.currentSnackbarData?.dismiss() }
                            )
                        }
                    ) {
                        Text(it.visuals.message)
                    }
                }
            }
        }
    }
}

@Composable
fun OvertimeRequestItem(
    requestDate: String,
    hours: Double,
    reason: String,
    status: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                // Overtime Hours and Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${hours.toString()} Hours",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.blue700
                    )
                    
                    // Status badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = getStatusBackgroundColor(status),
                        border = BorderStroke(1.dp, getStatusColor(status).copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = status,
                                tint = getStatusColor(status),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = status.uppercase(),
                                fontSize = 12.sp,
                                color = getStatusColor(status),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date
                Text(
                    text = "Date: ${formatDate(requestDate)}",
                    fontSize = 14.sp,
                    color = AppColors.gray600
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Reason
                Text(
                    text = "Reason: $reason",
                    fontSize = 14.sp,
                    color = AppColors.gray700,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OvertimeRequestsScreenPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.gray50
    ) {
        OvertimeRequestsScreen()
    }
}

