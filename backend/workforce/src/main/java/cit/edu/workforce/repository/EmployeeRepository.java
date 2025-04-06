package cit.edu.workforce.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.UserAccountEntity;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {
    Optional<EmployeeEntity> findByEmail(String email);
    Optional<EmployeeEntity> findByUserAccount(UserAccountEntity userAccount);
    List<EmployeeEntity> findByStatus(String status);
    Page<EmployeeEntity> findByStatus(String status, Pageable pageable);
    Boolean existsByEmail(String email);
    
    Page<EmployeeEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);
    
    Page<EmployeeEntity> findByEmployeeId(UUID employeeId, Pageable pageable);
    
    Page<EmployeeEntity> findByDepartmentDepartmentNameContainingIgnoreCase(String departmentName, Pageable pageable);
} 