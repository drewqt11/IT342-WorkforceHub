package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.JobPostingDTO;
import cit.edu.workforce.Service.JobPostingService;
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
@RequestMapping("/api/recruitment/job-postings")
@Tag(name = "Job Posting Management", description = "APIs for managing job postings")
@SecurityRequirement(name = "bearerAuth")
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @Autowired
    public JobPostingController(JobPostingService jobPostingService) {
        this.jobPostingService = jobPostingService;
    }

    @GetMapping
    @Operation(summary = "Get all job postings", description = "Get a paginated list of all job postings")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<JobPostingDTO>> getAllJobPostings(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "postingDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        return ResponseEntity.ok(jobPostingService.getAllJobPostings(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job posting by ID", description = "Get a job posting by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<JobPostingDTO> getJobPostingById(@PathVariable String id) {
        return jobPostingService.getJobPostingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    @Operation(summary = "Get active job postings", description = "Get a paginated list of active job postings")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<JobPostingDTO>> getActiveJobPostings(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "postingDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        return ResponseEntity.ok(jobPostingService.getActiveJobPostings(pageable));
    }

    @GetMapping("/internal")
    @Operation(summary = "Get internal job postings", description = "Get a paginated list of internal job postings")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<JobPostingDTO>> getInternalJobPostings(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "postingDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        return ResponseEntity.ok(jobPostingService.getInternalJobPostings(pageable));
    }

    @GetMapping("/external")
    @Operation(summary = "Get external job postings", description = "Get a paginated list of external job postings")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<JobPostingDTO>> getExternalJobPostings(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "postingDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        return ResponseEntity.ok(jobPostingService.getExternalJobPostings(pageable));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get job postings by department", description = "Get job postings for a specific department")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<JobPostingDTO>> getJobPostingsByDepartment(
            @PathVariable String departmentId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(jobPostingService.getJobPostingsByDepartment(departmentId, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search job postings", description = "Search job postings by title or description")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<JobPostingDTO>> searchJobPostings(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(jobPostingService.searchJobPostings(query, pageable));
    }

    @PostMapping
    @Operation(summary = "Create job posting", description = "Create a new job posting")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<JobPostingDTO> createJobPosting(
            @Parameter(description = "Job title") @RequestParam String title,
            @Parameter(description = "Job description") @RequestParam String description,
            @Parameter(description = "Job qualifications") @RequestParam(required = false) String qualifications,
            @Parameter(description = "Job responsibilities") @RequestParam(required = false) String responsibilities,
            @Parameter(description = "Department ID") @RequestParam(required = false) String departmentId,
            @Parameter(description = "Job title ID") @RequestParam(required = false) String jobTitleId,
            @Parameter(description = "Job location") @RequestParam(required = false) String location,
            @Parameter(description = "Employment type (FULL_TIME, PART_TIME, CONTRACT, INTERN)") 
                @RequestParam(required = false) String employmentType,
            @Parameter(description = "Experience level (ENTRY, MID, SENIOR, EXECUTIVE)") 
                @RequestParam(required = false) String experienceLevel,
            @Parameter(description = "Minimum salary") @RequestParam(required = false) Double salaryMin,
            @Parameter(description = "Maximum salary") @RequestParam(required = false) Double salaryMax,
            @Parameter(description = "Closing date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closingDate,
            @Parameter(description = "Is internal posting") @RequestParam(required = false) Boolean isInternal,
            @Parameter(description = "External URL") @RequestParam(required = false) String externalUrl,
            @Parameter(description = "Posted by employee ID") @RequestParam(required = false) String postedById) {

        JobPostingDTO jobPosting = jobPostingService.createJobPosting(
                title, description, qualifications, responsibilities, departmentId, jobTitleId,
                location, employmentType, experienceLevel, salaryMin, salaryMax, closingDate,
                isInternal, externalUrl, postedById);
        
        return new ResponseEntity<>(jobPosting, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update job posting", description = "Update an existing job posting")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<JobPostingDTO> updateJobPosting(
            @PathVariable String id,
            @Parameter(description = "Job title") @RequestParam(required = false) String title,
            @Parameter(description = "Job description") @RequestParam(required = false) String description,
            @Parameter(description = "Job qualifications") @RequestParam(required = false) String qualifications,
            @Parameter(description = "Job responsibilities") @RequestParam(required = false) String responsibilities,
            @Parameter(description = "Department ID") @RequestParam(required = false) String departmentId,
            @Parameter(description = "Job title ID") @RequestParam(required = false) String jobTitleId,
            @Parameter(description = "Job location") @RequestParam(required = false) String location,
            @Parameter(description = "Employment type (FULL_TIME, PART_TIME, CONTRACT, INTERN)") 
                @RequestParam(required = false) String employmentType,
            @Parameter(description = "Experience level (ENTRY, MID, SENIOR, EXECUTIVE)") 
                @RequestParam(required = false) String experienceLevel,
            @Parameter(description = "Minimum salary") @RequestParam(required = false) Double salaryMin,
            @Parameter(description = "Maximum salary") @RequestParam(required = false) Double salaryMax,
            @Parameter(description = "Closing date") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closingDate,
            @Parameter(description = "Is internal posting") @RequestParam(required = false) Boolean isInternal,
            @Parameter(description = "Is active") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "External URL") @RequestParam(required = false) String externalUrl) {

        JobPostingDTO jobPosting = jobPostingService.updateJobPosting(
                id, title, description, qualifications, responsibilities, departmentId, jobTitleId,
                location, employmentType, experienceLevel, salaryMin, salaryMax, closingDate,
                isInternal, isActive, externalUrl);
        
        return ResponseEntity.ok(jobPosting);
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate job posting", description = "Deactivate a job posting")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deactivateJobPosting(@PathVariable String id) {
        jobPostingService.deactivateJobPosting(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate job posting", description = "Activate a job posting")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Void> activateJobPosting(@PathVariable String id) {
        jobPostingService.activateJobPosting(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete job posting", description = "Delete a job posting")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteJobPosting(@PathVariable String id) {
        jobPostingService.deleteJobPosting(id);
        return ResponseEntity.noContent().build();
    }
}

// New file: Controller for managing job postings in the recruitment module
// Provides API endpoints for creating, updating, and searching job postings 