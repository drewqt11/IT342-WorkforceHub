package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.PerformanceEvaluationDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.PerformanceEvaluationEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.PerformanceEvaluationRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PerformanceEvaluationService - Service for managing performance evaluations
 * New file: Provides functionality for creating and managing performance evaluations
 */
@Service
public class PerformanceEvaluationService {

    private final PerformanceEvaluationRepository performanceEvaluationRepository;
    private final EmployeeRepository employeeRepository;
    private final UserAccountRepository userAccountRepository;

    @Autowired
    public PerformanceEvaluationService(
            PerformanceEvaluationRepository performanceEvaluationRepository,
            EmployeeRepository employeeRepository,
            UserAccountRepository userAccountRepository) {
        this.performanceEvaluationRepository = performanceEvaluationRepository;
        this.employeeRepository = employeeRepository;
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Create a new performance evaluation
     */
    @Transactional
    public PerformanceEvaluationDTO createPerformanceEvaluation(PerformanceEvaluationDTO performanceEvaluationDTO) {
        // Validate input
        if (performanceEvaluationDTO.getEmployeeId() == null || performanceEvaluationDTO.getEmployeeId().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee ID is required");
        }
        
        if (performanceEvaluationDTO.getEvaluationPeriodStart() == null || 
            performanceEvaluationDTO.getEvaluationPeriodEnd() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Evaluation period start and end dates are required");
        }
        
        if (performanceEvaluationDTO.getEvaluationPeriodEnd().isBefore(performanceEvaluationDTO.getEvaluationPeriodStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Evaluation period end date must be after start date");
        }
        
        // Get the employee
        EmployeeEntity employee = employeeRepository.findById(performanceEvaluationDTO.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        // Get the current user as reviewer
        UserAccountEntity reviewer = getCurrentUser();
        
        // Create the performance evaluation
        PerformanceEvaluationEntity evaluation = new PerformanceEvaluationEntity();
        evaluation.setEmployee(employee);
        evaluation.setReviewer(reviewer);
        evaluation.setEvaluationPeriodStart(performanceEvaluationDTO.getEvaluationPeriodStart());
        evaluation.setEvaluationPeriodEnd(performanceEvaluationDTO.getEvaluationPeriodEnd());
        
        // Set optional fields if provided
        if (performanceEvaluationDTO.getOverallScore() != null) {
            if (performanceEvaluationDTO.getOverallScore().compareTo(BigDecimal.ZERO) < 0 || 
                performanceEvaluationDTO.getOverallScore().compareTo(new BigDecimal("5.0")) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Overall score must be between 0 and 5");
            }
            evaluation.setOverallScore(performanceEvaluationDTO.getOverallScore());
        }
        
        if (performanceEvaluationDTO.getRemarks() != null) {
            evaluation.setRemarks(performanceEvaluationDTO.getRemarks());
        }
        
        // Set evaluation date to current date if not provided
        evaluation.setEvaluationDate(performanceEvaluationDTO.getEvaluationDate() != null ? 
                performanceEvaluationDTO.getEvaluationDate() : LocalDate.now());
        
        PerformanceEvaluationEntity savedEvaluation = performanceEvaluationRepository.save(evaluation);
        return convertToDTO(savedEvaluation);
    }

    /**
     * Get a specific performance evaluation by ID
     */
    @Transactional(readOnly = true)
    public Optional<PerformanceEvaluationDTO> getPerformanceEvaluationById(String evaluationId) {
        return performanceEvaluationRepository.findById(evaluationId)
                .map(this::convertToDTO);
    }

    /**
     * Update an existing performance evaluation
     */
    @Transactional
    public PerformanceEvaluationDTO updatePerformanceEvaluation(String evaluationId, PerformanceEvaluationDTO evaluationDTO) {
        PerformanceEvaluationEntity evaluation = performanceEvaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Performance evaluation not found"));
        
        // Update fields if provided
        if (evaluationDTO.getEvaluationPeriodStart() != null) {
            evaluation.setEvaluationPeriodStart(evaluationDTO.getEvaluationPeriodStart());
        }
        
        if (evaluationDTO.getEvaluationPeriodEnd() != null) {
            evaluation.setEvaluationPeriodEnd(evaluationDTO.getEvaluationPeriodEnd());
        }
        
        // Validate date range
        if (evaluation.getEvaluationPeriodEnd().isBefore(evaluation.getEvaluationPeriodStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Evaluation period end date must be after start date");
        }
        
        if (evaluationDTO.getOverallScore() != null) {
            if (evaluationDTO.getOverallScore().compareTo(BigDecimal.ZERO) < 0 || 
                evaluationDTO.getOverallScore().compareTo(new BigDecimal("5.0")) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Overall score must be between 0 and 5");
            }
            evaluation.setOverallScore(evaluationDTO.getOverallScore());
        }
        
        if (evaluationDTO.getRemarks() != null) {
            evaluation.setRemarks(evaluationDTO.getRemarks());
        }
        
        if (evaluationDTO.getEvaluationDate() != null) {
            evaluation.setEvaluationDate(evaluationDTO.getEvaluationDate());
        }
        
        PerformanceEvaluationEntity updatedEvaluation = performanceEvaluationRepository.save(evaluation);
        return convertToDTO(updatedEvaluation);
    }

    /**
     * Delete a performance evaluation
     */
    @Transactional
    public void deletePerformanceEvaluation(String evaluationId) {
        if (!performanceEvaluationRepository.existsById(evaluationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Performance evaluation not found");
        }
        
        performanceEvaluationRepository.deleteById(evaluationId);
    }

    /**
     * Get all performance evaluations for the current employee
     */
    @Transactional(readOnly = true)
    public List<PerformanceEvaluationDTO> getCurrentEmployeeEvaluations() {
        EmployeeEntity employee = getCurrentEmployee();
        
        return performanceEvaluationRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated performance evaluations for the current employee
     */
    @Transactional(readOnly = true)
    public Page<PerformanceEvaluationDTO> getCurrentEmployeeEvaluations(Pageable pageable) {
        EmployeeEntity employee = getCurrentEmployee();
        
        return performanceEvaluationRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get all performance evaluations for a specific employee (HR/Admin only)
     */
    @Transactional(readOnly = true)
    public List<PerformanceEvaluationDTO> getEmployeeEvaluations(String employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return performanceEvaluationRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated performance evaluations for a specific employee (HR/Admin only)
     */
    @Transactional(readOnly = true)
    public Page<PerformanceEvaluationDTO> getEmployeeEvaluations(String employeeId, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return performanceEvaluationRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get the most recent performance evaluation for an employee
     */
    @Transactional(readOnly = true)
    public Optional<PerformanceEvaluationDTO> getMostRecentEvaluation(String employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        PerformanceEvaluationEntity evaluation = performanceEvaluationRepository.findFirstByEmployeeOrderByEvaluationDateDesc(employee);
        return evaluation != null ? Optional.of(convertToDTO(evaluation)) : Optional.empty();
    }

    /**
     * Check if the current user is authorized to access or modify an evaluation
     */
    public boolean isAuthorizedForEvaluation(PerformanceEvaluationEntity evaluation) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        // HR and Admin roles have access to all evaluations
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR") || a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // Employees can only access their own evaluations
        try {
            EmployeeEntity currentEmployee = getCurrentEmployee();
            return evaluation.getEmployee().getEmployeeId().equals(currentEmployee.getEmployeeId());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Convert a PerformanceEvaluationEntity to a PerformanceEvaluationDTO
     */
    private PerformanceEvaluationDTO convertToDTO(PerformanceEvaluationEntity entity) {
        PerformanceEvaluationDTO dto = new PerformanceEvaluationDTO();
        dto.setEvaluationId(entity.getEvaluationId());
        dto.setEmployeeId(entity.getEmployee().getEmployeeId());
        dto.setEmployeeName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName());
        dto.setReviewerId(entity.getReviewer().getUserId());
        
        // Try to get reviewer name from related employee if possible
        Optional<EmployeeEntity> reviewerEmployee = employeeRepository.findByEmail(entity.getReviewer().getEmailAddress());
        if (reviewerEmployee.isPresent()) {
            dto.setReviewerName(reviewerEmployee.get().getFirstName() + " " + reviewerEmployee.get().getLastName());
        } else {
            dto.setReviewerName("HR Staff");
        }
        
        dto.setEvaluationPeriodStart(entity.getEvaluationPeriodStart());
        dto.setEvaluationPeriodEnd(entity.getEvaluationPeriodEnd());
        dto.setOverallScore(entity.getOverallScore());
        dto.setRemarks(entity.getRemarks());
        dto.setEvaluationDate(entity.getEvaluationDate());
        
        return dto;
    }

    /**
     * Get the current employee from the security context
     */
    private EmployeeEntity getCurrentEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found for current user"));
    }

    /**
     * Get the current user account from the security context
     */
    private UserAccountEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return userAccountRepository.findByEmailAddress(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User account not found for current user"));
    }
} 