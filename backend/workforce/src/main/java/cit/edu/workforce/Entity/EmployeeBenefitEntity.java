package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_benefit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBenefitEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "enrollment_id", updatable = false, nullable = false, length = 36)
    private String enrollmentId;

    // New mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    // New mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "benefit_plan_id", nullable = false)
    private BenefitPlanEntity benefitPlan;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Column(name = "coverage_start_date", nullable = false)
    private LocalDate coverageStartDate;

    @Column(name = "coverage_end_date")
    private LocalDate coverageEndDate;

    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, INACTIVE, PENDING

    @Column(name = "dependents_count")
    private Integer dependentsCount = 0;

    @Column(name = "additional_details", length = 1000)
    private String additionalDetails;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// New file: Entity for employee benefit enrollments
// Links employees to benefit plans with enrollment details and status 