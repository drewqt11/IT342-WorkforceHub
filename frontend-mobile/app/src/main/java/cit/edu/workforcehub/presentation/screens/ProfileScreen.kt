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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.ScrollState
import cit.edu.workforcehub.R
import cit.edu.workforcehub.api.ApiHelper
import cit.edu.workforcehub.api.models.EmployeeProfile
import cit.edu.workforcehub.presentation.components.AppScreen
import cit.edu.workforcehub.presentation.components.UniversalDrawer
import cit.edu.workforcehub.presentation.theme.AppColors
import kotlinx.coroutines.launch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import cit.edu.workforcehub.presentation.theme.CustomIcons

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
        color = AppColors.gray50
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
                // Main content
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.blue500)
                    }
                } else if (error != null) {
                    ErrorView(error = error!!)
                } else if (profileData != null) {
                    ProfileContent(profile = profileData!!, scrollState = scrollState)
                }
                
                // Menu button in top-left corner
                IconButton(
                    onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 30.dp, bottom = 19.dp)
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .align(Alignment.TopStart)
                        .zIndex(10f)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_sort_by_size),
                        contentDescription = "Menu",
                        tint = Color.Transparent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContent(profile: EmployeeProfile, scrollState: ScrollState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 50.dp)
    ) {
        // My Profile title at the top of the scrollable content
        Text(
            text = "My Profile",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.gray800,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )
        
        // User Profile Card
        UserProfileCard(profile = profile)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Personal Information
        PersonalInfoSection(profile = profile)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Employment Information
        EmploymentInfoSection(profile = profile)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Account Information
        AccountInfoSection(profile = profile)
        
        // Add extra space at the bottom to ensure the last card is fully visible when scrolling
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun UserProfileCard(profile: EmployeeProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.white),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile avatar with initials on light blue background
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE1F0FF)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4285F4)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${profile.firstName.firstOrNull() ?: ""}${profile.lastName.firstOrNull() ?: ""}", 
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.white
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User Name
            Text(
                text = "${profile.firstName} ${profile.lastName}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.gray800
            )
            
            // Role or N/A
            Text(
                text = profile.jobName ?: "N/A",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = AppColors.gray500,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            
            // Contact Information
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Email
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F5F9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = profile.email,
                        fontSize = 14.sp,
                        color = AppColors.gray700
                    )
                }
                
                // Phone
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F5F9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = profile.phoneNumber ?: "N/A",
                        fontSize = 14.sp,
                        color = AppColors.gray700
                    )
                }
                
                // Location
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F5F9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = profile.address ?: "N/A",
                        fontSize = 14.sp,
                        color = AppColors.gray700
                    )
                }
                
                // Website/URL
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F5F9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "N/A",
                        fontSize = 14.sp,
                        color = AppColors.gray700
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Divider(
                color = AppColors.gray200,
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status indicators in a row at the bottom of card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F5F9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "Status",
                        fontSize = 12.sp,
                        color = AppColors.gray500
                    )
                    
                    Text(
                        text = if (profile.status) "Active" else "Inactive",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (profile.status) Color(0xFF22C55E) else Color(0xFFEF4444)
                    )
                }
                
                // Employment
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F5F9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "Employment",
                        fontSize = 12.sp,
                        color = AppColors.gray500
                    )
                    
                    Text(
                        text = profile.employmentStatus ?: "N/A",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.gray800
                    )
                }
                
                // Role
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F5F9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "Role",
                        fontSize = 12.sp,
                        color = AppColors.gray500
                    )
                    
                    Text(
                        text = profile.roleName ?: "Employee",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.gray800
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalInfoSection(profile: EmployeeProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.white),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = CustomIcons.Person,
                    contentDescription = null,
                    tint = Color(0xFF4285F4),
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Personal Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.gray800
                )
            }
            
            Divider(color = AppColors.gray200, thickness = 1.dp)
            
            // Info items
            ModernInfoItem(label = "Employee ID", value = profile.employeeId)
            ModernInfoItem(label = "ID Number", value = profile.idNumber ?: "22-4480-181")
            ModernInfoItem(label = "Full Name", value = "${profile.firstName} ${profile.lastName}")
            ModernInfoItem(label = "Gender", value = profile.gender ?: "N/A")
            ModernInfoItem(
                label = "Date of Birth", 
                value = profile.dateOfBirth ?: "N/A", 
                iconVector = CustomIcons.CalendarToday,
                iconTint = Color(0xFF4285F4)
            )
            ModernInfoItem(
                label = "Marital Status", 
                value = profile.maritalStatus ?: "N/A", 
                iconVector = Icons.Default.Favorite,
                iconTint = Color(0xFF4285F4)
            )
        }
    }
}

@Composable
fun EmploymentInfoSection(profile: EmployeeProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.white),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = CustomIcons.Business,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Employment Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.gray800
                )
            }
            
            Divider(color = AppColors.gray200, thickness = 1.dp)
            
            // Status with green badge
            ModernInfoItem(
                label = "Status", 
                value = "Active",
                isHighlighted = true,
                highlightColor = Color(0xFFDCFCE7),
                textColor = Color(0xFF22C55E)
            )
            
            ModernInfoItem(label = "Employment", value = profile.employmentStatus ?: "ACTIVE")
            
            ModernInfoItem(
                label = "Hire Date", 
                value = profile.hireDate ?: "May 1, 2025",
                iconVector = CustomIcons.CalendarToday,
                iconTint = Color(0xFF10B981)
            )
            
            ModernInfoItem(
                label = "Department", 
                value = profile.departmentName ?: "N/A",
                iconVector = CustomIcons.Business,
                iconTint = Color(0xFF10B981)
            )
            
            ModernInfoItem(
                label = "Job Title", 
                value = profile.jobName ?: "N/A",
                iconVector = CustomIcons.Work,
                iconTint = Color(0xFF10B981)
            )
            
            ModernInfoItem(
                label = "Role", 
                value = profile.roleName ?: "Employee",
                iconVector = Icons.Default.Person,
                iconTint = Color(0xFF10B981)
            )
        }
    }
}

@Composable
fun AccountInfoSection(profile: EmployeeProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.white),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color(0xFF4285F4),
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Account Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.gray800
                )
            }
            
            Divider(color = AppColors.gray200, thickness = 1.dp)
            
            // Info items
            ModernInfoItem(
                label = "User ID", 
                value = "N/A",
                iconVector = CustomIcons.Key,
                iconTint = Color(0xFF4285F4)
            )
            
            ModernInfoItem(
                label = "Account Status", 
                value = "Active",
                isHighlighted = true,
                highlightColor = Color(0xFFDCFCE7),
                textColor = Color(0xFF22C55E)
            )
            
            ModernInfoItem(
                label = "Account Created", 
                value = profile.createdAt?.let { 
                    it.substring(0, it.indexOf('T')) 
                } ?: "May 1, 2025 5:52 PM",
                iconVector = CustomIcons.CalendarToday,
                iconTint = Color(0xFF4285F4)
            )
            
            ModernInfoItem(
                label = "Last Login", 
                value = "May 2, 2025 6:28 AM",
                iconVector = CustomIcons.Login,
                iconTint = Color(0xFF4285F4)
            )
        }
    }
}

@Composable
fun ModernInfoItem(
    label: String, 
    value: String, 
    iconVector: ImageVector? = null,
    iconTint: Color = AppColors.gray500,
    isHighlighted: Boolean = false,
    highlightColor: Color = AppColors.white,
    textColor: Color = AppColors.gray800
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = AppColors.gray500,
            modifier = Modifier.width(120.dp)
        )
        
        if (isHighlighted) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(highlightColor)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (iconVector != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(6.dp))
                
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = AppColors.gray700
                )
            }
        } else {
            Text(
                text = value,
                fontSize = 14.sp,
                color = AppColors.gray700
            )
        }
    }
}

@Composable
fun ErrorView(error: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = AppColors.red,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Error loading profile",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.gray800
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = error,
            fontSize = 16.sp,
            color = AppColors.gray600,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { /* Retry logic */ },
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.blue500),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier.size(18.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    "Retry", 
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
} 