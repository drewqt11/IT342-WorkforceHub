package cit.edu.workforce.Controller;

import cit.edu.workforce.Entity.Role;
import cit.edu.workforce.Repository.RoleRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/system/roles")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class RoleManagementController {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleManagementController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<Role> getRoleById(@PathVariable String roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        return ResponseEntity.ok(role);
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@Valid @RequestBody Role role) {
        // Check if role already exists
        if (roleRepository.existsById(role.getRoleId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role ID already exists");
        }
        
        Role savedRole = roleRepository.save(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRole);
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<Role> updateRole(
            @PathVariable String roleId,
            @Valid @RequestBody Role roleDetails) {
        
        // Check if role exists
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        
        // Update role name
        role.setRoleName(roleDetails.getRoleName());
        
        Role updatedRole = roleRepository.save(role);
        return ResponseEntity.ok(updatedRole);
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable String roleId) {
        // Checks if the role exists
        if (!roleRepository.existsById(roleId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");
        }
        
        // In a real application, you would check if the role is assigned to any employees
        // and handle accordingly (e.g., prevent deletion or reassign employees)
        
        roleRepository.deleteById(roleId);
        return ResponseEntity.noContent().build();
    }
} 