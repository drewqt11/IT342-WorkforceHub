package cit.edu.workforcehub.presentation.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import cit.edu.workforcehub.R
import cit.edu.workforcehub.api.ApiHelper
import cit.edu.workforcehub.api.models.Document
import cit.edu.workforcehub.api.models.DocumentStatus
import cit.edu.workforcehub.api.models.DocumentType
import cit.edu.workforcehub.api.models.EmployeeProfile
import cit.edu.workforcehub.presentation.components.AppHeader
import cit.edu.workforcehub.presentation.components.AppScreen
import cit.edu.workforcehub.presentation.components.LoadingComponent
import cit.edu.workforcehub.presentation.components.UniversalDrawer
import cit.edu.workforcehub.presentation.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper function to save a file to the device storage
 */
suspend fun saveFile(
    context: Context,
    fileName: String,
    mimeType: String,
    byteArray: ByteArray
): Uri? {
    return try {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val resolver = context.contentResolver
        
        // Use appropriate URI based on Android version
        val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }
        
        val uri = resolver.insert(contentUri, contentValues)
        
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { output ->
                output.write(byteArray)
            }
            uri
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e("DocumentsViewScreen", "Error saving file", e)
        null
    }
}

/**
 * Create a gradient button composable that matches the design from the image
 */
@Composable
fun GradientButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF3B82F6), // Blue
                            Color(0xFF0EA5E9), // Light blue
                            Color(0xFF14B8A6)  // Teal
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .then(
                    if (!enabled) Modifier.background(
                        Color(0x80FFFFFF),
                        RoundedCornerShape(12.dp)
                    ) else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DocumentsViewScreen(
    onLogout: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToLeaveRequests: () -> Unit = {},
    onNavigateToOvertimeRequests: () -> Unit = {},
    onNavigateToReimbursementRequests: () -> Unit = {},
    onNavigateToPerformance: () -> Unit = {},
    onNavigateToTraining: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // State for drawer
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // State for profile data
    var profileData by remember { mutableStateOf<EmployeeProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.gray100
    ) {
        // Using the Universal Drawer
        UniversalDrawer(
            drawerState = drawerState,
            currentScreen = AppScreen.DOCUMENTS,
            onLogout = onLogout,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToAttendance = onNavigateToAttendance,
            onNavigateToLeaveRequests = onNavigateToLeaveRequests,
            onNavigateToOvertimeRequests = onNavigateToOvertimeRequests,
            onNavigateToReimbursementRequests = onNavigateToReimbursementRequests,
            onNavigateToPerformance = onNavigateToPerformance,
            onNavigateToTraining = onNavigateToTraining,
            onNavigateToProfile = onNavigateToProfile
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                AppHeader(
                    onMenuClick = { 
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    modifier = Modifier.zIndex(1f),
                    providedFirstName = profileData?.firstName ?: "",
                    providedLastName = profileData?.lastName ?: "",
                    providedRole = profileData?.jobName ?: "Employee",
                    onProfileClick = onNavigateToProfile,
                    onLogoutClick = onLogout
                )
                
                // Main content
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingComponent()
                    }
                } else if (error != null) {
                    DocumentsErrorView(error = error!!)
                } else if (profileData != null) {
                    // Document content with the new design
                    DocumentContent(
                        employeeId = profileData!!.employeeId,
                        employeeName = "${profileData!!.firstName} ${profileData!!.lastName}"
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentsErrorView(error: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = AppColors.red,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = AppColors.red,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DocumentContent(
    employeeId: String,
    employeeName: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // State for documents
    var documents by remember { mutableStateOf<List<Document>>(emptyList()) }
    var isLoadingDocuments by remember { mutableStateOf(true) }
    var documentsError by remember { mutableStateOf<String?>(null) }
    
    // State for document upload
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var showDocumentTypeDialog by remember { mutableStateOf(false) }
    var selectedDocumentType by remember { mutableStateOf<DocumentType?>(null) }
    var selectedFile by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    
    // State for document download
    var isDownloading by remember { mutableStateOf(false) }
    var downloadingDocumentId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadError by remember { mutableStateOf<String?>(null) }
    
    // State for document replace
    var documentToReplace by remember { mutableStateOf<String?>(null) }
    
    // Function to fetch documents
    fun fetchDocuments() {
        isLoadingDocuments = true
        documentsError = null
        
        scope.launch {
            try {
                val documentService = ApiHelper.getDocumentService()
                val response = documentService.getEmployeeDocuments(employeeId)
                
                if (response.isSuccessful && response.body() != null) {
                    documents = response.body()!!
                } else {
                    documentsError = "Failed to load documents: ${response.message()}"
                }
                isLoadingDocuments = false
            } catch (e: Exception) {
                documentsError = "Error loading documents: ${e.message}"
                isLoadingDocuments = false
            }
        }
    }
    
    // Request storage permissions
    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }
        
        if (allGranted) {
            // Permissions granted, now we can access files
        } else {
            Toast.makeText(context, "Permissions are required to upload/download files", Toast.LENGTH_LONG).show()
        }
    }
    
    // Request necessary permissions
    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(requiredPermissions)
    }
    
    // File picker launcher
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedFile = it
            
            // Get the file name
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = it.getString(displayNameIndex)
                    }
                }
            }
            
            if (documentToReplace != null) {
                // If replacing a document, no need to select document type
                val docId = documentToReplace
                if (docId != null) {
                    // Manually inline the function to avoid reference issue
                    isUploading = true
                    uploadError = null
                    
                    scope.launch {
                        try {
                            val documentService = ApiHelper.getDocumentService()
                            
                            // Get the actual file from the URI
                            val contentResolver = context.contentResolver
                            val inputStream = contentResolver.openInputStream(uri)
                            val tempFile = File.createTempFile("upload", null, context.cacheDir)
                            
                            inputStream?.use { input ->
                                FileOutputStream(tempFile).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            
                            // Create multipart request
                            val requestFile = tempFile.asRequestBody(
                                contentResolver.getType(uri)?.toMediaTypeOrNull() ?: "application/octet-stream".toMediaTypeOrNull()
                            )
                            
                            val filePart = MultipartBody.Part.createFormData(
                                "file",
                                fileName,
                                requestFile
                            )
                            
                            // Make the API call
                            val response = documentService.replaceDocument(
                                documentId = docId,
                                file = filePart
                            )
                            
                            if (response.isSuccessful) {
                                // Refresh document list
                                fetchDocuments()
                                showUploadDialog = false
                                selectedFile = null
                                fileName = ""
                                
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Document replaced successfully", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                uploadError = "Failed to replace document: ${response.message()}"
                            }
                            
                            isUploading = false
                            // Clean up temp file
                            tempFile.delete()
                            
                        } catch (e: Exception) {
                            uploadError = "Error replacing document: ${e.message}"
                            isUploading = false
                        }
                    }
                }
                documentToReplace = null
            } else {
                // If uploading a new document, show document type selection dialog
                showDocumentTypeDialog = true
            }
        }
    }
    
    // Function to upload a document
    fun uploadDocument() {
        val file = selectedFile ?: return
        val documentType = selectedDocumentType ?: return
        
        isUploading = true
        uploadError = null
        
        scope.launch {
            try {
                val documentService = ApiHelper.getDocumentService()
                
                // Get the actual file from the URI
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(file)
                val tempFile = File.createTempFile("upload", null, context.cacheDir)
                
                inputStream?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Create multipart request
                val requestFile = tempFile.asRequestBody(
                    contentResolver.getType(file)?.toMediaTypeOrNull() ?: "application/octet-stream".toMediaTypeOrNull()
                )
                
                val filePart = MultipartBody.Part.createFormData(
                    "file",
                    fileName,
                    requestFile
                )
                
                // Make the API call
                val response = documentService.uploadDocument(
                    employeeId = employeeId,
                    file = filePart,
                    documentType = documentType.value
                )
                
                if (response.isSuccessful) {
                    // Refresh document list
                    fetchDocuments()
                    showUploadDialog = false
                    selectedFile = null
                    selectedDocumentType = null
                    fileName = ""
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Document uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    uploadError = "Failed to upload document: ${response.message()}"
                }
                
                isUploading = false
                // Clean up temp file
                tempFile.delete()
                
            } catch (e: Exception) {
                uploadError = "Error uploading document: ${e.message}"
                isUploading = false
            }
        }
    }
    
    // Function to download a document
    suspend fun downloadDocument(document: Document) {
        try {
            isDownloading = true
            downloadingDocumentId = document.documentId
            downloadProgress = 0.1f
            downloadError = null
            
            val response = ApiHelper.getDocumentService().downloadDocument(document.documentId)
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                downloadProgress = 0.4f
                
                // Create the file name with proper extension if missing
                var documentName = document.fileName
                
                // Determine mime type from file extension
                val fileExtension = documentName.substringAfterLast('.', "")
                var mimeType = document.fileType
                
                // If no file extension or mime type, try to detect from content
                if (fileExtension.isEmpty() || mimeType.isEmpty()) {
                    // Default to PDF if we can't determine
                    if (!documentName.contains(".")) {
                        documentName = "$documentName.pdf"
                        mimeType = "application/pdf"
                    } else {
                        // Try to determine mime type from extension
                        mimeType = when (fileExtension.lowercase()) {
                            "pdf" -> "application/pdf"
                            "doc", "docx" -> "application/msword"
                            "xls", "xlsx" -> "application/vnd.ms-excel"
                            "ppt", "pptx" -> "application/vnd.ms-powerpoint"
                            "jpg", "jpeg" -> "image/jpeg"
                            "png" -> "image/png"
                            "txt" -> "text/plain"
                            else -> "application/octet-stream" // Generic binary file
                        }
                    }
                }
                
                Log.d("DocumentsViewScreen", "Downloading file: $documentName with mime type: $mimeType")
                
                // Save the file with proper mime type
                val uri = saveFile(
                    context, 
                    documentName, 
                    mimeType, 
                    responseBody.bytes()
                )
                
                if (uri != null) {
                    downloadProgress = 1.0f
                    
                    withContext(Dispatchers.Main) {
                        // Create an intent to view the downloaded file
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, mimeType)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("DocumentsViewScreen", "No app to handle this file type", e)
                            Toast.makeText(
                                context,
                                "Document downloaded but no app available to open it",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        
                        Toast.makeText(
                            context,
                            "Document downloaded successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    downloadError = "Failed to save document"
                }
            } else {
                downloadError = "Failed to download document: ${response.code()} ${response.message()}"
            }
        } catch (e: Exception) {
            downloadError = "Error downloading document: ${e.localizedMessage}"
            Log.e("DocumentsViewScreen", "Download error", e)
        } finally {
            isDownloading = false
            downloadingDocumentId = null
        }
    }
    
    // Organize documents by category
    val personalDocuments = documents.filter { 
        it.documentType.equals(DocumentType.RESUME.value, ignoreCase = true) ||
        it.documentType.equals(DocumentType.ID.value, ignoreCase = true) ||
        it.documentType.equals(DocumentType.CERTIFICATE.value, ignoreCase = true)
    }
    
    val governmentDocuments = documents.filter {
        it.documentType.startsWith("BIR", ignoreCase = true) ||
        it.documentType.contains("SSS", ignoreCase = true) ||
        it.documentType.contains("PHILHEALTH", ignoreCase = true) ||
        it.documentType.contains("PAG-IBIG", ignoreCase = true)
    }
    
    val companyDocuments = documents.filter {
        it.documentType.equals(DocumentType.CONTRACT.value, ignoreCase = true) ||
        it.documentType.contains("AGREEMENT", ignoreCase = true) ||
        !personalDocuments.contains(it) && !governmentDocuments.contains(it)
    }
    
    // Fetch documents when the screen is first shown
    LaunchedEffect(Unit) {
        fetchDocuments()
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Documents",
                            color = Color(0xFF1F2937),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1F2937)
                )
            )
        },
        floatingActionButton = {
            // Floating Action Button for uploading documents
            FloatingActionButton(
                onClick = { showUploadDialog = true },
                containerColor = Color(0xFF3B82F6),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Upload Document"
                )
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = Color(0xFFF9FAFB)
        ) {
            if (isLoadingDocuments) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF3B82F6))
                }
            } else if (documentsError != null && documents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Info",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(64.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No documents found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B5563)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Upload a document to get started",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { showUploadDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF3B82F6), // Blue
                                                Color(0xFF0EA5E9), // Light blue
                                                Color(0xFF14B8A6)  // Teal
                                            )
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Upload,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = "Upload Document",
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Personal Documents Section
                    SectionHeader(
                        icon = Icons.Default.Person,
                        title = "Personal"
                    )
                    
                    if (personalDocuments.isEmpty()) {
                        // Display some default document cards for personal docs
                        DocumentCard(
                            icon = Icons.Default.Description,
                            title = "Resume/Curriculum Vitae",
                            status = "Not uploaded yet",
                            isUploaded = false,
                            actions = listOf("upload"),
                            onUpload = { 
                                selectedDocumentType = DocumentType.RESUME
                                filePicker.launch("*/*") 
                            }
                        )
                        
                        DocumentCard(
                            icon = Icons.Default.Description,
                            title = "Birth Certificate",
                            status = "Not uploaded yet",
                            isUploaded = false,
                            actions = listOf("upload"),
                            onUpload = { 
                                selectedDocumentType = DocumentType.CERTIFICATE
                                filePicker.launch("*/*") 
                            }
                        )
                        
                        DocumentCard(
                            icon = Icons.Default.Description,
                            title = "Government Issue ID",
                            status = "Not uploaded yet",
                            isUploaded = false,
                            actions = listOf("upload"),
                            onUpload = { 
                                selectedDocumentType = DocumentType.ID
                                filePicker.launch("*/*") 
                            }
                        )
                    } else {
                        // Display actual personal documents
                        personalDocuments.forEach { document ->
                            DocumentCard(
                                icon = Icons.Default.Description,
                                iconTint = Color(0xFF22C55E),
                                title = document.name,
                                status = "Uploaded",
                                isUploaded = true,
                                actions = listOf("view", "replace", "download"),
                                onView = { /* Open document view */ },
                                onReplace = { 
                                    documentToReplace = document.documentId
                                    filePicker.launch("*/*") 
                                },
                                onDownload = { 
                                    scope.launch { 
                                        downloadDocument(document) 
                                    }
                                },
                                isDownloading = isDownloading && downloadingDocumentId == document.documentId
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Government Related Section
                    SectionHeader(
                        icon = Icons.Default.Business,
                        title = "Government Related"
                    )
                    
                    Text(
                        text = "BIR",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    
                    if (governmentDocuments.isEmpty()) {
                        // Display default government document cards
                        DocumentCard(
                            icon = Icons.Default.Description,
                            title = "BIR Form 1902",
                            status = "Not uploaded yet",
                            isUploaded = false,
                            actions = listOf("upload"),
                            onUpload = { 
                                selectedDocumentType = DocumentType.OTHER
                                filePicker.launch("*/*") 
                            }
                        )
                        
                        DocumentCard(
                            icon = Icons.Default.Description,
                            title = "BIR TAX Identification Number",
                            status = "Not uploaded yet",
                            isUploaded = false,
                            actions = listOf("upload"),
                            onUpload = { 
                                selectedDocumentType = DocumentType.OTHER
                                filePicker.launch("*/*") 
                            }
                        )
                        
                        DocumentCard(
                            icon = Icons.Default.Description,
                            title = "BIR Form 2316",
                            status = "Not uploaded yet",
                            isUploaded = false,
                            actions = listOf("upload"),
                            onUpload = { 
                                selectedDocumentType = DocumentType.OTHER
                                filePicker.launch("*/*") 
                            }
                        )
                        
                        DocumentCard(
                            icon = Icons.Default.Description,
                            title = "SSS ID",
                            status = "Not uploaded yet",
                            isUploaded = false,
                            actions = listOf("upload"),
                            onUpload = { 
                                selectedDocumentType = DocumentType.OTHER
                                filePicker.launch("*/*") 
                            }
                        )
                        
                        DocumentCard(
                            icon = Icons.Default.Description,
                            title = "Philhealth ID",
                            status = "Not uploaded yet",
                            isUploaded = false,
                            actions = listOf("upload"),
                            onUpload = { 
                                selectedDocumentType = DocumentType.OTHER
                                filePicker.launch("*/*") 
                            }
                        )
                        
                        DocumentCard(
                            icon = Icons.Default.Description,
                            title = "PAG-IBIG Membership ID",
                            status = "Not uploaded yet",
                            isUploaded = false,
                            actions = listOf("upload"),
                            onUpload = { 
                                selectedDocumentType = DocumentType.OTHER
                                filePicker.launch("*/*") 
                            }
                        )
                    } else {
                        // Display actual government documents
                        governmentDocuments.forEach { document ->
                            DocumentCard(
                                icon = Icons.Default.Description,
                                iconTint = Color(0xFF22C55E),
                                title = document.name,
                                status = "Uploaded",
                                isUploaded = true,
                                actions = listOf("view", "replace", "download"),
                                onView = { /* Open document view */ },
                                onReplace = { 
                                    documentToReplace = document.documentId
                                    filePicker.launch("*/*") 
                                },
                                onDownload = { 
                                    scope.launch { 
                                        downloadDocument(document) 
                                    }
                                },
                                isDownloading = isDownloading && downloadingDocumentId == document.documentId
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Company Section
                    SectionHeader(
                        icon = Icons.Default.Business,
                        title = "Company"
                    )
                    
                    if (companyDocuments.isEmpty()) {
                        // Display default company document cards
                        DocumentCard(
                            icon = Icons.Default.Description,
                            title = "Confidentiality Agreement",
                            status = "Not uploaded yet",
                            isUploaded = false,
                            actions = listOf("upload"),
                            onUpload = { 
                                selectedDocumentType = DocumentType.OTHER
                                filePicker.launch("*/*") 
                            }
                        )
                        
                        DocumentCard(
                            icon = Icons.Default.Description,
                            title = "Employment Contract",
                            status = "Not uploaded yet",
                            isUploaded = false,
                            actions = listOf("upload"),
                            onUpload = { 
                                selectedDocumentType = DocumentType.CONTRACT
                                filePicker.launch("*/*") 
                            }
                        )
                    } else {
                        // Display actual company documents
                        companyDocuments.forEach { document ->
                            DocumentCard(
                                icon = Icons.Default.Description,
                                iconTint = Color(0xFF22C55E),
                                title = document.name,
                                status = "Uploaded",
                                isUploaded = true,
                                actions = listOf("view", "replace", "download"),
                                onView = { /* Open document view */ },
                                onReplace = { 
                                    documentToReplace = document.documentId
                                    filePicker.launch("*/*") 
                                },
                                onDownload = { 
                                    scope.launch { 
                                        downloadDocument(document) 
                                    }
                                },
                                isDownloading = isDownloading && downloadingDocumentId == document.documentId
                            )
                        }
                    }
                    
                    // Add some space at the bottom
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
        // Document type selection dialog
        if (showDocumentTypeDialog) {
            AlertDialog(
                onDismissRequest = { showDocumentTypeDialog = false },
                title = { 
                    Text(
                        text = "Select Document Type",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    ) 
                },
                text = {
                    Column {
                        DocumentType.values().forEach { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedDocumentType = type
                                        showDocumentTypeDialog = false
                                        uploadDocument()
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = type.displayName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF4B5563)
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(
                        onClick = { showDocumentTypeDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF3B82F6)
                        )
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        // Upload dialog
        if (showUploadDialog) {
            AlertDialog(
                onDismissRequest = { 
                    if (!isUploading) {
                        showUploadDialog = false
                        selectedFile = null
                        selectedDocumentType = null
                        fileName = ""
                    }
                },
                title = { 
                    Text(
                        text = "Upload Document",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    ) 
                },
                text = {
                    Column {
                        if (selectedFile != null) {
                            Text(
                                text = "Selected file: $fileName",
                                color = Color(0xFF4B5563)
                            )
                        } else {
                            Text(
                                text = "Please select a file to upload",
                                color = Color(0xFF4B5563)
                            )
                        }
                        
                        if (uploadError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uploadError!!,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (selectedFile == null) {
                                filePicker.launch("*/*")
                            } else {
                                uploadDocument()
                            }
                        },
                        enabled = !isUploading && (selectedFile == null || (selectedFile != null && selectedDocumentType != null)),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF3B82F6), // Blue
                                            Color(0xFF0EA5E9), // Light blue
                                            Color(0xFF14B8A6)  // Teal
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .then(
                                    if (!(!isUploading && (selectedFile == null || (selectedFile != null && selectedDocumentType != null)))) 
                                        Modifier.background(
                                            Color(0x80FFFFFF),
                                            RoundedCornerShape(12.dp)
                                        ) else Modifier
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isUploading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = if (selectedFile == null) "Select File" else "Upload",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            if (!isUploading) {
                                showUploadDialog = false
                                selectedFile = null
                                selectedDocumentType = null
                                fileName = ""
                            }
                        },
                        enabled = !isUploading,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF3B82F6)
                        )
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun SectionHeader(
    icon: ImageVector,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF3B82F6),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1F2937)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentCard(
    icon: ImageVector,
    title: String,
    status: String,
    isUploaded: Boolean,
    actions: List<String>,
    iconTint: Color = Color.Gray,
    onUpload: () -> Unit = {},
    onView: () -> Unit = {},
    onReplace: () -> Unit = {},
    onDownload: () -> Unit = {},
    isDownloading: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with light blue background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFFEFF6FF), 
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isUploaded) Color(0xFF3B82F6) else Color(0xFF9CA3AF),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937)
                )
                
                Text(
                    text = if (isUploaded) "Uploaded" else "Not uploaded yet",
                    fontSize = 12.sp,
                    color = if (isUploaded) Color(0xFF22C55E) else Color(0xFF6B7280)
                )
            }
            
            // Actions
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (actions.contains("view") && isUploaded) {
                    OutlinedButton(
                        onClick = onView,
                        modifier = Modifier.padding(end = 8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF3B82F6)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "View",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View", fontSize = 12.sp)
                    }
                }
                
                if (actions.contains("replace") && isUploaded) {
                    OutlinedButton(
                        onClick = onReplace,
                        modifier = Modifier.padding(end = 8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF3B82F6)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Replace",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Replace", fontSize = 12.sp)
                    }
                }
                
                if (actions.contains("download") && isUploaded) {
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.padding(end = 8.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        enabled = !isDownloading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF3B82F6), // Blue
                                            Color(0xFF0EA5E9), // Light blue
                                            Color(0xFF14B8A6)  // Teal
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .then(
                                    if (isDownloading) Modifier.background(
                                        Color(0x80FFFFFF),
                                        RoundedCornerShape(12.dp)
                                    ) else Modifier
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                
                if (actions.contains("upload") && !isUploaded) {
                    Button(
                        onClick = onUpload,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF3B82F6), // Blue
                                            Color(0xFF0EA5E9), // Light blue
                                            Color(0xFF14B8A6)  // Teal
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Upload,
                                    contentDescription = "Upload",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Upload", 
                                    fontSize = 12.sp,
                                    color = Color.White,
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

@Composable
fun DocumentItem(
    document: Document,
    isDownloading: Boolean = false,
    onDownload: () -> Unit = {},
    onReplace: () -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val uploadDate = try {
        // Try to parse the date string
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(document.uploadDate)
        date?.let { dateFormat.format(it) } ?: document.uploadDate
    } catch (e: Exception) {
        document.uploadDate
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppColors.gray200)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Document Icon and Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Document icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.blue100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = AppColors.blue500,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Document name and type
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = document.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.gray800,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = document.documentType,
                        fontSize = 14.sp,
                        color = AppColors.gray500
                    )
                }
                
                // Download button
                IconButton(
                    onClick = onDownload,
                    enabled = !isDownloading
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = AppColors.blue500,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            tint = AppColors.blue500
                        )
                    }
                }
                
                // Replace button
                IconButton(
                    onClick = onReplace
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = "Replace",
                        tint = AppColors.blue500
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = AppColors.gray200,
                thickness = 1.dp
            )
            
            // File details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left column
                Column {
                    DetailItem("File Name", document.fileName)
                    DetailItem("Upload Date", uploadDate)
                }
                
                // Right column
                Column {
                    DetailItem("File Type", document.fileType)
                    DetailItem("Status", document.status, isStatus = true)
                }
            }
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    isStatus: Boolean = false
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.gray500
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        if (isStatus) {
            val (bgColor, textColor) = when(value.uppercase()) {
                DocumentStatus.APPROVED.value -> Pair(AppColors.teal100, AppColors.teal900)
                DocumentStatus.REJECTED.value -> Pair(AppColors.redLight, AppColors.red)
                else -> Pair(AppColors.blue100, AppColors.blue700)
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(bgColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            }
        } else {
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = AppColors.gray700
            )
        }
    }
}

