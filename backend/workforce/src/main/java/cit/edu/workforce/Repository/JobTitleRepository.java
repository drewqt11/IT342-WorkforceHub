package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.JobTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobTitleRepository extends JpaRepository<JobTitle, UUID> {
    boolean existsByJobName(String jobName);
} 