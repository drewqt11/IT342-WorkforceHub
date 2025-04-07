package cit.edu.workforce.Service;

import cit.edu.workforce.Entity.DocumentEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Repository.DocumentRepository;
import cit.edu.workforce.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentService {

    @Value("${app.document.upload-dir:uploads}")
    private String uploadDir;

    private final DocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, EmployeeRepository employeeRepository) {
        this.documentRepository = documentRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Upload a document for an employee
     *
     * @param employeeId ID of the employee to upload the document for
     * @param file File to upload
     * @param documentType Type of document (e.g., "CERTIFICATE", "ID", "CONTRACT")
     * @return The created DocumentEntity
     */
    @Transactional
    public DocumentEntity uploadDocument(String employeeId, MultipartFile file, String documentType) {
        // Validate employee exists
        EmployeeEntity employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        // Create directory if it doesn't exist
        File directory = new File(uploadDir + "/" + employeeId);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try {
            // Generate a unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".") ?
                originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
            String uniqueFilename = UUID.randomUUID() + fileExtension;
            String filePath = uploadDir + "/" + employeeId + "/" + uniqueFilename;

            // Save file to disk
            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());

            // Save document metadata to database
            DocumentEntity document = new DocumentEntity();
            document.setDocumentType(documentType);
            document.setFilePath(filePath);
            document.setFileName(originalFilename);
            document.setStatus("PENDING");
            document.setUploadedAt(LocalDateTime.now());
            document.setEmployee(employee);

            return documentRepository.save(document);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save document: " + e.getMessage());
        }
    }

    /**
     * Get all documents for an employee
     *
     * @param employeeId ID of the employee to get documents for
     * @return List of DocumentEntity objects
     */
    @Transactional(readOnly = true)
    public List<DocumentEntity> getDocumentsByEmployeeId(String employeeId) {
        // Check if employee exists
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }

        return documentRepository.findByEmployeeEmployeeId(employeeId);
    }

    /**
     * Get a document by ID
     *
     * @param documentId ID of the document to get
     * @return DocumentEntity
     */
    @Transactional(readOnly = true)
    public DocumentEntity getDocumentById(String documentId) {
        return documentRepository.findById(documentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    /**
     * Approve a document
     *
     * @param documentId ID of the document to approve
     * @return The updated DocumentEntity
     */
    @Transactional
    public DocumentEntity approveDocument(String documentId) {
        DocumentEntity document = documentRepository.findById(documentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        document.setStatus("APPROVED");
        document.setApprovedAt(LocalDateTime.now());

        return documentRepository.save(document);
    }

    /**
     * Reject a document
     *
     * @param documentId ID of the document to reject
     * @return The updated DocumentEntity
     */
    @Transactional
    public DocumentEntity rejectDocument(String documentId) {
        DocumentEntity document = documentRepository.findById(documentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        document.setStatus("REJECTED");

        return documentRepository.save(document);
    }

    /**
     * Check if the current user has access to a document
     *
     * @param documentId ID of the document to check
     * @return true if the user has access, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean hasAccessToDocument(String documentId) {
        // Get current authenticated user
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        // Find document
        Optional<DocumentEntity> documentOpt = documentRepository.findById(documentId);
        if (documentOpt.isEmpty()) {
            return false;
        }

        DocumentEntity document = documentOpt.get();
        EmployeeEntity employee = document.getEmployee();

        // Check if user is the owner of the document or has admin/HR role
        boolean isOwner = employee.getEmail().equals(username);
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
            .getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));

        return isOwner || isAdmin;
    }
}