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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Value("${app.oauth2.redirect-uri:http://localhost:5173/oauth2/redirect}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @Transactional
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Get the registration ID (e.g., "microsoft", "google")
        String registrationId = null;
        if (authentication instanceof OAuth2AuthenticationToken) {
            registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            logger.info("OAuth2 authentication success with registration ID: {}", registrationId);
        }

        // Create OAuth2UserInfo with the registrationId to handle provider-specific
        // attributes
        OAuth2UserInfo userInfo = new OAuth2UserInfo(attributes, registrationId);
        String email = userInfo.getEmail();

        if (email == null || email.isEmpty()) {
            logger.error("Email not found in OAuth2 user attributes: {}", attributes);
            // Fallback for Microsoft-specific fields
            if ("microsoft".equals(registrationId)) {
                if (attributes.containsKey("userPrincipalName")) {
                    email = (String) attributes.get("userPrincipalName");
                } else if (attributes.containsKey("mail")) {
                    email = (String) attributes.get("mail");
                }
            }

            if (email == null || email.isEmpty()) {
                throw new RuntimeException("Email not found in OAuth2 user attributes");
            }
        }

        logger.info("Processing OAuth2 login for email: {}", email);

        // Find the user account associated with this email
        Optional<UserAccountEntity> userAccountOptional = userAccountRepository.findByEmailAddress(email);
        UserAccountEntity userAccount;
        EmployeeEntity employee;

        if (userAccountOptional.isEmpty()) {
            logger.info("Auto-registering new user with email: {}", email);

            // Auto-register the user if not found
            userAccount = new UserAccountEntity();
            userAccount.setEmailAddress(email);
            userAccount.setCreatedAt(LocalDateTime.now());
            userAccount.setLastLogin(LocalDateTime.now());
            userAccount.setActive(true);
            userAccount.setPassword(UUID.randomUUID().toString()); // Generate random password (won't be used)
            userAccountRepository.save(userAccount);

            // Get default role (EMPLOYEE)
            RoleEntity role = roleRepository.findById("ROLE_EMPLOYEE")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));

            // Create employee
            employee = new EmployeeEntity();
            employee.setFirstName(userInfo.getFirstName() != null ? userInfo.getFirstName() : "");
            employee.setIdNumber(userInfo.getIdNumber());
            employee.setLastName(userInfo.getLastName() != null ? userInfo.getLastName() : "");
            employee.setEmail(email);
            employee.setHireDate(LocalDate.now());
            employee.setStatus("ACTIVE");
            employee.setEmploymentStatus("PENDING");
            employee.setRole(role);
            employee.setUserAccount(userAccount);
            employeeRepository.save(employee);

            logger.info("Created new employee record with ID: {}", employee.getEmployeeId());
        } else {
            userAccount = userAccountOptional.get();

            // Find the employee associated with this user account
            Optional<EmployeeEntity> employeeOptional = employeeRepository.findByUserAccount(userAccount);
            if (employeeOptional.isEmpty()) {
                logger.info("User account exists but no employee record found. Creating employee record for user: {}",
                        userAccount.getUserId());

                // Get default employee role
                RoleEntity role = roleRepository.findById("ROLE_EMPLOYEE")
                        .orElseThrow(() -> new RuntimeException("Default role not found"));

                // Create employee for existing user account
                employee = new EmployeeEntity();
                employee.setFirstName(userInfo.getFirstName() != null ? userInfo.getFirstName() : "");
                employee.setIdNumber(userInfo.getIdNumber());
                employee.setLastName(userInfo.getLastName() != null ? userInfo.getLastName() : "");
                employee.setEmail(email);
                employee.setHireDate(LocalDate.now());
                employee.setStatus("ACTIVE");
                employee.setEmploymentStatus("PENDING");
                employee.setRole(role);
                employee.setUserAccount(userAccount);
                employeeRepository.save(employee);

                logger.info("Created new employee record with ID: {}", employee.getEmployeeId());
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

        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("token", token)
                .queryParam("userId", userAccount.getUserId())
                .queryParam("email", userAccount.getEmailAddress())
                .queryParam("role", roleName)
                .queryParam("employeeId", employee.getEmployeeId())
                .queryParam("firstName", employee.getFirstName())
                .queryParam("lastName", employee.getLastName())
                .build().toUriString();

        logger.info("Redirecting to: {}", targetUrl);
        return targetUrl;
    }
}