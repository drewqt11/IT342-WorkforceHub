package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DependentEnrollmentDTO {
    private String dependentEnrollmentId;
    private String dependentId;
    private String dependentName;
    private String relationship;
    private LocalDate dateOfBirth;
    private String gender;
    private boolean isPrimaryBeneficiary;
    private String coverageLevel;
    private LocalDateTime effectiveDate;
    private LocalDateTime endDate;
    private String status;
    private String verificationStatus;
    private LocalDateTime verificationDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}