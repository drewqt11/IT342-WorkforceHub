package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.PerformanceEvaluationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PerformanceEvaluationRepository extends JpaRepository<PerformanceEvaluationEntity, String> {
    
    List<PerformanceEvaluationEntity> findByEmployee(EmployeeEntity employee);
    
    Page<PerformanceEvaluationEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    List<PerformanceEvaluationEntity> findByEvaluator(EmployeeEntity evaluator);
    
    Page<PerformanceEvaluationEntity> findByEvaluator(EmployeeEntity evaluator, Pageable pageable);
    
    List<PerformanceEvaluationEntity> findByStatus(String status);
    
    Page<PerformanceEvaluationEntity> findByStatus(String status, Pageable pageable);
    
    List<PerformanceEvaluationEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);
    
    Page<PerformanceEvaluationEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status, Pageable pageable);
    
    List<PerformanceEvaluationEntity> findByEvaluatorAndStatus(EmployeeEntity evaluator, String status);
    
    Page<PerformanceEvaluationEntity> findByEvaluatorAndStatus(EmployeeEntity evaluator, String status, Pageable pageable);
    
    List<PerformanceEvaluationEntity> findByEvaluationType(String evaluationType);
    
    Page<PerformanceEvaluationEntity> findByEvaluationType(String evaluationType, Pageable pageable);
    
    List<PerformanceEvaluationEntity> findByDueDateBefore(LocalDate date);
    
    Page<PerformanceEvaluationEntity> findByDueDateBefore(LocalDate date, Pageable pageable);
    
    List<PerformanceEvaluationEntity> findBySubmittedDateBetween(LocalDate startDate, LocalDate endDate);
    
    Page<PerformanceEvaluationEntity> findBySubmittedDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    List<PerformanceEvaluationEntity> findByEmployeeAndEvaluationPeriodStartGreaterThanEqualAndEvaluationPeriodEndLessThanEqual(
            EmployeeEntity employee, LocalDate startDate, LocalDate endDate);
    
    Page<PerformanceEvaluationEntity> findByEmployeeAndEvaluationPeriodStartGreaterThanEqualAndEvaluationPeriodEndLessThanEqual(
            EmployeeEntity employee, LocalDate startDate, LocalDate endDate, Pageable pageable);
} 