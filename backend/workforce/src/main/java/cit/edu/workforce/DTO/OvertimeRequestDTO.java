package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * OvertimeRequestDTO - Data Transfer Object for OvertimeRequest entity
 * New file: Used to transfer overtime request data between layers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeRequestDTO {
    
    private String otRequestId;
    private String employeeId;
    private String employeeName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal totalHours;
    private String reason;
    private String status;
    private String reviewedBy;
    private LocalDate reviewedAt;
} 