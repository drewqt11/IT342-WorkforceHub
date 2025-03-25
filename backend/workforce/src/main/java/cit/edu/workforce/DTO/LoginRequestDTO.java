package cit.edu.workforce.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login request payload")
public class LoginRequestDTO {
    @Schema(description = "Email for authentication", example = "john.doe@example.com", required = true)
    private String email;
    
    @Schema(description = "User password", example = "password123", required = true)
    private String password;

    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
} 