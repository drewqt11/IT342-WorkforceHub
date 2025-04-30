package com.example.myapplication.presentation.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import com.example.myapplication.presentation.components.AppScreen
import com.example.myapplication.presentation.components.UniversalDrawer
import com.example.myapplication.presentation.theme.AppColors

@Composable
fun TimeAttendanceScreen(
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToLeaveRequests: () -> Unit = {},
    onNavigateToPerformance: () -> Unit = {},
    onNavigateToTraining: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // State for drawer
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // For scrolling content
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.gray50
    ) {
        // Using the Universal Drawer instead of directly using ModalNavigationDrawer
        UniversalDrawer(
            drawerState = drawerState,
            currentScreen = AppScreen.TIME_ATTENDANCE,
            onLogout = onLogout,
            onNavigateToDashboard = onBack, // Navigate back to dashboard
            onNavigateToAttendance = {}, // Already on attendance screen
            onNavigateToLeaveRequests = onNavigateToLeaveRequests,
            onNavigateToPerformance = onNavigateToPerformance,
            onNavigateToTraining = onNavigateToTraining,
            onNavigateToProfile = onNavigateToProfile
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background grid pattern
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.05f)
                ) {
                    val gridSpacing = 50f
                    val strokeWidth = 1f
                    
                    for (i in 0..(size.width / gridSpacing).toInt()) {
                        drawLine(
                            color = AppColors.blue300,
                            start = Offset(i * gridSpacing, 0f),
                            end = Offset(i * gridSpacing, size.height),
                            strokeWidth = strokeWidth
                        )
                    }
                    
                    for (i in 0..(size.height / gridSpacing).toInt()) {
                        drawLine(
                            color = AppColors.blue300,
                            start = Offset(0f, i * gridSpacing),
                            end = Offset(size.width, i * gridSpacing),
                            strokeWidth = strokeWidth
                        )
                    }
                }
                
                // Scrollable content
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Content that scrolls underneath the header
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 220.dp)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Time clock card
                            TimeClockCard()
                            
                            // Attendance history card 
                            AttendanceHistoryCard()

                            // Bottom spacing
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                    
                    // Fixed header on top (doesn't scroll)
                    TimeAttendanceHeader(
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
}

@Composable
fun TimeAttendanceHeader(
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
            .height(220.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(AppColors.blue500, AppColors.teal500),
                    startX = 0f,
                    endX = 1200f
                )
            )
    ) {
        // Decorative circles
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.15f)
        ) {
            // Large circle
            drawCircle(
                color = Color.White,
                center = Offset(size.width * 0.8f, size.height * 0.2f),
                radius = size.width * 0.3f
            )

            // Small circle
            drawCircle(
                color = Color.White,
                center = Offset(size.width * 0.2f, size.height * 0.7f),
                radius = size.width * 0.1f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 19.dp)
        ) {
            // Menu button at top left
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0x22FFFFFF), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_sort_by_size),
                        contentDescription = "Menu",
                        tint = AppColors.white,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // User profile section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // User avatar
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .shadow(4.dp, CircleShape)
                        .background(AppColors.white, CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PP",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.blue700
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // User info
                Column {
                    // Welcome message
                    Text(
                        text = "Welcome back,",
                        color = AppColors.white.copy(alpha = 0.85f),
                        fontSize = 16.sp
                    )

                    Text(
                        text = "Full Name",
                        color = AppColors.white,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = "ID Number",
                        color = AppColors.white.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x22FFFFFF))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                    contentDescription = "Date",
                    tint = AppColors.white,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Get current date formatted nicely
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
                val formattedDate = today.format(formatter)

                Text(
                    text = formattedDate,
                    color = AppColors.white,
                    fontSize = 14.sp
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
            color = AppColors.white
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = AppColors.white.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun TimeClockCard() {
    Spacer(modifier = Modifier.width(8.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.white),
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
                            .background(AppColors.blue50, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,  // Placeholder icon
                            contentDescription = "Time Clock",
                            tint = AppColors.blue700,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Time Clock",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.gray800,
                        letterSpacing = 0.25.sp
                    )
                }
                
                // Status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppColors.greenLight)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Clocked In",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.green
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider(color = AppColors.gray200)
            
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
                        .background(AppColors.gray50)
                        .clickable { /* TODO: Clock in */ }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Clock In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.gray700
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Clock Out button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(2.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.blue500)
                        .clickable { /* TODO: Clock out */ }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Clock Out",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.white
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
            tint = AppColors.gray500,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = time,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.gray800
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = AppColors.gray500
        )
    }
}

@Composable
fun AttendanceHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.white),
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
                            .background(AppColors.teal50, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,  // Placeholder icon
                            contentDescription = "Attendance History",
                            tint = AppColors.teal700,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Recent Attendance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.gray800,
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
                        color = AppColors.blue500
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
                color = AppColors.gray200
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
                color = AppColors.gray200
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
                color = AppColors.gray800
            )
            
            // Status pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        when (status) {
                            "Approved" -> AppColors.greenLight
                            "Pending" -> AppColors.amberLight
                            else -> AppColors.gray100
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = status,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (status) {
                        "Approved" -> AppColors.green
                        "Pending" -> AppColors.amber
                        else -> AppColors.gray700
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
                    tint = AppColors.gray500,
                    modifier = Modifier.size(14.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "In: $clockIn",
                    fontSize = 14.sp,
                    color = AppColors.gray700
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Clock Out",
                    tint = AppColors.gray500,
                    modifier = Modifier.size(14.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "Out: $clockOut",
                    fontSize = 14.sp,
                    color = AppColors.gray700
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Hours Worked",
                    tint = AppColors.gray500,
                    modifier = Modifier.size(14.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = hoursWorked,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.blue700
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
        color = AppColors.gray50
    ) {
        TimeAttendanceScreen()
    }
}

