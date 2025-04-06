package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.EmployeeDTO;
import cit.edu.workforce.Service.EmployeeService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Employee Management", description = "Employee management APIs")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/employee/profile")
    @Operation(summary = "Get employee profile", description = "Get the profile of the currently logged-in employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> getEmployeeProfile() {
        return employeeService.getCurrentEmployee()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{id}")
    @Operation(summary = "Get employee by ID", description = "Get an employee by their ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @employeeService.isCurrentEmployee(#id)")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable UUID id) {
        return employeeService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/hr/employees")
    @Operation(summary = "Get all employees", description = "Get a paginated list of all employees")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<EmployeeDTO>> getAllEmployees(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "lastName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Filter by name") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by employee ID") @RequestParam(required = false) String employeeId,
            @Parameter(description = "Filter by department") @RequestParam(required = false) String department,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status) {
        
        Sort sort = "desc".equalsIgnoreCase(direction) ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        if (name != null || employeeId != null || department != null || status != null) {
            return ResponseEntity.ok(employeeService.searchEmployees(name, employeeId, department, status, pageable));
        } else {
            return ResponseEntity.ok(employeeService.getAllEmployeesPaged(pageable));
        }
    }

    @GetMapping("/hr/employees/active")
    @Operation(summary = "Get all active employees", description = "Get a paginated list of all active employees")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<EmployeeDTO>> getAllActiveEmployees(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "lastName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {
        
        Sort sort = "desc".equalsIgnoreCase(direction) ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(employeeService.getAllActiveEmployeesPaged(pageable));
    }

    @PostMapping("/hr/employees")
    @Operation(summary = "Create employee", description = "Create a new employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        return new ResponseEntity<>(employeeService.createEmployee(employeeDTO), HttpStatus.CREATED);
    }

    @PutMapping("/hr/employees/{id}")
    @Operation(summary = "Update employee", description = "Update an existing employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable UUID id, @Valid @RequestBody EmployeeDTO employeeDTO) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employeeDTO));
    }
    
    @PatchMapping("/hr/employees/{id}")
    @Operation(summary = "Partially update employee", description = "Update specific fields of an existing employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> partialUpdateEmployee(@PathVariable UUID id, @RequestBody EmployeeDTO employeeDTO) {
        return ResponseEntity.ok(employeeService.updateEmployeePartially(id, employeeDTO));
    }
    
    @PatchMapping("/employee/profile")
    @Operation(summary = "Update own profile", description = "Allow employee to update specific fields of their own profile")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> updateOwnProfile(@RequestBody EmployeeDTO employeeDTO) {
        return employeeService.getCurrentEmployee()
                .map(currentEmployee -> {
                    // Only allow updating non-sensitive fields
                    EmployeeDTO limitedUpdate = new EmployeeDTO();
                    limitedUpdate.setAddress(employeeDTO.getAddress());
                    limitedUpdate.setPhoneNumber(employeeDTO.getPhoneNumber());
                    limitedUpdate.setMaritalStatus(employeeDTO.getMaritalStatus());
                    
                    return ResponseEntity.ok(employeeService.updateEmployeePartially(
                            currentEmployee.getEmployeeId(), limitedUpdate));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/hr/employees/{id}/deactivate")
    @Operation(summary = "Deactivate employee", description = "Deactivate an employee (soft delete)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> deactivateEmployee(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.deactivateEmployee(id));
    }
    
    @PatchMapping("/hr/employees/{id}/activate")
    @Operation(summary = "Activate employee", description = "Activate a previously deactivated employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> activateEmployee(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.activateEmployee(id));
    }

    @PatchMapping("/hr/employees/{id}/role")
    @Operation(summary = "Assign role to employee", description = "Assign a role to an employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> assignRoleToEmployee(@PathVariable UUID id, @RequestParam String roleId) {
        return ResponseEntity.ok(employeeService.assignRole(id, roleId));
    }

    @PatchMapping("/hr/employees/{id}/department")
    @Operation(summary = "Assign department to employee", description = "Assign a department to an employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> assignDepartmentToEmployee(@PathVariable UUID id, @RequestParam UUID departmentId) {
        return ResponseEntity.ok(employeeService.assignDepartment(id, departmentId));
    }

    @PatchMapping("/hr/employees/{id}/job")
    @Operation(summary = "Assign job title to employee", description = "Assign a job title to an employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> assignJobTitleToEmployee(@PathVariable UUID id, @RequestParam UUID jobId) {
        return ResponseEntity.ok(employeeService.assignJobTitle(id, jobId));
    }
} 