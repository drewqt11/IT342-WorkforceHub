package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "leave_types")
public class LeaveTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer defaultDays;

    @Column(nullable = false)
    private Boolean isPaid = true;

    @Column(nullable = false)
    private Boolean requiresApproval = true;

    @Column(nullable = false)
    private Boolean isActive = true;
}

// New file: Entity for leave types with name, description, and default days allotment 