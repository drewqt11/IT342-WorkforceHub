package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.TrainingProgramDTO;
import cit.edu.workforce.Entity.TrainingProgramEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.TrainingProgramRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import cit.edu.workforce.Repository.TrainingEnrollmentRepository;
import cit.edu.workforce.Repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TrainingProgramService - Service for managing training programs
 * New file: Provides functionality for creating and managing training programs
 */
@Service
@RequiredArgsConstructor
public class TrainingProgramService {

    private final TrainingProgramRepository trainingProgramRepository;
    private final TrainingEnrollmentRepository trainingEnrollmentRepository;
    private final UserAccountRepository userAccountRepository;
    private final EmployeeRepository employeeRepository;
    
    private static final List<String> VALID_TRAINING_MODES = Arrays.asList("Online", "In-person", "Hybrid");

    /**
     * Create a new training program
     * 
     * @param trainingProgramDTO The DTO containing training program data
     * @return The created training program DTO
     */
    @Transactional
    public TrainingProgramDTO createTrainingProgram(TrainingProgramDTO trainingProgramDTO) {
        // Validate input
        if (trainingProgramDTO.getTitle() == null || trainingProgramDTO.getTitle().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        
        if (trainingProgramDTO.getStartDate() == null || trainingProgramDTO.getEndDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end dates are required");
        }
        
        if (trainingProgramDTO.getEndDate().isBefore(trainingProgramDTO.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }
        
        if (trainingProgramDTO.getTrainingMode() == null || trainingProgramDTO.getTrainingMode().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Training mode is required");
        }
        
        if (!VALID_TRAINING_MODES.contains(trainingProgramDTO.getTrainingMode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Invalid training mode. Must be one of: " + String.join(", ", VALID_TRAINING_MODES));
        }
        
        // Get the current user as creator
        UserAccountEntity createdBy = getCurrentUser();
        
        // Create the training program
        TrainingProgramEntity trainingProgram = new TrainingProgramEntity();
        trainingProgram.setTitle(trainingProgramDTO.getTitle());
        trainingProgram.setDescription(trainingProgramDTO.getDescription());
        trainingProgram.setProvider(trainingProgramDTO.getProvider());
        trainingProgram.setStartDate(trainingProgramDTO.getStartDate());
        trainingProgram.setEndDate(trainingProgramDTO.getEndDate());
        trainingProgram.setTrainingMode(trainingProgramDTO.getTrainingMode());
        trainingProgram.setActive(trainingProgramDTO.isActive());
        trainingProgram.setCreatedBy(createdBy);
        
        TrainingProgramEntity savedTrainingProgram = trainingProgramRepository.save(trainingProgram);
        return convertToDTO(savedTrainingProgram);
    }

    /**
     * Get training program by ID
     * 
     * @param trainingId The training program ID
     * @return The training program DTO if found
     */
    public Optional<TrainingProgramDTO> getTrainingProgramById(String trainingId) {
        return trainingProgramRepository.findById(trainingId)
                .map(this::convertToDTO);
    }

    /**
     * Update an existing training program
     * 
     * @param trainingId The ID of the training program to update
     * @param trainingProgramDTO The updated training program data
     * @return The updated training program DTO
     */
    @Transactional
    public TrainingProgramDTO updateTrainingProgram(String trainingId, TrainingProgramDTO trainingProgramDTO) {
        TrainingProgramEntity trainingProgram = trainingProgramRepository.findById(trainingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training program not found"));
        
        // Update fields if provided
        if (trainingProgramDTO.getTitle() != null && !trainingProgramDTO.getTitle().isEmpty()) {
            trainingProgram.setTitle(trainingProgramDTO.getTitle());
        }
        
        if (trainingProgramDTO.getDescription() != null) {
            trainingProgram.setDescription(trainingProgramDTO.getDescription());
        }
        
        if (trainingProgramDTO.getProvider() != null) {
            trainingProgram.setProvider(trainingProgramDTO.getProvider());
        }
        
        if (trainingProgramDTO.getStartDate() != null) {
            trainingProgram.setStartDate(trainingProgramDTO.getStartDate());
        }
        
        if (trainingProgramDTO.getEndDate() != null) {
            trainingProgram.setEndDate(trainingProgramDTO.getEndDate());
        }
        
        // Validate date range
        if (trainingProgram.getEndDate().isBefore(trainingProgram.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }
        
        if (trainingProgramDTO.getTrainingMode() != null && !trainingProgramDTO.getTrainingMode().isEmpty()) {
            if (!VALID_TRAINING_MODES.contains(trainingProgramDTO.getTrainingMode())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Invalid training mode. Must be one of: " + String.join(", ", VALID_TRAINING_MODES));
            }
            trainingProgram.setTrainingMode(trainingProgramDTO.getTrainingMode());
        }
        
        trainingProgram.setActive(trainingProgramDTO.isActive());
        
        TrainingProgramEntity updatedTrainingProgram = trainingProgramRepository.save(trainingProgram);
        return convertToDTO(updatedTrainingProgram);
    }

    /**
     * Delete a training program
     * 
     * @param trainingId The ID of the training program to delete
     */
    @Transactional
    public void deleteTrainingProgram(String trainingId) {
        if (!trainingProgramRepository.existsById(trainingId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Training program not found");
        }
        
        // Check if there are any enrollments for this program
        TrainingProgramEntity trainingProgram = trainingProgramRepository.findById(trainingId).get();
        long enrollmentCount = trainingEnrollmentRepository.countByTrainingProgram(trainingProgram);
        
        if (enrollmentCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Cannot delete a training program that has enrollments. Deactivate it instead.");
        }
        
        trainingProgramRepository.deleteById(trainingId);
    }

    /**
     * Get all training programs
     * 
     * @return List of training program DTOs
     */
    public List<TrainingProgramDTO> getAllTrainingPrograms() {
        return trainingProgramRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated training programs
     */
    @Transactional(readOnly = true)
    public Page<TrainingProgramDTO> getAllTrainingPrograms(Pageable pageable) {
        return trainingProgramRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get all active training programs
     */
    @Transactional(readOnly = true)
    public List<TrainingProgramDTO> getActiveTrainingPrograms() {
        return trainingProgramRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated active training programs
     */
    @Transactional(readOnly = true)
    public Page<TrainingProgramDTO> getActiveTrainingPrograms(Pageable pageable) {
        return trainingProgramRepository.findByIsActiveTrue(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get all active and not yet ended training programs
     */
    @Transactional(readOnly = true)
    public List<TrainingProgramDTO> getActiveAndNotEndedTrainingPrograms() {
        return trainingProgramRepository.findActiveAndNotEndedTrainingPrograms().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated active and not yet ended training programs
     */
    @Transactional(readOnly = true)
    public Page<TrainingProgramDTO> getActiveAndNotEndedTrainingPrograms(Pageable pageable) {
        return trainingProgramRepository.findActiveAndNotEndedTrainingPrograms(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Search training programs by title
     */
    @Transactional(readOnly = true)
    public Page<TrainingProgramDTO> searchTrainingProgramsByTitle(String title, Pageable pageable) {
        return trainingProgramRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Search training programs by provider
     */
    @Transactional(readOnly = true)
    public Page<TrainingProgramDTO> searchTrainingProgramsByProvider(String provider, Pageable pageable) {
        return trainingProgramRepository.findByProviderContainingIgnoreCase(provider, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get training programs by training mode
     * 
     * @param trainingMode The training mode to filter by
     * @param pageable Pagination information
     * @return Page of training program DTOs
     */
    @Transactional(readOnly = true)
    public Page<TrainingProgramDTO> getTrainingProgramsByMode(String trainingMode, Pageable pageable) {
        if (!VALID_TRAINING_MODES.contains(trainingMode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid training mode. Must be one of: " + String.join(", ", VALID_TRAINING_MODES));
        }
        
        return trainingProgramRepository.findByTrainingMode(trainingMode, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get training programs by date range
     * 
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @param pageable Pagination information
     * @return Page of training program DTOs
     */
    @Transactional(readOnly = true)
    public Page<TrainingProgramDTO> getTrainingProgramsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before end date");
        }
        
        return trainingProgramRepository.findByStartDateBetween(startDate, endDate, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Map a TrainingProgramEntity to a TrainingProgramDTO
     * 
     * @param entity The entity to map
     * @return The mapped DTO
     */
    private TrainingProgramDTO convertToDTO(TrainingProgramEntity entity) {
        TrainingProgramDTO dto = new TrainingProgramDTO();
        dto.setTrainingId(entity.getTrainingId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setProvider(entity.getProvider());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setTrainingMode(entity.getTrainingMode());
        dto.setActive(entity.isActive());
        
        if (entity.getCreatedBy() != null) {
            dto.setCreatedById(entity.getCreatedBy().getUserId());
            dto.setCreatedByName(entity.getCreatedBy().getEmailAddress());
        }
        
        // Get enrollment count
        long enrollmentCount = trainingEnrollmentRepository.countByTrainingProgram(entity);
        dto.setEnrollmentCount((int) enrollmentCount);
        
        return dto;
    }

    /**
     * Get the current user account from the security context
     */
    private UserAccountEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String currentUserName = authentication.getName();
        return userAccountRepository.findByEmailAddress(currentUserName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));
    }
} 