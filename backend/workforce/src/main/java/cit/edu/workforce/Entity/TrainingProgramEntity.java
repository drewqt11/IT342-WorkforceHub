package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * TrainingProgramEntity - Represents the training program table in the database
 */
@Entity
@Table(name = "TRAINING_PROGRAM")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgramEntity {
    
    @Id
    @GeneratedValue(generator = "training-program-id")
    @GenericGenerator(name = "training-program-id", strategy = "cit.edu.workforce.Utils.TrainingProgramIdGenerator")
    @Column(name = "TRAINING_ID")
    private String trainingId;
    
    @Column(name = "TITLE", nullable = false)
    private String title;
    
    @Column(name = "DESCRIPTION", length = 1000)
    private String description;
    
    @Column(name = "PROVIDER")
    private String provider;
    
    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "END_DATE", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "TRAINING_MODE", nullable = false)
    private String trainingMode;
    
    @Column(name = "IS_ACTIVE")
    private boolean isActive = true;
    
    @ManyToOne
    @JoinColumn(name = "CREATED_BY", referencedColumnName = "USER_ID")
    private UserAccountEntity createdBy;

    // New relationship added: Training Program has many Training Enrollments
    @OneToMany(mappedBy = "trainingProgram", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingEnrollmentEntity> enrollments = new ArrayList<>();
} 