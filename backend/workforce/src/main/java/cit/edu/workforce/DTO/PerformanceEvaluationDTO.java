package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceEvaluationDTO {
    
    private String evaluationId;
    private String employeeId;
    private String employeeName;
    private String evaluationType;
    private LocalDate evaluationPeriodStart;
    private LocalDate evaluationPeriodEnd;
    private LocalDate submittedDate;
    private LocalDate dueDate;
    private String status;
    private Integer overallRating;
    private String performanceSummary;
    private String strengths;
    private String areasForImprovement;
    private String goalsAchieved;
    private String goalsForNextPeriod;
    private String evaluatorId;
    private String evaluatorName;
    private String evaluatorComments;
    private String employeeComments;
    private LocalDate acknowledgementDate;
}

// New file: DTO for performance evaluations 