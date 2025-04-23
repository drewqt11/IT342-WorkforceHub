package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceImprovementPlanDTO {
    
    private String pipId;
    private String employeeId;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String performanceIssues;
    private String improvementGoals;
    private String actionPlan;
    private String resourcesProvided;
    private String evaluationCriteria;
    private String consequences;
    private String managerId;
    private String managerName;
    private String hrRepresentativeId;
    private String hrRepresentativeName;
    private String progressNotes;
    private String finalOutcome;
    private LocalDate completionDate;
}

// New file: DTO for performance improvement plans 