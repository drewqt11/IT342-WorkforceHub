package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;



@Entity
@Table(name = "job_title")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobTitleEntity {

    @Id
    @GeneratedValue(generator = "custom-job-id")
    @GenericGenerator(name = "custom-job-id", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "job_id", updatable = false, nullable = false, length = 36)
    private String jobId;

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Column(name = "job_description")
    private String jobDescription;

    @Column(name = "pay_grade")
    private String payGrade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private DepartmentEntity department;
}