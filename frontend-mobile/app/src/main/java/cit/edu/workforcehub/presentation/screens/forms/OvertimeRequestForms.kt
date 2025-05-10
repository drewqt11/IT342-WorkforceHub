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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.window.Dialog
import cit.edu.workforcehub.R
import cit.edu.workforcehub.api.ApiHelper
import cit.edu.workforcehub.presentation.theme.AppColors
import kotlinx.coroutines.launch
import retrofit2.Response
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class TimeObject(
    val hour: Int,
    val minute: Int,
    val second: Int = 0,
    val nano: Int = 0
)

data class OvertimeRequestData(
    val date: String,
    val startTime: String,
    val endTime: String,
    val totalHours: Double,
    val reason: String,
    val status: String = "PENDING"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OvertimeRequestForm(
    onBackPressed: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Form state
    var date by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    
    // Time selection states
    var startHour by remember { mutableStateOf(9) }
    var startMinute by remember { mutableStateOf(0) }
    var startAmPm by remember { mutableStateOf(true) } // true = AM, false = PM
    
    var endHour by remember { mutableStateOf(5) }
    var endMinute by remember { mutableStateOf(0) }
    var endAmPm by remember { mutableStateOf(false) } // true = AM, false = PM
    
    // Dialog states
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    // Date and time picker states
    val datePickerState = rememberDatePickerState()
    
    // Convert 12-hour time to 24-hour time for the time picker
    val startHour24 = if (startAmPm) {
        if (startHour == 12) 0 else startHour
    } else {
        if (startHour == 12) 12 else startHour + 12
    }
    
    val endHour24 = if (endAmPm) {
        if (endHour == 12) 0 else endHour
    } else {
        if (endHour == 12) 12 else endHour + 12
    }
    
    val startTimePickerState = rememberTimePickerState(
        initialHour = startHour24,
        initialMinute = startMinute,
        is24Hour = false  // Explicitly set to false for 12-hour format
    )
    val endTimePickerState = rememberTimePickerState(
        initialHour = endHour24,
        initialMinute = endMinute,
        is24Hour = false  // Explicitly set to false for 12-hour format
    )
    
    // Calculate total hours
    val totalHours by remember {
        derivedStateOf {
            calculateTotalHours(
                startHour = if (startAmPm) startHour else startHour + 12,
                startMinute = startMinute,
                endHour = if (endAmPm) endHour else endHour + 12,
                endMinute = endMinute
            )
        }
    }
    
    // Submission state
    var isSubmitting by remember { mutableStateOf(false) }
    
    // Helper function to convert milliseconds to LocalDate
    fun millisecondsToFormattedDate(milliseconds: Long?): String {
        if (milliseconds == null) return ""
        val instant = Instant.ofEpochMilli(milliseconds)
        val date = LocalDate.ofInstant(instant, ZoneId.systemDefault())
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date)
    }
    
    // Format time to display
    fun formatTime(hour: Int, minute: Int, isAm: Boolean): String {
        // Convert hour to 12-hour format (1-12)
        val hour12 = when {
            hour == 0 -> 12  // 0:00 becomes 12:00 AM
            hour > 12 -> hour - 12  // 13:00 becomes 1:00 PM
            else -> hour  // 1-12 stays the same
        }
        
        // Format with leading zeros
        val formattedHour = String.format("%02d", hour12)
        val formattedMinute = String.format("%02d", minute)
        val period = if (isAm) "AM" else "PM"
        
        return "$formattedHour:$formattedMinute $period"
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
                    text = "Request Overtime",
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
                    
                    // Form header with blue indicator (moved outside of scrollable area)
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
                                text = "Overtime Request Form",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.gray800
                            )
                            Text(
                                text = "Please provide the details for your overtime request",
                                fontSize = 14.sp,
                                color = AppColors.gray500,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Form content - wrapped in a padding container
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Form fields in a column
                            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                // Date Field
                                Column {
                                    Text(
                                        buildAnnotatedString {
                                            append("Date")
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
                                        value = date,
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
                                                    onClick = { showDatePicker = true },
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
                                
                                // Start Time
                                Column {
                                    Text(
                                        buildAnnotatedString {
                                            append("Start Time")
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
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .shadow(
                                                    elevation = 2.dp,
                                                    shape = RoundedCornerShape(12.dp),
                                                    ambientColor = AppColors.gray300.copy(alpha = 0.2f)
                                                ),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = AppColors.gray200
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Icon with gradient background
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .padding(6.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(
                                                            brush = Brush.linearGradient(
                                                                colors = listOf(AppColors.blue50, AppColors.blue100),
                                                                start = Offset(0f, 0f),
                                                                end = Offset(0f, Float.POSITIVE_INFINITY)
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Timer,
                                                        contentDescription = "Start Time",
                                                        tint = AppColors.blue600,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                
                                                // Text field
                                                OutlinedTextField(
                                                    value = formatTime(startHour, startMinute, startAmPm),
                                                    onValueChange = { /* Read only */ },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(56.dp),
                                                    readOnly = true,
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = Color.Transparent,
                                                        unfocusedBorderColor = Color.Transparent,
                                                        focusedContainerColor = Color.Transparent,
                                                        unfocusedContainerColor = Color.Transparent,
                                                        cursorColor = AppColors.blue500
                                                    ),
                                                    textStyle = LocalTextStyle.current.copy(
                                                        color = AppColors.gray900,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    singleLine = true,
                                                    trailingIcon = {
                                                        // Time picker icon
                                                        Box(
                                                            modifier = Modifier
                                                                .size(40.dp)
                                                                .padding(6.dp)
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(
                                                                    brush = Brush.linearGradient(
                                                                        colors = listOf(AppColors.blue100, AppColors.blue300),
                                                                        start = Offset(0f, 0f),
                                                                        end = Offset(0f, Float.POSITIVE_INFINITY)
                                                                    )
                                                                )
                                                                .clickable { showStartTimePicker = true },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Timer,
                                                                contentDescription = "Select Time",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // End Time
                                Column {
                                    Text(
                                        buildAnnotatedString {
                                            append("End Time")
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
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .shadow(
                                                    elevation = 2.dp,
                                                    shape = RoundedCornerShape(12.dp),
                                                    ambientColor = AppColors.gray300.copy(alpha = 0.2f)
                                                ),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = AppColors.gray200
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Icon with gradient background
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .padding(6.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(
                                                            brush = Brush.linearGradient(
                                                                colors = listOf(AppColors.blue50, AppColors.blue100),
                                                                start = Offset(0f, 0f),
                                                                end = Offset(0f, Float.POSITIVE_INFINITY)
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Timer,
                                                        contentDescription = "End Time",
                                                        tint = AppColors.blue600,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                
                                                // Text field
                                                OutlinedTextField(
                                                    value = formatTime(endHour, endMinute, endAmPm),
                                                    onValueChange = { /* Read only */ },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(56.dp),
                                                    readOnly = true,
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = Color.Transparent,
                                                        unfocusedBorderColor = Color.Transparent,
                                                        focusedContainerColor = Color.Transparent,
                                                        unfocusedContainerColor = Color.Transparent,
                                                        cursorColor = AppColors.blue500
                                                    ),
                                                    textStyle = LocalTextStyle.current.copy(
                                                        color = AppColors.gray900,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    singleLine = true,
                                                    trailingIcon = {
                                                        // Time picker icon
                                                        Box(
                                                            modifier = Modifier
                                                                .size(40.dp)
                                                                .padding(6.dp)
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(
                                                                    brush = Brush.linearGradient(
                                                                        colors = listOf(AppColors.blue100, AppColors.blue300),
                                                                        start = Offset(0f, 0f),
                                                                        end = Offset(0f, Float.POSITIVE_INFINITY)
                                                                    )
                                                                )
                                                                .clickable { showEndTimePicker = true },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Timer,
                                                                contentDescription = "Select Time",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // Total Hours (calculated automatically)
                                Column {
                                    Text(
                                        buildAnnotatedString {
                                            append("Total Hours")
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
                                    
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(
                                                elevation = 2.dp,
                                                shape = RoundedCornerShape(12.dp),
                                                ambientColor = AppColors.gray300.copy(alpha = 0.3f)
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                        color = AppColors.blue50,
                                        border = BorderStroke(1.dp, AppColors.blue100)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Timer,
                                                contentDescription = "Total Hours",
                                                tint = AppColors.blue500,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            
                                            Spacer(modifier = Modifier.width(12.dp))
                                            
                                            Text(
                                                text = "${formatDecimal(totalHours)} hours",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = AppColors.blue700,
                                                letterSpacing = 0.1.sp
                                            )
                                        }
                                    }
                                }
                                
                                // Reason
                                Column {
                                    Text(
                                        buildAnnotatedString {
                                            append("Reason for Overtime")
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
                                                "Enter reason for overtime request", 
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
                                        text = "Please make sure all times are accurate. Your overtime request will be reviewed by HR.",
                                        fontSize = 13.sp,
                                        color = AppColors.blue700,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Submit button with gradient
                Button(
                    onClick = {
                        if (validateForm(date, reason, totalHours)) {
                            isSubmitting = true
                            scope.launch {
                                try {
                                    val employeeService = ApiHelper.getEmployeeService()
                                    // Convert AM/PM time to 24-hour format
                                    val start24Hour = convertTo24Hour(startHour, startMinute, startAmPm)
                                    val end24Hour = convertTo24Hour(endHour, endMinute, endAmPm)
                                    
                                    val requestData = OvertimeRequestData(
                                        date = date,
                                        startTime = formatTimeForApi(start24Hour.first, start24Hour.second),
                                        endTime = formatTimeForApi(end24Hour.first, end24Hour.second),
                                        totalHours = totalHours,
                                        reason = reason
                                    )
                                    
                                    val response = employeeService.createOvertimeRequest(requestData)
                                    
                                    if (response.isSuccessful) {
                                        snackbarHostState.showSnackbar("Overtime request submitted successfully!")
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
            
            // Date Picker Dialog
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                date = millisecondsToFormattedDate(datePickerState.selectedDateMillis)
                                showDatePicker = false
                            }
                        ) {
                            Text("OK", color = AppColors.blue500)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDatePicker = false }
                        ) {
                            Text("Cancel", color = AppColors.gray600)
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            
            // Start Time Picker Dialog
            if (showStartTimePicker) {
                TimePickerDialog(
                    onDismissRequest = { showStartTimePicker = false },
                    onConfirm = { isAm ->
                        // Extract hours and minutes from the time picker state 
                        // and convert to 12-hour format with AM/PM
                        val selectedHour = startTimePickerState.hour
                        
                        // Convert to 12-hour format
                        startHour = selectedHour % 12
                        if (startHour == 0) startHour = 12
                        
                        startMinute = startTimePickerState.minute
                        startAmPm = isAm
                        showStartTimePicker = false
                    },
                    timePickerState = startTimePickerState,
                    confirmButtonColor = AppColors.blue500,
                    dismissButtonColor = AppColors.gray600
                )
            }
            
            // End Time Picker Dialog
            if (showEndTimePicker) {
                TimePickerDialog(
                    onDismissRequest = { showEndTimePicker = false },
                    onConfirm = { isAm ->
                        // Extract hours and minutes from the time picker state
                        // and convert to 12-hour format with AM/PM
                        val selectedHour = endTimePickerState.hour
                        
                        // Convert to 12-hour format
                        endHour = selectedHour % 12
                        if (endHour == 0) endHour = 12
                        
                        endMinute = endTimePickerState.minute
                        endAmPm = isAm
                        showEndTimePicker = false
                    },
                    timePickerState = endTimePickerState,
                    confirmButtonColor = AppColors.blue500,
                    dismissButtonColor = AppColors.gray600
                )
            }
        }
    }
}

// Custom Time Picker Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Boolean) -> Unit,
    timePickerState: TimePickerState,
    confirmButtonColor: Color = AppColors.blue600,
    dismissButtonColor: Color = AppColors.gray700
) {
    // Local state to track if we're displaying AM or PM
    val isAm = remember { mutableStateOf(timePickerState.hour < 12) }
    
    // Current hour in 24-hour format from time picker
    val currentHour = remember { mutableStateOf(timePickerState.hour) }
    
    // Update isAm when timePickerState.hour changes
    LaunchedEffect(timePickerState.hour) {
        isAm.value = timePickerState.hour < 12
    }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppColors.white,
            modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Time",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = AppColors.gray800
                )
                
                TimeInput(
                    state = timePickerState,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = TimePickerDefaults.colors(
                        timeSelectorSelectedContainerColor = AppColors.blue500,
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContainerColor = AppColors.blue50,
                        timeSelectorUnselectedContentColor = AppColors.blue700
                    )
                )
                
                // Removed AM/PM selector buttons
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest
                    ) {
                        Text("Cancel", color = dismissButtonColor)
                    }
                    
                    TextButton(
                        onClick = { onConfirm(isAm.value) }
                    ) {
                        Text("OK", color = confirmButtonColor)
                    }
                }
            }
        }
    }
}

// Helper functions
private fun calculateTotalHours(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Double {
    // Convert hours and minutes to minutes
    val startTimeInMinutes = startHour * 60 + startMinute
    var endTimeInMinutes = endHour * 60 + endMinute
    
    // If end time is before start time, assume it's the next day
    if (endTimeInMinutes < startTimeInMinutes) {
        endTimeInMinutes += 24 * 60 // Add 24 hours
    }
    
    // Calculate difference in minutes
    val diffInMinutes = endTimeInMinutes - startTimeInMinutes
    
    // Convert back to hours with decimal places
    return diffInMinutes / 60.0
}

private fun formatDecimal(value: Double): String {
    val df = DecimalFormat("#.##")
    return df.format(value)
}

// Validate form fields
private fun validateForm(date: String, reason: String, totalHours: Double): Boolean {
    return date.isNotBlank() && reason.isNotBlank() && totalHours > 0
}

// Add this function to convert 12-hour format to 24-hour format
private fun convertTo24Hour(hour: Int, minute: Int, isAm: Boolean): Pair<Int, Int> {
    val hour24 = when {
        // Midnight (12 AM) should be 0 in 24-hour format
        hour == 12 && isAm -> 0
        // PM hours (except 12 PM) add 12
        !isAm && hour < 12 -> hour + 12
        // Everything else remains the same
        else -> hour
    }
    return Pair(hour24, minute)
}

// Add this function to format time in HH:mm format
private fun formatTimeForApi(hour: Int, minute: Int): String {
    return String.format("%02d:%02d", hour, minute)
}

@Preview(showBackground = true)
@Composable
fun OvertimeRequestFormPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.gray100
    ) {
        OvertimeRequestForm(onBackPressed = {})
    }
}

