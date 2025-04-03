package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.CertificationDTO;
import cit.edu.workforce.Entity.Certification;
import cit.edu.workforce.Entity.Employee;
import cit.edu.workforce.Repository.CertificationRepository;
import cit.edu.workforce.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CertificationService {

    private final CertificationRepository certificationRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public CertificationService(CertificationRepository certificationRepository,
                               EmployeeRepository employeeRepository) {
        this.certificationRepository = certificationRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<CertificationDTO> getEmployeeCertifications(UUID employeeId) {
        // Verify employee exists
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return certificationRepository.findByEmployeeEmployeeId(employeeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CertificationDTO getCertification(UUID certificationId) {
        Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certification not found"));
        
        return convertToDTO(certification);
    }

    public CertificationDTO addCertification(UUID employeeId, CertificationDTO certificationDTO) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        Certification certification = new Certification();
        certification.setCertificateName(certificationDTO.getCertificateName());
        certification.setIssueDate(certificationDTO.getIssueDate());
        certification.setExpiryDate(certificationDTO.getExpiryDate());
        certification.setStatus(certificationDTO.getStatus() != null ? certificationDTO.getStatus() : "ACTIVE");
        certification.setEmployee(employee);
        
        Certification savedCertification = certificationRepository.save(certification);
        return convertToDTO(savedCertification);
    }

    public CertificationDTO updateCertification(UUID certificationId, CertificationDTO certificationDTO) {
        Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certification not found"));
        
        certification.setCertificateName(certificationDTO.getCertificateName());
        certification.setIssueDate(certificationDTO.getIssueDate());
        certification.setExpiryDate(certificationDTO.getExpiryDate());
        certification.setStatus(certificationDTO.getStatus());
        
        Certification updatedCertification = certificationRepository.save(certification);
        return convertToDTO(updatedCertification);
    }

    public void deleteCertification(UUID certificationId) {
        Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certification not found"));
        
        certificationRepository.delete(certification);
    }

    private CertificationDTO convertToDTO(Certification certification) {
        CertificationDTO dto = new CertificationDTO();
        dto.setCertificateId(certification.getCertificateId());
        dto.setCertificateName(certification.getCertificateName());
        dto.setIssueDate(certification.getIssueDate());
        dto.setExpiryDate(certification.getExpiryDate());
        dto.setStatus(certification.getStatus());
        if (certification.getEmployee() != null) {
            dto.setEmployeeId(certification.getEmployee().getEmployeeId());
        }
        return dto;
    }
} 