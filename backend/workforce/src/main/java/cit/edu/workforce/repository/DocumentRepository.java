package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.DocumentEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {
    List<DocumentEntity> findByEmployee(EmployeeEntity employee);
    List<DocumentEntity> findByEmployeeAndDocumentType(EmployeeEntity employee, String documentType);
    List<DocumentEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);
    List<DocumentEntity> findByEmployeeEmployeeId(UUID employeeId);
} 