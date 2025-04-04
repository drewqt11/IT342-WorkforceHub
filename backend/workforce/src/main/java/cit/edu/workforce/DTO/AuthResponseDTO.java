package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String tokenType = "Bearer";
    private UUID userId;
    private String emailAddress;
    private String role;
    private UUID employeeId;
    private String firstName;
    private String lastName;
    
    public AuthResponseDTO(String token, UUID userId, String emailAddress, String role, UUID employeeId, String firstName, String lastName) {
        this.token = token;
        this.userId = userId;
        this.emailAddress = emailAddress;
        this.role = role;
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
    }
} 