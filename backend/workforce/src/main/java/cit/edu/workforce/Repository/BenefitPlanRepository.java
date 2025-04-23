package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.BenefitPlanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BenefitPlanRepository extends JpaRepository<BenefitPlanEntity, String> {
    
    List<BenefitPlanEntity> findByIsActive(Boolean isActive);
    
    Page<BenefitPlanEntity> findByIsActive(Boolean isActive, Pageable pageable);
    
    List<BenefitPlanEntity> findByPlanType(String planType);
    
    Page<BenefitPlanEntity> findByPlanType(String planType, Pageable pageable);
    
    Page<BenefitPlanEntity> findByPlanNameContainingIgnoreCase(String planName, Pageable pageable);
    
    boolean existsByPlanName(String planName);
}

// New file: Repository for benefit plans