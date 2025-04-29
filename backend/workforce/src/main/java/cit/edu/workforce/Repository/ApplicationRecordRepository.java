package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.ApplicantEntity;
import cit.edu.workforce.Entity.ApplicationRecordEntity;
import cit.edu.workforce.Entity.JobListingEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ApplicationRecordRepository - Repository for application records
 * New file: Provides methods to access application record data
 */
@Repository
public interface ApplicationRecordRepository extends JpaRepository<ApplicationRecordEntity, String> {
    
    /**
     * Find applications by applicant
     */
    List<ApplicationRecordEntity> findByApplicant(ApplicantEntity applicant);
    
    /**
     * Find applications by applicant with pagination
     */
    Page<ApplicationRecordEntity> findByApplicant(ApplicantEntity applicant, Pageable pageable);
    
    /**
     * Find applications by job listing
     */
    List<ApplicationRecordEntity> findByJobListing(JobListingEntity jobListing);
    
    /**
     * Find applications by job listing with pagination
     */
    Page<ApplicationRecordEntity> findByJobListing(JobListingEntity jobListing, Pageable pageable);
    
    /**
     * Find applications by reviewer
     */
    List<ApplicationRecordEntity> findByReviewedBy(UserAccountEntity reviewedBy);
    
    /**
     * Find applications by reviewer with pagination
     */
    Page<ApplicationRecordEntity> findByReviewedBy(UserAccountEntity reviewedBy, Pageable pageable);
    
    /**
     * Find applications by status
     */
    List<ApplicationRecordEntity> findByStatus(String status);
    
    /**
     * Find applications by status with pagination
     */
    Page<ApplicationRecordEntity> findByStatus(String status, Pageable pageable);
    
    /**
     * Find applications by applicant and status
     */
    List<ApplicationRecordEntity> findByApplicantAndStatus(ApplicantEntity applicant, String status);
    
    /**
     * Find applications by applicant and status with pagination
     */
    Page<ApplicationRecordEntity> findByApplicantAndStatus(ApplicantEntity applicant, String status, Pageable pageable);
    
    /**
     * Find applications by job listing and status
     */
    List<ApplicationRecordEntity> findByJobListingAndStatus(JobListingEntity jobListing, String status);
    
    /**
     * Find applications by job listing and status with pagination
     */
    Page<ApplicationRecordEntity> findByJobListingAndStatus(JobListingEntity jobListing, String status, Pageable pageable);
    
    /**
     * Find applications by applicant and job listing
     */
    List<ApplicationRecordEntity> findByApplicantAndJobListing(ApplicantEntity applicant, JobListingEntity jobListing);
    
    /**
     * Count applications by job listing
     */
    long countByJobListing(JobListingEntity jobListing);
    
    /**
     * Count applications by applicant
     */
    long countByApplicant(ApplicantEntity applicant);
    
    /**
     * Count applications by job listing and status
     */
    long countByJobListingAndStatus(JobListingEntity jobListing, String status);
    
    /**
     * Find applications reviewed within a time range
     */
    List<ApplicationRecordEntity> findByReviewedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
    
    /**
     * Find applications by job listing and reviewed by status in a time range
     */
    @Query("SELECT a FROM ApplicationRecordEntity a WHERE a.jobListing = :jobListing AND a.status = :status AND a.reviewedAt BETWEEN :startDateTime AND :endDateTime")
    List<ApplicationRecordEntity> findByJobListingAndStatusAndReviewedAtBetween(JobListingEntity jobListing, String status, LocalDateTime startDateTime, LocalDateTime endDateTime);
} 