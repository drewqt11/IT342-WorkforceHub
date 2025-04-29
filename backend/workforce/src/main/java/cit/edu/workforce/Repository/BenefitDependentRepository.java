package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.BenefitDependentEntity;
import cit.edu.workforce.Entity.BenefitEnrollmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * BenefitDependentRepository - Repository for benefit dependents
 * New file: Provides methods to access benefit dependent data
 */
@Repository
public interface BenefitDependentRepository extends JpaRepository<BenefitDependentEntity, String> {
    
    /**
     * Find all dependents for a specific benefit enrollment
     */
    List<BenefitDependentEntity> findByBenefitEnrollment(BenefitEnrollmentEntity benefitEnrollment);
    
    /**
     * Find paginated dependents for a specific benefit enrollment
     */
    Page<BenefitDependentEntity> findByBenefitEnrollment(BenefitEnrollmentEntity benefitEnrollment, Pageable pageable);
    
    /**
     * Find dependents by name pattern
     */
    List<BenefitDependentEntity> findByNameContainingIgnoreCase(String namePattern);
    
    /**
     * Find dependents by relationship type
     */
    List<BenefitDependentEntity> findByRelationship(String relationship);
    
    /**
     * Count dependents for a specific benefit enrollment
     */
    long countByBenefitEnrollment(BenefitEnrollmentEntity benefitEnrollment);
} 