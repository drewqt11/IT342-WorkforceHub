package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.AuthResponseDTO;
import cit.edu.workforce.DTO.EmployeeRegistrationDTO;
import cit.edu.workforce.DTO.LoginRequestDTO;
import cit.edu.workforce.DTO.PasswordCreationDTO;
import cit.edu.workforce.DTO.TokenRefreshResponseDTO;
import cit.edu.workforce.Entity.EmailDomainListEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.RefreshTokenEntity;
import cit.edu.workforce.Entity.RoleEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.RoleRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import cit.edu.workforce.Security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthService(
            AuthenticationManager authenticationManager,
            UserAccountRepository userAccountRepository,
            EmployeeRepository employeeRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            EmailDomainListService emailDomainListService,
            UserDetailsService userDetailsService,
            RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailDomainListService = emailDomainListService;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        // Check if email domain is valid
        if (!emailDomainListService.isValidDomain(loginRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email domain. Please use a valid domain.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        UserAccountEntity userAccount = userAccountRepository.findByEmailAddress(loginRequest.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Update last login time
        userAccount.setLastLogin(LocalDateTime.now());
        userAccountRepository.save(userAccount);

        Optional<EmployeeEntity> employeeOptional = employeeRepository.findByUserAccount(userAccount);
        if (employeeOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee profile not found");
        }

        EmployeeEntity employee = employeeOptional.get();
        String roleName = employee.getRole() != null ? employee.getRole().getRoleName() : "Unknown";

        // Generate refresh token
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(userAccount.getUserId());

        return new AuthResponseDTO(
                jwt,
                refreshToken.getToken(),
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email domain. Only approved domains are allowed.");
        }

        // Check if email already exists
        if (userAccountRepository.existsByEmailAddress(registrationDTO.getEmail()) ||
            employeeRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Default employee role not found"));

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
        employee.setEmploymentStatus("PENDING"); // New employees start as pending until approved
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

        // Generate refresh token
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(userAccount.getUserId());

        return new AuthResponseDTO(
                jwt,
                refreshToken.getToken(),
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

    @Transactional
    public TokenRefreshResponseDTO refreshToken(String refreshToken) {
        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshTokenEntity::getUserAccount)
                .map(userAccount -> {
                    // Mark current refresh token as used
                    refreshTokenService.markTokenAsUsed(refreshToken);

                    // Create new refresh token
                    RefreshTokenEntity newRefreshToken = refreshTokenService.createRefreshToken(userAccount.getUserId());

                    // Generate new access token
                    String token = jwtTokenProvider.generateToken(userAccount.getEmailAddress());

                    return new TokenRefreshResponseDTO(token, newRefreshToken.getToken(), "Bearer");
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Invalid refresh token"));
    }

    @Transactional
    public void logout(String userId) {
        refreshTokenService.revokeAllByUser(userId);
    }

    @Transactional
    public AuthResponseDTO getOAuth2TokenInfo(String email) {
        // Validate email domain first
        if (!emailDomainListService.isValidDomain(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email domain. Only approved domains are allowed.");
        }

        // Find user account by email
        Optional<UserAccountEntity> userAccountOptional = userAccountRepository.findByEmailAddress(email);

        if (userAccountOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        UserAccountEntity userAccount = userAccountOptional.get();

        // Update last login time
        userAccount.setLastLogin(LocalDateTime.now());
        userAccountRepository.save(userAccount);

        // Find employee
        Optional<EmployeeEntity> employeeOptional = employeeRepository.findByUserAccount(userAccount);
        if (employeeOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee profile not found");
        }

        EmployeeEntity employee = employeeOptional.get();

        // Generate JWT token
        String token = jwtTokenProvider.generateTokenWithClaims(
            email,
            userAccount.getUserId(),
            email,
            employee.getRole() != null ? employee.getRole().getRoleName() : "ROLE_EMPLOYEE"
        );

        // Generate refresh token
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(userAccount.getUserId());

        String roleName = employee.getRole() != null ? employee.getRole().getRoleName() : "ROLE_EMPLOYEE";

        return new AuthResponseDTO(
                token,
                refreshToken.getToken(),
                userAccount.getUserId(),
                userAccount.getEmailAddress(),
                roleName,
                employee.getEmployeeId(),
                employee.getFirstName(),
                employee.getLastName()
        );
    }

    @Transactional
    public AuthResponseDTO createPassword(PasswordCreationDTO passwordCreationDTO) {
        // Validate email domain
        if (!emailDomainListService.isValidDomain(passwordCreationDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email domain. Only approved domains are allowed.");
        }

        // Find user account by email
        UserAccountEntity userAccount = userAccountRepository.findByEmailAddress(passwordCreationDTO.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Update the password
        userAccount.setPassword(passwordEncoder.encode(passwordCreationDTO.getPassword()));
        userAccountRepository.save(userAccount);

        // Find employee
        EmployeeEntity employee = employeeRepository.findByUserAccount(userAccount)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee profile not found"));

        String roleName = employee.getRole() != null ? employee.getRole().getRoleName() : "Unknown";

        // Generate JWT token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        passwordCreationDTO.getEmail(),
                        passwordCreationDTO.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Generate refresh token
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(userAccount.getUserId());

        return new AuthResponseDTO(
                jwt,
                refreshToken.getToken(),
                userAccount.getUserId(),
                userAccount.getEmailAddress(),
                roleName,
                employee.getEmployeeId(),
                employee.getFirstName(),
                employee.getLastName()
        );
    }
}