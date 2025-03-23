package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.ApiResponseDTO;
import cit.edu.workforce.DTO.LeaveRequestDTO;
import cit.edu.workforce.Service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class LeaveRequestController {
    
    @Autowired
    private LeaveRequestService leaveRequestService;
    
    @GetMapping("/leave/{id}")
    @PreAuthorize("hasRole('HR_STAFF') or hasRole('ADMIN') or @securityService.isLeaveRequestOwner(authentication, #id)")
    public ResponseEntity<?> getLeaveRequestById(@PathVariable Long id) {
        try {
            LeaveRequestDTO leaveRequest = leaveRequestService.getLeaveRequestById(id);
            return ResponseEntity.ok(ApiResponseDTO.success(leaveRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
    
    @GetMapping("/leave/employee/{employeeId}")
    @PreAuthorize("hasRole('HR_STAFF') or hasRole('ADMIN') or @securityService.isEmployeeOwner(authentication, #employeeId)")
    public ResponseEntity<?> getLeaveRequestsByEmployeeId(@PathVariable Long employeeId) {
        try {
            List<LeaveRequestDTO> leaveRequests = leaveRequestService.getLeaveRequestsByEmployeeId(employeeId);
            return ResponseEntity.ok(ApiResponseDTO.success(leaveRequests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
    
    @GetMapping("/leave/status/{status}")
    @PreAuthorize("hasRole('HR_STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> getLeaveRequestsByStatus(@PathVariable String status) {
        try {
            List<LeaveRequestDTO> leaveRequests = leaveRequestService.getLeaveRequestsByStatus(status);
            return ResponseEntity.ok(ApiResponseDTO.success(leaveRequests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
    
    @PostMapping("/leave")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('HR_STAFF') or hasRole('ADMIN') or @securityService.isEmployeeOwner(authentication, #leaveRequestDTO.employeeId)")
    public ResponseEntity<?> createLeaveRequest(@RequestBody LeaveRequestDTO leaveRequestDTO) {
        try {
            LeaveRequestDTO createdLeaveRequest = leaveRequestService.createLeaveRequest(leaveRequestDTO);
            return ResponseEntity.ok(ApiResponseDTO.success("Leave request submitted successfully", createdLeaveRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
    
    @PutMapping("/leave/{id}")
    @PreAuthorize("hasRole('HR_STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> updateLeaveRequestStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            LeaveRequestDTO updatedLeaveRequest = leaveRequestService.updateLeaveRequestStatus(id, status);
            return ResponseEntity.ok(ApiResponseDTO.success("Leave request status updated to " + status, updatedLeaveRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
} 