package cit.edu.workforce.Service;

import cit.edu.workforce.Entity.UserEntity;
import cit.edu.workforce.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public UserEntity registerUser(UserEntity user) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already taken");
        }
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Ensure the user has at least one role
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Set.of("EMPLOYEE"));
        }
        
        return userRepository.save(user);
    }
    
    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public boolean validatePassword(UserEntity user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }
    
    public void setTwoFactorAuth(Long userId, boolean enable) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTwoFactorEnabled(enable);
        userRepository.save(user);
    }
}