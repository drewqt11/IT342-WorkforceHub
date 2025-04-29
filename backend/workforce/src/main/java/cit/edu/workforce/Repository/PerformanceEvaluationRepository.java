package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.PerformanceEvaluationEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * PerformanceEvaluationRepository - Repository for managing performance evaluations
 * New file: Provides methods to access performance evaluation data
 */
@Repository
public interface PerformanceEvaluationRepository extends JpaRepository<PerformanceEvaluationEntity, String> {
    
    /**
     * Find all performance evaluations for an employee
     */
    List<PerformanceEvaluationEntity> findByEmployee(EmployeeEntity employee);
    
    /**
     * Find paginated performance evaluations for an employee
     */
    Page<PerformanceEvaluationEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    /**
     * Find performance evaluations created by a specific reviewer
     */
    List<PerformanceEvaluationEntity> findByReviewer(UserAccountEntity reviewer);
    
    /**
     * Find paginated performance evaluations created by a specific reviewer
     */
    Page<PerformanceEvaluationEntity> findByReviewer(UserAccountEntity reviewer, Pageable pageable);
    
    /**
     * Find evaluations within a date range (based on evaluation date)
     */
    List<PerformanceEvaluationEntity> findByEvaluationDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find paginated evaluations within a date range (based on evaluation date)
     */
    Page<PerformanceEvaluationEntity> findByEvaluationDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Find most recent evaluation for an employee
     */
    PerformanceEvaluationEntity findFirstByEmployeeOrderByEvaluationDateDesc(EmployeeEntity employee);
} 