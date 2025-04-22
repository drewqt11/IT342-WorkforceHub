package cit.edu.workforce.Controller;

import cit.edu.workforce.Entity.DepartmentEntity;
import cit.edu.workforce.Entity.JobTitleEntity;
import cit.edu.workforce.Service.DepartmentService;
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

@RestController
@RequestMapping("/api/hr/job-titles")
@Tag(name = "Job Title Management", description = "Job title management APIs")
@SecurityRequirement(name = "bearerAuth")
public class JobTitleController {

    private final JobTitleService jobTitleService;
    private final DepartmentService departmentService;

    @Autowired
    public JobTitleController(JobTitleService jobTitleService, DepartmentService departmentService) {
        this.jobTitleService = jobTitleService;
        this.departmentService = departmentService;
    }

    @GetMapping
    @Operation(summary = "Get all job titles", description = "Get a list of all job titles")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<JobTitleEntity>> getAllJobTitles() {
        return ResponseEntity.ok(jobTitleService.getAllJobTitles());
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get job titles by department ID", description = "Get a list of job titles for a specific department")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<JobTitleEntity>> getJobTitlesByDepartmentId(@PathVariable String departmentId) {
        return ResponseEntity.ok(jobTitleService.getJobTitlesByDepartmentId(departmentId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job title by ID", description = "Get a job title by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<JobTitleEntity> getJobTitleById(@PathVariable String id) {
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
            @RequestParam(required = false) String payGrade,
            @RequestParam String departmentId) {
        DepartmentEntity department = departmentService.getDepartmentById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        return new ResponseEntity<>(
                jobTitleService.createJobTitle(jobName, jobDescription, payGrade, department),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update job title", description = "Update an existing job title")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<JobTitleEntity> updateJobTitle(
            @PathVariable String id,
            @RequestParam String jobName,
            @RequestParam(required = false) String jobDescription,
            @RequestParam(required = false) String payGrade,
            @RequestParam String departmentId) {
        DepartmentEntity department = departmentService.getDepartmentById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        return ResponseEntity.ok(
                jobTitleService.updateJobTitle(id, jobName, jobDescription, payGrade, department));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete job title", description = "Delete a job title")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteJobTitle(@PathVariable String id) {
        jobTitleService.deleteJobTitle(id);
        return ResponseEntity.noContent().build();
    }
}