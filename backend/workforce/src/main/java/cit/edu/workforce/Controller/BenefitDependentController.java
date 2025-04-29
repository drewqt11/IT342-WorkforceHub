package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.BenefitDependentDTO;
import cit.edu.workforce.Service.BenefitDependentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BenefitDependentController - Provides API endpoints for benefit dependent management
 * New file: Implements endpoints for creating, reading, updating, and deleting benefit dependents.
 * 
 * This controller handles all benefit dependent-related operations including:
 * - Adding dependents to benefit enrollments
 * - Viewing and managing dependents
 * - Updating dependent information
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Benefit Dependents", description = "Benefit dependent management APIs")
@SecurityRequirement(name = "bearerAuth")
public class BenefitDependentController {

    private final BenefitDependentService benefitDependentService;

    @Autowired
    public BenefitDependentController(BenefitDependentService benefitDependentService) {
        this.benefitDependentService = benefitDependentService;
    }

    /**
     * Get all dependents for a benefit enrollment
     * Employees can only access their own enrollments' dependents, HR/Admin can access any
     */
    @GetMapping("/benefit-enrollments/{enrollmentId}/dependents")
    @Operation(summary = "Get enrollment dependents", description = "Get all dependents for a benefit enrollment")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<BenefitDependentDTO>> getDependentsByEnrollmentId(
            @Parameter(description = "Benefit enrollment ID") @PathVariable String enrollmentId) {
        return ResponseEntity.ok(benefitDependentService.getDependentsByEnrollmentId(enrollmentId));
    }

    /**
     * Get paginated dependents for a benefit enrollment
     * Employees can only access their own enrollments' dependents, HR/Admin can access any
     */
    @GetMapping("/benefit-enrollments/{enrollmentId}/dependents/paginated")
    @Operation(summary = "Get paginated enrollment dependents", description = "Get paginated dependents for a benefit enrollment")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<BenefitDependentDTO>> getDependentsByEnrollmentIdPaginated(
            @Parameter(description = "Benefit enrollment ID") @PathVariable String enrollmentId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(benefitDependentService.getDependentsByEnrollmentId(enrollmentId, pageable));
    }

    /**
     * Get a dependent by ID
     * Employees can only access their own enrollments' dependents, HR/Admin can access any
     */
    @GetMapping("/benefit-dependents/{dependentId}")
    @Operation(summary = "Get dependent by ID", description = "Get a dependent by its ID")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitDependentDTO> getDependentById(
            @Parameter(description = "Benefit dependent ID") @PathVariable String dependentId) {
        return ResponseEntity.ok(benefitDependentService.getDependentById(dependentId));
    }

    /**
     * Add a dependent to a benefit enrollment
     * Employees can only add dependents to their own enrollments, HR/Admin can add to any
     */
    @PostMapping("/benefit-enrollments/{enrollmentId}/dependents")
    @Operation(summary = "Add dependent", description = "Add a dependent to a benefit enrollment")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitDependentDTO> addDependent(
            @Parameter(description = "Benefit enrollment ID") @PathVariable String enrollmentId,
            @Valid @RequestBody BenefitDependentDTO benefitDependentDTO) {
        return new ResponseEntity<>(
                benefitDependentService.addDependent(enrollmentId, benefitDependentDTO),
                HttpStatus.CREATED);
    }

    /**
     * Update a dependent
     * Employees can only update their own enrollments' dependents, HR/Admin can update any
     */
    @PutMapping("/benefit-dependents/{dependentId}")
    @Operation(summary = "Update dependent", description = "Update a dependent")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitDependentDTO> updateDependent(
            @Parameter(description = "Benefit dependent ID") @PathVariable String dependentId,
            @Valid @RequestBody BenefitDependentDTO benefitDependentDTO) {
        return ResponseEntity.ok(benefitDependentService.updateDependent(dependentId, benefitDependentDTO));
    }

    /**
     * Delete a dependent
     * Employees can only delete their own enrollments' dependents, HR/Admin can delete any
     */
    @DeleteMapping("/benefit-dependents/{dependentId}")
    @Operation(summary = "Delete dependent", description = "Delete a dependent")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteDependent(
            @Parameter(description = "Benefit dependent ID") @PathVariable String dependentId) {
        benefitDependentService.deleteDependent(dependentId);
        return ResponseEntity.noContent().build();
    }
} 