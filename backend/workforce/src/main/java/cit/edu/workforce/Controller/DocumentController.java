package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.DocumentDTO;
import cit.edu.workforce.Entity.DocumentEntity;
import cit.edu.workforce.Service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

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

    @PostMapping("/employees/{employeeId}/documents")
    @Operation(summary = "Upload document", description = "Upload a document for an employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @employeeService.isCurrentEmployee(#employeeId)")
    public ResponseEntity<DocumentDTO> uploadDocument(
            @PathVariable String employeeId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType) {
        DocumentEntity document = documentService.uploadDocument(employeeId, file, documentType);
        return ResponseEntity.ok(documentService.convertToDTO(document));
    }

    @GetMapping("/employees/{employeeId}/documents")
    @Operation(summary = "Get employee documents", description = "Get all documents for an employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @employeeService.isCurrentEmployee(#employeeId)")
    public ResponseEntity<List<DocumentDTO>> getEmployeeDocuments(@PathVariable String employeeId) {
        List<DocumentEntity> documents = documentService.getDocumentsByEmployeeId(employeeId);
        List<DocumentDTO> documentDTOs = documents.stream()
            .map(documentService::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(documentDTOs);
    }

    @GetMapping("/documents/{documentId}")
    @Operation(summary = "Get document by ID", description = "Get a document by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @documentService.hasAccessToDocument(#documentId)")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable String documentId) {
        DocumentEntity document = documentService.getDocumentById(documentId);
        return ResponseEntity.ok(documentService.convertToDTO(document));
    }

    @PutMapping("/documents/{documentId}")
    @Operation(summary = "Replace document", description = "Replace an existing document with a new one")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @documentService.hasAccessToDocument(#documentId)")
    public ResponseEntity<DocumentDTO> replaceDocument(
            @PathVariable String documentId,
            @RequestParam("file") MultipartFile file) {
        DocumentEntity document = documentService.replaceDocument(documentId, file);
        return ResponseEntity.ok(documentService.convertToDTO(document));
    }

    @PatchMapping("/hr/documents/{documentId}/approve")
    @Operation(summary = "Approve document", description = "Approve a document (HR or Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<DocumentDTO> approveDocument(@PathVariable String documentId) {
        DocumentEntity document = documentService.approveDocument(documentId);
        return ResponseEntity.ok(documentService.convertToDTO(document));
    }

    @PatchMapping("/hr/documents/{documentId}/reject")
    @Operation(summary = "Reject document", description = "Reject a document (HR or Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<DocumentDTO> rejectDocument(@PathVariable String documentId) {
        DocumentEntity document = documentService.rejectDocument(documentId);
        return ResponseEntity.ok(documentService.convertToDTO(document));
    }

    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable String documentId) {
        DocumentEntity document = documentService.getDocumentById(documentId);
        byte[] fileContent = documentService.getDocumentContent(documentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(document.getFileType()));
        headers.setContentDispositionFormData("attachment", document.getFileName());

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }

    @GetMapping("/documents/{documentId}/view")
    public ResponseEntity<byte[]> viewDocument(@PathVariable String documentId) {
        DocumentEntity document = documentService.getDocumentById(documentId);
        byte[] fileContent = documentService.getDocumentContent(documentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(document.getFileType()));
        headers.setContentDispositionFormData("inline", document.getFileName());

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }
}