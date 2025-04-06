package cit.edu.workforce.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cit.edu.workforce.Entity.EducationalBackgroundEntity;
import cit.edu.workforce.Entity.EmployeeEntity;

@Repository
public interface EducationalBackgroundRepository extends JpaRepository<EducationalBackgroundEntity, UUID> {
    List<EducationalBackgroundEntity> findByEmployee(EmployeeEntity employee);
    List<EducationalBackgroundEntity> findByEmployeeAndLevel(EmployeeEntity employee, String level);
} 