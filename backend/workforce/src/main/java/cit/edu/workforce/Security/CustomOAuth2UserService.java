package cit.edu.workforce.Security;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.RoleEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.RoleRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import cit.edu.workforce.Service.EmailDomainListService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

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
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = new OAuth2UserInfo(oAuth2User.getAttributes());

        if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        // Validate email domain
        if (!emailDomainListService.isValidDomain(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationException("Email domain not allowed");
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

        // Return the OAuth2User with authorities
        return new DefaultOAuth2User(
                authorities,
                oAuth2UserInfo.getAttributes(),
                "email"
        );
    }

    @Transactional
    private UserAccountEntity registerNewUser(OAuth2UserInfo oAuth2UserInfo) {
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

        return userAccount;
    }

    @Transactional
    private UserAccountEntity updateExistingUser(UserAccountEntity userAccount, OAuth2UserInfo oAuth2UserInfo) {
        userAccount.setLastLogin(LocalDateTime.now());
        return userAccountRepository.save(userAccount);
    }
} 