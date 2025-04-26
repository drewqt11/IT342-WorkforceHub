package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * BenefitEnrollmentDTO - Data Transfer Object for benefit enrollment information
 * New file: This DTO is used to transfer benefit enrollment data between the service layer and API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitEnrollmentDTO {

    private String enrollmentId;
    private String employeeId;
    private String employeeName;
    private String planId;
    private String planName;
    private String planType;
    private LocalDate enrollmentDate;
    private String status;
    private String cancellationReason;
    private List<BenefitDependentDTO> dependents;
    private int dependentCount;
}