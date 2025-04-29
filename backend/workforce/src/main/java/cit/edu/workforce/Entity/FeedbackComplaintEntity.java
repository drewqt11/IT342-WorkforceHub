package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

/**
 * FeedbackComplaintEntity - Represents employee feedback, complaints, or concerns
 * New file: This entity stores feedback, complaints, and concerns submitted by employees,
 * including category, subject, description, and status.
 */
@Entity
@Table(name = "feedback_complaint")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackComplaintEntity {

    @Id
    @GeneratedValue(generator = "custom-feedback-id")
    @GenericGenerator(name = "custom-feedback-id", strategy = "cit.edu.workforce.Utils.FeedbackComplaintIdGenerator")
    @Column(name = "feedback_id", updatable = false, nullable = false, length = 16)
    private String feedbackId;

    // New relationship added: Feedback/Complaint is submitted by an Employee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private EmployeeEntity employee;

    // New relationship added: Feedback/Complaint is resolved by a User (HR)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolver_id")
    private UserAccountEntity resolver;

    @Column(name = "category", nullable = false)
    private String category; // Feedback, Complaint, Concern

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "status", nullable = false)
    private String status; // Open, In Review, Resolved, Closed

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        if (status == null) {
            status = "Open";
        }
    }
} 