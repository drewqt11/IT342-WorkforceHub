package cit.edu.workforcehub.presentation.screens.forms

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.workforcehub.R
import cit.edu.workforcehub.api.ApiHelper
import cit.edu.workforcehub.api.models.EmployeeProfile
import cit.edu.workforcehub.presentation.theme.AppColors
import cit.edu.workforcehub.presentation.theme.AppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview
import cit.edu.workforcehub.presentation.components.LoadingComponent

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun EnrollmentScreen(
    onNavigateToDashboard: () -> Unit = {}
) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 3
    val gradientColors = listOf(AppColors.blue500, AppColors.blue700)
    
    // Profile data state
    var profileData by remember { mutableStateOf<EmployeeProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    
    // Form state
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var dateOfBirth by remember { mutableStateOf("") }
    var maritalStatus by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }
    var consentToDataProcessing by remember { mutableStateOf(false) }
    
    // Coroutine scope for API calls
    val coroutineScope = rememberCoroutineScope()
    
    // Function to check if the profile is complete
    fun isProfileComplete(profile: EmployeeProfile): Boolean {
        // Check if all required fields are filled
        return profile.firstName.isNotBlank() &&
               profile.lastName.isNotBlank() &&
               profile.email.isNotBlank() &&
               !profile.phoneNumber.isNullOrBlank() &&
               !profile.gender.isNullOrBlank() &&
               !profile.dateOfBirth.isNullOrBlank() &&
               !profile.maritalStatus.isNullOrBlank() &&
               !profile.address.isNullOrBlank()
    }
    
    // Fetch profile data
    LaunchedEffect(key1 = true) {
        try {
            val employeeService = ApiHelper.getEmployeeService()
            val response = employeeService.getProfile()
            
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!
                profileData = profile
                
                // Check if the profile is already completed
                val profileComplete = isProfileComplete(profile)
                
                // If profile is complete, navigate back to dashboard
                if (profileComplete) {
                    onNavigateToDashboard()
                    return@LaunchedEffect
                }
                
                // Pre-fill form with existing data
                firstName = profile.firstName
                lastName = profile.lastName
                email = profile.email
                phoneNumber = profile.phoneNumber ?: ""
                gender = profile.gender ?: "Male"
                dateOfBirth = profile.dateOfBirth ?: ""
                maritalStatus = profile.maritalStatus ?: ""
                address = profile.address ?: ""
                
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
    
    // Function to update profile
    val updateProfile: () -> Unit = {
        coroutineScope.launch {
            isSubmitting = true
            
            try {
                val employeeService = ApiHelper.getEmployeeService()
                
                // Create updated profile with the new data
                val updatedProfile = profileData?.copy(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    phoneNumber = phoneNumber.trim(),
                    gender = gender.trim(),
                    dateOfBirth = dateOfBirth.trim(),
                    maritalStatus = maritalStatus.trim(),
                    address = address.trim(),
                    status = true // Set status to true to indicate the profile is complete
                )
                
                if (updatedProfile != null) {
                    val response = employeeService.patchProfile(updatedProfile)
                    
                    if (response.isSuccessful) {
                        // Navigate to dashboard on success
                        onNavigateToDashboard()
                    } else {
                        error = "Failed to update profile: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                error = "Error updating profile: ${e.message}"
            }
            
            isSubmitting = false
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 6.dp,
                color = AppColors.white
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp)
                ) {
                    // Logo and title row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Logo
                        Image(
                            painter = painterResource(id = R.drawable.logo_with_no_text),
                            contentDescription = "Workforce Hub Logo",
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(AppColors.white)
                                .border(1.dp, AppColors.gray200, CircleShape)
                                .padding(6.dp)
                        )
                        
                        // Title
                        Column(
                            modifier = Modifier
                                .padding(start = 12.dp)
                        ) {
                            Text(
                                text = "WORKFORCE HUB",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.gray800
                            )
                            Text(
                                text = "ENTERPRISE PORTAL",
                                fontSize = 16.sp,
                                color = AppColors.gray500
                            )
                        }
                        
                        // Add a spacer to push content to the left
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Form title and description
                    Text(
                        text = "Employee Enrollment Form",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppColors.gray800
                    )
                    Text(
                        text = "Complete all required information to finalize your registration",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.gray500
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Step indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StepIndicator(
                            number = 1,
                            title = "Personal",
                            isActive = currentStep == 1,
                            isCompleted = currentStep > 1,
                            onClick = { if (currentStep > 1) currentStep = 1 }
                        )
                        
                        StepIndicator(
                            number = 2,
                            title = "Address",
                            isActive = currentStep == 2,
                            isCompleted = currentStep > 2,
                            onClick = { if (currentStep > 2 || currentStep == 1) currentStep = 2 }
                        )
                        
                        StepIndicator(
                            number = 3,
                            title = "Account",
                            isActive = currentStep == 3,
                            isCompleted = currentStep > 3,
                            onClick = { if (currentStep > 1) currentStep = 3 }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                LoadingComponent()
            }
        } else if (error != null) {
            // Error state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = error ?: "Unknown error",
                        color = AppColors.red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { error = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.blue500
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }
        } else {
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .background(AppColors.gray50)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.white
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        when (currentStep) {
                            1 -> PersonalInformationStep(
                                firstName = firstName,
                                lastName = lastName,
                                phoneNumber = phoneNumber,
                                email = email,
                                selectedGender = gender,
                                dateOfBirth = dateOfBirth,
                                maritalStatus = maritalStatus,
                                onFirstNameChange = { firstName = it },
                                onLastNameChange = { lastName = it },
                                onPhoneNumberChange = { phoneNumber = it },
                                onEmailChange = { email = it },
                                onGenderChange = { gender = it },
                                onDateOfBirthChange = { dateOfBirth = it },
                                onMaritalStatusChange = { maritalStatus = it }
                            )
                            2 -> AddressInformationStep(
                                address = address,
                                onAddressChange = { address = it }
                            )
                            3 -> AccountDetailsStep(
                                profile = EmployeeProfile(
                                    employeeId = profileData?.employeeId ?: "",
                                    idNumber = profileData?.idNumber,
                                    firstName = firstName,
                                    lastName = lastName,
                                    email = email,
                                    gender = gender,
                                    hireDate = profileData?.hireDate,
                                    dateOfBirth = dateOfBirth,
                                    address = address,
                                    phoneNumber = phoneNumber,
                                    maritalStatus = maritalStatus,
                                    status = true,
                                    employmentStatus = profileData?.employmentStatus,
                                    departmentId = profileData?.departmentId,
                                    departmentName = profileData?.departmentName,
                                    jobId = profileData?.jobId,
                                    jobName = profileData?.jobName,
                                    roleId = profileData?.roleId,
                                    roleName = profileData?.roleName,
                                    createdAt = profileData?.createdAt
                                ),
                                agreeToTerms = agreeToTerms,
                                consentToDataProcessing = consentToDataProcessing,
                                onAgreeToTermsChange = { agreeToTerms = it },
                                onConsentToDataProcessingChange = { consentToDataProcessing = it }
                            )
                        }
                        
                        // Action buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left buttons (Save as Draft and Previous)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Save as Draft button
                                OutlinedButton(
                                    onClick = { /* Save draft logic */ },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = AppColors.gray600
                                    ),
                                    border = BorderStroke(1.dp, AppColors.gray300),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Save,
                                        contentDescription = "Save Draft",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save as Draft", fontSize = 12.sp)
                                }
                                
                                // Previous button (conditionally shown)
                                if (currentStep > 1) {
                                    OutlinedButton(
                                        onClick = { currentStep-- },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = AppColors.gray600
                                        ),
                                        border = BorderStroke(1.dp, AppColors.gray300),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Previous",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Previous", fontSize = 12.sp)
                                    }
                                }
                            }
                            
                            // Right button (Next or Complete)
                            if (currentStep == 3) {
                                Button(
                                    onClick = { updateProfile() },
                                    enabled = !isSubmitting && agreeToTerms && consentToDataProcessing,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.green,
                                        disabledContainerColor = AppColors.gray300
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Text("Complete", fontSize = 14.sp, color = AppColors.white)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Complete",
                                        modifier = Modifier.size(16.dp),
                                        tint = AppColors.white
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { currentStep++ },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.blue500
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Text("Next", fontSize = 14.sp, color = AppColors.white)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Next",
                                        modifier = Modifier.size(16.dp),
                                        tint = AppColors.white
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

@Composable
fun StepIndicator(
    number: Int,
    title: String,
    isActive: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> AppColors.green
                        isActive -> AppColors.blue500
                        else -> AppColors.gray200
                    }
                )
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = AppColors.white,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = number.toString(),
                    color = if (isActive) AppColors.white else AppColors.gray600,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = when {
                isCompleted -> AppColors.green
                isActive -> AppColors.blue500
                else -> AppColors.gray500
            },
            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInformationStep(
    firstName: String,
    lastName: String,
    phoneNumber: String,
    email: String,
    selectedGender: String,
    dateOfBirth: String,
    maritalStatus: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onMaritalStatusChange: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showMaritalStatusDropdown by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
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
                    text = "Personal Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.gray800
                )
                Text(
                    text = "Please provide your basic personal details",
                    fontSize = 12.sp,
                    color = AppColors.gray500,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        
        // Form fields
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            // First Name, Last Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormField(
                    label = "First Name",
                    value = firstName,
                    onValueChange = onFirstNameChange,
                    placeholder = "Enter first name",
                    leadingIcon = Icons.Default.Person,
                    isRequired = true,
                    modifier = Modifier.weight(1f)
                )
                
                FormField(
                    label = "Last Name",
                    value = lastName,
                    onValueChange = onLastNameChange,
                    placeholder = "Enter last name",
                    leadingIcon = Icons.Default.Person,
                    isRequired = true,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Date of Birth field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    buildAnnotatedString {
                        append("Date of Birth")
                        withStyle(style = SpanStyle(color = AppColors.red)) {
                            append(" *")
                        }
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.gray800
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { },
                    readOnly = true,
                    placeholder = { Text("Pick a date", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = AppColors.gray500
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { showDatePicker = true }),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.blue500,
                        unfocusedBorderColor = AppColors.gray300,
                        focusedContainerColor = AppColors.white,
                        unfocusedContainerColor = AppColors.white
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Show Date Picker",
                            tint = AppColors.blue500,
                            modifier = Modifier.clickable { showDatePicker = true }
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Marital Status field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    buildAnnotatedString {
                        append("Marital Status")
                        withStyle(style = SpanStyle(color = AppColors.red)) {
                            append(" *")
                        }
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.gray800
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedTextField(
                        value = maritalStatus,
                        onValueChange = { },
                        readOnly = true,
                        placeholder = { Text("Select marital status", fontSize = 14.sp) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Dropdown",
                                tint = AppColors.blue500,
                                modifier = Modifier.clickable { showMaritalStatusDropdown = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { showMaritalStatusDropdown = true }),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.blue500,
                            unfocusedBorderColor = AppColors.gray300,
                            focusedContainerColor = AppColors.white,
                            unfocusedContainerColor = AppColors.white
                        )
                    )
                    
                    DropdownMenu(
                        expanded = showMaritalStatusDropdown,
                        onDismissRequest = { showMaritalStatusDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        listOf("SINGLE", "MARRIED", "DIVORCED", "WIDOWED").forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    onMaritalStatusChange(status)
                                    showMaritalStatusDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Phone Number
            FormField(
                label = "Phone Number",
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                placeholder = "Enter phone number",
                leadingIcon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone,
                isRequired = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email Address
            FormField(
                label = "Email Address",
                value = email,
                onValueChange = onEmailChange,
                placeholder = "Enter email address",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                isRequired = true,
                readOnly = true // Email is from Microsoft auth, shouldn't be editable
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Gender selection
            Text(
                buildAnnotatedString {
                    append("Gender")
                    withStyle(style = SpanStyle(color = AppColors.red)) {
                        append(" *")
                    }
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.gray800
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Gender radio options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Use explicit options for gender to ensure it works reliably
                GenderOption(
                    gender = "Male",
                    selected = selectedGender == "Male",
                    onClick = { onGenderChange("Male") },
                    modifier = Modifier.weight(1f)
                )
                
                GenderOption(
                    gender = "Female",
                    selected = selectedGender == "Female",
                    onClick = { onGenderChange("Female") },
                    modifier = Modifier.weight(1f)
                )
                
                GenderOption(
                    gender = "Other",
                    selected = selectedGender == "Other",
                    onClick = { onGenderChange("Other") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Date(millis)
                        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        onDateOfBirthChange(format.format(date))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = AppColors.blue500)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = AppColors.gray600)
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}

@Composable
fun GenderOption(
    gender: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = if (selected) AppColors.blue500 else AppColors.gray300,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                if (selected) AppColors.blue50 else AppColors.white
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = AppColors.blue500,
                    unselectedColor = AppColors.gray400
                ),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = gender,
                fontSize = 13.sp,
                color = if (selected) AppColors.blue700 else AppColors.gray700,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isRequired: Boolean = false,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Column(modifier = modifier) {
        Text(
            buildAnnotatedString {
                append(label)
                if (isRequired) {
                    withStyle(style = SpanStyle(color = AppColors.red)) {
                        append(" *")
                    }
                }
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.gray800
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            leadingIcon = leadingIcon?.let { 
                { 
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = AppColors.gray500
                    ) 
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            readOnly = readOnly,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.blue500,
                unfocusedBorderColor = AppColors.gray300,
                focusedContainerColor = AppColors.white,
                unfocusedContainerColor = AppColors.white
            )
        )
    }
}

@Composable
fun AddressInformationStep(
    address: String,
    onAddressChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
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
                    text = "Address Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.gray800
                )
                Text(
                    text = "Your residential address details",
                    fontSize = 12.sp,
                    color = AppColors.gray500,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        
        // Primary Address info box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.blue50)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AppColors.blue500)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    // Title
                    Text(
                        text = "Primary Address",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.gray800
                    )
                    
                    // Description
                    Text(
                        text = "This address will be used for official communications",
                        fontSize = 12.sp,
                        color = AppColors.gray600,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
        
        // Form fields
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            // Complete Address field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    buildAnnotatedString {
                        append("Complete Address")
                        withStyle(style = SpanStyle(color = AppColors.red)) {
                            append(" *")
                        }
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.gray800
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = onAddressChange,
                    placeholder = { Text("Enter your complete address", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Address",
                            tint = AppColors.gray500
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.blue500,
                        unfocusedBorderColor = AppColors.gray300,
                        focusedContainerColor = AppColors.white,
                        unfocusedContainerColor = AppColors.white
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Please include building number, street, barangay, city, province, and ZIP code",
                    fontSize = 12.sp,
                    color = AppColors.gray500,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Country field (fixed to Philippines)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Country",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.gray800
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = "Philippines",
                    onValueChange = { },
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Country",
                            tint = AppColors.gray500
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = AppColors.gray300,
                        disabledTextColor = AppColors.gray700,
                        disabledContainerColor = AppColors.gray50
                    ),
                    enabled = false
                )
            }
        }
    }
}

@Composable
fun AccountDetailsStep(
    profile: EmployeeProfile,
    agreeToTerms: Boolean,
    consentToDataProcessing: Boolean,
    onAgreeToTermsChange: (Boolean) -> Unit,
    onConsentToDataProcessingChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
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
                    text = "Account Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.gray800
                )
                Text(
                    text = "Review your information before submission",
                    fontSize = 12.sp,
                    color = AppColors.gray500,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        
        // Account Security info box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.blue50)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AppColors.blue500)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    // Title
                    Text(
                        text = "Account Security",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.gray800
                    )
                    
                    // Description
                    Text(
                        text = "Your account is secured with Microsoft authentication",
                        fontSize = 12.sp,
                        color = AppColors.gray600,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
        
        // Information summary
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.white
            ),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, AppColors.gray200)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Personal Information",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.gray800
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                InfoRow("Name", "${profile.firstName} ${profile.lastName}")
                InfoRow("Email", profile.email)
                InfoRow("Phone", profile.phoneNumber ?: "Not provided")
                InfoRow("Gender", profile.gender ?: "Not provided")
                InfoRow("Date of Birth", profile.dateOfBirth ?: "Not provided")
                InfoRow("Marital Status", profile.maritalStatus ?: "Not provided")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Address Information",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.gray800
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                InfoRow("Address", profile.address ?: "Not provided")
                InfoRow("Country", "Philippines")
            }
        }
        
        // Terms and Conditions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.gray50)
                .padding(16.dp)
        ) {
            Text(
                text = "Terms and Conditions",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.gray800,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Terms of Service checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onAgreeToTermsChange(!agreeToTerms) }
            ) {
                Checkbox(
                    checked = agreeToTerms,
                    onCheckedChange = { onAgreeToTermsChange(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AppColors.blue500
                    )
                )
                Text(
                    buildAnnotatedString {
                        append("I agree to the ")
                        withStyle(style = SpanStyle(color = AppColors.blue500, fontWeight = FontWeight.Bold)) {
                            append("Terms of Service")
                        }
                        append(" and ")
                        withStyle(style = SpanStyle(color = AppColors.blue500, fontWeight = FontWeight.Bold)) {
                            append("Privacy Policy")
                        }
                    },
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            // Privacy Policy checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onConsentToDataProcessingChange(!consentToDataProcessing) }
            ) {
                Checkbox(
                    checked = consentToDataProcessing,
                    onCheckedChange = { onConsentToDataProcessingChange(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AppColors.blue500
                    )
                )
                Text(
                    text = "I consent to the collection and processing of my personal information as described in the Privacy Policy",
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = AppColors.gray600,
            fontSize = 13.sp,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            color = AppColors.gray800,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .weight(0.6f)
                .padding(start = 8.dp)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun EnrollmentScreenPreview() {
    AppTheme {
        EnrollmentScreen()
    }
} 