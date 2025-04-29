package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CertificateDTO - Data Transfer Object for certificate information
 * New file: This DTO is used to transfer certificate data between the service layer and API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDTO {
    
    private String certificateId;
    private String filePath;
    private LocalDateTime uploadedAt;
    private String status;
    private LocalDateTime verifiedAt;
    private String remarks;
    
    // Training Enrollment information
    private String enrollmentId;
    
    // HR Admin information (verifier)
    private String verifiedById;
    private String verifiedByName;
} 