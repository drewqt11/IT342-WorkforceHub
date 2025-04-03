package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.DocumentDTO;
import cit.edu.workforce.Service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/employees/{employeeId}/documents")
    @PreAuthorize("hasRole('HR_ADMIN') or @userSecurity.isEmployee(#employeeId)")
    public ResponseEntity<List<DocumentDTO>> getEmployeeDocuments(@PathVariable UUID employeeId) {
        List<DocumentDTO> documents = documentService.getEmployeeDocuments(employeeId);
        return ResponseEntity.ok(documents);
    }

    @PostMapping("/employees/{employeeId}/documents")
    @PreAuthorize("hasRole('HR_ADMIN') or @userSecurity.isEmployee(#employeeId)")
    public ResponseEntity<DocumentDTO> uploadDocument(
            @PathVariable UUID employeeId,
            @RequestParam("type") String documentType,
            @RequestParam("file") MultipartFile file) {
        
        DocumentDTO uploadedDocument = documentService.uploadDocument(employeeId, documentType, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedDocument);
    }

    @PutMapping("/documents/{documentId}/approve")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<DocumentDTO> approveDocument(@PathVariable UUID documentId) {
        DocumentDTO approvedDocument = documentService.approveDocument(documentId);
        return ResponseEntity.ok(approvedDocument);
    }

    @PutMapping("/documents/{documentId}/reject")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<DocumentDTO> rejectDocument(@PathVariable UUID documentId) {
        DocumentDTO rejectedDocument = documentService.rejectDocument(documentId);
        return ResponseEntity.ok(rejectedDocument);
    }

    @DeleteMapping("/documents/{documentId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
} 