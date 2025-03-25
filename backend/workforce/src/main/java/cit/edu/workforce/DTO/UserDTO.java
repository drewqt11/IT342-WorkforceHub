package cit.edu.workforce.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User Data Transfer Object")
public class UserDTO {
    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;
    
    @Schema(description = "First name of the user", example = "John")
    private String firstName;
    
    @Schema(description = "Last name of the user", example = "Doe")
    private String lastName;
    
    @Schema(description = "Username for login", example = "johndoe")
    private String username;
    
    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;

    public UserDTO() {
    }

    public UserDTO(Long id, String firstName, String lastName, String username, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
} 