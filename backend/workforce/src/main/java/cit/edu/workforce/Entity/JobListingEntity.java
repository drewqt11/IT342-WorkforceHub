package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JobListingEntity - Entity for job listings
 * New file: This entity represents job postings created by the HR department
 */
@Entity
@Table(name = "job_listing")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobListingEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "job_id", updatable = false, nullable = false, length = 36)
    private String jobId;

    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;
    
    @Column(name = "qualifications", columnDefinition = "TEXT")
    private String qualifications;
    
    @Column(name = "employment_type", nullable = false)
    private String employmentType; // Full-time, Part-time, Contract
    
    @Column(name = "job_type", nullable = false)
    private String jobType; // Internal, External
    
    @Column(name = "date_posted", nullable = false)
    private LocalDate datePosted;
    
    @Column(name = "application_deadline", nullable = false)
    private LocalDate applicationDeadline;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    // New relationship added: Job listing belongs to a department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;
    
    // New relationship added: Job listing has many application records
    @OneToMany(mappedBy = "jobListing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationRecordEntity> applications = new ArrayList<>();
    
    // Set default values
    @PrePersist
    protected void onCreate() {
        datePosted = LocalDate.now();
        isActive = true;
    }
} 