package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.AuthRequest;
import cit.edu.workforce.DTO.AuthResponse;
import cit.edu.workforce.DTO.RegisterRequest;
import cit.edu.workforce.Entity.Employee;
import cit.edu.workforce.Entity.Role;
import cit.edu.workforce.Entity.UserAccount;
import cit.edu.workforce.Repository.EmailDomainListRepository;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.RoleRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import cit.edu.workforce.Security.JwtTokenProvider;
import cit.edu.workforce.Security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final EmailDomainListRepository emailDomainListRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager,
                      UserAccountRepository userAccountRepository,
                      EmployeeRepository employeeRepository,
                      RoleRepository roleRepository,
                      EmailDomainListRepository emailDomainListRepository,
                      PasswordEncoder passwordEncoder,
                      JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.emailDomainListRepository = emailDomainListRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse login(AuthRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Update last login time
        UserAccount userAccount = userAccountRepository.findByEmailAddress(loginRequest.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        userAccount.setLastLogin(LocalDateTime.now());
        userAccountRepository.save(userAccount);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtTokenProvider.generateToken(authentication);
        
        return new AuthResponse(jwt, userDetails.getUserId(), userDetails.getUsername(), userDetails.getRole());
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if email already exists
        if (userAccountRepository.existsByEmailAddress(registerRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already taken");
        }
        
        // Check if email domain is in the allowed list
        String emailDomain = registerRequest.getEmail().substring(registerRequest.getEmail().indexOf('@') + 1);
        if (!emailDomainListRepository.existsByDomainNameAndIsActiveTrue(emailDomain)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email domain is not allowed");
        }
        
        // Create user account
        UserAccount userAccount = new UserAccount();
        userAccount.setEmailAddress(registerRequest.getEmail());
        userAccount.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userAccount.setIsActive(true);
        userAccount.setCreatedAt(LocalDateTime.now());
        userAccountRepository.save(userAccount);
        
        // Get the default employee role (or create a pending role if not found)
        Role employeeRole = roleRepository.findById("EMPLOYEE")
                .orElseGet(() -> {
                    Role pendingRole = new Role("PENDING", "Pending");
                    return roleRepository.save(pendingRole);
                });
        
        // Create employee record
        Employee employee = new Employee();
        employee.setFirstName(registerRequest.getFirstName());
        employee.setLastName(registerRequest.getLastName());
        employee.setEmail(registerRequest.getEmail());
        employee.setGender(registerRequest.getGender());
        employee.setPhone(registerRequest.getPhone());
        employee.setAddress(registerRequest.getAddress());
        employee.setHireDate(LocalDate.now());
        employee.setStatus("ACTIVE");
        employee.setUserAccount(userAccount);
        employee.setRole(employeeRole);
        employeeRepository.save(employee);
        
        // Generate token for automatic login after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(),
                        registerRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return new AuthResponse(jwt, userDetails.getUserId(), userDetails.getUsername(), userDetails.getRole());
    }
} 