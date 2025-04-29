package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.BenefitPlanDTO;
import cit.edu.workforce.Service.BenefitPlanService;
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
 * BenefitPlanController - Provides API endpoints for benefit plan management
 * New file: Implements endpoints for creating, reading, updating, and deactivating benefit plans.
 * 
 * This controller handles all benefit plan-related operations including:
 * - Creating new benefit plans (HR/Admin only)
 * - Listing active benefit plans for employees to view
 * - Updating and deactivating existing plans (HR/Admin only)
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Benefit Plans", description = "Benefit plan management APIs")
@SecurityRequirement(name = "bearerAuth")
public class BenefitPlanController {

    private final BenefitPlanService benefitPlanService;

    @Autowired
    public BenefitPlanController(BenefitPlanService benefitPlanService) {
        this.benefitPlanService = benefitPlanService;
    }

    /**
     * Get all active benefit plans
     * Endpoint accessible to all authenticated users
     */
    @GetMapping("/benefit-plans")
    @Operation(summary = "Get all active benefit plans", description = "Get a list of all active benefit plans")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<BenefitPlanDTO>> getAllActiveBenefitPlans() {
        return ResponseEntity.ok(benefitPlanService.getAllActiveBenefitPlans());
    }

    /**
     * Get paginated active benefit plans
     * Endpoint accessible to all authenticated users
     */
    @GetMapping("/benefit-plans/paginated")
    @Operation(summary = "Get paginated active benefit plans", description = "Get a paginated list of active benefit plans")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<BenefitPlanDTO>> getPaginatedActiveBenefitPlans(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "planName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(benefitPlanService.getAllActiveBenefitPlans(pageable));
    }

    /**
     * Get a benefit plan by ID
     * Endpoint accessible to all authenticated users
     */
    @GetMapping("/benefit-plans/{planId}")
    @Operation(summary = "Get benefit plan by ID", description = "Get a benefit plan by its ID")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitPlanDTO> getBenefitPlanById(
            @Parameter(description = "Benefit plan ID") @PathVariable String planId) {
        return ResponseEntity.ok(benefitPlanService.getBenefitPlanById(planId));
    }

    /**
     * Get benefit plans by type
     * Endpoint accessible to all authenticated users
     */
    @GetMapping("/benefit-plans/type/{planType}")
    @Operation(summary = "Get benefit plans by type", description = "Get a list of benefit plans by plan type")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<BenefitPlanDTO>> getBenefitPlansByType(
            @Parameter(description = "Plan type (e.g., Health, Dental, Life)") @PathVariable String planType) {
        return ResponseEntity.ok(benefitPlanService.getBenefitPlansByType(planType));
    }

    /**
     * Create a new benefit plan
     * HR/Admin only endpoint
     */
    @PostMapping("/hr/benefit-plans")
    @Operation(summary = "Create benefit plan", description = "Create a new benefit plan (HR/Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitPlanDTO> createBenefitPlan(@Valid @RequestBody BenefitPlanDTO benefitPlanDTO) {
        return new ResponseEntity<>(benefitPlanService.createBenefitPlan(benefitPlanDTO), HttpStatus.CREATED);
    }

    /**
     * Update an existing benefit plan
     * HR/Admin only endpoint
     */
    @PutMapping("/hr/benefit-plans/{planId}")
    @Operation(summary = "Update benefit plan", description = "Update an existing benefit plan (HR/Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitPlanDTO> updateBenefitPlan(
            @Parameter(description = "Benefit plan ID") @PathVariable String planId,
            @Valid @RequestBody BenefitPlanDTO benefitPlanDTO) {
        return ResponseEntity.ok(benefitPlanService.updateBenefitPlan(planId, benefitPlanDTO));
    }

    /**
     * Deactivate a benefit plan
     * HR/Admin only endpoint
     */
    @PatchMapping("/hr/benefit-plans/{planId}/deactivate")
    @Operation(summary = "Deactivate benefit plan", description = "Deactivate an existing benefit plan (HR/Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitPlanDTO> deactivateBenefitPlan(
            @Parameter(description = "Benefit plan ID") @PathVariable String planId) {
        return ResponseEntity.ok(benefitPlanService.deactivateBenefitPlan(planId));
    }

    /**
     * Activate a benefit plan
     * HR/Admin only endpoint
     */
    @PatchMapping("/hr/benefit-plans/{planId}/activate")
    @Operation(summary = "Activate benefit plan", description = "Activate an existing benefit plan (HR/Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitPlanDTO> activateBenefitPlan(
            @Parameter(description = "Benefit plan ID") @PathVariable String planId) {
        return ResponseEntity.ok(benefitPlanService.activateBenefitPlan(planId));
    }
} 