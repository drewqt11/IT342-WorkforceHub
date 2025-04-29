package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.AttendanceRecordEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * AttendanceRecordRepository - Repository for attendance records
 * New file: Provides methods to access attendance record data
 */
@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecordEntity, String> {

    /**
     * Find an attendance record for an employee on a specific date
     */
    Optional<AttendanceRecordEntity> findByEmployeeAndDate(EmployeeEntity employee, LocalDate date);
    
    /**
     * Find all attendance records for an employee
     */
    List<AttendanceRecordEntity> findByEmployee(EmployeeEntity employee);
    
    /**
     * Find paginated attendance records for an employee
     */
    Page<AttendanceRecordEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    /**
     * Find attendance records for an employee between start and end dates
     */
    List<AttendanceRecordEntity> findByEmployeeAndDateBetween(EmployeeEntity employee, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find paginated attendance records for an employee between start and end dates
     */
    Page<AttendanceRecordEntity> findByEmployeeAndDateBetween(EmployeeEntity employee, LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Find all attendance records for a specific date
     */
    List<AttendanceRecordEntity> findByDate(LocalDate date);
    
    /**
     * Find all attendance records for a specific date with a specific status
     */
    List<AttendanceRecordEntity> findByDateAndStatus(LocalDate date, String status);
    
    /**
     * Count attendance records by employee and date range with a specific status
     */
    @Query("SELECT COUNT(a) FROM AttendanceRecordEntity a WHERE a.employee = ?1 AND a.date BETWEEN ?2 AND ?3 AND a.status = ?4")
    int countByEmployeeAndDateBetweenAndStatus(EmployeeEntity employee, LocalDate startDate, LocalDate endDate, String status);
    
    /**
     * Calculate total overtime hours for an employee between start and end dates
     */
    @Query("SELECT COALESCE(SUM(a.overtimeHours), 0) FROM AttendanceRecordEntity a WHERE a.employee = ?1 AND a.date BETWEEN ?2 AND ?3")
    double getTotalOvertimeHours(EmployeeEntity employee, LocalDate startDate, LocalDate endDate);
} 