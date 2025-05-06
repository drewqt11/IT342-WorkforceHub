package cit.edu.workforcehub.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import cit.edu.workforcehub.api.ApiHelper
import cit.edu.workforcehub.api.models.EmployeeProfile
import cit.edu.workforcehub.api.models.LeaveRequest
import cit.edu.workforcehub.presentation.components.AppHeader
import cit.edu.workforcehub.presentation.components.AppScreen
import cit.edu.workforcehub.presentation.components.LoadingComponent
import cit.edu.workforcehub.presentation.components.UniversalDrawer
import cit.edu.workforcehub.presentation.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveRequestScreen(
    onLogout: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToLeaveRequests: () -> Unit = {},
    onNavigateToPerformance: () -> Unit = {},
    onNavigateToTraining: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
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
    
    // State for form fields
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var leaveType by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    
    // State for leave request submission
    var isSubmitting by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showLeaveTypeDropdown by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Leave types
    val leaveTypes = listOf(
        "Annual Leave",
        "Sick Leave",
        "Maternity Leave",
        "Paternity Leave",
        "Bereavement Leave",
        "Vacation Leave",
        "Unpaid Leave"
    )

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
    
    // Date picker states
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()
    
    // Format dates for display
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedStartDate = startDate?.let { dateFormat.format(Date(it)) } ?: "Select date"
    val formattedEndDate = endDate?.let { dateFormat.format(Date(it)) } ?: "Select date"
    
    // Calculate leave duration (days)
    val leaveDuration = if (startDate != null && endDate != null) {
        val diff = endDate!! - startDate!!
        TimeUnit.MILLISECONDS.toDays(diff) + 1
    } else null

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = cit.edu.workforcehub.presentation.theme.AppColors.gray100
    ) {
        // Using the UniversalDrawer
        UniversalDrawer(
            drawerState = drawerState,
            currentScreen = AppScreen.LEAVE_REQUESTS,
            onLogout = onLogout,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToAttendance = onNavigateToAttendance,
            onNavigateToLeaveRequests = {}, // Already on leave requests, no need to navigate
            onNavigateToPerformance = onNavigateToPerformance,
            onNavigateToTraining = onNavigateToTraining,
            onNavigateToProfile = onNavigateToProfile
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Show loading component if data is still loading
                if (isLoading) {
                    LoadingComponent()
                } else {
                    // Content that scrolls underneath the header
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 250.dp)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Leave Request Form
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = cit.edu.workforcehub.presentation.theme.AppColors.white),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Submit Leave Request",
                                    color = cit.edu.workforcehub.presentation.theme.AppColors.gray800,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                
                                // Date Range Selection
                                Text(
                                    text = "Leave Period",
                                    color = cit.edu.workforcehub.presentation.theme.AppColors.gray700,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Start Date Picker
                                    OutlinedTextField(
                                        value = formattedStartDate,
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("Start Date") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AppColors.blue500,
                                            unfocusedBorderColor = AppColors.gray300,
                                        ),
                                        trailingIcon = {
                                            TextButton(onClick = { showStartDatePicker = true }) {
                                                Text("Select", color = AppColors.blue500)
                                            }
                                        }
                                    )
                                    
                                    // End Date Picker
                                    OutlinedTextField(
                                        value = formattedEndDate,
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("End Date") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AppColors.blue500,
                                            unfocusedBorderColor = AppColors.gray300,
                                        ),
                                        trailingIcon = {
                                            TextButton(onClick = { showEndDatePicker = true }) {
                                                Text("Select", color = AppColors.blue500)
                                            }
                                        }
                                    )
                                }
                                
                                if (leaveDuration != null) {
                                    Text(
                                        text = "Duration: $leaveDuration day${if (leaveDuration > 1) "s" else ""}",
                                        color = cit.edu.workforcehub.presentation.theme.AppColors.blue500,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                
                                // Leave Type Selection
                                Text(
                                    text = "Leave Type",
                                    color = cit.edu.workforcehub.presentation.theme.AppColors.gray700,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                ExposedDropdownMenuBox(
                                    expanded = showLeaveTypeDropdown,
                                    onExpandedChange = { showLeaveTypeDropdown = !showLeaveTypeDropdown },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = leaveType,
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("Select Leave Type") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLeaveTypeDropdown) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AppColors.blue500,
                                            unfocusedBorderColor = AppColors.gray300,
                                        )
                                    )
                                    
                                    ExposedDropdownMenu(
                                        expanded = showLeaveTypeDropdown,
                                        onDismissRequest = { showLeaveTypeDropdown = false },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        leaveTypes.forEach { type ->
                                            DropdownMenuItem(
                                                text = { Text(text = type) },
                                                onClick = {
                                                    leaveType = type
                                                    showLeaveTypeDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Reason Input
                                Text(
                                    text = "Reason for Leave",
                                    color = cit.edu.workforcehub.presentation.theme.AppColors.gray700,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                OutlinedTextField(
                                    value = reason,
                                    onValueChange = { reason = it },
                                    placeholder = { Text("Please provide details about your leave request") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.blue500,
                                        unfocusedBorderColor = AppColors.gray300,
                                    )
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Submit Button
                                Button(
                                    onClick = {
                                        scope.launch {
                                            if (startDate == null || endDate == null || leaveType.isEmpty() || reason.isEmpty()) {
                                                snackbarHostState.showSnackbar("Please fill in all fields")
                                                return@launch
                                            }
                                            
                                            if (endDate!! < startDate!!) {
                                                snackbarHostState.showSnackbar("End date cannot be earlier than start date")
                                                return@launch
                                            }
                                            
                                            isSubmitting = true
                                            
                                            try {
                                                val employeeService = ApiHelper.getEmployeeService()
                                                val leaveRequest = LeaveRequest(
                                                    employeeId = profileData?.employeeId ?: "",
                                                    startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(startDate!!)),
                                                    endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(endDate!!)),
                                                    leaveType = leaveType,
                                                    reason = reason
                                                )
                                                
                                                val response = employeeService.submitLeaveRequest(leaveRequest)
                                                
                                                if (response.isSuccessful) {
                                                    snackbarHostState.showSnackbar("Leave request submitted successfully")
                                                    // Reset form
                                                    startDate = null
                                                    endDate = null
                                                    leaveType = ""
                                                    reason = ""
                                                } else {
                                                    // Extract error message from response body if available
                                                    val errorBody = response.errorBody()?.string()
                                                    val errorMessage = if (errorBody?.contains("No leave balance found") == true) {
                                                        "No leave balance found for leave type: $leaveType. Please contact HR."
                                                    } else {
                                                        "Failed to submit leave request: ${response.code()} ${response.message()}"
                                                    }
                                                    snackbarHostState.showSnackbar(errorMessage)
                                                }
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar("Error submitting leave request: ${e.message}")
                                            } finally {
                                                isSubmitting = false
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    enabled = !isSubmitting,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.blue500,
                                        disabledContainerColor = AppColors.gray300
                                    )
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(
                                            color = AppColors.white,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "Submit Request",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Info Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = cit.edu.workforcehub.presentation.theme.AppColors.white),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(cit.edu.workforcehub.presentation.theme.AppColors.blue50, cit.edu.workforcehub.presentation.theme.AppColors.teal50),
                                            start = Offset(0f, 0f),
                                            end = Offset(1000f, 0f)
                                        )
                                    )
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Leave Request Information",
                                        color = cit.edu.workforcehub.presentation.theme.AppColors.gray800,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    Text(
                                        text = "Please submit your leave request at least 7 days in advance. For sick leave, please submit as soon as possible and provide relevant documentation.",
                                        color = cit.edu.workforcehub.presentation.theme.AppColors.gray700,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(bottom = 16.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    Text(
                                        text = "Available Leave Types: Annual, Sick, Maternity, Paternity, Bereavement, Vacation, and Unpaid Leave.",
                                        color = cit.edu.workforcehub.presentation.theme.AppColors.blue700,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 16.dp),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = cit.edu.workforcehub.presentation.theme.AppColors.blue500,
                                                shape = RoundedCornerShape(24.dp)
                                            )
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "HR will review your request",
                                            color = cit.edu.workforcehub.presentation.theme.AppColors.white,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Spacer to ensure content doesn't get cut off at the bottom
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }

                // Fixed header on top (doesn't scroll)
                AppHeader(
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    modifier = Modifier.zIndex(1f), // Ensure header stays on top
                    onProfileClick = onNavigateToProfile,
                    forceAutoFetch = true // Let AppHeader handle profile data fetching
                )
                
                // Snackbar host
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
                    snackbar = { snackbarData ->
                        Snackbar(
                            modifier = Modifier.padding(8.dp),
                            shape = RoundedCornerShape(8.dp),
                            containerColor = AppColors.gray800,
                            contentColor = AppColors.white,
                            action = {
                                if (snackbarData.visuals.actionLabel != null) {
                                    TextButton(onClick = { snackbarData.performAction() }) {
                                        Text(
                                            text = snackbarData.visuals.actionLabel ?: "",
                                            color = AppColors.blue100
                                        )
                                    }
                                }
                            }
                        ) {
                            Text(snackbarData.visuals.message)
                        }
                    }
                )
            }
        }
        
        // Date pickers dialogs
        if (showStartDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            startDatePickerState.selectedDateMillis?.let {
                                startDate = it
                            }
                            showStartDatePicker = false
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showStartDatePicker = false }
                    ) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = startDatePickerState)
            }
        }
        
        if (showEndDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            endDatePickerState.selectedDateMillis?.let {
                                endDate = it
                            }
                            showEndDatePicker = false
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEndDatePicker = false }
                    ) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = endDatePickerState)
            }
        }
    }
}