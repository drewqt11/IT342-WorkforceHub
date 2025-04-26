package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.LeaveBalanceDTO;
import cit.edu.workforce.DTO.LeaveRequestDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.LeaveBalanceEntity;
import cit.edu.workforce.Entity.LeaveRequestEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.LeaveBalanceRepository;
import cit.edu.workforce.Repository.LeaveRequestRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * LeaveService - Service for managing leave requests and balances
 * New file: Provides functionality for creating and managing leave requests and balances
 */
@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserAccountRepository userAccountRepository;

    @Autowired
    public LeaveService(
            LeaveRequestRepository leaveRequestRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            EmployeeRepository employeeRepository,
            UserAccountRepository userAccountRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.employeeRepository = employeeRepository;
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Create a new leave request
     */
    @Transactional
    public LeaveRequestDTO createLeaveRequest(LeaveRequestDTO leaveRequestDTO) {
        EmployeeEntity employee = getCurrentEmployee();
        
        // Validate leave type
        String leaveType = leaveRequestDTO.getLeaveType();
        if (leaveType == null || leaveType.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave type is required");
        }
        
        // Validate dates
        LocalDate startDate = leaveRequestDTO.getStartDate();
        LocalDate endDate = leaveRequestDTO.getEndDate();
        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end dates are required");
        }
        
        if (startDate.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot request leave for past dates");
        }
        
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }
        
        // Check if employee has any overlapping leave requests
        List<LeaveRequestEntity> overlappingRequests = leaveRequestRepository.findOverlappingLeaveRequests(
                employee, startDate, endDate);
        if (!overlappingRequests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You already have a leave request for this period");
        }
        
        // Calculate total days
        BigDecimal totalDays = calculateTotalLeaveDays(startDate, endDate);
        
        // Check leave balance
        LeaveBalanceEntity leaveBalance = leaveBalanceRepository.findByEmployeeAndLeaveType(employee, leaveType)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "No leave balance found for leave type: " + leaveType));
        
        if (leaveBalance.getRemainingDays().compareTo(totalDays) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Insufficient leave balance. Available: " + leaveBalance.getRemainingDays() + 
                    ", Requested: " + totalDays);
        }
        
        // Create leave request
        LeaveRequestEntity leaveRequest = new LeaveRequestEntity();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(startDate);
        leaveRequest.setEndDate(endDate);
        leaveRequest.setTotalDays(totalDays);
        leaveRequest.setReason(leaveRequestDTO.getReason());
        leaveRequest.setStatus("PENDING");
        
        LeaveRequestEntity savedRequest = leaveRequestRepository.save(leaveRequest);
        return convertToDTO(savedRequest);
    }

    /**
     * Get leave requests for the current employee
     */
    @Transactional(readOnly = true)
    public List<LeaveRequestDTO> getCurrentEmployeeLeaveRequests() {
        EmployeeEntity employee = getCurrentEmployee();
        
        return leaveRequestRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get leave requests for the current employee with pagination
     */
    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getCurrentEmployeeLeaveRequests(Pageable pageable) {
        EmployeeEntity employee = getCurrentEmployee();
        
        return leaveRequestRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get leave requests for the current employee with a specific status
     */
    @Transactional(readOnly = true)
    public List<LeaveRequestDTO> getCurrentEmployeeLeaveRequestsByStatus(String status) {
        EmployeeEntity employee = getCurrentEmployee();
        
        return leaveRequestRepository.findByEmployeeAndStatus(employee, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get leave request by ID
     */
    @Transactional(readOnly = true)
    public Optional<LeaveRequestDTO> getLeaveRequestById(String leaveId) {
        return leaveRequestRepository.findById(leaveId)
                .map(this::convertToDTO);
    }

    /**
     * Cancel a leave request
     */
    @Transactional
    public LeaveRequestDTO cancelLeaveRequest(String leaveId) {
        EmployeeEntity employee = getCurrentEmployee();
        
        LeaveRequestEntity leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found"));
        
        // Verify that the leave request belongs to the current employee
        if (!leaveRequest.getEmployee().getEmployeeId().equals(employee.getEmployeeId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own leave requests");
        }
        
        // Verify that the leave request is in PENDING status
        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Only pending leave requests can be canceled");
        }
        
        // Update status to CANCELED
        leaveRequest.setStatus("CANCELED");
        
        LeaveRequestEntity updatedRequest = leaveRequestRepository.save(leaveRequest);
        return convertToDTO(updatedRequest);
    }

    /**
     * Update a leave request (HR only)
     */
    @Transactional
    public LeaveRequestDTO updateLeaveRequest(String leaveId, LeaveRequestDTO leaveRequestDTO) {
        LeaveRequestEntity leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found"));
        
        // Update fields
        if (leaveRequestDTO.getLeaveType() != null && !leaveRequestDTO.getLeaveType().isEmpty()) {
            leaveRequest.setLeaveType(leaveRequestDTO.getLeaveType());
        }
        
        if (leaveRequestDTO.getStartDate() != null && leaveRequestDTO.getEndDate() != null) {
            if (leaveRequestDTO.getEndDate().isBefore(leaveRequestDTO.getStartDate())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
            }
            
            leaveRequest.setStartDate(leaveRequestDTO.getStartDate());
            leaveRequest.setEndDate(leaveRequestDTO.getEndDate());
            
            // Recalculate total days
            BigDecimal totalDays = calculateTotalLeaveDays(
                    leaveRequestDTO.getStartDate(), leaveRequestDTO.getEndDate());
            leaveRequest.setTotalDays(totalDays);
        }
        
        if (leaveRequestDTO.getReason() != null) {
            leaveRequest.setReason(leaveRequestDTO.getReason());
        }
        
        LeaveRequestEntity updatedRequest = leaveRequestRepository.save(leaveRequest);
        return convertToDTO(updatedRequest);
    }

    /**
     * Approve or reject a leave request (HR only)
     */
    @Transactional
    public LeaveRequestDTO reviewLeaveRequest(String leaveId, String status) {
        LeaveRequestEntity leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found"));
        
        // Validate status
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Status must be either APPROVED or REJECTED");
        }
        
        // Verify that the leave request is in PENDING status
        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Only pending leave requests can be reviewed");
        }
        
        // Update leave request
        leaveRequest.setStatus(status);
        leaveRequest.setReviewedBy(getCurrentUser());
        leaveRequest.setReviewedAt(LocalDateTime.now());
        
        // Update leave balance if approved
        if ("APPROVED".equals(status)) {
            updateLeaveBalance(leaveRequest);
        }
        
        LeaveRequestEntity updatedRequest = leaveRequestRepository.save(leaveRequest);
        return convertToDTO(updatedRequest);
    }

    /**
     * Get all leave requests with a specific status (HR only)
     */
    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getLeaveRequestsByStatus(String status, Pageable pageable) {
        return leaveRequestRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get leave requests for a specific employee (HR only)
     */
    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getEmployeeLeaveRequests(String employeeId, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return leaveRequestRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get leave balances for the current employee
     */
    @Transactional(readOnly = true)
    public List<LeaveBalanceDTO> getCurrentEmployeeLeaveBalances() {
        EmployeeEntity employee = getCurrentEmployee();
        
        return leaveBalanceRepository.findByEmployee(employee).stream()
                .map(this::convertToLeaveBalanceDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get leave balance for the current employee and leave type
     */
    @Transactional(readOnly = true)
    public Optional<LeaveBalanceDTO> getCurrentEmployeeLeaveBalance(String leaveType) {
        EmployeeEntity employee = getCurrentEmployee();
        
        return leaveBalanceRepository.findByEmployeeAndLeaveType(employee, leaveType)
                .map(this::convertToLeaveBalanceDTO);
    }

    /**
     * Get leave balances for a specific employee (HR only)
     */
    @Transactional(readOnly = true)
    public List<LeaveBalanceDTO> getEmployeeLeaveBalances(String employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return leaveBalanceRepository.findByEmployee(employee).stream()
                .map(this::convertToLeaveBalanceDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create or update a leave balance (HR only)
     */
    @Transactional
    public LeaveBalanceDTO createOrUpdateLeaveBalance(String employeeId, LeaveBalanceDTO leaveBalanceDTO) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        // Validate leave type
        String leaveType = leaveBalanceDTO.getLeaveType();
        if (leaveType == null || leaveType.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave type is required");
        }
        
        // Check if leave balance already exists
        LeaveBalanceEntity leaveBalance = leaveBalanceRepository
                .findByEmployeeAndLeaveType(employee, leaveType)
                .orElse(new LeaveBalanceEntity());
        
        // Set/update fields
        if (leaveBalance.getBalanceId() == null) {
            leaveBalance.setEmployee(employee);
            leaveBalance.setLeaveType(leaveType);
        }
        
        if (leaveBalanceDTO.getAllocatedDays() != null) {
            // Calculate remaining days
            BigDecimal usedDays = leaveBalance.getUsedDays() != null ? 
                    leaveBalance.getUsedDays() : BigDecimal.ZERO;
            BigDecimal remainingDays = leaveBalanceDTO.getAllocatedDays().subtract(usedDays);
            
            leaveBalance.setAllocatedDays(leaveBalanceDTO.getAllocatedDays());
            leaveBalance.setRemainingDays(remainingDays);
        }
        
        if (leaveBalanceDTO.getResetDate() != null) {
            leaveBalance.setResetDate(leaveBalanceDTO.getResetDate());
        } else if (leaveBalance.getResetDate() == null) {
            // Set default reset date to one year from now
            leaveBalance.setResetDate(LocalDate.now().plusYears(1));
        }
        
        LeaveBalanceEntity savedBalance = leaveBalanceRepository.save(leaveBalance);
        return convertToLeaveBalanceDTO(savedBalance);
    }

    /**
     * Update leave balance when a leave request is approved
     */
    private void updateLeaveBalance(LeaveRequestEntity leaveRequest) {
        LeaveBalanceEntity leaveBalance = leaveBalanceRepository
                .findByEmployeeAndLeaveType(leaveRequest.getEmployee(), leaveRequest.getLeaveType())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "No leave balance found for leave type: " + leaveRequest.getLeaveType()));
        
        // Update used and remaining days
        BigDecimal newUsedDays = leaveBalance.getUsedDays().add(leaveRequest.getTotalDays());
        BigDecimal newRemainingDays = leaveBalance.getAllocatedDays().subtract(newUsedDays);
        
        if (newRemainingDays.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Insufficient leave balance. This request would result in a negative balance.");
        }
        
        leaveBalance.setUsedDays(newUsedDays);
        leaveBalance.setRemainingDays(newRemainingDays);
        
        leaveBalanceRepository.save(leaveBalance);
    }

    /**
     * Calculate total days between start and end dates
     */
    private BigDecimal calculateTotalLeaveDays(LocalDate startDate, LocalDate endDate) {
        if (startDate.equals(endDate)) {
            return BigDecimal.ONE;
        }
        
        int days = Period.between(startDate, endDate).getDays() + 1; // inclusive
        return new BigDecimal(days);
    }

    /**
     * Convert LeaveRequestEntity to LeaveRequestDTO
     */
    private LeaveRequestDTO convertToDTO(LeaveRequestEntity entity) {
        if (entity == null) {
            return null;
        }
        
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setLeaveId(entity.getLeaveId());
        dto.setEmployeeId(entity.getEmployee().getEmployeeId());
        dto.setEmployeeName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName());
        dto.setLeaveType(entity.getLeaveType());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setTotalDays(entity.getTotalDays());
        dto.setReason(entity.getReason());
        dto.setStatus(entity.getStatus());
        
        if (entity.getReviewedBy() != null) {
            dto.setReviewedBy(entity.getReviewedBy().getUserId());
        }
        
        if (entity.getReviewedAt() != null) {
            dto.setReviewedAt(entity.getReviewedAt().toLocalDate());
        }
        
        return dto;
    }

    /**
     * Convert LeaveBalanceEntity to LeaveBalanceDTO
     */
    private LeaveBalanceDTO convertToLeaveBalanceDTO(LeaveBalanceEntity entity) {
        if (entity == null) {
            return null;
        }
        
        LeaveBalanceDTO dto = new LeaveBalanceDTO();
        dto.setBalanceId(entity.getBalanceId());
        dto.setEmployeeId(entity.getEmployee().getEmployeeId());
        dto.setEmployeeName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName());
        dto.setLeaveType(entity.getLeaveType());
        dto.setAllocatedDays(entity.getAllocatedDays());
        dto.setUsedDays(entity.getUsedDays());
        dto.setRemainingDays(entity.getRemainingDays());
        dto.setResetDate(entity.getResetDate());
        
        return dto;
    }

    /**
     * Get the currently authenticated employee
     */
    private EmployeeEntity getCurrentEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        return employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Employee not found"));
    }

    /**
     * Get the currently authenticated user
     */
    private UserAccountEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        return userAccountRepository.findByEmailAddress(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
} 