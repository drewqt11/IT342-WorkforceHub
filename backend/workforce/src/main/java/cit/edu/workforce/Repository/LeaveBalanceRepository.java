package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.LeaveBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing leave balance data
 */
@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalanceEntity, String> {
    
    /**
     * Find all leave balances for a specific employee
     */
    List<LeaveBalanceEntity> findByEmployeeEmployeeId(String employeeId);
    
    /**
     * Find a specific leave balance for an employee by leave type
     */
    Optional<LeaveBalanceEntity> findByEmployeeEmployeeIdAndLeaveType(String employeeId, String leaveType);
    
    /**
     * Delete all leave balances for a specific employee
     */
    void deleteByEmployeeEmployeeId(String employeeId);
} 