package cit.edu.workforce.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cit.edu.workforce.Entity.EmergencyContactEntity;
import cit.edu.workforce.Entity.EmployeeEntity;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContactEntity, UUID> {
    List<EmergencyContactEntity> findByEmployee(EmployeeEntity employee);
} 