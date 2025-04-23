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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Benefit Plans", description = "APIs for managing employee benefit plans")
@SecurityRequirement(name = "bearerAuth")
public class BenefitPlanController {

    private final BenefitPlanService benefitPlanService;

    @Autowired
    public BenefitPlanController(BenefitPlanService benefitPlanService) {
        this.benefitPlanService = benefitPlanService;
    }

    @GetMapping("/employee/benefit-plans")
    @Operation(summary = "Get all active benefit plans", description = "Get a list of all active benefit plans available to employees")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<BenefitPlanDTO>> getAllActiveBenefitPlans() {
        return ResponseEntity.ok(benefitPlanService.getAllActiveBenefitPlans());
    }

    @GetMapping("/hr/benefit-plans")
    @Operation(summary = "Get all benefit plans", description = "Get a paginated list of all benefit plans")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<BenefitPlanDTO>> getAllBenefitPlans(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "planName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(benefitPlanService.getAllBenefitPlansPaged(pageable));
    }

    @GetMapping("/hr/benefit-plans/search")
    @Operation(summary = "Search benefit plans", description = "Search for benefit plans by name")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<BenefitPlanDTO>> searchBenefitPlans(
            @Parameter(description = "Plan name query") @RequestParam String planName,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "planName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(benefitPlanService.searchBenefitPlans(planName, pageable));
    }

    @GetMapping("/hr/benefit-plans/type/{planType}")
    @Operation(summary = "Get benefit plans by type", description = "Get benefit plans filtered by plan type")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<BenefitPlanDTO>> getBenefitPlansByType(
            @PathVariable String planType,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "planName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(benefitPlanService.getBenefitPlansByTypePaged(planType, pageable));
    }

    @GetMapping("/hr/benefit-plans/{id}")
    @Operation(summary = "Get benefit plan by ID", description = "Get a benefit plan by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitPlanDTO> getBenefitPlanById(@PathVariable("id") String benefitPlanId) {
        return benefitPlanService.getBenefitPlanById(benefitPlanId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/hr/benefit-plans")
    @Operation(summary = "Create benefit plan", description = "Create a new benefit plan")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitPlanDTO> createBenefitPlan(@Valid @RequestBody BenefitPlanDTO benefitPlanDTO) {
        try {
            BenefitPlanDTO createdBenefitPlan = benefitPlanService.createBenefitPlan(benefitPlanDTO);
            return new ResponseEntity<>(createdBenefitPlan, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating benefit plan", e);
        }
    }

    @PutMapping("/hr/benefit-plans/{id}")
    @Operation(summary = "Update benefit plan", description = "Update an existing benefit plan")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitPlanDTO> updateBenefitPlan(
            @PathVariable("id") String benefitPlanId,
            @Valid @RequestBody BenefitPlanDTO benefitPlanDTO) {
        try {
            BenefitPlanDTO updatedBenefitPlan = benefitPlanService.updateBenefitPlan(benefitPlanId, benefitPlanDTO);
            return ResponseEntity.ok(updatedBenefitPlan);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating benefit plan", e);
        }
    }

    @PutMapping("/hr/benefit-plans/{id}/activate")
    @Operation(summary = "Activate benefit plan", description = "Activate a benefit plan")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitPlanDTO> activateBenefitPlan(@PathVariable("id") String benefitPlanId) {
        try {
            BenefitPlanDTO activatedBenefitPlan = benefitPlanService.activateBenefitPlan(benefitPlanId);
            return ResponseEntity.ok(activatedBenefitPlan);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error activating benefit plan", e);
        }
    }

    @PutMapping("/hr/benefit-plans/{id}/deactivate")
    @Operation(summary = "Deactivate benefit plan", description = "Deactivate a benefit plan")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitPlanDTO> deactivateBenefitPlan(@PathVariable("id") String benefitPlanId) {
        try {
            BenefitPlanDTO deactivatedBenefitPlan = benefitPlanService.deactivateBenefitPlan(benefitPlanId);
            return ResponseEntity.ok(deactivatedBenefitPlan);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deactivating benefit plan", e);
        }
    }

    @DeleteMapping("/admin/benefit-plans/{id}")
    @Operation(summary = "Delete benefit plan", description = "Delete a benefit plan (Admin only)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteBenefitPlan(@PathVariable("id") String benefitPlanId) {
        try {
            benefitPlanService.deleteBenefitPlan(benefitPlanId);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting benefit plan", e);
        }
    }
}

// New file: Controller for benefit plans in the Benefits Administration module
// Provides endpoints for creating, viewing, and managing employee benefit plans 