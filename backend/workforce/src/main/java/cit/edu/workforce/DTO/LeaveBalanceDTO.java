package cit.edu.workforce.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Leave Balance DTO")
public class LeaveBalanceDTO {

    private String id;

    private String employeeId;

    private String employeeName;

    private String leaveTypeId;

    private String leaveTypeName;

    @NotNull(message = "Allowed days is required")
    @Min(value = 0, message = "Allowed days cannot be negative")
    private Double allowedDays;

    private Double usedDays = 0.0;

    private Double pendingDays = 0.0;

    private Double remainingDays;

    @NotNull(message = "Year is required")
    private Integer year;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    private Boolean isActive = true;
}

// New file: DTO for leave balances 