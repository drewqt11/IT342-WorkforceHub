package cit.edu.workforcehub.presentation.screens.forms

/**
 * Reimbursement Request Form Screen
 * 
 * API Endpoint: POST /api/employee/reimbursement-requests
 * 
 * API Payload Structure:
 * {
 *   "reimbursementId": "string",           // Auto-generated
 *   "employeeId": "string",                // From authentication
 *   "employeeName": "string",              // From authentication
 *   "planId": "string",                    // Optional
 *   "planName": "string",                  // Optional
 *   "planType": "string",                  // Optional
 *   "requestDate": "2025-05-09",           // Server-generated
 *   "expenseDate": "2025-05-09",           // User input
 *   "amountRequested": 0,                  // User input (as "amount" in UI)
 *   "documentPath": "string",              // Optional
 *   "reason": "string",                    // User input (as "description" in UI)
 *   "status": "string",                    // Default: "PENDING"
 *   "reviewedById": "string",              // Filled during review
 *   "reviewedByName": "string",            // Filled during review
 *   "reviewedAt": "2025-05-09T22:04:08.252Z", // Filled during review
 *   "remarks": "string"                    // Filled during review
 * }
 * 
 * Required User Inputs:
 * - Amount Requested (mapped from "amount" field)
 * - Expense Date
 * - Reason (mapped from "description" field)
 */

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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.workforcehub.R
import cit.edu.workforcehub.api.ApiHelper
import cit.edu.workforcehub.api.models.ReimbursementRequest
import cit.edu.workforcehub.presentation.theme.AppColors
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Screen for submitting reimbursement requests.
 * This screen allows employees to submit expense reimbursement requests.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReimbursementRequestFormScreen(
    onNavigateBack: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Form state variables
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expenseDate by remember { mutableStateOf("") }
    var isDatePickerVisible by remember { mutableStateOf(false) }
    
    // Category dropdown state
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    
    // Validation state
    var isFormValid by remember {
        mutableStateOf(false)
    }
    
    // Error messages for validation
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var expenseDateError by remember { mutableStateOf<String?>(null) }
    
    // API state
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Context and scope
    val context = LocalContext.current
    
    // Predefined categories for reimbursement
    val categories = listOf(
        "Office Supplies",
        "Travel",
        "Meals",
        "Training",
        "Software/Hardware",
        "Medical Expenses",
        "Professional Development",
        "Other"
    )
    
    // Date picker state
    val datePickerState = rememberDatePickerState()
    
    // Validate form whenever fields change
    LaunchedEffect(amount, category, description, expenseDate) {
        isFormValid = validateForm(
            amount,
            category,
            description,
            expenseDate,
            onAmountError = { amountError = it },
            onCategoryError = { categoryError = it },
            onDescriptionError = { descriptionError = it },
            onExpenseDateError = { expenseDateError = it }
        )
    }
    
    // Helper function to convert milliseconds to LocalDate
    fun millisecondsToFormattedDate(milliseconds: Long?): String {
        if (milliseconds == null) return ""
        val instant = Instant.ofEpochMilli(milliseconds)
        val date = LocalDate.ofInstant(instant, ZoneId.systemDefault())
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date)
    }
    
    // Success dialog content
    if (showSuccessDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onNavigateBack()
            },
            title = {
                Text(
                    text = "Request Submitted",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Your reimbursement request has been submitted successfully and is pending approval.",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(
                        text = "OK",
                        color = AppColors.blue500,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = AppColors.white,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Date picker dialog
    if (isDatePickerVisible) {
        DatePickerDialog(
            onDismissRequest = { isDatePickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            expenseDate = millisecondsToFormattedDate(millis)
                        }
                        isDatePickerVisible = false
                    }
                ) {
                    Text("OK", color = AppColors.blue500)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isDatePickerVisible = false }
                ) {
                    Text("Cancel", color = AppColors.gray600)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                        onClick = onNavigateBack,
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
                    text = "Request Reimbursement",
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
                                text = "Reimbursement Request Form",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.gray800
                            )
                            Text(
                                text = "Please provide the details for your expense reimbursement",
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
                        // Form content with modern styling
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Amount field
                            Column {
                                Text(
                                    text = "Amount",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.gray800,
                                    letterSpacing = 0.1.sp
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
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
                                        color = if (amountError != null) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) 
                                               else AppColors.gray200
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
                                                imageVector = Icons.Default.AttachMoney,
                                                contentDescription = "Amount",
                                                tint = AppColors.blue600,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        
                                        // Text field
                                        OutlinedTextField(
                                            value = amount,
                                            onValueChange = { 
                                                // Only allow numeric input with at most one decimal point
                                                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                                    amount = it
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp),
                                            placeholder = { 
                                                Text(
                                                    "Enter amount", 
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    color = AppColors.gray400
                                                ) 
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color.Transparent,
                                                unfocusedBorderColor = Color.Transparent,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White,
                                                cursorColor = AppColors.blue500
                                            ),
                                            textStyle = LocalTextStyle.current.copy(
                                                color = AppColors.gray900,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            singleLine = true
                                        )
                                    }
                                }
                                
                                // Error message
                                if (amountError != null) {
                                    Text(
                                        text = amountError!!,
                                        color = AppColors.gray700,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                }
                            }
                            
                            // Category field with dropdown
                            Column {
                                Text(
                                    text = "Category",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.gray800,
                                    letterSpacing = 0.1.sp
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Box(modifier = Modifier.fillMaxWidth()) {
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
                                            color = if (categoryError != null) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) 
                                                   else AppColors.gray200
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
                                                    imageVector = Icons.Default.Category,
                                                    contentDescription = "Category",
                                                    tint = AppColors.blue600,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            
                                            // Text field
                                            OutlinedTextField(
                                                value = category,
                                                onValueChange = { /* Read only */ },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(56.dp),
                                                readOnly = true,
                                                placeholder = { 
                                                    Text(
                                                        "Select a category", 
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Normal,
                                                        color = AppColors.gray400
                                                    ) 
                                                },
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
                                                singleLine = true
                                            )
                                            
                                            // Dropdown icon
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
                                                    .clickable { isCategoryDropdownExpanded = true },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Select Category",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                    
                                    DropdownMenu(
                                        expanded = isCategoryDropdownExpanded,
                                        onDismissRequest = { isCategoryDropdownExpanded = false },
                                        modifier = Modifier
                                            .background(AppColors.white)
                                            .shadow(
                                                elevation = 4.dp,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        categories.forEach { categoryOption ->
                                            DropdownMenuItem(
                                                text = { 
                                                    Text(
                                                        categoryOption,
                                                        fontSize = 15.sp,
                                                        color = AppColors.gray900
                                                    ) 
                                                },
                                                onClick = {
                                                    category = categoryOption
                                                    isCategoryDropdownExpanded = false
                                                },
                                                leadingIcon = {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(30.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(AppColors.blue50)
                                                            .padding(6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Category,
                                                            contentDescription = null,
                                                            tint = AppColors.blue600,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                                
                                // Error message
                                if (categoryError != null) {
                                    Text(
                                        text = categoryError!!,
                                        color = AppColors.gray700,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                }
                            }
                            
                            // Date field with date picker
                            Column {
                                Text(
                                    text = "Expense Date",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.gray800,
                                    letterSpacing = 0.1.sp
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
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
                                        color = if (expenseDateError != null) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) 
                                               else AppColors.gray200
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
                                                imageVector = Icons.Default.CalendarMonth,
                                                contentDescription = "Date",
                                                tint = AppColors.blue600,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        
                                        // Text field
                                        OutlinedTextField(
                                            value = expenseDate,
                                            onValueChange = { /* Date is set via picker */ },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp),
                                            readOnly = true,
                                            placeholder = { 
                                                Text(
                                                    "Select date", 
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    color = AppColors.gray400
                                                ) 
                                            },
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
                                            singleLine = true
                                        )
                                        
                                        // Calendar icon
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
                                                .clickable { isDatePickerVisible = true },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarMonth,
                                                contentDescription = "Select Date",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                                
                                // Error message
                                if (expenseDateError != null) {
                                    Text(
                                        text = expenseDateError!!,
                                        color = AppColors.gray700,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                }
                            }
                            
                            // Description field
                            Column {
                                Text(
                                    text = "Description",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.gray800,
                                    letterSpacing = 0.1.sp
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
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
                                        color = if (descriptionError != null) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) 
                                               else AppColors.gray200
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(4.dp),
                                        verticalAlignment = Alignment.Top
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
                                                imageVector = Icons.Default.Description,
                                                contentDescription = "Description",
                                                tint = AppColors.blue600,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        
                                        // Text field
                                        OutlinedTextField(
                                            value = description,
                                            onValueChange = { description = it },
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 120.dp),
                                            placeholder = { 
                                                Text(
                                                    "Enter description of expense", 
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    color = AppColors.gray400
                                                ) 
                                            },
                                            keyboardOptions = KeyboardOptions(
                                                capitalization = KeyboardCapitalization.Sentences,
                                                keyboardType = KeyboardType.Text
                                            ),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color.Transparent,
                                                unfocusedBorderColor = Color.Transparent,
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                cursorColor = AppColors.blue500
                                            ),
                                            textStyle = LocalTextStyle.current.copy(
                                                textAlign = TextAlign.Start,
                                                color = AppColors.gray900,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Normal
                                            ),
                                            maxLines = 5
                                        )
                                    }
                                }
                                
                                // Error message
                                if (descriptionError != null) {
                                    Text(
                                        text = descriptionError!!,
                                        color = AppColors.gray700,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                }
                            }
                            
                            // Information card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = 1.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        ambientColor = AppColors.blue300.copy(alpha = 0.3f)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = AppColors.blue50
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, AppColors.blue100)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(
                                                        AppColors.blue300,
                                                        AppColors.blue400
                                                    ),
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Information",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column {
                                        Text(
                                            text = "Submission Guidelines",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.blue700,
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Text(
                                            text = "Please provide accurate details for your reimbursement request. Attach receipts if required by your organization's policy.",
                                            fontSize = 13.sp,
                                            color = AppColors.blue700,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                            
                            // Add bottom spacing
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
                
                // Submit button with gradient - positioned outside the scrollable area
                Button(
                    onClick = {
                        if (isFormValid) {
                            isSubmitting = true
                            submitError = null
                            
                            scope.launch {
                                try {
                                    // Create ReimbursementRequest object
                                    val request = ReimbursementRequest(
                                        employeeId = "",  // Will be filled by backend based on token
                                        expenseDate = expenseDate,
                                        amount = amount.toDoubleOrNull() ?: 0.0,
                                        description = description,
                                        // Other fields will be defaulted or filled by backend
                                        status = "PENDING"
                                    )
                                    
                                    // Submit using the API
                                    val employeeService = ApiHelper.getEmployeeService()
                                    val response = employeeService.submitReimbursementRequest(request)
                                    
                                    if (response.isSuccessful) {
                                        // Show success dialog
                                        showSuccessDialog = true
                                    } else {
                                        // Show error
                                        submitError = "Failed to submit: ${response.message()}"
                                        snackbarHostState.showSnackbar(submitError ?: "An error occurred")
                                    }
                                } catch (e: Exception) {
                                    // Handle exception
                                    submitError = "Error: ${e.message}"
                                    snackbarHostState.showSnackbar(submitError ?: "An unexpected error occurred")
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        } else {
                            // Form is not valid, show error message
                            scope.launch {
                                snackbarHostState.showSnackbar("Please fill all required fields correctly")
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
        }
    }
}

/**
 * Validates the form fields and sets appropriate error messages
 */
private fun validateForm(
    amount: String,
    category: String,
    description: String,
    expenseDate: String,
    onAmountError: (String?) -> Unit,
    onCategoryError: (String?) -> Unit,
    onDescriptionError: (String?) -> Unit,
    onExpenseDateError: (String?) -> Unit
): Boolean {
    var isValid = true
    
    // Validate amount (maps to amountRequested in API)
    if (amount.isBlank()) {
        onAmountError("Amount is required")
        isValid = false
    } else if (amount.toDoubleOrNull() == null) {
        onAmountError("Amount must be a valid number")
        isValid = false
    } else if (amount.toDoubleOrNull()!! <= 0) {
        onAmountError("Amount must be greater than zero")
        isValid = false
    } else {
        onAmountError(null)
    }
    
    // Validate category (UI only, not sent to API)
    if (category.isBlank()) {
        onCategoryError("Category is required")
        isValid = false
    } else {
        onCategoryError(null)
    }
    
    // Validate description (maps to reason in API)
    if (description.isBlank()) {
        onDescriptionError("Description is required")
        isValid = false
    } else if (description.length < 5) {
        onDescriptionError("Description must be at least 5 characters")
        isValid = false
    } else {
        onDescriptionError(null)
    }
    
    // Validate expense date
    if (expenseDate.isBlank()) {
        onExpenseDateError("Expense date is required")
        isValid = false
    } else {
        onExpenseDateError(null)
    }
    
    return isValid
}

@Preview(showBackground = true)
@Composable
fun ReimbursementRequestFormPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.gray100
    ) {
        ReimbursementRequestFormScreen(onNavigateBack = {})
    }
}

