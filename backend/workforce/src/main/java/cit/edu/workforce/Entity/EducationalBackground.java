package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "educational_background")
public class EducationalBackground {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "education_id", updatable = false, nullable = false)
    private UUID educationId;

    @Column
    private String level;

    @Column(name = "institution_name")
    private String institutionName;

    @Column
    private String degree;

    @Column(name = "year_graduated")
    private String yearGraduated;

    @Column
    private String honors;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
} 