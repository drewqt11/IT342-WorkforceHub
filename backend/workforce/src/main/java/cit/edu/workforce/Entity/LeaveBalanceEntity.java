package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "leave_balances")
public class LeaveBalanceEntity {

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
    private Double allowedDays;

    @Column(nullable = false)
    private Double usedDays = 0.0;

    @Column(nullable = false)
    private Double pendingDays = 0.0;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private Boolean isActive = true;
}

// New file: Entity for employee leave balances with allowed, used, and pending days
// New relationship added: Many-to-one relationship with Employee
// New relationship added: Many-to-one relationship with LeaveType 