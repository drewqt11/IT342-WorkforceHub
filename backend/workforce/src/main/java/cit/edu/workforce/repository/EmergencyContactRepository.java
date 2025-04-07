package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmergencyContactEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContactEntity, UUID> {
    List<EmergencyContactEntity> findByEmployee(EmployeeEntity employee);
} 