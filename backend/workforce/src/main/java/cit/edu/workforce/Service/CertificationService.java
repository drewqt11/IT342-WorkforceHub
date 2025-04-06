package cit.edu.workforce.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cit.edu.workforce.Entity.CertificationEntity;
import cit.edu.workforce.Entity.DocumentEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Repository.CertificationRepository;
import cit.edu.workforce.Repository.EmployeeRepository;

@Service
public class CertificationService {

    private final CertificationRepository certificationRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentService documentService;

    @Autowired
    public CertificationService(
            CertificationRepository certificationRepository,
            EmployeeRepository employeeRepository,
            DocumentService documentService) {
        this.certificationRepository = certificationRepository;
        this.employeeRepository = employeeRepository;
        this.documentService = documentService;
    }

    @Transactional(readOnly = true)
    public List<CertificationEntity> getCertificationsByEmployeeId(UUID employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        return certificationRepository.findByEmployee(employee);
    }

    @Transactional(readOnly = true)
    public CertificationEntity getCertificationById(UUID certificationId) {
        return certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certification not found"));
    }

    @Transactional
    public CertificationEntity createCertification(
            UUID employeeId, 
            String certificateName, 
            LocalDate issueDate, 
            LocalDate expiryDate,
            UUID documentId) {
        
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        // Validate dates
        if (issueDate != null && expiryDate != null && expiryDate.isBefore(issueDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry date cannot be before issue date");
        }
        
        // Create certification
        CertificationEntity certification = new CertificationEntity();
        certification.setCertificateName(certificateName);
        certification.setIssueDate(issueDate);
        certification.setExpiryDate(expiryDate);
        certification.setStatus("PENDING");
        certification.setEmployee(employee);
        
        // If a document is provided, verify it exists and belongs to the employee
        if (documentId != null) {
            DocumentEntity document = documentService.getDocumentById(documentId);
            
            if (document.getEmployee() == null || !document.getEmployee().getEmployeeId().equals(employeeId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document does not belong to the specified employee");
            }
        }
        
        return certificationRepository.save(certification);
    }

    @Transactional
    public CertificationEntity updateCertification(
            UUID certificationId,
            String certificateName,
            LocalDate issueDate,
            LocalDate expiryDate) {
        
        CertificationEntity certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certification not found"));
        
        // Ensure user has access to this certification
        if (!hasAccessToCertification(certification)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to update this certification");
        }
        
        // Validate dates
        if (issueDate != null && expiryDate != null && expiryDate.isBefore(issueDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry date cannot be before issue date");
        }
        
        // Update fields if provided
        if (certificateName != null) {
            certification.setCertificateName(certificateName);
        }
        
        if (issueDate != null) {
            certification.setIssueDate(issueDate);
        }
        
        if (expiryDate != null) {
            certification.setExpiryDate(expiryDate);
        }
        
        return certificationRepository.save(certification);
    }

    @Transactional
    public CertificationEntity approveCertification(UUID certificationId) {
        CertificationEntity certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certification not found"));
        
        certification.setStatus("APPROVED");
        return certificationRepository.save(certification);
    }

    @Transactional
    public CertificationEntity rejectCertification(UUID certificationId) {
        CertificationEntity certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certification not found"));
        
        certification.setStatus("REJECTED");
        return certificationRepository.save(certification);
    }
    
    @Transactional(readOnly = true)
    public boolean hasAccessToCertification(CertificationEntity certification) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        // HR and Admins have access to all certifications
        boolean isAdminOrHR = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
        
        if (isAdminOrHR) {
            return true;
        }
        
        // Regular employees only have access to their own certifications
        String email = null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        
        return certification.getEmployee() != null && 
               certification.getEmployee().getEmail().equals(email);
    }
    
    @Transactional(readOnly = true)
    public boolean hasAccessToCertification(UUID certificationId) {
        Optional<CertificationEntity> certificationOpt = certificationRepository.findById(certificationId);
        return certificationOpt.isPresent() && hasAccessToCertification(certificationOpt.get());
    }
} 