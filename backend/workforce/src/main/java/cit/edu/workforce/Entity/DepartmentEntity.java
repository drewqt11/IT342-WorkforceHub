package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import java.util.ArrayList;
import java.util.List;



@Entity
@Table(name = "department")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentEntity {

    @Id
    @GeneratedValue(generator = "custom-department-id")
    @GenericGenerator(name = "custom-department-id", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "deparment_id", updatable = false, nullable = false, length = 36)
    private String departmentId;

    @Column(name = "department_name", nullable = false)
    private String departmentName;
    
    @Column(name = "description", nullable = true, length = 500)
    private String description;

    // New mapping: Department has many JobTitles
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobTitleEntity> jobTitles = new ArrayList<>();
    
    // New relationship added: Department has many job listings
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobListingEntity> jobListings = new ArrayList<>();
}