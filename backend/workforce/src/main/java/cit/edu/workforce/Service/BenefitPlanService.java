package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.BenefitPlanDTO;
import cit.edu.workforce.Entity.BenefitPlanEntity;
import cit.edu.workforce.Repository.BenefitEnrollmentRepository;
import cit.edu.workforce.Repository.BenefitPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * BenefitPlanService - Service for managing benefit plans
 * New file: This service provides methods for creating, reading, updating, and deleting benefit plans.
 */
@Service
public class BenefitPlanService {

    private final BenefitPlanRepository benefitPlanRepository;
    private final BenefitEnrollmentRepository benefitEnrollmentRepository;

    @Autowired
    public BenefitPlanService(
            BenefitPlanRepository benefitPlanRepository,
            BenefitEnrollmentRepository benefitEnrollmentRepository) {
        this.benefitPlanRepository = benefitPlanRepository;
        this.benefitEnrollmentRepository = benefitEnrollmentRepository;
    }

    /**
     * Get all active benefit plans
     *
     * @return List of benefit plan DTOs
     */
    public List<BenefitPlanDTO> getAllActiveBenefitPlans() {
        return benefitPlanRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated active benefit plans
     *
     * @param pageable Pagination information
     * @return Page of benefit plan DTOs
     */
    public Page<BenefitPlanDTO> getAllActiveBenefitPlans(Pageable pageable) {
        return benefitPlanRepository.findByIsActiveTrue(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get a benefit plan by ID
     *
     * @param planId Benefit plan ID
     * @return Benefit plan DTO
     */
    public BenefitPlanDTO getBenefitPlanById(String planId) {
        BenefitPlanEntity benefitPlan = benefitPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit plan not found with ID: " + planId));
        
        return convertToDTO(benefitPlan);
    }

    /**
     * Get benefit plans by plan type
     *
     * @param planType Plan type (Health, Dental, Life, etc.)
     * @return List of benefit plan DTOs
     */
    public List<BenefitPlanDTO> getBenefitPlansByType(String planType) {
        return benefitPlanRepository.findByPlanTypeAndIsActiveTrue(planType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated benefit plans by plan type
     *
     * @param planType Plan type
     * @param pageable Pagination information
     * @return Page of benefit plan DTOs
     */
    public Page<BenefitPlanDTO> getBenefitPlansByType(String planType, Pageable pageable) {
        return benefitPlanRepository.findByPlanTypeAndIsActiveTrue(planType, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Create a new benefit plan
     *
     * @param benefitPlanDTO Benefit plan information
     * @return Created benefit plan DTO
     */
    @Transactional
    public BenefitPlanDTO createBenefitPlan(BenefitPlanDTO benefitPlanDTO) {
        // Validate plan name
        if (benefitPlanDTO.getPlanName() == null || benefitPlanDTO.getPlanName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan name is required");
        }
        
        // Validate plan type
        if (benefitPlanDTO.getPlanType() == null || benefitPlanDTO.getPlanType().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan type is required");
        }

        BenefitPlanEntity benefitPlan = new BenefitPlanEntity();
        benefitPlan.setPlanName(benefitPlanDTO.getPlanName());
        benefitPlan.setDescription(benefitPlanDTO.getDescription());
        benefitPlan.setProvider(benefitPlanDTO.getProvider());
        benefitPlan.setEligibility(benefitPlanDTO.getEligibility());
        benefitPlan.setPlanType(benefitPlanDTO.getPlanType());
        benefitPlan.setMaxCoverage(benefitPlanDTO.getMaxCoverage());
        benefitPlan.setActive(true);

        BenefitPlanEntity savedPlan = benefitPlanRepository.save(benefitPlan);
        return convertToDTO(savedPlan);
    }

    /**
     * Update an existing benefit plan
     *
     * @param planId         Benefit plan ID
     * @param benefitPlanDTO Updated benefit plan information
     * @return Updated benefit plan DTO
     */
    @Transactional
    public BenefitPlanDTO updateBenefitPlan(String planId, BenefitPlanDTO benefitPlanDTO) {
        BenefitPlanEntity benefitPlan = benefitPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit plan not found with ID: " + planId));

        if (benefitPlanDTO.getPlanName() != null && !benefitPlanDTO.getPlanName().trim().isEmpty()) {
            benefitPlan.setPlanName(benefitPlanDTO.getPlanName());
        }
        
        if (benefitPlanDTO.getDescription() != null) {
            benefitPlan.setDescription(benefitPlanDTO.getDescription());
        }
        
        if (benefitPlanDTO.getProvider() != null) {
            benefitPlan.setProvider(benefitPlanDTO.getProvider());
        }
        
        if (benefitPlanDTO.getEligibility() != null) {
            benefitPlan.setEligibility(benefitPlanDTO.getEligibility());
        }
        
        if (benefitPlanDTO.getPlanType() != null && !benefitPlanDTO.getPlanType().trim().isEmpty()) {
            benefitPlan.setPlanType(benefitPlanDTO.getPlanType());
        }
        
        if (benefitPlanDTO.getMaxCoverage() != null) {
            benefitPlan.setMaxCoverage(benefitPlanDTO.getMaxCoverage());
        }

        BenefitPlanEntity updatedPlan = benefitPlanRepository.save(benefitPlan);
        return convertToDTO(updatedPlan);
    }

    /**
     * Deactivate a benefit plan
     *
     * @param planId Benefit plan ID
     * @return Deactivated benefit plan DTO
     */
    @Transactional
    public BenefitPlanDTO deactivateBenefitPlan(String planId) {
        BenefitPlanEntity benefitPlan = benefitPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit plan not found with ID: " + planId));

        benefitPlan.setActive(false);
        BenefitPlanEntity deactivatedPlan = benefitPlanRepository.save(benefitPlan);
        return convertToDTO(deactivatedPlan);
    }

    /**
     * Activate a benefit plan
     *
     * @param planId Benefit plan ID
     * @return Activated benefit plan DTO
     */
    @Transactional
    public BenefitPlanDTO activateBenefitPlan(String planId) {
        BenefitPlanEntity benefitPlan = benefitPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit plan not found with ID: " + planId));

        benefitPlan.setActive(true);
        BenefitPlanEntity activatedPlan = benefitPlanRepository.save(benefitPlan);
        return convertToDTO(activatedPlan);
    }

    /**
     * Convert a BenefitPlanEntity to a BenefitPlanDTO
     *
     * @param benefitPlan Benefit plan entity
     * @return Benefit plan DTO
     */
    private BenefitPlanDTO convertToDTO(BenefitPlanEntity benefitPlan) {
        BenefitPlanDTO dto = new BenefitPlanDTO();
        dto.setPlanId(benefitPlan.getPlanId());
        dto.setPlanName(benefitPlan.getPlanName());
        dto.setDescription(benefitPlan.getDescription());
        dto.setProvider(benefitPlan.getProvider());
        dto.setEligibility(benefitPlan.getEligibility());
        dto.setPlanType(benefitPlan.getPlanType());
        dto.setMaxCoverage(benefitPlan.getMaxCoverage());
        dto.setCreatedAt(benefitPlan.getCreatedAt());
        dto.setActive(benefitPlan.isActive());
        
        // Count enrollments
        long enrollmentCount = benefitPlan.getEnrollments().size();
        dto.setEnrollmentCount(enrollmentCount);
        
        return dto;
    }
} 