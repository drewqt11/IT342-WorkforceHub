package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UUID userId;
    private String email;
    private String role;
    private UUID employeeId;
    private String firstName;
    private String lastName;
    
    public AuthResponseDTO(String accessToken, String refreshToken, UUID userId, String email, String role, 
                          UUID employeeId, String firstName, String lastName) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    public AuthResponseDTO(String accessToken, UUID userId, String email, String role, 
                          UUID employeeId, String firstName, String lastName) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
    }
} 