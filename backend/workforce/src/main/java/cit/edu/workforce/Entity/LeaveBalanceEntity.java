package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * LeaveBalanceEntity - Represents an employee's leave balance
 * New file: This entity stores information about the employee's leave balance,
 * including allocated, used, and remaining days.
 */
@Entity
@Table(name = "leave_balance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceEntity {

    @Id
    @GeneratedValue(generator = "custom-balance-id")
    @GenericGenerator(name = "custom-balance-id", strategy = "cit.edu.workforce.Utils.LeaveBalanceIdGenerator")
    @Column(name = "balance_id", updatable = false, nullable = false, length = 16)
    private String balanceId;

    // New relationship added: Leave balance belongs to an Employee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private EmployeeEntity employee;

    @Column(name = "leave_type", nullable = false)
    private String leaveType; // Sick, Vacation

    @Column(name = "allocated_days", precision = 5, scale = 2, nullable = false)
    private BigDecimal allocatedDays;

    @Column(name = "used_days", precision = 5, scale = 2, nullable = false)
    private BigDecimal usedDays = BigDecimal.ZERO;

    @Column(name = "remaining_days", precision = 5, scale = 2, nullable = false)
    private BigDecimal remainingDays;

    @Column(name = "reset_date", nullable = false)
    private LocalDate resetDate;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Initialize remaining days to allocated days
        if (remainingDays == null) {
            remainingDays = allocatedDays;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 