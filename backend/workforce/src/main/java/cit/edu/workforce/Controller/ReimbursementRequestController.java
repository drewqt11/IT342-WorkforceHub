package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.ReimbursementRequestDTO;
import cit.edu.workforce.Service.ReimbursementRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Tag(name = "Reimbursement Request Controller", description = "APIs for managing employee reimbursement requests")
@SecurityRequirement(name = "bearerAuth")
public class ReimbursementRequestController {

    private final ReimbursementRequestService reimbursementRequestService;

    @Autowired
    public ReimbursementRequestController(ReimbursementRequestService reimbursementRequestService) {
        this.reimbursementRequestService = reimbursementRequestService;
    }

    // Employee endpoints

    @Operation(summary = "Get current employee's reimbursement requests")
    @GetMapping("/employee/reimbursements")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE') or hasRole('ROLE_HR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ReimbursementRequestDTO>> getCurrentEmployeeReimbursementRequests() {
        List<ReimbursementRequestDTO> requests = reimbursementRequestService.getCurrentEmployeeReimbursementRequests();
        return ResponseEntity.ok(requests);
    }

    @Operation(summary = "Get current employee's reimbursement requests (paginated)")
    @GetMapping("/employee/reimbursements/paged")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE') or hasRole('ROLE_HR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<ReimbursementRequestDTO>> getCurrentEmployeeReimbursementRequestsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "requestDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReimbursementRequestDTO> requests = reimbursementRequestService.getCurrentEmployeeReimbursementRequestsPaged(pageable);
        return ResponseEntity.ok(requests);
    }

    @Operation(summary = "Create new reimbursement request")
    @PostMapping("/employee/reimbursements")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE') or hasRole('ROLE_HR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ReimbursementRequestDTO> createReimbursementRequest(
            @Valid @RequestBody ReimbursementRequestDTO requestDTO) {
        ReimbursementRequestDTO createdRequest = reimbursementRequestService.createReimbursementRequest(requestDTO);
        return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
    }

    @Operation(summary = "Update reimbursement request")
    @PutMapping("/employee/reimbursements/{id}")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE') or hasRole('ROLE_HR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ReimbursementRequestDTO> updateReimbursementRequest(
            @PathVariable("id") String requestId,
            @Valid @RequestBody ReimbursementRequestDTO requestDTO) {
        ReimbursementRequestDTO updatedRequest = reimbursementRequestService.updateReimbursementRequest(requestId, requestDTO);
        return ResponseEntity.ok(updatedRequest);
    }

    @Operation(summary = "Delete reimbursement request")
    @DeleteMapping("/employee/reimbursements/{id}")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE') or hasRole('ROLE_HR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteReimbursementRequest(@PathVariable("id") String requestId) {
        reimbursementRequestService.deleteReimbursementRequest(requestId);
        return ResponseEntity.noContent().build();
    }

    // HR endpoints

    @Operation(summary = "Get all reimbursement requests (paginated)")
    @GetMapping("/hr/reimbursements")
    @PreAuthorize("hasRole('ROLE_HR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<ReimbursementRequestDTO>> getAllReimbursementRequestsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "requestDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReimbursementRequestDTO> requests = reimbursementRequestService.getAllReimbursementRequestsPaged(pageable);
        return ResponseEntity.ok(requests);
    }

    @Operation(summary = "Get reimbursement requests by employee ID (paginated)")
    @GetMapping("/hr/employees/{employeeId}/reimbursements")
    @PreAuthorize("hasRole('ROLE_HR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<ReimbursementRequestDTO>> getEmployeeReimbursementRequestsPaged(
            @PathVariable String employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "requestDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReimbursementRequestDTO> requests = reimbursementRequestService.getEmployeeReimbursementRequestsPaged(employeeId, pageable);
        return ResponseEntity.ok(requests);
    }

    @Operation(summary = "Get reimbursement requests by status (paginated)")
    @GetMapping("/hr/reimbursements/status/{status}")
    @PreAuthorize("hasRole('ROLE_HR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<ReimbursementRequestDTO>> getReimbursementRequestsByStatusPaged(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "requestDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReimbursementRequestDTO> requests = reimbursementRequestService.getReimbursementRequestsByStatusPaged(status, pageable);
        return ResponseEntity.ok(requests);
    }

    @Operation(summary = "Get reimbursement request by ID")
    @GetMapping("/hr/reimbursements/{id}")
    @PreAuthorize("hasRole('ROLE_HR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ReimbursementRequestDTO> getReimbursementRequestById(@PathVariable("id") String requestId) {
        Optional<ReimbursementRequestDTO> request = reimbursementRequestService.getReimbursementRequestById(requestId);
        return request.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Approve reimbursement request")
    @PutMapping("/hr/reimbursements/{id}/approve")
    @PreAuthorize("hasRole('ROLE_HR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ReimbursementRequestDTO> approveReimbursementRequest(
            @PathVariable("id") String requestId,
            @RequestParam(required = false) String comments) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String approverEmail = auth.getName();
        
        // TODO: Get approver ID from email - this would need a method to get employee by email
        String approverId = "HR001"; // This is a placeholder - implement actual logic
        
        ReimbursementRequestDTO approvedRequest = reimbursementRequestService.approveReimbursementRequest(
                requestId, approverId, comments);
        return ResponseEntity.ok(approvedRequest);
    }

    @Operation(summary = "Reject reimbursement request")
    @PutMapping("/hr/reimbursements/{id}/reject")
    @PreAuthorize("hasRole('ROLE_HR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ReimbursementRequestDTO> rejectReimbursementRequest(
            @PathVariable("id") String requestId,
            @RequestParam String comments) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String approverEmail = auth.getName();
        
        // TODO: Get approver ID from email - this would need a method to get employee by email
        String approverId = "HR001"; // This is a placeholder - implement actual logic
        
        ReimbursementRequestDTO rejectedRequest = reimbursementRequestService.rejectReimbursementRequest(
                requestId, approverId, comments);
        return ResponseEntity.ok(rejectedRequest);
    }

    @Operation(summary = "Mark reimbursement request as paid")
    @PutMapping("/hr/reimbursements/{id}/paid")
    @PreAuthorize("hasRole('ROLE_HR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ReimbursementRequestDTO> markReimbursementRequestAsPaid(
            @PathVariable("id") String requestId,
            @RequestParam String paymentReference) {
        ReimbursementRequestDTO paidRequest = reimbursementRequestService.markAsPaid(
                requestId, paymentReference);
        return ResponseEntity.ok(paidRequest);
    }
}

// New controller for reimbursement requests in the Benefits Administration module 