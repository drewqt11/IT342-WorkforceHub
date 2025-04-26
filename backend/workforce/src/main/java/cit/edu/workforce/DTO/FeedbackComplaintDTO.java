package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * FeedbackComplaintDTO - Data Transfer Object for feedback and complaint information
 * New file: This DTO is used to transfer feedback and complaint data between the service layer and API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackComplaintDTO {
    
    private String feedbackId;
    private String employeeId;
    private String employeeName;
    private String resolverId;
    private String resolverName;
    private String category;
    private String subject;
    private String description;
    private LocalDateTime submittedAt;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;
    private String status;
} 