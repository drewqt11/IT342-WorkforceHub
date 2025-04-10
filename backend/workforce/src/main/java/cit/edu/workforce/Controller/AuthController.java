package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.AuthResponseDTO;
import cit.edu.workforce.DTO.EmployeeRegistrationDTO;
import cit.edu.workforce.DTO.LoginRequestDTO;
import cit.edu.workforce.DTO.PasswordCreationDTO;
import cit.edu.workforce.DTO.TokenRefreshRequestDTO;
import cit.edu.workforce.DTO.TokenRefreshResponseDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import cit.edu.workforce.Service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;


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

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh token", description = "Refresh an access token using a refresh token")
    public ResponseEntity<TokenRefreshResponseDTO> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO request) {
        TokenRefreshResponseDTO tokenRefreshResponse = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(tokenRefreshResponse);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Logout", description = "Logout a user by revoking their refresh tokens")
    public ResponseEntity<Void> logout(@RequestParam String userId) {
        authService.logout(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/oauth2/token-info/{email}")
    @Operation(summary = "Get token info", description = "Retrieves information about a user from email after OAuth2 login")
    public ResponseEntity<AuthResponseDTO> getOAuth2TokenInfo(@PathVariable String email) {
        AuthResponseDTO authResponse = authService.getOAuth2TokenInfo(email);
        return ResponseEntity.ok(authResponse);
    }
    
    @GetMapping("/oauth2/user-info")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get OAuth2 user info", description = "Get information about the authenticated OAuth2 user")
    public ResponseEntity<Map<String, Object>> getOAuth2UserInfo(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> userInfo = new LinkedHashMap<>();
        
        if (principal != null) {
            userInfo.put("name", principal.getAttribute("name"));
            userInfo.put("email", principal.getAttribute("email"));
            
            // Add Microsoft-specific attributes if they exist
            if (principal.getAttribute("userPrincipalName") != null) {
                userInfo.put("userPrincipalName", principal.getAttribute("userPrincipalName"));
            }
            if (principal.getAttribute("mail") != null) {
                userInfo.put("mail", principal.getAttribute("mail"));
            }
            if (principal.getAttribute("givenName") != null) {
                userInfo.put("givenName", principal.getAttribute("givenName"));
            }
            if (principal.getAttribute("surname") != null) {
                userInfo.put("surname", principal.getAttribute("surname"));
            }
            
            // Add authorities
            userInfo.put("authorities", principal.getAuthorities());
        }
        
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/dashboard/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Admin dashboard", description = "Admin dashboard endpoint")
    public ResponseEntity<String> adminDashboard(Principal principal) {
        return ResponseEntity.ok("Hello, Admin " + principal.getName());
    }

    @GetMapping("/dashboard/employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Employee dashboard", description = "Employee dashboard endpoint")
    public ResponseEntity<String> employeeDashboard(Principal principal) {
        return ResponseEntity.ok("Hello, " + principal.getName());
    }

    @PostMapping("/create-password")
    @Operation(summary = "Create password", description = "Creates a password for a user after OAuth2 authentication")
    public ResponseEntity<AuthResponseDTO> createPassword(@Valid @RequestBody PasswordCreationDTO passwordCreationDTO) {
        AuthResponseDTO authResponse = authService.createPassword(passwordCreationDTO);
        return ResponseEntity.ok(authResponse);
    }
}