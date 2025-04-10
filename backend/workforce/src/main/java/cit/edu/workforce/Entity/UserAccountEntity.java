package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

/**
 * Entity class representing a user account in the system.
 * This class is responsible for storing authentication credentials and account status.
 * User accounts are linked to employee records and contain login information.
 */
@Entity
@Table(name = "user_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountEntity {

    /**
     * Unique identifier for the user account.
     * Format: "USER-XXXX-XXXXX" with random characters 0-9 and a-f.
     * Generated using a custom ID generator.
     */
    @Id
    @GeneratedValue(generator = "custom-user-id")
    @GenericGenerator(name = "custom-user-id", strategy = "cit.edu.workforce.Utils.UserIdGenerator")
    @Column(name = "user_id", updatable = false, nullable = false, length = 16)
    private String userId;

    /**
     * Email address used for login.
     * Must be unique and is validated against approved email domains.
     */
    @Column(name = "email_address", unique = true, nullable = false)
    private String emailAddress;

    /**
     * Encrypted password for the account.
     * Stored using BCrypt hashing.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Timestamp when the user account was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of the user's last successful login.
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /**
     * Flag indicating whether the account is active.
     * Accounts are soft-deleted by setting this to false.
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    /**
     * Lifecycle callback method that's automatically called before persisting a new entity.
     * Sets default values for createdAt and isActive fields.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        isActive = true;
    }
}
