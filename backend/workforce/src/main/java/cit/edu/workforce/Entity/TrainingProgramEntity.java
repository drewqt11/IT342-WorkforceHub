package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "training_program")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgramEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "program_id", updatable = false, nullable = false, length = 36)
    private String programId;

    @Column(name = "program_name", nullable = false)
    private String programName;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "program_type", nullable = false)
    private String programType; // TECHNICAL, SOFT_SKILLS, COMPLIANCE, LEADERSHIP, etc.

    @Column(name = "category")
    private String category;

    @Column(name = "delivery_method", nullable = false)
    private String deliveryMethod; // ONLINE, IN_PERSON, HYBRID, WEBINAR, etc.

    @Column(name = "provider")
    private String provider;

    @Column(name = "duration_hours")
    private Integer durationHours;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "location")
    private String location;

    @Column(name = "instructor")
    private String instructor;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "prerequisites", length = 1000)
    private String prerequisites;

    @Column(name = "materials_url")
    private String materialsUrl;

    @Column(name = "certification_offered")
    private Boolean certificationOffered = false;

    @Column(name = "certification_name")
    private String certificationName;

    @Column(name = "certification_validity_months")
    private Integer certificationValidityMonths;

    @Column(name = "is_mandatory")
    private Boolean isMandatory = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // New mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private EmployeeEntity createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // New relationship added
    @OneToMany(mappedBy = "trainingProgram", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TrainingEnrollmentEntity> enrollments = new HashSet<>();

    // New mapping - departments that this training is applicable to
    @ManyToMany
    @JoinTable(
        name = "training_program_department",
        joinColumns = @JoinColumn(name = "program_id"),
        inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private Set<DepartmentEntity> applicableDepartments = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// New file: Entity for training programs in the Training & Development module
// Contains details about training courses, certification options, and enrollment capacity