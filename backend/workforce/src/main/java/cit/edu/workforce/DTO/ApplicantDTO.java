package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * ApplicantDTO - Data Transfer Object for applicant information
 * New file: This DTO is used to transfer applicant data between the service layer and API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantDTO {
    
    private String applicantId;
    private String userId; // If internal applicant
    private String fullName;
    private String email;
    private String phoneNumber;
    private String resumePdfPath;
    private boolean isInternal;
    private LocalDate applicationDate;
    private int totalApplications; // Count of job applications from this applicant
} 