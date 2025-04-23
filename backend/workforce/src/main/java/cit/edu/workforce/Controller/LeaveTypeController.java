package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.LeaveTypeDTO;
import cit.edu.workforce.Service.LeaveTypeService;
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
@Tag(name = "Leave Types", description = "APIs for managing leave types")
@SecurityRequirement(name = "bearerAuth")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    @Autowired
    public LeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    @GetMapping("/leave-types")
    @Operation(summary = "Get all active leave types", description = "Get a list of all active leave types")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<LeaveTypeDTO>> getAllActiveLeaveTypes() {
        return ResponseEntity.ok(leaveTypeService.getAllActiveLeaveTypes());
    }

    @GetMapping("/hr/leave-types")
    @Operation(summary = "Get all leave types (paged)", description = "Get a paginated list of all leave types")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<LeaveTypeDTO>> getAllLeaveTypes(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Filter by name") @RequestParam(required = false) String name) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (name != null && !name.isEmpty()) {
            return ResponseEntity.ok(leaveTypeService.searchLeaveTypes(name, pageable));
        } else {
            return ResponseEntity.ok(leaveTypeService.getAllLeaveTypesPaged(pageable));
        }
    }

    @GetMapping("/hr/leave-types/{id}")
    @Operation(summary = "Get leave type by ID", description = "Get a leave type by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveTypeDTO> getLeaveTypeById(@PathVariable String id) {
        return leaveTypeService.getLeaveTypeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/hr/leave-types")
    @Operation(summary = "Create leave type", description = "Create a new leave type")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveTypeDTO> createLeaveType(@Valid @RequestBody LeaveTypeDTO leaveTypeDTO) {
        try {
            return new ResponseEntity<>(leaveTypeService.createLeaveType(leaveTypeDTO), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/hr/leave-types/{id}")
    @Operation(summary = "Update leave type", description = "Update an existing leave type")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveTypeDTO> updateLeaveType(
            @PathVariable String id,
            @Valid @RequestBody LeaveTypeDTO leaveTypeDTO) {
        try {
            return ResponseEntity.ok(leaveTypeService.updateLeaveType(id, leaveTypeDTO));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/hr/leave-types/{id}/deactivate")
    @Operation(summary = "Deactivate leave type", description = "Deactivate a leave type")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveTypeDTO> deactivateLeaveType(@PathVariable String id) {
        try {
            return ResponseEntity.ok(leaveTypeService.deactivateLeaveType(id));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/hr/leave-types/{id}/activate")
    @Operation(summary = "Activate leave type", description = "Activate a leave type")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<LeaveTypeDTO> activateLeaveType(@PathVariable String id) {
        try {
            return ResponseEntity.ok(leaveTypeService.activateLeaveType(id));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/hr/leave-types/{id}")
    @Operation(summary = "Delete leave type", description = "Delete a leave type")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteLeaveType(@PathVariable String id) {
        try {
            leaveTypeService.deleteLeaveType(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}

// New file: Controller for leave types management
// Endpoints for create, read, update, delete, and search leave types 