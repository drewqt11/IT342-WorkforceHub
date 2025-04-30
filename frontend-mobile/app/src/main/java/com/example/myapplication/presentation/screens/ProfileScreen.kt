package com.example.myapplication.presentation.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.R
import com.example.myapplication.api.ApiHelper
import com.example.myapplication.api.models.EmployeeProfile
import com.example.myapplication.presentation.components.AppScreen
import com.example.myapplication.presentation.components.UniversalDrawer
import com.example.myapplication.presentation.theme.AppColors
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToLeaveRequests: () -> Unit = {},
    onNavigateToPerformance: () -> Unit = {},
    onNavigateToTraining: () -> Unit = {}
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
        color = AppColors.gray50
    ) {
        // Using the Universal Drawer instead of directly using ModalNavigationDrawer
        UniversalDrawer(
            drawerState = drawerState,
            currentScreen = AppScreen.PROFILE,
            onLogout = onLogout,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToAttendance = onNavigateToAttendance,
            onNavigateToLeaveRequests = onNavigateToLeaveRequests,
            onNavigateToPerformance = onNavigateToPerformance,
            onNavigateToTraining = onNavigateToTraining,
            onNavigateToProfile = {} // Already on profile, no need to navigate
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
                
                // New Profile Header
                ModernProfileHeader(
                    profile = profileData,
                    isLoading = isLoading,
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    onBackClick = onNavigateToDashboard,
                    modifier = Modifier.zIndex(1f)
                )
                
                // Scrollable content
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Content that scrolls underneath the header
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 260.dp) // Increased to accommodate larger header
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (isLoading) {
                                LoadingProfileCard()
                            } else if (error != null) {
                                ErrorProfileCard(error = error!!)
                            } else if (profileData != null) {
                                // Profile info cards
                                ProfileInfoCard(profile = profileData!!)

                                IdDetailsCard(profile = profileData!!)
                                
                                EmploymentDetailsCard(profile = profileData!!)
                            }
                            
                            // Bottom spacing
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernProfileHeader(
    profile: EmployeeProfile?,
    isLoading: Boolean,
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
            .height(220.dp)
    ) {
        // Background with gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(AppColors.blue500, AppColors.teal500),
                        startX = 0f,
                        endX = 1200f
                    )
                )
        )
        
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
        
        // Top navigation row with only menu button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Menu button on the left
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_sort_by_size),
                    contentDescription = "Menu",
                    tint = AppColors.white,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Empty spacer on right for balance
            Spacer(modifier = Modifier.size(40.dp))
        }
        
        // Profile information centered
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                // Loading avatar placeholder
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Loading text placeholders
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                )
            } else if (profile != null) {
                // Profile avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        .shadow(5.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${profile.firstName.firstOrNull() ?: ""}${profile.lastName.firstOrNull() ?: ""}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.blue700
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // User name
                Text(
                    text = "${profile.firstName} ${profile.lastName}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Job information
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val jobInfo = if (profile.jobName != null) {
                        profile.jobName
                    } else if (profile.roleName != null) {
                        profile.roleName
                    } else {
                        "Employee"
                    }
                    
                    Text(
                        text = jobInfo,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                    
                    if (profile.departmentName != null) {
                        Text(
                            text = " • ${profile.departmentName}",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Status badge (if applicable)
        if (profile != null && profile.employmentStatus != null) {
            val statusColor = when (profile.employmentStatus) {
                "ACTIVE" -> AppColors.green
                "PENDING" -> AppColors.amber
                "INACTIVE" -> AppColors.red
                else -> AppColors.gray500
            }
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 20.dp) // Overlap with content below
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White)
                    .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    Text(
                        text = profile.employmentStatus,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingProfileCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
                    .padding(16.dp),
                color = AppColors.blue500
            )
            Text(
                text = "Loading profile information...",
                fontSize = 16.sp,
                color = AppColors.gray700
            )
        }
    }
}

@Composable
fun ErrorProfileCard(error: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Error",
                tint = AppColors.red,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Unable to load profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.gray800
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                fontSize = 14.sp,
                color = AppColors.gray700,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProfileInfoCard(profile: EmployeeProfile) {
    Spacer(modifier = Modifier.width(8.dp))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Profile header with name and role
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Personal Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.gray800
                    )
                    
                    Text(
                        text = "Your basic profile details",
                        fontSize = 14.sp,
                        color = AppColors.gray500
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Full name
            ProfileDataRow(label = "Full Name", value = "${profile.firstName} ${profile.lastName}")
            
            // Email
            ProfileDataRow(label = "Email", value = profile.email)
            
            // Phone number
            ProfileDataRow(
                label = "Phone Number", 
                value = profile.phoneNumber ?: "Not provided"
            )
            
            // Gender
            ProfileDataRow(
                label = "Gender", 
                value = profile.gender ?: "Not provided"
            )
            
            // Date of Birth
            ProfileDataRow(
                label = "Date of Birth", 
                value = profile.dateOfBirth ?: "Not provided"
            )
            
            // Address
            ProfileDataRow(
                label = "Address", 
                value = profile.address ?: "Not provided"
            )
            
            // Marital Status
            ProfileDataRow(
                label = "Marital Status", 
                value = profile.maritalStatus ?: "Not provided"
            )
        }
    }
}

@Composable
fun IdDetailsCard(profile: EmployeeProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Identification",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.gray800
                    )

                    Text(
                        text = "Your system identifiers",
                        fontSize = 14.sp,
                        color = AppColors.gray500
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Employee ID
            ProfileDataRow(label = "Employee ID", value = profile.employeeId)

            // ID Number
            ProfileDataRow(
                label = "ID Number",
                value = profile.idNumber ?: "Not provided"
            )

            // Account Created
            ProfileDataRow(
                label = "Account Created",
                value = profile.createdAt?.let {
                    it.substring(0, it.indexOf('T'))
                } ?: "Not available"
            )
        }
    }
}

@Composable
fun EmploymentDetailsCard(profile: EmployeeProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Employment Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.gray800
                    )
                    
                    Text(
                        text = "Your work-related information",
                        fontSize = 14.sp,
                        color = AppColors.gray500
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Department
            ProfileDataRow(
                label = "Department", 
                value = profile.departmentName ?: "Not assigned"
            )
            
            // Job Title
            ProfileDataRow(
                label = "Job Title", 
                value = profile.jobName ?: "Not assigned"
            )
            
            // Role
            ProfileDataRow(
                label = "Role", 
                value = profile.roleName ?: "Not assigned"
            )
            
            // Hire Date
            ProfileDataRow(
                label = "Hire Date", 
                value = profile.hireDate ?: "Not provided"
            )
        }
    }
}

@Composable
fun ProfileDataRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = AppColors.gray700
            )
            
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.gray800
            )
        }
        
        Divider(
            color = AppColors.gray200
        )
    }
} 