package cit.edu.workforcehub.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.workforcehub.R
import cit.edu.workforcehub.presentation.theme.AppColors
import kotlinx.coroutines.launch

/**
 * Enum class that defines the different screens in the app
 * Used to determine which menu item should be highlighted
 */
enum class AppScreen {
    DASHBOARD,
    TIME_ATTENDANCE,
    LEAVE_REQUESTS,
    PERFORMANCE,
    TRAINING,
    PROFILE,
    ENROLLMENT
}

/**
 * UniversalDrawer is a composable function that provides a consistent navigation drawer
 * across all screens. It handles the drawer state and navigation logic internally.
 * 
 * @param drawerState the state of the drawer (open/closed)
 * @param currentScreen the current screen to highlight the appropriate menu item
 * @param onLogout function to call when logging out
 * @param onNavigateToDashboard function to navigate to dashboard
 * @param onNavigateToAttendance function to navigate to time & attendance
 * @param onNavigateToLeaveRequests function to navigate to leave requests
 * @param onNavigateToPerformance function to navigate to performance
 * @param onNavigateToTraining function to navigate to training
 * @param onNavigateToProfile function to navigate to profile
 * @param content the content to display in the main area of the screen
 */
@Composable
fun UniversalDrawer(
    drawerState: DrawerState,
    currentScreen: AppScreen,
    onLogout: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToLeaveRequests: () -> Unit,
    onNavigateToPerformance: () -> Unit,
    onNavigateToTraining: () -> Unit,
    onNavigateToProfile: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SideBarMenu(
                currentScreen = currentScreen,
                drawerState = drawerState,
                onLogout = onLogout,
                onNavigateToDashboard = onNavigateToDashboard,
                onNavigateToAttendance = onNavigateToAttendance,
                onNavigateToLeaveRequests = onNavigateToLeaveRequests,
                onNavigateToPerformance = onNavigateToPerformance,
                onNavigateToTraining = onNavigateToTraining,
                onNavigateToProfile = onNavigateToProfile
            )
        },
        content = content
    )
}

/**
 * Data class representing a menu section with items
 */
data class MenuSection(
    val title: String? = null,
    val items: List<MenuItem>
)

/**
 * Data class representing a menu item
 */
data class MenuItem(
    val title: String,
    val icon: Int,
    val screen: AppScreen? = null,
    val onClick: () -> Unit,
    val hasSubmenu: Boolean = false,
    val submenuItems: List<MenuItem> = emptyList()
)

/**
 * SideBarMenu is a composable function that displays a navigation drawer menu
 * It handles all navigation logic internally
 * 
 * @param currentScreen the current screen being displayed to highlight the appropriate menu item
 * @param drawerState the state of the drawer (open/closed)
 */
@Composable
fun SideBarMenu(
    currentScreen: AppScreen,
    drawerState: DrawerState,
    onLogout: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToLeaveRequests: () -> Unit,
    onNavigateToPerformance: () -> Unit,
    onNavigateToTraining: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // State to track which menu item is expanded
    var expandedItemTitle by remember { mutableStateOf<String?>(null) }
    
    // Define menu sections and items
    val mainMenuItems = listOf(
        MenuItem(
            title = "Dashboard",
            icon = R.drawable.dashboard2,
            screen = AppScreen.DASHBOARD,
            onClick = {
                scope.launch {
                    drawerState.close()
                    onNavigateToDashboard()
                }
            }
        )
    )
    
    val myProfileMenuItem = MenuItem(
        title = "My Profile",
        icon = R.drawable.user,
        screen = AppScreen.PROFILE,
        onClick = {
            scope.launch {
                drawerState.close()
                onNavigateToProfile()
            }
        }
    )
    
    val benefitsMenuItem = MenuItem(
        title = "Benefits",
        icon = R.drawable.heart,
        screen = null,
        onClick = { /* This will be handled by submenu expansion */ },
        hasSubmenu = true,
        submenuItems = listOf(
            MenuItem(
                title = "Philhealth",
                icon = R.drawable.heart,
                screen = null,
                onClick = { /* Navigate to Philhealth Benefits */ }
            ),
            MenuItem(
                title = "Social Security",
                icon = R.drawable.heart,
                screen = null,
                onClick = { /* Navigate to Social Security Benefits */ }
            ),
            MenuItem(
                title = "Pag-ibig",
                icon = R.drawable.heart,
                screen = null,
                onClick = { /* Navigate to Pag-ibig Benefits */ }
            ),
            MenuItem(
                title = "Other Benefits",
                icon = R.drawable.heart,
                screen = null,
                onClick = { /* Navigate to Other Benefits */ }
            )
        )
    )
    
    val workScheduleItems = listOf(
        MenuItem(
            title = "Attendance Logs",
            icon = R.drawable.clock,
            screen = AppScreen.TIME_ATTENDANCE,
            onClick = {
                scope.launch {
                    drawerState.close()
                    onNavigateToAttendance()
                }
            }
        ),
        MenuItem(
            title = "Schedule",
            icon = R.drawable.calendar,
            screen = null,
            onClick = { /* Navigate to Schedule */ }
        ),
        MenuItem(
            title = "Requests",
            icon = R.drawable.calendarcheck,
            screen = null,
            onClick = { /* This will be handled by submenu expansion */ },
            hasSubmenu = true,
            submenuItems = listOf(
                MenuItem(
                    title = "Leave",
                    icon = R.drawable.time,
                    screen = AppScreen.LEAVE_REQUESTS,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onNavigateToLeaveRequests()
                        }
                    }
                ),
                MenuItem(
                    title = "Overtime",
                    icon = R.drawable.time,
                    screen = null,
                    onClick = { /* Navigate to Overtime Requests */ }
                ),
                MenuItem(
                    title = "Reimbursement",
                    icon = R.drawable.time,
                    screen = null,
                    onClick = { /* Navigate to Reimbursement Requests */ }
                )
            )
        )
    )
    
    val careerDevItems = listOf(
        MenuItem(
            title = "Training & Events",
            icon = R.drawable.graduation,
            screen = AppScreen.TRAINING,
            onClick = {
                scope.launch {
                    drawerState.close()
                    onNavigateToTraining()
                }
            }
        ),
        MenuItem(
            title = "Careers",
            icon = R.drawable.baggage,
            screen = null,
            onClick = { /* Navigate to Careers */ }
        ),
        MenuItem(
            title = "My Applications",
            icon = R.drawable.edit,
            screen = null,
            onClick = { /* This will be handled by submenu expansion */ },
            hasSubmenu = true,
            submenuItems = listOf(
                MenuItem(
                    title = "Benefits",
                    icon = R.drawable.heart,
                    screen = null,
                    onClick = { /* Navigate to Benefits Applications */ }
                ),
                MenuItem(
                    title = "Jobs",
                    icon = R.drawable.baggage,
                    screen = null,
                    onClick = { /* Navigate to Job Applications */ }
                ),
                MenuItem(
                    title = "Training & Events",
                    icon = R.drawable.graduation,
                    screen = null,
                    onClick = { /* Navigate to Training Applications */ }
                )
            )
        )
    )
    
    val perfFeedbackItems = listOf(
        MenuItem(
            title = "Performance Evaluation",
            icon = R.drawable.performance_icon,
            screen = AppScreen.PERFORMANCE,
            onClick = {
                scope.launch {
                    drawerState.close()
                    onNavigateToPerformance()
                }
            }
        ),
        MenuItem(
            title = "Improvement Plan",
            icon = R.drawable.circle,
            screen = null,
            onClick = { /* Navigate to Improvement Plan */ }
        ),
        MenuItem(
            title = "Feedbacks & Complaints",
            icon = R.drawable.chat,
            screen = null,
            onClick = { /* Navigate to Feedbacks & Complaints */ }
        ),
        MenuItem(
            title = "Sanction Reports",
            icon = R.drawable.warning,
            screen = null,
            onClick = { /* Navigate to Sanction Reports */ }
        )
    )
    
    val logoutMenuItem = MenuItem(
        title = "Logout",
        icon = R.drawable.logout_icon,
        screen = null,
        onClick = {
            scope.launch {
                drawerState.close()
                onLogout()
            }
        }
    )
    
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp),
        color = AppColors.blue50,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(AppColors.blue50)
        ) {
            // Fixed Drawer header with app logo and title - This part won't scroll
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.blue50)
                    .padding(top = 26.dp, bottom = 16.dp, start = 36.dp, end = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(10.dp),
                                spotColor = Color.Gray.copy(alpha = 0.3f)
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(5.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_with_no_text),
                            contentDescription = "Workforce Hub Logo",
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "Workforce Hub",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppColors.blue900
                        )
                        
                        Text(
                            text = "Enterprise Portal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.blue700
                        )
                    }
                }
            }
            
            // Divider between fixed header and scrollable content
            Divider(
                color = AppColors.gray200,
                thickness = 2.dp,
            )
            
            // Scrollable menu items starting from Dashboard
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(vertical = 8.dp)
            ) {
                // Dashboard item with special styling to match the image
                mainMenuItems.forEach { item ->
                    val isMenuItemSelected = currentScreen == item.screen
                    MenuItemRow(
                        item = item,
                        isSelected = isMenuItemSelected,
                        tint = AppColors.blue700,
                        currentScreen = currentScreen,
                        expandedItemTitle = expandedItemTitle,
                        onExpandedItemChange = { title -> expandedItemTitle = title }
                    )
                }
                
                // MAIN section
                SectionHeader(title = "MAIN")
                
                // Profile menu item explicit selection check
                val isProfileSelected = currentScreen == myProfileMenuItem.screen
                MenuItemRow(
                    item = myProfileMenuItem,
                    isSelected = isProfileSelected,
                    tint = AppColors.blue700,
                    currentScreen = currentScreen,
                    expandedItemTitle = expandedItemTitle,
                    onExpandedItemChange = { title -> expandedItemTitle = title }
                )
                
                // Check if any submenu items are selected for the benefits menu
                val hasBenefitsActiveSubmenu = benefitsMenuItem.hasSubmenu && 
                    benefitsMenuItem.submenuItems.any { it.screen == currentScreen }
                MenuItemRow(
                    item = benefitsMenuItem,
                    isSelected = false, // Benefits doesn't have a screen
                    hasActiveSubmenuItem = hasBenefitsActiveSubmenu,
                    tint = AppColors.blue700,
                    currentScreen = currentScreen,
                    expandedItemTitle = expandedItemTitle,
                    onExpandedItemChange = { title -> expandedItemTitle = title }
                )
                
                // WORK & SCHEDULE section
                SectionHeader(title = "WORK & SCHEDULE")
                
                workScheduleItems.forEach { item ->
                    // Check if the main item or any of its submenu items are selected
                    val hasSelectedSubmenuItem = item.hasSubmenu && item.submenuItems.any { 
                        subItem -> currentScreen == subItem.screen 
                    }
                    val isWorkItemSelected = currentScreen == item.screen || hasSelectedSubmenuItem
                    
                    MenuItemRow(
                        item = item,
                        isSelected = isWorkItemSelected,
                        hasActiveSubmenuItem = hasSelectedSubmenuItem,
                        tint = AppColors.blue700,
                        currentScreen = currentScreen,
                        expandedItemTitle = expandedItemTitle,
                        onExpandedItemChange = { title -> expandedItemTitle = title }
                    )
                }
                
                // CAREER & DEVELOPMENT section
                SectionHeader(title = "CAREER & DEVELOPMENT")
                
                careerDevItems.forEach { item ->
                    val hasSelectedCareerSubmenuItem = item.hasSubmenu && 
                        item.submenuItems.any { subItem -> currentScreen == subItem.screen }
                    val isCareerItemSelected = currentScreen == item.screen || hasSelectedCareerSubmenuItem
                    
                    MenuItemRow(
                        item = item,
                        isSelected = isCareerItemSelected,
                        hasActiveSubmenuItem = hasSelectedCareerSubmenuItem,
                        tint = AppColors.blue700,
                        currentScreen = currentScreen,
                        expandedItemTitle = expandedItemTitle,
                        onExpandedItemChange = { title -> expandedItemTitle = title }
                    )
                }
                
                // PERFORMANCE & FEEDBACK section
                SectionHeader(title = "PERFORMANCE & FEEDBACK")
                
                perfFeedbackItems.forEach { item ->
                    val hasSelectedPerfSubmenuItem = item.hasSubmenu && 
                        item.submenuItems.any { subItem -> currentScreen == subItem.screen }
                    val isPerfItemSelected = currentScreen == item.screen || hasSelectedPerfSubmenuItem
                    
                    MenuItemRow(
                        item = item,
                        isSelected = isPerfItemSelected,
                        hasActiveSubmenuItem = hasSelectedPerfSubmenuItem,
                        tint = AppColors.blue700,
                        currentScreen = currentScreen,
                        expandedItemTitle = expandedItemTitle,
                        onExpandedItemChange = { title -> expandedItemTitle = title }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Add divider before logout
                Divider(
                    color = AppColors.gray200,
                    thickness = 2.dp,
                )
                
                // Logout item at the bottom
                MenuItemRow(
                    item = logoutMenuItem,
                    isSelected = false,
                    hasActiveSubmenuItem = false,
                    tint = AppColors.red,
                    currentScreen = currentScreen,
                    textColor = AppColors.red,
                    expandedItemTitle = expandedItemTitle,
                    onExpandedItemChange = { title -> expandedItemTitle = title }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Section header for menu categories
 */
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = AppColors.gray700,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

/**
 * MenuItemRow is a composable that represents a single navigation item in the menu
 */
@Composable
fun MenuItemRow(
    item: MenuItem,
    isSelected: Boolean = false,
    hasActiveSubmenuItem: Boolean = false,
    tint: Color = AppColors.blue700,
    currentScreen: AppScreen? = null,
    textColor: Color = AppColors.blue700,
    expandedItemTitle: String? = null,
    onExpandedItemChange: (String?) -> Unit = {}
) {
    // Auto-expand submenu if any of its items are selected
    val shouldBeExpanded = item.hasSubmenu && 
        (item.submenuItems.any { it.screen == currentScreen } || 
        hasActiveSubmenuItem || expandedItemTitle == item.title)
    
    var expanded by remember { mutableStateOf(shouldBeExpanded) }
    
    // Update expanded state when expandedItemTitle changes
    LaunchedEffect(expandedItemTitle) {
        expanded = shouldBeExpanded
    }
    
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "rotation"
    )
    
    Column {
        // Using a Box with background and then clickable Row inside to ensure proper rendering
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .shadow(
                    elevation = if (isSelected || (expanded && item.hasSubmenu)) 4.dp else 0.dp,
                    shape = RoundedCornerShape(8.dp),
                    spotColor = AppColors.gray900
                )
                .background(
                    color = if (isSelected || (expanded && item.hasSubmenu)) Color.White else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (item.hasSubmenu) {
                            if (expanded) {
                                // If already expanded, close it
                                onExpandedItemChange(null)
                            } else {
                                // If not expanded, expand it and close others
                                onExpandedItemChange(item.title)
                            }
                            expanded = !expanded
                        } else {
                            // For regular menu items, just execute onClick
                            item.onClick()
                        }
                    }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = item.icon),
                    contentDescription = item.title,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected || hasActiveSubmenuItem || expanded) FontWeight.Bold else FontWeight.SemiBold,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                
                if (item.hasSubmenu) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_down),
                        contentDescription = "Expand",
                        tint = tint,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(rotationState)
                    )
                }
            }
        }
        
        // Submenu items
        if (item.hasSubmenu) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.blue50)
                        .padding(top = 4.dp, bottom = 4.dp, start = 55.dp, end = 16.dp)
                ) {
                    // Add a blue vertical line on the left
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Vertical blue indicator line
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height((item.submenuItems.size * 44).dp)
                                .background(AppColors.blue100)
                                .align(Alignment.CenterStart)
                                .offset(x = (-48).dp)
                        )
                        
                        // Submenu items column
                        Column {
                            item.submenuItems.forEach { subItem ->
                                val isSubitemSelected = currentScreen == subItem.screen
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .shadow(
                                            elevation = if (isSubitemSelected) 4.dp else 0.dp,
                                            shape = RoundedCornerShape(8.dp),
                                            spotColor = AppColors.gray900
                                        )
                                        .background(
                                            color = if (isSubitemSelected) Color.White else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { subItem.onClick() }
                                ) {
                                    Text(
                                        text = subItem.title,
                                        fontSize = 14.sp,
                                        color = AppColors.blue700,
                                        fontWeight = if (isSubitemSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp, horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Remove dividers to match the image
        if (!isSelected && !item.hasSubmenu) {
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
} 