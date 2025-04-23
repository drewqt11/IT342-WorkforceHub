package cit.edu.workforce.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Leave Type DTO")
public class LeaveTypeDTO {

    private String id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Default days is required")
    @Min(value = 0, message = "Default days cannot be negative")
    private Integer defaultDays;

    private Boolean isPaid = true;

    private Boolean requiresApproval = true;

    private Boolean isActive = true;
}

// New file: DTO for leave types 