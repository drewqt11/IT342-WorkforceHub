package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_enrollment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingEnrollmentEntity {

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
    @JoinColumn(name = "program_id", nullable = false)
    private TrainingProgramEntity trainingProgram;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "status", nullable = false)
    private String status = "ENROLLED"; // ENROLLED, IN_PROGRESS, COMPLETED, FAILED, WITHDRAWN

    @Column(name = "completion_percentage")
    private Integer completionPercentage = 0;

    @Column(name = "score")
    private Double score;

    @Column(name = "certificate_url")
    private String certificateUrl;

    @Column(name = "certificate_expiry_date")
    private LocalDate certificateExpiryDate;

    @Column(name = "feedback", length = 2000)
    private String feedback;

    @Column(name = "instructor_comments", length = 1000)
    private String instructorComments;

    // New mapping - who assigned this training to the employee (if applicable)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_by")
    private EmployeeEntity assignedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        enrollmentDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// New file: Entity for training enrollments in the Training & Development module
// Links employees to training programs with enrollment status and completion tracking