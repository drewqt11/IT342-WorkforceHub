package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "benefit_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlanEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "benefit_plan_id", updatable = false, nullable = false, length = 36)
    private String benefitPlanId;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "plan_type", nullable = false)
    private String planType; // HEALTH, DENTAL, VISION, LIFE, RETIREMENT, etc.

    @Column(name = "provider")
    private String provider;

    @Column(name = "coverage_details", length = 1000)
    private String coverageDetails;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // New relationship added
    @OneToMany(mappedBy = "benefitPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeeBenefitEntity> enrolledEmployees = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// New file: Entity for benefit plans in the Benefits Administration module
// Contains fields for plan details, coverage information, and active status 