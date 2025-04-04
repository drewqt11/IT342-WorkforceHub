package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.CertificationEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CertificationRepository extends JpaRepository<CertificationEntity, UUID> {
    List<CertificationEntity> findByEmployee(EmployeeEntity employee);
    List<CertificationEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);
} 