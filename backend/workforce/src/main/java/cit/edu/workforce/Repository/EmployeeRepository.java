package cit.edu.workforce.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.UserAccountEntity;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, String> {

    Optional<EmployeeEntity> findByEmail(String email);

    Optional<EmployeeEntity> findByUserAccount(UserAccountEntity userAccount);

    List<EmployeeEntity> findByStatus(Boolean status);

    Page<EmployeeEntity> findByStatus(Boolean status, Pageable pageable);

    Boolean existsByEmail(String email);

    Page<EmployeeEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    Page<EmployeeEntity> findByEmployeeId(String employeeId, Pageable pageable);

    Page<EmployeeEntity> findByDepartmentDepartmentNameContainingIgnoreCase(String departmentName, Pageable pageable);

    @Query("SELECT e FROM EmployeeEntity e WHERE e.status = true AND e.userAccount IS NOT NULL AND e.userAccount.isActive = true")
    Page<EmployeeEntity> findByStatusAndUserAccountActive(Pageable pageable);

    @Query("SELECT e FROM EmployeeEntity e WHERE e.status = false AND e.userAccount IS NOT NULL AND e.userAccount.isActive = true")
    Page<EmployeeEntity> findByStatusInactive(Pageable pageable);

    @Query("SELECT e FROM EmployeeEntity e WHERE e.status = false AND e.userAccount IS NOT NULL AND e.userAccount.isActive = false")
    Page<EmployeeEntity> findByStatusDeactivated(Pageable pageable);
}
