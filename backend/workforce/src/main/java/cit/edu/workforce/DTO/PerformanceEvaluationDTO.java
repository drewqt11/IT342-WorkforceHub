package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * PerformanceEvaluationDTO - Data Transfer Object for performance evaluation information
 * New file: This DTO is used to transfer performance evaluation data between the service layer and API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceEvaluationDTO {
    
    private String evaluationId;
    private String employeeId;
    private String employeeName;
    private String reviewerId;
    private String reviewerName;
    private LocalDate evaluationPeriodStart;
    private LocalDate evaluationPeriodEnd;
    private BigDecimal overallScore;
    private String remarks;
    private LocalDate evaluationDate;
} 