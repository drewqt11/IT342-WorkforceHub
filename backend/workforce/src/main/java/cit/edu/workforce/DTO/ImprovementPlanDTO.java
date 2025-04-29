package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * ImprovementPlanDTO - Data Transfer Object for performance improvement plan information
 * New file: This DTO is used to transfer improvement plan data between the service layer and API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImprovementPlanDTO {
    
    private String planId;
    private String employeeId;
    private String employeeName;
    private String initiatorId;
    private String initiatorName;
    private String reason;
    private String actionSteps;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private long daysRemaining;
} 