package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EducationalBackgroundEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EducationalBackgroundRepository extends JpaRepository<EducationalBackgroundEntity, UUID> {
    List<EducationalBackgroundEntity> findByEmployee(EmployeeEntity employee);
    List<EducationalBackgroundEntity> findByEmployeeAndLevel(EmployeeEntity employee, String level);
} 