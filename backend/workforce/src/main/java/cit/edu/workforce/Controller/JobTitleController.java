package cit.edu.workforce.Controller;

import cit.edu.workforce.Entity.JobTitle;
import cit.edu.workforce.Repository.JobTitleRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@PreAuthorize("hasRole('HR_ADMIN')")
public class JobTitleController {

    private final JobTitleRepository jobTitleRepository;

    @Autowired
    public JobTitleController(JobTitleRepository jobTitleRepository) {
        this.jobTitleRepository = jobTitleRepository;
    }

    @GetMapping
    public ResponseEntity<List<JobTitle>> getAllJobTitles() {
        List<JobTitle> jobTitles = jobTitleRepository.findAll();
        return ResponseEntity.ok(jobTitles);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobTitle> getJobTitleById(@PathVariable UUID jobId) {
        JobTitle jobTitle = jobTitleRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job title not found"));
        return ResponseEntity.ok(jobTitle);
    }

    @PostMapping
    public ResponseEntity<JobTitle> createJobTitle(@Valid @RequestBody JobTitle jobTitle) {
        // Check if job name already exists
        if (jobTitleRepository.existsByJobName(jobTitle.getJobName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job title name already exists");
        }
        
        JobTitle savedJobTitle = jobTitleRepository.save(jobTitle);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedJobTitle);
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<JobTitle> updateJobTitle(
            @PathVariable UUID jobId,
            @Valid @RequestBody JobTitle jobTitleDetails) {
        
        JobTitle jobTitle = jobTitleRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job title not found"));
        
        // Check if new name already exists for another job title
        if (!jobTitle.getJobName().equals(jobTitleDetails.getJobName()) && 
                jobTitleRepository.existsByJobName(jobTitleDetails.getJobName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job title name already exists");
        }
        
        jobTitle.setJobName(jobTitleDetails.getJobName());
        jobTitle.setJobDescription(jobTitleDetails.getJobDescription());
        jobTitle.setPayGrade(jobTitleDetails.getPayGrade());
        
        JobTitle updatedJobTitle = jobTitleRepository.save(jobTitle);
        return ResponseEntity.ok(updatedJobTitle);
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> deleteJobTitle(@PathVariable UUID jobId) {
        JobTitle jobTitle = jobTitleRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job title not found"));
        
        // In a real application, we might want to check if there are employees with this job title
        // and handle that accordingly (e.g., prevent deletion or move employees to a default job title)
        
        jobTitleRepository.delete(jobTitle);
        return ResponseEntity.noContent().build();
    }
} 