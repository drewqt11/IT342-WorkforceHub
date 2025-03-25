package cit.edu.workforce.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login response containing JWT token and user information")
public class LoginResponseDTO {
    @Schema(description = "JWT authentication token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "User information")
    private UserDTO user;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
} 