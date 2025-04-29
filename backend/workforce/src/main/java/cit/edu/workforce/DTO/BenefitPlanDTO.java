package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BenefitPlanDTO - Data Transfer Object for benefit plan information
 * New file: This DTO is used to transfer benefit plan data between the service layer and API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlanDTO {
    
    private String planId;
    private String planName;
    private String description;
    private String provider;
    private String eligibility;
    private String planType;
    private BigDecimal maxCoverage;
    private LocalDateTime createdAt;
    private boolean isActive;
    private Long enrollmentCount;
} 