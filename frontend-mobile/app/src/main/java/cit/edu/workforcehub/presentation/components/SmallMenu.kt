package cit.edu.workforcehub.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import cit.edu.workforcehub.api.ApiHelper
import cit.edu.workforcehub.api.models.EmployeeProfile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.style.TextOverflow
import cit.edu.workforcehub.presentation.theme.AppColors
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures

/**
 * A small dropdown menu that displays user profile information.
 * 
 * @param isVisible Whether the menu is visible
 * @param onDismiss Callback when the menu is dismissed
 * @param modifier Modifier for the menu
 */
@Composable
fun SmallMenu(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onLogout: () -> Unit, // Keeping parameter for backward compatibility
    modifier: Modifier = Modifier,
) {
    // State for profile data
    var profileData by remember { mutableStateOf<EmployeeProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Fetch profile data
    LaunchedEffect(key1 = isVisible) {
        if (isVisible) {
            isLoading = true
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

    if (isVisible) {
        Popup(
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                focusable = true
            ),
            alignment = Alignment.TopEnd
        ) {
            Column(
                modifier = modifier
                    .padding(top = 55.dp, end = 16.dp)
                    .width(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = Color.Black.copy(alpha = 0.25f),
                        ambientColor = Color.Gray.copy(alpha = 0.15f)
                    )
                    .background(Color.White)
                    .border(
                        width = 0.5.dp,
                        color = Color.LightGray,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { /* Consume touch events here to prevent dismissal */ }
                        )
                    }
            ) {
                // Title "Mini Profile" at the top right corner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, end = 12.dp, bottom = 0.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = "Mini Profile",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.blue500,
                        letterSpacing = 0.3.sp
                    )
                }
                
                // User details section with adjusted padding
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = AppColors.blue500,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else if (error != null) {
                        Text(
                            text = "Error loading profile",
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        // Full name
                        Text(
                            text = "${profileData?.firstName ?: ""} ${profileData?.lastName ?: ""}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )
                        
                        // Email
                        Text(
                            text = profileData?.email ?: "",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // ID Number
                        Text(
                            text = "ID Number: ${profileData?.idNumber ?: ""}",
                            fontSize = 12.sp,
                            color = Color.Gray,
                        )
                        
                        // Status
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status: ",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            
                            val isActive = profileData?.status == true
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isActive) 
                                            Color(0xFFCCFBF1) // Light green background for Active
                                        else 
                                            Color(0xFFFCE8E6) // Light red background for Inactive
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isActive) "Active" else "Inactive",
                                    fontSize = 12.sp,
                                    color = if (isActive) 
                                              Color(0xFF134E4A)  // Green text
                                            else 
                                              Color(0xFFEA4335),  // Red text
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview of the SmallMenu component
 */
@androidx.compose.ui.tooling.preview.Preview
@Composable
fun SmallMenuPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // We need this column for the preview to show the menu properly
        SmallMenu(
            isVisible = true,
            onDismiss = { },
            onLogout = { }
        )
    }
}

