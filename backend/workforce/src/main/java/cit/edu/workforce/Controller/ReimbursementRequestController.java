package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.ReimbursementRequestDTO;
import cit.edu.workforce.Service.ReimbursementRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * ReimbursementRequestController - Provides API endpoints for reimbursement request management
 * New file: Implements endpoints for creating, reading, updating, and managing reimbursement requests.
 * 
 * This controller handles all reimbursement request-related operations including:
 * - Creating new reimbursement requests
 * - Viewing and searching requests
 * - Approving or rejecting requests (HR/Admin only)
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Reimbursement Requests", description = "Reimbursement request management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ReimbursementRequestController {

    private final ReimbursementRequestService reimbursementRequestService;

    @Autowired
    public ReimbursementRequestController(ReimbursementRequestService reimbursementRequestService) {
        this.reimbursementRequestService = reimbursementRequestService;
    }

    /**
     * Get all reimbursement requests for the current employee
     * Endpoint accessible to all authenticated users
     */
    @GetMapping("/employee/reimbursement-requests")
    @Operation(summary = "Get my reimbursement requests", description = "Get all reimbursement requests for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<ReimbursementRequestDTO>> getCurrentEmployeeReimbursementRequests() {
        return ResponseEntity.ok(reimbursementRequestService.getCurrentEmployeeReimbursementRequests());
    }

    /**
     * Get paginated reimbursement requests for the current employee
     * Endpoint accessible to all authenticated users
     */
    @GetMapping("/employee/reimbursement-requests/paginated")
    @Operation(summary = "Get my paginated reimbursement requests", description = "Get paginated reimbursement requests for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<ReimbursementRequestDTO>> getCurrentEmployeeReimbursementRequestsPaginated(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "requestDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(reimbursementRequestService.getCurrentEmployeeReimbursementRequests(pageable));
    }

    /**
     * Get a reimbursement request by ID
     * Employees can only access their own requests, HR/Admin can access any
     */
    @GetMapping("/reimbursement-requests/{reimbursementId}")
    @Operation(summary = "Get reimbursement request by ID", description = "Get a reimbursement request by its ID")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<ReimbursementRequestDTO> getReimbursementRequestById(
            @Parameter(description = "Reimbursement request ID") @PathVariable String reimbursementId) {
        return ResponseEntity.ok(reimbursementRequestService.getReimbursementRequestById(reimbursementId));
    }

    /**
     * Create a new reimbursement request for the current employee
     * Endpoint accessible to all authenticated users
     */
    @PostMapping(value = "/employee/reimbursement-requests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create reimbursement request", description = "Create a new reimbursement request for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<ReimbursementRequestDTO> createReimbursementRequest(
            @RequestParam("expenseDate") String expenseDate,
            @RequestParam("amountRequested") String amountRequested,
            @RequestParam("reason") String reason,
            @RequestParam(value = "receiptImage1", required = true) MultipartFile receiptImage1,
            @RequestParam(value = "receiptImage2", required = false) MultipartFile receiptImage2) {
        
        try {
            ReimbursementRequestDTO dto = new ReimbursementRequestDTO();
            dto.setExpenseDate(LocalDate.parse(expenseDate));
            dto.setAmountRequested(new BigDecimal(amountRequested));
            dto.setReason(reason);
            dto.setReceiptImage1(receiptImage1.getBytes());
            
            if (receiptImage2 != null && !receiptImage2.isEmpty()) {
                dto.setReceiptImage2(receiptImage2.getBytes());
            }

            return new ResponseEntity<>(
                    reimbursementRequestService.createReimbursementRequest(dto),
                    HttpStatus.CREATED);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Failed to process receipt images: " + e.getMessage());
        }
    }

    /**
     * Update an existing reimbursement request
     * Employees can only update their own requests, HR/Admin can update any
     */
    @PutMapping("/reimbursement-requests/{reimbursementId}")
    @Operation(summary = "Update reimbursement request", description = "Update an existing reimbursement request")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<ReimbursementRequestDTO> updateReimbursementRequest(
            @Parameter(description = "Reimbursement request ID") @PathVariable String reimbursementId,
            @Valid @RequestBody ReimbursementRequestDTO reimbursementRequestDTO) {
        return ResponseEntity.ok(
                reimbursementRequestService.updateReimbursementRequest(reimbursementId, reimbursementRequestDTO));
    }

    /**
     * Delete a reimbursement request
     * Employees can only delete their own pending requests, HR/Admin can delete any pending requests
     */
    @DeleteMapping("/reimbursement-requests/{reimbursementId}")
    @Operation(summary = "Delete reimbursement request", description = "Delete a pending reimbursement request")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteReimbursementRequest(
            @Parameter(description = "Reimbursement request ID") @PathVariable String reimbursementId) {
        reimbursementRequestService.deleteReimbursementRequest(reimbursementId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all reimbursement requests (HR/Admin only)
     */
    @GetMapping("/hr/reimbursement-requests")
    @Operation(summary = "Get all reimbursement requests", description = "Get all reimbursement requests (HR/Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<ReimbursementRequestDTO>> getAllReimbursementRequests(
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "requestDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(reimbursementRequestService.getAllReimbursementRequests(status, pageable));
    }

    /**
     * Get all reimbursement requests for a specific employee (HR/Admin only)
     */
    @GetMapping("/hr/employees/{employeeId}/reimbursement-requests")
    @Operation(summary = "Get employee's reimbursement requests", description = "Get all reimbursement requests for a specific employee (HR/Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<ReimbursementRequestDTO>> getEmployeeReimbursementRequests(
            @Parameter(description = "Employee ID") @PathVariable String employeeId,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "requestDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(reimbursementRequestService.getEmployeeReimbursementRequests(employeeId, status, pageable));
    }

    /**
     * Create a reimbursement request for any employee (HR/Admin only)
     */
    @PostMapping("/hr/employees/{employeeId}/reimbursement-requests")
    @Operation(summary = "Create employee reimbursement request", description = "Create a reimbursement request for any employee (HR/Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<ReimbursementRequestDTO> createEmployeeReimbursementRequest(
            @Parameter(description = "Employee ID") @PathVariable String employeeId,
            @Valid @RequestBody ReimbursementRequestDTO reimbursementRequestDTO) {
        return new ResponseEntity<>(
                reimbursementRequestService.createEmployeeReimbursementRequest(employeeId, reimbursementRequestDTO),
                HttpStatus.CREATED);
    }

    /**
     * Approve a reimbursement request (HR/Admin only)
     */
    @PatchMapping("/hr/reimbursement-requests/{reimbursementId}/approve")
    @Operation(summary = "Approve reimbursement request", description = "Approve a reimbursement request (HR/Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<ReimbursementRequestDTO> approveReimbursementRequest(
            @Parameter(description = "Reimbursement request ID") @PathVariable String reimbursementId,
            @Parameter(description = "Approval remarks") @RequestBody(required = false) Map<String, String> requestBody) {
        
        String remarks = (requestBody != null) ? requestBody.get("remarks") : null;
        return ResponseEntity.ok(reimbursementRequestService.approveReimbursementRequest(reimbursementId, remarks));
    }

    /**
     * Reject a reimbursement request (HR/Admin only)
     */
    @PatchMapping("/hr/reimbursement-requests/{reimbursementId}/reject")
    @Operation(summary = "Reject reimbursement request", description = "Reject a reimbursement request (HR/Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<ReimbursementRequestDTO> rejectReimbursementRequest(
            @Parameter(description = "Reimbursement request ID") @PathVariable String reimbursementId,
            @Parameter(description = "Rejection remarks") @RequestBody(required = false) Map<String, String> requestBody) {
        
        String remarks = (requestBody != null) ? requestBody.get("remarks") : null;
        return ResponseEntity.ok(reimbursementRequestService.rejectReimbursementRequest(reimbursementId, remarks));
    }
} 