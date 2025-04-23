package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingEnrollmentDTO {
    
    private String enrollmentId;
    private String employeeId;
    private String employeeName;
    private String programId;
    private String programName;
    private String programType;
    private LocalDate enrollmentDate;
    private LocalDate completionDate;
    private LocalDate dueDate;
    private String status;
    private Integer completionPercentage;
    private Double score;
    private String certificateUrl;
    private LocalDate certificateExpiryDate;
    private String feedback;
    private String instructorComments;
    private String assignedById;
    private String assignedByName;
}

// New file: DTO for training enrollments 