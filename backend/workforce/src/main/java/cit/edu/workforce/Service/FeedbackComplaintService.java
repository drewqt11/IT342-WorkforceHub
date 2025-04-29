package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.FeedbackComplaintDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.FeedbackComplaintEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.FeedbackComplaintRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * FeedbackComplaintService - Service for managing feedback and complaints
 * New file: Provides functionality for creating and managing feedback and complaints
 */
@Service
public class FeedbackComplaintService {

    private final FeedbackComplaintRepository feedbackComplaintRepository;
    private final EmployeeRepository employeeRepository;
    private final UserAccountRepository userAccountRepository;
    
    private static final List<String> VALID_CATEGORIES = Arrays.asList("Feedback", "Complaint", "Concern");
    private static final List<String> VALID_STATUSES = Arrays.asList("Open", "In Review", "Resolved", "Closed");

    @Autowired
    public FeedbackComplaintService(
            FeedbackComplaintRepository feedbackComplaintRepository,
            EmployeeRepository employeeRepository,
            UserAccountRepository userAccountRepository) {
        this.feedbackComplaintRepository = feedbackComplaintRepository;
        this.employeeRepository = employeeRepository;
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Create a new feedback/complaint
     */
    @Transactional
    public FeedbackComplaintDTO createFeedbackComplaint(FeedbackComplaintDTO feedbackComplaintDTO) {
        // Validate input
        if (feedbackComplaintDTO.getCategory() == null || feedbackComplaintDTO.getCategory().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is required");
        }
        
        if (!VALID_CATEGORIES.contains(feedbackComplaintDTO.getCategory())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Invalid category. Must be one of: " + String.join(", ", VALID_CATEGORIES));
        }
        
        if (feedbackComplaintDTO.getSubject() == null || feedbackComplaintDTO.getSubject().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject is required");
        }
        
        if (feedbackComplaintDTO.getDescription() == null || feedbackComplaintDTO.getDescription().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required");
        }
        
        // Get the current employee
        EmployeeEntity employee = getCurrentEmployee();
        
        // Create the feedback/complaint
        FeedbackComplaintEntity feedbackComplaint = new FeedbackComplaintEntity();
        feedbackComplaint.setEmployee(employee);
        feedbackComplaint.setCategory(feedbackComplaintDTO.getCategory());
        feedbackComplaint.setSubject(feedbackComplaintDTO.getSubject());
        feedbackComplaint.setDescription(feedbackComplaintDTO.getDescription());
        feedbackComplaint.setSubmittedAt(LocalDateTime.now());
        feedbackComplaint.setStatus("Open"); // Default status
        
        FeedbackComplaintEntity savedFeedbackComplaint = feedbackComplaintRepository.save(feedbackComplaint);
        return convertToDTO(savedFeedbackComplaint);
    }

    /**
     * Get a specific feedback/complaint by ID
     */
    @Transactional(readOnly = true)
    public Optional<FeedbackComplaintDTO> getFeedbackComplaintById(String feedbackId) {
        return feedbackComplaintRepository.findById(feedbackId)
                .map(this::convertToDTO);
    }

    /**
     * Update the status of a feedback/complaint (HR only)
     */
    @Transactional
    public FeedbackComplaintDTO updateFeedbackComplaintStatus(String feedbackId, String status, String resolutionNotes) {
        if (!VALID_STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Invalid status. Must be one of: " + String.join(", ", VALID_STATUSES));
        }
        
        FeedbackComplaintEntity feedbackComplaint = feedbackComplaintRepository.findById(feedbackId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback/complaint not found"));
        
        // Get the current user as resolver if status is being updated to "Resolved" or "Closed"
        if (("Resolved".equals(status) || "Closed".equals(status)) && 
                (!"Resolved".equals(feedbackComplaint.getStatus()) && !"Closed".equals(feedbackComplaint.getStatus()))) {
            
            UserAccountEntity resolver = getCurrentUser();
            feedbackComplaint.setResolver(resolver);
            feedbackComplaint.setResolvedAt(LocalDateTime.now());
        }
        
        feedbackComplaint.setStatus(status);
        
        if (resolutionNotes != null && !resolutionNotes.isEmpty()) {
            feedbackComplaint.setResolutionNotes(resolutionNotes);
        }
        
        FeedbackComplaintEntity updatedFeedbackComplaint = feedbackComplaintRepository.save(feedbackComplaint);
        return convertToDTO(updatedFeedbackComplaint);
    }

    /**
     * Get all feedback/complaints submitted by the current employee
     */
    @Transactional(readOnly = true)
    public List<FeedbackComplaintDTO> getCurrentEmployeeFeedbackComplaints() {
        EmployeeEntity employee = getCurrentEmployee();
        
        return feedbackComplaintRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated feedback/complaints submitted by the current employee
     */
    @Transactional(readOnly = true)
    public Page<FeedbackComplaintDTO> getCurrentEmployeeFeedbackComplaints(Pageable pageable) {
        EmployeeEntity employee = getCurrentEmployee();
        
        return feedbackComplaintRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get all feedback/complaints submitted by a specific employee (HR/Admin only)
     */
    @Transactional(readOnly = true)
    public Page<FeedbackComplaintDTO> getEmployeeFeedbackComplaints(String employeeId, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return feedbackComplaintRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get all feedback/complaints with a specific status (HR/Admin only)
     */
    @Transactional(readOnly = true)
    public Page<FeedbackComplaintDTO> getFeedbackComplaintsByStatus(String status, Pageable pageable) {
        if (!VALID_STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Invalid status. Must be one of: " + String.join(", ", VALID_STATUSES));
        }
        
        return feedbackComplaintRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get all feedback/complaints with a specific category (HR/Admin only)
     */
    @Transactional(readOnly = true)
    public Page<FeedbackComplaintDTO> getFeedbackComplaintsByCategory(String category, Pageable pageable) {
        if (!VALID_CATEGORIES.contains(category)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Invalid category. Must be one of: " + String.join(", ", VALID_CATEGORIES));
        }
        
        return feedbackComplaintRepository.findByCategory(category, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Check if the current user is authorized to access this feedback/complaint
     */
    public boolean isAuthorizedForFeedbackComplaint(FeedbackComplaintEntity feedbackComplaint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        // HR and Admin roles have access to all feedback/complaints
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR") || a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // Employees can only access their own feedback/complaints
        try {
            EmployeeEntity currentEmployee = getCurrentEmployee();
            return feedbackComplaint.getEmployee().getEmployeeId().equals(currentEmployee.getEmployeeId());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Convert a FeedbackComplaintEntity to a FeedbackComplaintDTO
     */
    private FeedbackComplaintDTO convertToDTO(FeedbackComplaintEntity entity) {
        FeedbackComplaintDTO dto = new FeedbackComplaintDTO();
        dto.setFeedbackId(entity.getFeedbackId());
        dto.setEmployeeId(entity.getEmployee().getEmployeeId());
        dto.setEmployeeName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName());
        
        if (entity.getResolver() != null) {
            dto.setResolverId(entity.getResolver().getUserId());
            
            // Try to get resolver name from related employee if possible
            Optional<EmployeeEntity> resolverEmployee = employeeRepository.findByEmail(entity.getResolver().getEmailAddress());
            if (resolverEmployee.isPresent()) {
                dto.setResolverName(resolverEmployee.get().getFirstName() + " " + resolverEmployee.get().getLastName());
            } else {
                dto.setResolverName("HR Staff");
            }
        }
        
        dto.setCategory(entity.getCategory());
        dto.setSubject(entity.getSubject());
        dto.setDescription(entity.getDescription());
        dto.setSubmittedAt(entity.getSubmittedAt());
        dto.setResolvedAt(entity.getResolvedAt());
        dto.setResolutionNotes(entity.getResolutionNotes());
        dto.setStatus(entity.getStatus());
        
        return dto;
    }

    /**
     * Get the current employee from the security context
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
     */
    private UserAccountEntity getCurrentUser() {
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
} 