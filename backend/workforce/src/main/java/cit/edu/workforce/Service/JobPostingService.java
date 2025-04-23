package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.JobPostingDTO;
import cit.edu.workforce.Entity.DepartmentEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.JobPostingEntity;
import cit.edu.workforce.Entity.JobTitleEntity;
import cit.edu.workforce.Repository.DepartmentRepository;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.JobPostingRepository;
import cit.edu.workforce.Repository.JobTitleRepository;
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
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final DepartmentRepository departmentRepository;
    private final JobTitleRepository jobTitleRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public JobPostingService(
            JobPostingRepository jobPostingRepository,
            DepartmentRepository departmentRepository,
            JobTitleRepository jobTitleRepository,
            EmployeeRepository employeeRepository) {
        this.jobPostingRepository = jobPostingRepository;
        this.departmentRepository = departmentRepository;
        this.jobTitleRepository = jobTitleRepository;
        this.employeeRepository = employeeRepository;
    }

    // Convert entity to DTO
    public JobPostingDTO convertToDTO(JobPostingEntity posting) {
        JobPostingDTO dto = new JobPostingDTO();
        dto.setPostingId(posting.getPostingId());
        dto.setTitle(posting.getTitle());
        dto.setDescription(posting.getDescription());
        dto.setQualifications(posting.getQualifications());
        dto.setResponsibilities(posting.getResponsibilities());

        if (posting.getDepartment() != null) {
            dto.setDepartmentId(posting.getDepartment().getDepartmentId());
            dto.setDepartmentName(posting.getDepartment().getDepartmentName());
        }

        if (posting.getJobTitle() != null) {
            dto.setJobTitleId(posting.getJobTitle().getJobId());
            dto.setJobTitleName(posting.getJobTitle().getJobName());
        }

        dto.setLocation(posting.getLocation());
        dto.setEmploymentType(posting.getEmploymentType());
        dto.setExperienceLevel(posting.getExperienceLevel());
        dto.setSalaryMin(posting.getSalaryMin());
        dto.setSalaryMax(posting.getSalaryMax());
        dto.setPostingDate(posting.getPostingDate());
        dto.setClosingDate(posting.getClosingDate());
        dto.setIsInternal(posting.getIsInternal());
        dto.setIsActive(posting.getIsActive());
        dto.setExternalUrl(posting.getExternalUrl());

        if (posting.getPostedBy() != null) {
            dto.setPostedById(posting.getPostedBy().getEmployeeId());
            dto.setPostedByName(posting.getPostedBy().getFirstName() + " " + posting.getPostedBy().getLastName());
        }

        dto.setApplicationsCount(posting.getApplications() != null ? posting.getApplications().size() : 0);
        
        return dto;
    }

    @Transactional(readOnly = true)
    public List<JobPostingDTO> getAllJobPostings() {
        return jobPostingRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<JobPostingDTO> getAllJobPostings(Pageable pageable) {
        return jobPostingRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<JobPostingDTO> getJobPostingById(String postingId) {
        return jobPostingRepository.findById(postingId)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<JobPostingDTO> getActiveJobPostings() {
        return jobPostingRepository.findByIsActive(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<JobPostingDTO> getActiveJobPostings(Pageable pageable) {
        return jobPostingRepository.findByIsActive(true, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<JobPostingDTO> getInternalJobPostings() {
        return jobPostingRepository.findByIsActiveAndIsInternal(true, true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<JobPostingDTO> getInternalJobPostings(Pageable pageable) {
        return jobPostingRepository.findByIsActiveAndIsInternal(true, true, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<JobPostingDTO> getExternalJobPostings() {
        return jobPostingRepository.findByIsActiveAndIsInternal(true, false).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<JobPostingDTO> getExternalJobPostings(Pageable pageable) {
        return jobPostingRepository.findByIsActiveAndIsInternal(true, false, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<JobPostingDTO> getJobPostingsByDepartment(String departmentId) {
        Optional<DepartmentEntity> department = departmentRepository.findById(departmentId);
        if (department.isPresent()) {
            return jobPostingRepository.findByDepartment(department.get()).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public Page<JobPostingDTO> getJobPostingsByDepartment(String departmentId, Pageable pageable) {
        Optional<DepartmentEntity> department = departmentRepository.findById(departmentId);
        if (department.isPresent()) {
            return jobPostingRepository.findByDepartment(department.get(), pageable)
                    .map(this::convertToDTO);
        }
        return Page.empty();
    }

    @Transactional(readOnly = true)
    public List<JobPostingDTO> getJobPostingsByJobTitle(String jobTitleId) {
        Optional<JobTitleEntity> jobTitle = jobTitleRepository.findById(jobTitleId);
        if (jobTitle.isPresent()) {
            return jobPostingRepository.findByJobTitle(jobTitle.get()).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public Page<JobPostingDTO> searchJobPostings(String query, Pageable pageable) {
        return jobPostingRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query, pageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public JobPostingDTO createJobPosting(
            String title,
            String description,
            String qualifications,
            String responsibilities,
            String departmentId,
            String jobTitleId,
            String location,
            String employmentType,
            String experienceLevel,
            Double salaryMin,
            Double salaryMax,
            LocalDate closingDate,
            Boolean isInternal,
            String externalUrl,
            String postedById) {

        JobPostingEntity posting = new JobPostingEntity();
        posting.setTitle(title);
        posting.setDescription(description);
        posting.setQualifications(qualifications);
        posting.setResponsibilities(responsibilities);
        
        // Set department if provided
        if (departmentId != null) {
            departmentRepository.findById(departmentId).ifPresent(posting::setDepartment);
        }
        
        // Set job title if provided
        if (jobTitleId != null) {
            jobTitleRepository.findById(jobTitleId).ifPresent(posting::setJobTitle);
        }
        
        posting.setLocation(location);
        posting.setEmploymentType(employmentType);
        posting.setExperienceLevel(experienceLevel);
        posting.setSalaryMin(salaryMin);
        posting.setSalaryMax(salaryMax);
        posting.setClosingDate(closingDate);
        posting.setIsInternal(isInternal != null ? isInternal : false);
        posting.setIsActive(true);
        posting.setExternalUrl(externalUrl);
        
        // Set posted by if provided
        if (postedById != null) {
            employeeRepository.findById(postedById).ifPresent(posting::setPostedBy);
        }

        JobPostingEntity savedPosting = jobPostingRepository.save(posting);
        return convertToDTO(savedPosting);
    }

    @Transactional
    public JobPostingDTO updateJobPosting(
            String postingId,
            String title,
            String description,
            String qualifications,
            String responsibilities,
            String departmentId,
            String jobTitleId,
            String location,
            String employmentType,
            String experienceLevel,
            Double salaryMin,
            Double salaryMax,
            LocalDate closingDate,
            Boolean isInternal,
            Boolean isActive,
            String externalUrl) {

        JobPostingEntity posting = jobPostingRepository.findById(postingId)
                .orElseThrow(() -> new RuntimeException("Job posting not found"));

        if (title != null) posting.setTitle(title);
        if (description != null) posting.setDescription(description);
        if (qualifications != null) posting.setQualifications(qualifications);
        if (responsibilities != null) posting.setResponsibilities(responsibilities);
        
        // Update department if provided
        if (departmentId != null) {
            departmentRepository.findById(departmentId).ifPresent(posting::setDepartment);
        }
        
        // Update job title if provided
        if (jobTitleId != null) {
            jobTitleRepository.findById(jobTitleId).ifPresent(posting::setJobTitle);
        }
        
        if (location != null) posting.setLocation(location);
        if (employmentType != null) posting.setEmploymentType(employmentType);
        if (experienceLevel != null) posting.setExperienceLevel(experienceLevel);
        if (salaryMin != null) posting.setSalaryMin(salaryMin);
        if (salaryMax != null) posting.setSalaryMax(salaryMax);
        if (closingDate != null) posting.setClosingDate(closingDate);
        if (isInternal != null) posting.setIsInternal(isInternal);
        if (isActive != null) posting.setIsActive(isActive);
        if (externalUrl != null) posting.setExternalUrl(externalUrl);

        JobPostingEntity updatedPosting = jobPostingRepository.save(posting);
        return convertToDTO(updatedPosting);
    }

    @Transactional
    public void deactivateJobPosting(String postingId) {
        JobPostingEntity posting = jobPostingRepository.findById(postingId)
                .orElseThrow(() -> new RuntimeException("Job posting not found"));
        posting.setIsActive(false);
        jobPostingRepository.save(posting);
    }

    @Transactional
    public void activateJobPosting(String postingId) {
        JobPostingEntity posting = jobPostingRepository.findById(postingId)
                .orElseThrow(() -> new RuntimeException("Job posting not found"));
        posting.setIsActive(true);
        jobPostingRepository.save(posting);
    }

    @Transactional
    public void deleteJobPosting(String postingId) {
        jobPostingRepository.deleteById(postingId);
    }
}

// New file: Service for managing job postings in the recruitment module
// Handles creating, updating, and searching for job postings 