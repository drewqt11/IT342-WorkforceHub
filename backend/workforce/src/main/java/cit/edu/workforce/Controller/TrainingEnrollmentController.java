package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.TrainingEnrollmentDTO;
import cit.edu.workforce.Service.TrainingEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/training/enrollments")
@Tag(name = "Training Enrollment Management", description = "APIs for managing employee training enrollments")
@SecurityRequirement(name = "bearerAuth")
public class TrainingEnrollmentController {

    private final TrainingEnrollmentService trainingEnrollmentService;

    @Autowired
    public TrainingEnrollmentController(TrainingEnrollmentService trainingEnrollmentService) {
        this.trainingEnrollmentService = trainingEnrollmentService;
    }

    @GetMapping
    @Operation(summary = "Get all training enrollments", description = "Get a paginated list of all training enrollments")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<TrainingEnrollmentDTO>> getAllTrainingEnrollments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingEnrollmentService.getAllTrainingEnrollments(pageable));
    }

    @GetMapping("/{enrollmentId}")
    @Operation(summary = "Get enrollment by ID", description = "Get details of a specific training enrollment")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<TrainingEnrollmentDTO> getTrainingEnrollmentById(@PathVariable String enrollmentId) {
        return trainingEnrollmentService.getTrainingEnrollmentById(enrollmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get enrollments by employee", description = "Get training enrollments for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<TrainingEnrollmentDTO>> getEnrollmentsByEmployee(
            @PathVariable String employeeId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingEnrollmentService.getEnrollmentsByEmployee(employeeId, pageable));
    }

    @GetMapping("/program/{programId}")
    @Operation(summary = "Get enrollments by program", description = "Get enrollments for a specific training program")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Page<TrainingEnrollmentDTO>> getEnrollmentsByProgram(
            @PathVariable String programId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingEnrollmentService.getEnrollmentsByProgram(programId, pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get enrollments by status", description = "Get enrollments with a specific status")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Page<TrainingEnrollmentDTO>> getEnrollmentsByStatus(
            @PathVariable String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingEnrollmentService.getEnrollmentsByStatus(status, pageable));
    }

    @GetMapping("/employee/{employeeId}/status/{status}")
    @Operation(summary = "Get enrollments by employee and status", description = "Get enrollments for a specific employee with a specific status")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<TrainingEnrollmentDTO>> getEnrollmentsByEmployeeAndStatus(
            @PathVariable String employeeId,
            @PathVariable String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingEnrollmentService.getEnrollmentsByEmployeeAndStatus(employeeId, status, pageable));
    }

    @GetMapping("/program/{programId}/status/{status}")
    @Operation(summary = "Get enrollments by program and status", description = "Get enrollments for a specific program with a specific status")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Page<TrainingEnrollmentDTO>> getEnrollmentsByProgramAndStatus(
            @PathVariable String programId,
            @PathVariable String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingEnrollmentService.getEnrollmentsByProgramAndStatus(programId, status, pageable));
    }

    @GetMapping("/approaching-due")
    @Operation(summary = "Get enrollments approaching due date", description = "Get enrollments approaching their due date")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<List<TrainingEnrollmentDTO>> getEnrollmentsApproachingDueDate(
            @Parameter(description = "Reference date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate) {
        
        return ResponseEntity.ok(trainingEnrollmentService.getEnrollmentsApproachingDueDate(referenceDate));
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed enrollments", description = "Get enrollments completed within a date range")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<List<TrainingEnrollmentDTO>> getCompletedEnrollmentsInDateRange(
            @Parameter(description = "Start date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        return ResponseEntity.ok(trainingEnrollmentService.getCompletedEnrollmentsInDateRange(startDate, endDate));
    }

    @GetMapping("/assigned-by/{managerId}")
    @Operation(summary = "Get enrollments by manager", description = "Get enrollments assigned by a specific manager")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<List<TrainingEnrollmentDTO>> getEnrollmentsByAssignedManager(@PathVariable String managerId) {
        return ResponseEntity.ok(trainingEnrollmentService.getEnrollmentsByAssignedManager(managerId));
    }

    @GetMapping("/employee/{employeeId}/expiring-certifications")
    @Operation(summary = "Get expiring certifications", description = "Get expiring certifications for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<List<TrainingEnrollmentDTO>> getExpiringCertifications(
            @PathVariable String employeeId,
            @Parameter(description = "Reference date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate) {
        
        return ResponseEntity.ok(trainingEnrollmentService.getExpiringCertifications(employeeId, referenceDate));
    }

    @PostMapping
    @Operation(summary = "Create training enrollment", description = "Enroll an employee in a training program")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<TrainingEnrollmentDTO> createTrainingEnrollment(
            @Parameter(description = "Employee ID") @RequestParam String employeeId,
            @Parameter(description = "Program ID") @RequestParam String programId,
            @Parameter(description = "Assigned by (manager ID)") @RequestParam(required = false) String assignedById) {
        
        TrainingEnrollmentDTO enrollment = trainingEnrollmentService.createTrainingEnrollment(employeeId, programId, assignedById);
        return new ResponseEntity<>(enrollment, HttpStatus.CREATED);
    }

    @PostMapping("/assign")
    @Operation(summary = "Assign training", description = "Assign a training program to an employee (manager only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<TrainingEnrollmentDTO> assignTrainingToEmployee(
            @Parameter(description = "Employee ID") @RequestParam String employeeId,
            @Parameter(description = "Program ID") @RequestParam String programId,
            @Parameter(description = "Manager ID") @RequestParam String managerId,
            @Parameter(description = "Due date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        
        TrainingEnrollmentDTO enrollment = trainingEnrollmentService.assignTrainingToEmployee(employeeId, programId, managerId, dueDate);
        return new ResponseEntity<>(enrollment, HttpStatus.CREATED);
    }

    @PutMapping("/{enrollmentId}/progress")
    @Operation(summary = "Update enrollment progress", description = "Update the progress of a training enrollment")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<TrainingEnrollmentDTO> updateEnrollmentProgress(
            @PathVariable String enrollmentId,
            @Parameter(description = "Completion percentage") @RequestParam(required = false) Integer completionPercentage,
            @Parameter(description = "Status") @RequestParam(required = false) String status,
            @Parameter(description = "Feedback") @RequestParam(required = false) String feedback) {
        
        TrainingEnrollmentDTO enrollment = trainingEnrollmentService.updateEnrollmentProgress(
                enrollmentId, completionPercentage, status, feedback);
        return ResponseEntity.ok(enrollment);
    }

    @PutMapping("/{enrollmentId}/complete")
    @Operation(summary = "Complete enrollment", description = "Mark a training enrollment as completed with assessment")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<TrainingEnrollmentDTO> completeEnrollment(
            @PathVariable String enrollmentId,
            @Parameter(description = "Score") @RequestParam(required = false) Double score,
            @Parameter(description = "Instructor comments") @RequestParam(required = false) String instructorComments,
            @Parameter(description = "Certificate URL") @RequestParam(required = false) String certificateUrl) {
        
        TrainingEnrollmentDTO enrollment = trainingEnrollmentService.completeEnrollment(
                enrollmentId, score, instructorComments, certificateUrl);
        return ResponseEntity.ok(enrollment);
    }

    @PutMapping("/{enrollmentId}")
    @Operation(summary = "Update enrollment", description = "Update details of a training enrollment")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<TrainingEnrollmentDTO> updateEnrollment(
            @PathVariable String enrollmentId,
            @Parameter(description = "Due date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @Parameter(description = "Status") @RequestParam(required = false) String status,
            @Parameter(description = "Completion percentage") @RequestParam(required = false) Integer completionPercentage,
            @Parameter(description = "Score") @RequestParam(required = false) Double score,
            @Parameter(description = "Feedback") @RequestParam(required = false) String feedback,
            @Parameter(description = "Instructor comments") @RequestParam(required = false) String instructorComments,
            @Parameter(description = "Certificate URL") @RequestParam(required = false) String certificateUrl,
            @Parameter(description = "Certificate expiry date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate certificateExpiryDate) {
        
        TrainingEnrollmentDTO enrollment = trainingEnrollmentService.updateEnrollment(
                enrollmentId, dueDate, status, completionPercentage, score, feedback, 
                instructorComments, certificateUrl, certificateExpiryDate);
        return ResponseEntity.ok(enrollment);
    }

    @PutMapping("/{enrollmentId}/cancel")
    @Operation(summary = "Cancel enrollment", description = "Cancel a training enrollment (for enrolled or in-progress only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<TrainingEnrollmentDTO> cancelEnrollment(
            @PathVariable String enrollmentId,
            @Parameter(description = "Reason for cancellation") @RequestParam(required = false) String reason) {
        
        TrainingEnrollmentDTO enrollment = trainingEnrollmentService.cancelEnrollment(enrollmentId, reason);
        return ResponseEntity.ok(enrollment);
    }

    @DeleteMapping("/{enrollmentId}")
    @Operation(summary = "Delete enrollment", description = "Delete a training enrollment")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable String enrollmentId) {
        trainingEnrollmentService.deleteEnrollment(enrollmentId);
        return ResponseEntity.noContent().build();
    }
}

// New file: Controller for managing training enrollments in the Training & Development module
// Provides API endpoints for enrolling employees in training programs, tracking progress, and managing certifications 