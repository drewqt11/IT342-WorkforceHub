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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
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
import cit.edu.workforcehub.presentation.screens.forms.OvertimeRequestForm
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

/**
 * Formats hours to remove decimal places when displaying whole numbers
 */
private fun formatHours(hours: Double): String {
    return if (hours == hours.toInt().toDouble()) {
        hours.toInt().toString()
    } else {
        hours.toString()
    }
}

// Add this formatting function for times
private fun formatTimeWith12Hour(timeString: String): String {
    try {
        // Parse the time string (expecting format like "09:00" or "17:00")
        val parts = timeString.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].substring(0, 2).toInt() // Take only the first 2 characters in case there are seconds
        
        // Convert to 12-hour format
        val hourIn12 = when (hour) {
            0 -> 12
            in 1..12 -> hour
            else -> hour - 12
        }
        
        // Determine AM/PM
        val amPm = if (hour < 12) "AM" else "PM"
        
        // Format the minute with leading zero if needed
        val minuteFormatted = if (minute < 10) "0$minute" else minute.toString()
        
        return "$hourIn12:$minuteFormatted $amPm"
    } catch (e: Exception) {
        // Return the original string if parsing fails
        return timeString
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
    
    // State for navigation and dialogs
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showOvertimeRequestForm by remember { mutableStateOf(false) }
    
    // State for cancel confirmation
    var showCancelConfirmationDialog by remember { mutableStateOf(false) }
    var selectedRequestToCancel by remember { mutableStateOf<OvertimeRequest?>(null) }
    var isCancelling by remember { mutableStateOf(false) }
    
    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Inside the OvertimeRequestsScreen function, add a state for the selected tab
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Pending", "Approved", "Rejected")

    // Filter requests based on the selected tab
    val filteredRequests = remember(overtimeRequests, selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> overtimeRequests.filter { (it.status ?: "").uppercase() == "PENDING" }
            1 -> overtimeRequests.filter { (it.status ?: "").uppercase() == "APPROVED" }
            2 -> overtimeRequests.filter { (it.status ?: "").uppercase() == "REJECTED" }
            else -> overtimeRequests
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

    // If showing the overtime request form, display it
    if (showOvertimeRequestForm) {
        OvertimeRequestForm(
            onBackPressed = { 
                showOvertimeRequestForm = false 
            },
            onSuccess = {
                // Refresh the overtime requests list and return to the main screen
                showOvertimeRequestForm = false
                isLoadingOvertimeRequests = true
                scope.launch {
                    try {
                        val employeeService = ApiHelper.getEmployeeService()
                        val response = employeeService.getOvertimeRequests()
                        
                        if (response.isSuccessful && response.body() != null) {
                            overtimeRequests = response.body()!!
                        } else {
                            snackbarHostState.showSnackbar("Failed to refresh overtime requests: ${response.message()}")
                        }
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error refreshing overtime requests: ${e.message}")
                    } finally {
                        isLoadingOvertimeRequests = false
                    }
                }
            }
        )
        return // Return early to show only the form
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
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Header section - Add Overtime Requests header
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
                                    .weight(1f),  // Make the card take remaining space
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
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {
                                    // Fixed content (Submit button, Directory header, Tabs)
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
                                                    .clickable { showConfirmationDialog = true },
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
                                    
                                        // Overtime Requests Directory header
                                        Column {
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

                                            // Status filter tabs
                                            TabRow(
                                                selectedTabIndex = selectedTabIndex,
                                                containerColor = Color.Transparent,
                                                contentColor = AppColors.blue500,
                                                indicator = { tabPositions ->
                                                    if (selectedTabIndex < tabPositions.size) {
                                                        Box(
                                                            Modifier
                                                                .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                                                .height(3.dp)
                                                                .background(
                                                                    brush = Brush.linearGradient(
                                                                        colors = listOf(AppColors.blue500, AppColors.teal500),
                                                                        start = Offset(0f, 0f),
                                                                        end = Offset(100f, 0f)
                                                                    ),
                                                                    shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                                                                )
                                                        )
                                                    }
                                                },
                                                divider = {
                                                    HorizontalDivider(
                                                        thickness = 1.dp,
                                                        color = AppColors.gray200
                                                    )
                                                }
                                            ) {
                                                tabTitles.forEachIndexed { index, title ->
                                                    Tab(
                                                        selected = selectedTabIndex == index,
                                                        onClick = { selectedTabIndex = index },
                                                        text = {
                                                            Text(
                                                                text = title,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                                                fontSize = 14.sp,
                                                                color = if (selectedTabIndex == index) AppColors.teal500 else AppColors.gray600
                                                            )
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    // THIS IS THE SCROLLABLE PART - Overtime requests list
                                    if (isLoadingOvertimeRequests) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
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
                                                .weight(1f)
                                                .padding(16.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AppColors.redLight),
                                            contentAlignment = Alignment.Center
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
                                    } else if (filteredRequests.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
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
                                                    text = when (selectedTabIndex) {
                                                        0 -> "You don't have any pending overtime requests"
                                                        1 -> "You don't have any approved overtime requests"
                                                        2 -> "You don't have any rejected overtime requests"
                                                        else -> "You haven't submitted any overtime requests yet"
                                                    },
                                                    fontSize = 14.sp,
                                                    color = AppColors.gray500,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    } else {
                                        // SCROLLABLE CONTENT - Overtime requests list
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .verticalScroll(scrollState)
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            filteredRequests.forEachIndexed { index, request ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .shadow(
                                                            elevation = 3.dp,
                                                            shape = RoundedCornerShape(16.dp),
                                                            spotColor = AppColors.blue700.copy(alpha = 0.15f)
                                                        )
                                                        .clip(RoundedCornerShape(16.dp)),
                                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                                    shape = RoundedCornerShape(16.dp),
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(
                                                                brush = Brush.linearGradient(
                                                                    colors = listOf(
                                                                        AppColors.white,
                                                                        AppColors.blue50.copy(alpha = 0.5f)
                                                                    ),
                                                                    start = Offset(0f, 0f),
                                                                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                                                )
                                                            )
                                                            .border(
                                                                width = 1.dp,
                                                                color = AppColors.gray200,
                                                                shape = RoundedCornerShape(16.dp)
                                                            )
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(16.dp)
                                                        ) {
                                                            // Top row with hours and status
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                // Hours with icon
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                ) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .size(32.dp)
                                                                            .clip(RoundedCornerShape(8.dp))
                                                                            .background(
                                                                                brush = Brush.linearGradient(
                                                                                    colors = listOf(
                                                                                        AppColors.blue500, 
                                                                                        AppColors.blue300
                                                                                    ),
                                                                                    start = Offset(0f, 0f),
                                                                                    end = Offset(32f, 32f)
                                                                                )
                                                                            ),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.CalendarMonth,
                                                                            contentDescription = "Date",
                                                                            tint = Color.White,
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                    }
                                                                    
                                                                    Spacer(modifier = Modifier.width(8.dp))
                                                                    
                                                                    Text(
                                                                        text = formatDate(request.date),
                                                                        fontSize = 16.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = AppColors.blue700
                                                                    )
                                                                }
                                                                
                                                                // Status badge
                                                                Surface(
                                                                    shape = RoundedCornerShape(20.dp),
                                                                    color = getStatusBackgroundColor(request.status ?: "PENDING"),
                                                                    border = BorderStroke(1.dp, getStatusColor(request.status ?: "PENDING").copy(alpha = 0.3f)),
                                                                    modifier = Modifier.shadow(
                                                                        elevation = 2.dp,
                                                                        spotColor = getStatusColor(request.status ?: "PENDING").copy(alpha = 0.2f),
                                                                        shape = RoundedCornerShape(20.dp)
                                                                    )
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.CheckCircle,
                                                                            contentDescription = request.status,
                                                                            tint = getStatusColor(request.status ?: "PENDING"),
                                                                            modifier = Modifier.size(14.dp)
                                                                        )
                                                                        Text(
                                                                            text = (request.status ?: "PENDING").uppercase(),
                                                                            fontSize = 13.sp,
                                                                            color = getStatusColor(request.status ?: "PENDING"),
                                                                            fontWeight = FontWeight.SemiBold
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                            
                                                            Spacer(modifier = Modifier.height(16.dp))
                                                            
                                                    HorizontalDivider(
                                                                color = AppColors.gray200.copy(alpha = 0.6f),
                                                                thickness = 1.dp
                                                            )
                                                            
                                                            Spacer(modifier = Modifier.height(12.dp))
                                                            
                                                            // Hours row
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier.padding(vertical = 2.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.AccessTime,
                                                                    contentDescription = "Hours",
                                                                    tint = AppColors.gray500,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Text(
                                                                    text = "Total overtime: ${formatHours(request.totalHours)} hours",
                                                                    fontSize = 14.sp,
                                                                    color = AppColors.gray700,
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                            }
                                                            
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            
                                                            // Reason row
                                                            Row(
                                                                verticalAlignment = Alignment.Top,
                                                                modifier = Modifier.padding(vertical = 2.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Info,
                                                                    contentDescription = "Reason",
                                                                    tint = AppColors.gray500,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Text(
                                                                    text = "Reason: ${request.reason}",
                                                                    fontSize = 14.sp,
                                                                    color = AppColors.gray700,
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                            }
                                                            
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            
                                                            // Time details
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(AppColors.blue50.copy(alpha = 0.5f))
                                                                    .padding(8.dp),
                                                                horizontalArrangement = Arrangement.SpaceEvenly
                                                            ) {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Text(
                                                                        text = "Start: ",
                                                                        fontSize = 13.sp,
                                                                        color = AppColors.gray600
                                                                    )
                                                                    Text(
                                                                        text = formatTimeWith12Hour(request.startTime),
                                                                        fontSize = 13.sp,
                                                                        color = AppColors.blue700,
                                                                        fontWeight = FontWeight.SemiBold
                                                                    )
                                                                }
                                                                
                                                                HorizontalDivider(
                                                                    modifier = Modifier
                                                                        .height(16.dp)
                                                                        .width(1.dp),
                                                                    color = AppColors.gray300
                                                                )
                                                                
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Text(
                                                                        text = "End: ",
                                                                        fontSize = 13.sp,
                                                                        color = AppColors.gray600
                                                                    )
                                                                    Text(
                                                                        text = formatTimeWith12Hour(request.endTime),
                                                                        fontSize = 13.sp,
                                                                        color = AppColors.blue700,
                                                                        fontWeight = FontWeight.SemiBold
                                                                    )
                                                                }
                                                            }
                                                            
                                                            // Add Cancel button for PENDING requests
                                                            if ((request.status ?: "").uppercase() == "PENDING") {
                                                                Spacer(modifier = Modifier.height(12.dp))
                                                                HorizontalDivider(
                                                                    color = AppColors.gray200.copy(alpha = 0.6f),
                                                                    thickness = 1.dp
                                                                )
                                                                Spacer(modifier = Modifier.height(12.dp))
                                                                
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth(),
                                                                    contentAlignment = Alignment.CenterEnd
                                                                ) {
                                                                    Surface(
                                                                        modifier = Modifier
                                                                            .shadow(
                                                                                elevation = 2.dp,
                                                                                shape = RoundedCornerShape(8.dp),
                                                                                spotColor = AppColors.red.copy(alpha = 0.3f)
                                                                            )
                                                                            .border(
                                                                                width = 1.dp,
                                                                                color = Color.White.copy(alpha = 0.6f),
                                                                                shape = RoundedCornerShape(8.dp)
                                                                            )
                                                                            .clickable { 
                                                                                selectedRequestToCancel = request
                                                                                showCancelConfirmationDialog = true
                                                                            },
                                                                        shape = RoundedCornerShape(8.dp),
                                                                        color = Color.Transparent
                                                                    ) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .width(100.dp)
                                                                                .background(
                                                                                    color = AppColors.red.copy(alpha = 0.8f)
                                                                                )
                                                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                                                            contentAlignment = Alignment.Center
                                                                        ) {
                                                                            Text(
                                                                                text = "Cancel",
                                                                                color = Color.White,
                                                                                fontWeight = FontWeight.Medium,
                                                                                fontSize = 12.sp
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                            
                                            // Add some bottom padding
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }
                            }
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
                
                // Confirmation Dialog
                if (showConfirmationDialog) {
                    AlertDialog(
                        onDismissRequest = { showConfirmationDialog = false },
                        title = { 
                            Text(
                                text = "Submit Overtime Request",
                                fontWeight = FontWeight.Bold
                            ) 
                        },
                        text = { 
                            Text(
                                text = "Do you want to proceed with submitting a new overtime request?",
                                fontSize = 16.sp
                            ) 
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showConfirmationDialog = false
                                    showOvertimeRequestForm = true
                                }
                            ) {
                                Text(
                                    text = "Proceed",
                                    color = AppColors.blue500,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showConfirmationDialog = false }
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = AppColors.gray600
                                )
                            }
                        },
                        containerColor = AppColors.white,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
                
                // Cancel Confirmation Dialog
                if (showCancelConfirmationDialog && selectedRequestToCancel != null) {
                    AlertDialog(
                        onDismissRequest = { 
                            if (!isCancelling) {
                                showCancelConfirmationDialog = false
                                selectedRequestToCancel = null
                            }
                        },
                        title = { 
                            Text(
                                text = "Cancel Request",
                                fontWeight = FontWeight.Bold,
                                color = AppColors.gray800
                            ) 
                        },
                        text = { 
            Column {
                    Text(
                                    text = "Are you sure you want to cancel this request?",
                        fontSize = 16.sp,
                                    color = AppColors.gray700
                                )
                                
                                if (isCancelling) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = AppColors.blue500,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                            text = "Cancelling...",
                                            fontSize = 14.sp,
                                            color = AppColors.blue500
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    // Get the request ID and call the cancel API
                                    val requestId = selectedRequestToCancel?.overtimeRequestId
                                    if (requestId != null) {
                                        isCancelling = true
                                        
                                        scope.launch {
                                            try {
                                                val employeeService = ApiHelper.getEmployeeService()
                                                val response = employeeService.cancelOvertimeRequest(requestId)
                                                
                                                if (response.isSuccessful) {
                                                    // On success, refresh the list of requests
                                                    val updatedListResponse = employeeService.getOvertimeRequests()
                                                    if (updatedListResponse.isSuccessful && updatedListResponse.body() != null) {
                                                        overtimeRequests = updatedListResponse.body()!!
                                                        snackbarHostState.showSnackbar("Request cancelled successfully")
                                                    }
                                                } else {
                                                    snackbarHostState.showSnackbar("Failed to cancel request: ${response.message()}")
                                                }
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar("Error cancelling request: ${e.message}")
                                            } finally {
                                                isCancelling = false
                                                showCancelConfirmationDialog = false
                                                selectedRequestToCancel = null
                                            }
                                        }
                                    }
                                },
                                enabled = !isCancelling
                            ) {
                Text(
                                    text = "Yes",
                                    color = if (!isCancelling) AppColors.red else AppColors.gray400,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { 
                                    if (!isCancelling) {
                                        showCancelConfirmationDialog = false
                                        selectedRequestToCancel = null
                                    }
                                },
                                enabled = !isCancelling
                            ) {
                Text(
                                    text = "No",
                                    color = if (!isCancelling) AppColors.gray600 else AppColors.gray400
                                )
                            }
                        },
                        containerColor = AppColors.white,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
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

