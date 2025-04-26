package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.ApplicationRecordDTO;
import cit.edu.workforce.Entity.ApplicantEntity;
import cit.edu.workforce.Entity.ApplicationRecordEntity;
import cit.edu.workforce.Entity.JobListingEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.ApplicantRepository;
import cit.edu.workforce.Repository.ApplicationRecordRepository;
import cit.edu.workforce.Repository.JobListingRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ApplicationRecordService - Service for managing application records
 * New file: This service provides methods to manage job applications including CRUD operations and status updates
 */
@Service
public class ApplicationRecordService {

    private final ApplicationRecordRepository applicationRecordRepository;
    private final ApplicantRepository applicantRepository;
    private final JobListingRepository jobListingRepository;
    private final UserAccountRepository userAccountRepository;

    @Autowired
    public ApplicationRecordService(
            ApplicationRecordRepository applicationRecordRepository,
            ApplicantRepository applicantRepository,
            JobListingRepository jobListingRepository,
            UserAccountRepository userAccountRepository) {
        this.applicationRecordRepository = applicationRecordRepository;
        this.applicantRepository = applicantRepository;
        this.jobListingRepository = jobListingRepository;
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Get all application records
     *
     * @return List of application record DTOs
     */
    @Transactional(readOnly = true)
    public List<ApplicationRecordDTO> getAllApplications() {
        return applicationRecordRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated application records
     *
     * @param pageable Pagination information
     * @return Page of application record DTOs
     */
    @Transactional(readOnly = true)
    public Page<ApplicationRecordDTO> getAllApplications(Pageable pageable) {
        return applicationRecordRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get application record by ID
     *
     * @param applicationId Application record ID
     * @return Application record DTO
     */
    @Transactional(readOnly = true)
    public ApplicationRecordDTO getApplicationById(String applicationId) {
        ApplicationRecordEntity application = applicationRecordRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Application not found with ID: " + applicationId));

        // Check if the current user has permission to view this application
        if (!hasPermissionToViewApplication(application)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to view this application");
        }

        return convertToDTO(application);
    }

    /**
     * Get application records by applicant
     *
     * @param applicantId Applicant ID
     * @return List of application record DTOs
     */
    @Transactional(readOnly = true)
    public List<ApplicationRecordDTO> getApplicationsByApplicant(String applicantId) {
        ApplicantEntity applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Applicant not found with ID: " + applicantId));

        // Check if the current user has permission to view this applicant's applications
        if (!hasPermissionToViewApplicantApplications(applicant)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to view this applicant's applications");
        }

        return applicationRecordRepository.findByApplicant(applicant).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated application records by applicant
     *
     * @param applicantId Applicant ID
     * @param pageable    Pagination information
     * @return Page of application record DTOs
     */
    @Transactional(readOnly = true)
    public Page<ApplicationRecordDTO> getApplicationsByApplicant(String applicantId, Pageable pageable) {
        ApplicantEntity applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Applicant not found with ID: " + applicantId));

        // Check if the current user has permission to view this applicant's applications
        if (!hasPermissionToViewApplicantApplications(applicant)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to view this applicant's applications");
        }

        return applicationRecordRepository.findByApplicant(applicant, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get application records by job listing
     *
     * @param jobId Job listing ID
     * @return List of application record DTOs
     */
    @Transactional(readOnly = true)
    public List<ApplicationRecordDTO> getApplicationsByJobListing(String jobId) {
        JobListingEntity jobListing = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Job listing not found with ID: " + jobId));

        // Only HR and admins can view all applications for a job listing
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrHR = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));

        if (!isAdminOrHR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to view all applications for this job listing");
        }

        return applicationRecordRepository.findByJobListing(jobListing).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated application records by job listing
     *
     * @param jobId    Job listing ID
     * @param pageable Pagination information
     * @return Page of application record DTOs
     */
    @Transactional(readOnly = true)
    public Page<ApplicationRecordDTO> getApplicationsByJobListing(String jobId, Pageable pageable) {
        JobListingEntity jobListing = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Job listing not found with ID: " + jobId));

        // Only HR and admins can view all applications for a job listing
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrHR = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));

        if (!isAdminOrHR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to view all applications for this job listing");
        }

        return applicationRecordRepository.findByJobListing(jobListing, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get application records by status
     *
     * @param status Status to filter by
     * @return List of application record DTOs
     */
    @Transactional(readOnly = true)
    public List<ApplicationRecordDTO> getApplicationsByStatus(String status) {
        validateStatus(status);

        // Only HR and admins can view all applications by status
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrHR = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));

        if (!isAdminOrHR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to view all applications by status");
        }

        return applicationRecordRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated application records by status
     *
     * @param status   Status to filter by
     * @param pageable Pagination information
     * @return Page of application record DTOs
     */
    @Transactional(readOnly = true)
    public Page<ApplicationRecordDTO> getApplicationsByStatus(String status, Pageable pageable) {
        validateStatus(status);

        // Only HR and admins can view all applications by status
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrHR = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));

        if (!isAdminOrHR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to view all applications by status");
        }

        return applicationRecordRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get the current user's application records
     *
     * @return List of application record DTOs
     */
    @Transactional(readOnly = true)
    public List<ApplicationRecordDTO> getCurrentUserApplications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        String email = authentication.getName();
        UserAccountEntity user = userAccountRepository.findByEmailAddress(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with email: " + email));

        // Get applicant profile for this user
        ApplicantEntity applicant = applicantRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No applicant profile found for the current user"));

        return applicationRecordRepository.findByApplicant(applicant).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated current user's application records
     *
     * @param pageable Pagination information
     * @return Page of application record DTOs
     */
    @Transactional(readOnly = true)
    public Page<ApplicationRecordDTO> getCurrentUserApplications(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        String email = authentication.getName();
        UserAccountEntity user = userAccountRepository.findByEmailAddress(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with email: " + email));

        // Get applicant profile for this user
        ApplicantEntity applicant = applicantRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No applicant profile found for the current user"));

        return applicationRecordRepository.findByApplicant(applicant, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Create a new application record
     *
     * @param applicantId Applicant ID
     * @param jobId       Job listing ID
     * @return Created application record DTO
     */
    @Transactional
    public ApplicationRecordDTO createApplication(String applicantId, String jobId) {
        // Validate inputs
        ApplicantEntity applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Applicant not found with ID: " + applicantId));

        JobListingEntity jobListing = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Job listing not found with ID: " + jobId));

        // Check if the job listing is active
        if (!jobListing.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot apply to an inactive job listing");
        }

        // Check if the application deadline has passed
        if (jobListing.getApplicationDeadline().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application deadline has passed");
        }

        // Check if the job type matches the applicant type (internal vs external)
        if (jobListing.getJobType().equals("INTERNAL") && !applicant.isInternal()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "External applicants cannot apply for internal job listings");
        }

        // Check if the applicant has already applied for this job
        applicationRecordRepository.findByApplicantAndJobListing(applicant, jobListing).stream().findFirst()
                .ifPresent(existingApplication -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already applied for this job");
                });

        // Create new application record
        ApplicationRecordEntity application = new ApplicationRecordEntity();
        application.setApplicant(applicant);
        application.setJobListing(jobListing);
        application.setStatus("PENDING");

        // Save and return
        ApplicationRecordEntity savedApplication = applicationRecordRepository.save(application);
        return convertToDTO(savedApplication);
    }

    /**
     * Update an application's status
     *
     * @param applicationId Application record ID
     * @param status        New status
     * @param remarks       Remarks about the status update
     * @return Updated application record DTO
     */
    @Transactional
    public ApplicationRecordDTO updateApplicationStatus(String applicationId, String status, String remarks) {
        // Validate status
        validateStatus(status);

        // Get application
        ApplicationRecordEntity application = applicationRecordRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Application not found with ID: " + applicationId));

        // Only HR and admins can update application status
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrHR = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));

        if (!isAdminOrHR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to update application status");
        }

        // Get current user
        String email = authentication.getName();
        UserAccountEntity reviewer = userAccountRepository.findByEmailAddress(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with email: " + email));

        // Update application
        application.setStatus(status);
        application.setRemarks(remarks);
        application.setReviewedBy(reviewer);
        application.setReviewedAt(LocalDateTime.now());

        // Save and return
        ApplicationRecordEntity updatedApplication = applicationRecordRepository.save(application);
        return convertToDTO(updatedApplication);
    }

    /**
     * Convert an application record entity to DTO
     *
     * @param application Application record entity
     * @return Application record DTO
     */
    private ApplicationRecordDTO convertToDTO(ApplicationRecordEntity application) {
        ApplicationRecordDTO dto = new ApplicationRecordDTO();
        dto.setApplicationId(application.getApplicationId());
        
        if (application.getApplicant() != null) {
            dto.setApplicantId(application.getApplicant().getApplicantId());
            dto.setApplicantName(application.getApplicant().getFullName());
            dto.setInternal(application.getApplicant().isInternal());
            dto.setResumePath(application.getApplicant().getResumePdfPath());
        }
        
        if (application.getJobListing() != null) {
            dto.setJobId(application.getJobListing().getJobId());
            dto.setJobTitle(application.getJobListing().getTitle());
            
            if (application.getJobListing().getDepartment() != null) {
                dto.setDepartmentName(application.getJobListing().getDepartment().getDepartmentName());
            }
        }
        
        dto.setStatus(application.getStatus());
        dto.setRemarks(application.getRemarks());
        dto.setReviewedAt(application.getReviewedAt());
        
        if (application.getReviewedBy() != null) {
            dto.setReviewedBy(application.getReviewedBy().getUserId());
            dto.setReviewerName(application.getReviewedBy().getEmailAddress().split("@")[0]); // Simplified for demo
        }
        
        return dto;
    }

    /**
     * Check if the current user has permission to view an application
     *
     * @param application Application record entity
     * @return True if the current user has permission, false otherwise
     */
    private boolean hasPermissionToViewApplication(ApplicationRecordEntity application) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        // HR and Admins have permission to view any application
        boolean isAdminOrHR = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));

        if (isAdminOrHR) {
            return true;
        }

        // Users can only view their own applications
        String email = authentication.getName();
        return application.getApplicant() != null && 
               application.getApplicant().isInternal() && 
               application.getApplicant().getUser() != null && 
               application.getApplicant().getUser().getEmailAddress().equals(email);
    }

    /**
     * Check if the current user has permission to view an applicant's applications
     *
     * @param applicant Applicant entity
     * @return True if the current user has permission, false otherwise
     */
    private boolean hasPermissionToViewApplicantApplications(ApplicantEntity applicant) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        // HR and Admins have permission to view any applicant's applications
        boolean isAdminOrHR = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));

        if (isAdminOrHR) {
            return true;
        }

        // Users can only view their own applications
        String email = authentication.getName();
        return applicant.isInternal() && 
               applicant.getUser() != null && 
               applicant.getUser().getEmailAddress().equals(email);
    }

    /**
     * Validate application status
     *
     * @param status Status to validate
     */
    private void validateStatus(String status) {
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }

        List<String> validStatuses = List.of("PENDING", "SHORTLISTED", "REJECTED", "HIRED");
        if (!validStatuses.contains(status.toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status. Valid values are: " + String.join(", ", validStatuses));
        }
    }
} 