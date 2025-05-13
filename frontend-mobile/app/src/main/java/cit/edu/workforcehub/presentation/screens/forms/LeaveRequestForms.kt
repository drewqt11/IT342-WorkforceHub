package cit.edu.workforcehub.presentation.screens.forms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.workforcehub.R
import cit.edu.workforcehub.api.ApiHelper
import cit.edu.workforcehub.presentation.theme.AppColors
import kotlinx.coroutines.launch
import retrofit2.Response
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class LeaveRequestData(
    val leaveType: String,
    val startDate: String,
    val endDate: String,
    val reason: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveRequestForm(
    onBackPressed: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Form state
    var leaveType by remember { mutableStateOf("") }
    var leaveTypeExpanded by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf("2025-05-08") }
    var endDate by remember { mutableStateOf("2025-05-10") }
    var reason by remember { mutableStateOf("") }
    
    // Date picker states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()
    
    // Submission state
    var isSubmitting by remember { mutableStateOf(false) }
    
    // Available leave types
    val leaveTypes = listOf("Annual Leave", "Sick Leave", "Casual Leave", "Study Leave", "Maternity Leave", "Paternity Leave", "Others")
    
    // Helper function to convert milliseconds to LocalDate
    fun millisecondsToFormattedDate(milliseconds: Long?): String {
        if (milliseconds == null) return ""
        val instant = Instant.ofEpochMilli(milliseconds)
        val date = LocalDate.ofInstant(instant, ZoneId.systemDefault())
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AppColors.gray100)
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Back button row with logo and enterprise portal text
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackPressed,
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(2.dp, RoundedCornerShape(24.dp))
                            .background(AppColors.white, RoundedCornerShape(24.dp))
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AppColors.blue500,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Logo and enterprise portal text
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(AppColors.blue500, AppColors.teal500),
                                    startX = 0f,
                                    endX = 800f
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        // Logo with shadow and glow effects
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(8.dp),
                                    spotColor = Color.Black.copy(alpha = 0.3f)
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFCCCCCC),
                                            Color(0xFFEEEEEE),
                                            Color(0xFFDDDDDD)
                                        )
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(3.dp)
                        ) {
                            // Multi-layer effect for depth
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                AppColors.blue100.copy(alpha = 0.3f),
                                                Color.White.copy(alpha = 0.9f)
                                            ),
                                            radius = 25f
                                        )
                                    )
                                    .padding(1.dp)
                                    .border(
                                        width = 0.5.dp,
                                        color = AppColors.blue100.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(1.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_with_no_text),
                                    contentDescription = "Company Logo",
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column {
                            Text(
                                text = "WORKFORCE HUB",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Enterprise Portal",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                letterSpacing = 0.25.sp
                            )
                        }
                    }
                }
                
                // Form title
                Text(
                    text = "Request Leave",
                    fontWeight = FontWeight.Bold,
                    color = AppColors.gray800,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )

                // Form Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = AppColors.blue700.copy(alpha = 0.2f)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.white
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, AppColors.gray200)
                ) {
                    // Gradient top bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(AppColors.blue500, AppColors.teal500)
                                )
                            )
                    )
                    // Section header (not scrollable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                    ) {
                        // Blue vertical indicator
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(36.dp)
                                .background(AppColors.blue500, RoundedCornerShape(2.dp))
                        )
                        // Section title and subtitle
                        Column(
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            Text(
                                text = "Leave Request Form",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.gray800
                            )
                            Text(
                                text = "Please provide the details for your leave request",
                                fontSize = 14.sp,
                                color = AppColors.gray500,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    // Scrollable form content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Leave Type Dropdown
                            Column {
                                Text(
                                    buildAnnotatedString {
                                        append("Leave Type")
                                        withStyle(style = SpanStyle(color = AppColors.red)) {
                                            append(" *")
                                        }
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.gray800,
                                    letterSpacing = 0.1.sp
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = leaveType,
                                        onValueChange = {},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(
                                                elevation = 2.dp,
                                                shape = RoundedCornerShape(12.dp),
                                                ambientColor = AppColors.gray300.copy(alpha = 0.3f)
                                            ),
                                        readOnly = true,
                                        placeholder = { 
                                            Text(
                                                "Select leave type", 
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = AppColors.gray400,
                                                letterSpacing = 0.1.sp
                                            ) 
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Select",
                                                tint = AppColors.blue600,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AppColors.blue400,
                                            unfocusedBorderColor = AppColors.gray200,
                                            focusedContainerColor = AppColors.white,
                                            unfocusedContainerColor = AppColors.white,
                                            cursorColor = AppColors.blue500,
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        textStyle = LocalTextStyle.current.copy(
                                            color = AppColors.gray900,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 0.1.sp
                                        )
                                    )
                                    
                                    // Invisible clickable box for dropdown trigger
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { leaveTypeExpanded = true }
                                    )
                                    
                                    DropdownMenu(
                                        expanded = leaveTypeExpanded,
                                        onDismissRequest = { leaveTypeExpanded = false },
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .background(AppColors.white)
                                    ) {
                                        leaveTypes.forEach { type ->
                                            DropdownMenuItem(
                                                text = { 
                                                    Text(
                                                        text = type,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = AppColors.gray800,
                                                        letterSpacing = 0.1.sp
                                                    ) 
                                                },
                                                onClick = {
                                                    leaveType = type
                                                    leaveTypeExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Date fields in a row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Start Date
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        buildAnnotatedString {
                                            append("Start Date")
                                            withStyle(style = SpanStyle(color = AppColors.red)) {
                                                append(" *")
                                            }
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.gray800,
                                        letterSpacing = 0.1.sp
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    OutlinedTextField(
                                        value = startDate,
                                        onValueChange = {},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(
                                                elevation = 2.dp,
                                                shape = RoundedCornerShape(12.dp),
                                                ambientColor = AppColors.gray300.copy(alpha = 0.3f)
                                            ),
                                        readOnly = true,
                                        placeholder = { 
                                            Text(
                                                "Select date", 
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = AppColors.gray400,
                                                letterSpacing = 0.1.sp
                                            ) 
                                        },
                                        trailingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .padding(4.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(AppColors.blue50)
                                                    .padding(4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                IconButton(
                                                    onClick = { showStartDatePicker = true },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CalendarMonth,
                                                        contentDescription = "Select Date",
                                                        tint = AppColors.blue600,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AppColors.blue400,
                                            unfocusedBorderColor = AppColors.gray200,
                                            focusedContainerColor = AppColors.white,
                                            unfocusedContainerColor = AppColors.white,
                                            cursorColor = AppColors.blue500
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        textStyle = LocalTextStyle.current.copy(
                                            color = AppColors.gray900,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 0.1.sp
                                        )
                                    )
                                }
                                
                                // End Date
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        buildAnnotatedString {
                                            append("End Date")
                                            withStyle(style = SpanStyle(color = AppColors.red)) {
                                                append(" *")
                                            }
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.gray800,
                                        letterSpacing = 0.1.sp
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    OutlinedTextField(
                                        value = endDate,
                                        onValueChange = {},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(
                                                elevation = 2.dp,
                                                shape = RoundedCornerShape(12.dp),
                                                ambientColor = AppColors.gray300.copy(alpha = 0.3f)
                                            ),
                                        readOnly = true,
                                        placeholder = { 
                                            Text(
                                                "Select date", 
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = AppColors.gray400,
                                                letterSpacing = 0.1.sp
                                            ) 
                                        },
                                        trailingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .padding(4.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(AppColors.blue50)
                                                    .padding(4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                IconButton(
                                                    onClick = { showEndDatePicker = true },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CalendarMonth,
                                                        contentDescription = "Select Date",
                                                        tint = AppColors.blue600,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AppColors.blue400,
                                            unfocusedBorderColor = AppColors.gray200,
                                            focusedContainerColor = AppColors.white,
                                            unfocusedContainerColor = AppColors.white,
                                            cursorColor = AppColors.blue500
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        textStyle = LocalTextStyle.current.copy(
                                            color = AppColors.gray900,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 0.1.sp
                                        )
                                    )
                                }
                            }
                            
                            // Reason
                            Column {
                                Text(
                                    buildAnnotatedString {
                                        append("Reason for Leave")
                                        withStyle(style = SpanStyle(color = AppColors.red)) {
                                            append(" *")
                                        }
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.gray800,
                                    letterSpacing = 0.1.sp
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                OutlinedTextField(
                                    value = reason,
                                    onValueChange = { reason = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 120.dp)
                                        .shadow(
                                            elevation = 2.dp,
                                            shape = RoundedCornerShape(12.dp),
                                            ambientColor = AppColors.gray300.copy(alpha = 0.3f)
                                        ),
                                    placeholder = { 
                                        Text(
                                            "Enter reason for leave request", 
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = AppColors.gray400,
                                            letterSpacing = 0.1.sp
                                        ) 
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.blue400,
                                        unfocusedBorderColor = AppColors.gray200,
                                        focusedContainerColor = AppColors.white,
                                        unfocusedContainerColor = AppColors.white,
                                        cursorColor = AppColors.blue500
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    maxLines = 5,
                                    textStyle = LocalTextStyle.current.copy(
                                        textAlign = TextAlign.Start,
                                        color = AppColors.gray900,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal,
                                        letterSpacing = 0.1.sp
                                    )
                                )
                            }
                            
                            // Information box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppColors.blue50)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Please make sure all dates are accurate. Your leave request will be reviewed by HR.",
                                    fontSize = 13.sp,
                                    color = AppColors.blue700,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
                
                // Submit button with gradient
                Button(
                    onClick = {
                        if (validateForm(leaveType, startDate, endDate, reason)) {
                            isSubmitting = true
                            scope.launch {
                                try {
                                    val employeeService = ApiHelper.getEmployeeService()
                                    val requestData = LeaveRequestData(
                                        leaveType = leaveType,
                                        startDate = startDate,
                                        endDate = endDate,
                                        reason = reason
                                    )
                                    
                                    val response = employeeService.createLeaveRequest(requestData)
                                    
                                    if (response.isSuccessful) {
                                        snackbarHostState.showSnackbar("Leave request submitted successfully!")
                                        onSuccess()
                                    } else {
                                        snackbarHostState.showSnackbar("Failed to submit request: ${response.message()}")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please fill all fields correctly")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(vertical = 8.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = AppColors.blue700.copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    enabled = !isSubmitting
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(AppColors.blue500, AppColors.teal500),
                                    start = Offset(0f, 0f),
                                    end = Offset(Float.POSITIVE_INFINITY, 0f)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "SUBMIT REQUEST",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            // Date Pickers
            if (showStartDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showStartDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                startDate = millisecondsToFormattedDate(startDatePickerState.selectedDateMillis)
                                showStartDatePicker = false
                            }
                        ) {
                            Text("OK", color = AppColors.blue500)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showStartDatePicker = false }
                        ) {
                            Text("Cancel", color = AppColors.gray600)
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
                                endDate = millisecondsToFormattedDate(endDatePickerState.selectedDateMillis)
                                showEndDatePicker = false
                            }
                        ) {
                            Text("OK", color = AppColors.blue500)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showEndDatePicker = false }
                        ) {
                            Text("Cancel", color = AppColors.gray600)
                        }
                    }
                ) {
                    DatePicker(state = endDatePickerState)
                }
            }
        }
    }
}

// Validate form fields
private fun validateForm(leaveType: String, startDate: String, endDate: String, reason: String): Boolean {
    return leaveType.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank() && reason.isNotBlank()
}

@Preview(showBackground = true)
@Composable
fun LeaveRequestFormPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.gray100
    ) {
        LeaveRequestForm(onBackPressed = {})
    }
}


