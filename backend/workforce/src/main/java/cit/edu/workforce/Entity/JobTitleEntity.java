package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "job_title")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobTitleEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "job_id", updatable = false, nullable = false)
    private UUID jobId;

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Column(name = "job_description")
    private String jobDescription;

    @Column(name = "pay_grade")
    private String payGrade;
} 