package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.LeaveRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * LeaveRequestRepository - Repository for leave requests
 * New file: Provides methods to access leave request data
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequestEntity, String> {
    
    /**
     * Find all leave requests for an employee
     */
    List<LeaveRequestEntity> findByEmployee(EmployeeEntity employee);
    
    /**
     * Find paginated leave requests for an employee
     */
    Page<LeaveRequestEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    /**
     * Find leave requests for an employee with a specific status
     */
    List<LeaveRequestEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);
    
    /**
     * Find paginated leave requests for an employee with a specific status
     */
    Page<LeaveRequestEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status, Pageable pageable);
    
    /**
     * Find all leave requests with a specific status
     */
    List<LeaveRequestEntity> findByStatus(String status);
    
    /**
     * Find paginated leave requests with a specific status
     */
    Page<LeaveRequestEntity> findByStatus(String status, Pageable pageable);
    
    /**
     * Find all leave requests overlapping with a date range for an employee
     */
    @Query("SELECT l FROM LeaveRequestEntity l WHERE l.employee = ?1 AND ((l.startDate BETWEEN ?2 AND ?3) OR (l.endDate BETWEEN ?2 AND ?3) OR (?2 BETWEEN l.startDate AND l.endDate))")
    List<LeaveRequestEntity> findOverlappingLeaveRequests(EmployeeEntity employee, LocalDate startDate, LocalDate endDate);
    
    /**
     * Count active leave requests for an employee on a specific date
     */
    @Query("SELECT COUNT(l) FROM LeaveRequestEntity l WHERE l.employee = ?1 AND ?2 BETWEEN l.startDate AND l.endDate AND l.status = 'APPROVED'")
    int countActiveLeaveRequests(EmployeeEntity employee, LocalDate date);
} 