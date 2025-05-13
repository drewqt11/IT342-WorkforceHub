package cit.edu.workforce.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

/**
 * ApplicationRecordEntity - Entity for job application records
 * New file: This entity represents applications submitted by applicants for job listings
 */
@Entity
@Table(name = "application_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRecordEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "application_id", updatable = false, nullable = false, length = 36)
    private String applicationId;
    
    @Column(name = "status", nullable = false)
    private String status; // Pending, Shortlisted, Rejected, Hired
    
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    // New relationship added: Application belongs to an applicant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    @JsonBackReference("applicant-applications")
    private ApplicantEntity applicant;
    
    // New relationship added: Application is for a job listing
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonBackReference("job-applications")
    private JobListingEntity jobListing;
    
    // New relationship added: Application is reviewed by a user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private UserAccountEntity reviewedBy;
    
    // Set default values for new applications
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = "PENDING";
        }
    }
} 