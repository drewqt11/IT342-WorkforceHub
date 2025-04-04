package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "educational_background")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EducationalBackgroundEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "education_id", updatable = false, nullable = false)
    private UUID educationId;

    @Column(name = "level", nullable = false)
    private String level;

    @Column(name = "institution_name", nullable = false)
    private String institutionName;

    @Column(name = "degree")
    private String degree;

    @Column(name = "year_graduated")
    private String yearGraduated;

    @Column(name = "honors")
    private String honors;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private EmployeeEntity employee;
} 