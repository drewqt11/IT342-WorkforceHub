package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TrainingEnrollmentDTO - Data Transfer Object for training enrollment information
 * New file: This DTO is used to transfer training enrollment data between the service layer and API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingEnrollmentDTO {
    
    private String enrollmentId;
    private LocalDate enrolledDate;
    private String enrollmentType;
    private String status;
    private LocalDateTime completionDate;
    
    // Employee information
    private String employeeId;
    private String employeeName;
    
    // Training Program information (null if event-based)
    private String trainingId;
    private String trainingTitle;
    private String trainingProvider;
    private String trainingMode;
    
    // Event information (null if program-based)
    private String eventId;
    private String eventType;
    private String eventTitle;
    private LocalDateTime eventDatetime;
    
    // Certificate information
    private List<CertificateDTO> certificates;
    private int certificateCount;
} 