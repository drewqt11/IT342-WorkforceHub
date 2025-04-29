package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * LeaveBalanceDTO - Data Transfer Object for LeaveBalance entity
 * New file: Used to transfer leave balance data between layers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceDTO {
    
    private String balanceId;
    private String employeeId;
    private String employeeName;
    private String leaveType;
    private BigDecimal allocatedDays;
    private BigDecimal usedDays;
    private BigDecimal remainingDays;
    private LocalDate resetDate;
} 