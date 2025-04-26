package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.ImprovementPlanEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * ImprovementPlanRepository - Repository for managing improvement plans
 * New file: Provides methods to access improvement plan data
 */
@Repository
public interface ImprovementPlanRepository extends JpaRepository<ImprovementPlanEntity, String> {
    
    /**
     * Find all improvement plans for an employee
     */
    List<ImprovementPlanEntity> findByEmployee(EmployeeEntity employee);
    
    /**
     * Find paginated improvement plans for an employee
     */
    Page<ImprovementPlanEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    /**
     * Find improvement plans initiated by a specific user
     */
    List<ImprovementPlanEntity> findByInitiator(UserAccountEntity initiator);
    
    /**
     * Find paginated improvement plans initiated by a specific user
     */
    Page<ImprovementPlanEntity> findByInitiator(UserAccountEntity initiator, Pageable pageable);
    
    /**
     * Find improvement plans with a specific status
     */
    List<ImprovementPlanEntity> findByStatus(String status);
    
    /**
     * Find paginated improvement plans with a specific status
     */
    Page<ImprovementPlanEntity> findByStatus(String status, Pageable pageable);
    
    /**
     * Find active improvement plans for an employee (status = "Open")
     */
    List<ImprovementPlanEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);
    
    /**
     * Find paginated active improvement plans for an employee
     */
    Page<ImprovementPlanEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status, Pageable pageable);
    
    /**
     * Find improvement plans expiring within the next N days
     */
    @Query("SELECT p FROM ImprovementPlanEntity p WHERE p.status = 'Open' AND p.endDate BETWEEN CURRENT_DATE AND ?1")
    List<ImprovementPlanEntity> findPlansExpiringBy(LocalDate futureDate);
} 