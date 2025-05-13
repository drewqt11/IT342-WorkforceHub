package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.ReimbursementRequestEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * ReimbursementRequestRepository - Repository for reimbursement requests
 * New file: Provides methods to access reimbursement request data
 */
@Repository
public interface ReimbursementRequestRepository extends JpaRepository<ReimbursementRequestEntity, String> {
    
    /**
     * Find all reimbursement requests for a specific employee
     */
    List<ReimbursementRequestEntity> findByEmployee(EmployeeEntity employee);
    
    /**
     * Find paginated reimbursement requests for a specific employee
     */
    Page<ReimbursementRequestEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    /**
     * Find reimbursement requests by status (PENDING, APPROVED, REJECTED)
     */
    List<ReimbursementRequestEntity> findByStatus(String status);
    
    /**
     * Find paginated reimbursement requests by status
     */
    Page<ReimbursementRequestEntity> findByStatus(String status, Pageable pageable);
    
    /**
     * Find reimbursement requests by reviewer
     */
    List<ReimbursementRequestEntity> findByReviewedBy(UserAccountEntity reviewedBy);
    
    /**
     * Find reimbursement requests by expense date range
     */
    @Query("SELECT r FROM ReimbursementRequestEntity r WHERE r.expenseDate BETWEEN :startDate AND :endDate")
    List<ReimbursementRequestEntity> findByExpenseDateBetween(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    /**
     * Find paginated reimbursement requests by expense date range
     */
    @Query("SELECT r FROM ReimbursementRequestEntity r WHERE r.expenseDate BETWEEN :startDate AND :endDate")
    Page<ReimbursementRequestEntity> findByExpenseDateBetween(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate, 
            Pageable pageable);
    
    /**
     * Find reimbursement requests by employee and status
     */
    Page<ReimbursementRequestEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status, Pageable pageable);
} 