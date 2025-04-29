package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.TrainingProgramEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * TrainingProgramRepository - Repository for managing training programs
 * New file: Provides methods to access training program data
 */
@Repository
public interface TrainingProgramRepository extends JpaRepository<TrainingProgramEntity, String> {
    
    /**
     * Find training programs with a specific title (case-insensitive partial match)
     */
    List<TrainingProgramEntity> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Find paginated training programs with a specific title (case-insensitive partial match)
     */
    Page<TrainingProgramEntity> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    /**
     * Find training programs by provider (case-insensitive partial match)
     */
    List<TrainingProgramEntity> findByProviderContainingIgnoreCase(String provider);
    
    /**
     * Find paginated training programs by provider (case-insensitive partial match)
     */
    Page<TrainingProgramEntity> findByProviderContainingIgnoreCase(String provider, Pageable pageable);
    
    /**
     * Find active training programs
     */
    List<TrainingProgramEntity> findByIsActiveTrue();
    
    /**
     * Find paginated active training programs
     */
    Page<TrainingProgramEntity> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Find training programs by created user
     */
    List<TrainingProgramEntity> findByCreatedBy(UserAccountEntity createdBy);
    
    /**
     * Find paginated training programs by created user
     */
    Page<TrainingProgramEntity> findByCreatedBy(UserAccountEntity createdBy, Pageable pageable);
    
    /**
     * Find training programs starting within a date range
     */
    List<TrainingProgramEntity> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find paginated training programs starting within a date range
     */
    Page<TrainingProgramEntity> findByStartDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Find training programs by training mode
     */
    List<TrainingProgramEntity> findByTrainingMode(String trainingMode);
    
    /**
     * Find paginated training programs by training mode
     */
    Page<TrainingProgramEntity> findByTrainingMode(String trainingMode, Pageable pageable);
    
    /**
     * Find training programs that are active and have not yet ended
     */
    @Query("SELECT t FROM TrainingProgramEntity t WHERE t.isActive = true AND t.endDate >= CURRENT_DATE")
    List<TrainingProgramEntity> findActiveAndNotEndedTrainingPrograms();
    
    /**
     * Find paginated training programs that are active and have not yet ended
     */
    @Query("SELECT t FROM TrainingProgramEntity t WHERE t.isActive = true AND t.endDate >= CURRENT_DATE")
    Page<TrainingProgramEntity> findActiveAndNotEndedTrainingPrograms(Pageable pageable);
} 