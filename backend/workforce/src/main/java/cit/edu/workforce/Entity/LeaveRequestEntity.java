package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * LeaveRequestEntity - Represents an employee's request for leave
 * New file: This entity stores information about leave requests including
 * leave type, date range, reason, and approval status.
 */
@Entity
@Table(name = "leave_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestEntity {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Manila");

    @Id
    @GeneratedValue(generator = "custom-leave-id")
    @GenericGenerator(name = "custom-leave-id", strategy = "cit.edu.workforce.Utils.LeaveIdGenerator")
    @Column(name = "leave_id", updatable = false, nullable = false, length = 16)
    private String leaveId;

    // New relationship added: Leave request belongs to an Employee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private EmployeeEntity employee;

    @Column(name = "leave_type", nullable = false)
    private String leaveType; // Sick, Vacation, Emergency

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_days", precision = 5, scale = 2, nullable = false)
    private BigDecimal totalDays;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    // New relationship added: Leave request is reviewed by a User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private UserAccountEntity reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(ZONE_ID);
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(ZONE_ID);
    }
} 