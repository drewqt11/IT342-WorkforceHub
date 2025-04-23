package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.BenefitPlanEntity;
import cit.edu.workforce.Entity.EmployeeBenefitEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeBenefitRepository extends JpaRepository<EmployeeBenefitEntity, String> {
    
    List<EmployeeBenefitEntity> findByEmployee(EmployeeEntity employee);
    
    Page<EmployeeBenefitEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    List<EmployeeBenefitEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);
    
    List<EmployeeBenefitEntity> findByBenefitPlan(BenefitPlanEntity benefitPlan);
    
    Page<EmployeeBenefitEntity> findByBenefitPlan(BenefitPlanEntity benefitPlan, Pageable pageable);
    
    List<EmployeeBenefitEntity> findByStatus(String status);
    
    Page<EmployeeBenefitEntity> findByStatus(String status, Pageable pageable);
    
    Optional<EmployeeBenefitEntity> findByEmployeeAndBenefitPlan(EmployeeEntity employee, BenefitPlanEntity benefitPlan);
    
    boolean existsByEmployeeAndBenefitPlan(EmployeeEntity employee, BenefitPlanEntity benefitPlan);
    
    List<EmployeeBenefitEntity> findByEmployeeAndCoverageEndDateBefore(EmployeeEntity employee, LocalDate date);
    
    List<EmployeeBenefitEntity> findByEmployeeAndCoverageStartDateLessThanEqualAndCoverageEndDateGreaterThanEqual(
            EmployeeEntity employee, LocalDate currentDate, LocalDate currentDate2);
}

// New file: Repository for employee benefit enrollments 