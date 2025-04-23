package cit.edu.workforce.Entity;

import cit.edu.workforce.Enum.OvertimeRequestStatus;
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
@Table(name = "overtime_requests")
public class OvertimeRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Double hours;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = true, length = 1000)
    private String comments;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OvertimeRequestStatus status = OvertimeRequestStatus.PENDING;

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

// New file: Entity for overtime requests with date, hours, reason, and approval status
// New relationship added: Many-to-one relationship with Employee (requester)
// New relationship added: Many-to-one relationship with Employee (approver) 