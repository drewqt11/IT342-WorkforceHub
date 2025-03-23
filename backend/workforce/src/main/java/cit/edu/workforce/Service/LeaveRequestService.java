package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.LeaveRequestDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.LeaveRequestEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveRequestService {
    
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;
    
    @Autowired
    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository, 
                             EmployeeRepository employeeRepository,
                             NotificationService notificationService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }
    
    @Transactional
    public LeaveRequestDTO createLeaveRequest(LeaveRequestDTO leaveRequestDTO) {
        EmployeeEntity employee = employeeRepository.findById(leaveRequestDTO.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        LeaveRequestEntity leaveRequest = new LeaveRequestEntity();
        leaveRequest.setEmployee(employee);
        leaveRequest.setStartDate(leaveRequestDTO.getStartDate());
        leaveRequest.setEndDate(leaveRequestDTO.getEndDate());
        leaveRequest.setLeaveType(leaveRequestDTO.getLeaveType());
        leaveRequest.setReason(leaveRequestDTO.getReason());
        
        LeaveRequestEntity savedLeaveRequest = leaveRequestRepository.save(leaveRequest);
        
        // Notify HR staff about the new leave request
        notificationService.createNotification(
                employee.getUser().getId(), 
                "New leave request submitted", 
                "INFO"
        );
        
        return convertToDTO(savedLeaveRequest);
    }
    
    public LeaveRequestDTO getLeaveRequestById(Long id) {
        LeaveRequestEntity leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        return convertToDTO(leaveRequest);
    }
    
    public List<LeaveRequestDTO> getLeaveRequestsByEmployeeId(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<LeaveRequestDTO> getLeaveRequestsByStatus(String status) {
        return leaveRequestRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public LeaveRequestDTO updateLeaveRequestStatus(Long id, String status) {
        LeaveRequestEntity leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        
        leaveRequest.setStatus(status);
        
        if ("APPROVED".equals(status) || "DENIED".equals(status)) {
            leaveRequest.setApprovedAt(LocalDateTime.now());
            
            // Notify the employee about the status change
            notificationService.createNotification(
                    leaveRequest.getEmployee().getUser().getId(), 
                    "Your leave request has been " + status.toLowerCase(), 
                    "INFO"
            );
        }
        
        LeaveRequestEntity updatedLeaveRequest = leaveRequestRepository.save(leaveRequest);
        return convertToDTO(updatedLeaveRequest);
    }
    
    private LeaveRequestDTO convertToDTO(LeaveRequestEntity entity) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getEmployee().getId());
        dto.setEmployeeName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setLeaveType(entity.getLeaveType());
        dto.setStatus(entity.getStatus());
        dto.setReason(entity.getReason());
        dto.setRequestedAt(entity.getRequestedAt());
        dto.setApprovedAt(entity.getApprovedAt());
        return dto;
    }
} 