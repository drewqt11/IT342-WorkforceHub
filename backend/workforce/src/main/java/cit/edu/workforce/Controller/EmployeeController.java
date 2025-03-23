package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.ApiResponseDTO;
import cit.edu.workforce.DTO.EmployeeDTO;
import cit.edu.workforce.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;
    
    @GetMapping
    @PreAuthorize("hasRole('HR_STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllEmployees() {
        try {
            List<EmployeeDTO> employees = employeeService.getAllEmployees();
            return ResponseEntity.ok(ApiResponseDTO.success(employees));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HR_STAFF') or hasRole('ADMIN') or @securityService.isEmployeeOwner(authentication, #id)")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        try {
            EmployeeDTO employee = employeeService.getEmployeeById(id);
            return ResponseEntity.ok(ApiResponseDTO.success(employee));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('HR_STAFF') or hasRole('ADMIN') or @securityService.isUserOwner(authentication, #userId)")
    public ResponseEntity<?> getEmployeeByUserId(@PathVariable Long userId) {
        try {
            EmployeeDTO employee = employeeService.getEmployeeByUserId(userId);
            return ResponseEntity.ok(ApiResponseDTO.success(employee));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('HR_STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> createEmployee(@RequestBody EmployeeDTO employeeDTO) {
        try {
            Long userId = employeeDTO.getUserId();
            if (userId == null) {
                return ResponseEntity.badRequest().body(ApiResponseDTO.error("User ID is required"));
            }
            EmployeeDTO createdEmployee = employeeService.createEmployee(employeeDTO, userId);
            return ResponseEntity.ok(ApiResponseDTO.success("Employee created successfully", createdEmployee));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR_STAFF') or hasRole('ADMIN') or @securityService.isEmployeeOwner(authentication, #id)")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @RequestBody EmployeeDTO employeeDTO) {
        try {
            EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, employeeDTO);
            return ResponseEntity.ok(ApiResponseDTO.success("Employee updated successfully", updatedEmployee));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR_STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok(ApiResponseDTO.success("Employee deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
} 