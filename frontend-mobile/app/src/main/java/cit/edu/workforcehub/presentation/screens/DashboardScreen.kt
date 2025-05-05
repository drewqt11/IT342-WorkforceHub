package cit.edu.workforcehub.presentation.screens

import android.os.Build
import android.widget.ToggleButton
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import cit.edu.workforcehub.presentation.components.AppScreen
import cit.edu.workforcehub.presentation.components.UniversalDrawer
import cit.edu.workforcehub.presentation.theme.AppColors
import cit.edu.workforcehub.api.models.EmployeeProfile
import cit.edu.workforcehub.api.ApiHelper
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import cit.edu.workforcehub.presentation.viewmodels.DashboardViewModel
import cit.edu.workforcehub.presentation.viewmodels.DashboardViewModelFactory
import cit.edu.workforcehub.presentation.components.LoadingComponent
import cit.edu.workforcehub.presentation.components.NotificationDialog
import cit.edu.workforcehub.presentation.components.NotificationType
import cit.edu.workforcehub.presentation.theme.AppTheme
import cit.edu.workforcehub.presentation.components.AppHeader
import cit.edu.workforcehub.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            application = LocalContext.current.applicationContext as android.app.Application,
            owner = LocalSavedStateRegistryOwner.current
        )
    ),
    onLogout: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToLeaveRequests: () -> Unit = {},
    onNavigateToPerformance: () -> Unit = {},
    onNavigateToTraining: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // For animated decorative elements
    val infiniteTransition = rememberInfiniteTransition(label = "circle_animation")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha_animation"
    )
    
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000),
            repeatMode = RepeatMode.Restart
        ), label = "rotation_animation"
    )
    
    // State for drawer
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Scroll state for the content - persisted across recompositions but not across process deaths
    val scrollState = rememberScrollState()

    // State for profile data
    var profileData by remember { mutableStateOf<EmployeeProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // State for time tracking - using rememberSaveable to persist across activity recreations
    var isClockedIn by rememberSaveable { mutableStateOf(viewModel.isClockedIn.value ?: false) }
    var currentTime by rememberSaveable { mutableStateOf(viewModel.currentTime.value ?: "00:00:00") }
    var hoursWorked by rememberSaveable { mutableStateOf(viewModel.hoursWorked.value ?: "00:00:00") }
    var breakTime by rememberSaveable { mutableStateOf(viewModel.breakTime.value ?: "00:00:00") }
    var currentDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    
    // Observe changes from viewModel
    val viewModelIsClockedIn by viewModel.isClockedIn.observeAsState(false)
    val viewModelCurrentTime by viewModel.currentTime.observeAsState("00:00:00")
    val viewModelHoursWorked by viewModel.hoursWorked.observeAsState("00:00:00")
    val viewModelBreakTime by viewModel.breakTime.observeAsState("00:00:00")
    
    // New state for API operations
    val apiError by viewModel.error.observeAsState(null)
    
    // Observe showNotification state for notification display
    val showNotification by viewModel.showNotification.observeAsState(false)
    val notificationTitle by viewModel.notificationTitle.observeAsState("")
    val notificationMessage by viewModel.notificationMessage.observeAsState("")
    val notificationType by viewModel.notificationType.observeAsState(NotificationType.INFO)
    
    // Sync UI state with viewModel when values change
    LaunchedEffect(viewModelIsClockedIn, viewModelCurrentTime, viewModelHoursWorked, viewModelBreakTime) {
        isClockedIn = viewModelIsClockedIn
        currentTime = viewModelCurrentTime
        hoursWorked = viewModelHoursWorked
        breakTime = viewModelBreakTime
    }
    
    // Update current time every second
    LaunchedEffect(key1 = true) {
        while(true) {
            val now = LocalTime.now()
            val formattedTime = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}:${now.second.toString().padStart(2, '0')}"
            viewModel.updateCurrentTime(formattedTime)
            delay(1000)
        }
    }
    
    // Save state changes to viewModel
    LaunchedEffect(isClockedIn, currentTime, hoursWorked, breakTime) {
        viewModel.updateClockState(isClockedIn)
        viewModel.updateCurrentTime(currentTime)
        viewModel.updateHoursWorked(hoursWorked)
        viewModel.updateBreakTime(breakTime)
    }
    
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

    // Remember active break using rememberSaveable
    var activeBreak by rememberSaveable { mutableStateOf<String?>(viewModel.activeBreak.value) }
    
    // Observe activeBreak from viewModel
    val viewModelActiveBreak by viewModel.activeBreak.observeAsState(null)
    
    // Sync UI state with viewModel
    LaunchedEffect(viewModelActiveBreak) {
        activeBreak = viewModelActiveBreak
    }
    
    // Save state changes to viewModel
    LaunchedEffect(activeBreak) {
        viewModel.updateActiveBreak(activeBreak)
    }

    // Show notification dialog if needed
    if (showNotification) {
        AppTheme {
            NotificationDialog(
                title = notificationTitle,
                message = notificationMessage,
                type = notificationType,
                onDismiss = { viewModel.dismissNotification() }
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = cit.edu.workforcehub.presentation.theme.AppColors.gray50
    ) {
        // Using the Universal Drawer instead of directly using ModalNavigationDrawer
        UniversalDrawer(
            drawerState = drawerState,
            currentScreen = AppScreen.DASHBOARD,
            onLogout = {
                // Clear saved state on logout
                viewModel.clearState()
                onLogout()
            },
            onNavigateToDashboard = {}, // Already on dashboard, no need to navigate
            onNavigateToAttendance = onNavigateToAttendance,
            onNavigateToLeaveRequests = onNavigateToLeaveRequests,
            onNavigateToPerformance = onNavigateToPerformance,
            onNavigateToTraining = onNavigateToTraining,
            onNavigateToProfile = onNavigateToProfile
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
                
                // Animated circle elements
                Canvas(
                    modifier = Modifier
                        .size(300.dp)
                        .align(Alignment.Center)
                        .alpha(0.03f)
                ) {
                    // Outer circle
                    drawCircle(
                        color = cit.edu.workforcehub.presentation.theme.AppColors.blue500,
                        radius = size.width / 2,
                        style = Stroke(width = 2f)
                    )
                    
                    // Middle circle with rotation
                    withTransform({
                        rotate(rotationAngle, pivot = Offset(size.width / 2, size.height / 2))
                    }) {
                        drawCircle(
                            color = cit.edu.workforcehub.presentation.theme.AppColors.teal500,
                            radius = size.width / 3,
                            style = Stroke(width = 1.5f)
                        )
                    }
                    
                    // Inner circle with opposite rotation
                    withTransform({
                        rotate(-rotationAngle, pivot = Offset(size.width / 2, size.height / 2))
                    }) {
                        drawCircle(
                            color = cit.edu.workforcehub.presentation.theme.AppColors.blue700,
                            radius = size.width / 4,
                            style = Stroke(width = 1f)
                        )
                    }
                }
                
                // Scrollable content (cards but not header)
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Show loading component if data is still loading
                    if (isLoading) {
                        LoadingComponent()
                    } else {
                        // Content that scrolls underneath the header
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 235.dp) // Increased from 190.dp to move cards further down
                                .verticalScroll(scrollState),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Add some spacing before the Time Tracker Card
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Check if user account is active and show appropriate content
                            if (profileData?.status == true) {
                                // Time Tracker Card - only shown if user is active
                                TimeTrackerCardWrapper(
                                    currentDate = currentDate,
                                    currentTime = currentTime,
                                    workStatus = if (isClockedIn) "Clocked In" else "Clocked Out",
                                    hoursWorked = hoursWorked,
                                    breakTime = breakTime,
                                    isClockedIn = isClockedIn,
                                    onClockInOutClick = { 
                                        // Use viewModel to clock in or out instead of just toggling the state
                                        if (isClockedIn) {
                                            viewModel.clockOut()
                                        } else {
                                            viewModel.clockIn()
                                        }
                                    },
                                    activeBreak = activeBreak,
                                    onActiveBreakChange = { newBreakType ->
                                        // Call viewModel to manage break state
                                        if (newBreakType == null) {
                                            viewModel.endBreak()
                                        } else {
                                            viewModel.startBreak(newBreakType)
                                        }
                                    },
                                    isLoading = isLoading,
                                    error = apiError
                                )
                            } else {
                                // Card showing inactive account status
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 2.dp
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(AppColors.redLight, Color.White),
                                                    start = Offset(0f, 0f),
                                                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                                )
                                            )
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Inactive Account",
                                                tint = AppColors.red,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            
                                            Spacer(modifier = Modifier.height(16.dp))
                                            
                                            Text(
                                                text = "Account Status is Inactive",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AppColors.gray900,
                                                textAlign = TextAlign.Center
                                            )
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            Text(
                                                text = "Please contact HR to activate your account",
                                                fontSize = 14.sp,
                                                color = AppColors.gray600,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Various cards for different functionality
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                
                                // Bottom spacing
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                    
                    // Fixed header on top (doesn't scroll)
                    AppHeader(
                        profileData = profileData,
                        isLoading = isLoading,
                        onMenuClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        modifier = Modifier.zIndex(1f) // Ensure header stays on top
                    )
                }
            }
        }
    }
}

@Composable
fun TimeTrackerCardWrapper(
    currentDate: LocalDate,
    currentTime: String,
    workStatus: String,
    hoursWorked: String,
    breakTime: String,
    isClockedIn: Boolean,
    onClockInOutClick: () -> Unit,
    activeBreak: String?,
    onActiveBreakChange: (String?) -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    // Get the viewModel from the parent using the factory
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            application = LocalContext.current.applicationContext as android.app.Application,
            owner = LocalSavedStateRegistryOwner.current
        )
    )
    val clockInTime by viewModel.clockInTime.observeAsState()
    
    // Observe break taken status
    val morningBreakTaken by viewModel.morningBreakTaken.observeAsState(false)
    val lunchBreakTaken by viewModel.lunchBreakTaken.observeAsState(false)
    val afternoonBreakTaken by viewModel.afternoonBreakTaken.observeAsState(false)
    
    if (!isClockedIn) {
        CollapsedTimeTrackerCard(
            currentDate = currentDate,
            currentTime = currentTime,
            workStatus = workStatus,
            hoursWorked = hoursWorked,
            breakTime = breakTime,
            isClockedIn = isClockedIn,
            onClockInOutClick = onClockInOutClick,
            isLoading = isLoading,
            error = error,
            activeBreak = activeBreak
        )
    } else {
        ExpandedTimeTrackerCard(
            currentDate = currentDate,
            currentTime = currentTime,
            workStatus = workStatus,
            hoursWorked = hoursWorked,
            breakTime = breakTime,
            onClockOutClick = onClockInOutClick,
            activeBreak = activeBreak,
            onActiveBreakChange = onActiveBreakChange,
            isLoading = isLoading,
            error = error,
            clockInTime = clockInTime,
            morningBreakTaken = morningBreakTaken,
            lunchBreakTaken = lunchBreakTaken,
            afternoonBreakTaken = afternoonBreakTaken
        )
    }
}

@Composable
private fun CollapsedTimeTrackerCard(
    currentDate: LocalDate,
    currentTime: String,
    workStatus: String,
    hoursWorked: String,
    breakTime: String,
    isClockedIn: Boolean,
    onClockInOutClick: () -> Unit,
    isLoading: Boolean = false,
    error: String? = null,
    activeBreak: String? = null
) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
    val formattedDate = currentDate.format(formatter)
    
    // Get current time for the real-time clock
    var currentTimeState by remember { mutableStateOf(LocalTime.now()) }
    
    // Update time every second
    LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(1000)
            currentTimeState = LocalTime.now()
        }
    }
    
    val hours = currentTimeState.hour % 12
    val displayHours = if (hours == 0) "12" else hours.toString().padStart(2, '0')
    val minutes = currentTimeState.minute.toString().padStart(2, '0')
    val seconds = currentTimeState.second.toString().padStart(2, '0')
    val amPm = if (currentTimeState.hour < 12) "AM" else "PM"
    
    // Split the UI into frame and content
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppColors.gray200)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(AppColors.blue500, AppColors.teal500)
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
            )
            
            // Main content
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.White, AppColors.teal100, AppColors.blue100),
                                start = Offset(1f, 1f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                        .padding(24.dp)
                ) {
                    // Time Tracker header section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        // Clock icon with gradient border
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(AppColors.blue500, AppColors.teal500)
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_recent_history),
                                contentDescription = "Time Tracker",
                                tint = AppColors.blue500,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Title
                        Text(
                            text = "Time Tracker",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.gray900
                        )
                        
                        Text(
                            text = "Record your work hours",
                            fontSize = 14.sp,
                            color = AppColors.gray600
                        )
                    }
                    
                    // Time Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 1.dp
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            // Date display
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                                    contentDescription = "Calendar",
                                    tint = AppColors.blue500,
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = formattedDate,
                                    fontSize = 14.sp,
                                    color = AppColors.gray600
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Current time display with individual segments
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = displayHours,
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.gray900
                                )
                                
                                Text(
                                    text = ":",
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.gray900
                                )
                                
                                Text(
                                    text = minutes,
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.gray900
                                )
                                
                                Text(
                                    text = ":",
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.gray900
                                )
                                
                                Text(
                                    text = seconds,
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.gray900
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = amPm,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.gray600,
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Status Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 1.dp
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Work Status with dot
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = if (isClockedIn) AppColors.green else AppColors.gray500,
                                            shape = CircleShape
                                        )
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = "Work Status",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.gray700
                                )
                            }
                            
                            // Status badge (Clocked In/Out)
                            Text(
                                text = workStatus,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.gray700,
                                modifier = Modifier
                                    .background(
                                        color = AppColors.gray200,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Info Cards (Hours Worked and Break Time) in a Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Hours Worked Card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White // Changed from light green
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 1.dp
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(AppColors.teal50, Color.White) // teal100 to white
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                // Hours worked label with icon
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_recent_history),
                                        contentDescription = "Hours",
                                        tint = Color(0xFF4ECCA3), // Green color
                                        modifier = Modifier.size(16.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = "Hours Worked",
                                        fontSize = 13.sp,
                                        color = AppColors.gray600
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Hours worked value
                                Text(
                                    text = hoursWorked,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.gray900
                                )
                            }
                        }
                        
                        // Break Time Card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White // Changed from light blue
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 1.dp
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(AppColors.blue50, Color.White)
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                // Break time label with icon
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.coffee),
                                        contentDescription = "Break",
                                        modifier = Modifier.size(16.dp),
                                        colorFilter = ColorFilter.tint(AppColors.blue500)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = if (activeBreak != null) "Active Break" else "No Active Break",
                                        fontSize = 13.sp,
                                        color = AppColors.gray600
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Break time value - display static "00:00:00" when no active break
                                Text(
                                    text = if (activeBreak != null) breakTime else "00:00:00",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.gray900
                                )
                            }
                        }
                    }
                    
                    // Clock In/Out button
                    Button(
                        onClick = onClockInOutClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.blue500
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                            .height(56.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp
                        ),
                        enabled = !isLoading // Disable when loading
                    ) {
                        if (isLoading) {
                            // Show loading indicator
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Clock In",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = if (isLoading) "Processing..." else "Clock In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    
                    // Display error if any
                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedTimeTrackerCard(
    currentDate: LocalDate,
    currentTime: String,
    workStatus: String,
    hoursWorked: String,
    breakTime: String,
    onClockOutClick: () -> Unit,
    activeBreak: String? = null,
    onActiveBreakChange: (String?) -> Unit = {},
    isLoading: Boolean = false,
    error: String? = null,
    clockInTime: String? = null,
    morningBreakTaken: Boolean = false,
    lunchBreakTaken: Boolean = false,
    afternoonBreakTaken: Boolean = false
) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
    val formattedDate = currentDate.format(formatter)
    val dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy")
    val formattedDateValue = currentDate.format(dateFormatter)
    
    // Get current time for the real-time clock
    var currentTimeState by remember { mutableStateOf(LocalTime.now()) }
    
    // Update time every second
    LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(1000)
            currentTimeState = LocalTime.now()
        }
    }
    
    val hours = currentTimeState.hour % 12
    val displayHours = if (hours == 0) "12" else hours.toString().padStart(2, '0')
    val minutes = currentTimeState.minute.toString().padStart(2, '0')
    val seconds = currentTimeState.second.toString().padStart(2, '0')
    val amPm = if (currentTimeState.hour < 12) "AM" else "PM"
    
    // Split the UI into frame and content
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppColors.gray200)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(AppColors.blue500, AppColors.teal500)
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
            )
            
            // Main content
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.White, AppColors.teal100, AppColors.blue100),
                                start = Offset(1f, 1f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                        .padding(24.dp)
                ) {
                    // Time Tracker header section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        // Clock icon with gradient border
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(AppColors.blue500, AppColors.teal500)
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_recent_history),
                                contentDescription = "Time Tracker",
                                tint = AppColors.blue500,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Title
                        Text(
                            text = "Time Tracker",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.gray900
                        )
                        
                        Text(
                            text = "Record your work hours",
                            fontSize = 14.sp,
                            color = AppColors.gray600
                        )
                    }
                    
                    // Time Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 1.dp
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            // Date display
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                                    contentDescription = "Calendar",
                                    tint = AppColors.blue500,
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = formattedDate,
                                    fontSize = 14.sp,
                                    color = AppColors.gray600
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Current time display with individual segments
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = displayHours,
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.gray900
                                )
                                
                                Text(
                                    text = ":",
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.gray900
                                )
                                
                                Text(
                                    text = minutes,
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.gray900
                                )
                                
                                Text(
                                    text = ":",
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.gray900
                                )
                                
                                Text(
                                    text = seconds,
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.gray900
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = amPm,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.gray600,
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Work Status Card with Progress Bar
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 1.dp
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Work Status with dot
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = AppColors.teal700,
                                                shape = CircleShape
                                            )
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = "Work Status",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AppColors.gray700
                                    )
                                }
                                
                                // Status badge (Clocked In)
                                Text(
                                    text = workStatus,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.teal900,
                                    modifier = Modifier
                                        .background(
                                            color = Color(0xFFCCFBF1), // Light green
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(
                                        color = AppColors.gray200,
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .background(
                                            color = AppColors.teal700,
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Break Status Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 1.dp
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Break Status with dot
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = if (activeBreak != null) AppColors.teal700 else AppColors.gray500,
                                            shape = CircleShape
                                        )
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = "Break Status",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.gray700
                                )
                            }
                            
                            // Status badge (Working/On Break)
                            Text(
                                text = activeBreak ?: "Working",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (activeBreak != null) AppColors.teal900 else AppColors.gray700,
                                modifier = Modifier
                                    .background(
                                        color = if (activeBreak != null) Color(0xFFCCFBF1) else AppColors.gray200,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Info Cards (Hours Worked and Break Time) in a Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Hours Worked Card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White // Changed from Color(0xFFF0FFF4)
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 1.dp
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(AppColors.teal50, Color.White) // teal100 to white
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                // Hours worked label with icon
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_recent_history),
                                        contentDescription = "Hours",
                                        tint = Color(0xFF4ECCA3), // Green color
                                        modifier = Modifier.size(16.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = "Hours Worked",
                                        fontSize = 13.sp,
                                        color = AppColors.gray600
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Hours worked value - use the passed in parameter instead of hardcoded value
                                Text(
                                    text = hoursWorked,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.gray900
                                )
                            }
                        }
                        
                        // Break Time Card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White // Changed from light blue
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 1.dp
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(AppColors.blue50, Color.White)
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                // Break time label with icon
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.coffee),
                                        contentDescription = "Break",
                                        modifier = Modifier.size(16.dp),
                                        colorFilter = ColorFilter.tint(AppColors.blue500)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = if (activeBreak != null) "On Break" else "Break Time",
                                        fontSize = 13.sp,
                                        color = AppColors.gray600
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Break time value
                                Text(
                                    text = breakTime,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.gray900
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Break Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Morning Break Button
                        if (activeBreak == "Morning Break") {
                            // Active Morning Break Button (Green)
                            ActiveBreakButton(
                                breakType = "Morning Break",
                                onEndBreakClick = { onActiveBreakChange(null) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Regular Morning Break Button (Blue)
                            ButtonWithIcon(
                                text = "Morning Break",
                                onClick = { onActiveBreakChange("Morning Break") },
                                modifier = Modifier.weight(1f),
                                enabled = activeBreak == null && !morningBreakTaken,
                                breakTaken = morningBreakTaken
                            )
                        }
                        
                        // Lunch Break Button
                        if (activeBreak == "Lunch Break") {
                            // Active Lunch Break Button (Green)
                            ActiveBreakButton(
                                breakType = "Lunch Break",
                                onEndBreakClick = { onActiveBreakChange(null) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Regular Lunch Break Button (Blue)
                            ButtonWithIcon(
                                text = "Lunch Break",
                                onClick = { onActiveBreakChange("Lunch Break") },
                                modifier = Modifier.weight(1f),
                                enabled = activeBreak == null && !lunchBreakTaken,
                                breakTaken = lunchBreakTaken
                            )
                        }
                        
                        // Afternoon Break Button
                        if (activeBreak == "Afternoon Break") {
                            // Active Afternoon Break Button (Green)
                            ActiveBreakButton(
                                breakType = "Afternoon Break",
                                onEndBreakClick = { onActiveBreakChange(null) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Regular Afternoon Break Button (Blue)
                            ButtonWithIcon(
                                text = "Afternoon Break",
                                onClick = { onActiveBreakChange("Afternoon Break") },
                                modifier = Modifier.weight(1f),
                                enabled = activeBreak == null && !afternoonBreakTaken,
                                breakTaken = afternoonBreakTaken
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Attendance Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 1.dp
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(AppColors.teal50, Color.White) // teal100 to white
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            // Attendance Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                                    contentDescription = "Calendar",
                                    tint = AppColors.teal500,
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = "Today's Attendance",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.gray700
                                )
                            }
                            
                            // Attendance Grid Content
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Clock In Time
                                AttendanceItem(
                                    label = "Clock In",
                                    value = clockInTime ?: "-",
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Hours worked (calculated from clockIn till now or clockOut)
                                AttendanceItem(
                                    label = "Hours Worked",
                                    value = hoursWorked,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Status
                                AttendanceItem(
                                    label = "Status",
                                    value = workStatus,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Break time
                                AttendanceItem(
                                    label = "Break Time",
                                    value = breakTime,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    // Clock Out button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            AppColors.blue500,
                                            AppColors.teal500
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(enabled = !isLoading) { onClockOutClick() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (isLoading) {
                                    // Show loading indicator
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Clock Out",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = if (isLoading) "Processing..." else "Clock Out",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    // Display error if any
                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ButtonWithIcon(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    breakTaken: Boolean = false
) {
    // Split text by space to show on two lines (e.g., "Morning Break" -> "Morning" + "Break")
    val parts = text.split(" ", limit = 2)
    val firstLine = parts.getOrNull(0) ?: ""
    val secondLine = parts.getOrNull(1) ?: ""
    
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF3B82F6), // Bright blue color
            disabledContainerColor = if (breakTaken) Color(0xFFE5E7EB) else Color(0xFFE5E7EB) // Red-tinted when used, lighter blue when just disabled
        ),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.height(80.dp),
        enabled = enabled
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                // Coffee icon
                Image(
                    painter = painterResource(id = R.drawable.coffee),
                    contentDescription = "Coffee",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(bottom = 2.dp),
                    colorFilter = ColorFilter.tint(if (breakTaken) Color.White else Color.White)
                )
                
                // First part of text
                Text(
                    text = firstLine,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (breakTaken) Color.White else Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(0.dp)
                )
                
                // Second part of text (if exists)
                if (secondLine.isNotEmpty()) {
                    Text(
                        text = if (breakTaken) "Used" else secondLine,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (breakTaken) Color.White else Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 0.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveBreakButton(
    breakType: String,
    onEndBreakClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Split text by space to show on two lines (e.g., "Morning Break" -> "Morning" + "Break")
    val parts = breakType.split(" ", limit = 2)
    val firstLine = parts.getOrNull(0) ?: ""
    val secondLine = parts.getOrNull(1) ?: ""
    
    Button(
        onClick = onEndBreakClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.teal500 // Changed from Color(0xFF10B981) to AppColors.teal500
        ),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.height(80.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                // Coffee icon
                Image(
                    painter = painterResource(id = R.drawable.coffee),
                    contentDescription = "Coffee",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(bottom = 2.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
                
                // "End Morning" text
                Text(
                    text = "End ${firstLine}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(0.dp)
                )
                
                // "Break" text
                if (secondLine.isNotEmpty()) {
                    Text(
                        text = secondLine,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 0.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendanceItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    // Set fixed height with proper spacing
    Card(
        modifier = modifier
            .height(100.dp), // Reduced height
        colors = CardDefaults.cardColors(
            containerColor = AppColors.gray100
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp), // Reduced padding
            verticalArrangement = Arrangement.Top // Align to top
        ) {
            // Label at the top
            Text(
                text = label,
                fontSize = 11.sp, // Smaller font size
                color = AppColors.gray600,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(4.dp)) // Smaller spacing
            
            // Value with appropriate font size
            Text(
                text = value,
                fontSize = 14.sp, // Smaller font
                fontWeight = FontWeight.Medium,
                color = AppColors.gray900,
                lineHeight = 18.sp, // Smaller line height
                modifier = Modifier.fillMaxWidth() // Fill available width
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = cit.edu.workforcehub.presentation.theme.AppColors.gray50
    ) {
        DashboardScreen(
            onLogout = {},
            onNavigateToAttendance = {},
            onNavigateToLeaveRequests = {},
            onNavigateToPerformance = {},
            onNavigateToTraining = {},
            onNavigateToProfile = {}
        )
    }
} 