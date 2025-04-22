package cit.edu.workforce.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cit.edu.workforce.Entity.JobTitleEntity;

@Repository
public interface JobTitleRepository extends JpaRepository<JobTitleEntity, String> {

    Optional<JobTitleEntity> findByJobName(String jobName);
    List<JobTitleEntity> findByDepartment_DepartmentId(String departmentId);
}
