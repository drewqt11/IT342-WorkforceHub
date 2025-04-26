package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.BenefitDependentDTO;
import cit.edu.workforce.Entity.BenefitDependentEntity;
import cit.edu.workforce.Entity.BenefitEnrollmentEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Repository.BenefitDependentRepository;
import cit.edu.workforce.Repository.BenefitEnrollmentRepository;
import cit.edu.workforce.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BenefitDependentService - Service for managing benefit dependents
 * New file: This service provides methods for creating, reading, updating, and deleting benefit dependents.
 */
@Service
public class BenefitDependentService {

    private final BenefitDependentRepository benefitDependentRepository;
    private final BenefitEnrollmentRepository benefitEnrollmentRepository;
    private final EmployeeRepository employeeRepository;
    private final BenefitEnrollmentService benefitEnrollmentService;

    @Autowired
    public BenefitDependentService(
            BenefitDependentRepository benefitDependentRepository,
            BenefitEnrollmentRepository benefitEnrollmentRepository,
            EmployeeRepository employeeRepository,
            BenefitEnrollmentService benefitEnrollmentService) {
        this.benefitDependentRepository = benefitDependentRepository;
        this.benefitEnrollmentRepository = benefitEnrollmentRepository;
        this.employeeRepository = employeeRepository;
        this.benefitEnrollmentService = benefitEnrollmentService;
    }

    /**
     * Get all dependents for a specific enrollment
     *
     * @param enrollmentId Benefit enrollment ID
     * @return List of benefit dependent DTOs
     */
    public List<BenefitDependentDTO> getDependentsByEnrollmentId(String enrollmentId) {
        BenefitEnrollmentEntity enrollment = benefitEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit enrollment not found with ID: " + enrollmentId));

        // Check if the current user has access to this enrollment
        if (!benefitEnrollmentService.isAuthorizedToAccessEnrollment(enrollment)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't have permission to access this enrollment's dependents");
        }

        return benefitDependentRepository.findByBenefitEnrollment(enrollment).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated dependents for a specific enrollment
     *
     * @param enrollmentId Benefit enrollment ID
     * @param pageable     Pagination information
     * @return Page of benefit dependent DTOs
     */
    public Page<BenefitDependentDTO> getDependentsByEnrollmentId(String enrollmentId, Pageable pageable) {
        BenefitEnrollmentEntity enrollment = benefitEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit enrollment not found with ID: " + enrollmentId));

        // Check if the current user has access to this enrollment
        if (!benefitEnrollmentService.isAuthorizedToAccessEnrollment(enrollment)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't have permission to access this enrollment's dependents");
        }

        return benefitDependentRepository.findByBenefitEnrollment(enrollment, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get a dependent by ID
     *
     * @param dependentId Dependent ID
     * @return Benefit dependent DTO
     */
    public BenefitDependentDTO getDependentById(String dependentId) {
        BenefitDependentEntity dependent = benefitDependentRepository.findById(dependentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit dependent not found with ID: " + dependentId));

        // Check if the current user has access to this dependent's enrollment
        if (!benefitEnrollmentService.isAuthorizedToAccessEnrollment(dependent.getBenefitEnrollment())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't have permission to access this dependent");
        }

        return convertToDTO(dependent);
    }

    /**
     * Add a new dependent to a benefit enrollment
     *
     * @param enrollmentId     Benefit enrollment ID
     * @param benefitDependentDTO Dependent information
     * @return Created benefit dependent DTO
     */
    @Transactional
    public BenefitDependentDTO addDependent(String enrollmentId, BenefitDependentDTO benefitDependentDTO) {
        // Validate input
        if (benefitDependentDTO.getName() == null || benefitDependentDTO.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dependent name is required");
        }

        if (benefitDependentDTO.getRelationship() == null || benefitDependentDTO.getRelationship().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Relationship is required");
        }

        BenefitEnrollmentEntity enrollment = benefitEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit enrollment not found with ID: " + enrollmentId));

        // Check if the current user has access to this enrollment
        if (!benefitEnrollmentService.isAuthorizedToAccessEnrollment(enrollment)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't have permission to add dependents to this enrollment");
        }

        // Check if the enrollment is active
        if (!"Active".equals(enrollment.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot add dependents to an inactive/cancelled enrollment");
        }

        BenefitDependentEntity dependent = new BenefitDependentEntity();
        dependent.setBenefitEnrollment(enrollment);
        dependent.setName(benefitDependentDTO.getName());
        dependent.setRelationship(benefitDependentDTO.getRelationship());
        dependent.setBirthdate(benefitDependentDTO.getBirthdate());

        BenefitDependentEntity savedDependent = benefitDependentRepository.save(dependent);
        return convertToDTO(savedDependent);
    }

    /**
     * Update an existing dependent
     *
     * @param dependentId         Dependent ID
     * @param benefitDependentDTO Updated dependent information
     * @return Updated benefit dependent DTO
     */
    @Transactional
    public BenefitDependentDTO updateDependent(String dependentId, BenefitDependentDTO benefitDependentDTO) {
        BenefitDependentEntity dependent = benefitDependentRepository.findById(dependentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit dependent not found with ID: " + dependentId));

        // Check if the current user has access to this dependent's enrollment
        if (!benefitEnrollmentService.isAuthorizedToAccessEnrollment(dependent.getBenefitEnrollment())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't have permission to update this dependent");
        }

        // Check if the enrollment is active
        if (!"Active".equals(dependent.getBenefitEnrollment().getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot update dependents in an inactive/cancelled enrollment");
        }

        if (benefitDependentDTO.getName() != null && !benefitDependentDTO.getName().trim().isEmpty()) {
            dependent.setName(benefitDependentDTO.getName());
        }

        if (benefitDependentDTO.getRelationship() != null && !benefitDependentDTO.getRelationship().trim().isEmpty()) {
            dependent.setRelationship(benefitDependentDTO.getRelationship());
        }

        if (benefitDependentDTO.getBirthdate() != null) {
            dependent.setBirthdate(benefitDependentDTO.getBirthdate());
        }

        BenefitDependentEntity updatedDependent = benefitDependentRepository.save(dependent);
        return convertToDTO(updatedDependent);
    }

    /**
     * Delete a dependent
     *
     * @param dependentId Dependent ID
     */
    @Transactional
    public void deleteDependent(String dependentId) {
        BenefitDependentEntity dependent = benefitDependentRepository.findById(dependentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit dependent not found with ID: " + dependentId));

        // Check if the current user has access to this dependent's enrollment
        if (!benefitEnrollmentService.isAuthorizedToAccessEnrollment(dependent.getBenefitEnrollment())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't have permission to delete this dependent");
        }

        // Check if the enrollment is active
        if (!"Active".equals(dependent.getBenefitEnrollment().getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot delete dependents from an inactive/cancelled enrollment");
        }

        benefitDependentRepository.delete(dependent);
    }

    /**
     * Convert a BenefitDependentEntity to a BenefitDependentDTO
     *
     * @param dependent Benefit dependent entity
     * @return Benefit dependent DTO
     */
    private BenefitDependentDTO convertToDTO(BenefitDependentEntity dependent) {
        BenefitDependentDTO dto = new BenefitDependentDTO();
        dto.setDependentId(dependent.getDependentId());
        dto.setEnrollmentId(dependent.getBenefitEnrollment().getEnrollmentId());
        dto.setName(dependent.getName());
        dto.setRelationship(dependent.getRelationship());
        dto.setBirthdate(dependent.getBirthdate());
        return dto;
    }
} 