package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, String> {
    Optional<DepartmentEntity> findByDepartmentName(String departmentName);
}