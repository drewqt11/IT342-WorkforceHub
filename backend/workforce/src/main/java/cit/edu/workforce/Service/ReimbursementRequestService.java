package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.ReimbursementRequestDTO;
import cit.edu.workforce.Entity.BenefitPlanEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.ReimbursementRequestEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.BenefitPlanRepository;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.ReimbursementRequestRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ReimbursementRequestService - Service for managing reimbursement requests
 * New file: This service provides methods for creating, reading, updating, and managing
 * reimbursement requests for employee benefits.
 */
@Service
public class ReimbursementRequestService {

    private final ReimbursementRequestRepository reimbursementRequestRepository;
    private final BenefitPlanRepository benefitPlanRepository;
    private final EmployeeRepository employeeRepository;
    private final UserAccountRepository userAccountRepository;

    @Autowired
    public ReimbursementRequestService(
            ReimbursementRequestRepository reimbursementRequestRepository,
            BenefitPlanRepository benefitPlanRepository,
            EmployeeRepository employeeRepository,
            UserAccountRepository userAccountRepository) {
        this.reimbursementRequestRepository = reimbursementRequestRepository;
        this.benefitPlanRepository = benefitPlanRepository;
        this.employeeRepository = employeeRepository;
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Get all reimbursement requests for the current employee
     *
     * @return List of reimbursement request DTOs
     */
    public List<ReimbursementRequestDTO> getCurrentEmployeeReimbursementRequests() {
        EmployeeEntity employee = getCurrentEmployee();
        return reimbursementRequestRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated reimbursement requests for the current employee
     *
     * @param pageable Pagination information
     * @return Page of reimbursement request DTOs
     */
    public Page<ReimbursementRequestDTO> getCurrentEmployeeReimbursementRequests(Pageable pageable) {
        EmployeeEntity employee = getCurrentEmployee();
        return reimbursementRequestRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get a reimbursement request by ID
     *
     * @param reimbursementId Reimbursement request ID
     * @return Reimbursement request DTO
     */
    public ReimbursementRequestDTO getReimbursementRequestById(String reimbursementId) {
        ReimbursementRequestEntity request = reimbursementRequestRepository.findById(reimbursementId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reimbursement request not found with ID: " + reimbursementId));

        // Check if the current user has access to this request
        if (!isAuthorizedToAccessRequest(request)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't have permission to access this reimbursement request");
        }

        return convertToDTO(request);
    }

    /**
     * Get all reimbursement requests (HR/Admin only)
     *
     * @param status    Status filter (optional)
     * @param pageable  Pagination information
     * @return Page of reimbursement request DTOs
     */
    public Page<ReimbursementRequestDTO> getAllReimbursementRequests(String status, Pageable pageable) {
        Page<ReimbursementRequestEntity> requests;
        
        if (status != null && !status.isEmpty()) {
            requests = reimbursementRequestRepository.findByStatus(status, pageable);
        } else {
            requests = reimbursementRequestRepository.findAll(pageable);
        }
        
        return requests.map(this::convertToDTO);
    }

    /**
     * Get all reimbursement requests for a specific employee (HR/Admin only)
     *
     * @param employeeId Employee ID
     * @param status     Status filter (optional)
     * @param pageable   Pagination information
     * @return Page of reimbursement request DTOs
     */
    public Page<ReimbursementRequestDTO> getEmployeeReimbursementRequests(
            String employeeId, String status, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with ID: " + employeeId));

        if (status != null && !status.isEmpty()) {
            return reimbursementRequestRepository.findByEmployeeAndStatus(employee, status, pageable)
                    .map(this::convertToDTO);
        } else {
            return reimbursementRequestRepository.findByEmployee(employee, pageable)
                    .map(this::convertToDTO);
        }
    }

    /**
     * Create a new reimbursement request for the current employee
     *
     * @param reimbursementRequestDTO Reimbursement request information
     * @return Created reimbursement request DTO
     */
    @Transactional
    public ReimbursementRequestDTO createReimbursementRequest(ReimbursementRequestDTO reimbursementRequestDTO) {
        // Validate request
        validateReimbursementRequest(reimbursementRequestDTO);
        
        EmployeeEntity employee = getCurrentEmployee();
        BenefitPlanEntity benefitPlan = benefitPlanRepository.findById(reimbursementRequestDTO.getPlanId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit plan not found with ID: " + reimbursementRequestDTO.getPlanId()));

        // Check if the benefit plan is active
        if (!benefitPlan.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot submit reimbursement for an inactive benefit plan");
        }

        ReimbursementRequestEntity request = new ReimbursementRequestEntity();
        request.setEmployee(employee);
        request.setBenefitPlan(benefitPlan);
        request.setRequestDate(LocalDate.now());
        request.setExpenseDate(reimbursementRequestDTO.getExpenseDate());
        request.setAmountRequested(reimbursementRequestDTO.getAmountRequested());
        request.setDocumentPath(reimbursementRequestDTO.getDocumentPath());
        request.setReason(reimbursementRequestDTO.getReason());
        request.setStatus("PENDING");

        ReimbursementRequestEntity savedRequest = reimbursementRequestRepository.save(request);
        return convertToDTO(savedRequest);
    }

    /**
     * Create a reimbursement request for any employee (HR/Admin only)
     *
     * @param employeeId             Employee ID
     * @param reimbursementRequestDTO Reimbursement request information
     * @return Created reimbursement request DTO
     */
    @Transactional
    public ReimbursementRequestDTO createEmployeeReimbursementRequest(
            String employeeId, ReimbursementRequestDTO reimbursementRequestDTO) {
        // Validate request
        validateReimbursementRequest(reimbursementRequestDTO);
        
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with ID: " + employeeId));

        BenefitPlanEntity benefitPlan = benefitPlanRepository.findById(reimbursementRequestDTO.getPlanId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benefit plan not found with ID: " + reimbursementRequestDTO.getPlanId()));

        // Check if the benefit plan is active
        if (!benefitPlan.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot submit reimbursement for an inactive benefit plan");
        }

        ReimbursementRequestEntity request = new ReimbursementRequestEntity();
        request.setEmployee(employee);
        request.setBenefitPlan(benefitPlan);
        request.setRequestDate(LocalDate.now());
        request.setExpenseDate(reimbursementRequestDTO.getExpenseDate());
        request.setAmountRequested(reimbursementRequestDTO.getAmountRequested());
        request.setDocumentPath(reimbursementRequestDTO.getDocumentPath());
        request.setReason(reimbursementRequestDTO.getReason());
        request.setStatus("PENDING");

        ReimbursementRequestEntity savedRequest = reimbursementRequestRepository.save(request);
        return convertToDTO(savedRequest);
    }

    /**
     * Update an existing reimbursement request
     *
     * @param reimbursementId         Reimbursement request ID
     * @param reimbursementRequestDTO Updated reimbursement request information
     * @return Updated reimbursement request DTO
     */
    @Transactional
    public ReimbursementRequestDTO updateReimbursementRequest(
            String reimbursementId, ReimbursementRequestDTO reimbursementRequestDTO) {
        ReimbursementRequestEntity request = reimbursementRequestRepository.findById(reimbursementId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reimbursement request not found with ID: " + reimbursementId));

        // Check if the current user is the owner of this request
        EmployeeEntity currentEmployee = getCurrentEmployee();
        if (!request.getEmployee().getEmployeeId().equals(currentEmployee.getEmployeeId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't have permission to update this reimbursement request");
        }

        // Check if the request is still pending
        if (!"PENDING".equals(request.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot update a reimbursement request that has been reviewed");
        }

        // Update fields
        if (reimbursementRequestDTO.getExpenseDate() != null) {
            request.setExpenseDate(reimbursementRequestDTO.getExpenseDate());
        }
        
        if (reimbursementRequestDTO.getAmountRequested() != null) {
            request.setAmountRequested(reimbursementRequestDTO.getAmountRequested());
        }
        
        if (reimbursementRequestDTO.getDocumentPath() != null) {
            request.setDocumentPath(reimbursementRequestDTO.getDocumentPath());
        }
        
        if (reimbursementRequestDTO.getReason() != null && !reimbursementRequestDTO.getReason().trim().isEmpty()) {
            request.setReason(reimbursementRequestDTO.getReason());
        }

        ReimbursementRequestEntity updatedRequest = reimbursementRequestRepository.save(request);
        return convertToDTO(updatedRequest);
    }

    /**
     * Delete a pending reimbursement request
     *
     * @param reimbursementId Reimbursement request ID
     */
    @Transactional
    public void deleteReimbursementRequest(String reimbursementId) {
        ReimbursementRequestEntity request = reimbursementRequestRepository.findById(reimbursementId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reimbursement request not found with ID: " + reimbursementId));

        // Check if the current user is the owner of this request
        EmployeeEntity currentEmployee = getCurrentEmployee();
        if (!request.getEmployee().getEmployeeId().equals(currentEmployee.getEmployeeId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't have permission to delete this reimbursement request");
        }

        // Check if the request is still pending
        if (!"PENDING".equals(request.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot delete a reimbursement request that has been reviewed");
        }

        reimbursementRequestRepository.delete(request);
    }

    /**
     * Approve a reimbursement request (HR/Admin only)
     *
     * @param reimbursementId Reimbursement request ID
     * @param remarks         Approval remarks (optional)
     * @return Approved reimbursement request DTO
     */
    @Transactional
    public ReimbursementRequestDTO approveReimbursementRequest(String reimbursementId, String remarks) {
        ReimbursementRequestEntity request = reimbursementRequestRepository.findById(reimbursementId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reimbursement request not found with ID: " + reimbursementId));

        // Check if the request is still pending
        if (!"PENDING".equals(request.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "This reimbursement request has already been reviewed");
        }

        // Get current user
        UserAccountEntity currentUser = getCurrentUserAccount();

        // Update request status
        request.setStatus("APPROVED");
        request.setReviewedBy(currentUser);
        request.setReviewedAt(LocalDateTime.now());
        request.setRemarks(remarks);

        ReimbursementRequestEntity approvedRequest = reimbursementRequestRepository.save(request);
        return convertToDTO(approvedRequest);
    }

    /**
     * Reject a reimbursement request (HR/Admin only)
     *
     * @param reimbursementId Reimbursement request ID
     * @param remarks         Rejection reason (optional)
     * @return Rejected reimbursement request DTO
     */
    @Transactional
    public ReimbursementRequestDTO rejectReimbursementRequest(String reimbursementId, String remarks) {
        ReimbursementRequestEntity request = reimbursementRequestRepository.findById(reimbursementId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reimbursement request not found with ID: " + reimbursementId));

        // Check if the request is still pending
        if (!"PENDING".equals(request.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "This reimbursement request has already been reviewed");
        }

        // Get current user
        UserAccountEntity currentUser = getCurrentUserAccount();

        // Update request status
        request.setStatus("REJECTED");
        request.setReviewedBy(currentUser);
        request.setReviewedAt(LocalDateTime.now());
        request.setRemarks(remarks);

        ReimbursementRequestEntity rejectedRequest = reimbursementRequestRepository.save(request);
        return convertToDTO(rejectedRequest);
    }

    /**
     * Check if the current user is authorized to access a reimbursement request
     *
     * @param request Reimbursement request entity
     * @return true if authorized, false otherwise
     */
    private boolean isAuthorizedToAccessRequest(ReimbursementRequestEntity request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        // HR and Admin roles have access to all requests
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR") || a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // Employees can only access their own requests
        try {
            EmployeeEntity currentEmployee = getCurrentEmployee();
            return request.getEmployee().getEmployeeId().equals(currentEmployee.getEmployeeId());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate a reimbursement request
     *
     * @param reimbursementRequestDTO Reimbursement request to validate
     */
    private void validateReimbursementRequest(ReimbursementRequestDTO reimbursementRequestDTO) {
        if (reimbursementRequestDTO.getPlanId() == null || reimbursementRequestDTO.getPlanId().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Benefit plan ID is required");
        }
        
        if (reimbursementRequestDTO.getExpenseDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expense date is required");
        }
        
        if (reimbursementRequestDTO.getExpenseDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expense date cannot be in the future");
        }
        
        if (reimbursementRequestDTO.getAmountRequested() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount requested is required");
        }
        
        if (reimbursementRequestDTO.getAmountRequested().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount requested must be greater than zero");
        }
        
        if (reimbursementRequestDTO.getReason() == null || reimbursementRequestDTO.getReason().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reason is required");
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
     * Get the current user account from the security context
     *
     * @return Current user account entity
     */
    private UserAccountEntity getCurrentUserAccount() {
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

    /**
     * Convert a ReimbursementRequestEntity to a ReimbursementRequestDTO
     *
     * @param request Reimbursement request entity
     * @return Reimbursement request DTO
     */
    private ReimbursementRequestDTO convertToDTO(ReimbursementRequestEntity request) {
        ReimbursementRequestDTO dto = new ReimbursementRequestDTO();
        dto.setReimbursementId(request.getReimbursementId());
        dto.setEmployeeId(request.getEmployee().getEmployeeId());
        dto.setEmployeeName(request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName());
        dto.setPlanId(request.getBenefitPlan().getPlanId());
        dto.setPlanName(request.getBenefitPlan().getPlanName());
        dto.setPlanType(request.getBenefitPlan().getPlanType());
        dto.setRequestDate(request.getRequestDate());
        dto.setExpenseDate(request.getExpenseDate());
        dto.setAmountRequested(request.getAmountRequested());
        dto.setDocumentPath(request.getDocumentPath());
        dto.setReason(request.getReason());
        dto.setStatus(request.getStatus());
        
        if (request.getReviewedBy() != null) {
            dto.setReviewedById(request.getReviewedBy().getUserId());
            dto.setReviewedByName(request.getReviewedBy().getEmailAddress()); // Use email if no name available
        }
        
        dto.setReviewedAt(request.getReviewedAt());
        dto.setRemarks(request.getRemarks());
        
        return dto;
    }
} 