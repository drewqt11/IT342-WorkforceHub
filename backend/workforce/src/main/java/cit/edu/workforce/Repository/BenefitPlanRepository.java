package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.BenefitPlanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * BenefitPlanRepository - Repository for benefit plans
 * New file: Provides methods to access benefit plan data
 */
@Repository
public interface BenefitPlanRepository extends JpaRepository<BenefitPlanEntity, String> {
    
    /**
     * Find all active benefit plans
     */
    List<BenefitPlanEntity> findByIsActiveTrue();
    
    /**
     * Find paginated active benefit plans
     */
    Page<BenefitPlanEntity> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Find benefit plans by plan type (Health, Dental, Life, etc.)
     */
    List<BenefitPlanEntity> findByPlanTypeAndIsActiveTrue(String planType);
    
    /**
     * Find paginated benefit plans by plan type
     */
    Page<BenefitPlanEntity> findByPlanTypeAndIsActiveTrue(String planType, Pageable pageable);
    
    /**
     * Find benefit plans by provider
     */
    List<BenefitPlanEntity> findByProviderAndIsActiveTrue(String provider);
    
    /**
     * Find paginated benefit plans by provider
     */
    Page<BenefitPlanEntity> findByProviderAndIsActiveTrue(String provider, Pageable pageable);
} 