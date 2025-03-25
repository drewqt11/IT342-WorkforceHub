package cit.edu.workforce.DTO;

public class LoginResponseDTO {
    private String token;
    private UserDTO userInfo;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, UserDTO userInfo) {
        this.token = token;
        this.userInfo = userInfo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDTO getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserDTO userInfo) {
        this.userInfo = userInfo;
    }
} 