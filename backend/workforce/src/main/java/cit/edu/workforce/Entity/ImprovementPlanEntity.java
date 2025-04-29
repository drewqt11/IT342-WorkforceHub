package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;

/**
 * ImprovementPlanEntity - Represents an employee performance improvement plan
 * New file: This entity stores information about performance improvement plans,
 * including reason, action steps, and duration.
 */
@Entity
@Table(name = "improvement_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImprovementPlanEntity {

    @Id
    @GeneratedValue(generator = "custom-plan-id")
    @GenericGenerator(name = "custom-plan-id", strategy = "cit.edu.workforce.Utils.ImprovementPlanIdGenerator")
    @Column(name = "plan_id", updatable = false, nullable = false, length = 16)
    private String planId;

    // New relationship added: Improvement Plan belongs to an Employee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private EmployeeEntity employee;

    // New relationship added: Improvement Plan is initiated by a User (HR)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private UserAccountEntity initiator;

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(name = "action_steps", columnDefinition = "TEXT", nullable = false)
    private String actionSteps;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "status", nullable = false)
    private String status; // Open, Completed, Cancelled
} 