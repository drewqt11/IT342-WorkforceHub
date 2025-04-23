package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.OvertimeRequestEntity;
import cit.edu.workforce.Enum.OvertimeRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequestEntity, String> {

    Page<OvertimeRequestEntity> findByEmployeeAndIsActiveTrue(EmployeeEntity employee, Pageable pageable);

    Page<OvertimeRequestEntity> findByStatusAndIsActiveTrue(OvertimeRequestStatus status, Pageable pageable);

    List<OvertimeRequestEntity> findByEmployeeAndStatusAndIsActiveTrue(
            EmployeeEntity employee, OvertimeRequestStatus status);

    @Query("SELECT or FROM OvertimeRequestEntity or WHERE or.employee = ?1 AND " +
            "or.date BETWEEN ?2 AND ?3 AND or.status = 'APPROVED' AND or.isActive = true")
    List<OvertimeRequestEntity> findApprovedOvertimeRequestsForDateRange(
            EmployeeEntity employee, LocalDate startDate, LocalDate endDate);

    @Query("SELECT or FROM OvertimeRequestEntity or WHERE " +
            "or.date BETWEEN ?1 AND ?2 AND or.status = 'APPROVED' AND or.isActive = true")
    Page<OvertimeRequestEntity> findApprovedOvertimeRequestsForDateRange(
            LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT or FROM OvertimeRequestEntity or WHERE or.employee.department.id = ?1 AND " +
            "or.status = 'PENDING' AND or.isActive = true")
    Page<OvertimeRequestEntity> findPendingOvertimeRequestsByDepartment(String departmentId, Pageable pageable);
}

// New file: Repository for overtime requests
// New mapping: findByEmployeeAndIsActiveTrue
// New mapping: findByStatusAndIsActiveTrue
// New mapping: findByEmployeeAndStatusAndIsActiveTrue
// New mapping: findApprovedOvertimeRequestsForDateRange
// New mapping: findPendingOvertimeRequestsByDepartment 