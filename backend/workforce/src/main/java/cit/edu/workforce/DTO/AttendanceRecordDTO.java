package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * AttendanceRecordDTO - Data Transfer Object for AttendanceRecord entity
 * Updated file: Removed location fields to follow the ERD
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordDTO {
    
    private String attendanceId;
    private String employeeId;
    private String employeeName;
    private LocalDate date;
    private LocalTime clockInTime;
    private LocalTime clockOutTime;
    private BigDecimal totalHours;
    private String status;
    private String remarks;
    private BigDecimal overtimeHours;
    private Integer tardinessMinutes;
    private Integer undertimeMinutes;
    private String reasonForAbsence;
    private boolean approvedByManager;
} 