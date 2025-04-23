package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.OvertimeRequestDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.OvertimeRequestEntity;
import cit.edu.workforce.Enum.OvertimeRequestStatus;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.OvertimeRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OvertimeRequestService {

    private final OvertimeRequestRepository overtimeRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    @Autowired
    public OvertimeRequestService(
            OvertimeRequestRepository overtimeRequestRepository,
            EmployeeRepository employeeRepository,
            EmployeeService employeeService) {
        this.overtimeRequestRepository = overtimeRequestRepository;
        this.employeeRepository = employeeRepository;
        this.employeeService = employeeService;
    }

    @Transactional(readOnly = true)
    public Page<OvertimeRequestDTO> getEmployeeOvertimeRequests(String employeeId, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

        return overtimeRequestRepository.findByEmployeeAndIsActiveTrue(employee, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<OvertimeRequestDTO> getCurrentEmployeeOvertimeRequests(Pageable pageable) {
        return employeeService.getCurrentEmployee()
                .map(employeeDTO -> getEmployeeOvertimeRequests(employeeDTO.getEmployeeId(), pageable))
                .orElseThrow(() -> new IllegalStateException("No authenticated employee found"));
    }

    @Transactional(readOnly = true)
    public Page<OvertimeRequestDTO> getOvertimeRequestsByStatus(OvertimeRequestStatus status, Pageable pageable) {
        return overtimeRequestRepository.findByStatusAndIsActiveTrue(status, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<OvertimeRequestDTO> getPendingOvertimeRequestsByDepartment(String departmentId, Pageable pageable) {
        return overtimeRequestRepository.findPendingOvertimeRequestsByDepartment(departmentId, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<OvertimeRequestDTO> getOvertimeRequestById(String id) {
        return overtimeRequestRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public OvertimeRequestDTO createOvertimeRequest(OvertimeRequestDTO overtimeRequestDTO) {
        EmployeeEntity employee;
        
        // If employee ID is provided, use it; otherwise use the current employee
        if (overtimeRequestDTO.getEmployeeId() != null && !overtimeRequestDTO.getEmployeeId().isEmpty()) {
            employee = employeeRepository.findById(overtimeRequestDTO.getEmployeeId())
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + overtimeRequestDTO.getEmployeeId()));
        } else {
            employee = employeeService.getCurrentEmployee()
                    .map(employeeDTO -> employeeRepository.findById(employeeDTO.getEmployeeId())
                            .orElseThrow(() -> new EntityNotFoundException("Employee not found")))
                    .orElseThrow(() -> new IllegalStateException("No authenticated employee found"));
        }

        // Validate overtime hours
        if (overtimeRequestDTO.getHours() <= 0) {
            throw new IllegalArgumentException("Overtime hours must be greater than zero");
        }

        OvertimeRequestEntity entity = new OvertimeRequestEntity();
        entity.setEmployee(employee);
        entity.setDate(overtimeRequestDTO.getDate());
        entity.setHours(overtimeRequestDTO.getHours());
        entity.setReason(overtimeRequestDTO.getReason());
        entity.setComments(overtimeRequestDTO.getComments());
        entity.setStatus(OvertimeRequestStatus.PENDING);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setIsActive(true);

        return convertToDTO(overtimeRequestRepository.save(entity));
    }

    @Transactional
    public OvertimeRequestDTO approveOvertimeRequest(String id, String approverId, String comments) {
        OvertimeRequestEntity overtimeRequest = overtimeRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Overtime request not found with id: " + id));

        // Verify status is PENDING
        if (overtimeRequest.getStatus() != OvertimeRequestStatus.PENDING) {
            throw new IllegalStateException("Cannot approve an overtime request that is not in PENDING status");
        }

        EmployeeEntity approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new EntityNotFoundException("Approver not found with id: " + approverId));

        overtimeRequest.setStatus(OvertimeRequestStatus.APPROVED);
        overtimeRequest.setApprovedBy(approver);
        overtimeRequest.setApprovedAt(LocalDateTime.now());
        if (comments != null && !comments.isEmpty()) {
            overtimeRequest.setComments(comments);
        }

        return convertToDTO(overtimeRequestRepository.save(overtimeRequest));
    }

    @Transactional
    public OvertimeRequestDTO rejectOvertimeRequest(String id, String approverId, String comments) {
        OvertimeRequestEntity overtimeRequest = overtimeRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Overtime request not found with id: " + id));

        // Verify status is PENDING
        if (overtimeRequest.getStatus() != OvertimeRequestStatus.PENDING) {
            throw new IllegalStateException("Cannot reject an overtime request that is not in PENDING status");
        }

        EmployeeEntity approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new EntityNotFoundException("Approver not found with id: " + approverId));

        overtimeRequest.setStatus(OvertimeRequestStatus.REJECTED);
        overtimeRequest.setApprovedBy(approver);
        overtimeRequest.setApprovedAt(LocalDateTime.now());
        if (comments != null && !comments.isEmpty()) {
            overtimeRequest.setComments(comments);
        }

        return convertToDTO(overtimeRequestRepository.save(overtimeRequest));
    }

    @Transactional
    public OvertimeRequestDTO cancelOvertimeRequest(String id) {
        OvertimeRequestEntity overtimeRequest = overtimeRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Overtime request not found with id: " + id));

        // Verify status is PENDING
        if (overtimeRequest.getStatus() != OvertimeRequestStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel an overtime request that is not in PENDING status");
        }

        // Check if the cancellation is requested by the same employee who created the request
        EmployeeEntity currentEmployee = employeeService.getCurrentEmployee()
                .map(employeeDTO -> employeeRepository.findById(employeeDTO.getEmployeeId())
                        .orElseThrow(() -> new EntityNotFoundException("Employee not found")))
                .orElseThrow(() -> new IllegalStateException("No authenticated employee found"));

        if (!overtimeRequest.getEmployee().getEmployeeId().equals(currentEmployee.getEmployeeId())) {
            throw new IllegalStateException("You can only cancel your own overtime requests");
        }

        overtimeRequest.setStatus(OvertimeRequestStatus.CANCELLED);
        return convertToDTO(overtimeRequestRepository.save(overtimeRequest));
    }

    @Transactional(readOnly = true)
    public List<OvertimeRequestDTO> getApprovedOvertimeRequestsForDateRange(
            String employeeId, LocalDate startDate, LocalDate endDate) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

        return overtimeRequestRepository.findApprovedOvertimeRequestsForDateRange(employee, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private OvertimeRequestDTO convertToDTO(OvertimeRequestEntity entity) {
        OvertimeRequestDTO dto = new OvertimeRequestDTO();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getEmployee().getEmployeeId());
        dto.setEmployeeName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName());
        dto.setDate(entity.getDate());
        dto.setHours(entity.getHours());
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

// New file: Service for overtime request management
// Handles employee overtime requests and approval process 