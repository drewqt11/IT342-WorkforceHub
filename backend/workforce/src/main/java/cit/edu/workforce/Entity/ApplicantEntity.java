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
 * ApplicantEntity - Entity for job applicants
 * New file: This entity represents applicants for job listings
 */
@Entity
@Table(name = "applicant")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "applicant_id", updatable = false, nullable = false, length = 36)
    private String applicantId;
    
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "resume_pdf_path")
    private String resumePdfPath;
    
    @Column(name = "is_internal", nullable = false)
    private boolean isInternal = false;
    
    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;
    
    // New relationship added: Applicant can be an existing user (internal applicant)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccountEntity user;
    
    // New relationship added: Applicant has many application records
    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationRecordEntity> applications = new ArrayList<>();
    
    // Set default values
    @PrePersist
    protected void onCreate() {
        applicationDate = LocalDate.now();
    }
} 