package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.ApplicationRecordDTO;
import cit.edu.workforce.Service.ApplicationRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * ApplicationRecordController - Provides API endpoints for job application management
 * New file: This controller handles all job application-related operations including
 * creating applications and updating application status.
 */
@RestController
@RequestMapping("/api/applications")
@Tag(name = "Applications", description = "Application API - Manage job applications")
public class ApplicationRecordController {

    private final ApplicationRecordService applicationRecordService;

    @Autowired
    public ApplicationRecordController(ApplicationRecordService applicationRecordService) {
        this.applicationRecordService = applicationRecordService;
    }

    /**
     * Get all applications (HR/Admin only)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    @Operation(summary = "Get all applications", description = "Retrieve all job applications with pagination support (HR/Admin only)")
    public ResponseEntity<Page<ApplicationRecordDTO>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reviewedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        
        Page<ApplicationRecordDTO> applications = applicationRecordService.getAllApplications(pageable);
        return ResponseEntity.ok(applications);
    }

    /**
     * Get application by ID
     */
    @GetMapping("/{applicationId}")
    @Operation(summary = "Get application by ID", description = "Retrieve a specific job application by ID")
    public ResponseEntity<ApplicationRecordDTO> getApplicationById(
            @Parameter(description = "Application ID") @PathVariable String applicationId) {
        
        ApplicationRecordDTO application = applicationRecordService.getApplicationById(applicationId);
        return ResponseEntity.ok(application);
    }

    /**
     * Get applications by applicant
     */
    @GetMapping("/applicant/{applicantId}")
    @Operation(summary = "Get applications by applicant", description = "Retrieve job applications for a specific applicant with pagination support")
    public ResponseEntity<Page<ApplicationRecordDTO>> getApplicationsByApplicant(
            @Parameter(description = "Applicant ID") @PathVariable String applicantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reviewedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        
        Page<ApplicationRecordDTO> applications = applicationRecordService.getApplicationsByApplicant(applicantId, pageable);
        return ResponseEntity.ok(applications);
    }

    /**
     * Get applications by job listing (HR/Admin only)
     */
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    @Operation(summary = "Get applications by job listing", description = "Retrieve job applications for a specific job listing with pagination support (HR/Admin only)")
    public ResponseEntity<Page<ApplicationRecordDTO>> getApplicationsByJobListing(
            @Parameter(description = "Job ID") @PathVariable String jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reviewedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        
        Page<ApplicationRecordDTO> applications = applicationRecordService.getApplicationsByJobListing(jobId, pageable);
        return ResponseEntity.ok(applications);
    }

    /**
     * Get applications by status (HR/Admin only)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    @Operation(summary = "Get applications by status", description = "Retrieve job applications by status with pagination support (HR/Admin only)")
    public ResponseEntity<Page<ApplicationRecordDTO>> getApplicationsByStatus(
            @Parameter(description = "Status (PENDING, SHORTLISTED, REJECTED, HIRED)") @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reviewedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        
        Page<ApplicationRecordDTO> applications = applicationRecordService.getApplicationsByStatus(status, pageable);
        return ResponseEntity.ok(applications);
    }

    /**
     * Get current user's applications
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user's applications", description = "Retrieve the current user's job applications")
    public ResponseEntity<List<ApplicationRecordDTO>> getCurrentUserApplications() {
        List<ApplicationRecordDTO> applications = applicationRecordService.getCurrentUserApplications();
        return ResponseEntity.ok(applications);
    }

    /**
     * Get current user's applications with pagination
     */
    @GetMapping("/me/paged")
    @Operation(summary = "Get current user's applications with pagination", description = "Retrieve the current user's job applications with pagination support")
    public ResponseEntity<Page<ApplicationRecordDTO>> getCurrentUserApplicationsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reviewedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        
        Page<ApplicationRecordDTO> applications = applicationRecordService.getCurrentUserApplications(pageable);
        return ResponseEntity.ok(applications);
    }

    /**
     * Create a new application
     */
    @PostMapping
    @Operation(summary = "Create application", description = "Create a new job application")
    public ResponseEntity<ApplicationRecordDTO> createApplication(
            @Parameter(description = "Applicant ID") @RequestParam String applicantId,
            @Parameter(description = "Job ID") @RequestParam String jobId) {
        
        ApplicationRecordDTO createdApplication = applicationRecordService.createApplication(applicantId, jobId);
        return new ResponseEntity<>(createdApplication, HttpStatus.CREATED);
    }

    /**
     * Update application status (HR/Admin only)
     */
    @PutMapping("/{applicationId}/status")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    @Operation(summary = "Update application status", description = "Update the status of a job application (HR/Admin only)")
    public ResponseEntity<ApplicationRecordDTO> updateApplicationStatus(
            @Parameter(description = "Application ID") @PathVariable String applicationId,
            @Parameter(description = "Status (PENDING, SHORTLISTED, REJECTED, HIRED)") @RequestParam String status,
            @Parameter(description = "Remarks") @RequestParam(required = false) String remarks) {
        
        ApplicationRecordDTO updatedApplication = applicationRecordService.updateApplicationStatus(
                applicationId, status, remarks);
        
        return ResponseEntity.ok(updatedApplication);
    }
} 