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
 * ReimbursementRequestEntity - Represents an employee's request for reimbursement
 * New file: This entity stores information about employee reimbursement requests,
 * including the related benefit plan, amount requested, approval status, and documentation.
 */
@Entity
@Table(name = "reimbursement_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementRequestEntity {

    @Id
    @GeneratedValue(generator = "custom-reimbursement-id")
    @GenericGenerator(name = "custom-reimbursement-id", strategy = "cit.edu.workforce.Utils.ReimbursementRequestIdGenerator")
    @Column(name = "reimbursement_id", updatable = false, nullable = false, length = 16)
    private String reimbursementId;

    // New relationship added: Reimbursement Request belongs to an Employee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private EmployeeEntity employee;

    // New relationship added: Reimbursement Request belongs to a Benefit Plan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private BenefitPlanEntity benefitPlan;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "amount_requested", precision = 10, scale = 2, nullable = false)
    private BigDecimal amountRequested;

    @Column(name = "document_path")
    private String documentPath;

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    // New relationship added: Reimbursement Request is reviewed by a User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private UserAccountEntity reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "remarks")
    private String remarks;
    
    @PrePersist
    protected void onCreate() {
        if (requestDate == null) {
            requestDate = LocalDate.now();
        }
        if (status == null || status.isEmpty()) {
            status = "PENDING";
        }
    }
} 