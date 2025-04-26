package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.JobListingDTO;
import cit.edu.workforce.Entity.DepartmentEntity;
import cit.edu.workforce.Entity.JobListingEntity;
import cit.edu.workforce.Repository.ApplicationRecordRepository;
import cit.edu.workforce.Repository.DepartmentRepository;
import cit.edu.workforce.Repository.JobListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JobListingService - Service for managing job listings
 * New file: This service provides methods to manage job listings including CRUD operations and search functionality
 */
@Service
public class JobListingService {

    private final JobListingRepository jobListingRepository;
    private final DepartmentRepository departmentRepository;
    private final ApplicationRecordRepository applicationRecordRepository;

    @Autowired
    public JobListingService(
            JobListingRepository jobListingRepository,
            DepartmentRepository departmentRepository,
            ApplicationRecordRepository applicationRecordRepository) {
        this.jobListingRepository = jobListingRepository;
        this.departmentRepository = departmentRepository;
        this.applicationRecordRepository = applicationRecordRepository;
    }

    /**
     * Get all job listings
     *
     * @return List of job listing DTOs
     */
    @Transactional(readOnly = true)
    public List<JobListingDTO> getAllJobListings() {
        return jobListingRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated job listings
     *
     * @param pageable Pagination information
     * @return Page of job listing DTOs
     */
    @Transactional(readOnly = true)
    public Page<JobListingDTO> getAllJobListings(Pageable pageable) {
        return jobListingRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get all active job listings
     *
     * @return List of active job listing DTOs
     */
    @Transactional(readOnly = true)
    public List<JobListingDTO> getActiveJobListings() {
        return jobListingRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated active job listings
     *
     * @param pageable Pagination information
     * @return Page of active job listing DTOs
     */
    @Transactional(readOnly = true)
    public Page<JobListingDTO> getActiveJobListings(Pageable pageable) {
        return jobListingRepository.findByIsActiveTrue(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get job listing by ID
     *
     * @param jobId Job listing ID
     * @return Job listing DTO
     */
    @Transactional(readOnly = true)
    public JobListingDTO getJobListingById(String jobId) {
        JobListingEntity jobListing = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Job listing not found with ID: " + jobId));

        return convertToDTO(jobListing);
    }

    /**
     * Get job listings by department
     *
     * @param departmentId Department ID
     * @return List of job listing DTOs
     */
    @Transactional(readOnly = true)
    public List<JobListingDTO> getJobListingsByDepartment(String departmentId) {
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Department not found with ID: " + departmentId));

        return jobListingRepository.findByDepartment(department).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated job listings by department
     *
     * @param departmentId Department ID
     * @param pageable     Pagination information
     * @return Page of job listing DTOs
     */
    @Transactional(readOnly = true)
    public Page<JobListingDTO> getJobListingsByDepartment(String departmentId, Pageable pageable) {
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Department not found with ID: " + departmentId));

        return jobListingRepository.findByDepartment(department, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get active job listings by department
     *
     * @param departmentId Department ID
     * @return List of active job listing DTOs
     */
    @Transactional(readOnly = true)
    public List<JobListingDTO> getActiveJobListingsByDepartment(String departmentId) {
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Department not found with ID: " + departmentId));

        return jobListingRepository.findByDepartmentAndIsActiveTrue(department).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated active job listings by department
     *
     * @param departmentId Department ID
     * @param pageable     Pagination information
     * @return Page of active job listing DTOs
     */
    @Transactional(readOnly = true)
    public Page<JobListingDTO> getActiveJobListingsByDepartment(String departmentId, Pageable pageable) {
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Department not found with ID: " + departmentId));

        return jobListingRepository.findByDepartmentAndIsActiveTrue(department, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get job listings by job type (Internal, External)
     *
     * @param jobType Job type
     * @return List of job listing DTOs
     */
    @Transactional(readOnly = true)
    public List<JobListingDTO> getJobListingsByJobType(String jobType) {
        validateJobType(jobType);
        return jobListingRepository.findByJobType(jobType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated job listings by job type
     *
     * @param jobType   Job type
     * @param pageable  Pagination information
     * @return Page of job listing DTOs
     */
    @Transactional(readOnly = true)
    public Page<JobListingDTO> getJobListingsByJobType(String jobType, Pageable pageable) {
        validateJobType(jobType);
        return jobListingRepository.findByJobType(jobType, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get active job listings by job type
     *
     * @param jobType Job type
     * @return List of active job listing DTOs
     */
    @Transactional(readOnly = true)
    public List<JobListingDTO> getActiveJobListingsByJobType(String jobType) {
        validateJobType(jobType);
        return jobListingRepository.findByJobTypeAndIsActiveTrue(jobType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated active job listings by job type
     *
     * @param jobType   Job type
     * @param pageable  Pagination information
     * @return Page of active job listing DTOs
     */
    @Transactional(readOnly = true)
    public Page<JobListingDTO> getActiveJobListingsByJobType(String jobType, Pageable pageable) {
        validateJobType(jobType);
        return jobListingRepository.findByJobTypeAndIsActiveTrue(jobType, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Search for job listings by title or description
     *
     * @param searchTerm Search term
     * @param pageable   Pagination information
     * @return Page of job listing DTOs
     */
    @Transactional(readOnly = true)
    public Page<JobListingDTO> searchJobListings(String searchTerm, Pageable pageable) {
        return jobListingRepository.searchByTitleOrDescription(searchTerm, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Create a new job listing
     *
     * @param title               Job title
     * @param departmentId        Department ID
     * @param jobDescription      Job description
     * @param qualifications      Job qualifications
     * @param employmentType      Employment type (Full-time, Part-time, Contract)
     * @param jobType             Job type (Internal, External)
     * @param applicationDeadline Application deadline
     * @return Created job listing DTO
     */
    @Transactional
    public JobListingDTO createJobListing(
            String title,
            String departmentId,
            String jobDescription,
            String qualifications,
            String employmentType,
            String jobType,
            LocalDate applicationDeadline) {

        // Validate inputs
        if (title == null || title.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job title is required");
        }

        if (departmentId == null || departmentId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department ID is required");
        }

        validateEmploymentType(employmentType);
        validateJobType(jobType);

        if (applicationDeadline == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application deadline is required");
        }

        if (applicationDeadline.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application deadline cannot be in the past");
        }

        // Get department
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Department not found with ID: " + departmentId));

        // Create new job listing
        JobListingEntity jobListing = new JobListingEntity();
        jobListing.setTitle(title);
        jobListing.setDepartment(department);
        jobListing.setJobDescription(jobDescription);
        jobListing.setQualifications(qualifications);
        jobListing.setEmploymentType(employmentType);
        jobListing.setJobType(jobType);
        jobListing.setDatePosted(LocalDate.now());
        jobListing.setApplicationDeadline(applicationDeadline);
        jobListing.setActive(true);

        // Save and return
        JobListingEntity savedJobListing = jobListingRepository.save(jobListing);
        return convertToDTO(savedJobListing);
    }

    /**
     * Update an existing job listing
     *
     * @param jobId               Job listing ID
     * @param title               Job title
     * @param departmentId        Department ID
     * @param jobDescription      Job description
     * @param qualifications      Job qualifications
     * @param employmentType      Employment type (Full-time, Part-time, Contract)
     * @param jobType             Job type (Internal, External)
     * @param applicationDeadline Application deadline
     * @param isActive            Whether the job listing is active
     * @return Updated job listing DTO
     */
    @Transactional
    public JobListingDTO updateJobListing(
            String jobId,
            String title,
            String departmentId,
            String jobDescription,
            String qualifications,
            String employmentType,
            String jobType,
            LocalDate applicationDeadline,
            Boolean isActive) {

        // Get existing job listing
        JobListingEntity jobListing = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Job listing not found with ID: " + jobId));

        // Update fields if provided
        if (title != null && !title.trim().isEmpty()) {
            jobListing.setTitle(title);
        }

        if (departmentId != null && !departmentId.trim().isEmpty()) {
            DepartmentEntity department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Department not found with ID: " + departmentId));
            jobListing.setDepartment(department);
        }

        if (jobDescription != null) {
            jobListing.setJobDescription(jobDescription);
        }

        if (qualifications != null) {
            jobListing.setQualifications(qualifications);
        }

        if (employmentType != null) {
            validateEmploymentType(employmentType);
            jobListing.setEmploymentType(employmentType);
        }

        if (jobType != null) {
            validateJobType(jobType);
            jobListing.setJobType(jobType);
        }

        if (applicationDeadline != null) {
            if (applicationDeadline.isBefore(LocalDate.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application deadline cannot be in the past");
            }
            jobListing.setApplicationDeadline(applicationDeadline);
        }

        if (isActive != null) {
            jobListing.setActive(isActive);
        }

        // Save and return
        JobListingEntity updatedJobListing = jobListingRepository.save(jobListing);
        return convertToDTO(updatedJobListing);
    }

    /**
     * Deactivate a job listing
     *
     * @param jobId Job listing ID
     * @return Deactivated job listing DTO
     */
    @Transactional
    public JobListingDTO deactivateJobListing(String jobId) {
        JobListingEntity jobListing = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Job listing not found with ID: " + jobId));

        jobListing.setActive(false);
        JobListingEntity deactivatedJobListing = jobListingRepository.save(jobListing);
        return convertToDTO(deactivatedJobListing);
    }

    /**
     * Activate a job listing
     *
     * @param jobId Job listing ID
     * @return Activated job listing DTO
     */
    @Transactional
    public JobListingDTO activateJobListing(String jobId) {
        JobListingEntity jobListing = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Job listing not found with ID: " + jobId));

        jobListing.setActive(true);
        JobListingEntity activatedJobListing = jobListingRepository.save(jobListing);
        return convertToDTO(activatedJobListing);
    }

    /**
     * Delete a job listing
     *
     * @param jobId Job listing ID
     */
    @Transactional
    public void deleteJobListing(String jobId) {
        JobListingEntity jobListing = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Job listing not found with ID: " + jobId));

        // Check if there are any applications for this job listing
        long applicationCount = applicationRecordRepository.countByJobListing(jobListing);
        if (applicationCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot delete job listing with existing applications. Deactivate it instead.");
        }

        jobListingRepository.delete(jobListing);
    }

    /**
     * Convert a job listing entity to DTO
     *
     * @param jobListing Job listing entity
     * @return Job listing DTO
     */
    private JobListingDTO convertToDTO(JobListingEntity jobListing) {
        JobListingDTO dto = new JobListingDTO();
        dto.setJobId(jobListing.getJobId());
        dto.setTitle(jobListing.getTitle());
        
        if (jobListing.getDepartment() != null) {
            dto.setDepartmentId(jobListing.getDepartment().getDepartmentId());
            dto.setDepartmentName(jobListing.getDepartment().getDepartmentName());
        }
        
        dto.setJobDescription(jobListing.getJobDescription());
        dto.setQualifications(jobListing.getQualifications());
        dto.setEmploymentType(jobListing.getEmploymentType());
        dto.setJobType(jobListing.getJobType());
        dto.setDatePosted(jobListing.getDatePosted());
        dto.setApplicationDeadline(jobListing.getApplicationDeadline());
        dto.setActive(jobListing.isActive());
        
        // Get total number of applications for this job listing
        long totalApplications = applicationRecordRepository.countByJobListing(jobListing);
        dto.setTotalApplications((int) totalApplications);
        
        return dto;
    }

    /**
     * Validate employment type
     *
     * @param employmentType Employment type to validate
     */
    private void validateEmploymentType(String employmentType) {
        if (employmentType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employment type is required");
        }

        List<String> validTypes = List.of("FULL_TIME", "PART_TIME", "CONTRACT");
        if (!validTypes.contains(employmentType.toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid employment type. Valid values are: " + String.join(", ", validTypes));
        }
    }

    /**
     * Validate job type
     *
     * @param jobType Job type to validate
     */
    private void validateJobType(String jobType) {
        if (jobType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job type is required");
        }

        List<String> validTypes = List.of("INTERNAL", "EXTERNAL", "BOTH");
        if (!validTypes.contains(jobType.toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid job type. Valid values are: " + String.join(", ", validTypes));
        }
    }
} 