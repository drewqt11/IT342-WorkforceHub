package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.LeaveTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveTypeEntity, String> {

    Optional<LeaveTypeEntity> findByName(String name);

    List<LeaveTypeEntity> findByIsActiveTrue();

    Page<LeaveTypeEntity> findByIsActiveTrue(Pageable pageable);

    Page<LeaveTypeEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByName(String name);
}

// New file: Repository for leave types 