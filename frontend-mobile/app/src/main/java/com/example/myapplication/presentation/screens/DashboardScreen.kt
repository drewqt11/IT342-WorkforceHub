package com.example.myapplication.presentation.screens

import android.R
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun DashboardScreen(
    onLogout: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToLeaveRequests: () -> Unit = {},
    onNavigateToPerformance: () -> Unit = {},
    onNavigateToTraining: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // For animated decorative elements
    val infiniteTransition = rememberInfiniteTransition(label = "circle_animation")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha_animation"
    )
    
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000),
            repeatMode = RepeatMode.Restart
        ), label = "rotation_animation"
    )
    
    // State for drawer
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Scroll state for the content
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.gray50
    ) {
        // Using the Universal Drawer instead of directly using ModalNavigationDrawer
        UniversalDrawer(
            drawerState = drawerState,
            currentScreen = AppScreen.DASHBOARD,
            onLogout = onLogout,
            onNavigateToDashboard = {}, // Already on dashboard, no need to navigate
            onNavigateToAttendance = onNavigateToAttendance,
            onNavigateToLeaveRequests = onNavigateToLeaveRequests,
            onNavigateToPerformance = onNavigateToPerformance,
            onNavigateToTraining = onNavigateToTraining,
            onNavigateToProfile = onNavigateToProfile
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Decorative background elements
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.05f)
                ) {
                    // Background grid pattern
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
                
                // Animated circle elements
                Canvas(
                    modifier = Modifier
                        .size(300.dp)
                        .align(Alignment.Center)
                        .alpha(0.03f)
                ) {
                    // Outer circle
                    drawCircle(
                        color = AppColors.blue500,
                        radius = size.width / 2,
                        style = Stroke(width = 2f)
                    )
                    
                    // Middle circle with rotation
                    withTransform({
                        rotate(rotationAngle, pivot = Offset(size.width / 2, size.height / 2))
                    }) {
                        drawCircle(
                            color = AppColors.teal500,
                            radius = size.width / 3,
                            style = Stroke(width = 1.5f)
                        )
                    }
                    
                    // Inner circle with opposite rotation
                    withTransform({
                        rotate(-rotationAngle, pivot = Offset(size.width / 2, size.height / 2))
                    }) {
                        drawCircle(
                            color = AppColors.blue700,
                            radius = size.width / 4,
                            style = Stroke(width = 1f)
                        )
                    }
                }
                
                // Scrollable content (cards but not header)
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Content that scrolls underneath the header
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 220.dp) // Increased from 190.dp to move cards further down
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Various cards for different functionality
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Attendance card
                            AttendanceCard()
                            
                            // Leave requests card
                            LeaveRequestsCard()
    
                            // Performance metrics card
                            PerformanceMetricsCard()
    
                            // Upcoming training card
                            UpcomingTrainingCard()
                            
                            // Bottom spacing
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                    
                    // Fixed header on top (doesn't scroll)
                    DashboardHeader(
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
fun DashboardHeader(
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
                        .clip(CircleShape)
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
fun AttendanceCard() {
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
                    // Small icon for attendance
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(AppColors.blue50, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_recent_history),
                            contentDescription = "Attendance",
                            tint = AppColors.blue700,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Today's Attendance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.gray800,
                        letterSpacing = 0.25.sp
                    )
                }

                // Current date
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("MMM d")
                val formattedDate = today.format(formatter)

                Text(
                    text = formattedDate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.gray500
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                AppColors.gray200,
                                AppColors.blue100,
                                AppColors.gray200
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AttendanceTimeItem(
                    label = "Clock In",
                    time = "08:30 AM",
                    backgroundColor = AppColors.blue50,
                    backgroundStrokeColor = AppColors.blue300,
                    textColor = AppColors.blue700,
                    iconRes = R.drawable.ic_menu_recent_history
                )

                AttendanceTimeItem(
                    label = "Clock Out",
                    time = "05:30 PM",
                    backgroundColor = AppColors.teal50,
                    backgroundStrokeColor = AppColors.teal300,
                    textColor = AppColors.teal700,
                    iconRes = R.drawable.ic_menu_close_clear_cancel
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Work progress section
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Progress",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.gray700
                    )

                    Text(
                        text = "9h 00m / 8h 00m",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.blue700
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = 1.125f, // 9h / 8h = 1.125
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AppColors.blue500,
                    trackColor = AppColors.blue100
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Overtime indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AppColors.green, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "1h 00m overtime",
                        fontSize = 12.sp,
                        color = AppColors.green
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceTimeItem(
    label: String,
    time: String,
    backgroundColor: Color,
    backgroundStrokeColor: Color,
    textColor: Color,
    iconRes: Int
) {
    Box(
        modifier = Modifier
            .size(width = 160.dp, height = 90.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(1.dp) // Space for stroke
    ) {
        // Border effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(backgroundStrokeColor.copy(alpha = 0.5f), backgroundColor),
                        radius = 400f
                    ),
                    shape = RoundedCornerShape(15.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Label with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = label,
                        tint = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = time,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun LeaveRequestsCard() {
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
                    // Small icon for leave
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(AppColors.teal50, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                            contentDescription = "Leave Requests",
                            tint = AppColors.teal700,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Leave Requests",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.gray800,
                        letterSpacing = 0.25.sp
                    )
                }

                // Leave balance indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.blue50)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "15 days left",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.blue700
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LeaveRequestItem(
                type = "Annual Leave",
                status = "Approved",
                date = "May 10-15, 2024",
                days = "5 days",
                statusColor = AppColors.green
            )

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = AppColors.gray200
            )

            LeaveRequestItem(
                type = "Sick Leave",
                status = "Pending",
                date = "April 28, 2024",
                days = "1 day",
                statusColor = AppColors.amber
            )

            Spacer(modifier = Modifier.height(12.dp))

            // "Apply for leave" button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.teal50)
                    .clickable { /* TODO: Navigate to leave application */ }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Apply for Leave",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.teal700
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_add),
                        contentDescription = "Apply",
                        tint = AppColors.teal700,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LeaveRequestItem(
    type: String,
    status: String,
    date: String,
    days: String,
    statusColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = type,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.gray800
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Days indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AppColors.gray100)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = days,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.gray700
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                    contentDescription = "Date",
                    tint = AppColors.gray500,
                    modifier = Modifier.size(12.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = date,
                    fontSize = 12.sp,
                    color = AppColors.gray500
                )
            }
        }

        Box(
            modifier = Modifier
                .background(
                    color = when (status) {
                        "Approved" -> AppColors.greenLight
                        "Pending" -> AppColors.amberLight
                        "Rejected" -> AppColors.redLight
                        else -> AppColors.gray100
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = status,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = statusColor
            )
        }
    }
}

@Composable
fun UpcomingTrainingCard() {
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
                    // Small icon for training
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(AppColors.blue50, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_edit),
                            contentDescription = "Trainings",
                            tint = AppColors.blue700,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Upcoming Trainings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.gray800,
                        letterSpacing = 0.25.sp
                    )
                }

                // Training completion indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AppColors.blue500, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "3 courses",
                        fontSize = 12.sp,
                        color = AppColors.gray500
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Featured training card with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                // Background with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(AppColors.blue500, AppColors.blue700),
                                start = Offset(0f, 0f),
                                end = Offset(800f, 0f)
                            )
                        )
                        .padding(20.dp)
                ) {
                    // Decorative elements
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .alpha(0.1f)
                    ) {
                        // Various decorative shapes
                        drawCircle(
                            color = AppColors.white,
                            radius = 30.dp.toPx(),
                            center = Offset(size.width * 0.85f, size.height * 0.3f),
                            style = Stroke(width = 2f)
                        )

                        drawCircle(
                            color = AppColors.white,
                            radius = 15.dp.toPx(),
                            center = Offset(size.width * 0.1f, size.height * 0.8f),
                            style = Stroke(width = 1.5f)
                        )
                    }

                    Column {
                        Text(
                            text = "Cloud Computing Fundamentals",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.white
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                                contentDescription = "Date",
                                tint = AppColors.white.copy(alpha = 0.9f),
                                modifier = Modifier.size(14.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "May 5, 2024 • 10:00 AM",
                                fontSize = 14.sp,
                                color = AppColors.white.copy(alpha = 0.9f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_directions),
                                contentDescription = "Location",
                                tint = AppColors.white.copy(alpha = 0.9f),
                                modifier = Modifier.size(14.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Virtual Meeting (Zoom)",
                                fontSize = 14.sp,
                                color = AppColors.white.copy(alpha = 0.9f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Join button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33FFFFFF))
                                .clickable { /* TODO: Join meeting */ }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_call),
                                    contentDescription = "Join",
                                    tint = AppColors.white,
                                    modifier = Modifier.size(14.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Join Meeting",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.white
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // "View all trainings" button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.blue50)
                    .clickable { /* TODO: Navigate to all trainings */ }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "View All Trainings",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.blue700
                )
            }
        }
    }
}

@Composable
fun PerformanceMetricsCard() {
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
                    // Small icon for performance
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(AppColors.blue50, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_sort_by_size),
                            contentDescription = "Performance",
                            tint = AppColors.blue700,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Performance Metrics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.gray800,
                        letterSpacing = 0.25.sp
                    )
                }

                // Time period selector (dropdown)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.gray100)
                        .clickable { /* TODO: Show time period options */ }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "This Month",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.gray700
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Icon(
                            painter = painterResource(id = android.R.drawable.arrow_down_float),
                            contentDescription = "Select Period",
                            tint = AppColors.gray700,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PerformanceMetricItem(
                    label = "Tasks Completed",
                    value = "24",
                    change = "+3",
                    isPositive = true,
                    color = AppColors.blue500,
                    backgroundColor = AppColors.blue50
                )

                PerformanceMetricItem(
                    label = "Projects",
                    value = "3",
                    change = "0",
                    isPositive = true,
                    color = AppColors.teal500,
                    backgroundColor = AppColors.teal50
                )

                PerformanceMetricItem(
                    label = "Rating",
                    value = "4.8",
                    change = "+0.2",
                    isPositive = true,
                    color = AppColors.blue700,
                    backgroundColor = AppColors.blue50
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Performance review note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.blue50)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_dialog_info),
                    contentDescription = "Info",
                    tint = AppColors.blue700,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Next performance review: May 15, 2024",
                    fontSize = 12.sp,
                    color = AppColors.blue700
                )
            }
        }
    }
}

@Composable
fun PerformanceMetricItem(
    label: String,
    value: String,
    change: String,
    isPositive: Boolean,
    color: Color,
    backgroundColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(12.dp)
            .width(100.dp)
    ) {
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            color = AppColors.gray700,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Change indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isPositive) AppColors.greenLight else AppColors.redLight
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                painter = painterResource(
                    id = if (isPositive) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float
                ),
                contentDescription = "Change",
                tint = if (isPositive) AppColors.green else AppColors.red,
                modifier = Modifier.size(10.dp)
            )

            Spacer(modifier = Modifier.width(2.dp))

            Text(
                text = change,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isPositive) AppColors.green else AppColors.red
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.gray50
    ) {
        DashboardScreen(
            onLogout = {},
            onNavigateToAttendance = {},
            onNavigateToLeaveRequests = {},
            onNavigateToPerformance = {},
            onNavigateToTraining = {},
            onNavigateToProfile = {}
        )
    }
} 