package cit.edu.workforce.Controller;

import cit.edu.workforce.Entity.DepartmentEntity;
import cit.edu.workforce.Service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/departments")
@Tag(name = "Department Management", description = "Department management APIs")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @Operation(summary = "Get all departments", description = "Get a list of all departments")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<DepartmentEntity>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID", description = "Get a department by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<DepartmentEntity> getDepartmentById(@PathVariable String id) {
        return departmentService.getDepartmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create department", description = "Create a new department")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<DepartmentEntity> createDepartment(@RequestParam String departmentName) {
        return new ResponseEntity<>(departmentService.createDepartment(departmentName), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update department", description = "Update an existing department")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<DepartmentEntity> updateDepartment(@PathVariable String id, @RequestParam String departmentName) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, departmentName));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete department", description = "Delete a department")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable String id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}