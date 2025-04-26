package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * JobListingDTO - Data Transfer Object for job listing information
 * New file: This DTO is used to transfer job listing data between the service layer and API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobListingDTO {
    
    private String jobId;
    private String title;
    private String departmentId;
    private String departmentName;
    private String jobDescription;
    private String qualifications;
    private String employmentType; // Full-time, Part-time, Contract
    private String jobType; // Internal, External
    private LocalDate datePosted;
    private LocalDate applicationDeadline;
    private boolean isActive;
    private int totalApplications; // Count of applications for this job listing
} 