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

    @PostMapping("/initialize")
    @Operation(summary = "Initialize default roles", description = "Initialize default roles if they don't exist")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> initializeDefaultRoles() {
        roleService.initializeDefaultRoles();
        return ResponseEntity.ok().build();
    }
} 