package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.CertificateEntity;
import cit.edu.workforce.Entity.TrainingEnrollmentEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CertificateRepository - Repository for managing certificates
 * New file: Provides methods to access certificate data
 */
@Repository
public interface CertificateRepository extends JpaRepository<CertificateEntity, String> {
    
    /**
     * Find all certificates for a specific training enrollment
     */
    List<CertificateEntity> findByTrainingEnrollment(TrainingEnrollmentEntity trainingEnrollment);
    
    /**
     * Find paginated certificates for a specific training enrollment
     */
    Page<CertificateEntity> findByTrainingEnrollment(TrainingEnrollmentEntity trainingEnrollment, Pageable pageable);
    
    /**
     * Find certificates by status
     */
    List<CertificateEntity> findByStatus(String status);
    
    /**
     * Find paginated certificates by status
     */
    Page<CertificateEntity> findByStatus(String status, Pageable pageable);
    
    /**
     * Find certificates verified by a specific user
     */
    List<CertificateEntity> findByVerifiedBy(UserAccountEntity verifiedBy);
    
    /**
     * Find paginated certificates verified by a specific user
     */
    Page<CertificateEntity> findByVerifiedBy(UserAccountEntity verifiedBy, Pageable pageable);
    
    /**
     * Find certificates uploaded within a date-time range
     */
    List<CertificateEntity> findByUploadedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
    
    /**
     * Find paginated certificates uploaded within a date-time range
     */
    Page<CertificateEntity> findByUploadedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);
    
    /**
     * Count certificates by status for a specific training enrollment
     */
    long countByTrainingEnrollmentAndStatus(TrainingEnrollmentEntity trainingEnrollment, String status);
    
    /**
     * Find certificates by enrollment and status
     */
    List<CertificateEntity> findByTrainingEnrollmentAndStatus(TrainingEnrollmentEntity trainingEnrollment, String status);
    
    /**
     * Count pending certificates
     */
    @Query("SELECT COUNT(c) FROM CertificateEntity c WHERE c.status = 'Pending'")
    long countPendingCertificates();
    
    /**
     * Find all pending certificates
     */
    @Query("SELECT c FROM CertificateEntity c WHERE c.status = 'Pending'")
    List<CertificateEntity> findPendingCertificates();
    
    /**
     * Find paginated pending certificates
     */
    @Query("SELECT c FROM CertificateEntity c WHERE c.status = 'Pending'")
    Page<CertificateEntity> findPendingCertificates(Pageable pageable);
    
    /**
     * Find all verified certificates
     */
    @Query("SELECT c FROM CertificateEntity c WHERE c.status = 'Verified'")
    List<CertificateEntity> findVerifiedCertificates();
    
    /**
     * Find paginated verified certificates
     */
    @Query("SELECT c FROM CertificateEntity c WHERE c.status = 'Verified'")
    Page<CertificateEntity> findVerifiedCertificates(Pageable pageable);
    
    /**
     * Find all rejected certificates
     */
    @Query("SELECT c FROM CertificateEntity c WHERE c.status = 'Rejected'")
    List<CertificateEntity> findRejectedCertificates();
    
    /**
     * Find paginated rejected certificates
     */
    @Query("SELECT c FROM CertificateEntity c WHERE c.status = 'Rejected'")
    Page<CertificateEntity> findRejectedCertificates(Pageable pageable);
} 