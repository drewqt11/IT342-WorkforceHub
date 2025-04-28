package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountEntity {

    @Id
    @GeneratedValue(generator = "custom-user-id")
    @GenericGenerator(name = "custom-user-id", strategy = "cit.edu.workforce.Utils.UserIdGenerator")
    @Column(name = "user_id", updatable = false, nullable = false, length = 16)
    private String userId;

    @Column(name = "email_address", unique = true, nullable = false)
    private String emailAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        isActive = false;
    }
}