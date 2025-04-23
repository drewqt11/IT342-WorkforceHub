package cit.edu.workforce.Entity;

import cit.edu.workforce.Enum.LeaveRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "leave_requests")
public class LeaveRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    @ManyToOne
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveTypeEntity leaveType;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Double days;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = true, length = 1000)
    private String comments;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LeaveRequestStatus status = LeaveRequestStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private EmployeeEntity approvedBy;

    @Column
    private LocalDateTime approvedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean isActive = true;
}

// New file: Entity for leave requests with start date, end date, reason, and approval status
// New relationship added: Many-to-one relationship with Employee (requester)
// New relationship added: Many-to-one relationship with LeaveType
// New relationship added: Many-to-one relationship with Employee (approver) 