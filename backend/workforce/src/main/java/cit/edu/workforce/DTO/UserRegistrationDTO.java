package cit.edu.workforce.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User registration request payload")
public class UserRegistrationDTO {
    @Schema(description = "First name of the user", example = "John", required = true)
    private String firstName;
    
    @Schema(description = "Last name of the user", example = "Doe", required = true)
    private String lastName;
    
    @Schema(description = "Username for login", example = "johndoe", required = true)
    private String username;
    
    @Schema(description = "Email address of the user", example = "john.doe@example.com", required = true)
    private String email;
    
    @Schema(description = "User password", example = "password123", required = true, minLength = 8)
    private String password;

    public UserRegistrationDTO() {
    }

    public UserRegistrationDTO(String firstName, String lastName, String username, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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