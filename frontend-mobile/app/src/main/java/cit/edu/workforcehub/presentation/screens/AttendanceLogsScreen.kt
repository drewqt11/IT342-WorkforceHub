package cit.edu.workforcehub.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import cit.edu.workforcehub.api.models.AttendanceRecord
import cit.edu.workforcehub.presentation.components.*
import cit.edu.workforcehub.presentation.theme.AppColors
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.LocalTime
import java.time.format.DateTimeParseException
import java.time.LocalDateTime

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
 * Formats a time string from 24-hour format (HH:mm:ss) to 12-hour format (h:mm a)
 */
private fun formatTime(timeString: String?): String {
    if (timeString == null) return "-"
    return try {
        val time = if (timeString.contains("T")) {
            // Handle ISO datetime format
            LocalDateTime.parse(timeString).toLocalTime()
        } else {
            // Handle time-only format
            LocalTime.parse(timeString)
        }
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        time.format(formatter)
    } catch (e: DateTimeParseException) {
        timeString
    }
}

@Composable
fun TimeAttendanceScreen(
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
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // State for profile data
    var profileData by remember { mutableStateOf<EmployeeProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // State for attendance records
    var attendanceRecords by remember { mutableStateOf<List<AttendanceRecord>>(emptyList()) }
    var isLoadingAttendance by remember { mutableStateOf(true) }
    var attendanceError by remember { mutableStateOf<String?>(null) }
    
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
    
    // Fetch attendance records
    LaunchedEffect(key1 = true) {
        try {
            val employeeService = ApiHelper.getEmployeeService()
            val response = employeeService.getAllAttendanceRecords()
            
            if (response.isSuccessful && response.body() != null) {
                attendanceRecords = response.body()!!
                isLoadingAttendance = false
            } else {
                attendanceError = "Failed to load attendance records: ${response.message()}"
                isLoadingAttendance = false
            }
        } catch (e: Exception) {
            attendanceError = "Error loading attendance records: ${e.message}"
            isLoadingAttendance = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.gray100
    ) {
        UniversalDrawer(
            drawerState = drawerState,
            currentScreen = AppScreen.TIME_ATTENDANCE,
            onLogout = onLogout,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToAttendance = {},
            onNavigateToLeaveRequests = onNavigateToLeaveRequests,
            onNavigateToOvertimeRequests = onNavigateToOvertimeRequests,
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
                            // Add Attendance Logs header at the top of the scrollable content
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 2.dp, top = 8.dp, bottom = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Attendance icon in circular blue background
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
                                        painter = painterResource(id = R.drawable.clock),
                                        contentDescription = "Attendance Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                // Attendance Logs text
                                Text(
                                    text = "Attendance Logs",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.gray800
                                )
                            }

                            // Main Attendance Records Card
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = "Attendance Directory",
                                                tint = AppColors.blue500
                                            )
                                            Column {
                                                Text(
                                                    text = "Attendance Directory",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AppColors.gray800
                                                )
                                                Text(
                                                    text = "Track attendance, hours, and status",
                                                    fontSize = 14.sp,
                                                    color = AppColors.gray500
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Display attendance records
                                    if (isLoadingAttendance) {
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
                                    } else if (attendanceError != null) {
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
                                                    text = "Error Loading Records",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AppColors.red
                                                )
                                                Text(
                                                    text = attendanceError ?: "Unknown error occurred",
                                                    fontSize = 14.sp,
                                                    color = AppColors.red.copy(alpha = 0.8f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    } else if (attendanceRecords.isEmpty()) {
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
                                                    contentDescription = "No Records",
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
                                                    text = "You don't have any attendance records yet",
                                                    fontSize = 14.sp,
                                                    color = AppColors.gray500,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    } else {
                                        // Display records
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Filter out records where clockOutTime is null
                                            val completedRecords = attendanceRecords.filter { record ->
                                                record.clockOutTime != null
                                            }

                                            if (completedRecords.isEmpty()) {
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
                                                            contentDescription = "No Records",
                                                            tint = AppColors.gray400,
                                                            modifier = Modifier.size(48.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        Text(
                                                            text = "No Completed Records",
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = AppColors.gray800
                                                        )
                                                        Text(
                                                            text = "You don't have any completed attendance records yet",
                                                            fontSize = 14.sp,
                                                            color = AppColors.gray500,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                            } else {
                                                completedRecords.forEach { record ->
                                                AttendanceRecord(
                                                    date = record.date,
                                                    clockIn = record.clockInTime,
                                                        clockOut = record.clockOutTime,
                                                        remarks = record.remarks,
                                                        status = record.status
                                                )
                                                
                                                    if (record != completedRecords.last()) {
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
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceRecord(
    date: String,
    clockIn: String?,
    clockOut: String?,
    remarks: String? = null,
    status: String? = null
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
                Text(
                    text = formatDate(date),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.gray800
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clock in badge and label
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppColors.blue50,
                            border = BorderStroke(1.dp, AppColors.blue100),
                            modifier = Modifier.width(100.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Login,
                                    contentDescription = "Clock In",
                                    tint = AppColors.blue500,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formatTime(clockIn),
                                    fontSize = 12.sp,
                                    color = AppColors.blue500,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Clocked in",
                            fontSize = 12.sp,
                            color = AppColors.gray900
                        )
                    }
                    
                    // Clock out badge and label
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppColors.blue50,
                            border = BorderStroke(1.dp, AppColors.blue100),
                            modifier = Modifier.width(100.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Clock Out",
                                    tint = AppColors.blue500,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formatTime(clockOut),
                                    fontSize = 12.sp,
                                    color = AppColors.blue500,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Clocked out",
                            fontSize = 12.sp,
                            color = AppColors.gray900
                        )
                    }
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Vertical Divider
                VerticalDivider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(2.dp)
                        .padding(vertical = 2.dp),
                    color = AppColors.teal500
                )
                
                // Status badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AppColors.greenLight,
                    border = BorderStroke(1.dp, AppColors.green.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = remarks ?: status ?: "Status",
                            tint = AppColors.green,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = remarks?.uppercase() ?: status?.uppercase() ?: "PRESENT",
                            fontSize = 12.sp,
                            color = AppColors.green,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimeAttendanceScreenPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.gray50
    ) {
        TimeAttendanceScreen()
    }
}

