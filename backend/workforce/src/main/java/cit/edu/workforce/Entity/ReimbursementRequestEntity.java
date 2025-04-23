package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reimbursement_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementRequestEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "request_id", updatable = false, nullable = false, length = 36)
    private String requestId;

    // New mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "expense_type", nullable = false)
    private String expenseType; // MEDICAL, TRAVEL, EDUCATION, SUPPLIES, OTHER

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency = "USD";

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "receipt_url")
    private String receiptUrl;

    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, PAID

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    // New mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approver_id")
    private EmployeeEntity approver;

    @Column(name = "approver_comments", length = 1000)
    private String approverComments;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        requestDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// New file: Entity for reimbursement requests
// Manages employee expense reimbursement requests with approval workflow 