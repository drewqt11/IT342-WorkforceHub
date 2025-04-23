package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.LeaveBalanceEntity;
import cit.edu.workforce.Entity.LeaveTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalanceEntity, String> {

    List<LeaveBalanceEntity> findByEmployeeAndIsActiveTrue(EmployeeEntity employee);

    Page<LeaveBalanceEntity> findByEmployeeAndIsActiveTrue(EmployeeEntity employee, Pageable pageable);

    List<LeaveBalanceEntity> findByEmployeeAndYearAndIsActiveTrue(EmployeeEntity employee, Integer year);

    Optional<LeaveBalanceEntity> findByEmployeeAndLeaveTypeAndYearAndIsActiveTrue(
            EmployeeEntity employee, LeaveTypeEntity leaveType, Integer year);
}

// New file: Repository for leave balances
// New mapping: findByEmployeeAndIsActiveTrue
// New mapping: findByEmployeeAndYearAndIsActiveTrue
// New mapping: findByEmployeeAndLeaveTypeAndYearAndIsActiveTrue 