package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.ReimbursementRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReimbursementRequestRepository extends JpaRepository<ReimbursementRequestEntity, String> {
    
    List<ReimbursementRequestEntity> findByEmployee(EmployeeEntity employee);
    
    Page<ReimbursementRequestEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    List<ReimbursementRequestEntity> findByStatus(String status);
    
    Page<ReimbursementRequestEntity> findByStatus(String status, Pageable pageable);
    
    List<ReimbursementRequestEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);
    
    Page<ReimbursementRequestEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status, Pageable pageable);
    
    List<ReimbursementRequestEntity> findByExpenseType(String expenseType);
    
    Page<ReimbursementRequestEntity> findByExpenseType(String expenseType, Pageable pageable);
    
    List<ReimbursementRequestEntity> findByRequestDateBetween(LocalDate startDate, LocalDate endDate);
    
    Page<ReimbursementRequestEntity> findByRequestDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    List<ReimbursementRequestEntity> findByEmployeeAndRequestDateBetween(
            EmployeeEntity employee, LocalDate startDate, LocalDate endDate);
    
    Page<ReimbursementRequestEntity> findByEmployeeAndRequestDateBetween(
            EmployeeEntity employee, LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    List<ReimbursementRequestEntity> findByApprover(EmployeeEntity approver);
    
    Page<ReimbursementRequestEntity> findByApprover(EmployeeEntity approver, Pageable pageable);
} 