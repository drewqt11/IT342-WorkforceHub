package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EducationalBackground;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EducationalBackgroundRepository extends JpaRepository<EducationalBackground, UUID> {
    List<EducationalBackground> findByEmployeeEmployeeId(UUID employeeId);
} 