package cit.edu.workforce.Security;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.RoleEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.RoleRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private RoleRepository roleRepository;

    private static final String FRONTEND_REDIRECT_URI = "http://localhost:5173/oauth2/redirect";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) 
            throws IOException, ServletException {
        
        String targetUrl = determineTargetUrl(request, response, authentication);
        
        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @Transactional
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        
        // Find the user account associated with this email
        Optional<UserAccountEntity> userAccountOptional = userAccountRepository.findByEmailAddress(email);
        UserAccountEntity userAccount;
        EmployeeEntity employee;
        
        if (userAccountOptional.isEmpty()) {
            logger.info("Auto-registering new user with email: " + email);
            
            // Auto-register the user if not found
            userAccount = new UserAccountEntity();
            userAccount.setEmailAddress(email);
            userAccount.setCreatedAt(LocalDateTime.now());
            userAccount.setLastLogin(LocalDateTime.now());
            userAccount.setActive(true);
            userAccount.setPassword(UUID.randomUUID().toString()); // Generate random password (won't be used)
            userAccountRepository.save(userAccount);
            
            // Get default employee role
            RoleEntity role = roleRepository.findById("ROLE_EMPLOYEE")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            
            // Create employee
            employee = new EmployeeEntity();
            employee.setFirstName((String) attributes.get("given_name") != null ? (String) attributes.get("given_name") : "");
            employee.setLastName((String) attributes.get("family_name") != null ? (String) attributes.get("family_name") : "");
            employee.setEmail(email);
            employee.setHireDate(LocalDate.now());
            employee.setStatus("ACTIVE");
            employee.setEmploymentStatus("FULL_TIME");
            employee.setRole(role);
            employee.setUserAccount(userAccount);
            employeeRepository.save(employee);
        } else {
            userAccount = userAccountOptional.get();
            
            // Find the employee associated with this user account
            Optional<EmployeeEntity> employeeOptional = employeeRepository.findByUserAccount(userAccount);
            if (employeeOptional.isEmpty()) {
                logger.info("User account exists but no employee record found. Creating employee record for user: " + userAccount.getUserId());
                
                // Get default employee role
                RoleEntity role = roleRepository.findById("ROLE_EMPLOYEE")
                        .orElseThrow(() -> new RuntimeException("Default role not found"));
                
                // Create employee for existing user account
                employee = new EmployeeEntity();
                employee.setFirstName((String) attributes.get("given_name") != null ? (String) attributes.get("given_name") : "");
                employee.setLastName((String) attributes.get("family_name") != null ? (String) attributes.get("family_name") : "");
                employee.setEmail(email);
                employee.setHireDate(LocalDate.now());
                employee.setStatus("ACTIVE");
                employee.setEmploymentStatus("FULL_TIME");
                employee.setRole(role);
                employee.setUserAccount(userAccount);
                employeeRepository.save(employee);
            } else {
                employee = employeeOptional.get();
            }
            
            // Update last login time
            userAccount.setLastLogin(LocalDateTime.now());
            userAccountRepository.save(userAccount);
        }
        
        // Generate JWT token
        String token = tokenProvider.generateToken(authentication);
        
        // Add employee details to redirect URL
        String roleName = employee.getRole() != null ? employee.getRole().getRoleName() : "Unknown";
        
        return UriComponentsBuilder.fromUriString(FRONTEND_REDIRECT_URI)
                .queryParam("token", token)
                .queryParam("userId", userAccount.getUserId())
                .queryParam("email", userAccount.getEmailAddress())
                .queryParam("role", roleName)
                .queryParam("employeeId", employee.getEmployeeId())
                .queryParam("firstName", employee.getFirstName())
                .queryParam("lastName", employee.getLastName())
                .build().toUriString();
    }
} 