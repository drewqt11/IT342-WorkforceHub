package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.DepartmentEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.TrainingProgramEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingProgramRepository extends JpaRepository<TrainingProgramEntity, String> {
    
    List<TrainingProgramEntity> findByIsActive(Boolean isActive);
    
    Page<TrainingProgramEntity> findByIsActive(Boolean isActive, Pageable pageable);
    
    List<TrainingProgramEntity> findByProgramType(String programType);
    
    Page<TrainingProgramEntity> findByProgramType(String programType, Pageable pageable);
    
    List<TrainingProgramEntity> findByDeliveryMethod(String deliveryMethod);
    
    Page<TrainingProgramEntity> findByDeliveryMethod(String deliveryMethod, Pageable pageable);
    
    List<TrainingProgramEntity> findByIsMandatory(Boolean isMandatory);
    
    Page<TrainingProgramEntity> findByIsMandatory(Boolean isMandatory, Pageable pageable);
    
    List<TrainingProgramEntity> findByCertificationOffered(Boolean certificationOffered);
    
    Page<TrainingProgramEntity> findByCertificationOffered(Boolean certificationOffered, Pageable pageable);
    
    List<TrainingProgramEntity> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    
    Page<TrainingProgramEntity> findByStartDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    List<TrainingProgramEntity> findByEndDateAfter(LocalDate date);
    
    Page<TrainingProgramEntity> findByEndDateAfter(LocalDate date, Pageable pageable);
    
    List<TrainingProgramEntity> findByCreatedBy(EmployeeEntity createdBy);
    
    Page<TrainingProgramEntity> findByCreatedBy(EmployeeEntity createdBy, Pageable pageable);
    
    @Query("SELECT tp FROM TrainingProgramEntity tp JOIN tp.applicableDepartments d WHERE d = :department")
    List<TrainingProgramEntity> findByApplicableDepartment(@Param("department") DepartmentEntity department);
    
    @Query("SELECT tp FROM TrainingProgramEntity tp JOIN tp.applicableDepartments d WHERE d = :department")
    Page<TrainingProgramEntity> findByApplicableDepartment(@Param("department") DepartmentEntity department, Pageable pageable);
    
    Page<TrainingProgramEntity> findByProgramNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String programName, String description, Pageable pageable);
} 