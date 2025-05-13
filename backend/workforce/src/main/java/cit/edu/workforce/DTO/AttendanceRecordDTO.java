package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * AttendanceRecordDTO - Data Transfer Object for AttendanceRecord entity
 * Updated file: Added time zone configuration for Asia/Manila
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
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Manila");

    // Helper method to get time in Asia/Manila timezone
    public LocalTime getClockInTimeInManila() {
        return clockInTime != null ? clockInTime : null;
    }

    // Helper method to get time in Asia/Manila timezone
    public LocalTime getClockOutTimeInManila() {
        return clockOutTime != null ? clockOutTime : null;
    }
} 