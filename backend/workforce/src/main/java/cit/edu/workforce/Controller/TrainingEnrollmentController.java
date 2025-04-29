package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.CertificateDTO;
import cit.edu.workforce.DTO.TrainingEnrollmentDTO;
import cit.edu.workforce.Service.TrainingEnrollmentService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * TrainingEnrollmentController - Provides API endpoints for training enrollment management
 * This controller handles all training enrollment-related operations including
 * creating, reading, and updating training enrollments.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Training Enrollment Management", description = "Training enrollment management APIs")
@SecurityRequirement(name = "bearerAuth")
public class TrainingEnrollmentController {
    
    @Autowired
    private TrainingEnrollmentService trainingEnrollmentService;
    
    @Operation(summary = "Create a new training program enrollment", 
              description = "Enrolls an employee in a training program. Requires HR or Admin role.")
    @PostMapping("/training-enrollments/program")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<TrainingEnrollmentDTO> createTrainingProgramEnrollment(
            @Valid @RequestBody TrainingEnrollmentDTO trainingEnrollmentDTO) {
        TrainingEnrollmentDTO createdEnrollment = trainingEnrollmentService.createTrainingProgramEnrollment(trainingEnrollmentDTO);
        return new ResponseEntity<>(createdEnrollment, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Create a new event enrollment", 
              description = "Enrolls an employee in an event. Requires HR or Admin role.")
    @PostMapping("/training-enrollments/event")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<TrainingEnrollmentDTO> createEventEnrollment(
            @Valid @RequestBody TrainingEnrollmentDTO trainingEnrollmentDTO) {
        TrainingEnrollmentDTO createdEnrollment = trainingEnrollmentService.createEventEnrollment(trainingEnrollmentDTO);
        return new ResponseEntity<>(createdEnrollment, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Self-enroll in a training program", 
              description = "Allows an employee to self-enroll in a training program")
    @PostMapping("/training-enrollments/self/program")
    public ResponseEntity<TrainingEnrollmentDTO> selfEnrollInTrainingProgram(
            @Valid @RequestBody TrainingEnrollmentDTO trainingEnrollmentDTO) {
        // Set enrollment type to "Self-enrolled"
        trainingEnrollmentDTO.setEnrollmentType("Self-enrolled");
        TrainingEnrollmentDTO createdEnrollment = trainingEnrollmentService.createTrainingProgramEnrollment(trainingEnrollmentDTO);
        return new ResponseEntity<>(createdEnrollment, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Self-enroll in an event", 
              description = "Allows an employee to self-enroll in an event")
    @PostMapping("/training-enrollments/self/event")
    public ResponseEntity<TrainingEnrollmentDTO> selfEnrollInEvent(
            @Valid @RequestBody TrainingEnrollmentDTO trainingEnrollmentDTO) {
        // Set enrollment type to "Self-enrolled"
        trainingEnrollmentDTO.setEnrollmentType("Self-enrolled");
        TrainingEnrollmentDTO createdEnrollment = trainingEnrollmentService.createEventEnrollment(trainingEnrollmentDTO);
        return new ResponseEntity<>(createdEnrollment, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Get enrollment by ID", description = "Returns an enrollment by its ID")
    @GetMapping("/training-enrollments/{enrollmentId}")
    public ResponseEntity<TrainingEnrollmentDTO> getEnrollmentById(
            @Parameter(description = "Enrollment ID") @PathVariable String enrollmentId) {
        
        Optional<TrainingEnrollmentDTO> enrollment = trainingEnrollmentService.getEnrollmentById(enrollmentId);
        
        return enrollment.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @Operation(summary = "Get enrollments by employee", description = "Returns all enrollments for a specific employee")
    @GetMapping("/training-enrollments/employee/{employeeId}")
    public ResponseEntity<List<TrainingEnrollmentDTO>> getEnrollmentsByEmployee(
            @Parameter(description = "Employee ID") @PathVariable String employeeId) {
        
        List<TrainingEnrollmentDTO> enrollments = trainingEnrollmentService.getEnrollmentsByEmployee(employeeId);
        return new ResponseEntity<>(enrollments, HttpStatus.OK);
    }
    
    @Operation(summary = "Get paginated enrollments by employee", 
              description = "Returns paginated enrollments for a specific employee")
    @GetMapping("/training-enrollments/employee/{employeeId}/paged")
    public ResponseEntity<Page<TrainingEnrollmentDTO>> getPaginatedEnrollmentsByEmployee(
            @Parameter(description = "Employee ID") @PathVariable String employeeId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "enrolledDate") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<TrainingEnrollmentDTO> enrollmentsPage = trainingEnrollmentService.getEnrollmentsByEmployee(employeeId, pageable);
        
        return new ResponseEntity<>(enrollmentsPage, HttpStatus.OK);
    }
    
    @Operation(summary = "Get enrollments by training program", 
              description = "Returns all enrollments for a specific training program")
    @GetMapping("/training-enrollments/program/{trainingId}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<List<TrainingEnrollmentDTO>> getEnrollmentsByTrainingProgram(
            @Parameter(description = "Training program ID") @PathVariable String trainingId) {
        
        List<TrainingEnrollmentDTO> enrollments = trainingEnrollmentService.getEnrollmentsByTrainingProgram(trainingId);
        return new ResponseEntity<>(enrollments, HttpStatus.OK);
    }
    
    @Operation(summary = "Get paginated enrollments by training program", 
              description = "Returns paginated enrollments for a specific training program")
    @GetMapping("/training-enrollments/program/{trainingId}/paged")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<Page<TrainingEnrollmentDTO>> getPaginatedEnrollmentsByTrainingProgram(
            @Parameter(description = "Training program ID") @PathVariable String trainingId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "enrolledDate") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<TrainingEnrollmentDTO> enrollmentsPage = trainingEnrollmentService.getEnrollmentsByTrainingProgram(trainingId, pageable);
        
        return new ResponseEntity<>(enrollmentsPage, HttpStatus.OK);
    }
    
    @Operation(summary = "Get enrollments by event", 
              description = "Returns all enrollments for a specific event")
    @GetMapping("/training-enrollments/event/{eventId}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<List<TrainingEnrollmentDTO>> getEnrollmentsByEvent(
            @Parameter(description = "Event ID") @PathVariable String eventId) {
        
        List<TrainingEnrollmentDTO> enrollments = trainingEnrollmentService.getEnrollmentsByEvent(eventId);
        return new ResponseEntity<>(enrollments, HttpStatus.OK);
    }
    
    @Operation(summary = "Get paginated enrollments by event", 
              description = "Returns paginated enrollments for a specific event")
    @GetMapping("/training-enrollments/event/{eventId}/paged")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<Page<TrainingEnrollmentDTO>> getPaginatedEnrollmentsByEvent(
            @Parameter(description = "Event ID") @PathVariable String eventId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "enrolledDate") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<TrainingEnrollmentDTO> enrollmentsPage = trainingEnrollmentService.getEnrollmentsByEvent(eventId, pageable);
        
        return new ResponseEntity<>(enrollmentsPage, HttpStatus.OK);
    }
    
    @Operation(summary = "Update enrollment status", 
              description = "Updates the status of an enrollment. Requires HR or Admin role.")
    @PutMapping("/training-enrollments/{enrollmentId}/status")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<TrainingEnrollmentDTO> updateEnrollmentStatus(
            @Parameter(description = "Enrollment ID") @PathVariable String enrollmentId,
            @Parameter(description = "New status (Enrolled, Completed, Cancelled)") @RequestParam String status) {
        
        TrainingEnrollmentDTO updatedEnrollment = trainingEnrollmentService.updateEnrollmentStatus(enrollmentId, status);
        return new ResponseEntity<>(updatedEnrollment, HttpStatus.OK);
    }
    
    @Operation(summary = "Complete enrollment", 
              description = "Marks an enrollment as completed. Requires HR or Admin role.")
    @PutMapping("/training-enrollments/{enrollmentId}/complete")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<TrainingEnrollmentDTO> completeEnrollment(
            @Parameter(description = "Enrollment ID") @PathVariable String enrollmentId) {
        
        TrainingEnrollmentDTO updatedEnrollment = trainingEnrollmentService.completeEnrollment(enrollmentId);
        return new ResponseEntity<>(updatedEnrollment, HttpStatus.OK);
    }
    
    @Operation(summary = "Cancel enrollment", 
              description = "Cancels an enrollment. Can be done by the enrolled employee or HR/Admin.")
    @PutMapping("/training-enrollments/{enrollmentId}/cancel")
    public ResponseEntity<TrainingEnrollmentDTO> cancelEnrollment(
            @Parameter(description = "Enrollment ID") @PathVariable String enrollmentId) {
        
        TrainingEnrollmentDTO updatedEnrollment = trainingEnrollmentService.cancelEnrollment(enrollmentId);
        return new ResponseEntity<>(updatedEnrollment, HttpStatus.OK);
    }
    
    @Operation(summary = "Add certificate to enrollment", 
              description = "Adds a certificate to an enrollment and marks it as completed")
    @PostMapping("/training-enrollments/{enrollmentId}/certificates")
    public ResponseEntity<TrainingEnrollmentDTO> addCertificate(
            @Parameter(description = "Enrollment ID") @PathVariable String enrollmentId,
            @Valid @RequestBody CertificateDTO certificateDTO) {
        
        TrainingEnrollmentDTO updatedEnrollment = trainingEnrollmentService.addCertificate(enrollmentId, certificateDTO);
        return new ResponseEntity<>(updatedEnrollment, HttpStatus.OK);
    }
} 