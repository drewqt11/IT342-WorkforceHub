package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.ApplicantDTO;
import cit.edu.workforce.Service.ApplicantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * ApplicantController - Provides API endpoints for applicant management
 * New file: This controller handles all applicant-related operations including
 * creating, reading, updating, and deleting applicants.
 */
@RestController
@RequestMapping("/api/applicants")
@Tag(name = "Applicants", description = "Applicant API - Manage job applicants")
public class ApplicantController {

    private final ApplicantService applicantService;

    @Autowired
    public ApplicantController(ApplicantService applicantService) {
        this.applicantService = applicantService;
    }

    /**
     * Get all applicants (HR/Admin only)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    @Operation(summary = "Get all applicants", description = "Retrieve all applicants with pagination support (HR/Admin only)")
    public ResponseEntity<Page<ApplicantDTO>> getAllApplicants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        
        Page<ApplicantDTO> applicants = applicantService.getAllApplicants(pageable);
        return ResponseEntity.ok(applicants);
    }

    /**
     * Get applicant by ID
     */
    @GetMapping("/{applicantId}")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @applicantService.hasPermissionToViewApplicant(#applicantId)")
    @Operation(summary = "Get applicant by ID", description = "Retrieve a specific applicant by ID")
    public ResponseEntity<ApplicantDTO> getApplicantById(
            @Parameter(description = "Applicant ID") @PathVariable String applicantId) {
        
        ApplicantDTO applicant = applicantService.getApplicantById(applicantId);
        return ResponseEntity.ok(applicant);
    }

    /**
     * Get current user's applicant profile
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user's applicant profile", description = "Retrieve the current user's applicant profile if it exists")
    public ResponseEntity<ApplicantDTO> getCurrentUserApplicantProfile() {
        ApplicantDTO applicant = applicantService.getCurrentUserApplicantProfile();
        
        if (applicant == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(applicant);
    }

    /**
     * Get internal applicants (HR/Admin only)
     */
    @GetMapping("/internal")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    @Operation(summary = "Get internal applicants", description = "Retrieve all internal applicants with pagination support (HR/Admin only)")
    public ResponseEntity<Page<ApplicantDTO>> getInternalApplicants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        
        Page<ApplicantDTO> applicants = applicantService.getInternalApplicants(pageable);
        return ResponseEntity.ok(applicants);
    }

    /**
     * Get external applicants (HR/Admin only)
     */
    @GetMapping("/external")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    @Operation(summary = "Get external applicants", description = "Retrieve all external applicants with pagination support (HR/Admin only)")
    public ResponseEntity<Page<ApplicantDTO>> getExternalApplicants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        
        Page<ApplicantDTO> applicants = applicantService.getExternalApplicants(pageable);
        return ResponseEntity.ok(applicants);
    }

    /**
     * Search applicants by name (HR/Admin only)
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    @Operation(summary = "Search applicants by name", description = "Search for applicants by name with pagination support (HR/Admin only)")
    public ResponseEntity<Page<ApplicantDTO>> searchApplicantsByName(
            @Parameter(description = "Name to search for") @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        
        Page<ApplicantDTO> applicants = applicantService.searchApplicantsByName(name, pageable);
        return ResponseEntity.ok(applicants);
    }

    /**
     * Create a new internal applicant profile for the current user
     */
    @PostMapping("/internal")
    @Operation(summary = "Create internal applicant profile", description = "Create a new internal applicant profile for the current user")
    public ResponseEntity<ApplicantDTO> createInternalApplicant(
            @Parameter(description = "Phone number") @RequestParam(required = false) String phoneNumber,
            @Parameter(description = "Resume file (PDF only)") @RequestParam(required = false) MultipartFile resumeFile) {
        
        try {
            // User ID is determined from the authenticated user
            ApplicantDTO createdApplicant = applicantService.createInternalApplicant(null, phoneNumber, resumeFile);
            return new ResponseEntity<>(createdApplicant, HttpStatus.CREATED);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Create a new external applicant
     */
    @PostMapping("/external")
    @Operation(summary = "Create external applicant", description = "Create a new external applicant")
    public ResponseEntity<ApplicantDTO> createExternalApplicant(
            @Parameter(description = "Full name") @RequestParam String fullName,
            @Parameter(description = "Email address") @RequestParam String email,
            @Parameter(description = "Phone number") @RequestParam(required = false) String phoneNumber,
            @Parameter(description = "Resume file (PDF only)") @RequestParam MultipartFile resumeFile) {
        
        try {
            ApplicantDTO createdApplicant = applicantService.createExternalApplicant(
                    fullName, email, phoneNumber, resumeFile);
            return new ResponseEntity<>(createdApplicant, HttpStatus.CREATED);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Update an applicant's information
     */
    @PutMapping("/{applicantId}")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @applicantService.hasPermissionToModifyApplicant(#applicantId)")
    @Operation(summary = "Update applicant", description = "Update an existing applicant's information")
    public ResponseEntity<ApplicantDTO> updateApplicant(
            @Parameter(description = "Applicant ID") @PathVariable String applicantId,
            @Parameter(description = "Full name") @RequestParam(required = false) String fullName,
            @Parameter(description = "Email address") @RequestParam(required = false) String email,
            @Parameter(description = "Phone number") @RequestParam(required = false) String phoneNumber,
            @Parameter(description = "Resume file (PDF only)") @RequestParam(required = false) MultipartFile resumeFile) {
        
        try {
            ApplicantDTO updatedApplicant = applicantService.updateApplicant(
                    applicantId, fullName, email, phoneNumber, resumeFile);
            return ResponseEntity.ok(updatedApplicant);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Delete an applicant (HR/Admin only or own profile)
     */
    @DeleteMapping("/{applicantId}")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @applicantService.hasPermissionToModifyApplicant(#applicantId)")
    @Operation(summary = "Delete applicant", description = "Delete an existing applicant")
    public ResponseEntity<Void> deleteApplicant(
            @Parameter(description = "Applicant ID") @PathVariable String applicantId) {
        
        applicantService.deleteApplicant(applicantId);
        return ResponseEntity.noContent().build();
    }
} 