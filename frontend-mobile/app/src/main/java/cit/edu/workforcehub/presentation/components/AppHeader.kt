package cit.edu.workforcehub.presentation.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.workforcehub.api.models.EmployeeProfile
import cit.edu.workforcehub.presentation.theme.AppColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * A reusable header component for app screens that displays user information and a date.
 * 
 * @param title Optional title to display in the header
 * @param profileData The employee profile data to display
 * @param isLoading Whether the profile data is loading
 * @param showDate Whether to show the date section
 * @param onMenuClick Callback when the menu button is clicked
 * @param modifier Modifier for the component
 */
@Composable
fun AppHeader(
    title: String? = null,
    profileData: EmployeeProfile? = null,
    isLoading: Boolean = true,
    showDate: Boolean = true,
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
            .height(if (showDate) 240.dp else 180.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(cit.edu.workforcehub.presentation.theme.AppColors.blue500, cit.edu.workforcehub.presentation.theme.AppColors.teal500),
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
                radius = size.width * 0.08f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 30.dp, bottom = 19.dp)
        ) {
            // Top row with menu button and title
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
                        tint = cit.edu.workforcehub.presentation.theme.AppColors.white,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                if (title != null) {
                    Text(
                        text = title,
                        color = cit.edu.workforcehub.presentation.theme.AppColors.white,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Empty spacer to balance layout if title is shown
                    Spacer(modifier = Modifier.size(40.dp))
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
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = AppColors.blue500,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (profileData != null) {
                        // Show initials from profile name
                        val firstInitial = profileData.firstName.firstOrNull()?.uppercase() ?: ""
                        val lastInitial = profileData.lastName.firstOrNull()?.uppercase() ?: ""
                        val initials = "$firstInitial$lastInitial"
                        
                        Text(
                            text = initials,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.blue500
                        )
                    } else {
                        // Default placeholder with initials
                        Text(
                            text = "AA",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.blue500
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // User info
                Column {
                    // Welcome message
                    Text(
                        text = "Welcome back,",
                        color = cit.edu.workforcehub.presentation.theme.AppColors.white.copy(alpha = 0.85f),
                        fontSize = 16.sp
                    )

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .height(24.dp)
                                .background(Color(0x22FFFFFF), RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(14.dp)
                                .background(Color(0x22FFFFFF), RoundedCornerShape(4.dp))
                        )
                    } else if (profileData != null) {
                        Text(
                            text = "${profileData.firstName} ${profileData.lastName}",
                            color = cit.edu.workforcehub.presentation.theme.AppColors.white,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = profileData.idNumber ?: "ID Pending",
                            color = cit.edu.workforcehub.presentation.theme.AppColors.white.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                    } else {
                        Text(
                            text = "Full Name",
                            color = cit.edu.workforcehub.presentation.theme.AppColors.white,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "ID Number",
                            color = cit.edu.workforcehub.presentation.theme.AppColors.white.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            if (showDate) {
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
                        tint = cit.edu.workforcehub.presentation.theme.AppColors.white,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Get current date formatted nicely
                    val today = LocalDate.now()
                    val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
                    val formattedDate = today.format(formatter)

                    Text(
                        text = formattedDate,
                        color = cit.edu.workforcehub.presentation.theme.AppColors.white,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
} 