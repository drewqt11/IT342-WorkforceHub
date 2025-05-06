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
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import android.view.MotionEvent
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.DisposableEffect
import kotlin.math.abs
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

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
 */
@Composable
fun AppHeader(
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    providedFirstName: String = "",
    providedLastName: String = "",
    providedRole: String = "Employee",
    forceAutoFetch: Boolean = false,
    onProfileClick: () -> Unit = {}
) {
    // State for profile data
    var profileData by remember { mutableStateOf<EmployeeProfile?>(null) }
    var isLoading by remember { mutableStateOf(providedFirstName.isEmpty() || providedLastName.isEmpty() || forceAutoFetch) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // State for dropdown menu
    var isMenuVisible by remember { mutableStateOf(false) }
    
    // Context for toast messages
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    
    // Create a simpler global overlay touch handler that doesn't block interaction
    val globalTouchHandler = remember {
        object : View.OnTouchListener {
            private var startY = 0f
            private var startX = 0f
            
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (!isMenuVisible) return false
                
                try {
                    // Get header location on screen
                    val headerLocation = IntArray(2)
                    val contentView = (context as? Activity)?.findViewById<View>(android.R.id.content)
                    contentView?.getLocationOnScreen(headerLocation)
                    val headerTop = headerLocation[1]
                    val headerBottom = headerTop + (70 * context.resources.displayMetrics.density).toInt()
                    
                    // Check if touch is within header bounds
                    val eventRawY = event?.rawY ?: 0f
                    val isTouchInHeader = eventRawY >= headerTop.toFloat() && eventRawY <= headerBottom.toFloat()
                    
                    // Don't close menu if interacting with the header
                    if (isTouchInHeader) {
                        return false
                    }
                    
                    // Check if touch is within menu bounds (approximate)
                    val menuRight = context.resources.displayMetrics.widthPixels - (16 * context.resources.displayMetrics.density).toInt()
                    val menuLeft = menuRight - (220 * context.resources.displayMetrics.density).toInt()
                    val menuTop = headerBottom
                    val menuBottom = menuTop + (150 * context.resources.displayMetrics.density).toInt()
                    
                    val eventRawX = event?.rawX ?: 0f
                    val isTouchInMenu = event != null && 
                            eventRawX >= menuLeft.toFloat() && eventRawX <= menuRight.toFloat() && 
                            eventRawY >= menuTop.toFloat() && eventRawY <= menuBottom.toFloat()
                    
                    // Don't close menu if interacting with the menu itself
                    if (isTouchInMenu) {
                        return false
                    }
                } catch (e: Exception) {
                    // If we can't determine bounds, proceed with normal behavior
                }
                
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startY = event.y
                        startX = event.x
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaY = abs(event.y - startY)
                        val deltaX = abs(event.x - startX)
                        
                        // If user scrolls more than 8dp, close the menu
                        if (deltaY > 8 || deltaX > 8) {
                            isMenuVisible = false
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        isMenuVisible = false
                    }
                }
                return false // Don't consume the event, allow it to pass through
            }
        }
    }
    
    // Apply the touch listener to the activity's window
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                activity?.window?.decorView?.setOnTouchListener(globalTouchHandler)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        activity?.window?.decorView?.setOnTouchListener(globalTouchHandler)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            activity?.window?.decorView?.setOnTouchListener(null)
        }
    }
    
    // Monitor scroll events globally by observing scroll state changes
    LaunchedEffect(isMenuVisible) {
        if (isMenuVisible) {
            // Cancel menu when any scrolling is detected
            val scrollListener = View.OnScrollChangeListener { _, _, _, _, _ ->
                if (isMenuVisible) {
                    isMenuVisible = false
                }
            }
            
            // Try to find scrollable views and attach listeners
            activity?.window?.decorView?.findScrollableChildren()?.forEach { scrollView ->
                scrollView.setOnScrollChangeListener(scrollListener)
            }
        }
    }
    
    // Only fetch profile data if names aren't provided or forceAutoFetch is true
    val shouldFetch = (providedFirstName.isEmpty() || providedLastName.isEmpty() || forceAutoFetch)
    
    // Handle back press to dismiss menu
    BackHandler(enabled = isMenuVisible) {
        isMenuVisible = false
    }
    
    // Close menu when clicking outside
    val interactionSource = remember { MutableInteractionSource() }
    
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
                color = Color(0xFFE0E0E0),
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

            // Profile button on the right
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White)
                    .padding(start = 3.dp, end = 6.dp, top = 3.dp, bottom = 3.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = rememberRipple(bounded = true),
                        onClick = {
                            isMenuVisible = !isMenuVisible
                        }
                    ),
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
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Dropdown Menu",
                    tint = AppColors.blue500,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
    
    // Dropdown menu
    SmallMenu(
        isVisible = isMenuVisible,
        onDismiss = { 
            // Explicitly set to false when dismissed
            isMenuVisible = false 
        },
        onLogout = {
            coroutineScope.launch {
                try {
                    val result = ApiHelper.logout()
                    if (result.isSuccess) {
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        // Execute the onProfileClick callback which should handle navigation in most cases
                        onProfileClick()
                    } else {
                        Toast.makeText(context, "Logout failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AppHeaderPreview() {
    androidx.compose.foundation.layout.Column {
        AppHeader(providedFirstName = "Andri", providedLastName = "Apas")
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AppHeaderMinimalPreview() {
    androidx.compose.foundation.layout.Column {
        AppHeader(providedFirstName = "", providedLastName = "")
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AppHeaderAutoFetchPreview() {
    androidx.compose.foundation.layout.Column {
        AppHeader(forceAutoFetch = true)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AppHeaderWithDropdownPreview() {
    androidx.compose.foundation.layout.Column {
        AppHeader(
            providedFirstName = "Andri",
            providedLastName = "Apas",
            providedRole = "Employee"
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AppHeaderWithMenuPreview() {
    androidx.compose.foundation.layout.Column {
        AppHeader(
            providedFirstName = "Louie James",
            providedLastName = "Carbungco",
            providedRole = "Employee"
        )
    }
}

// Helper function to find scrollable views
private fun View.findScrollableChildren(): List<View> {
    val scrollableViews = mutableListOf<View>()
    if (this is android.widget.ScrollView || 
        this is androidx.core.widget.NestedScrollView ||
        this is androidx.recyclerview.widget.RecyclerView) {
        scrollableViews.add(this)
    }
    
    if (this is ViewGroup) {
        for (i in 0 until childCount) {
            scrollableViews.addAll(getChildAt(i).findScrollableChildren())
        }
    }
    
    return scrollableViews
}