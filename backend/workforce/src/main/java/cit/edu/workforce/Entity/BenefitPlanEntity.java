package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * BenefitPlanEntity - Represents a benefit plan that employees can enroll in
 * New file: This entity stores information about benefit plans including plan type, 
 * name, description, provider, eligibility criteria, and maximum coverage amount.
 */
@Entity
@Table(name = "benefit_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlanEntity {

    @Id
    @GeneratedValue(generator = "custom-benefit-plan-id")
    @GenericGenerator(name = "custom-benefit-plan-id", strategy = "cit.edu.workforce.Utils.BenefitPlanIdGenerator")
    @Column(name = "plan_id", updatable = false, nullable = false, length = 16)
    private String planId;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "provider")
    private String provider;

    @Column(name = "eligibility")
    private String eligibility;

    @Column(name = "plan_type", nullable = false)
    private String planType; // Health, Dental, Life, etc.

    @Column(name = "max_coverage", precision = 10, scale = 2)
    private BigDecimal maxCoverage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    // New relationship added: Benefit Plan has many Benefit Enrollments
    @OneToMany(mappedBy = "benefitPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BenefitEnrollmentEntity> enrollments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 