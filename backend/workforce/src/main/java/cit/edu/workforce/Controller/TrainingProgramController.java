package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.TrainingProgramDTO;
import cit.edu.workforce.Service.TrainingProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/api/training/programs")
@Tag(name = "Training Program Management", description = "APIs for managing training programs")
@SecurityRequirement(name = "bearerAuth")
public class TrainingProgramController {

    private final TrainingProgramService trainingProgramService;

    @Autowired
    public TrainingProgramController(TrainingProgramService trainingProgramService) {
        this.trainingProgramService = trainingProgramService;
    }

    @GetMapping
    @Operation(summary = "Get all training programs", description = "Get a paginated list of all training programs")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Page<TrainingProgramDTO>> getAllTrainingPrograms(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "programName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        return ResponseEntity.ok(trainingProgramService.getAllTrainingPrograms(pageable));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active training programs", description = "Get a paginated list of active training programs")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<TrainingProgramDTO>> getActiveTrainingPrograms(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "programName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        return ResponseEntity.ok(trainingProgramService.getActiveTrainingPrograms(pageable));
    }

    @GetMapping("/{programId}")
    @Operation(summary = "Get training program by ID", description = "Get details of a specific training program by ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<TrainingProgramDTO> getTrainingProgramById(@PathVariable String programId) {
        return trainingProgramService.getTrainingProgramById(programId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{programType}")
    @Operation(summary = "Get programs by type", description = "Get training programs by program type")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<TrainingProgramDTO>> getTrainingProgramsByType(
            @PathVariable String programType,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingProgramService.getTrainingProgramsByType(programType, pageable));
    }

    @GetMapping("/delivery-method/{deliveryMethod}")
    @Operation(summary = "Get programs by delivery method", description = "Get training programs by delivery method")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<TrainingProgramDTO>> getTrainingProgramsByDeliveryMethod(
            @PathVariable String deliveryMethod,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingProgramService.getTrainingProgramsByDeliveryMethod(deliveryMethod, pageable));
    }

    @GetMapping("/mandatory")
    @Operation(summary = "Get mandatory programs", description = "Get mandatory training programs")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<TrainingProgramDTO>> getMandatoryTrainingPrograms(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingProgramService.getMandatoryTrainingPrograms(pageable));
    }

    @GetMapping("/certification")
    @Operation(summary = "Get programs with certification", description = "Get training programs that offer certification")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<TrainingProgramDTO>> getTrainingProgramsWithCertification(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingProgramService.getTrainingProgramsWithCertification(pageable));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming programs", description = "Get upcoming training programs (next 3 months)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<TrainingProgramDTO>> getUpcomingTrainingPrograms(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingProgramService.getUpcomingTrainingPrograms(pageable));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get programs by department", description = "Get training programs applicable to a specific department")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<TrainingProgramDTO>> getTrainingProgramsByDepartment(
            @PathVariable String departmentId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingProgramService.getTrainingProgramsByDepartment(departmentId, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search programs", description = "Search training programs by name or description")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<TrainingProgramDTO>> searchTrainingPrograms(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingProgramService.searchTrainingPrograms(searchTerm, pageable));
    }

    @GetMapping("/creator/{employeeId}")
    @Operation(summary = "Get programs by creator", description = "Get training programs created by a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<List<TrainingProgramDTO>> getTrainingProgramsByCreator(@PathVariable String employeeId) {
        return ResponseEntity.ok(trainingProgramService.getTrainingProgramsByCreator(employeeId));
    }

    @PostMapping
    @Operation(summary = "Create training program", description = "Create a new training program")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<TrainingProgramDTO> createTrainingProgram(
            @Parameter(description = "Program name") @RequestParam String programName,
            @Parameter(description = "Description") @RequestParam(required = false) String description,
            @Parameter(description = "Program type") @RequestParam String programType,
            @Parameter(description = "Category") @RequestParam(required = false) String category,
            @Parameter(description = "Delivery method") @RequestParam String deliveryMethod,
            @Parameter(description = "Provider") @RequestParam(required = false) String provider,
            @Parameter(description = "Duration hours") @RequestParam(required = false) Integer durationHours,
            @Parameter(description = "Start date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Location") @RequestParam(required = false) String location,
            @Parameter(description = "Instructor") @RequestParam(required = false) String instructor,
            @Parameter(description = "Max participants") @RequestParam(required = false) Integer maxParticipants,
            @Parameter(description = "Prerequisites") @RequestParam(required = false) String prerequisites,
            @Parameter(description = "Materials URL") @RequestParam(required = false) String materialsUrl,
            @Parameter(description = "Certification offered") @RequestParam(required = false) Boolean certificationOffered,
            @Parameter(description = "Certification name") @RequestParam(required = false) String certificationName,
            @Parameter(description = "Certification validity months") @RequestParam(required = false) Integer certificationValidityMonths,
            @Parameter(description = "Is mandatory") @RequestParam(required = false) Boolean isMandatory,
            @Parameter(description = "Is active") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Created by (employee ID)") @RequestParam String createdById,
            @Parameter(description = "Applicable department IDs") @RequestParam(required = false) List<String> applicableDepartmentIds) {
        
        TrainingProgramDTO program = trainingProgramService.createTrainingProgram(
                programName, description, programType, category, deliveryMethod, provider,
                durationHours, startDate, endDate, location, instructor, maxParticipants,
                prerequisites, materialsUrl, certificationOffered, certificationName,
                certificationValidityMonths, isMandatory, isActive, createdById, applicableDepartmentIds);
        
        return new ResponseEntity<>(program, HttpStatus.CREATED);
    }

    @PutMapping("/{programId}")
    @Operation(summary = "Update training program", description = "Update an existing training program")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<TrainingProgramDTO> updateTrainingProgram(
            @PathVariable String programId,
            @Parameter(description = "Program name") @RequestParam(required = false) String programName,
            @Parameter(description = "Description") @RequestParam(required = false) String description,
            @Parameter(description = "Program type") @RequestParam(required = false) String programType,
            @Parameter(description = "Category") @RequestParam(required = false) String category,
            @Parameter(description = "Delivery method") @RequestParam(required = false) String deliveryMethod,
            @Parameter(description = "Provider") @RequestParam(required = false) String provider,
            @Parameter(description = "Duration hours") @RequestParam(required = false) Integer durationHours,
            @Parameter(description = "Start date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Location") @RequestParam(required = false) String location,
            @Parameter(description = "Instructor") @RequestParam(required = false) String instructor,
            @Parameter(description = "Max participants") @RequestParam(required = false) Integer maxParticipants,
            @Parameter(description = "Prerequisites") @RequestParam(required = false) String prerequisites,
            @Parameter(description = "Materials URL") @RequestParam(required = false) String materialsUrl,
            @Parameter(description = "Certification offered") @RequestParam(required = false) Boolean certificationOffered,
            @Parameter(description = "Certification name") @RequestParam(required = false) String certificationName,
            @Parameter(description = "Certification validity months") @RequestParam(required = false) Integer certificationValidityMonths,
            @Parameter(description = "Is mandatory") @RequestParam(required = false) Boolean isMandatory,
            @Parameter(description = "Is active") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Applicable department IDs") @RequestParam(required = false) List<String> applicableDepartmentIds) {
        
        TrainingProgramDTO program = trainingProgramService.updateTrainingProgram(
                programId, programName, description, programType, category, deliveryMethod, provider,
                durationHours, startDate, endDate, location, instructor, maxParticipants,
                prerequisites, materialsUrl, certificationOffered, certificationName,
                certificationValidityMonths, isMandatory, isActive, applicableDepartmentIds);
        
        return ResponseEntity.ok(program);
    }

    @PutMapping("/{programId}/status")
    @Operation(summary = "Set program status", description = "Activate or deactivate a training program")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<TrainingProgramDTO> setTrainingProgramStatus(
            @PathVariable String programId,
            @Parameter(description = "Is active") @RequestParam boolean isActive) {
        
        TrainingProgramDTO program = trainingProgramService.setTrainingProgramActiveStatus(programId, isActive);
        return ResponseEntity.ok(program);
    }

    @DeleteMapping("/{programId}")
    @Operation(summary = "Delete training program", description = "Delete a training program (only if no enrollments exist)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTrainingProgram(@PathVariable String programId) {
        trainingProgramService.deleteTrainingProgram(programId);
        return ResponseEntity.noContent().build();
    }
}

// New file: Controller for managing training programs in the Training & Development module
// Provides API endpoints for creating, updating, and retrieving training programs 