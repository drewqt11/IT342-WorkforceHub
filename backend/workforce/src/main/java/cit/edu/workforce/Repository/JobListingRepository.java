package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.DepartmentEntity;
import cit.edu.workforce.Entity.JobListingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * JobListingRepository - Repository for job listings
 * New file: Provides methods to access job listing data
 */
@Repository
public interface JobListingRepository extends JpaRepository<JobListingEntity, String> {
    
    /**
     * Find active job listings
     */
    List<JobListingEntity> findByIsActiveTrue();
    
    /**
     * Find active job listings with pagination
     */
    Page<JobListingEntity> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Find job listings by department
     */
    List<JobListingEntity> findByDepartment(DepartmentEntity department);
    
    /**
     * Find job listings by department with pagination
     */
    Page<JobListingEntity> findByDepartment(DepartmentEntity department, Pageable pageable);
    
    /**
     * Find active job listings by department
     */
    List<JobListingEntity> findByDepartmentAndIsActiveTrue(DepartmentEntity department);
    
    /**
     * Find active job listings by department with pagination
     */
    Page<JobListingEntity> findByDepartmentAndIsActiveTrue(DepartmentEntity department, Pageable pageable);
    
    /**
     * Find job listings by job type (Internal, External)
     */
    List<JobListingEntity> findByJobType(String jobType);
    
    /**
     * Find job listings by job type with pagination
     */
    Page<JobListingEntity> findByJobType(String jobType, Pageable pageable);
    
    /**
     * Find active job listings by job type
     */
    List<JobListingEntity> findByJobTypeAndIsActiveTrue(String jobType);
    
    /**
     * Find active job listings by job type with pagination
     */
    Page<JobListingEntity> findByJobTypeAndIsActiveTrue(String jobType, Pageable pageable);
    
    /**
     * Find active job listings with application deadline after a specific date
     */
    List<JobListingEntity> findByApplicationDeadlineAfterAndIsActiveTrue(LocalDate date);
    
    /**
     * Find active job listings with application deadline after a specific date with pagination
     */
    Page<JobListingEntity> findByApplicationDeadlineAfterAndIsActiveTrue(LocalDate date, Pageable pageable);
    
    /**
     * Search job listings by title
     */
    Page<JobListingEntity> findByTitleContainingIgnoreCaseAndIsActiveTrue(String title, Pageable pageable);
    
    /**
     * Search for job listings by title or description
     */
    @Query("SELECT j FROM JobListingEntity j WHERE (LOWER(j.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
            + "LOWER(j.jobDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND j.isActive = true")
    Page<JobListingEntity> searchByTitleOrDescription(String searchTerm, Pageable pageable);
} 