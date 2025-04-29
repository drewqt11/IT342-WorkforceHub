package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.OvertimeRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * OvertimeRequestRepository - Repository for overtime requests
 * New file: Provides methods to access overtime request data
 */
@Repository
public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequestEntity, String> {
    
    /**
     * Find all overtime requests for an employee
     */
    List<OvertimeRequestEntity> findByEmployee(EmployeeEntity employee);
    
    /**
     * Find paginated overtime requests for an employee
     */
    Page<OvertimeRequestEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    /**
     * Find overtime requests for an employee with a specific status
     */
    List<OvertimeRequestEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);
    
    /**
     * Find paginated overtime requests for an employee with a specific status
     */
    Page<OvertimeRequestEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status, Pageable pageable);
    
    /**
     * Find all overtime requests with a specific status
     */
    List<OvertimeRequestEntity> findByStatus(String status);
    
    /**
     * Find paginated overtime requests with a specific status
     */
    Page<OvertimeRequestEntity> findByStatus(String status, Pageable pageable);
    
    /**
     * Find overtime requests for an employee on a specific date
     */
    List<OvertimeRequestEntity> findByEmployeeAndDate(EmployeeEntity employee, LocalDate date);
    
    /**
     * Calculate total approved overtime hours for an employee between start and end dates
     */
    @Query("SELECT COALESCE(SUM(o.totalHours), 0) FROM OvertimeRequestEntity o WHERE o.employee = ?1 AND o.date BETWEEN ?2 AND ?3 AND o.status = 'APPROVED'")
    double getTotalApprovedOvertimeHours(EmployeeEntity employee, LocalDate startDate, LocalDate endDate);
} 