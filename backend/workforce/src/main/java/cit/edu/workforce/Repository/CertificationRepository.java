package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, UUID> {
    List<Certification> findByEmployeeEmployeeId(UUID employeeId);
} 