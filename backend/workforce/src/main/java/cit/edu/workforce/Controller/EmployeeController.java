package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.EmployeeDTO;
import cit.edu.workforce.Service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Operation(summary = "Get all employees", description = "Get a list of all employees")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/hr/employees/active")
    @Operation(summary = "Get all active employees", description = "Get a list of all active employees")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<EmployeeDTO>> getAllActiveEmployees() {
        return ResponseEntity.ok(employeeService.getAllActiveEmployees());
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

    @DeleteMapping("/hr/employees/{id}")
    @Operation(summary = "Deactivate employee", description = "Deactivate an employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> deactivateEmployee(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.deactivateEmployee(id));
    }

    @PutMapping("/admin/employees/{id}/role")
    @Operation(summary = "Assign role to employee", description = "Assign a role to an employee")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> assignRoleToEmployee(@PathVariable UUID id, @RequestParam String roleId) {
        return ResponseEntity.ok(employeeService.assignRole(id, roleId));
    }

    @PutMapping("/hr/employees/{id}/department")
    @Operation(summary = "Assign department to employee", description = "Assign a department to an employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> assignDepartmentToEmployee(@PathVariable UUID id, @RequestParam UUID departmentId) {
        return ResponseEntity.ok(employeeService.assignDepartment(id, departmentId));
    }

    @PutMapping("/hr/employees/{id}/job")
    @Operation(summary = "Assign job title to employee", description = "Assign a job title to an employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> assignJobTitleToEmployee(@PathVariable UUID id, @RequestParam UUID jobId) {
        return ResponseEntity.ok(employeeService.assignJobTitle(id, jobId));
    }
} 