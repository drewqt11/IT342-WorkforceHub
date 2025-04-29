package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * BenefitDependentDTO - Data Transfer Object for benefit dependent information
 * New file: This DTO is used to transfer benefit dependent data between the service layer and API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitDependentDTO {
    
    private String dependentId;
    private String enrollmentId;
    private String name;
    private String relationship;
    private LocalDate birthdate;
} 