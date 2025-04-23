package cit.edu.workforce.DTO;

import cit.edu.workforce.Enum.LeaveRequestStatus;
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
@Schema(description = "Leave Request DTO")
public class LeaveRequestDTO {

    private String id;

    private String employeeId;

    private String employeeName;

    @NotNull(message = "Leave type is required")
    private String leaveTypeId;

    private String leaveTypeName;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Days is required")
    @Min(value = 0, message = "Days cannot be negative")
    private Double days;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String comments;

    private LeaveRequestStatus status = LeaveRequestStatus.PENDING;

    private String approvedById;

    private String approvedByName;

    private LocalDateTime approvedAt;

    private LocalDateTime createdAt;

    private Boolean isActive = true;
}

// New file: DTO for leave requests 