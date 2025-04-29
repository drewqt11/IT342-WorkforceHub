package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.OvertimeRequestDTO;
import cit.edu.workforce.Service.OvertimeService;
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
 * OvertimeController - Controller for overtime request management
 * New file: Provides API endpoints for overtime request management
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Overtime Management", description = "Overtime request management APIs")
@SecurityRequirement(name = "bearerAuth")
public class OvertimeController {

    private final OvertimeService overtimeService;

    @Autowired
    public OvertimeController(OvertimeService overtimeService) {
        this.overtimeService = overtimeService;
    }

    @PostMapping("/overtime/request")
    @Operation(summary = "Create overtime request", description = "Create a new overtime request")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<OvertimeRequestDTO> createOvertimeRequest(@Valid @RequestBody OvertimeRequestDTO overtimeRequestDTO) {
        return new ResponseEntity<>(overtimeService.createOvertimeRequest(overtimeRequestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/overtime/my-requests")
    @Operation(summary = "Get my overtime requests", description = "Get all overtime requests for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<OvertimeRequestDTO>> getMyOvertimeRequests() {
        return ResponseEntity.ok(overtimeService.getCurrentEmployeeOvertimeRequests());
    }

    @GetMapping("/overtime/my-requests/paged")
    @Operation(summary = "Get my overtime requests (paged)", description = "Get paginated overtime requests for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<OvertimeRequestDTO>> getMyOvertimeRequestsPaged(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(overtimeService.getCurrentEmployeeOvertimeRequests(pageable));
    }

    @GetMapping("/overtime/my-requests/status/{status}")
    @Operation(summary = "Get my overtime requests by status", description = "Get overtime requests for the current employee with a specific status")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<OvertimeRequestDTO>> getMyOvertimeRequestsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(overtimeService.getCurrentEmployeeOvertimeRequestsByStatus(status));
    }

    @GetMapping("/overtime/request/{otRequestId}")
    @Operation(summary = "Get overtime request by ID", description = "Get an overtime request by its ID")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<OvertimeRequestDTO> getOvertimeRequestById(@PathVariable String otRequestId) {
        return overtimeService.getOvertimeRequestById(otRequestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/overtime/request/{otRequestId}/cancel")
    @Operation(summary = "Cancel overtime request", description = "Cancel a pending overtime request")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<OvertimeRequestDTO> cancelOvertimeRequest(@PathVariable String otRequestId) {
        return ResponseEntity.ok(overtimeService.cancelOvertimeRequest(otRequestId));
    }

    @PutMapping("/hr/overtime/request/{otRequestId}")
    @Operation(summary = "Update overtime request", description = "Update an existing overtime request (HR only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<OvertimeRequestDTO> updateOvertimeRequest(
            @PathVariable String otRequestId,
            @Valid @RequestBody OvertimeRequestDTO overtimeRequestDTO) {
        return ResponseEntity.ok(overtimeService.updateOvertimeRequest(otRequestId, overtimeRequestDTO));
    }

    @PatchMapping("/hr/overtime/request/{otRequestId}/review")
    @Operation(summary = "Review overtime request", description = "Approve or reject an overtime request (HR only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<OvertimeRequestDTO> reviewOvertimeRequest(
            @PathVariable String otRequestId,
            @RequestParam String status) {
        return ResponseEntity.ok(overtimeService.reviewOvertimeRequest(otRequestId, status));
    }

    @GetMapping("/hr/overtime/requests/status/{status}")
    @Operation(summary = "Get overtime requests by status", description = "Get all overtime requests with a specific status (HR only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<OvertimeRequestDTO>> getOvertimeRequestsByStatus(
            @PathVariable String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(overtimeService.getOvertimeRequestsByStatus(status, pageable));
    }

    @GetMapping("/hr/overtime/employee/{employeeId}/requests")
    @Operation(summary = "Get employee overtime requests", description = "Get overtime requests for a specific employee (HR only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<OvertimeRequestDTO>> getEmployeeOvertimeRequests(
            @PathVariable String employeeId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(overtimeService.getEmployeeOvertimeRequests(employeeId, pageable));
    }
} 