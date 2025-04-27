package com.example.myapplication.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.screens.AppColors
import com.example.myapplication.R

/**
 * SideBarMenu is a composable function that displays a navigation drawer menu
 * It can be used with ModalNavigationDrawer in DashboardScreen
 */
@Composable
fun SideBarMenu(
    onCloseDrawer: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToLeaveRequests: () -> Unit = {},
    onNavigateToPerformance: () -> Unit = {},
    onNavigateToTraining: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(AppColors.white)
    ) {
        // Drawer header with app logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 30.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_with_no_text),
                        contentDescription = "Workforce Hub Logo",
                        modifier = Modifier.size(60.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "WORKFORCE HUB",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color(0xFF1F2937)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "ENTERPRISE PORTAL",
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
        
        // Divider
        Divider(color = AppColors.gray200)
        
        // Navigation items
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            MenuNavItem(
                title = "Dashboard",
                icon = R.drawable.home_dashboard_icon,
                isSelected = true,
                onClick = { onCloseDrawer() }
            )
            
            MenuNavItem(
                title = "Time & Attendance",
                icon = R.drawable.time_attendance_icon,
                onClick = onNavigateToAttendance
            )
            
            MenuNavItem(
                title = "Leave Requests",
                icon = R.drawable.leave_requests_icon,
                onClick = onNavigateToLeaveRequests
            )
            
            MenuNavItem(
                title = "Performance",
                icon = R.drawable.performance_icon,
                onClick = onNavigateToPerformance
            )
            
            MenuNavItem(
                title = "Training & Events",
                icon = R.drawable.training_icon,
                onClick = onNavigateToTraining
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = AppColors.gray200
            )
            
            MenuNavItem(
                title = "Profile",
                icon = R.drawable.ic_single_account_24dp,
                onClick = onNavigateToProfile
            )
        }
        
        // Divider before logout
        Divider(color = AppColors.gray200)
        
        // Logout button with some padding
        Box(modifier = Modifier.padding(vertical = 8.dp)) {
            MenuNavItem(
                title = "Logout",
                icon = R.drawable.logout_icon,
                tint = AppColors.red,
                onClick = onLogout
            )
        }
    }
}

/**
 * MenuNavItem is a composable that represents a single navigation item in the menu
 */
@Composable
fun MenuNavItem(
    title: String,
    icon: Int,
    isSelected: Boolean = false,
    tint: Color = AppColors.gray700,
    onClick: () -> Unit
) {
    val background = if (isSelected) AppColors.blue50 else Color.Transparent
    val textColor = if (isSelected) AppColors.blue700 else tint
    val iconTint = if (isSelected) AppColors.blue700 else tint
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(background)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
} 