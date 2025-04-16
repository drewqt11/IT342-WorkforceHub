package cit.edu.workforce.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cit.edu.workforce.Entity.CertificationEntity;
import cit.edu.workforce.Entity.EmployeeEntity;

@Repository
public interface CertificationRepository extends JpaRepository<CertificationEntity, String> {

    List<CertificationEntity> findByEmployee(EmployeeEntity employee);

    List<CertificationEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);
}
