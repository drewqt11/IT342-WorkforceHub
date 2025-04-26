package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.BenefitEnrollmentDTO;
import cit.edu.workforce.Service.BenefitEnrollmentService;
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

/**
 * BenefitEnrollmentController - Provides API endpoints for benefit enrollment management
 * New file: Implements endpoints for creating, reading, and managing benefit enrollments.
 * 
 * This controller handles all benefit enrollment-related operations including:
 * - Enrolling employees in benefit plans
 * - Viewing enrollment details
 * - Cancelling enrollments
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Benefit Enrollments", description = "Benefit enrollment management APIs")
@SecurityRequirement(name = "bearerAuth")
public class BenefitEnrollmentController {

    private final BenefitEnrollmentService benefitEnrollmentService;

    @Autowired
    public BenefitEnrollmentController(BenefitEnrollmentService benefitEnrollmentService) {
        this.benefitEnrollmentService = benefitEnrollmentService;
    }

    /**
     * Get all benefit enrollments for the current employee
     * Endpoint accessible to all authenticated users
     */
    @GetMapping("/employee/benefit-enrollments")
    @Operation(summary = "Get my benefit enrollments", description = "Get all benefit enrollments for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<BenefitEnrollmentDTO>> getCurrentEmployeeEnrollments() {
        return ResponseEntity.ok(benefitEnrollmentService.getCurrentEmployeeEnrollments());
    }

    /**
     * Get paginated benefit enrollments for the current employee
     * Endpoint accessible to all authenticated users
     */
    @GetMapping("/employee/benefit-enrollments/paginated")
    @Operation(summary = "Get my paginated benefit enrollments", description = "Get paginated benefit enrollments for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<BenefitEnrollmentDTO>> getCurrentEmployeeEnrollmentsPaginated(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "enrollmentDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(benefitEnrollmentService.getCurrentEmployeeEnrollments(pageable));
    }

    /**
     * Get a specific benefit enrollment by ID
     * Employees can only access their own enrollments, HR/Admin can access any
     */
    @GetMapping("/benefit-enrollments/{enrollmentId}")
    @Operation(summary = "Get benefit enrollment by ID", description = "Get a benefit enrollment by its ID")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitEnrollmentDTO> getEnrollmentById(
            @Parameter(description = "Benefit enrollment ID") @PathVariable String enrollmentId) {
        return ResponseEntity.ok(benefitEnrollmentService.getEnrollmentById(enrollmentId));
    }

    /**
     * Enroll the current employee in a benefit plan
     * Endpoint accessible to all authenticated users
     */
    @PostMapping("/employee/benefit-enrollments")
    @Operation(summary = "Enroll in benefit plan", description = "Enroll the current employee in a benefit plan")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitEnrollmentDTO> enrollInBenefitPlan(
            @Parameter(description = "Benefit plan ID") @RequestParam String planId) {
        return new ResponseEntity<>(benefitEnrollmentService.enrollInBenefitPlan(planId), HttpStatus.CREATED);
    }

    /**
     * Cancel a benefit enrollment
     * Employees can only cancel their own enrollments, HR/Admin can cancel any
     */
    @PatchMapping("/benefit-enrollments/{enrollmentId}/cancel")
    @Operation(summary = "Cancel benefit enrollment", description = "Cancel a benefit enrollment")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitEnrollmentDTO> cancelEnrollment(
            @Parameter(description = "Benefit enrollment ID") @PathVariable String enrollmentId,
            @Parameter(description = "Cancellation reason") @RequestBody(required = false) Map<String, String> requestBody) {
        
        String cancellationReason = (requestBody != null) ? requestBody.get("cancellationReason") : null;
        return ResponseEntity.ok(benefitEnrollmentService.cancelEnrollment(enrollmentId, cancellationReason));
    }

    /**
     * Get all benefit enrollments for a specific employee (HR/Admin only)
     */
    @GetMapping("/hr/employees/{employeeId}/benefit-enrollments")
    @Operation(summary = "Get employee's benefit enrollments", description = "Get all benefit enrollments for a specific employee (HR/Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<BenefitEnrollmentDTO>> getEmployeeEnrollments(
            @Parameter(description = "Employee ID") @PathVariable String employeeId) {
        return ResponseEntity.ok(benefitEnrollmentService.getEmployeeEnrollments(employeeId));
    }

    /**
     * Get paginated benefit enrollments for a specific employee (HR/Admin only)
     */
    @GetMapping("/hr/employees/{employeeId}/benefit-enrollments/paginated")
    @Operation(summary = "Get employee's paginated benefit enrollments", description = "Get paginated benefit enrollments for a specific employee (HR/Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<BenefitEnrollmentDTO>> getEmployeeEnrollmentsPaginated(
            @Parameter(description = "Employee ID") @PathVariable String employeeId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "enrollmentDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(benefitEnrollmentService.getEmployeeEnrollments(employeeId, pageable));
    }

    /**
     * Get all enrollments for a specific benefit plan (HR/Admin only)
     */
    @GetMapping("/hr/benefit-plans/{planId}/enrollments")
    @Operation(summary = "Get plan enrollments", description = "Get all enrollments for a specific benefit plan (HR/Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<BenefitEnrollmentDTO>> getPlanEnrollments(
            @Parameter(description = "Benefit plan ID") @PathVariable String planId) {
        return ResponseEntity.ok(benefitEnrollmentService.getPlanEnrollments(planId));
    }

    /**
     * Get paginated enrollments for a specific benefit plan (HR/Admin only)
     */
    @GetMapping("/hr/benefit-plans/{planId}/enrollments/paginated")
    @Operation(summary = "Get paginated plan enrollments", description = "Get paginated enrollments for a specific benefit plan (HR/Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<BenefitEnrollmentDTO>> getPlanEnrollmentsPaginated(
            @Parameter(description = "Benefit plan ID") @PathVariable String planId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "enrollmentDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(benefitEnrollmentService.getPlanEnrollments(planId, pageable));
    }

    /**
     * Enroll a specific employee in a benefit plan (HR/Admin only)
     */
    @PostMapping("/hr/employees/{employeeId}/benefit-enrollments")
    @Operation(summary = "Enroll employee in benefit plan", description = "Enroll a specific employee in a benefit plan (HR/Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<BenefitEnrollmentDTO> enrollEmployeeInPlan(
            @Parameter(description = "Employee ID") @PathVariable String employeeId,
            @Parameter(description = "Benefit plan ID") @RequestParam String planId) {
        return new ResponseEntity<>(benefitEnrollmentService.enrollEmployeeInPlan(employeeId, planId), HttpStatus.CREATED);
    }
} 