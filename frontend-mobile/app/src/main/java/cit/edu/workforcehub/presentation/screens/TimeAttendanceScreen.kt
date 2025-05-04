package cit.edu.workforcehub.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import cit.edu.workforcehub.api.ApiHelper
import cit.edu.workforcehub.api.models.EmployeeProfile
import cit.edu.workforcehub.presentation.components.AppHeader
import cit.edu.workforcehub.presentation.components.AppScreen
import cit.edu.workforcehub.presentation.components.UniversalDrawer
import cit.edu.workforcehub.presentation.components.LoadingComponent
import cit.edu.workforcehub.presentation.theme.AppColors

@Composable
fun TimeAttendanceScreen(
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
            currentScreen = AppScreen.TIME_ATTENDANCE,
            onLogout = onLogout,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToAttendance = {}, // Already on attendance, no need to navigate
            onNavigateToLeaveRequests = onNavigateToLeaveRequests,
            onNavigateToPerformance = onNavigateToPerformance,
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
                    title = "Time & Attendance",
                    profileData = profileData,
                    isLoading = isLoading,
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    modifier = Modifier.zIndex(1f) // Ensure header stays on top
                )
            }
        }
    }
}

@Composable
fun QuickStatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = cit.edu.workforcehub.presentation.theme.AppColors.white
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = cit.edu.workforcehub.presentation.theme.AppColors.white.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun TimeClockCard() {
    Spacer(modifier = Modifier.width(8.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cit.edu.workforcehub.presentation.theme.AppColors.white),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(cit.edu.workforcehub.presentation.theme.AppColors.blue50, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,  // Placeholder icon
                            contentDescription = "Time Clock",
                            tint = cit.edu.workforcehub.presentation.theme.AppColors.blue700,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Time Clock",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = cit.edu.workforcehub.presentation.theme.AppColors.gray800,
                        letterSpacing = 0.25.sp
                    )
                }
                
                // Status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(cit.edu.workforcehub.presentation.theme.AppColors.greenLight)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Clocked In",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cit.edu.workforcehub.presentation.theme.AppColors.green
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider(color = cit.edu.workforcehub.presentation.theme.AppColors.gray200)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Clock in/out buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Clock In button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(2.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(cit.edu.workforcehub.presentation.theme.AppColors.gray50)
                        .clickable { /* TODO: Clock in */ }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Clock In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = cit.edu.workforcehub.presentation.theme.AppColors.gray700
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Clock Out button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(2.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(cit.edu.workforcehub.presentation.theme.AppColors.blue500)
                        .clickable { /* TODO: Clock out */ }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Clock Out",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = cit.edu.workforcehub.presentation.theme.AppColors.white
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Clock info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ClockInfoItem(
                    label = "Clock In",
                    time = "08:30 AM",
                    iconVector = Icons.Filled.Info  // Placeholder icon
                )
                
                ClockInfoItem(
                    label = "Expected Out",
                    time = "05:30 PM",
                    iconVector = Icons.Filled.Info  // Placeholder icon 
                )
                
                ClockInfoItem(
                    label = "Hours Today",
                    time = "7h 45m",
                    iconVector = Icons.Filled.Refresh
                )
            }
        }
    }
}

@Composable
fun ClockInfoItem(
    label: String,
    time: String,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = label,
            tint = cit.edu.workforcehub.presentation.theme.AppColors.gray500,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = time,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = cit.edu.workforcehub.presentation.theme.AppColors.gray800
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = cit.edu.workforcehub.presentation.theme.AppColors.gray500
        )
    }
}

@Composable
fun AttendanceHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cit.edu.workforcehub.presentation.theme.AppColors.white),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(cit.edu.workforcehub.presentation.theme.AppColors.teal50, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,  // Placeholder icon
                            contentDescription = "Attendance History",
                            tint = cit.edu.workforcehub.presentation.theme.AppColors.teal700,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Recent Attendance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = cit.edu.workforcehub.presentation.theme.AppColors.gray800,
                        letterSpacing = 0.25.sp
                    )
                }
                
                // View all button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { /* TODO: View all */ }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "View All",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = cit.edu.workforcehub.presentation.theme.AppColors.blue500
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Attendance records
            AttendanceRecord(
                date = "Monday, May 1",
                clockIn = "08:32 AM",
                clockOut = "05:45 PM",
                hoursWorked = "9h 13m",
                status = "Approved"
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = cit.edu.workforcehub.presentation.theme.AppColors.gray200
            )
            
            AttendanceRecord(
                date = "Tuesday, May 2",
                clockIn = "08:28 AM",
                clockOut = "05:30 PM",
                hoursWorked = "9h 02m",
                status = "Approved"
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = cit.edu.workforcehub.presentation.theme.AppColors.gray200
            )
            
            AttendanceRecord(
                date = "Wednesday, May 3",
                clockIn = "08:45 AM",
                clockOut = "06:15 PM",
                hoursWorked = "9h 30m",
                status = "Pending"
            )
        }
    }
}

@Composable
fun AttendanceRecord(
    date: String,
    clockIn: String,
    clockOut: String,
    hoursWorked: String,
    status: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = cit.edu.workforcehub.presentation.theme.AppColors.gray800
            )
            
            // Status pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        when (status) {
                            "Approved" -> cit.edu.workforcehub.presentation.theme.AppColors.greenLight
                            "Pending" -> cit.edu.workforcehub.presentation.theme.AppColors.amberLight
                            else -> cit.edu.workforcehub.presentation.theme.AppColors.gray100
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = status,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (status) {
                        "Approved" -> cit.edu.workforcehub.presentation.theme.AppColors.green
                        "Pending" -> cit.edu.workforcehub.presentation.theme.AppColors.amber
                        else -> cit.edu.workforcehub.presentation.theme.AppColors.gray700
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,  // Placeholder icon
                    contentDescription = "Clock In",
                    tint = cit.edu.workforcehub.presentation.theme.AppColors.gray500,
                    modifier = Modifier.size(14.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "In: $clockIn",
                    fontSize = 14.sp,
                    color = cit.edu.workforcehub.presentation.theme.AppColors.gray700
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Clock Out",
                    tint = cit.edu.workforcehub.presentation.theme.AppColors.gray500,
                    modifier = Modifier.size(14.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "Out: $clockOut",
                    fontSize = 14.sp,
                    color = cit.edu.workforcehub.presentation.theme.AppColors.gray700
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Hours Worked",
                    tint = cit.edu.workforcehub.presentation.theme.AppColors.gray500,
                    modifier = Modifier.size(14.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = hoursWorked,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = cit.edu.workforcehub.presentation.theme.AppColors.blue700
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimeAttendanceScreenPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = cit.edu.workforcehub.presentation.theme.AppColors.gray50
    ) {
        TimeAttendanceScreen()
    }
}

