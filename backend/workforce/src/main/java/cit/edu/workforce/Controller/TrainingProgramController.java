package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.TrainingProgramDTO;
import cit.edu.workforce.Service.TrainingProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TrainingProgramController - Provides API endpoints for training program management
 * This controller handles all training program-related operations including
 * creating, reading, updating, and deleting training programs.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Training Program Management", description = "Training program management APIs")
@SecurityRequirement(name = "bearerAuth")
public class TrainingProgramController {
    
    @Autowired
    private TrainingProgramService trainingProgramService;
    
    @Operation(summary = "Create a new training program", description = "Creates a new training program. Requires HR or Admin role.")
    @PostMapping("/training-programs")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<TrainingProgramDTO> createTrainingProgram(
            @Valid @RequestBody TrainingProgramDTO trainingProgramDTO) {
        TrainingProgramDTO createdProgram = trainingProgramService.createTrainingProgram(trainingProgramDTO);
        return new ResponseEntity<>(createdProgram, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Get all training programs", description = "Returns a list of all training programs")
    @GetMapping("/training-programs")
    public ResponseEntity<List<TrainingProgramDTO>> getAllTrainingPrograms() {
        List<TrainingProgramDTO> programs = trainingProgramService.getAllTrainingPrograms();
        return new ResponseEntity<>(programs, HttpStatus.OK);
    }
    
    @Operation(summary = "Get paginated training programs", description = "Returns a paginated list of training programs")
    @GetMapping("/training-programs/paged")
    public ResponseEntity<Page<TrainingProgramDTO>> getPaginatedTrainingPrograms(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "title") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<TrainingProgramDTO> programsPage = trainingProgramService.getAllTrainingPrograms(pageable);
        
        return new ResponseEntity<>(programsPage, HttpStatus.OK);
    }
    
    @Operation(summary = "Get training program by ID", description = "Returns a training program by its ID")
    @GetMapping("/training-programs/{trainingId}")
    public ResponseEntity<TrainingProgramDTO> getTrainingProgramById(
            @Parameter(description = "Training program ID") @PathVariable String trainingId) {
        
        Optional<TrainingProgramDTO> program = trainingProgramService.getTrainingProgramById(trainingId);
        
        return program.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @Operation(summary = "Update a training program", description = "Updates an existing training program. Requires HR or Admin role.")
    @PutMapping("/training-programs/{trainingId}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<TrainingProgramDTO> updateTrainingProgram(
            @Parameter(description = "Training program ID") @PathVariable String trainingId,
            @Valid @RequestBody TrainingProgramDTO trainingProgramDTO) {
        
        TrainingProgramDTO updatedProgram = trainingProgramService.updateTrainingProgram(trainingId, trainingProgramDTO);
        return new ResponseEntity<>(updatedProgram, HttpStatus.OK);
    }
    
    @Operation(summary = "Delete a training program", description = "Deletes a training program. Requires HR or Admin role.")
    @DeleteMapping("/training-programs/{trainingId}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<Void> deleteTrainingProgram(
            @Parameter(description = "Training program ID") @PathVariable String trainingId) {
        
        trainingProgramService.deleteTrainingProgram(trainingId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @Operation(summary = "Get active training programs", description = "Returns a list of active training programs")
    @GetMapping("/training-programs/active")
    public ResponseEntity<List<TrainingProgramDTO>> getActiveTrainingPrograms() {
        List<TrainingProgramDTO> programs = trainingProgramService.getActiveTrainingPrograms();
        return new ResponseEntity<>(programs, HttpStatus.OK);
    }
    
    @Operation(summary = "Get paginated active training programs", description = "Returns a paginated list of active training programs")
    @GetMapping("/training-programs/active/paged")
    public ResponseEntity<Page<TrainingProgramDTO>> getPaginatedActiveTrainingPrograms(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "title") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<TrainingProgramDTO> programsPage = trainingProgramService.getActiveTrainingPrograms(pageable);
        
        return new ResponseEntity<>(programsPage, HttpStatus.OK);
    }
    
    @Operation(summary = "Get active and not ended training programs", 
            description = "Returns a list of active training programs that have not yet ended")
    @GetMapping("/training-programs/active-and-not-ended")
    public ResponseEntity<List<TrainingProgramDTO>> getActiveAndNotEndedTrainingPrograms() {
        List<TrainingProgramDTO> programs = trainingProgramService.getActiveAndNotEndedTrainingPrograms();
        return new ResponseEntity<>(programs, HttpStatus.OK);
    }
    
    @Operation(summary = "Search training programs by title", description = "Returns training programs matching the title search query")
    @GetMapping("/training-programs/search/title")
    public ResponseEntity<Page<TrainingProgramDTO>> searchTrainingProgramsByTitle(
            @Parameter(description = "Title search query") @RequestParam String title,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TrainingProgramDTO> results = trainingProgramService.searchTrainingProgramsByTitle(title, pageable);
        
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
    
    @Operation(summary = "Search training programs by provider", description = "Returns training programs matching the provider search query")
    @GetMapping("/training-programs/search/provider")
    public ResponseEntity<Page<TrainingProgramDTO>> searchTrainingProgramsByProvider(
            @Parameter(description = "Provider search query") @RequestParam String provider,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TrainingProgramDTO> results = trainingProgramService.searchTrainingProgramsByProvider(provider, pageable);
        
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
    
    @Operation(summary = "Get training programs by training mode", description = "Returns training programs with the specified training mode")
    @GetMapping("/training-programs/mode/{trainingMode}")
    public ResponseEntity<Page<TrainingProgramDTO>> getTrainingProgramsByMode(
            @Parameter(description = "Training mode (Online, In-person, Hybrid)") @PathVariable String trainingMode,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TrainingProgramDTO> results = trainingProgramService.getTrainingProgramsByMode(trainingMode, pageable);
        
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
    
    @Operation(summary = "Get training programs by date range", 
            description = "Returns training programs starting within the specified date range")
    @GetMapping("/training-programs/date-range")
    public ResponseEntity<Page<TrainingProgramDTO>> getTrainingProgramsByDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TrainingProgramDTO> results = trainingProgramService.getTrainingProgramsByDateRange(startDate, endDate, pageable);
        
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
} 