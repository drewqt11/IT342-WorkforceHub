package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * PerformanceEvaluationEntity - Represents an employee's performance evaluation
 * New file: This entity stores performance evaluation data including evaluation period,
 * overall score, and remarks.
 */
@Entity
@Table(name = "performance_evaluation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceEvaluationEntity {

    @Id
    @GeneratedValue(generator = "custom-evaluation-id")
    @GenericGenerator(name = "custom-evaluation-id", strategy = "cit.edu.workforce.Utils.PerformanceEvaluationIdGenerator")
    @Column(name = "evaluation_id", updatable = false, nullable = false, length = 16)
    private String evaluationId;

    // New relationship added: Performance Evaluation belongs to an Employee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private EmployeeEntity employee;

    // New relationship added: Performance Evaluation is managed by a User (HR)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private UserAccountEntity reviewer;

    @Column(name = "evaluation_period_start", nullable = false)
    private LocalDate evaluationPeriodStart;

    @Column(name = "evaluation_period_end", nullable = false)
    private LocalDate evaluationPeriodEnd;

    @Column(name = "overall_score", precision = 3, scale = 1)
    private BigDecimal overallScore;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "evaluation_date", nullable = false)
    private LocalDate evaluationDate;
} 