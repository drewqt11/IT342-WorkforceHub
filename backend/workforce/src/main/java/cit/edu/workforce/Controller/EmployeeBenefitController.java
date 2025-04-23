package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.EmployeeBenefitDTO;
import cit.edu.workforce.Service.EmployeeBenefitService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Employee Benefits", description = "APIs for managing employee benefit enrollments")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeBenefitController {

    private final EmployeeBenefitService employeeBenefitService;

    @Autowired
    public EmployeeBenefitController(EmployeeBenefitService employeeBenefitService) {
        this.employeeBenefitService = employeeBenefitService;
    }

    @GetMapping("/employee/benefits")
    @Operation(summary = "Get my benefits", description = "Get benefits for the currently logged-in employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<EmployeeBenefitDTO>> getMyBenefits() {
        try {
            return ResponseEntity.ok(employeeBenefitService.getCurrentEmployeeBenefits());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/hr/employee-benefits")
    @Operation(summary = "Get all employee benefits", description = "Get a paginated list of all employee benefit enrollments")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<EmployeeBenefitDTO>> getAllEmployeeBenefits(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "enrollmentDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(employeeBenefitService.getAllEmployeeBenefitsPaged(pageable));
    }

    @GetMapping("/hr/employees/{employeeId}/benefits")
    @Operation(summary = "Get employee benefits", description = "Get benefits for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<EmployeeBenefitDTO>> getEmployeeBenefits(@PathVariable String employeeId) {
        try {
            return ResponseEntity.ok(employeeBenefitService.getEmployeeBenefits(employeeId));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/hr/employees/{employeeId}/benefits/paged")
    @Operation(summary = "Get employee benefits (paged)", description = "Get a paginated list of benefits for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<EmployeeBenefitDTO>> getEmployeeBenefitsPaged(
            @PathVariable String employeeId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "enrollmentDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        try {
            return ResponseEntity.ok(employeeBenefitService.getEmployeeBenefitsPaged(employeeId, pageable));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/hr/employees/{employeeId}/active-benefits")
    @Operation(summary = "Get employee active benefits", description = "Get active benefits for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<EmployeeBenefitDTO>> getEmployeeActiveBenefits(@PathVariable String employeeId) {
        try {
            return ResponseEntity.ok(employeeBenefitService.getEmployeeActiveBenefits(employeeId));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/hr/benefit-plans/{benefitPlanId}/enrollments")
    @Operation(summary = "Get benefit plan enrollments", description = "Get enrollments for a specific benefit plan")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<EmployeeBenefitDTO>> getBenefitPlanEnrollments(@PathVariable String benefitPlanId) {
        try {
            return ResponseEntity.ok(employeeBenefitService.getBenefitPlanEnrollments(benefitPlanId));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/hr/benefit-plans/{benefitPlanId}/enrollments/paged")
    @Operation(summary = "Get benefit plan enrollments (paged)", description = "Get a paginated list of enrollments for a specific benefit plan")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<EmployeeBenefitDTO>> getBenefitPlanEnrollmentsPaged(
            @PathVariable String benefitPlanId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "enrollmentDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        try {
            return ResponseEntity.ok(employeeBenefitService.getBenefitPlanEnrollmentsPaged(benefitPlanId, pageable));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/hr/employee-benefits/{id}")
    @Operation(summary = "Get employee benefit by ID", description = "Get an employee benefit enrollment by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeBenefitDTO> getEmployeeBenefitById(@PathVariable("id") String enrollmentId) {
        return employeeBenefitService.getEmployeeBenefitById(enrollmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/hr/employee-benefits")
    @Operation(summary = "Enroll employee in benefit plan", description = "Enroll an employee in a benefit plan")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeBenefitDTO> enrollEmployeeInBenefit(@Valid @RequestBody EmployeeBenefitDTO employeeBenefitDTO) {
        try {
            EmployeeBenefitDTO enrolledBenefit = employeeBenefitService.enrollEmployeeInBenefit(employeeBenefitDTO);
            return new ResponseEntity<>(enrolledBenefit, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error enrolling employee in benefit plan", e);
        }
    }

    @PutMapping("/hr/employee-benefits/{id}")
    @Operation(summary = "Update employee benefit", description = "Update an employee benefit enrollment")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeBenefitDTO> updateEmployeeBenefit(
            @PathVariable("id") String enrollmentId,
            @Valid @RequestBody EmployeeBenefitDTO employeeBenefitDTO) {
        try {
            EmployeeBenefitDTO updatedBenefit = employeeBenefitService.updateEmployeeBenefit(enrollmentId, employeeBenefitDTO);
            return ResponseEntity.ok(updatedBenefit);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating employee benefit", e);
        }
    }

    @PutMapping("/hr/employee-benefits/{id}/cancel")
    @Operation(summary = "Cancel employee benefit", description = "Cancel an employee benefit enrollment")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeBenefitDTO> cancelEmployeeBenefit(@PathVariable("id") String enrollmentId) {
        try {
            EmployeeBenefitDTO canceledBenefit = employeeBenefitService.cancelEmployeeBenefit(enrollmentId);
            return ResponseEntity.ok(canceledBenefit);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error canceling employee benefit", e);
        }
    }

    @DeleteMapping("/admin/employee-benefits/{id}")
    @Operation(summary = "Delete employee benefit", description = "Delete an employee benefit enrollment (Admin only)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteEmployeeBenefit(@PathVariable("id") String enrollmentId) {
        try {
            employeeBenefitService.deleteEmployeeBenefit(enrollmentId);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting employee benefit", e);
        }
    }
}

// New file: Controller for employee benefits in the Benefits Administration module
// Provides endpoints for enrolling employees in benefit plans and managing enrollments 