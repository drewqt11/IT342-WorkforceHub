package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.AttendanceSummaryEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSummaryRepository extends JpaRepository<AttendanceSummaryEntity, String> {
    
    Page<AttendanceSummaryEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    List<AttendanceSummaryEntity> findByEmployeeAndDateBetween(EmployeeEntity employee, LocalDate startDate, LocalDate endDate);
    
    Optional<AttendanceSummaryEntity> findByEmployeeAndDate(EmployeeEntity employee, LocalDate date);
} 