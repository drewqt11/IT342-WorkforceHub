package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.ImprovementPlanDTO;
import cit.edu.workforce.Service.ImprovementPlanService;
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
import java.util.Map;
import java.util.Optional;

/**
 * ImprovementPlanController - Provides API endpoints for performance improvement plan management
 * New file: Implements endpoints for creating, reading, updating, and deleting performance improvement plans
 * 
 * This controller handles all improvement plan-related operations including:
 * - Creating and managing employee performance improvement plans
 * - Viewing active plans for employees
 * - Updating plan status
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Performance Improvement Plans", description = "Performance improvement plan management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ImprovementPlanController {

    private final ImprovementPlanService improvementPlanService;

    @Autowired
    public ImprovementPlanController(ImprovementPlanService improvementPlanService) {
        this.improvementPlanService = improvementPlanService;
    }

    /**
     * Create a new improvement plan (HR/Admin only)
     * Allows HR to create a new improvement plan for an employee
     */
    @PostMapping("/hr/improvement-plans")
    @Operation(summary = "Create improvement plan", description = "Create a new performance improvement plan for an employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<ImprovementPlanDTO> createImprovementPlan(
            @Valid @RequestBody ImprovementPlanDTO improvementPlanDTO) {
        return new ResponseEntity<>(
                improvementPlanService.createImprovementPlan(improvementPlanDTO),
                HttpStatus.CREATED);
    }

    /**
     * Get a specific improvement plan by ID
     * Employees can only access their own plans, HR/Admin can access any
     */
    @GetMapping("/improvement-plans/{id}")
    @Operation(summary = "Get improvement plan by ID", description = "Get details of a specific improvement plan")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<ImprovementPlanDTO> getImprovementPlanById(@PathVariable String id) {
        Optional<ImprovementPlanDTO> plan = improvementPlanService.getImprovementPlanById(id);
        return plan.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update an existing improvement plan (HR/Admin only)
     */
    @PutMapping("/hr/improvement-plans/{id}")
    @Operation(summary = "Update improvement plan", description = "Update an existing performance improvement plan")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<ImprovementPlanDTO> updateImprovementPlan(
            @PathVariable String id,
            @Valid @RequestBody ImprovementPlanDTO improvementPlanDTO) {
        return ResponseEntity.ok(
                improvementPlanService.updateImprovementPlan(id, improvementPlanDTO));
    }

    /**
     * Update the status of an improvement plan (HR/Admin only)
     */
    @PatchMapping("/hr/improvement-plans/{id}/status")
    @Operation(summary = "Update improvement plan status", description = "Update the status of an improvement plan")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<ImprovementPlanDTO> updateImprovementPlanStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> statusUpdate) {
        
        String status = statusUpdate.get("status");
        if (status == null || status.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(
                improvementPlanService.updateImprovementPlanStatus(id, status));
    }

    /**
     * Delete an improvement plan (HR/Admin only)
     */
    @DeleteMapping("/hr/improvement-plans/{id}")
    @Operation(summary = "Delete improvement plan", description = "Delete a performance improvement plan")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteImprovementPlan(@PathVariable String id) {
        improvementPlanService.deleteImprovementPlan(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all improvement plans for the current employee
     */
    @GetMapping("/employee/improvement-plans")
    @Operation(summary = "Get my improvement plans", description = "Get all improvement plans for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<ImprovementPlanDTO>> getEmployeeImprovementPlans() {
        // For employees, this gets their own plans
        return ResponseEntity.ok(improvementPlanService.getEmployeeImprovementPlans(null));
    }

    /**
     * Get all active improvement plans for the current employee
     */
    @GetMapping("/employee/improvement-plans/active")
    @Operation(summary = "Get my active improvement plans", description = "Get all active improvement plans for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<ImprovementPlanDTO>> getEmployeeActiveImprovementPlans() {
        // For employees, this gets their own active plans
        return ResponseEntity.ok(improvementPlanService.getEmployeeActiveImprovementPlans(null));
    }

    /**
     * Get all improvement plans for a specific employee (HR/Admin only)
     */
    @GetMapping("/hr/employees/{employeeId}/improvement-plans")
    @Operation(summary = "Get employee improvement plans", description = "Get all improvement plans for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<ImprovementPlanDTO>> getEmployeeImprovementPlans(@PathVariable String employeeId) {
        return ResponseEntity.ok(improvementPlanService.getEmployeeImprovementPlans(employeeId));
    }

    /**
     * Get paginated improvement plans for a specific employee (HR/Admin only)
     */
    @GetMapping("/hr/employees/{employeeId}/improvement-plans/paged")
    @Operation(summary = "Get employee improvement plans (paged)", description = "Get paginated improvement plans for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<ImprovementPlanDTO>> getEmployeeImprovementPlansPaged(
            @PathVariable String employeeId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "startDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(improvementPlanService.getEmployeeImprovementPlans(employeeId, pageable));
    }

    /**
     * Get all active improvement plans for a specific employee (HR/Admin only)
     */
    @GetMapping("/hr/employees/{employeeId}/improvement-plans/active")
    @Operation(summary = "Get employee active improvement plans", description = "Get all active improvement plans for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<ImprovementPlanDTO>> getEmployeeActiveImprovementPlans(@PathVariable String employeeId) {
        return ResponseEntity.ok(improvementPlanService.getEmployeeActiveImprovementPlans(employeeId));
    }

    /**
     * Get all improvement plans with a specific status (HR/Admin only)
     */
    @GetMapping("/hr/improvement-plans")
    @Operation(summary = "Get improvement plans by status", description = "Get all improvement plans with a specific status")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<ImprovementPlanDTO>> getImprovementPlansByStatus(
            @Parameter(description = "Status filter (Open, Completed, Cancelled)") @RequestParam(required = false, defaultValue = "Open") String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "startDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(improvementPlanService.getImprovementPlansByStatus(status, pageable));
    }

    /**
     * Get all improvement plans expiring soon (HR/Admin only)
     */
    @GetMapping("/hr/improvement-plans/expiring")
    @Operation(summary = "Get improvement plans expiring soon", description = "Get improvement plans that will expire within a specified number of days")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<ImprovementPlanDTO>> getPlansExpiringSoon(
            @Parameter(description = "Days until expiration") @RequestParam(defaultValue = "7") int days) {
        
        return ResponseEntity.ok(improvementPlanService.getPlansExpiringSoon(days));
    }
} 