package cit.edu.workforcehub.api.services

import cit.edu.workforcehub.api.models.Document
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit service interface for document-related API endpoints.
 */
interface DocumentService {
    /**
     * Upload a document for an employee.
     */
    @Multipart
    @POST("/api/employees/{employeeId}/documents")
    suspend fun uploadDocument(
        @Path("employeeId") employeeId: String,
        @Part file: MultipartBody.Part,
        @Query("documentType") documentType: String,
        @Query("documentName") documentName: String? = null
    ): Response<Document>
    
    /**
     * Replace an existing document.
     */
    @Multipart
    @PUT("/api/documents/{documentId}")
    suspend fun replaceDocument(
        @Path("documentId") documentId: String,
        @Part file: MultipartBody.Part,
        @Query("documentName") documentName: String? = null
    ): Response<Document>
    
    /**
     * Get information about a specific document.
     */
    @GET("/api/documents/{documentId}")
    suspend fun getDocument(
        @Path("documentId") documentId: String
    ): Response<Document>
    
    /**
     * Get all documents for an employee.
     */
    @GET("/api/employees/{employeeId}/documents")
    suspend fun getEmployeeDocuments(
        @Path("employeeId") employeeId: String
    ): Response<List<Document>>
    
    /**
     * Download a document.
     */
    @Streaming
    @GET("/api/documents/{documentId}/download")
    suspend fun downloadDocument(
        @Path("documentId") documentId: String
    ): Response<ResponseBody>

    /**
     * View a document directly.
     * This endpoint returns a URL as plain text, not JSON.
     */
    @Headers("Accept: text/plain")
    @GET("/api/documents/{documentId}/view")
    suspend fun viewDocument(
        @Path("documentId") documentId: String
    ): Response<ResponseBody>
} 