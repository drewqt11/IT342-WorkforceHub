package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.EmployeeDTO;
import cit.edu.workforce.Security.UserDetailsImpl;
import cit.edu.workforce.Service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees(
            @RequestParam(required = false) String status) {
        List<EmployeeDTO> employees = employeeService.getAllEmployees(status);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable UUID employeeId) {
        EmployeeDTO employee = employeeService.getEmployeeById(employeeId);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/profile")
    public ResponseEntity<EmployeeDTO> getCurrentEmployeeProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        EmployeeDTO employee = employeeService.getEmployeeByUserId(userDetails.getUserId());
        return ResponseEntity.ok(employee);
    }

    @PostMapping
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO createdEmployee = employeeService.createEmployee(employeeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable UUID employeeId,
            @Valid @RequestBody EmployeeDTO employeeDTO) {
        
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(employeeId, employeeDTO);
        return ResponseEntity.ok(updatedEmployee);
    }

    @PutMapping("/{employeeId}/role")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<EmployeeDTO> updateEmployeeRole(
            @PathVariable UUID employeeId,
            @RequestParam String roleId) {
        
        EmployeeDTO updatedEmployee = employeeService.updateEmployeeRole(employeeId, roleId);
        return ResponseEntity.ok(updatedEmployee);
    }

    @PutMapping("/{employeeId}/job-title")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<EmployeeDTO> updateEmployeeJobTitle(
            @PathVariable UUID employeeId,
            @RequestParam UUID jobId) {
        
        EmployeeDTO updatedEmployee = employeeService.updateEmployeeJobTitle(employeeId, jobId);
        return ResponseEntity.ok(updatedEmployee);
    }

    @PutMapping("/{employeeId}/department")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<EmployeeDTO> updateEmployeeDepartment(
            @PathVariable UUID employeeId,
            @RequestParam UUID departmentId) {
        
        EmployeeDTO updatedEmployee = employeeService.updateEmployeeDepartment(employeeId, departmentId);
        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<Void> deactivateEmployee(@PathVariable UUID employeeId) {
        employeeService.deactivateEmployee(employeeId);
        return ResponseEntity.noContent().build();
    }
} 