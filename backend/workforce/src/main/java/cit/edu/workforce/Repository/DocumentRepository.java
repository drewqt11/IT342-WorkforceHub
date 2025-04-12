package cit.edu.workforce.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cit.edu.workforce.Entity.DocumentEntity;
import cit.edu.workforce.Entity.EmployeeEntity;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {

    List<DocumentEntity> findByEmployee(EmployeeEntity employee);

    List<DocumentEntity> findByEmployeeAndDocumentType(EmployeeEntity employee, String documentType);

    List<DocumentEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);

    List<DocumentEntity> findByEmployeeEmployeeId(String employeeId);
}
