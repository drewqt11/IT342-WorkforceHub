package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_domain_list")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDomainListEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "domain_id", updatable = false, nullable = false, length = 36)
    private String domainId;

    @Column(name = "domain_name", nullable = false, unique = true)
    private String domainName;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}