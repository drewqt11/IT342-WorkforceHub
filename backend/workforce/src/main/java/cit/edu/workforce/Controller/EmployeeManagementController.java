package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.EmployeeDTO;
import cit.edu.workforce.Entity.DepartmentEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.JobTitleEntity;
import cit.edu.workforce.Entity.RoleEntity;
import cit.edu.workforce.Service.DepartmentService;
import cit.edu.workforce.Service.EmployeeService;
import cit.edu.workforce.Service.JobTitleService;
import cit.edu.workforce.Service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/hr")
@Tag(name = "Employee Management", description = "APIs for managing employee roles, job titles, and departments")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeManagementController {

    private final EmployeeService employeeService;
    private final RoleService roleService;
    private final JobTitleService jobTitleService;
    private final DepartmentService departmentService;

    @Autowired
    public EmployeeManagementController(
            EmployeeService employeeService,
            RoleService roleService,
            JobTitleService jobTitleService,
            DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.roleService = roleService;
        this.jobTitleService = jobTitleService;
        this.departmentService = departmentService;
    }

    /**
     * Update an employee's role
     * 
     * @param employeeId The ID of the employee to update
     * @param requestBody A map containing the new role ID
     * @return The updated employee DTO
     */
    @PatchMapping("/employees/{employeeId}/role")
    @Operation(summary = "Update employee role", description = "Update an employee's role (HR Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> updateEmployeeRole(
            @PathVariable String employeeId,
            @Valid @RequestBody Map<String, String> requestBody) {
        
        // Validate request body
        if (!requestBody.containsKey("roleId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role ID is required");
        }
        
        String roleId = requestBody.get("roleId");
        
        // Validate role exists
        Optional<RoleEntity> roleOptional = roleService.findById(roleId);
        if (roleOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with ID: " + roleId);
        }
        
        // Update employee role
        EmployeeDTO updatedEmployee = employeeService.updateEmployeeRole(employeeId, roleOptional.get());
        return ResponseEntity.ok(updatedEmployee);
    }

    /**
     * Update an employee's job title
     * 
     * @param employeeId The ID of the employee to update
     * @param requestBody A map containing the new job title ID
     * @return The updated employee DTO
     */
    @PatchMapping("/employees/{employeeId}/job-title")
    @Operation(summary = "Update employee job title", description = "Update an employee's job title (HR Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> updateEmployeeJobTitle(
            @PathVariable String employeeId,
            @Valid @RequestBody Map<String, String> requestBody) {
        
        // Validate request body
        if (!requestBody.containsKey("jobId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job ID is required");
        }
        
        String jobId = requestBody.get("jobId");
        
        // Validate job title exists
        Optional<JobTitleEntity> jobTitleOptional = jobTitleService.findById(jobId);
        if (jobTitleOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job title not found with ID: " + jobId);
        }
        
        // Update employee job title
        EmployeeDTO updatedEmployee = employeeService.updateEmployeeJobTitle(employeeId, jobTitleOptional.get());
        return ResponseEntity.ok(updatedEmployee);
    }

    /**
     * Update an employee's department
     * 
     * @param employeeId The ID of the employee to update
     * @param requestBody A map containing the new department ID
     * @return The updated employee DTO
     */
    @PatchMapping("/employees/{employeeId}/department")
    @Operation(summary = "Update employee department", description = "Update an employee's department (HR Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> updateEmployeeDepartment(
            @PathVariable String employeeId,
            @Valid @RequestBody Map<String, String> requestBody) {
        
        // Validate request body
        if (!requestBody.containsKey("departmentId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department ID is required");
        }
        
        String departmentId = requestBody.get("departmentId");
        
        // Validate department exists
        Optional<DepartmentEntity> departmentOptional = departmentService.findById(departmentId);
        if (departmentOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found with ID: " + departmentId);
        }
        
        // Update employee department
        EmployeeDTO updatedEmployee = employeeService.updateEmployeeDepartment(employeeId, departmentOptional.get());
        return ResponseEntity.ok(updatedEmployee);
    }
}
