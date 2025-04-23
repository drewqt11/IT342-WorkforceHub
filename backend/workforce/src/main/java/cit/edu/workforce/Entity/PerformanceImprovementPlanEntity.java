package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_improvement_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceImprovementPlanEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "pip_id", updatable = false, nullable = false, length = 36)
    private String pipId;

    // New mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, TERMINATED

    @Column(name = "performance_issues", nullable = false, length = 2000)
    private String performanceIssues;

    @Column(name = "improvement_goals", nullable = false, length = 2000)
    private String improvementGoals;

    @Column(name = "action_plan", nullable = false, length = 2000)
    private String actionPlan;

    @Column(name = "resources_provided", length = 1000)
    private String resourcesProvided;

    @Column(name = "evaluation_criteria", length = 1000)
    private String evaluationCriteria;

    @Column(name = "consequences", length = 1000)
    private String consequences;

    // New mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manager_id", nullable = false)
    private EmployeeEntity manager;

    // New mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hr_representative_id")
    private EmployeeEntity hrRepresentative;

    @Column(name = "progress_notes", length = 5000)
    private String progressNotes;

    @Column(name = "final_outcome", length = 2000)
    private String finalOutcome;

    @Column(name = "completion_date")
    private LocalDate completionDate;

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

// New file: Entity for performance improvement plans
// Tracks employee performance issues, improvement goals, and progress 