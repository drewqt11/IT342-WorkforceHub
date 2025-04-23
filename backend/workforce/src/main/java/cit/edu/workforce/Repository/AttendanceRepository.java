package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.AttendanceEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, String> {
    
    Page<AttendanceEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    List<AttendanceEntity> findByEmployeeAndClockInTimeBetween(EmployeeEntity employee, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT a FROM AttendanceEntity a WHERE a.employee = ?1 AND a.clockOutTime IS NULL AND a.clockInTime > ?2")
    Optional<AttendanceEntity> findActiveAttendanceByEmployee(EmployeeEntity employee, LocalDateTime today);
} 