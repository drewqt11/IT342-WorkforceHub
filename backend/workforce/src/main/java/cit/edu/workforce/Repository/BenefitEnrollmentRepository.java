package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.BenefitEnrollmentEntity;
import cit.edu.workforce.Entity.BenefitPlanEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BenefitEnrollmentRepository - Repository for managing benefit enrollments
 * New file: Provides methods to access benefit enrollment data
 */
@Repository
public interface BenefitEnrollmentRepository extends JpaRepository<BenefitEnrollmentEntity, String> {
    
    List<BenefitEnrollmentEntity> findByEmployee(EmployeeEntity employee);
    
    Page<BenefitEnrollmentEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    List<BenefitEnrollmentEntity> findByBenefitPlan(BenefitPlanEntity benefitPlan);
    
    Page<BenefitEnrollmentEntity> findByBenefitPlan(BenefitPlanEntity benefitPlan, Pageable pageable);
    
    List<BenefitEnrollmentEntity> findByStatus(String status);
    
    Page<BenefitEnrollmentEntity> findByStatus(String status, Pageable pageable);
    
    Optional<BenefitEnrollmentEntity> findByEmployeeAndBenefitPlan(EmployeeEntity employee, BenefitPlanEntity benefitPlan);
    
    List<BenefitEnrollmentEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);
    
    /**
     * Find benefit enrollments by employee and status with pagination
     */
    Page<BenefitEnrollmentEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status, Pageable pageable);
} 