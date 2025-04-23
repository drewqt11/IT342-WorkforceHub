package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.LeaveRequestDTO;
import cit.edu.workforce.Enum.LeaveRequestStatus;
import cit.edu.workforce.Service.EmployeeService;
import cit.edu.workforce.Service.LeaveRequestService;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Tag(name = "Leave Requests", description = "APIs for managing employee leave requests")
@SecurityRequirement(name = "bearerAuth")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final EmployeeService employeeService;

    @Autowired
    public LeaveRequestController(LeaveRequestService leaveRequestService, EmployeeService employeeService) {
        this.leaveRequestService = leaveRequestService;
        this.employeeService = employeeService;
    }

    @GetMapping("/employee/leave-requests")
    @Operation(summary = "Get my leave requests", description = "Get leave requests for the currently logged-in employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<LeaveRequestDTO>> getMyLeaveRequests(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        try {
            return ResponseEntity.ok(leaveRequestService.getCurrentEmployeeLeaveRequests(pageable));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/hr/employees/{employeeId}/leave-requests")
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

        try {
            return ResponseEntity.ok(leaveRequestService.getEmployeeLeaveRequests(employeeId, pageable));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/hr/leave-requests/status/{status}")
    @Operation(summary = "Get leave requests by status", description = "Get leave requests filtered by status")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<LeaveRequestDTO>> getLeaveRequestsByStatus(
            @PathVariable LeaveRequestStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(leaveRequestService.getLeaveRequestsByStatus(status, pageable));
    }

    @GetMapping("/hr/leave-requests/department/{departmentId}/pending")
    @Operation(summary = "Get pending leave requests by department", description = "Get pending leave requests for a specific department")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<LeaveRequestDTO>> getPendingLeaveRequestsByDepartment(
            @PathVariable String departmentId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(leaveRequestService.getPendingLeaveRequestsByDepartment(departmentId, pageable));
    }

    @GetMapping("/hr/leave-requests/{id}")
    @Operation(summary = "Get leave request by ID", description = "Get a leave request by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveRequestDTO> getLeaveRequestById(@PathVariable String id) {
        Optional<LeaveRequestDTO> leaveRequest = leaveRequestService.getLeaveRequestById(id);
        return leaveRequest
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found with id: " + id));
    }

    @PostMapping("/employee/leave-requests")
    @Operation(summary = "Create leave request", description = "Create a new leave request for the currently logged-in employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveRequestDTO> createLeaveRequest(@Valid @RequestBody LeaveRequestDTO leaveRequestDTO) {
        try {
            LeaveRequestDTO createdRequest = leaveRequestService.createLeaveRequest(leaveRequestDTO);
            return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating leave request: " + e.getMessage(), e);
        }
    }

    @PostMapping("/hr/leave-requests")
    @Operation(summary = "Create leave request on behalf of employee", description = "Create a new leave request for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveRequestDTO> createLeaveRequestForEmployee(@Valid @RequestBody LeaveRequestDTO leaveRequestDTO) {
        if (leaveRequestDTO.getEmployeeId() == null || leaveRequestDTO.getEmployeeId().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee ID is required");
        }
        
        try {
            return new ResponseEntity<>(leaveRequestService.createLeaveRequest(leaveRequestDTO), HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/hr/leave-requests/{id}/approve")
    @Operation(summary = "Approve leave request", description = "Approve a pending leave request")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveRequestDTO> approveLeaveRequest(
            @PathVariable String id,
            @Parameter(description = "Comments") @RequestParam(required = false) String comments) {
        try {
            // Extract approver ID from authentication context or another source
            // For now, we're assuming a method in the service can handle this
            LeaveRequestDTO approvedRequest = leaveRequestService.approveLeaveRequest(id, null, comments);
            return ResponseEntity.ok(approvedRequest);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error approving leave request: " + e.getMessage(), e);
        }
    }

    @PutMapping("/hr/leave-requests/{id}/reject")
    @Operation(summary = "Reject leave request", description = "Reject a pending leave request")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveRequestDTO> rejectLeaveRequest(
            @PathVariable String id,
            @Parameter(description = "Comments") @RequestParam(required = false) String comments) {
        try {
            // Extract approver ID from authentication context or another source
            // For now, we're assuming a method in the service can handle this
            LeaveRequestDTO rejectedRequest = leaveRequestService.rejectLeaveRequest(id, null, comments);
            return ResponseEntity.ok(rejectedRequest);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error rejecting leave request: " + e.getMessage(), e);
        }
    }

    @PutMapping("/employee/leave-requests/{id}/cancel")
    @Operation(summary = "Cancel leave request", description = "Cancel a pending leave request (only allowed for your own requests)")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveRequestDTO> cancelLeaveRequest(@PathVariable String id) {
        try {
            LeaveRequestDTO canceledRequest = leaveRequestService.cancelLeaveRequest(id);
            return ResponseEntity.ok(canceledRequest);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error canceling leave request: " + e.getMessage(), e);
        }
    }

    @GetMapping("/hr/employees/{employeeId}/leave-requests/date-range")
    @Operation(summary = "Get approved leave requests for date range", 
               description = "Get approved leave requests for a specific employee within a date range")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<LeaveRequestDTO>> getApprovedLeaveRequestsForDateRange(
            @PathVariable String employeeId,
            @Parameter(description = "Start date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            return ResponseEntity.ok(leaveRequestService.getApprovedLeaveRequestsForDateRange(
                    employeeId, startDate, endDate));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}

// New file: Controller for leave requests management
// Endpoints for submitting, approving, rejecting, and managing leave requests 