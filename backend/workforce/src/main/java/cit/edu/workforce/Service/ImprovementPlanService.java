package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.ImprovementPlanDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.ImprovementPlanEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.ImprovementPlanRepository;
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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ImprovementPlanService - Service for managing performance improvement plans
 * New file: Provides functionality for creating and managing performance improvement plans
 */
@Service
public class ImprovementPlanService {

    private final ImprovementPlanRepository improvementPlanRepository;
    private final EmployeeRepository employeeRepository;
    private final UserAccountRepository userAccountRepository;
    
    private static final List<String> VALID_STATUSES = Arrays.asList("Open", "Completed", "Cancelled");

    @Autowired
    public ImprovementPlanService(
            ImprovementPlanRepository improvementPlanRepository,
            EmployeeRepository employeeRepository,
            UserAccountRepository userAccountRepository) {
        this.improvementPlanRepository = improvementPlanRepository;
        this.employeeRepository = employeeRepository;
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Create a new performance improvement plan
     */
    @Transactional
    public ImprovementPlanDTO createImprovementPlan(ImprovementPlanDTO improvementPlanDTO) {
        // Validate input
        if (improvementPlanDTO.getEmployeeId() == null || improvementPlanDTO.getEmployeeId().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee ID is required");
        }
        
        if (improvementPlanDTO.getReason() == null || improvementPlanDTO.getReason().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reason is required");
        }
        
        if (improvementPlanDTO.getActionSteps() == null || improvementPlanDTO.getActionSteps().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action steps are required");
        }
        
        if (improvementPlanDTO.getStartDate() == null || improvementPlanDTO.getEndDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end dates are required");
        }
        
        if (improvementPlanDTO.getEndDate().isBefore(improvementPlanDTO.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }
        
        // Get the employee
        EmployeeEntity employee = employeeRepository.findById(improvementPlanDTO.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        // Get the current user as initiator
        UserAccountEntity initiator = getCurrentUser();
        
        // Create the improvement plan
        ImprovementPlanEntity plan = new ImprovementPlanEntity();
        plan.setEmployee(employee);
        plan.setInitiator(initiator);
        plan.setReason(improvementPlanDTO.getReason());
        plan.setActionSteps(improvementPlanDTO.getActionSteps());
        plan.setStartDate(improvementPlanDTO.getStartDate());
        plan.setEndDate(improvementPlanDTO.getEndDate());
        
        // Set status (default to "Open" if not provided)
        String status = improvementPlanDTO.getStatus();
        if (status == null || status.isEmpty()) {
            status = "Open";
        } else if (!VALID_STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Invalid status. Must be one of: " + String.join(", ", VALID_STATUSES));
        }
        plan.setStatus(status);
        
        ImprovementPlanEntity savedPlan = improvementPlanRepository.save(plan);
        return convertToDTO(savedPlan);
    }

    /**
     * Get a specific improvement plan by ID
     */
    @Transactional(readOnly = true)
    public Optional<ImprovementPlanDTO> getImprovementPlanById(String planId) {
        return improvementPlanRepository.findById(planId)
                .map(this::convertToDTO);
    }

    /**
     * Update an existing improvement plan
     */
    @Transactional
    public ImprovementPlanDTO updateImprovementPlan(String planId, ImprovementPlanDTO planDTO) {
        ImprovementPlanEntity plan = improvementPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Improvement plan not found"));
        
        // Update fields if provided
        if (planDTO.getReason() != null && !planDTO.getReason().isEmpty()) {
            plan.setReason(planDTO.getReason());
        }
        
        if (planDTO.getActionSteps() != null && !planDTO.getActionSteps().isEmpty()) {
            plan.setActionSteps(planDTO.getActionSteps());
        }
        
        if (planDTO.getStartDate() != null) {
            plan.setStartDate(planDTO.getStartDate());
        }
        
        if (planDTO.getEndDate() != null) {
            plan.setEndDate(planDTO.getEndDate());
        }
        
        // Validate date range
        if (plan.getEndDate().isBefore(plan.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }
        
        if (planDTO.getStatus() != null && !planDTO.getStatus().isEmpty()) {
            if (!VALID_STATUSES.contains(planDTO.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Invalid status. Must be one of: " + String.join(", ", VALID_STATUSES));
            }
            plan.setStatus(planDTO.getStatus());
        }
        
        ImprovementPlanEntity updatedPlan = improvementPlanRepository.save(plan);
        return convertToDTO(updatedPlan);
    }

    /**
     * Update the status of an improvement plan
     */
    @Transactional
    public ImprovementPlanDTO updateImprovementPlanStatus(String planId, String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Invalid status. Must be one of: " + String.join(", ", VALID_STATUSES));
        }
        
        ImprovementPlanEntity plan = improvementPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Improvement plan not found"));
        
        plan.setStatus(status);
        ImprovementPlanEntity updatedPlan = improvementPlanRepository.save(plan);
        return convertToDTO(updatedPlan);
    }

    /**
     * Delete an improvement plan
     */
    @Transactional
    public void deleteImprovementPlan(String planId) {
        if (!improvementPlanRepository.existsById(planId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Improvement plan not found");
        }
        
        improvementPlanRepository.deleteById(planId);
    }

    /**
     * Get all improvement plans for a specific employee
     */
    @Transactional(readOnly = true)
    public List<ImprovementPlanDTO> getEmployeeImprovementPlans(String employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return improvementPlanRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated improvement plans for a specific employee
     */
    @Transactional(readOnly = true)
    public Page<ImprovementPlanDTO> getEmployeeImprovementPlans(String employeeId, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return improvementPlanRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get all active improvement plans for a specific employee
     */
    @Transactional(readOnly = true)
    public List<ImprovementPlanDTO> getEmployeeActiveImprovementPlans(String employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return improvementPlanRepository.findByEmployeeAndStatus(employee, "Open").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all improvement plans by status
     */
    @Transactional(readOnly = true)
    public Page<ImprovementPlanDTO> getImprovementPlansByStatus(String status, Pageable pageable) {
        if (!VALID_STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Invalid status. Must be one of: " + String.join(", ", VALID_STATUSES));
        }
        
        return improvementPlanRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Find plans expiring soon
     */
    @Transactional(readOnly = true)
    public List<ImprovementPlanDTO> getPlansExpiringSoon(int days) {
        LocalDate futureDate = LocalDate.now().plusDays(days);
        
        return improvementPlanRepository.findPlansExpiringBy(futureDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check if the current user is authorized to access or modify a plan
     */
    public boolean isAuthorizedForImprovementPlan(ImprovementPlanEntity plan) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        // HR and Admin roles have access to all plans
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR") || a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // Employees can only access their own plans
        try {
            EmployeeEntity currentEmployee = getCurrentEmployee();
            return plan.getEmployee().getEmployeeId().equals(currentEmployee.getEmployeeId());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Convert an ImprovementPlanEntity to an ImprovementPlanDTO
     */
    private ImprovementPlanDTO convertToDTO(ImprovementPlanEntity entity) {
        ImprovementPlanDTO dto = new ImprovementPlanDTO();
        dto.setPlanId(entity.getPlanId());
        dto.setEmployeeId(entity.getEmployee().getEmployeeId());
        dto.setEmployeeName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName());
        dto.setInitiatorId(entity.getInitiator().getUserId());
        
        // Try to get initiator name from related employee if possible
        Optional<EmployeeEntity> initiatorEmployee = employeeRepository.findByEmail(entity.getInitiator().getEmailAddress());
        if (initiatorEmployee.isPresent()) {
            dto.setInitiatorName(initiatorEmployee.get().getFirstName() + " " + initiatorEmployee.get().getLastName());
        } else {
            dto.setInitiatorName("HR Staff");
        }
        
        dto.setReason(entity.getReason());
        dto.setActionSteps(entity.getActionSteps());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setStatus(entity.getStatus());
        
        // Calculate days remaining for active plans
        if ("Open".equals(entity.getStatus())) {
            long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), entity.getEndDate());
            dto.setDaysRemaining(Math.max(0, daysRemaining));
        } else {
            dto.setDaysRemaining(0);
        }
        
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