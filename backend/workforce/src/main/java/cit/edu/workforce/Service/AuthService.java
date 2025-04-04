package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.AuthResponseDTO;
import cit.edu.workforce.DTO.EmployeeRegistrationDTO;
import cit.edu.workforce.DTO.LoginRequestDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.RoleEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.RoleRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import cit.edu.workforce.Security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailDomainListService emailDomainListService;
    private final UserDetailsService userDetailsService;

    @Autowired
    public AuthService(
            AuthenticationManager authenticationManager,
            UserAccountRepository userAccountRepository,
            EmployeeRepository employeeRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            EmailDomainListService emailDomainListService,
            UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailDomainListService = emailDomainListService;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        UserAccountEntity userAccount = userAccountRepository.findByEmailAddress(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update last login time
        userAccount.setLastLogin(LocalDateTime.now());
        userAccountRepository.save(userAccount);

        Optional<EmployeeEntity> employeeOptional = employeeRepository.findByUserAccount(userAccount);
        if (employeeOptional.isEmpty()) {
            throw new RuntimeException("Employee profile not found");
        }

        EmployeeEntity employee = employeeOptional.get();
        String roleName = employee.getRole() != null ? employee.getRole().getRoleName() : "Unknown";

        return new AuthResponseDTO(
                jwt,
                userAccount.getUserId(),
                userAccount.getEmailAddress(),
                roleName,
                employee.getEmployeeId(),
                employee.getFirstName(),
                employee.getLastName()
        );
    }

    @Transactional
    public AuthResponseDTO register(EmployeeRegistrationDTO registrationDTO) {
        // Validate email domain
        if (!emailDomainListService.isValidDomain(registrationDTO.getEmail())) {
            throw new RuntimeException("Invalid email domain");
        }

        // Check if email already exists
        if (userAccountRepository.existsByEmailAddress(registrationDTO.getEmail()) || 
            employeeRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create user account
        UserAccountEntity userAccount = new UserAccountEntity();
        userAccount.setEmailAddress(registrationDTO.getEmail());
        userAccount.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        userAccount.setCreatedAt(LocalDateTime.now());
        userAccount.setActive(true);
        userAccountRepository.save(userAccount);

        // Get default role (EMPLOYEE)
        RoleEntity role = roleRepository.findById("ROLE_EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        // Create employee
        EmployeeEntity employee = new EmployeeEntity();
        employee.setFirstName(registrationDTO.getFirstName());
        employee.setLastName(registrationDTO.getLastName());
        employee.setEmail(registrationDTO.getEmail());
        employee.setGender(registrationDTO.getGender());
        employee.setDateOfBirth(registrationDTO.getDateOfBirth());
        employee.setAddress(registrationDTO.getAddress());
        employee.setPhoneNumber(registrationDTO.getPhoneNumber());
        employee.setMaritalStatus(registrationDTO.getMaritalStatus());
        employee.setHireDate(LocalDate.now());
        employee.setStatus("ACTIVE");
        employee.setRole(role);
        employee.setUserAccount(userAccount);
        employeeRepository.save(employee);

        // Generate JWT token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registrationDTO.getEmail(),
                        registrationDTO.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        return new AuthResponseDTO(
                jwt,
                userAccount.getUserId(),
                userAccount.getEmailAddress(),
                role.getRoleName(),
                employee.getEmployeeId(),
                employee.getFirstName(),
                employee.getLastName()
        );
    }
    
    @Transactional
    public String generateTokenForEmail(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return jwtTokenProvider.generateToken(userDetails.getUsername());
    }
} 