package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * BenefitEnrollmentEntity - Represents an employee's enrollment in a benefit plan
 * New file: This entity stores information about an employee's enrollment in a benefit plan,
 * including enrollment date, status, and any cancellation reason.
 */
@Entity
@Table(name = "benefit_enrollment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitEnrollmentEntity {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Manila");

    @Id
    @GeneratedValue(generator = "custom-benefit-enrollment-id")
    @GenericGenerator(name = "custom-benefit-enrollment-id", strategy = "cit.edu.workforce.Utils.BenefitEnrollmentIdGenerator")
    @Column(name = "enrollment_id", updatable = false, nullable = false, length = 16)
    private String enrollmentId;

    // New relationship added: Benefit Enrollment belongs to an Employee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private EmployeeEntity employee;

    // New relationship added: Benefit Enrollment belongs to a Benefit Plan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private BenefitPlanEntity benefitPlan;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Column(name = "status", nullable = false)
    private String status; // Active, Cancelled, Expired

    @Column(name = "cancellation_reason")
    private String cancellationReason;
    
    // New relationship added: Benefit Enrollment has many Benefit Dependents
    @OneToMany(mappedBy = "benefitEnrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BenefitDependentEntity> dependents = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        if (enrollmentDate == null) {
            enrollmentDate = LocalDate.now(ZONE_ID);
        }
        if (status == null || status.isEmpty()) {
            status = "Active";
        }
    }
} 