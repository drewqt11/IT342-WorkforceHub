package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationDTO {
    
    private String applicationId;
    private String postingId;
    private String jobTitle;
    private String departmentName;
    private String employeeId;
    private String applicantName;
    private String applicantEmail;
    private String applicantPhone;
    private String resumeUrl;
    private String coverLetterUrl;
    private LocalDate applicationDate;
    private String status;
    private String currentStage;
    private String source;
    private String internalNotes;
    private LocalDate interviewDate;
    private String interviewFeedback;
    private String reviewedById;
    private String reviewedByName;
    private LocalDate reviewDate;
}

// New file: DTO for job applications 