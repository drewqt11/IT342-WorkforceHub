package cit.edu.workforce.DTO;

import cit.edu.workforce.Enum.OvertimeRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Overtime Request DTO")
public class OvertimeRequestDTO {

    private String id;

    private String employeeId;

    private String employeeName;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Hours is required")
    @Min(value = 0, message = "Hours cannot be negative")
    private Double hours;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String comments;

    private OvertimeRequestStatus status = OvertimeRequestStatus.PENDING;

    private String approvedById;

    private String approvedByName;

    private LocalDateTime approvedAt;

    private LocalDateTime createdAt;

    private Boolean isActive = true;
}

// New file: DTO for overtime requests 