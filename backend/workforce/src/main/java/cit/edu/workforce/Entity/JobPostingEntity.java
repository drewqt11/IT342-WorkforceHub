package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "job_posting")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "posting_id", updatable = false, nullable = false, length = 36)
    private String postingId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, length = 5000)
    private String description;

    @Column(name = "qualifications", length = 2000)
    private String qualifications;

    @Column(name = "responsibilities", length = 2000)
    private String responsibilities;

    // New mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    // New mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id")
    private JobTitleEntity jobTitle;

    @Column(name = "location")
    private String location;

    @Column(name = "employment_type")
    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT, INTERN

    @Column(name = "experience_level")
    private String experienceLevel; // ENTRY, MID, SENIOR, EXECUTIVE

    @Column(name = "salary_min")
    private Double salaryMin;

    @Column(name = "salary_max")
    private Double salaryMax;

    @Column(name = "posting_date", nullable = false)
    private LocalDate postingDate;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @Column(name = "is_internal", nullable = false)
    private Boolean isInternal = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "external_url")
    private String externalUrl;

    // New mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "posted_by")
    private EmployeeEntity postedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // New relationship added
    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<JobApplicationEntity> applications = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        postingDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// New file: Entity for job postings in the recruitment module
// Includes details about job requirements, department, salary range, and status 