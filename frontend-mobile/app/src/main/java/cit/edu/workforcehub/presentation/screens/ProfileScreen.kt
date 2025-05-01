package cit.edu.workforcehub.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cit.edu.workforcehub.api.ApiHelper
import cit.edu.workforcehub.api.models.EmployeeProfile
import cit.edu.workforcehub.presentation.components.AppScreen
import cit.edu.workforcehub.presentation.components.UniversalDrawer
import cit.edu.workforcehub.presentation.theme.AppColors
import kotlinx.coroutines.launch

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
    // For animated decorative elements
    val infiniteTransition = rememberInfiniteTransition(label = "profile_animation")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ), label = "alpha_animation"
    )
    
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
                            color = cit.edu.workforcehub.presentation.theme.AppColors.blue300,
                            start = Offset(i * gridSpacing, 0f),
                            end = Offset(i * gridSpacing, size.height),
                            strokeWidth = strokeWidth
                        )
                    }
                    
                    for (i in 0..(size.height / gridSpacing).toInt()) {
                        drawLine(
                            color = cit.edu.workforcehub.presentation.theme.AppColors.blue300,
                            start = Offset(0f, i * gridSpacing),
                            end = Offset(size.width, i * gridSpacing),
                            strokeWidth = strokeWidth
                        )
                    }
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Profile Header with gradient
                    ProfileHeaderSection(
                        profile = profileData,
                        isLoading = isLoading,
                        onMenuClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        modifier = Modifier.zIndex(1f) // Ensure header stays on top
                    )

                    // Main content with card sections
                    AnimatedVisibility(
                        visible = !isLoading && error == null && profileData != null,
                        enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (profileData != null) {
                                // Main Information sections
                                PersonalInfoCard(profile = profileData!!)
                                WorkInfoCard(profile = profileData!!)
                                AccountInfoCard(profile = profileData!!)

                                // Bottom spacing
                                Spacer(modifier = Modifier.height(24.dp))
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
                }
            }
        }
    }
}

@Composable
fun ProfileHeaderSection(
    profile: EmployeeProfile?,
    isLoading: Boolean,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        // Header background with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(cit.edu.workforcehub.presentation.theme.AppColors.blue500, cit.edu.workforcehub.presentation.theme.AppColors.teal500),
                        startX = 0f,
                        endX = 1200f
                    )
                )
        ) {
            // Decorative circles in the background
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.15f)
            ) {
                // Large circle
                drawCircle(
                    color = Color.White,
                    center = Offset(size.width * 0.85f, size.height * 0.2f),
                    radius = size.width * 0.35f
                )
                
                // Medium circle
                drawCircle(
                    color = Color.White,
                    center = Offset(size.width * 0.15f, size.height * 0.75f),
                    radius = size.width * 0.15f
                )
                
                // Small circle
                drawCircle(
                    color = Color.White,
                    center = Offset(size.width * 0.6f, size.height * 0.9f),
                    radius = size.width * 0.05f
                )
            }
            
            // Top app bar with menu and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Menu button with translucent background
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }
                
                // Title with nice typography
                Text(
                    text = "Profile",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Empty spacer to balance layout
                Spacer(modifier = Modifier.size(40.dp))
            }
            
            // User basic info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .offset(y = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isLoading && profile != null) {
                    // User avatar
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(4.dp, CircleShape)
                            .background(cit.edu.workforcehub.presentation.theme.AppColors.white, CircleShape)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${profile.firstName.firstOrNull() ?: ""}${profile.lastName.firstOrNull() ?: ""}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = cit.edu.workforcehub.presentation.theme.AppColors.blue700
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // User info
                    Column {
                        Text(
                            text = "${profile.firstName} ${profile.lastName}",
                            color = cit.edu.workforcehub.presentation.theme.AppColors.white,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = profile.idNumber ?: "ID Pending",
                            color = cit.edu.workforcehub.presentation.theme.AppColors.white.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Status chip
                        val statusColor = when (profile.employmentStatus) {
                            "ACTIVE" -> cit.edu.workforcehub.presentation.theme.AppColors.green
                            "PENDING" -> cit.edu.workforcehub.presentation.theme.AppColors.amber
                            "INACTIVE" -> cit.edu.workforcehub.presentation.theme.AppColors.red
                            else -> cit.edu.workforcehub.presentation.theme.AppColors.gray500
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
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            // Status indicator dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            
                            Spacer(modifier = Modifier.width(6.dp))
                            
                            Text(
                                text = statusText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
                else if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(36.dp)
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
        icon = Icons.Default.Person,
        iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.blue50,
        iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.blue700
    ) {
        ProfileDetailItem(
            label = "Full Name",
            value = "${profile.firstName} ${profile.lastName}",
            icon = Icons.Default.Person,
            iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.blue50,
            iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.blue700
        )
        
        ProfileDetailItem(
            label = "Email",
            value = profile.email,
            icon = Icons.Default.Email,
            iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.teal50,
            iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.teal700
        )
        
        ProfileDetailItem(
            label = "Phone",
            value = profile.phoneNumber ?: "Not provided",
            icon = Icons.Default.Phone,
            iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.blue50,
            iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.blue700
        )
        
        ProfileDetailItem(
            label = "Address",
            value = profile.address ?: "Not provided",
            icon = Icons.Default.LocationOn,
            iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.teal50,
            iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.teal700
        )
        
        if (profile.gender != null || profile.dateOfBirth != null || profile.maritalStatus != null) {
            Divider(
                color = cit.edu.workforcehub.presentation.theme.AppColors.gray200,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            if (profile.gender != null) {
                ProfileDetailItem(
                    label = "Gender",
                    value = profile.gender,
                    icon = Icons.Default.Person,
                    iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.blue50,
                    iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.blue700
                )
            }
            
            if (profile.dateOfBirth != null) {
                ProfileDetailItem(
                    label = "Date of Birth",
                    value = profile.dateOfBirth,
                    icon = Icons.Default.DateRange,
                    iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.teal50,
                    iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.teal700
                )
            }
            
            if (profile.maritalStatus != null) {
                ProfileDetailItem(
                    label = "Marital Status",
                    value = profile.maritalStatus,
                    icon = Icons.Default.Person,
                    iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.blue50,
                    iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.blue700
                )
            }
        }
    }
}

@Composable
fun WorkInfoCard(profile: EmployeeProfile) {
    ProfileSectionCard(
        title = "Work Information",
        icon = Icons.Default.Info,
        iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.teal50,
        iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.teal700
    ) {
        ProfileDetailItem(
            label = "Department",
            value = profile.departmentName ?: "Not assigned",
            icon = Icons.Default.Home,
            iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.blue50,
            iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.blue700
        )
        
        ProfileDetailItem(
            label = "Job Title",
            value = profile.jobName ?: "Not assigned",
            icon = Icons.Default.Star,
            iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.teal50,
            iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.teal700
        )
        
        ProfileDetailItem(
            label = "Employee ID",
            value = profile.employeeId,
            icon = Icons.Default.AccountCircle,
            iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.blue50,
            iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.blue700
        )
        
        ProfileDetailItem(
            label = "Hire Date",
            value = profile.hireDate ?: "Not provided",
            icon = Icons.Default.DateRange,
            iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.teal50,
            iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.teal700
        )
        
        if (profile.employmentStatus != null) {
            ProfileDetailItem(
                label = "Employment Status",
                value = profile.employmentStatus,
                icon = Icons.Default.Info,
                iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.blue50,
                iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.blue700
            )
        }
    }
}

@Composable
fun AccountInfoCard(profile: EmployeeProfile) {
    ProfileSectionCard(
        title = "Account Information",
        icon = Icons.Default.AccountBox,
        iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.blue50,
        iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.blue700
    ) {
        ProfileDetailItem(
            label = "ID Number",
            value = profile.idNumber ?: "Not provided",
            icon = Icons.Default.Person,
            iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.teal50,
            iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.teal700
        )
        
        ProfileDetailItem(
            label = "Role",
            value = profile.roleName ?: "Not assigned",
            icon = Icons.Default.Settings,
            iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.blue50,
            iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.blue700
        )
        
        if (profile.createdAt != null) {
            ProfileDetailItem(
                label = "Account Created",
                value = profile.createdAt.let {
                    it.substring(0, it.indexOf('T'))
                },
                icon = Icons.Default.DateRange,
                iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.teal50,
                iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.teal700
            )
        }
        
        // Display account status
        val statusText = if (profile.status) "Active" else "Inactive"
        val statusIcon = if (profile.status) Icons.Default.Person else Icons.Default.AccountCircle
        
        ProfileDetailItem(
            label = "Account Status",
            value = statusText,
            icon = statusIcon,
            iconBackgroundColor = cit.edu.workforcehub.presentation.theme.AppColors.blue50,
            iconTintColor = cit.edu.workforcehub.presentation.theme.AppColors.blue700
        )
    }
}

@Composable
fun ProfileSectionCard(
    title: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTintColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cit.edu.workforcehub.presentation.theme.AppColors.white
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .animateContentSize(spring(stiffness = Spring.StiffnessLow))
        ) {
            // Section header with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon in a circle with theme color
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = iconBackgroundColor,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTintColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cit.edu.workforcehub.presentation.theme.AppColors.gray800
                    )
                }
            }
            
            Divider(
                color = cit.edu.workforcehub.presentation.theme.AppColors.gray200,
                thickness = 1.dp,
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
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
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTintColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with circular background
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconBackgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier.size(18.dp)
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
                color = cit.edu.workforcehub.presentation.theme.AppColors.gray500,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = if (value != "Not provided" && value != "Not assigned" && value != "Not available") 
                    FontWeight.Medium else FontWeight.Normal,
                color = if (value != "Not provided" && value != "Not assigned" && value != "Not available") 
                    cit.edu.workforcehub.presentation.theme.AppColors.gray800 else cit.edu.workforcehub.presentation.theme.AppColors.gray500,
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
        colors = CardDefaults.cardColors(containerColor = cit.edu.workforcehub.presentation.theme.AppColors.white),
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
                color = cit.edu.workforcehub.presentation.theme.AppColors.blue500,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Loading your profile...",
                fontSize = 16.sp,
                color = cit.edu.workforcehub.presentation.theme.AppColors.gray700,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorCard(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cit.edu.workforcehub.presentation.theme.AppColors.white),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                tint = cit.edu.workforcehub.presentation.theme.AppColors.red,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Failed to load profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = cit.edu.workforcehub.presentation.theme.AppColors.gray800,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = error,
                fontSize = 14.sp,
                color = cit.edu.workforcehub.presentation.theme.AppColors.gray500,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { /* Retry functionality could be added here */ },
                colors = CardDefaults.cardColors(
                    containerColor = cit.edu.workforcehub.presentation.theme.AppColors.blue50
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        tint = cit.edu.workforcehub.presentation.theme.AppColors.blue700,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Retry",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = cit.edu.workforcehub.presentation.theme.AppColors.blue700
                    )
                }
            }
        }
    }
} 