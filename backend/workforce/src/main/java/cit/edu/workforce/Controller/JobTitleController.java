package cit.edu.workforce.Controller;

import cit.edu.workforce.Entity.JobTitleEntity;
import cit.edu.workforce.Service.JobTitleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hr/job-titles")
@Tag(name = "Job Title Management", description = "Job title management APIs")
@SecurityRequirement(name = "bearerAuth")
public class JobTitleController {

    private final JobTitleService jobTitleService;

    @Autowired
    public JobTitleController(JobTitleService jobTitleService) {
        this.jobTitleService = jobTitleService;
    }

    @GetMapping
    @Operation(summary = "Get all job titles", description = "Get a list of all job titles")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<JobTitleEntity>> getAllJobTitles() {
        return ResponseEntity.ok(jobTitleService.getAllJobTitles());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job title by ID", description = "Get a job title by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<JobTitleEntity> getJobTitleById(@PathVariable UUID id) {
        return jobTitleService.getJobTitleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create job title", description = "Create a new job title")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<JobTitleEntity> createJobTitle(
            @RequestParam String jobName,
            @RequestParam(required = false) String jobDescription,
            @RequestParam(required = false) String payGrade) {
        return new ResponseEntity<>(
                jobTitleService.createJobTitle(jobName, jobDescription, payGrade),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update job title", description = "Update an existing job title")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<JobTitleEntity> updateJobTitle(
            @PathVariable UUID id,
            @RequestParam String jobName,
            @RequestParam(required = false) String jobDescription,
            @RequestParam(required = false) String payGrade) {
        return ResponseEntity.ok(
                jobTitleService.updateJobTitle(id, jobName, jobDescription, payGrade));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete job title", description = "Delete a job title")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteJobTitle(@PathVariable UUID id) {
        jobTitleService.deleteJobTitle(id);
        return ResponseEntity.noContent().build();
    }
} 