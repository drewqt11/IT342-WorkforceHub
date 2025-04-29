package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.LeaveBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * LeaveBalanceRepository - Repository for leave balances
 * New file: Provides methods to access leave balance data
 */
@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalanceEntity, String> {
    
    /**
     * Find all leave balances for an employee
     */
    List<LeaveBalanceEntity> findByEmployee(EmployeeEntity employee);
    
    /**
     * Find leave balance for an employee and leave type
     */
    Optional<LeaveBalanceEntity> findByEmployeeAndLeaveType(EmployeeEntity employee, String leaveType);
    
    /**
     * Check if a leave balance exists for an employee and leave type
     */
    boolean existsByEmployeeAndLeaveType(EmployeeEntity employee, String leaveType);
} 