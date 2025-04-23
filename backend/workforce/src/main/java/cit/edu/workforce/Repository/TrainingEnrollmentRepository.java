package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.TrainingEnrollmentEntity;
import cit.edu.workforce.Entity.TrainingProgramEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingEnrollmentRepository extends JpaRepository<TrainingEnrollmentEntity, String> {
    
    List<TrainingEnrollmentEntity> findByEmployee(EmployeeEntity employee);
    
    Page<TrainingEnrollmentEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    List<TrainingEnrollmentEntity> findByTrainingProgram(TrainingProgramEntity trainingProgram);
    
    Page<TrainingEnrollmentEntity> findByTrainingProgram(TrainingProgramEntity trainingProgram, Pageable pageable);
    
    List<TrainingEnrollmentEntity> findByStatus(String status);
    
    Page<TrainingEnrollmentEntity> findByStatus(String status, Pageable pageable);
    
    List<TrainingEnrollmentEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);
    
    Page<TrainingEnrollmentEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status, Pageable pageable);
    
    List<TrainingEnrollmentEntity> findByTrainingProgramAndStatus(TrainingProgramEntity trainingProgram, String status);
    
    Page<TrainingEnrollmentEntity> findByTrainingProgramAndStatus(TrainingProgramEntity trainingProgram, String status, Pageable pageable);
    
    Optional<TrainingEnrollmentEntity> findByEmployeeAndTrainingProgram(EmployeeEntity employee, TrainingProgramEntity trainingProgram);
    
    boolean existsByEmployeeAndTrainingProgram(EmployeeEntity employee, TrainingProgramEntity trainingProgram);
    
    List<TrainingEnrollmentEntity> findByEnrollmentDateBetween(LocalDate startDate, LocalDate endDate);
    
    Page<TrainingEnrollmentEntity> findByEnrollmentDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    List<TrainingEnrollmentEntity> findByDueDateBefore(LocalDate date);
    
    Page<TrainingEnrollmentEntity> findByDueDateBefore(LocalDate date, Pageable pageable);
    
    List<TrainingEnrollmentEntity> findByCompletionDateBetween(LocalDate startDate, LocalDate endDate);
    
    Page<TrainingEnrollmentEntity> findByCompletionDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    List<TrainingEnrollmentEntity> findByAssignedBy(EmployeeEntity assignedBy);
    
    Page<TrainingEnrollmentEntity> findByAssignedBy(EmployeeEntity assignedBy, Pageable pageable);
    
    List<TrainingEnrollmentEntity> findByEmployeeAndCertificateExpiryDateBefore(EmployeeEntity employee, LocalDate date);
    
    Page<TrainingEnrollmentEntity> findByEmployeeAndCertificateExpiryDateBefore(EmployeeEntity employee, LocalDate date, Pageable pageable);
} 