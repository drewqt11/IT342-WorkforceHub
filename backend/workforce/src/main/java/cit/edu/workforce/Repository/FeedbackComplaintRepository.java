package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.FeedbackComplaintEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FeedbackComplaintRepository - Repository for managing feedback and complaints
 * New file: Provides methods to access feedback and complaint data
 */
@Repository
public interface FeedbackComplaintRepository extends JpaRepository<FeedbackComplaintEntity, String> {
    
    /**
     * Find all feedback/complaints submitted by an employee
     */
    List<FeedbackComplaintEntity> findByEmployee(EmployeeEntity employee);
    
    /**
     * Find paginated feedback/complaints submitted by an employee
     */
    Page<FeedbackComplaintEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    /**
     * Find feedback/complaints resolved by a specific user
     */
    List<FeedbackComplaintEntity> findByResolver(UserAccountEntity resolver);
    
    /**
     * Find paginated feedback/complaints resolved by a specific user
     */
    Page<FeedbackComplaintEntity> findByResolver(UserAccountEntity resolver, Pageable pageable);
    
    /**
     * Find feedback/complaints with a specific status
     */
    List<FeedbackComplaintEntity> findByStatus(String status);
    
    /**
     * Find paginated feedback/complaints with a specific status
     */
    Page<FeedbackComplaintEntity> findByStatus(String status, Pageable pageable);
    
    /**
     * Find feedback/complaints submitted by an employee with a specific status
     */
    List<FeedbackComplaintEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);
    
    /**
     * Find paginated feedback/complaints submitted by an employee with a specific status
     */
    Page<FeedbackComplaintEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status, Pageable pageable);
    
    /**
     * Find feedback/complaints by category
     */
    List<FeedbackComplaintEntity> findByCategory(String category);
    
    /**
     * Find paginated feedback/complaints by category
     */
    Page<FeedbackComplaintEntity> findByCategory(String category, Pageable pageable);
    
    /**
     * Find feedback/complaints submitted within a specific date range
     */
    List<FeedbackComplaintEntity> findBySubmittedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
    
    /**
     * Find paginated feedback/complaints submitted within a specific date range
     */
    Page<FeedbackComplaintEntity> findBySubmittedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);
} 