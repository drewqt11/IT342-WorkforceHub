package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TrainingEnrollmentEntity - Represents an employee's enrollment in a training program or event
 * New file: This entity tracks an employee's enrollment status, type, and completion for training programs and events
 */
@Entity
@Table(name = "training_enrollment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingEnrollmentEntity {

    @Id
    @GeneratedValue(generator = "custom-enrollment-id")
    @GenericGenerator(name = "custom-enrollment-id", strategy = "cit.edu.workforce.Utils.TrainingEnrollmentIdGenerator")
    @Column(name = "enrollment_id", updatable = false, nullable = false, length = 16)
    private String enrollmentId;

    @Column(name = "enrolled_date", nullable = false)
    private LocalDate enrolledDate;

    @Column(name = "enrollment_type", nullable = false)
    private String enrollmentType; // Assigned, Self-enrolled

    @Column(name = "status", nullable = false)
    private String status; // Enrolled, Completed, Cancelled

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    // New relationship added: Training Enrollment belongs to an Employee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private EmployeeEntity employee;

    // New relationship added: Training Enrollment can belong to a Training Program (can be null if event-based)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_id")
    private TrainingProgramEntity trainingProgram;

    // New relationship added: Training Enrollment can belong to an Event (can be null if program-based)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private EventEntity event;

    // New relationship added: Training Enrollment has many Certificates
    @OneToMany(mappedBy = "trainingEnrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CertificateEntity> certificates = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        enrolledDate = LocalDate.now();
        if (status == null) {
            status = "Enrolled";
        }
    }
} 