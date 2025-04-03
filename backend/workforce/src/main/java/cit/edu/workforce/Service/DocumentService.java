package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.DocumentDTO;
import cit.edu.workforce.Entity.Document;
import cit.edu.workforce.Entity.Employee;
import cit.edu.workforce.Repository.DocumentRepository;
import cit.edu.workforce.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;
    private final Path uploadDir = Paths.get("uploads/documents");

    @Autowired
    public DocumentService(DocumentRepository documentRepository,
                          EmployeeRepository employeeRepository) {
        this.documentRepository = documentRepository;
        this.employeeRepository = employeeRepository;
        
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public List<DocumentDTO> getEmployeeDocuments(UUID employeeId) {
        // Verify employee exists
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return documentRepository.findByEmployeeEmployeeId(employeeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DocumentDTO uploadDocument(UUID employeeId, String documentType, MultipartFile file) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        try {
            // Generate unique filename
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = uploadDir.resolve(filename);
            
            // Save file to disk
            Files.copy(file.getInputStream(), targetPath);
            
            // Create document record
            Document document = new Document();
            document.setDocumentType(documentType);
            document.setFilePath(targetPath.toString());
            document.setStatus("PENDING");
            document.setUploadedAt(LocalDateTime.now());
            document.setEmployee(employee);
            
            Document savedDocument = documentRepository.save(document);
            return convertToDTO(savedDocument);
            
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload document: " + e.getMessage());
        }
    }

    public DocumentDTO approveDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        
        document.setStatus("APPROVED");
        document.setApprovedAt(LocalDateTime.now());
        
        Document updatedDocument = documentRepository.save(document);
        return convertToDTO(updatedDocument);
    }

    public DocumentDTO rejectDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        
        document.setStatus("REJECTED");
        
        Document updatedDocument = documentRepository.save(document);
        return convertToDTO(updatedDocument);
    }

    public void deleteDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        
        try {
            // Delete file from disk if it exists
            Path filePath = Paths.get(document.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            
            // Delete database record
            documentRepository.delete(document);
            
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete document file: " + e.getMessage());
        }
    }

    private DocumentDTO convertToDTO(Document document) {
        DocumentDTO dto = new DocumentDTO();
        dto.setDocumentId(document.getDocumentId());
        dto.setDocumentType(document.getDocumentType());
        dto.setFilePath(document.getFilePath());
        dto.setStatus(document.getStatus());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setApprovedAt(document.getApprovedAt());
        if (document.getEmployee() != null) {
            dto.setEmployeeId(document.getEmployee().getEmployeeId());
        }
        return dto;
    }
} 