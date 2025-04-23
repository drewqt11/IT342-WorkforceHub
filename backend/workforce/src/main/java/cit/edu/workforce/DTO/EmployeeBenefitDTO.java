package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBenefitDTO {
    
    private String enrollmentId;
    private String employeeId;
    private String employeeName;
    private String benefitPlanId;
    private String planName;
    private String planType;
    private LocalDate enrollmentDate;
    private LocalDate coverageStartDate;
    private LocalDate coverageEndDate;
    private String status;
    private Integer dependentsCount;
    private String additionalDetails;
}

// New file: DTO for employee benefit enrollments 