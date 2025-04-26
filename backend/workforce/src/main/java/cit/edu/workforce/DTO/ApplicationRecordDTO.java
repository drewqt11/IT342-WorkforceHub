package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * ApplicationRecordDTO - Data Transfer Object for application record information
 * New file: This DTO is used to transfer application record data between the service layer and API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRecordDTO {
    
    private String applicationId;
    private String applicantId;
    private String applicantName;
    private String jobId;
    private String jobTitle;
    private String departmentName;
    private String status;
    private String remarks;
    private String reviewedBy;
    private String reviewerName;
    private LocalDateTime reviewedAt;
    private boolean isInternal; // Whether the applicant is an internal employee
    private String resumePath; // Path to the applicant's resume
} 