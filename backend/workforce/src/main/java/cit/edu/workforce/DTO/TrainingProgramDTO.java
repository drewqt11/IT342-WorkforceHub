package cit.edu.workforce.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * TrainingProgramDTO - Represents a training program in the system
 * New file: Used for transferring training program data between the client and server
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgramDTO {
    
    private String trainingId;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    private String provider;
    
    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @NotBlank(message = "Training mode is required")
    private String trainingMode;
    
    private boolean isActive = true;
    
    private String createdById;
    
    private String createdByName;
    
    private int enrollmentCount = 0;
} 