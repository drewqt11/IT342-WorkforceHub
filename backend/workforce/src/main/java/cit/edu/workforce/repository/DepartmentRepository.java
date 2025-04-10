package cit.edu.workforce.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cit.edu.workforce.Entity.DepartmentEntity;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, String> {

    Optional<DepartmentEntity> findByDepartmentName(String departmentName);
}
