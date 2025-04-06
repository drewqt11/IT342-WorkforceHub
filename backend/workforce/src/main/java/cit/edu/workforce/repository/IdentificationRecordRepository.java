package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.IdentificationRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IdentificationRecordRepository extends JpaRepository<IdentificationRecordEntity, UUID> {
    List<IdentificationRecordEntity> findByEmployee(EmployeeEntity employee);
    List<IdentificationRecordEntity> findByEmployeeAndIdType(EmployeeEntity employee, String idType);
} 