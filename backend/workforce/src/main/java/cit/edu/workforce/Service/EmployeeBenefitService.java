package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.EmployeeBenefitDTO;
import cit.edu.workforce.Entity.BenefitPlanEntity;
import cit.edu.workforce.Entity.EmployeeBenefitEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Repository.BenefitPlanRepository;
import cit.edu.workforce.Repository.EmployeeBenefitRepository;
import cit.edu.workforce.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeBenefitService {

    private final EmployeeBenefitRepository employeeBenefitRepository;
    private final EmployeeRepository employeeRepository;
    private final BenefitPlanRepository benefitPlanRepository;
    private final UserAccountService userAccountService;

    @Autowired
    public EmployeeBenefitService(
            EmployeeBenefitRepository employeeBenefitRepository,
            EmployeeRepository employeeRepository,
            BenefitPlanRepository benefitPlanRepository,
            UserAccountService userAccountService) {
        this.employeeBenefitRepository = employeeBenefitRepository;
        this.employeeRepository = employeeRepository;
        this.benefitPlanRepository = benefitPlanRepository;
        this.userAccountService = userAccountService;
    }

    @Transactional(readOnly = true)
    public List<EmployeeBenefitDTO> getCurrentEmployeeBenefits() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        EmployeeEntity employee = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return employeeBenefitRepository.findByEmployeeAndStatus(employee, "ACTIVE").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeBenefitDTO> getAllEmployeeBenefits() {
        return employeeBenefitRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<EmployeeBenefitDTO> getAllEmployeeBenefitsPaged(Pageable pageable) {
        return employeeBenefitRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<EmployeeBenefitDTO> getEmployeeBenefits(String employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Employee not found with ID: " + employeeId));
        
        return employeeBenefitRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<EmployeeBenefitDTO> getEmployeeBenefitsPaged(String employeeId, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Employee not found with ID: " + employeeId));
        
        return employeeBenefitRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<EmployeeBenefitDTO> getEmployeeActiveBenefits(String employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Employee not found with ID: " + employeeId));
        
        return employeeBenefitRepository.findByEmployeeAndStatus(employee, "ACTIVE").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeBenefitDTO> getBenefitPlanEnrollments(String benefitPlanId) {
        BenefitPlanEntity benefitPlan = benefitPlanRepository.findById(benefitPlanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Benefit plan not found with ID: " + benefitPlanId));
        
        return employeeBenefitRepository.findByBenefitPlan(benefitPlan).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<EmployeeBenefitDTO> getBenefitPlanEnrollmentsPaged(String benefitPlanId, Pageable pageable) {
        BenefitPlanEntity benefitPlan = benefitPlanRepository.findById(benefitPlanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Benefit plan not found with ID: " + benefitPlanId));
        
        return employeeBenefitRepository.findByBenefitPlan(benefitPlan, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<EmployeeBenefitDTO> getEmployeeBenefitById(String enrollmentId) {
        return employeeBenefitRepository.findById(enrollmentId)
                .map(this::convertToDTO);
    }

    @Transactional
    public EmployeeBenefitDTO enrollEmployeeInBenefit(EmployeeBenefitDTO employeeBenefitDTO) {
        EmployeeEntity employee = employeeRepository.findById(employeeBenefitDTO.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Employee not found with ID: " + employeeBenefitDTO.getEmployeeId()));
        
        BenefitPlanEntity benefitPlan = benefitPlanRepository.findById(employeeBenefitDTO.getBenefitPlanId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Benefit plan not found with ID: " + employeeBenefitDTO.getBenefitPlanId()));
        
        // Check if employee is already enrolled in this benefit plan
        if (employeeBenefitRepository.existsByEmployeeAndBenefitPlan(employee, benefitPlan)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Employee is already enrolled in this benefit plan");
        }
        
        // Check if benefit plan is active
        if (!benefitPlan.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Cannot enroll in an inactive benefit plan");
        }
        
        EmployeeBenefitEntity employeeBenefit = new EmployeeBenefitEntity();
        employeeBenefit.setEmployee(employee);
        employeeBenefit.setBenefitPlan(benefitPlan);
        employeeBenefit.setEnrollmentDate(LocalDate.now());
        employeeBenefit.setCoverageStartDate(employeeBenefitDTO.getCoverageStartDate() != null ? 
                employeeBenefitDTO.getCoverageStartDate() : LocalDate.now());
        employeeBenefit.setCoverageEndDate(employeeBenefitDTO.getCoverageEndDate());
        employeeBenefit.setStatus(employeeBenefitDTO.getStatus() != null ? 
                employeeBenefitDTO.getStatus() : "ACTIVE");
        employeeBenefit.setDependentsCount(employeeBenefitDTO.getDependentsCount() != null ? 
                employeeBenefitDTO.getDependentsCount() : 0);
        employeeBenefit.setAdditionalDetails(employeeBenefitDTO.getAdditionalDetails());
        
        EmployeeBenefitEntity savedEmployeeBenefit = employeeBenefitRepository.save(employeeBenefit);
        return convertToDTO(savedEmployeeBenefit);
    }

    @Transactional
    public EmployeeBenefitDTO updateEmployeeBenefit(String enrollmentId, EmployeeBenefitDTO employeeBenefitDTO) {
        EmployeeBenefitEntity existingEmployeeBenefit = employeeBenefitRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Employee benefit enrollment not found with ID: " + enrollmentId));
        
        // Update fields
        if (employeeBenefitDTO.getCoverageStartDate() != null) {
            existingEmployeeBenefit.setCoverageStartDate(employeeBenefitDTO.getCoverageStartDate());
        }
        
        if (employeeBenefitDTO.getCoverageEndDate() != null) {
            existingEmployeeBenefit.setCoverageEndDate(employeeBenefitDTO.getCoverageEndDate());
        }
        
        if (employeeBenefitDTO.getStatus() != null) {
            existingEmployeeBenefit.setStatus(employeeBenefitDTO.getStatus());
        }
        
        if (employeeBenefitDTO.getDependentsCount() != null) {
            existingEmployeeBenefit.setDependentsCount(employeeBenefitDTO.getDependentsCount());
        }
        
        if (employeeBenefitDTO.getAdditionalDetails() != null) {
            existingEmployeeBenefit.setAdditionalDetails(employeeBenefitDTO.getAdditionalDetails());
        }
        
        EmployeeBenefitEntity updatedEmployeeBenefit = employeeBenefitRepository.save(existingEmployeeBenefit);
        return convertToDTO(updatedEmployeeBenefit);
    }

    @Transactional
    public EmployeeBenefitDTO cancelEmployeeBenefit(String enrollmentId) {
        EmployeeBenefitEntity employeeBenefit = employeeBenefitRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Employee benefit enrollment not found with ID: " + enrollmentId));
        
        employeeBenefit.setStatus("INACTIVE");
        employeeBenefit.setCoverageEndDate(LocalDate.now());
        
        EmployeeBenefitEntity updatedEmployeeBenefit = employeeBenefitRepository.save(employeeBenefit);
        return convertToDTO(updatedEmployeeBenefit);
    }

    @Transactional
    public void deleteEmployeeBenefit(String enrollmentId) {
        if (!employeeBenefitRepository.existsById(enrollmentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Employee benefit enrollment not found with ID: " + enrollmentId);
        }
        
        employeeBenefitRepository.deleteById(enrollmentId);
    }

    // Helper methods
    private EmployeeBenefitDTO convertToDTO(EmployeeBenefitEntity employeeBenefit) {
        EmployeeBenefitDTO dto = new EmployeeBenefitDTO();
        dto.setEnrollmentId(employeeBenefit.getEnrollmentId());
        dto.setEmployeeId(employeeBenefit.getEmployee().getEmployeeId());
        dto.setEmployeeName(employeeBenefit.getEmployee().getFirstName() + " " + employeeBenefit.getEmployee().getLastName());
        dto.setBenefitPlanId(employeeBenefit.getBenefitPlan().getBenefitPlanId());
        dto.setPlanName(employeeBenefit.getBenefitPlan().getPlanName());
        dto.setPlanType(employeeBenefit.getBenefitPlan().getPlanType());
        dto.setEnrollmentDate(employeeBenefit.getEnrollmentDate());
        dto.setCoverageStartDate(employeeBenefit.getCoverageStartDate());
        dto.setCoverageEndDate(employeeBenefit.getCoverageEndDate());
        dto.setStatus(employeeBenefit.getStatus());
        dto.setDependentsCount(employeeBenefit.getDependentsCount());
        dto.setAdditionalDetails(employeeBenefit.getAdditionalDetails());
        return dto;
    }
} 