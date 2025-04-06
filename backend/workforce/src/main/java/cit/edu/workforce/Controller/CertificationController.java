package cit.edu.workforce.Controller;

import cit.edu.workforce.Entity.CertificationEntity;
import cit.edu.workforce.Service.CertificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Certification Management", description = "Certification management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CertificationController {

    private final CertificationService certificationService;

    @Autowired
    public CertificationController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @GetMapping("/employees/{employeeId}/certifications")
    @Operation(summary = "Get employee certifications", description = "Get all certifications for an employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @employeeService.isCurrentEmployee(#employeeId)")
    public ResponseEntity<List<CertificationEntity>> getEmployeeCertifications(@PathVariable UUID employeeId) {
        List<CertificationEntity> certifications = certificationService.getCertificationsByEmployeeId(employeeId);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/certifications/{certificationId}")
    @Operation(summary = "Get certification by ID", description = "Get a certification by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @certificationService.hasAccessToCertification(#certificationId)")
    public ResponseEntity<CertificationEntity> getCertificationById(@PathVariable UUID certificationId) {
        CertificationEntity certification = certificationService.getCertificationById(certificationId);
        return ResponseEntity.ok(certification);
    }

    @PostMapping("/employees/{employeeId}/certifications")
    @Operation(summary = "Create certification", description = "Create a new certification for an employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @employeeService.isCurrentEmployee(#employeeId)")
    public ResponseEntity<CertificationEntity> createCertification(
            @PathVariable UUID employeeId,
            @RequestParam String certificateName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            @RequestParam(required = false) UUID documentId) {
        
        CertificationEntity certification = certificationService.createCertification(
                employeeId, certificateName, issueDate, expiryDate, documentId);
        
        return new ResponseEntity<>(certification, HttpStatus.CREATED);
    }

    @PutMapping("/certifications/{certificationId}")
    @Operation(summary = "Update certification", description = "Update an existing certification")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @certificationService.hasAccessToCertification(#certificationId)")
    public ResponseEntity<CertificationEntity> updateCertification(
            @PathVariable UUID certificationId,
            @RequestParam(required = false) String certificateName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate) {
        
        CertificationEntity certification = certificationService.updateCertification(
                certificationId, certificateName, issueDate, expiryDate);
        
        return ResponseEntity.ok(certification);
    }

    @PatchMapping("/hr/certifications/{certificationId}/approve")
    @Operation(summary = "Approve certification", description = "Approve a certification (HR or Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<CertificationEntity> approveCertification(@PathVariable UUID certificationId) {
        CertificationEntity certification = certificationService.approveCertification(certificationId);
        return ResponseEntity.ok(certification);
    }

    @PatchMapping("/hr/certifications/{certificationId}/reject")
    @Operation(summary = "Reject certification", description = "Reject a certification (HR or Admin only)")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<CertificationEntity> rejectCertification(@PathVariable UUID certificationId) {
        CertificationEntity certification = certificationService.rejectCertification(certificationId);
        return ResponseEntity.ok(certification);
    }
} 