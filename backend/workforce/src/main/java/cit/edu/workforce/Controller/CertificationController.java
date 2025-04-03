package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.CertificationDTO;
import cit.edu.workforce.Service.CertificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CertificationController {

    private final CertificationService certificationService;

    @Autowired
    public CertificationController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @GetMapping("/employees/{employeeId}/certifications")
    @PreAuthorize("hasRole('HR_ADMIN') or @userSecurity.isEmployee(#employeeId)")
    public ResponseEntity<List<CertificationDTO>> getEmployeeCertifications(@PathVariable UUID employeeId) {
        List<CertificationDTO> certifications = certificationService.getEmployeeCertifications(employeeId);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/certifications/{certificationId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<CertificationDTO> getCertification(@PathVariable UUID certificationId) {
        CertificationDTO certification = certificationService.getCertification(certificationId);
        return ResponseEntity.ok(certification);
    }

    @PostMapping("/employees/{employeeId}/certifications")
    @PreAuthorize("hasRole('HR_ADMIN') or @userSecurity.isEmployee(#employeeId)")
    public ResponseEntity<CertificationDTO> addCertification(
            @PathVariable UUID employeeId,
            @Valid @RequestBody CertificationDTO certificationDTO) {
        
        CertificationDTO addedCertification = certificationService.addCertification(employeeId, certificationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedCertification);
    }

    @PutMapping("/certifications/{certificationId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<CertificationDTO> updateCertification(
            @PathVariable UUID certificationId,
            @Valid @RequestBody CertificationDTO certificationDTO) {
        
        CertificationDTO updatedCertification = certificationService.updateCertification(certificationId, certificationDTO);
        return ResponseEntity.ok(updatedCertification);
    }

    @DeleteMapping("/certifications/{certificationId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<Void> deleteCertification(@PathVariable UUID certificationId) {
        certificationService.deleteCertification(certificationId);
        return ResponseEntity.noContent().build();
    }
} 