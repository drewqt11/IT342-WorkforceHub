package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * LeaveRequestDTO - Data Transfer Object for LeaveRequest entity
 * New file: Used to transfer leave request data between layers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {
    
    private String leaveId;
    private String employeeId;
    private String employeeName;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalDays;
    private String reason;
    private String status;
    private String reviewedBy;
    private LocalDate reviewedAt;
} 