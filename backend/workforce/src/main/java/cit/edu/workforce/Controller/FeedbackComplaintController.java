package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.FeedbackComplaintDTO;
import cit.edu.workforce.Service.FeedbackComplaintService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * FeedbackComplaintController - Provides API endpoints for feedback and complaint management
 * New file: Implements endpoints for creating, reading, and updating feedback and complaints
 * 
 * This controller handles all feedback/complaint-related operations including:
 * - Submitting feedback, complaints, and concerns
 * - Viewing submitted items
 * - Updating status of items
 * - Resolving items
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Feedback and Complaints", description = "Feedback and complaint management APIs")
@SecurityRequirement(name = "bearerAuth")
public class FeedbackComplaintController {

    private final FeedbackComplaintService feedbackComplaintService;

    @Autowired
    public FeedbackComplaintController(FeedbackComplaintService feedbackComplaintService) {
        this.feedbackComplaintService = feedbackComplaintService;
    }

    /**
     * Submit a new feedback/complaint
     * Allows employees to submit feedback, complaints, and concerns
     */
    @PostMapping("/employee/feedback")
    @Operation(summary = "Submit feedback or complaint", description = "Submit a new feedback, complaint, or concern")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<FeedbackComplaintDTO> createFeedbackComplaint(
            @Valid @RequestBody FeedbackComplaintDTO feedbackComplaintDTO) {
        return new ResponseEntity<>(
                feedbackComplaintService.createFeedbackComplaint(feedbackComplaintDTO),
                HttpStatus.CREATED);
    }

    /**
     * Get a specific feedback/complaint by ID
     * Employees can only access their own submissions, HR/Admin can access any
     */
    @GetMapping("/feedback/{id}")
    @Operation(summary = "Get feedback by ID", description = "Get details of a specific feedback or complaint")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<FeedbackComplaintDTO> getFeedbackComplaintById(@PathVariable String id) {
        Optional<FeedbackComplaintDTO> feedbackComplaint = feedbackComplaintService.getFeedbackComplaintById(id);
        return feedbackComplaint.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update the status of a feedback/complaint (HR/Admin only)
     */
    @PatchMapping("/hr/feedback/{id}/status")
    @Operation(summary = "Update feedback status", description = "Update the status of a feedback or complaint and optionally add resolution notes")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<FeedbackComplaintDTO> updateFeedbackComplaintStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> statusUpdate) {
        
        String status = statusUpdate.get("status");
        String resolutionNotes = statusUpdate.get("resolutionNotes");
        
        if (status == null || status.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(
                feedbackComplaintService.updateFeedbackComplaintStatus(id, status, resolutionNotes));
    }

    /**
     * Get all feedback/complaints submitted by the current employee
     */
    @GetMapping("/employee/feedback")
    @Operation(summary = "Get my feedback", description = "Get all feedback and complaints submitted by the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<FeedbackComplaintDTO>> getCurrentEmployeeFeedbackComplaints() {
        return ResponseEntity.ok(feedbackComplaintService.getCurrentEmployeeFeedbackComplaints());
    }

    /**
     * Get paginated feedback/complaints submitted by the current employee
     */
    @GetMapping("/employee/feedback/paged")
    @Operation(summary = "Get my feedback (paged)", description = "Get paginated feedback and complaints submitted by the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<FeedbackComplaintDTO>> getCurrentEmployeeFeedbackComplaintsPaged(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "submittedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(feedbackComplaintService.getCurrentEmployeeFeedbackComplaints(pageable));
    }

    /**
     * Get all feedback/complaints submitted by a specific employee (HR/Admin only)
     */
    @GetMapping("/hr/employees/{employeeId}/feedback")
    @Operation(summary = "Get employee feedback", description = "Get paginated feedback and complaints submitted by a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<FeedbackComplaintDTO>> getEmployeeFeedbackComplaints(
            @PathVariable String employeeId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "submittedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(feedbackComplaintService.getEmployeeFeedbackComplaints(employeeId, pageable));
    }

    /**
     * Get all feedback/complaints with a specific status (HR/Admin only)
     */
    @GetMapping("/hr/feedback")
    @Operation(summary = "Get feedback by status", description = "Get paginated feedback and complaints with a specific status")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<FeedbackComplaintDTO>> getFeedbackComplaintsByStatus(
            @Parameter(description = "Status filter (Open, In Review, Resolved, Closed)") @RequestParam String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "submittedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(feedbackComplaintService.getFeedbackComplaintsByStatus(status, pageable));
    }

    /**
     * Get all feedback/complaints with a specific category (HR/Admin only)
     */
    @GetMapping("/hr/feedback/category/{category}")
    @Operation(summary = "Get feedback by category", description = "Get paginated feedback and complaints with a specific category")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<FeedbackComplaintDTO>> getFeedbackComplaintsByCategory(
            @PathVariable String category,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "submittedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(feedbackComplaintService.getFeedbackComplaintsByCategory(category, pageable));
    }
} 