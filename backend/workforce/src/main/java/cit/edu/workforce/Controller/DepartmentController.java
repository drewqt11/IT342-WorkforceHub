package cit.edu.workforce.Controller;

import cit.edu.workforce.Entity.Department;
import cit.edu.workforce.Repository.DepartmentRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
@PreAuthorize("hasRole('HR_ADMIN')")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    @Autowired
    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{departmentId}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable UUID departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        return ResponseEntity.ok(department);
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody Department department) {
        // Check if department name already exists
        if (departmentRepository.existsByDepartmentName(department.getDepartmentName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department name already exists");
        }
        
        Department savedDepartment = departmentRepository.save(department);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDepartment);
    }

    @PutMapping("/{departmentId}")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable UUID departmentId,
            @Valid @RequestBody Department departmentDetails) {
        
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        
        // Check if new name already exists for another department
        if (!department.getDepartmentName().equals(departmentDetails.getDepartmentName()) && 
                departmentRepository.existsByDepartmentName(departmentDetails.getDepartmentName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department name already exists");
        }
        
        department.setDepartmentName(departmentDetails.getDepartmentName());
        
        Department updatedDepartment = departmentRepository.save(department);
        return ResponseEntity.ok(updatedDepartment);
    }

    @DeleteMapping("/{departmentId}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        
        // In a real application, we might want to check if there are employees in this department
        // and handle that accordingly (e.g., prevent deletion or move employees to a default department)
        
        departmentRepository.delete(department);
        return ResponseEntity.noContent().build();
    }
} 