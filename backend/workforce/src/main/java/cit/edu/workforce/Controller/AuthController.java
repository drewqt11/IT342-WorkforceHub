package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.AuthResponseDTO;
import cit.edu.workforce.DTO.EmployeeRegistrationDTO;
import cit.edu.workforce.DTO.LoginRequestDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import cit.edu.workforce.Service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {

    private final AuthService authService;
    private final UserAccountRepository userAccountRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public AuthController(
            AuthService authService, 
            UserAccountRepository userAccountRepository,
            EmployeeRepository employeeRepository) {
        this.authService = authService;
        this.userAccountRepository = userAccountRepository;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        AuthResponseDTO authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Registers a new employee")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody EmployeeRegistrationDTO registrationDTO) {
        AuthResponseDTO authResponse = authService.register(registrationDTO);
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }
    
    @GetMapping("/oauth2/token-info/{email}")
    @Operation(summary = "Get token info", description = "Retrieves information about a user from email after OAuth2 login")
    public ResponseEntity<AuthResponseDTO> getOAuth2TokenInfo(@PathVariable String email) {
        // Find user account by email
        Optional<UserAccountEntity> userAccountOptional = userAccountRepository.findByEmailAddress(email);
        if (userAccountOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        UserAccountEntity userAccount = userAccountOptional.get();
        
        // Find employee
        Optional<EmployeeEntity> employeeOptional = employeeRepository.findByUserAccount(userAccount);
        if (employeeOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        EmployeeEntity employee = employeeOptional.get();
        
        // Generate JWT token
        String token = authService.generateTokenForEmail(email);
        
        String roleName = employee.getRole() != null ? employee.getRole().getRoleName() : "Unknown";
        
        AuthResponseDTO response = new AuthResponseDTO(
                token,
                userAccount.getUserId(),
                userAccount.getEmailAddress(),
                roleName,
                employee.getEmployeeId(),
                employee.getFirstName(),
                employee.getLastName()
        );
        
        return ResponseEntity.ok(response);
    }
} 