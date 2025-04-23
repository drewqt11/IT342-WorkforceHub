package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.OvertimeRequestDTO;
import cit.edu.workforce.Enum.OvertimeRequestStatus;
import cit.edu.workforce.Service.EmployeeService;
import cit.edu.workforce.Service.OvertimeRequestService;
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

@RestController
@RequestMapping("/api")
@Tag(name = "Overtime Requests", description = "APIs for managing employee overtime requests")
@SecurityRequirement(name = "bearerAuth")
public class OvertimeRequestController {

    private final OvertimeRequestService overtimeRequestService;
    private final EmployeeService employeeService;

    @Autowired
    public OvertimeRequestController(OvertimeRequestService overtimeRequestService, EmployeeService employeeService) {
        this.overtimeRequestService = overtimeRequestService;
        this.employeeService = employeeService;
    }

    @GetMapping("/employee/overtime-requests")
    @Operation(summary = "Get my overtime requests", description = "Get overtime requests for the currently logged-in employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<OvertimeRequestDTO>> getMyOvertimeRequests(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        try {
            String employeeId = employeeService.getCurrentEmployee()
                    .map(employee -> employee.getEmployeeId())
                    .orElseThrow(() -> new IllegalStateException("No authenticated employee found"));
                    
            return ResponseEntity.ok(overtimeRequestService.getEmployeeOvertimeRequests(employeeId, pageable));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/hr/employees/{employeeId}/overtime-requests")
    @Operation(summary = "Get employee overtime requests", description = "Get overtime requests for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<OvertimeRequestDTO>> getEmployeeOvertimeRequests(
            @PathVariable String employeeId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        try {
            return ResponseEntity.ok(overtimeRequestService.getEmployeeOvertimeRequests(employeeId, pageable));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/hr/overtime-requests/status/{status}")
    @Operation(summary = "Get overtime requests by status", description = "Get overtime requests filtered by status")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<OvertimeRequestDTO>> getOvertimeRequestsByStatus(
            @PathVariable OvertimeRequestStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(overtimeRequestService.getOvertimeRequestsByStatus(status, pageable));
    }

    @GetMapping("/hr/overtime-requests/department/{departmentId}/pending")
    @Operation(summary = "Get pending overtime requests by department", description = "Get pending overtime requests for a specific department")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<OvertimeRequestDTO>> getPendingOvertimeRequestsByDepartment(
            @PathVariable String departmentId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(overtimeRequestService.getPendingOvertimeRequestsByDepartment(departmentId, pageable));
    }

    @GetMapping("/hr/overtime-requests/{id}")
    @Operation(summary = "Get overtime request by ID", description = "Get an overtime request by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<OvertimeRequestDTO> getOvertimeRequestById(@PathVariable String id) {
        return overtimeRequestService.getOvertimeRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/employee/overtime-requests")
    @Operation(summary = "Create overtime request", description = "Create a new overtime request for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<OvertimeRequestDTO> createOvertimeRequest(@Valid @RequestBody OvertimeRequestDTO overtimeRequestDTO) {
        try {
            return new ResponseEntity<>(overtimeRequestService.createOvertimeRequest(overtimeRequestDTO), HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/hr/overtime-requests")
    @Operation(summary = "Create overtime request on behalf of employee", description = "Create a new overtime request for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<OvertimeRequestDTO> createOvertimeRequestForEmployee(@Valid @RequestBody OvertimeRequestDTO overtimeRequestDTO) {
        if (overtimeRequestDTO.getEmployeeId() == null || overtimeRequestDTO.getEmployeeId().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee ID is required");
        }
        
        try {
            return new ResponseEntity<>(overtimeRequestService.createOvertimeRequest(overtimeRequestDTO), HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/hr/overtime-requests/{id}/approve")
    @Operation(summary = "Approve overtime request", description = "Approve a pending overtime request")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<OvertimeRequestDTO> approveOvertimeRequest(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> request) {
        
        try {
            String comments = request != null ? request.get("comments") : null;
            String approverId = employeeService.getCurrentEmployee()
                    .map(employee -> employee.getEmployeeId())
                    .orElseThrow(() -> new IllegalStateException("No authenticated employee found"));
            
            return ResponseEntity.ok(overtimeRequestService.approveOvertimeRequest(id, approverId, comments));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/hr/overtime-requests/{id}/reject")
    @Operation(summary = "Reject overtime request", description = "Reject a pending overtime request")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<OvertimeRequestDTO> rejectOvertimeRequest(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> request) {
        
        try {
            String comments = request != null ? request.get("comments") : null;
            String approverId = employeeService.getCurrentEmployee()
                    .map(employee -> employee.getEmployeeId())
                    .orElseThrow(() -> new IllegalStateException("No authenticated employee found"));
            
            return ResponseEntity.ok(overtimeRequestService.rejectOvertimeRequest(id, approverId, comments));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/employee/overtime-requests/{id}/cancel")
    @Operation(summary = "Cancel overtime request", description = "Cancel your own pending overtime request")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<OvertimeRequestDTO> cancelOvertimeRequest(@PathVariable String id) {
        try {
            return ResponseEntity.ok(overtimeRequestService.cancelOvertimeRequest(id));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/hr/employees/{employeeId}/overtime-requests/date-range")
    @Operation(summary = "Get approved overtime requests for date range", 
               description = "Get approved overtime requests for a specific employee within a date range")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<OvertimeRequestDTO>> getApprovedOvertimeRequestsForDateRange(
            @PathVariable String employeeId,
            @Parameter(description = "Start date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            return ResponseEntity.ok(overtimeRequestService.getApprovedOvertimeRequestsForDateRange(
                    employeeId, startDate, endDate));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}

// New file: Controller for overtime requests management
// Endpoints for submitting, approving, rejecting, and managing overtime requests 