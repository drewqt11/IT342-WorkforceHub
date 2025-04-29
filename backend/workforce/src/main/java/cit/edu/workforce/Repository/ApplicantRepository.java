package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.ApplicantEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ApplicantRepository - Repository for applicants
 * New file: Provides methods to access applicant data
 */
@Repository
public interface ApplicantRepository extends JpaRepository<ApplicantEntity, String> {
    
    /**
     * Find applicant by email
     */
    Optional<ApplicantEntity> findByEmail(String email);
    
    /**
     * Find applicants by user account (internal applicants)
     */
    Optional<ApplicantEntity> findByUser(UserAccountEntity user);
    
    /**
     * Find applicants by internal status
     */
    List<ApplicantEntity> findByIsInternal(boolean isInternal);
    
    /**
     * Find applicants by internal status with pagination
     */
    Page<ApplicantEntity> findByIsInternal(boolean isInternal, Pageable pageable);
    
    /**
     * Find applicants by application date range
     */
    List<ApplicantEntity> findByApplicationDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find applicants by application date range with pagination
     */
    Page<ApplicantEntity> findByApplicationDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Search applicants by name
     */
    Page<ApplicantEntity> findByFullNameContainingIgnoreCase(String name, Pageable pageable);
} 