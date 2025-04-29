package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.BenefitDependentDTO;
import cit.edu.workforce.DTO.BenefitEnrollmentDTO;
import cit.edu.workforce.Entity.BenefitDependentEntity;
import cit.edu.workforce.Entity.BenefitEnrollmentEntity;
import cit.edu.workforce.Entity.BenefitPlanEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Repository.BenefitDependentRepository;
import cit.edu.workforce.Repository.BenefitEnrollmentRepository;
import cit.edu.workforce.Repository.BenefitPlanRepository;
import cit.edu.workforce.Repository.EmployeeRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BenefitEnrollmentService - Service for managing benefit enrollments
 * New file: This service provides methods for creating, reading, updating, and deleting benefit enrollments.
 */
@Service
public class BenefitEnrollmentService {

    private final BenefitEnrollmentRepository benefitEnrollmentRepository;
    private final BenefitPlanRepository benefitPlanRepository;
    private final EmployeeRepository employeeRepository;
    private final BenefitDependentRepository benefitDependentRepository;
    private final UserAccountService userAccountService;

    @Autowired
    public BenefitEnrollmentService(
            BenefitEnrollmentRepository benefitEnrollmentRepository,
            BenefitPlanRepository benefitPlanRepository,
            EmployeeRepository employeeRepository,
            BenefitDependentRepository benefitDependentRepository,
            UserAccountService userAccountService) {
        this.benefitEnrollmentRepository = benefitEnrollmentRepository;
        this.benefitPlanRepository = benefitPlanRepository;
        this.employeeRepository = employeeRepository;
        this.benefitDependentRepository = benefitDependentRepository;
        this.userAccountService = userAccountService;
    }

    /**
     * Get all benefit enrollments for the current employee
     *
     * @return List of benefit enrollment DTOs
     */
    public List<BenefitEnrollmentDTO> getCurrentEmployeeEnrollments() {
        EmployeeEntity employee = getCurrentEmployee();
        return benefitEnrollmentRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated benefit enrollments for the current employee
     *
     * @param pageable Pagination information
     * @return Page of benefit enrollment DTOs
     */
    public Page<BenefitEnrollmentDTO> getCurrentEmployeeEnrollments(Pageable pageable) {
        EmployeeEntity employee = getCurrentEmployee();
        return benefitEnrollmentRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get a benefit enrollment by ID
     *
     * @param enrollmentId Benefit enrollment ID
     * @return Benefit enrollment DTO
     */
    public BenefitEnrollmentDTO getEnrollmentById(String enrollmentId) {
        BenefitEnrollmentEntity enrollment = benefitEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit enrollment not found with ID: " + enrollmentId));

        // Check if the current user has access to this enrollment
        if (!isAuthorizedToAccessEnrollment(enrollment)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't have permission to access this enrollment");
        }

        return convertToDTO(enrollment);
    }

    /**
     * Get all benefit enrollments for a specific employee (HR/Admin only)
     *
     * @param employeeId Employee ID
     * @return List of benefit enrollment DTOs
     */
    public List<BenefitEnrollmentDTO> getEmployeeEnrollments(String employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with ID: " + employeeId));

        return benefitEnrollmentRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated benefit enrollments for a specific employee (HR/Admin only)
     *
     * @param employeeId Employee ID
     * @param pageable   Pagination information
     * @return Page of benefit enrollment DTOs
     */
    public Page<BenefitEnrollmentDTO> getEmployeeEnrollments(String employeeId, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with ID: " + employeeId));

        return benefitEnrollmentRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get all enrollments for a specific benefit plan (HR/Admin only)
     *
     * @param planId Benefit plan ID
     * @return List of benefit enrollment DTOs
     */
    public List<BenefitEnrollmentDTO> getPlanEnrollments(String planId) {
        BenefitPlanEntity benefitPlan = benefitPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit plan not found with ID: " + planId));

        return benefitEnrollmentRepository.findByBenefitPlan(benefitPlan).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated enrollments for a specific benefit plan (HR/Admin only)
     *
     * @param planId   Benefit plan ID
     * @param pageable Pagination information
     * @return Page of benefit enrollment DTOs
     */
    public Page<BenefitEnrollmentDTO> getPlanEnrollments(String planId, Pageable pageable) {
        BenefitPlanEntity benefitPlan = benefitPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit plan not found with ID: " + planId));

        return benefitEnrollmentRepository.findByBenefitPlan(benefitPlan, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Create a new benefit enrollment for the current employee
     *
     * @param planId Benefit plan ID
     * @return Created benefit enrollment DTO
     */
    @Transactional
    public BenefitEnrollmentDTO enrollInBenefitPlan(String planId) {
        EmployeeEntity employee = getCurrentEmployee();
        BenefitPlanEntity benefitPlan = benefitPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit plan not found with ID: " + planId));

        // Check if the benefit plan is active
        if (!benefitPlan.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot enroll in an inactive benefit plan");
        }

        // Check if the employee is already enrolled in this benefit plan
        if (benefitEnrollmentRepository.findByEmployeeAndBenefitPlan(employee, benefitPlan).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "You are already enrolled in this benefit plan");
        }

        BenefitEnrollmentEntity enrollment = new BenefitEnrollmentEntity();
        enrollment.setEmployee(employee);
        enrollment.setBenefitPlan(benefitPlan);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setStatus("Active");

        BenefitEnrollmentEntity savedEnrollment = benefitEnrollmentRepository.save(enrollment);
        return convertToDTO(savedEnrollment);
    }

    /**
     * Create a new benefit enrollment for any employee (HR/Admin only)
     *
     * @param employeeId Employee ID
     * @param planId     Benefit plan ID
     * @return Created benefit enrollment DTO
     */
    @Transactional
    public BenefitEnrollmentDTO enrollEmployeeInPlan(String employeeId, String planId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with ID: " + employeeId));

        BenefitPlanEntity benefitPlan = benefitPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit plan not found with ID: " + planId));

        // Check if the benefit plan is active
        if (!benefitPlan.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot enroll in an inactive benefit plan");
        }

        // Check if the employee is already enrolled in this benefit plan
        if (benefitEnrollmentRepository.findByEmployeeAndBenefitPlan(employee, benefitPlan).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Employee is already enrolled in this benefit plan");
        }

        BenefitEnrollmentEntity enrollment = new BenefitEnrollmentEntity();
        enrollment.setEmployee(employee);
        enrollment.setBenefitPlan(benefitPlan);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setStatus("Active");

        BenefitEnrollmentEntity savedEnrollment = benefitEnrollmentRepository.save(enrollment);
        return convertToDTO(savedEnrollment);
    }

    /**
     * Cancel a benefit enrollment
     *
     * @param enrollmentId       Benefit enrollment ID
     * @param cancellationReason Reason for cancellation
     * @return Cancelled benefit enrollment DTO
     */
    @Transactional
    public BenefitEnrollmentDTO cancelEnrollment(String enrollmentId, String cancellationReason) {
        BenefitEnrollmentEntity enrollment = benefitEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit enrollment not found with ID: " + enrollmentId));

        // Check if the current user has access to this enrollment
        if (!isAuthorizedToAccessEnrollment(enrollment)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't have permission to cancel this enrollment");
        }

        // Check if the enrollment is already cancelled
        if ("Cancelled".equals(enrollment.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "This enrollment is already cancelled");
        }

        enrollment.setStatus("Cancelled");
        enrollment.setCancellationReason(cancellationReason);

        BenefitEnrollmentEntity cancelledEnrollment = benefitEnrollmentRepository.save(enrollment);
        return convertToDTO(cancelledEnrollment);
    }

    /**
     * Check if the current user is authorized to access a benefit enrollment
     *
     * @param enrollment Benefit enrollment entity
     * @return true if authorized, false otherwise
     */
    public boolean isAuthorizedToAccessEnrollment(BenefitEnrollmentEntity enrollment) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        // HR and Admin roles have access to all enrollments
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR") || a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // Employees can only access their own enrollments
        try {
            EmployeeEntity currentEmployee = getCurrentEmployee();
            return enrollment.getEmployee().getEmployeeId().equals(currentEmployee.getEmployeeId());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the current employee from the security context
     *
     * @return Current employee entity
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
     * Convert a BenefitEnrollmentEntity to a BenefitEnrollmentDTO
     *
     * @param enrollment Benefit enrollment entity
     * @return Benefit enrollment DTO
     */
    private BenefitEnrollmentDTO convertToDTO(BenefitEnrollmentEntity enrollment) {
        BenefitEnrollmentDTO dto = new BenefitEnrollmentDTO();
        dto.setEnrollmentId(enrollment.getEnrollmentId());
        dto.setEmployeeId(enrollment.getEmployee().getEmployeeId());
        dto.setEmployeeName(enrollment.getEmployee().getFirstName() + " " + enrollment.getEmployee().getLastName());
        dto.setPlanId(enrollment.getBenefitPlan().getPlanId());
        dto.setPlanName(enrollment.getBenefitPlan().getPlanName());
        dto.setPlanType(enrollment.getBenefitPlan().getPlanType());
        dto.setEnrollmentDate(enrollment.getEnrollmentDate());
        dto.setStatus(enrollment.getStatus());
        dto.setCancellationReason(enrollment.getCancellationReason());
        
        // Convert dependents if any
        if (enrollment.getDependents() != null && !enrollment.getDependents().isEmpty()) {
            List<BenefitDependentDTO> dependentDTOs = enrollment.getDependents().stream()
                    .map(this::convertDependentToDTO)
                    .collect(Collectors.toList());
            dto.setDependents(dependentDTOs);
            dto.setDependentCount(dependentDTOs.size());
        } else {
            dto.setDependents(new ArrayList<>());
            dto.setDependentCount(0);
        }
        
        return dto;
    }

    /**
     * Convert a BenefitDependentEntity to a BenefitDependentDTO
     *
     * @param dependent Benefit dependent entity
     * @return Benefit dependent DTO
     */
    private BenefitDependentDTO convertDependentToDTO(BenefitDependentEntity dependent) {
        BenefitDependentDTO dto = new BenefitDependentDTO();
        dto.setDependentId(dependent.getDependentId());
        dto.setEnrollmentId(dependent.getBenefitEnrollment().getEnrollmentId());
        dto.setName(dependent.getName());
        dto.setRelationship(dependent.getRelationship());
        dto.setBirthdate(dependent.getBirthdate());
        return dto;
    }
} 