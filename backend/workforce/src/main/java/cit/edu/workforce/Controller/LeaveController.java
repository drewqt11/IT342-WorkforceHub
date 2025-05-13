package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.LeaveRequestDTO;
import cit.edu.workforce.Service.LeaveService;
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
import java.util.Map;

/**
 * LeaveController - Provides API endpoints for leave request management
 * New file: Provides API endpoints for leave request management
 * This controller handles all leave-related operations including:
 * - Creating and managing leave requests
 * - Approving or rejecting leave requests
 * - Searching leave records by various criteria
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Leave Management", description = "API for managing leave requests")
@SecurityRequirement(name = "bearerAuth")
public class LeaveController {

    private final LeaveService leaveService;

    @Autowired
    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    /**
     * Create a leave request for the current employee
     * Allows employees to submit requests for time off
     */
    @PostMapping("/employee/leave-requests")
    @Operation(summary = "Create leave request", description = "Submit a new leave request")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveRequestDTO> createLeaveRequest(@Valid @RequestBody LeaveRequestDTO leaveRequestDTO) {
        return new ResponseEntity<>(leaveService.createLeaveRequest(leaveRequestDTO), HttpStatus.CREATED);
    }

    /**
     * Get all leave requests for the current employee
     */
    @GetMapping("/employee/leave-requests")
    @Operation(summary = "Get my leave requests", description = "Get all leave requests for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<LeaveRequestDTO>> getMyLeaveRequests() {
        return ResponseEntity.ok(leaveService.getCurrentEmployeeLeaveRequests());
    }

    /**
     * Get paginated leave requests for the current employee
     */
    @GetMapping("/employee/leave-requests/paged")
    @Operation(summary = "Get my leave requests (paged)", description = "Get paginated leave requests for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<LeaveRequestDTO>> getMyLeaveRequestsPaged(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(leaveService.getCurrentEmployeeLeaveRequests(pageable));
    }

    /**
     * Get a specific leave request by ID
     */
    @GetMapping("/employee/leave-requests/{id}")
    @Operation(summary = "Get leave request by ID", description = "Get details of a specific leave request")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @leaveService.isOwnLeaveRequest(#id)")
    public ResponseEntity<LeaveRequestDTO> getLeaveRequestById(@PathVariable String id) {
        return leaveService.getLeaveRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all leave requests for HR/admin to review
     * Admin/HR only endpoint
     */
    @GetMapping("/hr/leave-requests")
    @Operation(summary = "Get all leave requests", description = "Get paginated leave requests with optional filters")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<LeaveRequestDTO>> getAllLeaveRequests(
            @Parameter(description = "Employee ID filter") @RequestParam(required = false) String employeeId,
            @Parameter(description = "Status filter (PENDING, APPROVED, REJECTED)") @RequestParam(required = false) String status,
            @Parameter(description = "Leave type filter") @RequestParam(required = false) String leaveType,
            @Parameter(description = "Start date (inclusive)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (status != null) {
            return ResponseEntity.ok(leaveService.getLeaveRequestsByStatus(status, pageable));
        } else {
            return ResponseEntity.ok(leaveService.getLeaveRequestsByStatus("", pageable));
        }
    }

    /**
     * Get all pending leave requests that need approval
     * Admin/HR only endpoint
     */
    @GetMapping("/hr/leave-requests/pending")
    @Operation(summary = "Get pending leave requests", description = "Get all pending leave requests that need approval")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<LeaveRequestDTO>> getPendingLeaveRequests(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(leaveService.getLeaveRequestsByStatus("PENDING", pageable));
    }

    /**
     * Get all leave requests for a specific employee
     * Admin/HR only endpoint
     */
    @GetMapping("/hr/leave-requests/employee/{employeeId}")
    @Operation(summary = "Get employee leave requests", description = "Get leave requests for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<LeaveRequestDTO>> getEmployeeLeaveRequests(
            @PathVariable String employeeId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(leaveService.getEmployeeLeaveRequests(employeeId, pageable));
    }

    /**
     * Approve a leave request
     * Admin/HR only endpoint
     */
    @PatchMapping("/hr/leave-requests/{id}/approve")
    @Operation(summary = "Approve leave request", description = "Approve a pending leave request")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveRequestDTO> approveLeaveRequest(@PathVariable String id) {
        return ResponseEntity.ok(leaveService.reviewLeaveRequest(id, "APPROVED"));
    }

    /**
     * Reject a leave request
     * Admin/HR only endpoint
     */
    @PatchMapping("/hr/leave-requests/{id}/reject")
    @Operation(summary = "Reject leave request", description = "Reject a pending leave request")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveRequestDTO> rejectLeaveRequest(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> requestBody) {
        
        String reason = requestBody != null ? requestBody.get("reason") : null;
        return ResponseEntity.ok(leaveService.reviewLeaveRequest(id, "REJECTED"));
    }

    /**
     * Cancel a pending leave request
     * Employee can only cancel their own requests
     */
    @PatchMapping("/employee/leave-requests/{id}/cancel")
    @Operation(summary = "Cancel leave request", description = "Cancel a pending leave request")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN') and @leaveService.isOwnLeaveRequest(#id)")
    public ResponseEntity<LeaveRequestDTO> cancelLeaveRequest(@PathVariable String id) {
        return ResponseEntity.ok(leaveService.cancelLeaveRequest(id));
    }
} 