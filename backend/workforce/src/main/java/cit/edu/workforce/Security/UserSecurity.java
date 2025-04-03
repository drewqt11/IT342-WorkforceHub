package cit.edu.workforce.Security;

import cit.edu.workforce.Entity.Employee;
import cit.edu.workforce.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("userSecurity")
public class UserSecurity {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public UserSecurity(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public boolean isCurrentUser(UUID userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getUserId().equals(userId);
        }
        
        return false;
    }
    
    public boolean isCurrentUserOrAdmin(UUID employeeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // Check if user is an admin
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_HR_ADMIN") || role.equals("ROLE_SYSTEM_ADMIN"));
            
            if (isAdmin) {
                return true;
            }
            
            // Check if this is the current user's own employee record
            Employee employee = employeeRepository.findByUserAccountUserId(userDetails.getUserId()).orElse(null);
            return employee != null && employee.getEmployeeId().equals(employeeId);
        }
        
        return false;
    }
    
    public boolean isEmployee(UUID employeeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // Check if user is an admin
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_HR_ADMIN") || role.equals("ROLE_SYSTEM_ADMIN"));
            
            if (isAdmin) {
                return true;
            }
            
            // Check if this is the current user's own employee record
            Employee employee = employeeRepository.findByUserAccountUserId(userDetails.getUserId()).orElse(null);
            return employee != null && employee.getEmployeeId().equals(employeeId);
        }
        
        return false;
    }
} 