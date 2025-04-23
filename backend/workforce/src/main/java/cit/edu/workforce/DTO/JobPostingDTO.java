package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingDTO {
    
    private String postingId;
    private String title;
    private String description;
    private String qualifications;
    private String responsibilities;
    private String departmentId;
    private String departmentName;
    private String jobTitleId;
    private String jobTitleName;
    private String location;
    private String employmentType;
    private String experienceLevel;
    private Double salaryMin;
    private Double salaryMax;
    private LocalDate postingDate;
    private LocalDate closingDate;
    private Boolean isInternal;
    private Boolean isActive;
    private String externalUrl;
    private String postedById;
    private String postedByName;
    private Integer applicationsCount;
}

// New file: DTO for job postings 