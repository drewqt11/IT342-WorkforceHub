package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.EventEntity;
import cit.edu.workforce.Entity.TrainingEnrollmentEntity;
import cit.edu.workforce.Entity.TrainingProgramEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TrainingEnrollmentRepository - Repository for managing training enrollments
 * New file: Provides methods to access training enrollment data
 */
@Repository
public interface TrainingEnrollmentRepository extends JpaRepository<TrainingEnrollmentEntity, String> {
    
    /**
     * Find all enrollments for a specific employee
     */
    List<TrainingEnrollmentEntity> findByEmployee(EmployeeEntity employee);
    
    /**
     * Find paginated enrollments for a specific employee
     */
    Page<TrainingEnrollmentEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    
    /**
     * Find all enrollments for a specific training program
     */
    List<TrainingEnrollmentEntity> findByTrainingProgram(TrainingProgramEntity trainingProgram);
    
    /**
     * Find paginated enrollments for a specific training program
     */
    Page<TrainingEnrollmentEntity> findByTrainingProgram(TrainingProgramEntity trainingProgram, Pageable pageable);
    
    /**
     * Find all enrollments for a specific event
     */
    List<TrainingEnrollmentEntity> findByEvent(EventEntity event);
    
    /**
     * Find paginated enrollments for a specific event
     */
    Page<TrainingEnrollmentEntity> findByEvent(EventEntity event, Pageable pageable);
    
    /**
     * Find enrollments by status
     */
    List<TrainingEnrollmentEntity> findByStatus(String status);
    
    /**
     * Find paginated enrollments by status
     */
    Page<TrainingEnrollmentEntity> findByStatus(String status, Pageable pageable);
    
    /**
     * Find enrollments for an employee with a specific status
     */
    List<TrainingEnrollmentEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);
    
    /**
     * Find paginated enrollments for an employee with a specific status
     */
    Page<TrainingEnrollmentEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status, Pageable pageable);
    
    /**
     * Find enrollments by enrollment type
     */
    List<TrainingEnrollmentEntity> findByEnrollmentType(String enrollmentType);
    
    /**
     * Find paginated enrollments by enrollment type
     */
    Page<TrainingEnrollmentEntity> findByEnrollmentType(String enrollmentType, Pageable pageable);
    
    /**
     * Find an enrollment for a specific employee and training program
     */
    Optional<TrainingEnrollmentEntity> findByEmployeeAndTrainingProgram(EmployeeEntity employee, TrainingProgramEntity trainingProgram);
    
    /**
     * Find an enrollment for a specific employee and event
     */
    Optional<TrainingEnrollmentEntity> findByEmployeeAndEvent(EmployeeEntity employee, EventEntity event);
    
    /**
     * Find enrollments for a training program with a specific status
     */
    List<TrainingEnrollmentEntity> findByTrainingProgramAndStatus(TrainingProgramEntity trainingProgram, String status);
    
    /**
     * Find enrollments for an event with a specific status
     */
    List<TrainingEnrollmentEntity> findByEventAndStatus(EventEntity event, String status);
    
    /**
     * Count enrollments for a training program
     */
    long countByTrainingProgram(TrainingProgramEntity trainingProgram);
    
    /**
     * Count enrollments for an event
     */
    long countByEvent(EventEntity event);
    
    /**
     * Count completed enrollments for a training program
     */
    long countByTrainingProgramAndStatus(TrainingProgramEntity trainingProgram, String status);
    
    /**
     * Count completed enrollments for an event
     */
    long countByEventAndStatus(EventEntity event, String status);
    
    /**
     * Find enrollments made within a date range
     */
    List<TrainingEnrollmentEntity> findByEnrolledDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find enrollments for active training programs
     */
    @Query("SELECT e FROM TrainingEnrollmentEntity e WHERE e.trainingProgram.isActive = true")
    List<TrainingEnrollmentEntity> findByActiveTrainingPrograms();
    
    /**
     * Find enrollments for active events
     */
    @Query("SELECT e FROM TrainingEnrollmentEntity e WHERE e.event.isActive = true")
    List<TrainingEnrollmentEntity> findByActiveEvents();
} 