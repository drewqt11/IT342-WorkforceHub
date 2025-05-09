package cit.edu.workforcehub.api.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Model for document information returned from API.
 */
@JsonClass(generateAdapter = true)
data class Document(
    @Json(name = "documentId") val documentId: String,
    @Json(name = "employeeId") val employeeId: String,
    @Json(name = "documentType") val documentType: String,
    @Json(name = "fileName") val fileName: String,
    @Json(name = "status") val status: String,
    @Json(name = "uploadedAt") val uploadDate: String,
    @Json(name = "approvedAt") val approvedDate: String? = null,
    
    // Fields that might not be in the API response but are used in the app
    @Json(name = "fileType") val fileType: String = getMimeType(fileName),
    @Json(name = "fileSize") val fileSize: Long = 0,
    @Json(name = "name") val name: String = fileName, // Use fileName if name not provided
    @Json(name = "approvedBy") val approvedBy: String? = null,
    @Json(name = "url") val url: String? = null
) {
    companion object {
        /**
         * Get MIME type from file name
         */
        private fun getMimeType(fileName: String): String {
            return when {
                fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                fileName.endsWith(".doc", ignoreCase = true) -> "application/msword"
                fileName.endsWith(".docx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                fileName.endsWith(".xls", ignoreCase = true) -> "application/vnd.ms-excel"
                fileName.endsWith(".xlsx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                fileName.endsWith(".txt", ignoreCase = true) -> "text/plain"
                fileName.endsWith(".jpg", ignoreCase = true) -> "image/jpeg"
                fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg" 
                fileName.endsWith(".png", ignoreCase = true) -> "image/png"
                fileName.endsWith(".gif", ignoreCase = true) -> "image/gif"
                else -> "application/octet-stream"  // Default mime type for unknown file types
            }
        }
    }
}

/**
 * Enum of document status values.
 */
enum class DocumentStatus(val value: String) {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED")
}

/**
 * Enum of common document types.
 */
enum class DocumentType(val value: String, val displayName: String) {
    RESUME("RESUME", "Resume"),
    ID("ID", "ID Document"),
    CERTIFICATE("CERTIFICATE", "Certificate"),
    CONTRACT("CONTRACT", "Contract"),
    OTHER("OTHER", "Other")
} 