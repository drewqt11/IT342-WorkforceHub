package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.JobApplicationDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.JobApplicationEntity;
import cit.edu.workforce.Entity.JobPostingEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.JobApplicationRepository;
import cit.edu.workforce.Repository.JobPostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public JobApplicationService(
            JobApplicationRepository jobApplicationRepository,
            JobPostingRepository jobPostingRepository,
            EmployeeRepository employeeRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.employeeRepository = employeeRepository;
    }

    // Convert entity to DTO
    public JobApplicationDTO convertToDTO(JobApplicationEntity application) {
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setApplicationId(application.getApplicationId());
        
        if (application.getJobPosting() != null) {
            dto.setPostingId(application.getJobPosting().getPostingId());
            dto.setJobTitle(application.getJobPosting().getTitle());
            
            if (application.getJobPosting().getDepartment() != null) {
                dto.setDepartmentName(application.getJobPosting().getDepartment().getDepartmentName());
            }
        }
        
        if (application.getEmployee() != null) {
            dto.setEmployeeId(application.getEmployee().getEmployeeId());
            dto.setApplicantName(application.getEmployee().getFirstName() + " " + application.getEmployee().getLastName());
            dto.setApplicantEmail(application.getEmployee().getEmail());
            dto.setApplicantPhone(application.getEmployee().getPhoneNumber());
        } else {
            dto.setApplicantName(application.getApplicantName());
            dto.setApplicantEmail(application.getApplicantEmail());
            dto.setApplicantPhone(application.getApplicantPhone());
        }
        
        dto.setResumeUrl(application.getResumeUrl());
        dto.setCoverLetterUrl(application.getCoverLetterUrl());
        dto.setApplicationDate(application.getApplicationDate());
        dto.setStatus(application.getStatus());
        dto.setCurrentStage(application.getCurrentStage());
        dto.setSource(application.getSource());
        dto.setInternalNotes(application.getInternalNotes());
        dto.setInterviewDate(application.getInterviewDate());
        dto.setInterviewFeedback(application.getInterviewFeedback());
        
        if (application.getReviewedBy() != null) {
            dto.setReviewedById(application.getReviewedBy().getEmployeeId());
            dto.setReviewedByName(application.getReviewedBy().getFirstName() + " " + application.getReviewedBy().getLastName());
        }
        
        dto.setReviewDate(application.getReviewDate());
        
        return dto;
    }

    @Transactional(readOnly = true)
    public List<JobApplicationDTO> getAllJobApplications() {
        return jobApplicationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationDTO> getAllJobApplications(Pageable pageable) {
        return jobApplicationRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<JobApplicationDTO> getJobApplicationById(String applicationId) {
        return jobApplicationRepository.findById(applicationId)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<JobApplicationDTO> getJobApplicationsByJobPosting(String postingId) {
        Optional<JobPostingEntity> jobPosting = jobPostingRepository.findById(postingId);
        if (jobPosting.isPresent()) {
            return jobApplicationRepository.findByJobPosting(jobPosting.get()).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationDTO> getJobApplicationsByJobPosting(String postingId, Pageable pageable) {
        Optional<JobPostingEntity> jobPosting = jobPostingRepository.findById(postingId);
        if (jobPosting.isPresent()) {
            return jobApplicationRepository.findByJobPosting(jobPosting.get(), pageable)
                    .map(this::convertToDTO);
        }
        return Page.empty();
    }

    @Transactional(readOnly = true)
    public List<JobApplicationDTO> getJobApplicationsByEmployee(String employeeId) {
        Optional<EmployeeEntity> employee = employeeRepository.findById(employeeId);
        if (employee.isPresent()) {
            return jobApplicationRepository.findByEmployee(employee.get()).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationDTO> getJobApplicationsByEmployee(String employeeId, Pageable pageable) {
        Optional<EmployeeEntity> employee = employeeRepository.findById(employeeId);
        if (employee.isPresent()) {
            return jobApplicationRepository.findByEmployee(employee.get(), pageable)
                    .map(this::convertToDTO);
        }
        return Page.empty();
    }

    @Transactional(readOnly = true)
    public List<JobApplicationDTO> getJobApplicationsByStatus(String status) {
        return jobApplicationRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationDTO> getJobApplicationsByStatus(String status, Pageable pageable) {
        return jobApplicationRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<JobApplicationDTO> getJobApplicationsByStage(String stage) {
        return jobApplicationRepository.findByCurrentStage(stage).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationDTO> getJobApplicationsByStage(String stage, Pageable pageable) {
        return jobApplicationRepository.findByCurrentStage(stage, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationDTO> searchJobApplications(String query, Pageable pageable) {
        return jobApplicationRepository
                .findByApplicantNameContainingIgnoreCaseOrApplicantEmailContainingIgnoreCase(query, query, pageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public JobApplicationDTO createInternalJobApplication(
            String postingId,
            String employeeId,
            String coverLetterUrl) {

        // Check if job posting exists
        JobPostingEntity jobPosting = jobPostingRepository.findById(postingId)
                .orElseThrow(() -> new RuntimeException("Job posting not found"));

        // Check if employee exists
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Check if employee has already applied for this job
        if (jobApplicationRepository.existsByEmployeeAndJobPosting(employee, jobPosting)) {
            throw new RuntimeException("You have already applied for this job posting");
        }

        JobApplicationEntity application = new JobApplicationEntity();
        application.setJobPosting(jobPosting);
        application.setEmployee(employee);
        application.setCoverLetterUrl(coverLetterUrl);
        application.setStatus("APPLIED");
        application.setCurrentStage("APPLIED");
        application.setSource("INTERNAL");

        JobApplicationEntity savedApplication = jobApplicationRepository.save(application);
        return convertToDTO(savedApplication);
    }

    @Transactional
    public JobApplicationDTO createExternalJobApplication(
            String postingId,
            String applicantName,
            String applicantEmail,
            String applicantPhone,
            String resumeUrl,
            String coverLetterUrl,
            String source) {

        // Check if job posting exists
        JobPostingEntity jobPosting = jobPostingRepository.findById(postingId)
                .orElseThrow(() -> new RuntimeException("Job posting not found"));

        JobApplicationEntity application = new JobApplicationEntity();
        application.setJobPosting(jobPosting);
        application.setApplicantName(applicantName);
        application.setApplicantEmail(applicantEmail);
        application.setApplicantPhone(applicantPhone);
        application.setResumeUrl(resumeUrl);
        application.setCoverLetterUrl(coverLetterUrl);
        application.setStatus("APPLIED");
        application.setCurrentStage("APPLIED");
        application.setSource(source != null ? source : "CAREER_SITE");

        JobApplicationEntity savedApplication = jobApplicationRepository.save(application);
        return convertToDTO(savedApplication);
    }

    @Transactional
    public JobApplicationDTO updateApplicationStatus(
            String applicationId,
            String status,
            String currentStage,
            String internalNotes,
            String reviewedById) {

        JobApplicationEntity application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Job application not found"));

        if (status != null) application.setStatus(status);
        if (currentStage != null) application.setCurrentStage(currentStage);
        if (internalNotes != null) application.setInternalNotes(internalNotes);
        
        if (reviewedById != null) {
            employeeRepository.findById(reviewedById).ifPresent(employee -> {
                application.setReviewedBy(employee);
                application.setReviewDate(LocalDate.now());
            });
        }

        JobApplicationEntity updatedApplication = jobApplicationRepository.save(application);
        return convertToDTO(updatedApplication);
    }

    @Transactional
    public JobApplicationDTO scheduleInterview(
            String applicationId,
            LocalDate interviewDate) {

        JobApplicationEntity application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Job application not found"));

        application.setInterviewDate(interviewDate);
        application.setStatus("INTERVIEW");
        application.setCurrentStage("INTERVIEW");

        JobApplicationEntity updatedApplication = jobApplicationRepository.save(application);
        return convertToDTO(updatedApplication);
    }

    @Transactional
    public JobApplicationDTO addInterviewFeedback(
            String applicationId,
            String interviewFeedback,
            String reviewedById) {

        JobApplicationEntity application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Job application not found"));

        application.setInterviewFeedback(interviewFeedback);
        
        if (reviewedById != null) {
            employeeRepository.findById(reviewedById).ifPresent(employee -> {
                application.setReviewedBy(employee);
                application.setReviewDate(LocalDate.now());
            });
        }

        JobApplicationEntity updatedApplication = jobApplicationRepository.save(application);
        return convertToDTO(updatedApplication);
    }

    @Transactional
    public void deleteJobApplication(String applicationId) {
        jobApplicationRepository.deleteById(applicationId);
    }
}

// New file: Service for managing job applications in the recruitment module
// Handles creating applications, updating application status, and tracking the application process 