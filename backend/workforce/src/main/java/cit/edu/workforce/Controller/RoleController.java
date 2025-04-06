package cit.edu.workforce.Controller;

import cit.edu.workforce.Entity.RoleEntity;
import cit.edu.workforce.Service.RoleService;
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
@RequestMapping("/api/admin/roles")
@Tag(name = "Role Management", description = "Role management APIs")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @Operation(summary = "Get all roles", description = "Get a list of all roles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<RoleEntity>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID", description = "Get a role by its ID")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoleEntity> getRoleById(@PathVariable String id) {
        return roleService.getRoleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create role", description = "Create a new role")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoleEntity> createRole(
            @RequestParam String roleId,
            @RequestParam String roleName) {
        return new ResponseEntity<>(
                roleService.createRole(roleId, roleName),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update role", description = "Update an existing role")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoleEntity> updateRole(
            @PathVariable String id,
            @RequestParam String roleName) {
        return ResponseEntity.ok(roleService.updateRole(id, roleName));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role", description = "Delete a role")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteRole(@PathVariable String id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/initialize")
    @Operation(summary = "Initialize default roles", description = "Initialize default roles if they don't exist")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> initializeDefaultRoles() {
        roleService.initializeDefaultRoles();
        return ResponseEntity.ok().build();
    }
} 