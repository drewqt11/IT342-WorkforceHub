package cit.edu.workforce.Controller;

import cit.edu.workforce.Entity.DocumentEntity;
import cit.edu.workforce.Service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Document Management", description = "Document management APIs")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/employees/{employeeId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload document", description = "Upload a document for an employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @employeeService.isCurrentEmployee(#employeeId)")
    public ResponseEntity<DocumentEntity> uploadDocument(
            @PathVariable String employeeId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType) {

        DocumentEntity document = documentService.uploadDocument(employeeId, file, documentType);
        return new ResponseEntity<>(document, HttpStatus.CREATED);
    }

    @GetMapping("/employees/{employeeId}/documents")
    @Operation(summary = "Get employee documents", description = "Get all documents for an employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @employeeService.isCurrentEmployee(#employeeId)")
    public ResponseEntity<List<DocumentEntity>> getEmployeeDocuments(@PathVariable String employeeId) {
        List<DocumentEntity> documents = documentService.getDocumentsByEmployeeId(employeeId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/documents/{documentId}")
    @Operation(summary = "Get document by ID", description = "Get a document by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @documentService.hasAccessToDocument(#documentId)")
    public ResponseEntity<DocumentEntity> getDocumentById(@PathVariable String documentId) {
        DocumentEntity document = documentService.getDocumentById(documentId);
        return ResponseEntity.ok(document);
    }

    @PatchMapping("/hr/documents/{documentId}/approve")
    @Operation(summary = "Approve document", description = "Approve a document (HR or Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<DocumentEntity> approveDocument(@PathVariable String documentId) {
        DocumentEntity document = documentService.approveDocument(documentId);
        return ResponseEntity.ok(document);
    }

    @PatchMapping("/hr/documents/{documentId}/reject")
    @Operation(summary = "Reject document", description = "Reject a document (HR or Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<DocumentEntity> rejectDocument(@PathVariable String documentId) {
        DocumentEntity document = documentService.rejectDocument(documentId);
        return ResponseEntity.ok(document);
    }
}