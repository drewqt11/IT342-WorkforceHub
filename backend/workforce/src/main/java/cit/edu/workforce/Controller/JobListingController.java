package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.JobListingDTO;
import cit.edu.workforce.Service.JobListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

/**
 * JobListingController - Provides API endpoints for job listing management
 * New file: This controller handles all job listing-related operations including
 * creating, reading, updating, and deleting job listings.
 */
@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Job Listings", description = "Job Listing API - Manage job postings")
public class JobListingController {

    private final JobListingService jobListingService;

    @Autowired
    public JobListingController(JobListingService jobListingService) {
        this.jobListingService = jobListingService;
    }

    /**
     * Get all job listings
     */
    @GetMapping
    @Operation(summary = "Get all job listings", description = "Retrieve all job listings with pagination support")
    public ResponseEntity<Page<JobListingDTO>> getAllJobListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "datePosted") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        
        Page<JobListingDTO> jobListings = jobListingService.getAllJobListings(pageable);
        return ResponseEntity.ok(jobListings);
    }

    /**
     * Get all active job listings
     */
    @GetMapping("/active")
    @Operation(summary = "Get active job listings", description = "Retrieve all active job listings with pagination support")
    public ResponseEntity<Page<JobListingDTO>> getActiveJobListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "datePosted") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        
        Page<JobListingDTO> jobListings = jobListingService.getActiveJobListings(pageable);
        return ResponseEntity.ok(jobListings);
    }

    /**
     * Get a job listing by ID
     */
    @GetMapping("/{jobId}")
    @Operation(summary = "Get job listing by ID", description = "Retrieve a specific job listing by its ID")
    public ResponseEntity<JobListingDTO> getJobListingById(
            @Parameter(description = "Job ID") @PathVariable String jobId) {
        
        JobListingDTO jobListing = jobListingService.getJobListingById(jobId);
        return ResponseEntity.ok(jobListing);
    }

    /**
     * Get job listings by department
     */
    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get job listings by department", description = "Retrieve job listings for a specific department with pagination support")
    public ResponseEntity<Page<JobListingDTO>> getJobListingsByDepartment(
            @Parameter(description = "Department ID") @PathVariable String departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "datePosted") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        
        Page<JobListingDTO> jobListings = jobListingService.getJobListingsByDepartment(departmentId, pageable);
        return ResponseEntity.ok(jobListings);
    }

    /**
     * Get active job listings by department
     */
    @GetMapping("/department/{departmentId}/active")
    @Operation(summary = "Get active job listings by department", description = "Retrieve active job listings for a specific department with pagination support")
    public ResponseEntity<Page<JobListingDTO>> getActiveJobListingsByDepartment(
            @Parameter(description = "Department ID") @PathVariable String departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "datePosted") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        
        Page<JobListingDTO> jobListings = jobListingService.getActiveJobListingsByDepartment(departmentId, pageable);
        return ResponseEntity.ok(jobListings);
    }

    /**
     * Get job listings by job type (Internal, External)
     */
    @GetMapping("/type/{jobType}")
    @Operation(summary = "Get job listings by type", description = "Retrieve job listings by job type (Internal, External) with pagination support")
    public ResponseEntity<Page<JobListingDTO>> getJobListingsByJobType(
            @Parameter(description = "Job Type (INTERNAL, EXTERNAL, BOTH)") @PathVariable String jobType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "datePosted") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        
        Page<JobListingDTO> jobListings = jobListingService.getJobListingsByJobType(jobType, pageable);
        return ResponseEntity.ok(jobListings);
    }

    /**
     * Get active job listings by job type
     */
    @GetMapping("/type/{jobType}/active")
    @Operation(summary = "Get active job listings by type", description = "Retrieve active job listings by job type (Internal, External) with pagination support")
    public ResponseEntity<Page<JobListingDTO>> getActiveJobListingsByJobType(
            @Parameter(description = "Job Type (INTERNAL, EXTERNAL, BOTH)") @PathVariable String jobType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "datePosted") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        
        Page<JobListingDTO> jobListings = jobListingService.getActiveJobListingsByJobType(jobType, pageable);
        return ResponseEntity.ok(jobListings);
    }

    /**
     * Search job listings
     */
    @GetMapping("/search")
    @Operation(summary = "Search job listings", description = "Search for job listings by title or description with pagination support")
    public ResponseEntity<Page<JobListingDTO>> searchJobListings(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "datePosted") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        
        Page<JobListingDTO> jobListings = jobListingService.searchJobListings(searchTerm, pageable);
        return ResponseEntity.ok(jobListings);
    }

    /**
     * Create a new job listing (HR/Admin only)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    @Operation(summary = "Create job listing", description = "Create a new job listing (HR/Admin only)")
    public ResponseEntity<JobListingDTO> createJobListing(
            @Parameter(description = "Job title") @RequestParam String title,
            @Parameter(description = "Department ID") @RequestParam String departmentId,
            @Parameter(description = "Job description") @RequestParam(required = false) String jobDescription,
            @Parameter(description = "Qualifications") @RequestParam(required = false) String qualifications,
            @Parameter(description = "Employment type (FULL_TIME, PART_TIME, CONTRACT)") @RequestParam String employmentType,
            @Parameter(description = "Job type (INTERNAL, EXTERNAL, BOTH)") @RequestParam String jobType,
            @Parameter(description = "Application deadline") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate applicationDeadline) {
        
        JobListingDTO createdJobListing = jobListingService.createJobListing(
                title, departmentId, jobDescription, qualifications, employmentType, jobType, applicationDeadline);
        
        return new ResponseEntity<>(createdJobListing, HttpStatus.CREATED);
    }

    /**
     * Update a job listing (HR/Admin only)
     */
    @PutMapping("/{jobId}")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    @Operation(summary = "Update job listing", description = "Update an existing job listing (HR/Admin only)")
    public ResponseEntity<JobListingDTO> updateJobListing(
            @Parameter(description = "Job ID") @PathVariable String jobId,
            @Parameter(description = "Job title") @RequestParam(required = false) String title,
            @Parameter(description = "Department ID") @RequestParam(required = false) String departmentId,
            @Parameter(description = "Job description") @RequestParam(required = false) String jobDescription,
            @Parameter(description = "Qualifications") @RequestParam(required = false) String qualifications,
            @Parameter(description = "Employment type (FULL_TIME, PART_TIME, CONTRACT)") @RequestParam(required = false) String employmentType,
            @Parameter(description = "Job type (INTERNAL, EXTERNAL, BOTH)") @RequestParam(required = false) String jobType,
            @Parameter(description = "Application deadline") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate applicationDeadline,
            @Parameter(description = "Is active") @RequestParam(required = false) Boolean isActive) {
        
        JobListingDTO updatedJobListing = jobListingService.updateJobListing(
                jobId, title, departmentId, jobDescription, qualifications, employmentType, jobType, applicationDeadline, isActive);
        
        return ResponseEntity.ok(updatedJobListing);
    }

    /**
     * Deactivate a job listing (HR/Admin only)
     */
    @PutMapping("/{jobId}/deactivate")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    @Operation(summary = "Deactivate job listing", description = "Deactivate an existing job listing (HR/Admin only)")
    public ResponseEntity<JobListingDTO> deactivateJobListing(
            @Parameter(description = "Job ID") @PathVariable String jobId) {
        
        JobListingDTO deactivatedJobListing = jobListingService.deactivateJobListing(jobId);
        return ResponseEntity.ok(deactivatedJobListing);
    }

    /**
     * Activate a job listing (HR/Admin only)
     */
    @PutMapping("/{jobId}/activate")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    @Operation(summary = "Activate job listing", description = "Activate an existing job listing (HR/Admin only)")
    public ResponseEntity<JobListingDTO> activateJobListing(
            @Parameter(description = "Job ID") @PathVariable String jobId) {
        
        JobListingDTO activatedJobListing = jobListingService.activateJobListing(jobId);
        return ResponseEntity.ok(activatedJobListing);
    }

    /**
     * Delete a job listing (HR/Admin only)
     */
    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    @Operation(summary = "Delete job listing", description = "Delete an existing job listing (HR/Admin only)")
    public ResponseEntity<Void> deleteJobListing(
            @Parameter(description = "Job ID") @PathVariable String jobId) {
        
        jobListingService.deleteJobListing(jobId);
        return ResponseEntity.noContent().build();
    }
} 