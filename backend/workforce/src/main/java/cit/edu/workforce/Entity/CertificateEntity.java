package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

/**
 * CertificateEntity - Represents a certificate uploaded by an employee for a training or event
 * New file: This entity stores information about certificates, including file path, uploaded date,
 * verification status, and remarks
 */
@Entity
@Table(name = "certificate")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateEntity {

    @Id
    @GeneratedValue(generator = "custom-certificate-id")
    @GenericGenerator(name = "custom-certificate-id", strategy = "cit.edu.workforce.Utils.CertificateIdGenerator")
    @Column(name = "certificate_id", updatable = false, nullable = false, length = 16)
    private String certificateId;

    @Column(name = "file_path", nullable = false)
    private String filePath; // Uploaded certificate PDF

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "status", nullable = false)
    private String status; // Pending, Verified, Rejected

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // New relationship added: Certificate belongs to a Training Enrollment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private TrainingEnrollmentEntity trainingEnrollment;

    // New relationship added: Certificate is verified by a HR Admin (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private UserAccountEntity verifiedBy;
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        if (status == null) {
            status = "Pending";
        }
    }
} 