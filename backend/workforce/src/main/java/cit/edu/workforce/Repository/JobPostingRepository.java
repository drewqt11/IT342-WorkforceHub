package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.DepartmentEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.JobPostingEntity;
import cit.edu.workforce.Entity.JobTitleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPostingEntity, String> {
    
    List<JobPostingEntity> findByIsActive(Boolean isActive);
    
    Page<JobPostingEntity> findByIsActive(Boolean isActive, Pageable pageable);
    
    List<JobPostingEntity> findByIsActiveAndIsInternal(Boolean isActive, Boolean isInternal);
    
    Page<JobPostingEntity> findByIsActiveAndIsInternal(Boolean isActive, Boolean isInternal, Pageable pageable);
    
    List<JobPostingEntity> findByDepartment(DepartmentEntity department);
    
    Page<JobPostingEntity> findByDepartment(DepartmentEntity department, Pageable pageable);
    
    List<JobPostingEntity> findByJobTitle(JobTitleEntity jobTitle);
    
    Page<JobPostingEntity> findByJobTitle(JobTitleEntity jobTitle, Pageable pageable);
    
    List<JobPostingEntity> findByPostedBy(EmployeeEntity postedBy);
    
    Page<JobPostingEntity> findByPostedBy(EmployeeEntity postedBy, Pageable pageable);
    
    List<JobPostingEntity> findByPostingDateBetween(LocalDate startDate, LocalDate endDate);
    
    Page<JobPostingEntity> findByPostingDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    Page<JobPostingEntity> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title, String description, Pageable pageable);
    
    List<JobPostingEntity> findByClosingDateGreaterThanEqual(LocalDate today);
    
    Page<JobPostingEntity> findByClosingDateGreaterThanEqual(LocalDate today, Pageable pageable);
    
    List<JobPostingEntity> findByEmploymentType(String employmentType);
    
    Page<JobPostingEntity> findByEmploymentType(String employmentType, Pageable pageable);
} 