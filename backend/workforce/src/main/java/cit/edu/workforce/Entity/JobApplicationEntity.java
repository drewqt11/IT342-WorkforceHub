package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_application")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "application_id", updatable = false, nullable = false, length = 36)
    private String applicationId;

    // New mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "posting_id", nullable = false)
    private JobPostingEntity jobPosting;

    // New mapping for internal applications
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private EmployeeEntity employee;

    @Column(name = "applicant_name")
    private String applicantName;

    @Column(name = "applicant_email")
    private String applicantEmail;

    @Column(name = "applicant_phone")
    private String applicantPhone;

    @Column(name = "resume_url")
    private String resumeUrl;

    @Column(name = "cover_letter_url")
    private String coverLetterUrl;

    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;

    @Column(name = "status", nullable = false)
    private String status = "APPLIED"; // APPLIED, SCREENING, INTERVIEW, OFFER, HIRED, REJECTED

    @Column(name = "current_stage")
    private String currentStage; // SOURCED, APPLIED, SCREENING, ASSESSMENT, INTERVIEW, OFFER, HIRED

    @Column(name = "source")
    private String source; // INTERNAL, CAREER_SITE, REFERRAL, JOB_BOARD, etc.

    @Column(name = "internal_notes", length = 2000)
    private String internalNotes;

    @Column(name = "interview_date")
    private LocalDate interviewDate;

    @Column(name = "interview_feedback", length = 2000)
    private String interviewFeedback;

    // New mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewed_by")
    private EmployeeEntity reviewedBy;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        applicationDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// New file: Entity for job applications in the recruitment module
// Tracks applicant information, application status, and review process 