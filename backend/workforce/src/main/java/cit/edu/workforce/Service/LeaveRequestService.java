package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.LeaveRequestDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.LeaveRequestEntity;
import cit.edu.workforce.Enum.LeaveRequestStatus;
import cit.edu.workforce.Entity.LeaveTypeEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.LeaveRequestRepository;
import cit.edu.workforce.Repository.LeaveTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final EmployeeService employeeService;

    @Autowired
    public LeaveRequestService(
            LeaveRequestRepository leaveRequestRepository,
            EmployeeRepository employeeRepository,
            LeaveTypeRepository leaveTypeRepository,
            LeaveBalanceService leaveBalanceService,
            EmployeeService employeeService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveBalanceService = leaveBalanceService;
        this.employeeService = employeeService;
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getEmployeeLeaveRequests(String employeeId, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

        return leaveRequestRepository.findByEmployeeAndIsActiveTrue(employee, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getCurrentEmployeeLeaveRequests(Pageable pageable) {
        return employeeService.getCurrentEmployee()
                .map(employeeDTO -> getEmployeeLeaveRequests(employeeDTO.getEmployeeId(), pageable))
                .orElseThrow(() -> new IllegalStateException("No authenticated employee found"));
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getLeaveRequestsByStatus(LeaveRequestStatus status, Pageable pageable) {
        return leaveRequestRepository.findByStatusAndIsActiveTrue(status, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getPendingLeaveRequestsByDepartment(String departmentId, Pageable pageable) {
        return leaveRequestRepository.findPendingLeaveRequestsByDepartment(departmentId, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<LeaveRequestDTO> getLeaveRequestById(String id) {
        return leaveRequestRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public LeaveRequestDTO createLeaveRequest(LeaveRequestDTO leaveRequestDTO) {
        EmployeeEntity employee;
        
        // If employee ID is provided, use it; otherwise use the current employee
        if (leaveRequestDTO.getEmployeeId() != null && !leaveRequestDTO.getEmployeeId().isEmpty()) {
            employee = employeeRepository.findById(leaveRequestDTO.getEmployeeId())
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + leaveRequestDTO.getEmployeeId()));
        } else {
            employee = employeeService.getCurrentEmployee()
                    .map(employeeDTO -> employeeRepository.findById(employeeDTO.getEmployeeId())
                            .orElseThrow(() -> new EntityNotFoundException("Employee not found")))
                    .orElseThrow(() -> new IllegalStateException("No authenticated employee found"));
        }

        LeaveTypeEntity leaveType = leaveTypeRepository.findById(leaveRequestDTO.getLeaveTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Leave type not found with id: " + leaveRequestDTO.getLeaveTypeId()));

        // Calculate the number of days
        double days = calculateLeaveDays(leaveRequestDTO.getStartDate(), leaveRequestDTO.getEndDate());

        // Check if there is sufficient leave balance
        checkLeaveBalance(employee.getEmployeeId(), leaveType.getId(), LocalDate.now().getYear(), days);

        LeaveRequestEntity entity = new LeaveRequestEntity();
        entity.setEmployee(employee);
        entity.setLeaveType(leaveType);
        entity.setStartDate(leaveRequestDTO.getStartDate());
        entity.setEndDate(leaveRequestDTO.getEndDate());
        entity.setDays(days);
        entity.setReason(leaveRequestDTO.getReason());
        entity.setComments(leaveRequestDTO.getComments());
        entity.setStatus(LeaveRequestStatus.PENDING);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setIsActive(true);

        // Update leave balance - add to pending days
        leaveBalanceService.addPendingDaysToLeaveBalance(
                employee.getEmployeeId(), leaveType.getId(), LocalDate.now().getYear(), days);

        return convertToDTO(leaveRequestRepository.save(entity));
    }

    @Transactional
    public LeaveRequestDTO approveLeaveRequest(String id, String approverId, String comments) {
        LeaveRequestEntity leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Leave request not found with id: " + id));

        // Verify status is PENDING
        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new IllegalStateException("Cannot approve a leave request that is not in PENDING status");
        }

        EmployeeEntity approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new EntityNotFoundException("Approver not found with id: " + approverId));

        leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setApprovedAt(LocalDateTime.now());
        if (comments != null && !comments.isEmpty()) {
            leaveRequest.setComments(comments);
        }

        // Update leave balance - move from pending to used
        leaveBalanceService.updateLeaveBalanceForRequest(
                leaveRequest.getEmployee().getEmployeeId(),
                leaveRequest.getLeaveType().getId(),
                LocalDate.now().getYear(),
                leaveRequest.getDays(),
                true);

        return convertToDTO(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional
    public LeaveRequestDTO rejectLeaveRequest(String id, String approverId, String comments) {
        LeaveRequestEntity leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Leave request not found with id: " + id));

        // Verify status is PENDING
        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new IllegalStateException("Cannot reject a leave request that is not in PENDING status");
        }

        EmployeeEntity approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new EntityNotFoundException("Approver not found with id: " + approverId));

        leaveRequest.setStatus(LeaveRequestStatus.REJECTED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setApprovedAt(LocalDateTime.now());
        if (comments != null && !comments.isEmpty()) {
            leaveRequest.setComments(comments);
        }

        // Update leave balance - remove from pending
        leaveBalanceService.updateLeaveBalanceForRequest(
                leaveRequest.getEmployee().getEmployeeId(),
                leaveRequest.getLeaveType().getId(),
                LocalDate.now().getYear(),
                leaveRequest.getDays(),
                false);

        return convertToDTO(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional
    public LeaveRequestDTO cancelLeaveRequest(String id) {
        LeaveRequestEntity leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Leave request not found with id: " + id));

        // Verify status is PENDING
        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel a leave request that is not in PENDING status");
        }

        // Check if the cancellation is requested by the same employee who created the request
        EmployeeEntity currentEmployee = employeeService.getCurrentEmployee()
                .map(employeeDTO -> employeeRepository.findById(employeeDTO.getEmployeeId())
                        .orElseThrow(() -> new EntityNotFoundException("Employee not found")))
                .orElseThrow(() -> new IllegalStateException("No authenticated employee found"));

        if (!leaveRequest.getEmployee().getEmployeeId().equals(currentEmployee.getEmployeeId())) {
            throw new IllegalStateException("You can only cancel your own leave requests");
        }

        leaveRequest.setStatus(LeaveRequestStatus.CANCELLED);

        // Update leave balance - remove from pending
        leaveBalanceService.updateLeaveBalanceForRequest(
                leaveRequest.getEmployee().getEmployeeId(),
                leaveRequest.getLeaveType().getId(),
                LocalDate.now().getYear(),
                leaveRequest.getDays(),
                false);

        return convertToDTO(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDTO> getApprovedLeaveRequestsForDateRange(
            String employeeId, LocalDate startDate, LocalDate endDate) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

        return leaveRequestRepository.findApprovedLeaveRequestsForDateRange(employee, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private double calculateLeaveDays(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        // Simple implementation counting all days including weekends
        return Period.between(startDate, endDate).getDays() + 1.0;

        // For a more sophisticated implementation, you could exclude weekends and holidays
    }

    private void checkLeaveBalance(String employeeId, String leaveTypeId, int year, double requestedDays) {
        leaveBalanceService.getEmployeeLeaveBalances(employeeId).stream()
                .filter(balance -> balance.getLeaveTypeId().equals(leaveTypeId) && balance.getYear() == year)
                .findFirst()
                .ifPresentOrElse(
                        balance -> {
                            double availableDays = balance.getAllowedDays() - balance.getUsedDays() - balance.getPendingDays();
                            if (availableDays < requestedDays) {
                                throw new IllegalStateException("Insufficient leave balance. Available: " + 
                                        availableDays + ", Requested: " + requestedDays);
                            }
                        },
                        () -> {
                            throw new EntityNotFoundException("No leave balance found for the employee and leave type for year " + year);
                        }
                );
    }

    private LeaveRequestDTO convertToDTO(LeaveRequestEntity entity) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getEmployee().getEmployeeId());
        dto.setEmployeeName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName());
        dto.setLeaveTypeId(entity.getLeaveType().getId());
        dto.setLeaveTypeName(entity.getLeaveType().getName());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setDays(entity.getDays());
        dto.setReason(entity.getReason());
        dto.setComments(entity.getComments());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        
        if (entity.getApprovedBy() != null) {
            dto.setApprovedById(entity.getApprovedBy().getEmployeeId());
            dto.setApprovedByName(entity.getApprovedBy().getFirstName() + " " + entity.getApprovedBy().getLastName());
            dto.setApprovedAt(entity.getApprovedAt());
        }
        
        dto.setIsActive(entity.getIsActive());
        return dto;
    }
}

// New file: Service for leave request management
// Handles employee leave requests, approval process, and leave balance updates 