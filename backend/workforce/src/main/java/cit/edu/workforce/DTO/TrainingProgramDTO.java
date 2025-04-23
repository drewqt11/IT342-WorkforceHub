package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgramDTO {
    
    private String programId;
    private String programName;
    private String description;
    private String programType;
    private String category;
    private String deliveryMethod;
    private String provider;
    private Integer durationHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String instructor;
    private Integer maxParticipants;
    private String prerequisites;
    private String materialsUrl;
    private Boolean certificationOffered;
    private String certificationName;
    private Integer certificationValidityMonths;
    private Boolean isMandatory;
    private Boolean isActive;
    private String createdById;
    private String createdByName;
    private List<String> applicableDepartmentIds;
    private List<String> applicableDepartmentNames;
    private Integer enrollmentsCount;
}

// New file: DTO for training programs 