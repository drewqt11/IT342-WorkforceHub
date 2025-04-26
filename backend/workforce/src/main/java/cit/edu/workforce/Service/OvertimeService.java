package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.OvertimeRequestDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.OvertimeRequestEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.OvertimeRequestRepository;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * OvertimeService - Service for managing overtime requests
 * New file: Provides functionality for creating and managing overtime requests
 */
@Service
public class OvertimeService {

    private final OvertimeRequestRepository overtimeRepository;
    private final EmployeeRepository employeeRepository;
    private final UserAccountRepository userAccountRepository;

    @Autowired
    public OvertimeService(
            OvertimeRequestRepository overtimeRepository,
            EmployeeRepository employeeRepository,
            UserAccountRepository userAccountRepository) {
        this.overtimeRepository = overtimeRepository;
        this.employeeRepository = employeeRepository;
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Create a new overtime request
     */
    @Transactional
    public OvertimeRequestDTO createOvertimeRequest(OvertimeRequestDTO overtimeRequestDTO) {
        EmployeeEntity employee = getCurrentEmployee();
        
        // Validate required fields
        if (overtimeRequestDTO.getDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required");
        }
        
        if (overtimeRequestDTO.getStartTime() == null || overtimeRequestDTO.getEndTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end times are required");
        }
        
        if (overtimeRequestDTO.getReason() == null || overtimeRequestDTO.getReason().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reason is required");
        }
        
        // Check if end time is after start time
        if (overtimeRequestDTO.getEndTime().isBefore(overtimeRequestDTO.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }
        
        // Check if overtime is at least 30 minutes
        Duration duration = Duration.between(overtimeRequestDTO.getStartTime(), overtimeRequestDTO.getEndTime());
        if (duration.toMinutes() < 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Overtime must be at least 30 minutes");
        }
        
        // Calculate total hours
        double hours = duration.toMinutes() / 60.0;
        BigDecimal totalHours = BigDecimal.valueOf(hours).setScale(2, BigDecimal.ROUND_HALF_UP);
        
        // Create overtime request
        OvertimeRequestEntity overtimeRequest = new OvertimeRequestEntity();
        overtimeRequest.setEmployee(employee);
        overtimeRequest.setDate(overtimeRequestDTO.getDate());
        overtimeRequest.setStartTime(overtimeRequestDTO.getStartTime());
        overtimeRequest.setEndTime(overtimeRequestDTO.getEndTime());
        overtimeRequest.setTotalHours(totalHours);
        overtimeRequest.setReason(overtimeRequestDTO.getReason());
        overtimeRequest.setStatus("PENDING");
        
        OvertimeRequestEntity savedRequest = overtimeRepository.save(overtimeRequest);
        return convertToDTO(savedRequest);
    }

    /**
     * Get overtime requests for the current employee
     */
    @Transactional(readOnly = true)
    public List<OvertimeRequestDTO> getCurrentEmployeeOvertimeRequests() {
        EmployeeEntity employee = getCurrentEmployee();
        
        return overtimeRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get overtime requests for the current employee with pagination
     */
    @Transactional(readOnly = true)
    public Page<OvertimeRequestDTO> getCurrentEmployeeOvertimeRequests(Pageable pageable) {
        EmployeeEntity employee = getCurrentEmployee();
        
        return overtimeRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get overtime requests for the current employee with a specific status
     */
    @Transactional(readOnly = true)
    public List<OvertimeRequestDTO> getCurrentEmployeeOvertimeRequestsByStatus(String status) {
        EmployeeEntity employee = getCurrentEmployee();
        
        return overtimeRepository.findByEmployeeAndStatus(employee, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get overtime request by ID
     */
    @Transactional(readOnly = true)
    public Optional<OvertimeRequestDTO> getOvertimeRequestById(String otRequestId) {
        return overtimeRepository.findById(otRequestId)
                .map(this::convertToDTO);
    }

    /**
     * Cancel an overtime request
     */
    @Transactional
    public OvertimeRequestDTO cancelOvertimeRequest(String otRequestId) {
        EmployeeEntity employee = getCurrentEmployee();
        
        OvertimeRequestEntity overtimeRequest = overtimeRepository.findById(otRequestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Overtime request not found"));
        
        // Verify that the overtime request belongs to the current employee
        if (!overtimeRequest.getEmployee().getEmployeeId().equals(employee.getEmployeeId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own overtime requests");
        }
        
        // Verify that the overtime request is in PENDING status
        if (!"PENDING".equals(overtimeRequest.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Only pending overtime requests can be canceled");
        }
        
        // Update status to CANCELED
        overtimeRequest.setStatus("CANCELED");
        
        OvertimeRequestEntity updatedRequest = overtimeRepository.save(overtimeRequest);
        return convertToDTO(updatedRequest);
    }

    /**
     * Update an overtime request (HR only)
     */
    @Transactional
    public OvertimeRequestDTO updateOvertimeRequest(String otRequestId, OvertimeRequestDTO overtimeRequestDTO) {
        OvertimeRequestEntity overtimeRequest = overtimeRepository.findById(otRequestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Overtime request not found"));
        
        // Update fields
        if (overtimeRequestDTO.getDate() != null) {
            overtimeRequest.setDate(overtimeRequestDTO.getDate());
        }
        
        if (overtimeRequestDTO.getStartTime() != null && overtimeRequestDTO.getEndTime() != null) {
            if (overtimeRequestDTO.getEndTime().isBefore(overtimeRequestDTO.getStartTime())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
            }
            
            overtimeRequest.setStartTime(overtimeRequestDTO.getStartTime());
            overtimeRequest.setEndTime(overtimeRequestDTO.getEndTime());
            
            // Recalculate total hours
            Duration duration = Duration.between(overtimeRequestDTO.getStartTime(), overtimeRequestDTO.getEndTime());
            double hours = duration.toMinutes() / 60.0;
            BigDecimal totalHours = BigDecimal.valueOf(hours).setScale(2, BigDecimal.ROUND_HALF_UP);
            overtimeRequest.setTotalHours(totalHours);
        } else if (overtimeRequestDTO.getStartTime() != null) {
            if (overtimeRequest.getEndTime() != null && 
                    overtimeRequestDTO.getStartTime().isAfter(overtimeRequest.getEndTime())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time cannot be after end time");
            }
            
            overtimeRequest.setStartTime(overtimeRequestDTO.getStartTime());
            
            // Recalculate total hours if both start and end times are available
            if (overtimeRequest.getEndTime() != null) {
                Duration duration = Duration.between(overtimeRequestDTO.getStartTime(), overtimeRequest.getEndTime());
                double hours = duration.toMinutes() / 60.0;
                BigDecimal totalHours = BigDecimal.valueOf(hours).setScale(2, BigDecimal.ROUND_HALF_UP);
                overtimeRequest.setTotalHours(totalHours);
            }
        } else if (overtimeRequestDTO.getEndTime() != null) {
            if (overtimeRequest.getStartTime() != null && 
                    overtimeRequestDTO.getEndTime().isBefore(overtimeRequest.getStartTime())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time cannot be before start time");
            }
            
            overtimeRequest.setEndTime(overtimeRequestDTO.getEndTime());
            
            // Recalculate total hours if both start and end times are available
            if (overtimeRequest.getStartTime() != null) {
                Duration duration = Duration.between(overtimeRequest.getStartTime(), overtimeRequestDTO.getEndTime());
                double hours = duration.toMinutes() / 60.0;
                BigDecimal totalHours = BigDecimal.valueOf(hours).setScale(2, BigDecimal.ROUND_HALF_UP);
                overtimeRequest.setTotalHours(totalHours);
            }
        }
        
        if (overtimeRequestDTO.getReason() != null && !overtimeRequestDTO.getReason().isEmpty()) {
            overtimeRequest.setReason(overtimeRequestDTO.getReason());
        }
        
        OvertimeRequestEntity updatedRequest = overtimeRepository.save(overtimeRequest);
        return convertToDTO(updatedRequest);
    }

    /**
     * Approve or reject an overtime request (HR only)
     */
    @Transactional
    public OvertimeRequestDTO reviewOvertimeRequest(String otRequestId, String status) {
        OvertimeRequestEntity overtimeRequest = overtimeRepository.findById(otRequestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Overtime request not found"));
        
        // Validate status
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Status must be either APPROVED or REJECTED");
        }
        
        // Verify that the overtime request is in PENDING status
        if (!"PENDING".equals(overtimeRequest.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Only pending overtime requests can be reviewed");
        }
        
        // Update overtime request
        overtimeRequest.setStatus(status);
        overtimeRequest.setReviewedBy(getCurrentUser());
        overtimeRequest.setReviewedAt(LocalDateTime.now());
        
        OvertimeRequestEntity updatedRequest = overtimeRepository.save(overtimeRequest);
        return convertToDTO(updatedRequest);
    }

    /**
     * Get all overtime requests with a specific status (HR only)
     */
    @Transactional(readOnly = true)
    public Page<OvertimeRequestDTO> getOvertimeRequestsByStatus(String status, Pageable pageable) {
        return overtimeRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get overtime requests for a specific employee (HR only)
     */
    @Transactional(readOnly = true)
    public Page<OvertimeRequestDTO> getEmployeeOvertimeRequests(String employeeId, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return overtimeRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Convert OvertimeRequestEntity to OvertimeRequestDTO
     */
    private OvertimeRequestDTO convertToDTO(OvertimeRequestEntity entity) {
        if (entity == null) {
            return null;
        }
        
        OvertimeRequestDTO dto = new OvertimeRequestDTO();
        dto.setOtRequestId(entity.getOtRequestId());
        dto.setEmployeeId(entity.getEmployee().getEmployeeId());
        dto.setEmployeeName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName());
        dto.setDate(entity.getDate());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setTotalHours(entity.getTotalHours());
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