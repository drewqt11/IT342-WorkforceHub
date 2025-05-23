package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * AttendanceRecordEntity - Represents an employee's attendance record for a specific date
 * Updated file: Added time zone configuration for Asia/Manila
 */
@Entity
@Table(name = "attendance_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordEntity {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Manila");

    @Id
    @GeneratedValue(generator = "custom-attendance-id")
    @GenericGenerator(name = "custom-attendance-id", strategy = "cit.edu.workforce.Utils.AttendanceIdGenerator")
    @Column(name = "attendance_id", updatable = false, nullable = false, length = 16)
    private String attendanceId;

    // New relationship added: Attendance record belongs to an Employee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private EmployeeEntity employee;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "clock_in_time")
    @Temporal(TemporalType.TIME)
    private LocalTime clockInTime;

    @Column(name = "clock_out_time")
    @Temporal(TemporalType.TIME)
    private LocalTime clockOutTime;

    @Column(name = "total_hours", precision = 5, scale = 2)
    private BigDecimal totalHours;

    @Column(name = "status", nullable = false)
    private String status; // Present, Absent, Late

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "overtime_hours", precision = 5, scale = 2)
    private BigDecimal overtimeHours;

    @Column(name = "tardiness_minutes")
    private Integer tardinessMinutes;

    @Column(name = "undertime_minutes")
    private Integer undertimeMinutes;

    @Column(name = "reason_for_absence")
    private String reasonForAbsence;

    @Column(name = "approved_by_manager", nullable = false)
    private boolean approvedByManager = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(ZONE_ID);
        if (clockInTime != null) {
            clockInTime = clockInTime.truncatedTo(ChronoUnit.SECONDS);
        }
        if (clockOutTime != null) {
            clockOutTime = clockOutTime.truncatedTo(ChronoUnit.SECONDS);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(ZONE_ID);
        if (clockInTime != null) {
            clockInTime = clockInTime.truncatedTo(ChronoUnit.SECONDS);
        }
        if (clockOutTime != null) {
            clockOutTime = clockOutTime.truncatedTo(ChronoUnit.SECONDS);
        }
    }
} 