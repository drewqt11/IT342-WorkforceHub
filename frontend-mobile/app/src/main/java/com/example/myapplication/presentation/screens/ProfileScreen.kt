package com.example.myapplication.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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

// Define missing colors if they don't exist in the AppColors
object ExtendedAppColors {
    val gray600 = Color(0xFF6C757D)  // Standard Bootstrap gray-600
}

@Preview(showBackground = true)
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
        color = Color(0xFFF5F7FA) // Lighter background for more modern feel
    ) {
        // Using the Universal Drawer
        UniversalDrawer(
            drawerState = drawerState,
            currentScreen = AppScreen.PROFILE,
            onLogout = onLogout,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToAttendance = onNavigateToAttendance,
            onNavigateToLeaveRequests = onNavigateToLeaveRequests,
            onNavigateToPerformance = onNavigateToPerformance,
            onNavigateToTraining = onNavigateToTraining,
            onNavigateToProfile = {} // Already on profile
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Header with gradient 
                ProfileHeader(
                    profile = profileData,
                    isLoading = isLoading,
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
                
                // Content
                Spacer(modifier = Modifier.height(70.dp)) // Add space for the profile card overlap
                
                // Profile content
                AnimatedVisibility(
                    visible = !isLoading && error == null && profileData != null,
                    enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (profileData != null) {
                            // Main Information sections
                            PersonalInfoCard(profile = profileData!!)
                            WorkInfoCard(profile = profileData!!)
                            AccountInfoCard(profile = profileData!!)
                        }
                    }
                }
                
                // Loading and error states
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingCard()
                    }
                } else if (error != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorCard(error = error!!)
                    }
                }
                
                // Bottom spacing
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ProfileHeader(
    profile: EmployeeProfile?,
    isLoading: Boolean,
    onMenuClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Reduced height
    ) {
        // Header background with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp) // Reduced height
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF3B82F6),  // Blue 500
                            Color(0xFF2563EB)   // Darker blue for contrast
                        )
                    )
                )
        ) {
            // Decorative elements
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.1f)
            ) {
                // Subtle pattern
                drawCircle(
                    color = Color.White,
                    center = Offset(size.width * 0.85f, size.height * 0.3f),
                    radius = size.width * 0.2f
                )
                
                drawCircle(
                    color = Color.White,
                    center = Offset(size.width * 0.15f, size.height * 0.6f),
                    radius = size.width * 0.1f
                )
            }
            
            // Menu button and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Menu button
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }
                
                // Title
                Text(
                    text = "Profile",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Empty spacer to balance layout
                Spacer(modifier = Modifier.size(40.dp))
            }
        }
        
        // Profile card with photo
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 100.dp), // Position to overlap properly
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AppColors.blue500,
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else if (profile != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile photo
                    Box(
                        modifier = Modifier
                            .size(80.dp) // Slightly smaller
                            .clip(CircleShape)
                            .border(2.dp, AppColors.blue500, CircleShape)
                            .background(AppColors.gray800),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${profile.firstName.firstOrNull() ?: ""}${profile.lastName.firstOrNull() ?: ""}",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Name
                    Text(
                        text = "${profile.firstName} ${profile.lastName}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.gray800
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Role
                    Text(
                        text = profile.roleName ?: "Employee",
                        fontSize = 16.sp,
                        color = ExtendedAppColors.gray600
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Status indicator with improved style
                    val statusColor = when (profile.employmentStatus) {
                        "ACTIVE" -> AppColors.green
                        "PENDING" -> AppColors.amber
                        "INACTIVE" -> AppColors.red
                        else -> AppColors.gray500
                    }
                    
                    val statusText = when (profile.employmentStatus) {
                        "ACTIVE" -> "Active"
                        "PENDING" -> "Pending"
                        "INACTIVE" -> "Inactive"
                        else -> "Unknown"
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusColor.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        Text(
                            text = statusText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalInfoCard(profile: EmployeeProfile) {
    ProfileSectionCard(
        title = "Personal Information",
        icon = Icons.Default.Person
    ) {
        ProfileDetailItem(
            label = "Full Name",
            value = "${profile.firstName} ${profile.lastName}",
            icon = Icons.Default.Person
        )
        
        ProfileDetailItem(
            label = "Email",
            value = profile.email,
            icon = Icons.Default.Email
        )
        
        ProfileDetailItem(
            label = "Phone",
            value = profile.phoneNumber ?: "Not provided",
            icon = Icons.Default.Phone
        )
        
        ProfileDetailItem(
            label = "Address",
            value = profile.address ?: "Not provided",
            icon = Icons.Default.LocationOn
        )
    }
}

@Composable
fun WorkInfoCard(profile: EmployeeProfile) {
    ProfileSectionCard(
        title = "Work Information",
        icon = Icons.Default.AccountCircle
    ) {
        ProfileDetailItem(
            label = "Department",
            value = profile.departmentName ?: "Not assigned",
            icon = Icons.Default.AccountCircle
        )
        
        ProfileDetailItem(
            label = "Job Title",
            value = profile.jobName ?: "Not assigned",
            icon = Icons.Default.AccountCircle
        )
        
        ProfileDetailItem(
            label = "Employee ID",
            value = profile.employeeId,
            icon = Icons.Default.Person
        )
        
        ProfileDetailItem(
            label = "Hire Date",
            value = profile.hireDate ?: "Not provided",
            icon = Icons.Default.DateRange
        )
    }
}

@Composable
fun AccountInfoCard(profile: EmployeeProfile) {
    ProfileSectionCard(
        title = "Account Information",
        icon = Icons.Default.AccountBox
    ) {
        ProfileDetailItem(
            label = "ID Number",
            value = profile.idNumber ?: "Not provided",
            icon = Icons.Default.Person
        )
        
        ProfileDetailItem(
            label = "Role",
            value = profile.roleName ?: "Not assigned",
            icon = Icons.Default.Settings
        )
        
        ProfileDetailItem(
            label = "Account Created",
            value = profile.createdAt?.let {
                it.substring(0, it.indexOf('T'))
            } ?: "Not available",
            icon = Icons.Default.DateRange
        )
    }
}

@Composable
fun ProfileSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(spring(stiffness = Spring.StiffnessLow))
        ) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // Icon in a circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(AppColors.blue500.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AppColors.blue500,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.gray800
                )
            }
            
            Divider(
                color = Color(0xFFEEF2F6),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Section content
            content()
        }
    }
}

@Composable
fun ProfileDetailItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Small icon
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFFF5F7FA), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.blue500,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Label and value
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = AppColors.gray500,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = if (value != "Not provided" && value != "Not assigned" && value != "Not available") 
                    FontWeight.Medium else FontWeight.Normal,
                color = if (value != "Not provided" && value != "Not assigned" && value != "Not available") 
                    AppColors.gray800 else Color(0xFFADB5BD),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun LoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = AppColors.blue500,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Loading profile data...",
                fontSize = 16.sp,
                color = ExtendedAppColors.gray600,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorCard(error: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = AppColors.red,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Failed to load profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.gray800,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = error,
                fontSize = 14.sp,
                color = ExtendedAppColors.gray600,
                textAlign = TextAlign.Center
            )
        }
    }
} 