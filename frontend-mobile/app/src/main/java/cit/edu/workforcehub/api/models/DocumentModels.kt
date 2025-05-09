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
    @Json(name = "name") val name: String = getDisplayNameFromDocumentType(documentType),
    @Json(name = "approvedBy") val approvedBy: String? = null,
    @Json(name = "url") val url: String? = null
) {
    companion object {
        /**
         * Get MIME type from file name
         */
        fun getMimeType(fileName: String): String {
            return when {
                fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                fileName.endsWith(".doc", ignoreCase = true) -> "application/msword"
                fileName.endsWith(".docx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                fileName.endsWith(".png", ignoreCase = true) -> "image/png"
                fileName.endsWith(".txt", ignoreCase = true) -> "text/plain"
                else -> "application/octet-stream"
            }
        }
        
        /**
         * Map document type from API to proper display name
         */
        fun getDisplayNameFromDocumentType(documentType: String): String {
            return when (documentType) {
                "RESUME" -> "Resume/Curriculum Vitae"
                "BIRTH_CERTIFICATE" -> "Birth Certificate"
                "GOVERNMENT_ID" -> "Government Issue ID"
                "SSS_ID" -> "SSS ID"
                "TIN_ID" -> "BIR TAX Identification Number"
                "PHILHEALTH_ID" -> "PhilHealth ID"
                "PAG_IBIG_ID" -> "Pag-IBIG ID"
                "HMO_ID" -> "HMO ID"
                "BIR_FORM_1902" -> "BIR Form 1902"
                "BIR_FORM_2316" -> "BIR Form 2316"
                "CONFIDENTIALITY_AGREEMENT" -> "Confidentiality Agreement"
                "EMPLOYMENT_CONTRACT" -> "Employment Contract"
                else -> documentType
            }
        }
    }
}

/**
 * Enum for document types.
 */
enum class DocumentType(val value: String, val displayName: String) {
    RESUME("Resume/Curriculum Vitae", "Resume/Curriculum Vitae"),
    BIRTH_CERTIFICATE("Birth Certificate", "Birth Certificate"),
    GOVERNMENT_ID("Government Issue ID", "Government Issue ID"),
    SSS_ID("SSS ID", "SSS ID"),
    TIN_ID("BIR TAX Identification Number", "BIR TAX Identification Number"),
    PHILHEALTH_ID("PhilHealth ID", "PhilHealth ID"),
    PAG_IBIG_ID("Pag-IBIG ID", "Pag-IBIG ID"),
    HMO_ID("HMO ID", "HMO ID"),
    BIR_FORM_1902("BIR Form 1902", "BIR Form 1902"),
    BIR_FORM_2316("BIR Form 2316", "BIR Form 2316"),
    CONFIDENTIALITY_AGREEMENT("Confidentiality Agreement", "Confidentiality Agreement"),
    EMPLOYMENT_CONTRACT("Employment Contract", "Employment Contract")
}

/**
 * Enum for document status.
 */
enum class DocumentStatus(val value: String, val displayName: String) {
    PENDING("PENDING", "Pending"),
    APPROVED("APPROVED", "Approved"),
    REJECTED("REJECTED", "Rejected")
} 