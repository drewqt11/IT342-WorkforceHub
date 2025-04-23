package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.BenefitPlanDTO;
import cit.edu.workforce.Entity.BenefitPlanEntity;
import cit.edu.workforce.Repository.BenefitPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BenefitPlanService {

    private final BenefitPlanRepository benefitPlanRepository;

    @Autowired
    public BenefitPlanService(BenefitPlanRepository benefitPlanRepository) {
        this.benefitPlanRepository = benefitPlanRepository;
    }

    @Transactional(readOnly = true)
    public List<BenefitPlanDTO> getAllBenefitPlans() {
        return benefitPlanRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<BenefitPlanDTO> getAllBenefitPlansPaged(Pageable pageable) {
        return benefitPlanRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<BenefitPlanDTO> getAllActiveBenefitPlans() {
        return benefitPlanRepository.findByIsActive(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<BenefitPlanDTO> getAllActiveBenefitPlansPaged(Pageable pageable) {
        return benefitPlanRepository.findByIsActive(true, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<BenefitPlanDTO> getBenefitPlansByType(String planType) {
        return benefitPlanRepository.findByPlanType(planType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<BenefitPlanDTO> getBenefitPlansByTypePaged(String planType, Pageable pageable) {
        return benefitPlanRepository.findByPlanType(planType, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<BenefitPlanDTO> searchBenefitPlans(String planName, Pageable pageable) {
        return benefitPlanRepository.findByPlanNameContainingIgnoreCase(planName, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<BenefitPlanDTO> getBenefitPlanById(String benefitPlanId) {
        return benefitPlanRepository.findById(benefitPlanId)
                .map(this::convertToDTO);
    }

    @Transactional
    public BenefitPlanDTO createBenefitPlan(BenefitPlanDTO benefitPlanDTO) {
        // Check if a plan with the same name already exists
        if (benefitPlanRepository.existsByPlanName(benefitPlanDTO.getPlanName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Benefit plan with this name already exists");
        }

        BenefitPlanEntity benefitPlan = convertToEntity(benefitPlanDTO);
        BenefitPlanEntity savedBenefitPlan = benefitPlanRepository.save(benefitPlan);
        return convertToDTO(savedBenefitPlan);
    }

    @Transactional
    public BenefitPlanDTO updateBenefitPlan(String benefitPlanId, BenefitPlanDTO benefitPlanDTO) {
        BenefitPlanEntity existingBenefitPlan = benefitPlanRepository.findById(benefitPlanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Benefit plan not found with ID: " + benefitPlanId));

        // Check if name is being changed and if the new name already exists
        if (!existingBenefitPlan.getPlanName().equals(benefitPlanDTO.getPlanName()) && 
                benefitPlanRepository.existsByPlanName(benefitPlanDTO.getPlanName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Benefit plan with this name already exists");
        }

        // Update fields
        existingBenefitPlan.setPlanName(benefitPlanDTO.getPlanName());
        existingBenefitPlan.setDescription(benefitPlanDTO.getDescription());
        existingBenefitPlan.setPlanType(benefitPlanDTO.getPlanType());
        existingBenefitPlan.setProvider(benefitPlanDTO.getProvider());
        existingBenefitPlan.setCoverageDetails(benefitPlanDTO.getCoverageDetails());
        existingBenefitPlan.setStartDate(benefitPlanDTO.getStartDate());
        existingBenefitPlan.setEndDate(benefitPlanDTO.getEndDate());
        
        // Only update isActive if it's provided
        if (benefitPlanDTO.getIsActive() != null) {
            existingBenefitPlan.setIsActive(benefitPlanDTO.getIsActive());
        }

        BenefitPlanEntity updatedBenefitPlan = benefitPlanRepository.save(existingBenefitPlan);
        return convertToDTO(updatedBenefitPlan);
    }

    @Transactional
    public BenefitPlanDTO activateBenefitPlan(String benefitPlanId) {
        BenefitPlanEntity benefitPlan = benefitPlanRepository.findById(benefitPlanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Benefit plan not found with ID: " + benefitPlanId));
        
        benefitPlan.setIsActive(true);
        BenefitPlanEntity updatedBenefitPlan = benefitPlanRepository.save(benefitPlan);
        return convertToDTO(updatedBenefitPlan);
    }

    @Transactional
    public BenefitPlanDTO deactivateBenefitPlan(String benefitPlanId) {
        BenefitPlanEntity benefitPlan = benefitPlanRepository.findById(benefitPlanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Benefit plan not found with ID: " + benefitPlanId));
        
        benefitPlan.setIsActive(false);
        BenefitPlanEntity updatedBenefitPlan = benefitPlanRepository.save(benefitPlan);
        return convertToDTO(updatedBenefitPlan);
    }

    @Transactional
    public void deleteBenefitPlan(String benefitPlanId) {
        if (!benefitPlanRepository.existsById(benefitPlanId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Benefit plan not found with ID: " + benefitPlanId);
        }
        
        benefitPlanRepository.deleteById(benefitPlanId);
    }

    // Helper methods
    private BenefitPlanDTO convertToDTO(BenefitPlanEntity benefitPlan) {
        BenefitPlanDTO dto = new BenefitPlanDTO();
        dto.setBenefitPlanId(benefitPlan.getBenefitPlanId());
        dto.setPlanName(benefitPlan.getPlanName());
        dto.setDescription(benefitPlan.getDescription());
        dto.setPlanType(benefitPlan.getPlanType());
        dto.setProvider(benefitPlan.getProvider());
        dto.setCoverageDetails(benefitPlan.getCoverageDetails());
        dto.setStartDate(benefitPlan.getStartDate());
        dto.setEndDate(benefitPlan.getEndDate());
        dto.setIsActive(benefitPlan.getIsActive());
        return dto;
    }

    private BenefitPlanEntity convertToEntity(BenefitPlanDTO dto) {
        BenefitPlanEntity entity = new BenefitPlanEntity();
        entity.setPlanName(dto.getPlanName());
        entity.setDescription(dto.getDescription());
        entity.setPlanType(dto.getPlanType());
        entity.setProvider(dto.getProvider());
        entity.setCoverageDetails(dto.getCoverageDetails());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        
        // Default to active if not provided
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        
        return entity;
    }
} 