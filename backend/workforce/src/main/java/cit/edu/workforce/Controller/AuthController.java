package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.ApiResponseDTO;
import cit.edu.workforce.DTO.JwtResponseDTO;
import cit.edu.workforce.DTO.LoginDTO;
import cit.edu.workforce.Entity.UserEntity;
import cit.edu.workforce.Security.JwtUtils;
import cit.edu.workforce.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            Optional<UserEntity> userOptional = userService.findByUsername(loginDTO.getUsername());
            
            if (userOptional.isPresent()) {
                UserEntity user = userOptional.get();
                String jwt = jwtUtils.generateJwtToken(user);
                
                JwtResponseDTO jwtResponse = new JwtResponseDTO(
                        jwt,
                        user.getId(),
                        user.getUsername(),
                        user.getRoles()
                );
                
                return ResponseEntity.ok(ApiResponseDTO.success(jwtResponse));
            }
            
            return ResponseEntity.badRequest().body(ApiResponseDTO.error("User not found"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error("Invalid username or password"));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // In JWT, logout is handled client-side
        return ResponseEntity.ok(ApiResponseDTO.success("Logged out successfully", null));
    }
    
    @PostMapping("/2fa")
    public ResponseEntity<?> configureTwoFactor(
            @RequestParam Long userId,
            @RequestParam boolean enable) {
        try {
            userService.setTwoFactorAuth(userId, enable);
            String status = enable ? "enabled" : "disabled";
            return ResponseEntity.ok(ApiResponseDTO.success("Two-factor authentication " + status + " successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error("Error configuring 2FA: " + e.getMessage()));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserEntity user) {
        try {
            UserEntity registeredUser = userService.registerUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", registeredUser.getId());
            response.put("username", registeredUser.getUsername());
            
            return ResponseEntity.ok(ApiResponseDTO.success("User registered successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
} 