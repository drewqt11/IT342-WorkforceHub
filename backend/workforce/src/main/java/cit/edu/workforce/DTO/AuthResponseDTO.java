package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private String userId;
    private String email;
    private String role;
    private String employeeId;
    private String firstName;
    private String lastName;

    public AuthResponseDTO(String accessToken, String refreshToken, String userId, String email, String role,
                          String employeeId, String firstName, String lastName) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public AuthResponseDTO(String accessToken, String userId, String email, String role,
                          String employeeId, String firstName, String lastName) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}