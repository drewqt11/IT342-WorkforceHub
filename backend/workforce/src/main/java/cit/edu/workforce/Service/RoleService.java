package cit.edu.workforce.Service;

import cit.edu.workforce.Entity.RoleEntity;
import cit.edu.workforce.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleEntity> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<RoleEntity> getRoleById(String roleId) {
        return roleRepository.findById(roleId);
    }

    @Transactional(readOnly = true)
    public Optional<RoleEntity> findById(String roleId) {
        return roleRepository.findById(roleId);
    }

    @Transactional(readOnly = true)
    public Optional<RoleEntity> getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    @Transactional
    public RoleEntity createRole(String roleId, String roleName) {
        if (roleRepository.existsById(roleId)) {
            throw new RuntimeException("Role ID already exists");
        }

        RoleEntity role = new RoleEntity();
        role.setRoleId(roleId);
        role.setRoleName(roleName);

        return roleRepository.save(role);
    }

    @Transactional
    public RoleEntity updateRole(String roleId, String roleName) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        role.setRoleName(roleName);
        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole(String roleId) {
        roleRepository.deleteById(roleId);
    }

    @Transactional
    public void initializeDefaultRoles() {
        // Create default roles if they don't exist
        if (!roleRepository.existsById("ROLE_ADMIN")) {
            createRole("ROLE_ADMIN", "System Administrator");
        }
        if (!roleRepository.existsById("ROLE_HR")) {
            createRole("ROLE_HR", "HR Administrator");
        }
        if (!roleRepository.existsById("ROLE_EMPLOYEE")) {
            createRole("ROLE_EMPLOYEE", "Employee");
        }
    }
}