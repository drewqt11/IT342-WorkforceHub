package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.PerformanceEvaluationDTO;
import cit.edu.workforce.Service.PerformanceEvaluationService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * PerformanceEvaluationController - Provides API endpoints for performance evaluation management
 * New file: Implements endpoints for creating, reading, updating, and deleting performance evaluations
 * 
 * This controller handles all performance evaluation-related operations including:
 * - Creating and managing employee performance evaluations
 * - Viewing evaluation history for employees
 * - Updating evaluation scores and remarks
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Performance Evaluations", description = "Performance evaluation management APIs")
@SecurityRequirement(name = "bearerAuth")
public class PerformanceEvaluationController {

    private final PerformanceEvaluationService performanceEvaluationService;

    @Autowired
    public PerformanceEvaluationController(PerformanceEvaluationService performanceEvaluationService) {
        this.performanceEvaluationService = performanceEvaluationService;
    }

    /**
     * Create a new performance evaluation (HR/Admin only)
     * Allows HR to create a new evaluation for an employee
     */
    @PostMapping("/hr/performance-evaluations")
    @Operation(summary = "Create performance evaluation", description = "Create a new performance evaluation for an employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<PerformanceEvaluationDTO> createPerformanceEvaluation(
            @Valid @RequestBody PerformanceEvaluationDTO performanceEvaluationDTO) {
        return new ResponseEntity<>(
                performanceEvaluationService.createPerformanceEvaluation(performanceEvaluationDTO),
                HttpStatus.CREATED);
    }

    /**
     * Get a specific performance evaluation by ID
     * Employees can only access their own evaluations, HR/Admin can access any
     */
    @GetMapping("/performance-evaluations/{id}")
    @Operation(summary = "Get performance evaluation by ID", description = "Get details of a specific performance evaluation")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<PerformanceEvaluationDTO> getPerformanceEvaluationById(@PathVariable String id) {
        Optional<PerformanceEvaluationDTO> evaluation = performanceEvaluationService.getPerformanceEvaluationById(id);
        return evaluation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update an existing performance evaluation (HR/Admin only)
     */
    @PutMapping("/hr/performance-evaluations/{id}")
    @Operation(summary = "Update performance evaluation", description = "Update an existing performance evaluation")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<PerformanceEvaluationDTO> updatePerformanceEvaluation(
            @PathVariable String id,
            @Valid @RequestBody PerformanceEvaluationDTO performanceEvaluationDTO) {
        return ResponseEntity.ok(
                performanceEvaluationService.updatePerformanceEvaluation(id, performanceEvaluationDTO));
    }

    /**
     * Delete a performance evaluation (HR/Admin only)
     */
    @DeleteMapping("/hr/performance-evaluations/{id}")
    @Operation(summary = "Delete performance evaluation", description = "Delete a performance evaluation")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deletePerformanceEvaluation(@PathVariable String id) {
        performanceEvaluationService.deletePerformanceEvaluation(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all performance evaluations for the current employee
     */
    @GetMapping("/employee/performance-evaluations")
    @Operation(summary = "Get my performance evaluations", description = "Get all performance evaluations for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<PerformanceEvaluationDTO>> getCurrentEmployeeEvaluations() {
        return ResponseEntity.ok(performanceEvaluationService.getCurrentEmployeeEvaluations());
    }

    /**
     * Get paginated performance evaluations for the current employee
     */
    @GetMapping("/employee/performance-evaluations/paged")
    @Operation(summary = "Get my performance evaluations (paged)", description = "Get paginated performance evaluations for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<PerformanceEvaluationDTO>> getCurrentEmployeeEvaluationsPaged(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "evaluationDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(performanceEvaluationService.getCurrentEmployeeEvaluations(pageable));
    }

    /**
     * Get the most recent performance evaluation for the current employee
     */
    @GetMapping("/employee/performance-evaluations/recent")
    @Operation(summary = "Get my most recent evaluation", description = "Get the most recent performance evaluation for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<PerformanceEvaluationDTO> getCurrentEmployeeMostRecentEvaluation() {
        return performanceEvaluationService.getMostRecentEvaluation(null)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all performance evaluations for a specific employee (HR/Admin only)
     */
    @GetMapping("/hr/employees/{employeeId}/performance-evaluations")
    @Operation(summary = "Get employee evaluations", description = "Get all performance evaluations for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<PerformanceEvaluationDTO>> getEmployeeEvaluations(@PathVariable String employeeId) {
        return ResponseEntity.ok(performanceEvaluationService.getEmployeeEvaluations(employeeId));
    }

    /**
     * Get paginated performance evaluations for a specific employee (HR/Admin only)
     */
    @GetMapping("/hr/employees/{employeeId}/performance-evaluations/paged")
    @Operation(summary = "Get employee evaluations (paged)", description = "Get paginated performance evaluations for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<PerformanceEvaluationDTO>> getEmployeeEvaluationsPaged(
            @PathVariable String employeeId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "evaluationDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(performanceEvaluationService.getEmployeeEvaluations(employeeId, pageable));
    }

    /**
     * Get the most recent performance evaluation for a specific employee (HR/Admin only)
     */
    @GetMapping("/hr/employees/{employeeId}/performance-evaluations/recent")
    @Operation(summary = "Get employee's most recent evaluation", description = "Get the most recent performance evaluation for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<PerformanceEvaluationDTO> getEmployeeMostRecentEvaluation(@PathVariable String employeeId) {
        return performanceEvaluationService.getMostRecentEvaluation(employeeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
} 