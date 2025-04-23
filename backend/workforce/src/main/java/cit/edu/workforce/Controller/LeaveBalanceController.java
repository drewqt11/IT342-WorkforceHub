package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.LeaveBalanceDTO;
import cit.edu.workforce.Service.LeaveBalanceService;
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
@Tag(name = "Leave Balances", description = "APIs for managing employee leave balances")
@SecurityRequirement(name = "bearerAuth")
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    @Autowired
    public LeaveBalanceController(LeaveBalanceService leaveBalanceService) {
        this.leaveBalanceService = leaveBalanceService;
    }

    @GetMapping("/employee/leave-balances")
    @Operation(summary = "Get my leave balances", description = "Get leave balances for the currently logged-in employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<LeaveBalanceDTO>> getMyLeaveBalances() {
        try {
            return ResponseEntity.ok(leaveBalanceService.getCurrentEmployeeLeaveBalances());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/hr/employees/{employeeId}/leave-balances")
    @Operation(summary = "Get employee leave balances", description = "Get leave balances for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<LeaveBalanceDTO>> getEmployeeLeaveBalances(@PathVariable String employeeId) {
        try {
            return ResponseEntity.ok(leaveBalanceService.getEmployeeLeaveBalances(employeeId));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/hr/employees/{employeeId}/leave-balances/paged")
    @Operation(summary = "Get employee leave balances (paged)", description = "Get a paginated list of leave balances for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<LeaveBalanceDTO>> getEmployeeLeaveBalancesPaged(
            @PathVariable String employeeId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "leaveTypeName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        try {
            return ResponseEntity.ok(leaveBalanceService.getEmployeeLeaveBalancesPaged(employeeId, pageable));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/hr/leave-balances/{id}")
    @Operation(summary = "Get leave balance by ID", description = "Get a leave balance by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveBalanceDTO> getLeaveBalanceById(@PathVariable String id) {
        return leaveBalanceService.getLeaveBalanceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/hr/leave-balances")
    @Operation(summary = "Create leave balance", description = "Create a new leave balance for an employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveBalanceDTO> createLeaveBalance(@Valid @RequestBody LeaveBalanceDTO leaveBalanceDTO) {
        try {
            return new ResponseEntity<>(leaveBalanceService.createLeaveBalance(leaveBalanceDTO), HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/hr/leave-balances/{id}")
    @Operation(summary = "Update leave balance", description = "Update an existing leave balance")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveBalanceDTO> updateLeaveBalance(
            @PathVariable String id,
            @Valid @RequestBody LeaveBalanceDTO leaveBalanceDTO) {
        try {
            return ResponseEntity.ok(leaveBalanceService.updateLeaveBalance(id, leaveBalanceDTO));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/hr/employees/{employeeId}/assign-leave")
    @Operation(summary = "Assign leave to employee", description = "Assign a leave type to an employee for a specific year")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveBalanceDTO> assignLeaveToEmployee(
            @PathVariable String employeeId,
            @RequestParam String leaveTypeId,
            @RequestParam Integer year) {
        try {
            return new ResponseEntity<>(
                    leaveBalanceService.assignLeaveToEmployee(employeeId, leaveTypeId, year),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/hr/leave-balances/{id}/deactivate")
    @Operation(summary = "Deactivate leave balance", description = "Deactivate a leave balance")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveBalanceDTO> deactivateLeaveBalance(@PathVariable String id) {
        try {
            return ResponseEntity.ok(leaveBalanceService.deactivateLeaveBalance(id));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}

// New file: Controller for leave balances management
// Endpoints for creating, viewing, and managing employee leave balances 