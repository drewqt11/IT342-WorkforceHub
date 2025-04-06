package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {

    @Id
    @Column(name = "role_id", updatable = false, nullable = false, length = 20)
    private String roleId;

    @Column(name = "role_name", nullable = false)
    private String roleName;
} 