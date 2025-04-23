package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.LeaveRequestEntity;
import cit.edu.workforce.Enum.LeaveRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequestEntity, String> {

    Page<LeaveRequestEntity> findByEmployeeAndIsActiveTrue(EmployeeEntity employee, Pageable pageable);

    Page<LeaveRequestEntity> findByStatusAndIsActiveTrue(LeaveRequestStatus status, Pageable pageable);

    List<LeaveRequestEntity> findByEmployeeAndStatusAndIsActiveTrue(
            EmployeeEntity employee, LeaveRequestStatus status);

    @Query("SELECT lr FROM LeaveRequestEntity lr WHERE lr.employee = ?1 AND " +
            "((lr.startDate BETWEEN ?2 AND ?3) OR (lr.endDate BETWEEN ?2 AND ?3)) AND " +
            "lr.status = 'APPROVED' AND lr.isActive = true")
    List<LeaveRequestEntity> findApprovedLeaveRequestsForDateRange(
            EmployeeEntity employee, LocalDate startDate, LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequestEntity lr WHERE " +
            "((lr.startDate BETWEEN ?1 AND ?2) OR (lr.endDate BETWEEN ?1 AND ?2)) AND " +
            "lr.status = 'APPROVED' AND lr.isActive = true")
    Page<LeaveRequestEntity> findApprovedLeaveRequestsForDateRange(
            LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT lr FROM LeaveRequestEntity lr WHERE lr.employee.department.id = ?1 AND " +
            "lr.status = 'PENDING' AND lr.isActive = true")
    Page<LeaveRequestEntity> findPendingLeaveRequestsByDepartment(String departmentId, Pageable pageable);
}

// New file: Repository for leave requests
// New mapping: findByEmployeeAndIsActiveTrue
// New mapping: findByStatusAndIsActiveTrue
// New mapping: findByEmployeeAndStatusAndIsActiveTrue
// New mapping: findApprovedLeaveRequestsForDateRange
// New mapping: findPendingLeaveRequestsByDepartment 