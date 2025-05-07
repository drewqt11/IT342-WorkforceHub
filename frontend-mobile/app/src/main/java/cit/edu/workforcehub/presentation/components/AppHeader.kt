package cit.edu.workforcehub.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.workforcehub.R
import cit.edu.workforcehub.presentation.theme.AppColors
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import cit.edu.workforcehub.api.ApiHelper
import cit.edu.workforcehub.api.models.EmployeeProfile
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex

/**
 * A header component for app screens that automatically fetches profile data if not provided.
 * 
 * @param onMenuClick Callback when the menu button is clicked
 * @param modifier Modifier for the component
 * @param providedFirstName First name of the user (if provided externally)
 * @param providedLastName Last name of the user (if provided externally)
 * @param providedRole User's role in the organization (if provided externally)
 * @param forceAutoFetch Whether to force fetching profile data even if parameters are provided
 * @param onProfileClick Callback when the profile button is clicked
 * @param onLogoutClick Callback when logout is clicked
 */
@Composable
fun AppHeader(
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    providedFirstName: String = "",
    providedLastName: String = "",
    providedRole: String = "Employee",
    forceAutoFetch: Boolean = false,
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    // State for profile data
    var profileData by remember { mutableStateOf<EmployeeProfile?>(null) }
    var isLoading by remember { mutableStateOf(providedFirstName.isEmpty() || providedLastName.isEmpty() || forceAutoFetch) }
    var error by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    
    // Only fetch profile data if names aren't provided or forceAutoFetch is true
    val shouldFetch = (providedFirstName.isEmpty() || providedLastName.isEmpty() || forceAutoFetch)
    
    // Fetch profile data if needed
    LaunchedEffect(key1 = shouldFetch) {
        if (shouldFetch) {
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
    }
    
    // Use provided values or fallback to fetched profile data
    val firstName = if (providedFirstName.isNotEmpty() && !forceAutoFetch) providedFirstName else profileData?.firstName ?: ""
    val lastName = if (providedLastName.isNotEmpty() && !forceAutoFetch) providedLastName else profileData?.lastName ?: ""
    val role = if (providedRole != "Employee" && !forceAutoFetch) providedRole else profileData?.jobName ?: "Employee"
    val email = profileData?.email ?: ""
    val idNumber = profileData?.idNumber ?: ""
    val isActive = profileData?.isActive ?: false
    val status = if (isActive) "Active" else "Inactive"

    // Light gray color for border
    val lightGray = Color(0xFFE0E0E0)
    // Text colors
    val darkGray = Color(0xFF4F4F4F)
    val mediumGray = Color(0xFF9E9E9E)
    // Status colors
    val activeGreen = Color(0xFF4CAF50)
    val inactiveRed = Color(0xFFE53935)
    val activeBackground = Color(0xFFE6F4EA)
    val inactiveBackground = Color(0xFFFCE8E6)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                spotColor = AppColors.gray900.copy(alpha = 0.2f)
            )
            .background(AppColors.headerBackground)
            .height(70.dp)
            .border(
                width = 1.dp,
                color = lightGray,
                shape = RectangleShape
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 0.dp)
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Menu button on the left
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.menu),
                    contentDescription = "Menu",
                    tint = AppColors.blue900,
                    modifier = Modifier
                        .size(18.dp)
                )
            }

            // Profile button on the right with dropdown menu
            Box(
                modifier = Modifier.zIndex(1f)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White)
                        .padding(start = 3.dp, end = 6.dp, top = 3.dp, bottom = 3.dp)
                        .clickable { 
                            if (expanded) {
                                // If already expanded, close it
                                expanded = false
                            } else {
                                // If not expanded, show it
                                expanded = true
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Profile picture with initials
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .shadow(
                                elevation = 3.dp,
                                shape = CircleShape,
                                clip = false
                            )
                            .border(width = 1.5.dp, color = Color.White, shape = CircleShape)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(AppColors.blue700, AppColors.blue500, AppColors.blue100),
                                    start = Offset(0f, 0f),
                                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Get initials from profile name
                        val firstInitial = firstName.firstOrNull()?.uppercase() ?: ""
                        val lastInitial = lastName.firstOrNull()?.uppercase() ?: ""
                        val initials = "$firstInitial$lastInitial"

                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // User name and role (shown even during loading for smooth UI)
                    if (firstName.isNotEmpty() || lastName.isNotEmpty() || isLoading) {
                        Column(
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier
                                .height(34.dp)
                                .padding(vertical = 0.dp)
                        ) {
                            Text(
                                text = if (isLoading && firstName.isEmpty()) "Loading..." else "$firstName $lastName".trim(),
                                fontWeight = FontWeight.Medium,
                                fontSize = MaterialTheme.typography.labelMedium.fontSize,
                                color = AppColors.blue900,
                                lineHeight = 14.sp
                            )
                            Text(
                                text = role,
                                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                color = AppColors.blue500,
                                lineHeight = 11.sp,
                                modifier = Modifier.padding(top = 0.dp)
                            )
                        }
                    }
                    
                    // Dropdown arrow icon
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown Menu",
                        tint = AppColors.blue500,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
                
                // Profile dropdown menu - Redesigned to match the image
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(with(LocalDensity.current) { 260.dp })
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 0.5.dp,
                            color = lightGray,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    properties = PopupProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    // Header with "Mini Profile" text
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Text(
                            text = "Mini Profile",
                            style = TextStyle(
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                color = AppColors.blue900
                            )
                        )
                    }
                    
                    // User information content
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Full name
                            Text(
                                text = "$firstName $lastName",
                                style = TextStyle(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = darkGray
                                )
                            )
                            
                            // Email
                            Text(
                                text = email,
                                style = TextStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = mediumGray
                                )
                            )
                            
                            // ID Number
                            Text(
                                text = "ID Number: $idNumber",
                                style = TextStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = mediumGray
                                )
                            )
                            
                            // Status with colored indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status: ",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color = mediumGray
                                    )
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .background(
                                            color = if (isActive) activeBackground else inactiveBackground,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = status,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = if (isActive) activeGreen else inactiveRed
                                        )
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AppHeaderPreview() {
    androidx.compose.foundation.layout.Column {
        AppHeader(
            providedFirstName = "Andri", 
            providedLastName = "Apas", 
            onLogoutClick = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AppHeaderMinimalPreview() {
    androidx.compose.foundation.layout.Column {
        AppHeader(
            providedFirstName = "", 
            providedLastName = "", 
            onLogoutClick = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AppHeaderAutoFetchPreview() {
    androidx.compose.foundation.layout.Column {
        AppHeader(
            forceAutoFetch = true, 
            onLogoutClick = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AppHeaderWithDropdownPreview() {
    androidx.compose.foundation.layout.Column {
        AppHeader(
            providedFirstName = "Andri",
            providedLastName = "Apas",
            providedRole = "Employee",
            onLogoutClick = {}
        )
    }
}