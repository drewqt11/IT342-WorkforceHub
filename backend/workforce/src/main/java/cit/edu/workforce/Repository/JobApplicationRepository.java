package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.JobApplicationEntity;
import cit.edu.workforce.Entity.JobPostingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplicationEntity, String> {
    
    List<JobApplicationEntity> findByJobPosting(JobPostingEntity jobPosting);
    
    Page<JobApplicationEntity> findByJobPosting(JobPostingEntity jobPosting, Pageable pageable);
    
    List<JobApplicationEntity> findByEmployee(EmployeeEntity employee);
    
    Page<JobApplicationEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    List<JobApplicationEntity> findByStatus(String status);
    
    Page<JobApplicationEntity> findByStatus(String status, Pageable pageable);
    
    List<JobApplicationEntity> findByCurrentStage(String currentStage);
    
    Page<JobApplicationEntity> findByCurrentStage(String currentStage, Pageable pageable);
    
    List<JobApplicationEntity> findByJobPostingAndStatus(JobPostingEntity jobPosting, String status);
    
    Page<JobApplicationEntity> findByJobPostingAndStatus(JobPostingEntity jobPosting, String status, Pageable pageable);
    
    List<JobApplicationEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);
    
    Page<JobApplicationEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status, Pageable pageable);
    
    List<JobApplicationEntity> findByApplicationDateBetween(LocalDate startDate, LocalDate endDate);
    
    Page<JobApplicationEntity> findByApplicationDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    List<JobApplicationEntity> findBySource(String source);
    
    Page<JobApplicationEntity> findBySource(String source, Pageable pageable);
    
    Optional<JobApplicationEntity> findByEmployeeAndJobPosting(EmployeeEntity employee, JobPostingEntity jobPosting);
    
    boolean existsByEmployeeAndJobPosting(EmployeeEntity employee, JobPostingEntity jobPosting);
    
    Page<JobApplicationEntity> findByApplicantNameContainingIgnoreCaseOrApplicantEmailContainingIgnoreCase(
            String name, String email, Pageable pageable);
    
    List<JobApplicationEntity> findByReviewedBy(EmployeeEntity reviewedBy);
    
    Page<JobApplicationEntity> findByReviewedBy(EmployeeEntity reviewedBy, Pageable pageable);
}

// New file: Repository for job applications