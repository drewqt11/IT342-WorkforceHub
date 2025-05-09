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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material.icons.filled.Shield
import cit.edu.workforcehub.R
import cit.edu.workforcehub.api.ApiHelper
import cit.edu.workforcehub.api.models.EmployeeProfile
import cit.edu.workforcehub.presentation.components.AppScreen
import cit.edu.workforcehub.presentation.components.AppHeader
import cit.edu.workforcehub.presentation.components.UniversalDrawer
import cit.edu.workforcehub.presentation.theme.AppColors
import kotlinx.coroutines.launch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import cit.edu.workforcehub.presentation.theme.CustomIcons
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import cit.edu.workforcehub.presentation.components.LoadingComponent

/**
 * Formats a date string from ISO format (YYYY-MM-DD) to a readable format (Month DD, YYYY)
 * If the input is not a valid date or follows a different format, the original string is returned.
 */
private fun formatDate(dateString: String, includeTime: Boolean = false): String {
    return try {
        if (dateString.contains('T') && includeTime) {
            // Handle ISO format with time: YYYY-MM-DDThh:mm:ss
            val dateTime = LocalDate.parse(dateString.substring(0, dateString.indexOf('T')))
            val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
            
            // Extract the time portion if it exists
            val timePart = if (dateString.length > dateString.indexOf('T') + 1) {
                val timeString = dateString.substring(dateString.indexOf('T') + 1)
                try {
                    val hour = timeString.substring(0, 2).toInt()
                    val minute = timeString.substring(3, 5).toInt()
                    val amPm = if (hour >= 12) "PM" else "AM"
                    val hour12 = if (hour % 12 == 0) 12 else hour % 12
                    " ${hour12}:${minute.toString().padStart(2, '0')} $amPm"
                } catch (e: Exception) {
                    ""
                }
            } else {
                ""
            }
            
            "${dateTime.format(formatter)}$timePart"
        } else {
            // Handle just date: YYYY-MM-DD
            val date = LocalDate.parse(dateString.split('T')[0])
            val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
            date.format(formatter)
        }
    } catch (e: Exception) {
        // Return the original string if parsing fails
        dateString
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToLeaveRequests: () -> Unit = {},
    onNavigateToOvertimeRequests: () -> Unit = {},
    onNavigateToReimbursementRequests: () -> Unit = {},
    onNavigateToPerformance: () -> Unit = {},
    onNavigateToTraining: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToDocuments: () -> Unit = {}
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
    var dataDebug by remember { mutableStateOf("") }
    
    // Fetch profile data
    LaunchedEffect(key1 = true) {
        try {
            val employeeService = ApiHelper.getEmployeeService()
            val response = employeeService.getProfile()
            
            if (response.isSuccessful && response.body() != null) {
                val receivedProfile = response.body()!!
                
                // Ensure we have proper fallback values for nullable fields
                val updatedProfile = receivedProfile.copy(
                    phoneNumber = receivedProfile.phoneNumber?.takeIf { it.isNotBlank() } ?: "Not provided",
                    gender = receivedProfile.gender?.takeIf { it.isNotBlank() } ?: "Not provided",
                    dateOfBirth = receivedProfile.dateOfBirth?.takeIf { it.isNotBlank() } ?: "Not provided",
                    address = receivedProfile.address?.takeIf { it.isNotBlank() } ?: "Not provided",
                    maritalStatus = receivedProfile.maritalStatus?.takeIf { it.isNotBlank() } ?: "Not provided"
                )
                
                profileData = updatedProfile
                
                // Create debug info
                dataDebug = "Phone: ${receivedProfile.phoneNumber}, Gender: ${receivedProfile.gender}, " +
                         "DOB: ${receivedProfile.dateOfBirth}, Address: ${receivedProfile.address}, " +
                         "Marital: ${receivedProfile.maritalStatus}, " +
                         "IsActive: ${receivedProfile.isActive}"
                
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
        color = AppColors.gray100
    ) {
        // Using the Universal Drawer
        UniversalDrawer(
            drawerState = drawerState,
            currentScreen = AppScreen.PROFILE,
            onLogout = onLogout,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToAttendance = onNavigateToAttendance,
            onNavigateToLeaveRequests = onNavigateToLeaveRequests,
            onNavigateToOvertimeRequests = onNavigateToOvertimeRequests,
            onNavigateToReimbursementRequests = onNavigateToReimbursementRequests,
            onNavigateToPerformance = onNavigateToPerformance,
            onNavigateToTraining = onNavigateToTraining,
            onNavigateToProfile = {} // Already on profile
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Replace custom header with common AppHeader
                AppHeader(
                    onMenuClick = { 
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    modifier = Modifier.zIndex(1f),
                    providedFirstName = profileData?.firstName ?: "",
                    providedLastName = profileData?.lastName ?: "",
                    providedRole = profileData?.jobName ?: "Employee",
                    onProfileClick = {},  // Already on profile
                    onLogoutClick = onLogout
                )
                
                // Main content
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingComponent()
                    }
                } else if (error != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        ProfileErrorView(error = error!!)
                    }
                } else if (profileData != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        ProfileContent(
                            profile = profileData!!, 
                            scrollState = scrollState, 
                            debugInfo = dataDebug,
                            onNavigateToDocuments = onNavigateToDocuments
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    profile: EmployeeProfile, 
    scrollState: ScrollState, 
    debugInfo: String = "",
    onNavigateToDocuments: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp, top = 10.dp)
    ) {
        // Add My Profile header at the top of the scrollable content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile icon in circular blue background
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
                    painter = painterResource(id = R.drawable.user),
                    contentDescription = "Profile Icon",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // My Profile text
            Text(
                text = "My Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.gray800
            )
        }
        
        // Profile Card
        ProfileCard(
            profile = profile,
            onNavigateToDocuments = onNavigateToDocuments
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Personal Information
        PersonalInfoCard(profile = profile)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Employment Information
        EmploymentInfoCard(profile = profile)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Account Information
        AccountInfoCard(profile = profile)

        // Add extra space at the bottom to ensure the last card is fully visible when scrolling
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ProfileCard(
    profile: EmployeeProfile,
    onNavigateToDocuments: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppColors.gray200)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Add "View Documents" button at the top right corner
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(AppColors.blue500, AppColors.teal500)
                            )
                        )
                        .clickable { onNavigateToDocuments() }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "View Documents",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.offset(y = (-1).dp)
                        )
                    }
                }
            }
            
            // Main profile content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Get initials from profile name
                val firstInitial = profile.firstName.firstOrNull()?.uppercase() ?: ""
                val lastInitial = profile.lastName.firstOrNull()?.uppercase() ?: ""
                val initials = "$firstInitial$lastInitial"
                
                // Profile Image - Redesigned to show user initials instead of icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            clip = false
                        )
                        .border(width = 2.dp, color = Color.White, shape = CircleShape)
                        .clip(CircleShape)
                        .background(AppColors.blue100),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = AppColors.blue500,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Profile Name
                Text(
                    text = "${profile.firstName} ${profile.lastName}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.gray900
                )
                
                // Job Title
                Text(
                    text = profile.jobName ?: "Not Provided",
                    fontSize = 14.sp,
                    color = if (profile.jobName == null) AppColors.gray400 else AppColors.gray600,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                // Contact Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Email
                    ContactInfoItem(
                        icon = Icons.Default.Email,
                        iconBgColor = AppColors.blue100,
                        iconTint = AppColors.blue500,
                        text = profile.email
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Phone
                    ContactInfoItem(
                        icon = Icons.Default.Phone,
                        iconBgColor = AppColors.teal100,
                        iconTint = AppColors.teal500,
                        text = profile.phoneNumber ?: "09685861226"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Location
                    ContactInfoItem(
                        icon = Icons.Default.LocationOn,
                        iconBgColor = AppColors.blue100,
                        iconTint = AppColors.blue500,
                        text = profile.address ?: "Midori Plains Blk. 4 lot 7, Tungkop, Minglanilla"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Job
                    ContactInfoItem(
                        icon = Icons.Default.Business,
                        iconBgColor = AppColors.teal100,
                        iconTint = AppColors.teal500,
                        text = profile.jobName ?: "Not Provided"
                    )
                }
                
                // Divider
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = AppColors.gray200,
                    thickness = 1.dp
                )
                
                // Status indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Status
                    StatusItem(
                        icon = Icons.Default.Person,
                        iconTint = AppColors.blue500,
                        label = "Status",
                        value = if (profile.status) "Active" else "Inactive",
                        isActive = profile.status
                    )
                    
                    // Employment
                    StatusItem(
                        icon = CustomIcons.AccessTime,
                        iconTint = AppColors.teal500,
                        label = "Employment",
                        value = profile.employmentStatus ?: "FULL_TIME"
                    )
                    
                    // Role
                    StatusItem(
                        icon = Icons.Default.Person,
                        iconTint = AppColors.blue500,
                        label = "Role",
                        value = profile.roleName ?: "Employee",
                        iconDrawableRes = CustomIcons.Medal
                    )
                }
            }
        }
    }
}

@Composable
fun ContactInfoItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            fontSize = 14.sp,
            color = AppColors.gray700,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatusItem(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    isActive: Boolean? = null,
    iconDrawableRes: Int? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (iconDrawableRes != null) {
                Icon(
                    painter = painterResource(id = iconDrawableRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(25.dp)
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = AppColors.gray500
        )
        
        if (isActive != null) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isActive) AppColors.teal100 else AppColors.redLight)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isActive) AppColors.teal900 else AppColors.red
                )
            }
        } else {
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.gray800
            )
        }
    }
}

@Composable
fun PersonalInfoCard(profile: EmployeeProfile) {
    InfoCard(
        title = "Personal Information",
        icon = Icons.Default.Person,
        iconTint = AppColors.blue500
    ) {
        InfoItem(label = "Employee ID", value = profile.employeeId)
        InfoItem(label = "ID Number", value = profile.idNumber ?: "22-4480-181")
        InfoItem(label = "Full Name", value = "${profile.firstName} ${profile.lastName}")
        InfoItem(label = "Gender", value = profile.gender ?: "Not Provided")
        InfoItem(
            label = "Date of Birth",
            value = profile.dateOfBirth?.let { formatDate(it) } ?: "Not Provided",
        )
        InfoItem(
            label = "Marital Status",
            value = profile.maritalStatus ?: "Not Provided",
            isLast = true
        )
    }
}

@Composable
fun EmploymentInfoCard(profile: EmployeeProfile) {
    InfoCard(
        title = "Employment Information",
        icon = CustomIcons.Work,
        iconTint = AppColors.teal500
    ) {
        InfoItem(
            label = "Status",
            value = if (profile.status) "Active" else "Inactive",
            isHighlighted = true,
            highlightBgColor = if (profile.status) AppColors.teal100 else AppColors.redLight,
            highlightTextColor = if (profile.status) AppColors.teal900 else AppColors.red
        )
        InfoItem(label = "Employment", value = profile.employmentStatus ?: "FULL_TIME")
        InfoItem(
            label = "Hire Date",
            value = profile.hireDate?.let { formatDate(it) } ?: "May 2, 2025",
            icon = CustomIcons.CalendarToday,
            iconTint = AppColors.teal500
        )
        InfoItem(
            label = "Department",
            value = profile.departmentName ?: "Not Provided",
            icon = CustomIcons.Business,
            iconTint = AppColors.teal500
        )
        InfoItem(
            label = "Job Title",
            value = profile.jobName ?: "Not Provided",
            icon = CustomIcons.Work,
            iconTint = AppColors.teal500
        )
        InfoItem(
            label = "Role",
            value = profile.roleName ?: "Employee",
            iconDrawableRes = CustomIcons.Medal,
            iconTint = AppColors.teal500,
            isLast = true
        )
    }
}

@Composable
fun AccountInfoCard(profile: EmployeeProfile) {
    InfoCard(
        title = "Account Information",
        icon = Icons.Default.Shield,
        iconTint = AppColors.blue500
    ) {
        InfoItem(
            label = "Account Status",
            value = if (profile.isActive == true) "Active" else "Inactive",
            isHighlighted = true,
            highlightBgColor = if (profile.isActive == true) AppColors.teal100 else AppColors.redLight,
            highlightTextColor = if (profile.isActive == true) AppColors.teal900 else AppColors.red
        )
        InfoItem(
            label = "Account Created",
            value = profile.createdAt?.let { formatDate(it) } ?: "Not Provided",
            icon = CustomIcons.CalendarToday,
            iconTint = AppColors.blue500
        )
        InfoItem(
            label = "Last Login",
            value = profile.lastLogin?.let { formatDate(it, true) } ?: "Not Provided",
            icon = CustomIcons.Login,
            iconTint = AppColors.blue500,
            isLast = true
        )
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppColors.gray200)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.gray800
                )
            }
            
            // Content
            content()
        }
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String,
    icon: ImageVector? = null,
    iconTint: Color = AppColors.blue500,
    isHighlighted: Boolean = false,
    highlightBgColor: Color = AppColors.blue50,
    highlightTextColor: Color = AppColors.blue500,
    isLast: Boolean = false,
    iconDrawableRes: Int? = null,
    isNotProvided: Boolean = value == "Not Provided"
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label always goes on the left
            Text(
                text = label,
                fontSize = 14.sp,
                color = AppColors.gray500,
                modifier = Modifier.weight(0.4f)
            )
            
            // Value section takes the remaining space
            Box(
                modifier = Modifier.weight(0.6f),
                contentAlignment = Alignment.CenterStart // Align content to the start (left)
            ) {
                if (isHighlighted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(highlightBgColor)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = value,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = highlightTextColor
                        )
                    }
                } else if (icon != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(14.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        Text(
                            text = value,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isNotProvided) AppColors.gray400 else AppColors.gray700
                        )
                    }
                } else if (iconDrawableRes != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = iconDrawableRes),
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(14.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        Text(
                            text = value,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isNotProvided) AppColors.gray400 else AppColors.gray700
                        )
                    }
                } else {
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isNotProvided) AppColors.gray400 else AppColors.gray700
                    )
                }
            }
        }
        
        if (!isLast) {
            HorizontalDivider(
                color = AppColors.gray200,
                thickness = 1.dp
            )
        }
    }
}

@Composable
fun ProfileErrorView(error: String) {
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
    }
} 