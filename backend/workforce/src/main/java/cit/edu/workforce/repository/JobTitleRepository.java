package cit.edu.workforce.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cit.edu.workforce.Entity.JobTitleEntity;

@Repository
public interface JobTitleRepository extends JpaRepository<JobTitleEntity, UUID> {
    Optional<JobTitleEntity> findByJobName(String jobName);
} 