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
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.geometry.Offset
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

@Composable
fun SectionHeader(
    icon: ImageVector,
    title: String
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val (iconRef, titleRef) = createRefs()
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF3B82F6),
            modifier = Modifier
                .size(24.dp)
                .constrainAs(iconRef) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        )
        
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1F2937),
            modifier = Modifier
                .constrainAs(titleRef) {
                    start.linkTo(iconRef.end, margin = 8.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
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
    document: Document? = null,
    onViewClick: (Document) -> Unit = {},
    onReplaceClick: (Document) -> Unit = {},
    onDownloadClick: (Document) -> Unit = {},
    onUploadClick: () -> Unit = {},
    isDownloading: Boolean = false,
    downloadingDocumentId: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val (iconRef, contentRef, actionsRef) = createRefs()
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(iconRef) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            )
            
            Column(
                modifier = Modifier
                    .constrainAs(contentRef) {
                        start.linkTo(iconRef.end, margin = 16.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    }
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937)
                )
                
                Text(
                    text = status,
                    fontSize = 14.sp,
                    color = if (isUploaded) AppColors.green else AppColors.gray500,
                    letterSpacing = 0.1.sp
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .constrainAs(actionsRef) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            ) {
                if (actions.contains("replace") && document != null) {
                    OutlinedButton(
                        onClick = { onReplaceClick(document) },
                        modifier = Modifier.padding(end = 8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppColors.blue500,
                            disabledContentColor = AppColors.gray400
                        ),
                        border = BorderStroke(1.dp, if (!isDownloading || downloadingDocumentId != document.documentId) AppColors.blue300 else AppColors.gray300),
                        shape = RoundedCornerShape(8.dp)
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
                
                if (actions.contains("download") && document != null) {
                    Button(
                        onClick = { onDownloadClick(document) },
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        enabled = !isDownloading || downloadingDocumentId != document.documentId,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color(0xFFE5E7EB)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF3B82F6), Color(0xFF14B8A6)),
                                        start = Offset(0f, 0f),
                                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDownloading && downloadingDocumentId == document.documentId) {
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
                                    modifier = Modifier.size(1.dp)
                                )
                            }
                        }
                    }
                }
                
                if (actions.contains("upload")) {
                    Button(
                        onClick = { onUploadClick() },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "Upload",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Upload", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedDocumentCard(
    icon: ImageVector,
    title: String,
    status: String,
    isUploaded: Boolean,
    iconTint: Color = AppColors.gray400,
    document: Document? = null,
    onViewClick: (Document) -> Unit = {},
    onReplaceClick: (Document) -> Unit = {},
    onDownloadClick: (Document) -> Unit = {},
    onUploadClick: () -> Unit = {},
    isDownloading: Boolean = false,
    downloadingDocumentId: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = AppColors.gray400.copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.white
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppColors.gray200)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title and status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Document icon with background
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isUploaded) AppColors.teal100 else AppColors.gray100)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isUploaded) AppColors.teal500 else iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.gray800,
                        letterSpacing = 0.1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = status,
                        fontSize = 14.sp,
                        color = AppColors.gray500,
                        letterSpacing = 0.1.sp
                    )
                }
            }
            
            // Actions row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isUploaded && document != null) {
                    // Replace button
                    OutlinedButton(
                        onClick = { onReplaceClick(document) },
                        modifier = Modifier.padding(end = 8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppColors.blue500,
                            disabledContentColor = AppColors.gray400
                        ),
                        border = BorderStroke(1.dp, if (!isDownloading || downloadingDocumentId != document.documentId) AppColors.blue300 else AppColors.gray300),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Replace",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Replace", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    
                    // Download button
                    Button(
                        onClick = { onDownloadClick(document) },
                        modifier = Modifier
                            .size(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        enabled = !isDownloading || downloadingDocumentId != document.documentId,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = AppColors.gray200
                        )
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
                            if (isDownloading && downloadingDocumentId == document.documentId) {
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
                } else {
                    // Upload button
                    Button(
                        onClick = onUploadClick,
                        modifier = Modifier
                            .width(120.dp)
                            .height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        )
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
                                    "Upload",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DocumentsViewScreen(
    onBackToProfile: () -> Unit = {}
) {
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Back button row with logo and title text (copied design from LeaveRequestForms)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .padding(top = 35.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onBackToProfile() },
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
                
                // Logo and title text
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
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Title text
                    Column {
                        Text(
                            text = "WORKFORCE HUB",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 16.sp,
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
            
            // Content area
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    .weight(1f)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingComponent()
                    }
                } else if (error != null) {
                    DocumentsErrorView(error = error!!)
                } else if (profileData != null) {
                    // Document content with the new design
                    Column(modifier = Modifier.fillMaxSize()) {
                        // My Documents title
                        Text(
                            text = "My Documents",
                            fontWeight = FontWeight.Bold,
                            color = AppColors.gray800,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, end = 16.dp, bottom = 15.dp, top = 4.dp)
                        )
                        
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
    
    // State for tabs
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Personal", "Government", "Company")
    
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
    
    // Function to upload a document - moved here before use
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
                
                // Make the API call with appropriate document name
                val documentName = when (documentType) {
                    DocumentType.RESUME -> "Resume/Curriculum Vitae"
                    DocumentType.BIRTH_CERTIFICATE -> "Birth Certificate"
                    DocumentType.GOVERNMENT_ID -> "Government Issue ID"
                    DocumentType.SSS_ID -> "SSS ID"
                    DocumentType.TIN_ID -> "BIR TAX Identification Number"
                    DocumentType.PHILHEALTH_ID -> "Philhealth ID"
                    DocumentType.PAG_IBIG_ID -> "PAG-IBIG Membership ID"
                    DocumentType.HMO_ID -> "HMO ID"
                    DocumentType.BIR_FORM_1902 -> "BIR Form 1902"
                    DocumentType.BIR_FORM_2316 -> "BIR Form 2316"
                    DocumentType.CONFIDENTIALITY_AGREEMENT -> "Confidentiality Agreement"
                    DocumentType.EMPLOYMENT_CONTRACT -> "Employment Contract"
                }
                
                val response = documentService.uploadDocument(
                    employeeId = employeeId,
                    file = filePart,
                    documentType = documentType.value, // Use the value from the enum
                    documentName = documentName // Use the appropriate name based on document type
                )
                
                if (response.isSuccessful) {
                    // Refresh document list
                    fetchDocuments()
                    showUploadDialog = false
                    selectedFile = null
                    selectedDocumentType = null
                    fileName = ""
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "${documentName} uploaded successfully", Toast.LENGTH_SHORT).show()
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
                                file = filePart,
                                documentName = when (selectedDocumentType) {
                                    DocumentType.RESUME -> "Resume/Curriculum Vitae"
                                    DocumentType.BIRTH_CERTIFICATE -> "Birth Certificate"
                                    DocumentType.GOVERNMENT_ID -> "Government Issue ID"
                                    DocumentType.SSS_ID -> "SSS ID"
                                    DocumentType.TIN_ID -> "BIR TAX Identification Number"
                                    DocumentType.PHILHEALTH_ID -> "Philhealth ID"
                                    DocumentType.PAG_IBIG_ID -> "PAG-IBIG Membership ID"
                                    DocumentType.HMO_ID -> "HMO ID"
                                    DocumentType.BIR_FORM_1902 -> "BIR Form 1902"
                                    DocumentType.BIR_FORM_2316 -> "BIR Form 2316"
                                    DocumentType.CONFIDENTIALITY_AGREEMENT -> "Confidentiality Agreement"
                                    DocumentType.EMPLOYMENT_CONTRACT -> "Employment Contract"
                                    null -> "Document" // Fallback
                                }
                            )
                            
                            if (response.isSuccessful) {
                                // Refresh document list
                                fetchDocuments()
                                showUploadDialog = false
                                selectedFile = null
                                fileName = ""
                                
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Document updated successfully", Toast.LENGTH_SHORT).show()
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
                // For new uploads, proceed directly with the selected document type
                // No need to show document type selection dialog
                uploadDocument()
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
    
    // Function to view a document directly
    suspend fun viewDocument(document: Document) {
        try {
            isDownloading = true
            downloadingDocumentId = document.documentId
            
            val documentService = ApiHelper.getDocumentService()
            val response = documentService.viewDocument(document.documentId)
            
            if (response.isSuccessful && response.body() != null) {
                try {
                    // Directly read the URL string from the response body
                    val responseBody = response.body()!!
                    val viewUrl = responseBody.string().trim()
                    
                    Log.d("DocumentsViewScreen", "Received URL: $viewUrl")
                    
                    if (viewUrl.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            // Open the URL in a browser
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(viewUrl))
                            
                            try {
                                context.startActivity(intent)
                                Toast.makeText(
                                    context,
                                    "Opening document for viewing",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: Exception) {
                                Log.e("DocumentsViewScreen", "Failed to open document viewer", e)
                                Toast.makeText(
                                    context,
                                    "No app available to view this document",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Empty view URL returned from server",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DocumentsViewScreen", "Error processing response body", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Error processing document: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                val errorMessage = "Failed to view document: ${response.code()} ${response.message()}"
                Log.e("DocumentsViewScreen", errorMessage)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            Log.e("DocumentsViewScreen", "Error viewing document", e)
            
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Error viewing document: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } finally {
            isDownloading = false
            downloadingDocumentId = null
        }
    }
    
    // Fetch documents when the screen is first shown
    LaunchedEffect(Unit) {
        fetchDocuments()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main document card container
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
            
            // Form header with blue indicator - moved before tabs for better layout
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
                        text = "Document Management",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.gray800
                    )
                    Text(
                        text = "View, upload, and manage your important documents",
                        fontSize = 14.sp,
                        color = AppColors.gray500,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            // Add tabs with updated styling to match LeaveRequestForm
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                containerColor = Color.Transparent,
                contentColor = AppColors.blue500,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                .height(3.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(AppColors.blue500, AppColors.teal500),
                                        start = Offset(0f, 0f),
                                        end = Offset(100f, 0f)
                                    ),
                                    shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                                )
                        )
                    }
                },
                divider = {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = AppColors.gray200
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (selectedTabIndex == index) AppColors.blue500 else AppColors.gray600,
                                letterSpacing = 0.1.sp
                            )
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            
            // Document content
            if (isLoadingDocuments) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = AppColors.blue500,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Loading documents...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.gray700
                        )
                    }
                }
            } else if (documentsError != null && documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Info",
                            tint = AppColors.gray400,
                            modifier = Modifier.size(64.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No documents found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.gray700
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Upload a document to get started",
                            fontSize = 14.sp,
                            color = AppColors.gray500,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        GradientButton(
                            text = "Upload Document",
                            icon = Icons.Default.Upload,
                            onClick = { 
                                selectedDocumentType = DocumentType.RESUME
                                showUploadDialog = true 
                            }
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Content container
                    Column(
                                modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Conditionally display the document sections based on selected tab
                        when (selectedTabIndex) {
                            0 -> {
                                // Personal section header (fixed, not scrollable)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AppColors.blue50)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                            imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                            tint = AppColors.blue500,
                                            modifier = Modifier.size(20.dp)
                                    )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = "Personal",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.gray800,
                                        letterSpacing = 0.1.sp
                                    )
                                }
                                
                                // Scrollable content with just the document cards
                Column(
                    modifier = Modifier
                                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Find documents for each type
                                    val resumeDocument = documents.find { 
                                        it.name == "Resume/Curriculum Vitae" || it.documentType == "RESUME" || it.documentType == "Resume/Curriculum Vitae"
                                    }
                                    val birthCertDocument = documents.find { 
                                        it.name == "Birth Certificate" || it.documentType == "BIRTH_CERTIFICATE" || it.documentType == "Birth Certificate"
                                    }
                                    val govIdDocument = documents.find { 
                                        it.name == "Government Issue ID" || it.documentType == "GOVERNMENT_ID" || it.documentType == "Government Issue ID"
                                    }
                                    
                                    // Resume document card
                                    EnhancedDocumentCard(
                            icon = Icons.Default.Description,
                                        iconTint = if (resumeDocument != null) AppColors.green else AppColors.gray400,
                            title = "Resume/Curriculum Vitae",
                                        status = if (resumeDocument != null) "Uploaded" else "Not uploaded yet",
                                        isUploaded = resumeDocument != null,
                                        document = resumeDocument,
                                        onViewClick = { doc -> scope.launch { viewDocument(doc) } },
                                        onReplaceClick = { doc -> 
                                            documentToReplace = doc.documentId
                                selectedDocumentType = DocumentType.RESUME
                                filePicker.launch("*/*") 
                                        },
                                        onDownloadClick = { doc -> scope.launch { downloadDocument(doc) } },
                                        onUploadClick = { 
                                            selectedDocumentType = DocumentType.RESUME 
                                            showUploadDialog = true
                                        },
                                        isDownloading = isDownloading,
                                        downloadingDocumentId = downloadingDocumentId
                                    )
                                    
                                    // Birth Certificate document card
                                    EnhancedDocumentCard(
                            icon = Icons.Default.Description,
                                        iconTint = if (birthCertDocument != null) AppColors.green else AppColors.gray400,
                            title = "Birth Certificate",
                                        status = if (birthCertDocument != null) "Uploaded" else "Not uploaded yet",
                                        isUploaded = birthCertDocument != null,
                                        document = birthCertDocument,
                                        onViewClick = { doc -> scope.launch { viewDocument(doc) } },
                                        onReplaceClick = { doc -> 
                                            documentToReplace = doc.documentId
                                            selectedDocumentType = DocumentType.BIRTH_CERTIFICATE
                                filePicker.launch("*/*") 
                                        },
                                        onDownloadClick = { doc -> scope.launch { downloadDocument(doc) } },
                                        onUploadClick = { 
                                            selectedDocumentType = DocumentType.BIRTH_CERTIFICATE
                                            showUploadDialog = true
                                        },
                                        isDownloading = isDownloading,
                                        downloadingDocumentId = downloadingDocumentId
                                    )
                                    
                                    // Government Issue ID document card
                                    EnhancedDocumentCard(
                            icon = Icons.Default.Description,
                                        iconTint = if (govIdDocument != null) AppColors.green else AppColors.gray400,
                            title = "Government Issue ID",
                                        status = if (govIdDocument != null) "Uploaded" else "Not uploaded yet",
                                        isUploaded = govIdDocument != null,
                                        document = govIdDocument,
                                        onViewClick = { doc -> scope.launch { viewDocument(doc) } },
                                        onReplaceClick = { doc -> 
                                            documentToReplace = doc.documentId
                                            selectedDocumentType = DocumentType.GOVERNMENT_ID
                                    filePicker.launch("*/*") 
                                },
                                        onDownloadClick = { doc -> scope.launch { downloadDocument(doc) } },
                                        onUploadClick = { 
                                            selectedDocumentType = DocumentType.GOVERNMENT_ID
                                            showUploadDialog = true
                                        },
                                        isDownloading = isDownloading,
                                        downloadingDocumentId = downloadingDocumentId
                                    )
                                    
                                    // Bottom spacer for better scrolling experience
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                            1 -> {
                                // Government Related section header (fixed, not scrollable)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AppColors.blue50)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Business,
                                            contentDescription = null,
                                            tint = AppColors.blue500,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = "Government Related",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.gray800,
                                        letterSpacing = 0.1.sp
                                    )
                                }
                                
                                // Sub-tabs for Government section
                                var govSelectedTabIndex by remember { mutableStateOf(0) }
                                val govTabTitles = listOf("BIR", "Social Security")
                                
                                // Government section tabs
                                TabRow(
                                    selectedTabIndex = govSelectedTabIndex,
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                                    containerColor = Color.Transparent,
                                    contentColor = AppColors.blue500,
                                    indicator = { tabPositions ->
                                        if (govSelectedTabIndex < tabPositions.size) {
                                            Box(
                                                Modifier
                                                    .tabIndicatorOffset(tabPositions[govSelectedTabIndex])
                                                    .height(3.dp)
                                                    .background(
                                                        brush = Brush.linearGradient(
                                                            colors = listOf(AppColors.blue500, AppColors.teal500),
                                                            start = Offset(0f, 0f),
                                                            end = Offset(100f, 0f)
                                                        ),
                                                        shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                                                    )
                                            )
                                        }
                                    },
                                    divider = {
                                        HorizontalDivider(
                                            thickness = 1.dp,
                                            color = AppColors.gray200
                                        )
                                    }
                                ) {
                                    govTabTitles.forEachIndexed { index, title ->
                                        Tab(
                                            selected = govSelectedTabIndex == index,
                                            onClick = { govSelectedTabIndex = index },
                                            text = {
                    Text(
                                                    text = title,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontWeight = if (govSelectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                                                    color = if (govSelectedTabIndex == index) AppColors.blue500 else AppColors.gray600,
                                                    letterSpacing = 0.1.sp
                                                )
                                            },
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                                
                                // Scrollable content with just the document cards
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(scrollState),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Find documents for each type
                                    val birForm1902 = documents.find { 
                                        it.name == "BIR Form 1902" || it.documentType == "BIR_FORM_1902" || it.documentType == "BIR Form 1902"
                                    }
                                    val tinIdDocument = documents.find { 
                                        it.name == "BIR TAX Identification Number" || it.documentType == "TIN_ID" || it.documentType == "BIR TAX Identification Number"
                                    }
                                    val birForm2316 = documents.find { 
                                        it.name == "BIR Form 2316" || it.documentType == "BIR_FORM_2316" || it.documentType == "BIR Form 2316"
                                    }
                                    val sssIdDocument = documents.find { 
                                        it.name == "SSS ID" || it.documentType == "SSS_ID" || it.documentType == "SSS ID"
                                    }
                                    val philhealthDocument = documents.find { 
                                        it.name == "Philhealth ID" || it.documentType == "PHILHEALTH_ID" || it.documentType == "Philhealth ID"
                                    }
                                    val pagibigDocument = documents.find { 
                                        it.name == "PAG-IBIG Membership ID" || it.documentType == "PAG_IBIG_ID" || it.documentType == "PAG-IBIG Membership ID"
                                    }
                                    
                                    // Show appropriate documents based on selected government tab
                                    if (govSelectedTabIndex == 0) {
                                        // BIR Documents
                                        // BIR Form 1902
                                        EnhancedDocumentCard(
                            icon = Icons.Default.Description,
                                            iconTint = if (birForm1902 != null) AppColors.green else AppColors.gray400,
                            title = "BIR Form 1902",
                                            status = if (birForm1902 != null) "Uploaded" else "Not uploaded yet",
                                            isUploaded = birForm1902 != null,
                                            document = birForm1902,
                                            onViewClick = { doc -> scope.launch { viewDocument(doc) } },
                                            onReplaceClick = { doc -> 
                                                documentToReplace = doc.documentId
                                                selectedDocumentType = DocumentType.BIR_FORM_1902
                                filePicker.launch("*/*") 
                                            },
                                            onDownloadClick = { doc -> scope.launch { downloadDocument(doc) } },
                                            onUploadClick = { 
                                                selectedDocumentType = DocumentType.BIR_FORM_1902
                                                showUploadDialog = true
                                            },
                                            isDownloading = isDownloading,
                                            downloadingDocumentId = downloadingDocumentId
                                        )
                                        
                                        // TIN ID
                                        EnhancedDocumentCard(
                            icon = Icons.Default.Description,
                                            iconTint = if (tinIdDocument != null) AppColors.green else AppColors.gray400,
                            title = "BIR TAX Identification Number",
                                            status = if (tinIdDocument != null) "Uploaded" else "Not uploaded yet",
                                            isUploaded = tinIdDocument != null,
                                            document = tinIdDocument,
                                            onViewClick = { doc -> scope.launch { viewDocument(doc) } },
                                            onReplaceClick = { doc -> 
                                                documentToReplace = doc.documentId
                                                selectedDocumentType = DocumentType.TIN_ID
                                filePicker.launch("*/*") 
                                            },
                                            onDownloadClick = { doc -> scope.launch { downloadDocument(doc) } },
                                            onUploadClick = { 
                                                selectedDocumentType = DocumentType.TIN_ID
                                                showUploadDialog = true
                                            },
                                            isDownloading = isDownloading,
                                            downloadingDocumentId = downloadingDocumentId
                                        )
                                        
                                        // BIR Form 2316
                                        EnhancedDocumentCard(
                            icon = Icons.Default.Description,
                                            iconTint = if (birForm2316 != null) AppColors.green else AppColors.gray400,
                            title = "BIR Form 2316",
                                            status = if (birForm2316 != null) "Uploaded" else "Not uploaded yet",
                                            isUploaded = birForm2316 != null,
                                            document = birForm2316,
                                            onViewClick = { doc -> scope.launch { viewDocument(doc) } },
                                            onReplaceClick = { doc -> 
                                                documentToReplace = doc.documentId
                                                selectedDocumentType = DocumentType.BIR_FORM_2316
                                filePicker.launch("*/*") 
                                            },
                                            onDownloadClick = { doc -> scope.launch { downloadDocument(doc) } },
                                            onUploadClick = { 
                                                selectedDocumentType = DocumentType.BIR_FORM_2316
                                                showUploadDialog = true
                                            },
                                            isDownloading = isDownloading,
                                            downloadingDocumentId = downloadingDocumentId
                                        )
                                    } else {
                                        // Social Security Documents
                                        // SSS ID
                                        EnhancedDocumentCard(
                            icon = Icons.Default.Description,
                                            iconTint = if (sssIdDocument != null) AppColors.green else AppColors.gray400,
                            title = "SSS ID",
                                            status = if (sssIdDocument != null) "Uploaded" else "Not uploaded yet",
                                            isUploaded = sssIdDocument != null,
                                            document = sssIdDocument,
                                            onViewClick = { doc -> scope.launch { viewDocument(doc) } },
                                            onReplaceClick = { doc -> 
                                                documentToReplace = doc.documentId
                                                selectedDocumentType = DocumentType.SSS_ID
                                filePicker.launch("*/*") 
                                            },
                                            onDownloadClick = { doc -> scope.launch { downloadDocument(doc) } },
                                            onUploadClick = { 
                                                selectedDocumentType = DocumentType.SSS_ID
                                                showUploadDialog = true
                                            },
                                            isDownloading = isDownloading,
                                            downloadingDocumentId = downloadingDocumentId
                                        )
                                        
                                        // Philhealth ID
                                        EnhancedDocumentCard(
                            icon = Icons.Default.Description,
                                            iconTint = if (philhealthDocument != null) AppColors.green else AppColors.gray400,
                            title = "Philhealth ID",
                                            status = if (philhealthDocument != null) "Uploaded" else "Not uploaded yet",
                                            isUploaded = philhealthDocument != null,
                                            document = philhealthDocument,
                                            onViewClick = { doc -> scope.launch { viewDocument(doc) } },
                                            onReplaceClick = { doc -> 
                                                documentToReplace = doc.documentId
                                                selectedDocumentType = DocumentType.PHILHEALTH_ID
                                    filePicker.launch("*/*") 
                                },
                                            onDownloadClick = { doc -> scope.launch { downloadDocument(doc) } },
                                            onUploadClick = { 
                                                selectedDocumentType = DocumentType.PHILHEALTH_ID
                                                showUploadDialog = true
                                            },
                                            isDownloading = isDownloading,
                                            downloadingDocumentId = downloadingDocumentId
                                        )
                                        
                                        // PAG-IBIG ID
                                        EnhancedDocumentCard(
                            icon = Icons.Default.Description,
                                            iconTint = if (pagibigDocument != null) AppColors.green else AppColors.gray400,
                                            title = "PAG-IBIG Membership ID",
                                            status = if (pagibigDocument != null) "Uploaded" else "Not uploaded yet",
                                            isUploaded = pagibigDocument != null,
                                            document = pagibigDocument,
                                            onViewClick = { doc -> scope.launch { viewDocument(doc) } },
                                            onReplaceClick = { doc -> 
                                                documentToReplace = doc.documentId
                                                selectedDocumentType = DocumentType.PAG_IBIG_ID
                                    filePicker.launch("*/*") 
                                },
                                            onDownloadClick = { doc -> scope.launch { downloadDocument(doc) } },
                                            onUploadClick = { 
                                                selectedDocumentType = DocumentType.PAG_IBIG_ID
                                                showUploadDialog = true
                                            },
                                            isDownloading = isDownloading,
                                            downloadingDocumentId = downloadingDocumentId
                                        )
                                    }
                                    
                                    // Bottom spacer for better scrolling experience
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
                            2 -> {
                                // Company section header (fixed, not scrollable)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AppColors.blue50)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Business,
                                            contentDescription = null,
                                            tint = AppColors.blue500,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                Text(
                                        text = "Company",
                                    fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.gray800,
                                        letterSpacing = 0.1.sp
                                    )
                                }
                                
                                // Scrollable content with just the document cards
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(scrollState),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Find documents for company section
                                    val confidentialityDoc = documents.find { 
                                        it.name == "Confidentiality Agreement" || it.documentType == "CONFIDENTIALITY_AGREEMENT" || it.documentType == "Confidentiality Agreement"
                                    }
                                    val employmentContract = documents.find { 
                                        it.name == "Employment Contract" || it.documentType == "EMPLOYMENT_CONTRACT" || it.documentType == "Employment Contract"
                                    }
                                    
                                    // Confidentiality Agreement
                                    EnhancedDocumentCard(
                                        icon = Icons.Default.Description,
                                        iconTint = if (confidentialityDoc != null) AppColors.green else AppColors.gray400,
                                        title = "Confidentiality Agreement",
                                        status = if (confidentialityDoc != null) "Uploaded" else "Not uploaded yet",
                                        isUploaded = confidentialityDoc != null,
                                        document = confidentialityDoc,
                                        onViewClick = { doc -> scope.launch { viewDocument(doc) } },
                                        onReplaceClick = { doc -> 
                                            documentToReplace = doc.documentId
                                            selectedDocumentType = DocumentType.CONFIDENTIALITY_AGREEMENT
                                            filePicker.launch("*/*") 
                                        },
                                        onDownloadClick = { doc -> scope.launch { downloadDocument(doc) } },
                                        onUploadClick = { 
                                            selectedDocumentType = DocumentType.CONFIDENTIALITY_AGREEMENT
                                            showUploadDialog = true
                                        },
                                        isDownloading = isDownloading,
                                        downloadingDocumentId = downloadingDocumentId
                                    )
                                    
                                    // Employment Contract
                                    EnhancedDocumentCard(
                                        icon = Icons.Default.Description,
                                        iconTint = if (employmentContract != null) AppColors.green else AppColors.gray400,
                                        title = "Employment Contract",
                                        status = if (employmentContract != null) "Uploaded" else "Not uploaded yet",
                                        isUploaded = employmentContract != null,
                                        document = employmentContract,
                                        onViewClick = { doc -> scope.launch { viewDocument(doc) } },
                                        onReplaceClick = { doc -> 
                                            documentToReplace = doc.documentId
                                            selectedDocumentType = DocumentType.EMPLOYMENT_CONTRACT
                                            filePicker.launch("*/*") 
                                        },
                                        onDownloadClick = { doc -> scope.launch { downloadDocument(doc) } },
                                        onUploadClick = { 
                                            selectedDocumentType = DocumentType.EMPLOYMENT_CONTRACT
                                            showUploadDialog = true
                                        },
                                        isDownloading = isDownloading,
                                        downloadingDocumentId = downloadingDocumentId
                                    )
                                    
                                    // Bottom spacer for better scrolling experience
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Show the upload dialog when needed
    if (showUploadDialog) {
        UploadDialog(
            showUploadDialog = showUploadDialog,
            isUploading = isUploading,
            selectedFile = selectedFile,
            selectedDocumentType = selectedDocumentType,
            fileName = fileName,
            uploadError = uploadError,
            onDismissRequest = {
                            if (!isUploading) {
                                showUploadDialog = false
                                selectedFile = null
                                selectedDocumentType = null
                                fileName = ""
                            }
                        },
            onSelectFile = {
                filePicker.launch("*/*")
            },
            onUpload = {
                uploadDocument()
            }
        )
    }
}

@Composable
fun UploadDialog(
    showUploadDialog: Boolean,
    isUploading: Boolean,
    selectedFile: Uri?,
    selectedDocumentType: DocumentType?,
    fileName: String,
    uploadError: String? = null,
    onDismissRequest: () -> Unit,
    onSelectFile: () -> Unit,
    onUpload: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppColors.white,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = AppColors.blue700.copy(alpha = 0.2f)
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Document Upload",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AppColors.gray800,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Message
                Text(
                    text = "Do you want to continue with document upload?",
                    color = AppColors.gray700,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismissRequest,
                        enabled = !isUploading,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppColors.blue500
                        ),
                        border = BorderStroke(1.dp, AppColors.blue300),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Cancel",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Continue button
                    Button(
                        onClick = onSelectFile,
                        enabled = !isUploading,
                            modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.blue500,
                            contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                    ) {
                                Text(
                            text = "Continue",
                            fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                    }
                }
            }
        }
    }
}

