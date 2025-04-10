package cit.edu.workforce.Security;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.RoleEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.RoleRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import cit.edu.workforce.Service.EmailDomainListService;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmailDomainListService emailDomainListService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        try {
            // Get registration ID (microsoft, google, etc.)
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            logger.info("Processing OAuth2 user with registration ID: {}", registrationId);

            // Log attributes for debugging
            Map<String, Object> attributes = oAuth2User.getAttributes();
            logger.debug("OAuth2 user attributes: {}", attributes);

            return processOAuth2User(userRequest, oAuth2User, registrationId);
        } catch (Exception ex) {
            logger.error("Error processing OAuth2 user", ex);
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User,
            String registrationId) {
        OAuth2UserInfo oAuth2UserInfo = new OAuth2UserInfo(oAuth2User.getAttributes(), registrationId);

        if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
            logger.error("Email not found from OAuth2 provider");
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        logger.info("Processing user with email: {}", oAuth2UserInfo.getEmail());

        // Validate email domain
        if (!emailDomainListService.isValidDomain(oAuth2UserInfo.getEmail())) {
            logger.error("Email domain not allowed: {}", oAuth2UserInfo.getEmail());
            throw new OAuth2AuthenticationException("Email domain not allowed: " + oAuth2UserInfo.getEmail());
        }

        UserAccountEntity userAccount = userAccountRepository.findByEmailAddress(oAuth2UserInfo.getEmail())
                .orElse(null);

        if (userAccount == null) {
            userAccount = registerNewUser(oAuth2UserInfo);
        } else {
            userAccount = updateExistingUser(userAccount, oAuth2UserInfo);
        }

        // Load roles and authorities
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        Optional<EmployeeEntity> employeeOptional = employeeRepository.findByUserAccount(userAccount);
        if (employeeOptional.isPresent()) {
            EmployeeEntity employee = employeeOptional.get();
            if (employee.getRole() != null) {
                authorities.add(new SimpleGrantedAuthority(employee.getRole().getRoleId()));
            }
        }

        // Determine name attribute based on the OAuth2 provider
        String userNameAttributeName = "email";
        if ("microsoft".equals(registrationId)) {
            // For Microsoft, the key might be different
            if (oAuth2User.getAttributes().containsKey("userPrincipalName")) {
                userNameAttributeName = "userPrincipalName";
            } else if (oAuth2User.getAttributes().containsKey("mail")) {
                userNameAttributeName = "mail";
            }
        }

        // Return the OAuth2User with authorities
        return new DefaultOAuth2User(
                authorities,
                oAuth2User.getAttributes(),
                userNameAttributeName);
    }

    @Transactional
    private UserAccountEntity registerNewUser(OAuth2UserInfo oAuth2UserInfo) {
        logger.info("Registering new user with email: {}", oAuth2UserInfo.getEmail());

        // Create new user account
        UserAccountEntity userAccount = new UserAccountEntity();
        userAccount.setEmailAddress(oAuth2UserInfo.getEmail());
        userAccount.setCreatedAt(LocalDateTime.now());
        userAccount.setLastLogin(LocalDateTime.now());
        userAccount.setActive(true);
        userAccount.setPassword(UUID.randomUUID().toString()); // Generate random password (won't be used)
        userAccountRepository.save(userAccount);

        // Get default employee role
        RoleEntity role = roleRepository.findById("ROLE_EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        // Create employee
        EmployeeEntity employee = new EmployeeEntity();
        employee.setFirstName(oAuth2UserInfo.getFirstName() != null ? oAuth2UserInfo.getFirstName() : "");
        employee.setLastName(oAuth2UserInfo.getLastName() != null ? oAuth2UserInfo.getLastName() : "");
        employee.setEmail(oAuth2UserInfo.getEmail());
        employee.setHireDate(LocalDate.now());
        employee.setStatus("ACTIVE");
        employee.setRole(role);
        employee.setUserAccount(userAccount);
        employeeRepository.save(employee);

        logger.info("Successfully registered new user with ID: {}", userAccount.getUserId());

        return userAccount;
    }

    @Transactional
    private UserAccountEntity updateExistingUser(UserAccountEntity userAccount, OAuth2UserInfo oAuth2UserInfo) {
        logger.info("Updating existing user: {}", userAccount.getUserId());
        userAccount.setLastLogin(LocalDateTime.now());
        return userAccountRepository.save(userAccount);
    }
}
