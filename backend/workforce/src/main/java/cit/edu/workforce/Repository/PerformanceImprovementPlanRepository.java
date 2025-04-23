package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.PerformanceImprovementPlanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PerformanceImprovementPlanRepository extends JpaRepository<PerformanceImprovementPlanEntity, String> {
    
    List<PerformanceImprovementPlanEntity> findByEmployee(EmployeeEntity employee);
    
    Page<PerformanceImprovementPlanEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    List<PerformanceImprovementPlanEntity> findByManager(EmployeeEntity manager);
    
    Page<PerformanceImprovementPlanEntity> findByManager(EmployeeEntity manager, Pageable pageable);
    
    List<PerformanceImprovementPlanEntity> findByHrRepresentative(EmployeeEntity hrRepresentative);
    
    Page<PerformanceImprovementPlanEntity> findByHrRepresentative(EmployeeEntity hrRepresentative, Pageable pageable);
    
    List<PerformanceImprovementPlanEntity> findByStatus(String status);
    
    Page<PerformanceImprovementPlanEntity> findByStatus(String status, Pageable pageable);
    
    List<PerformanceImprovementPlanEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);
    
    Page<PerformanceImprovementPlanEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status, Pageable pageable);
    
    List<PerformanceImprovementPlanEntity> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    
    Page<PerformanceImprovementPlanEntity> findByStartDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    List<PerformanceImprovementPlanEntity> findByEndDateBefore(LocalDate date);
    
    Page<PerformanceImprovementPlanEntity> findByEndDateBefore(LocalDate date, Pageable pageable);
    
    List<PerformanceImprovementPlanEntity> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate currentDate, LocalDate currentDate2);
    
    Page<PerformanceImprovementPlanEntity> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate currentDate, LocalDate currentDate2, Pageable pageable);
    
    boolean existsByEmployeeAndStatusAndEndDateGreaterThanEqual(
            EmployeeEntity employee, String status, LocalDate date);
}

// New file: Repository for performance improvement plans