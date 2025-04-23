package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.JobApplicationDTO;
import cit.edu.workforce.Service.JobApplicationService;
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
@RequestMapping("/api/recruitment/applications")
@Tag(name = "Job Application Management", description = "APIs for managing job applications")
@SecurityRequirement(name = "bearerAuth")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @Autowired
    public JobApplicationController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @GetMapping
    @Operation(summary = "Get all job applications", description = "Get a paginated list of all job applications")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<JobApplicationDTO>> getAllJobApplications(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "applicationDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        return ResponseEntity.ok(jobApplicationService.getAllJobApplications(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job application by ID", description = "Get a job application by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<JobApplicationDTO> getJobApplicationById(@PathVariable String id) {
        return jobApplicationService.getJobApplicationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/posting/{postingId}")
    @Operation(summary = "Get applications by job posting", description = "Get applications for a specific job posting")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<JobApplicationDTO>> getApplicationsByJobPosting(
            @PathVariable String postingId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(jobApplicationService.getJobApplicationsByJobPosting(postingId, pageable));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get applications by employee", description = "Get applications submitted by a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<List<JobApplicationDTO>> getApplicationsByEmployee(@PathVariable String employeeId) {
        // For employee role, check if requesting own applications
        // This would typically be done with a custom method or in the service layer
        return ResponseEntity.ok(jobApplicationService.getJobApplicationsByEmployee(employeeId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get applications by status", description = "Get applications with a specific status")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<JobApplicationDTO>> getApplicationsByStatus(
            @PathVariable String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(jobApplicationService.getJobApplicationsByStatus(status, pageable));
    }

    @GetMapping("/stage/{stage}")
    @Operation(summary = "Get applications by stage", description = "Get applications at a specific stage in the recruitment process")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<JobApplicationDTO>> getApplicationsByStage(
            @PathVariable String stage,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(jobApplicationService.getJobApplicationsByStage(stage, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search job applications", description = "Search job applications by applicant name or email")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<JobApplicationDTO>> searchJobApplications(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(jobApplicationService.searchJobApplications(query, pageable));
    }

    @PostMapping("/internal")
    @Operation(summary = "Create internal job application", description = "Submit an internal job application")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<JobApplicationDTO> createInternalJobApplication(
            @Parameter(description = "Job posting ID") @RequestParam String postingId,
            @Parameter(description = "Employee ID") @RequestParam String employeeId,
            @Parameter(description = "Cover letter URL") @RequestParam(required = false) String coverLetterUrl) {

        JobApplicationDTO application = jobApplicationService.createInternalJobApplication(
                postingId, employeeId, coverLetterUrl);
        
        return new ResponseEntity<>(application, HttpStatus.CREATED);
    }

    @PostMapping("/external")
    @Operation(summary = "Create external job application", description = "Submit an external job application")
    public ResponseEntity<JobApplicationDTO> createExternalJobApplication(
            @Parameter(description = "Job posting ID") @RequestParam String postingId,
            @Parameter(description = "Applicant name") @RequestParam String applicantName,
            @Parameter(description = "Applicant email") @RequestParam String applicantEmail,
            @Parameter(description = "Applicant phone") @RequestParam(required = false) String applicantPhone,
            @Parameter(description = "Resume URL") @RequestParam(required = false) String resumeUrl,
            @Parameter(description = "Cover letter URL") @RequestParam(required = false) String coverLetterUrl,
            @Parameter(description = "Application source") @RequestParam(required = false) String source) {

        JobApplicationDTO application = jobApplicationService.createExternalJobApplication(
                postingId, applicantName, applicantEmail, applicantPhone, resumeUrl, coverLetterUrl, source);
        
        return new ResponseEntity<>(application, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update application status", description = "Update the status of a job application")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<JobApplicationDTO> updateApplicationStatus(
            @PathVariable String id,
            @Parameter(description = "Application status") @RequestParam String status,
            @Parameter(description = "Current stage") @RequestParam(required = false) String currentStage,
            @Parameter(description = "Internal notes") @RequestParam(required = false) String internalNotes,
            @Parameter(description = "Reviewed by employee ID") @RequestParam String reviewedById) {

        JobApplicationDTO application = jobApplicationService.updateApplicationStatus(
                id, status, currentStage, internalNotes, reviewedById);
        
        return ResponseEntity.ok(application);
    }

    @PutMapping("/{id}/schedule-interview")
    @Operation(summary = "Schedule interview", description = "Schedule an interview for a job application")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<JobApplicationDTO> scheduleInterview(
            @PathVariable String id,
            @Parameter(description = "Interview date") 
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate interviewDate) {

        JobApplicationDTO application = jobApplicationService.scheduleInterview(id, interviewDate);
        
        return ResponseEntity.ok(application);
    }

    @PutMapping("/{id}/interview-feedback")
    @Operation(summary = "Add interview feedback", description = "Add feedback after an interview")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<JobApplicationDTO> addInterviewFeedback(
            @PathVariable String id,
            @Parameter(description = "Interview feedback") @RequestParam String interviewFeedback,
            @Parameter(description = "Reviewed by employee ID") @RequestParam String reviewedById) {

        JobApplicationDTO application = jobApplicationService.addInterviewFeedback(
                id, interviewFeedback, reviewedById);
        
        return ResponseEntity.ok(application);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete job application", description = "Delete a job application")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteJobApplication(@PathVariable String id) {
        jobApplicationService.deleteJobApplication(id);
        return ResponseEntity.noContent().build();
    }
}

// New file: Controller for managing job applications in the recruitment module
// Provides API endpoints for creating and tracking job applications 