package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_evaluation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceEvaluationEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "evaluation_id", updatable = false, nullable = false, length = 36)
    private String evaluationId;

    // New mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    @Column(name = "evaluation_type", nullable = false)
    private String evaluationType; // ANNUAL, QUARTERLY, PROBATION, SELF, PEER

    @Column(name = "evaluation_period_start", nullable = false)
    private LocalDate evaluationPeriodStart;

    @Column(name = "evaluation_period_end", nullable = false)
    private LocalDate evaluationPeriodEnd;

    @Column(name = "submitted_date")
    private LocalDate submittedDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "status", nullable = false)
    private String status = "DRAFT"; // DRAFT, SUBMITTED, IN_REVIEW, COMPLETED, ACKNOWLEDGED

    @Column(name = "overall_rating")
    private Integer overallRating; // 1-5 scale

    @Column(name = "performance_summary", length = 2000)
    private String performanceSummary;

    @Column(name = "strengths", length = 1000)
    private String strengths;

    @Column(name = "areas_for_improvement", length = 1000)
    private String areasForImprovement;

    @Column(name = "goals_achieved", length = 1000)
    private String goalsAchieved;

    @Column(name = "goals_for_next_period", length = 1000)
    private String goalsForNextPeriod;

    // New mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evaluator_id")
    private EmployeeEntity evaluator;

    @Column(name = "evaluator_comments", length = 1000)
    private String evaluatorComments;

    @Column(name = "employee_comments", length = 1000)
    private String employeeComments;

    @Column(name = "acknowledgement_date")
    private LocalDate acknowledgementDate;

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

// New file: Entity for performance evaluations in the performance management module
// Tracks employee evaluations, ratings, feedback, and goals 