package cit.edu.workforce.Controller;

import cit.edu.workforce.Entity.DepartmentEntity;
import cit.edu.workforce.Entity.JobTitleEntity;
import cit.edu.workforce.Service.DepartmentService;
import cit.edu.workforce.Service.JobTitleService;
import cit.edu.workforce.Service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * JobTitleController - Provides API endpoints for job title management
 * New file: Implements endpoints for creating, reading, updating and deleting job titles
 * 
 * This controller handles all job title-related operations including:
 * - Creating and updating job titles
 * - Retrieving job titles by ID or department
 * - Deleting job titles (admin only)
 */
@RestController
@RequestMapping("/api/hr/job-titles")
@Tag(name = "Job Title Management", description = "Job title management APIs")
@SecurityRequirement(name = "bearerAuth")
public class JobTitleController {

    private final JobTitleService jobTitleService;
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

    @Autowired
    public JobTitleController(JobTitleService jobTitleService, DepartmentService departmentService, EmployeeService employeeService) {
        this.jobTitleService = jobTitleService;
        this.departmentService = departmentService;
        this.employeeService = employeeService;
    }

    /**
     * Get all job titles
     * Retrieves a list of all available job titles in the system
     */
    @GetMapping
    @Operation(summary = "Get all job titles", description = "Get a list of all job titles")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<JobTitleEntity>> getAllJobTitles() {
        return ResponseEntity.ok(jobTitleService.getAllJobTitles());
    }

    /**
     * Get job titles by department
     * Retrieves job titles associated with a specific department
     */
    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get job titles by department ID", description = "Get a list of job titles for a specific department")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<JobTitleEntity>> getJobTitlesByDepartmentId(@PathVariable String departmentId) {
        return ResponseEntity.ok(jobTitleService.getJobTitlesByDepartmentId(departmentId));
    }

    /**
     * Get a specific job title by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get job title by ID", description = "Get a job title by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<JobTitleEntity> getJobTitleById(@PathVariable String id) {
        return jobTitleService.getJobTitleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new job title
     * Adds a new job position to a specific department with optional description and pay grade
     */
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

    /**
     * Update an existing job title
     * Modifies the details of an existing job title including name, description, 
     * pay grade, and department association
     */
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

    /**
     * Delete a job title
     * Removes a job title from the system (admin only)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete job title", description = "Delete a job title")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') ")
    public ResponseEntity<Void> deleteJobTitle(@PathVariable String id) {
        jobTitleService.deleteJobTitle(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/employees/{employeeId}")
    @Operation(summary = "Update employee job title", description = "Update an employee's job title")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Void> updateEmployeeJobTitle(
            @PathVariable String employeeId,
            @RequestParam String jobTitleId) {
        employeeService.assignJobTitle(employeeId, jobTitleId);
        return ResponseEntity.ok().build();
    }
}