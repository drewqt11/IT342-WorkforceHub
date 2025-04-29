package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;

/**
 * BenefitDependentEntity - Represents a dependent in an employee's benefit enrollment
 * New file: This entity stores information about dependents covered under an employee's
 * benefit plan enrollment, including name, relationship, and birth date.
 */
@Entity
@Table(name = "benefit_dependent")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitDependentEntity {

    @Id
    @GeneratedValue(generator = "custom-benefit-dependent-id")
    @GenericGenerator(name = "custom-benefit-dependent-id", strategy = "cit.edu.workforce.Utils.BenefitDependentIdGenerator")
    @Column(name = "dependent_id", updatable = false, nullable = false, length = 16)
    private String dependentId;

    // New relationship added: Benefit Dependent belongs to a Benefit Enrollment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private BenefitEnrollmentEntity benefitEnrollment;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "relationship", nullable = false)
    private String relationship;

    @Column(name = "birthdate")
    private LocalDate birthdate;
} 