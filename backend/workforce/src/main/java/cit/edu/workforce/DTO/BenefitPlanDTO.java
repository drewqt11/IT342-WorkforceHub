package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlanDTO {
    
    private String benefitPlanId;
    private String planName;
    private String description;
    private String planType;
    private String provider;
    private String coverageDetails;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
}

// New file: DTO for benefit plans 