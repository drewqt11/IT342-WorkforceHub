package cit.edu.workforce.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.IdentificationRecordEntity;

@Repository
public interface IdentificationRecordRepository extends JpaRepository<IdentificationRecordEntity, UUID> {
    List<IdentificationRecordEntity> findByEmployee(EmployeeEntity employee);
    List<IdentificationRecordEntity> findByEmployeeAndIdType(EmployeeEntity employee, String idType);
} 