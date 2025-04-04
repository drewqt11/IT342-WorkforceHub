package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.EmployeeDTO;
import cit.edu.workforce.DTO.EmployeeRegistrationDTO;
import cit.edu.workforce.Entity.*;
import cit.edu.workforce.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserAccountService userAccountService;
    private final RoleService roleService;
    private final DepartmentService departmentService;
    private final JobTitleService jobTitleService;
    private final EmailDomainListService emailDomainListService;

    @Autowired
    public EmployeeService(
            EmployeeRepository employeeRepository,
            UserAccountService userAccountService,
            RoleService roleService,
            DepartmentService departmentService,
            JobTitleService jobTitleService,
            EmailDomainListService emailDomainListService) {
        this.employeeRepository = employeeRepository;
        this.userAccountService = userAccountService;
        this.roleService = roleService;
        this.departmentService = departmentService;
        this.jobTitleService = jobTitleService;
        this.emailDomainListService = emailDomainListService;
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllActiveEmployees() {
        return employeeRepository.findByStatus("ACTIVE").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<EmployeeDTO> getEmployeeById(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<EmployeeDTO> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    @Transactional
    public EmployeeDTO registerEmployee(EmployeeRegistrationDTO registrationDTO) {
        // Validate email domain
        if (!emailDomainListService.isValidDomain(registrationDTO.getEmail())) {
            throw new RuntimeException("Invalid email domain");
        }

        // Check if email already exists
        if (employeeRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create user account
        UserAccountEntity userAccount = userAccountService.createUserAccount(
                registrationDTO.getEmail(),
                registrationDTO.getPassword()
        );

        // Get default role (EMPLOYEE)
        RoleEntity role = roleService.getRoleById("ROLE_EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        // Create employee
        EmployeeEntity employee = new EmployeeEntity();
        employee.setFirstName(registrationDTO.getFirstName());
        employee.setLastName(registrationDTO.getLastName());
        employee.setEmail(registrationDTO.getEmail());
        employee.setGender(registrationDTO.getGender());
        employee.setDateOfBirth(registrationDTO.getDateOfBirth());
        employee.setAddress(registrationDTO.getAddress());
        employee.setPhoneNumber(registrationDTO.getPhoneNumber());
        employee.setMaritalStatus(registrationDTO.getMaritalStatus());
        employee.setHireDate(LocalDate.now());
        employee.setStatus("ACTIVE");
        employee.setRole(role);
        employee.setUserAccount(userAccount);

        EmployeeEntity savedEmployee = employeeRepository.save(employee);
        return convertToDTO(savedEmployee);
    }

    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        // Validate email domain
        if (!emailDomainListService.isValidDomain(employeeDTO.getEmail())) {
            throw new RuntimeException("Invalid email domain");
        }

        // Check if email already exists
        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Get role
        RoleEntity role = roleService.getRoleById(employeeDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Get department if specified
        DepartmentEntity department = null;
        if (employeeDTO.getDepartmentId() != null) {
            department = departmentService.getDepartmentById(employeeDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
        }

        // Get job title if specified
        JobTitleEntity jobTitle = null;
        if (employeeDTO.getJobId() != null) {
            jobTitle = jobTitleService.getJobTitleById(employeeDTO.getJobId())
                    .orElseThrow(() -> new RuntimeException("Job title not found"));
        }

        // Create employee
        EmployeeEntity employee = new EmployeeEntity();
        employee.setFirstName(employeeDTO.getFirstName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setEmail(employeeDTO.getEmail());
        employee.setGender(employeeDTO.getGender());
        employee.setHireDate(employeeDTO.getHireDate() != null ? employeeDTO.getHireDate() : LocalDate.now());
        employee.setDateOfBirth(employeeDTO.getDateOfBirth());
        employee.setAddress(employeeDTO.getAddress());
        employee.setPhoneNumber(employeeDTO.getPhoneNumber());
        employee.setMaritalStatus(employeeDTO.getMaritalStatus());
        employee.setStatus(employeeDTO.getStatus() != null ? employeeDTO.getStatus() : "ACTIVE");
        employee.setRole(role);
        employee.setDepartment(department);
        employee.setJobTitle(jobTitle);

        EmployeeEntity savedEmployee = employeeRepository.save(employee);
        return convertToDTO(savedEmployee);
    }

    @Transactional
    public EmployeeDTO updateEmployee(UUID employeeId, EmployeeDTO employeeDTO) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Update email if changed and not already taken
        if (!employee.getEmail().equals(employeeDTO.getEmail())) {
            if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
                throw new RuntimeException("Email already registered");
            }
            employee.setEmail(employeeDTO.getEmail());
        }

        // Update other fields
        employee.setFirstName(employeeDTO.getFirstName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setGender(employeeDTO.getGender());
        employee.setHireDate(employeeDTO.getHireDate());
        employee.setDateOfBirth(employeeDTO.getDateOfBirth());
        employee.setAddress(employeeDTO.getAddress());
        employee.setPhoneNumber(employeeDTO.getPhoneNumber());
        employee.setMaritalStatus(employeeDTO.getMaritalStatus());
        employee.setStatus(employeeDTO.getStatus());

        // Update role if specified
        if (employeeDTO.getRoleId() != null) {
            RoleEntity role = roleService.getRoleById(employeeDTO.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            employee.setRole(role);
        }

        // Update department if specified
        if (employeeDTO.getDepartmentId() != null) {
            DepartmentEntity department = departmentService.getDepartmentById(employeeDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            employee.setDepartment(department);
        }

        // Update job title if specified
        if (employeeDTO.getJobId() != null) {
            JobTitleEntity jobTitle = jobTitleService.getJobTitleById(employeeDTO.getJobId())
                    .orElseThrow(() -> new RuntimeException("Job title not found"));
            employee.setJobTitle(jobTitle);
        }

        EmployeeEntity updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    @Transactional
    public EmployeeDTO deactivateEmployee(UUID employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setStatus("INACTIVE");

        // Deactivate associated user account
        if (employee.getUserAccount() != null) {
            userAccountService.deactivateUser(employee.getUserAccount());
        }

        EmployeeEntity deactivatedEmployee = employeeRepository.save(employee);
        return convertToDTO(deactivatedEmployee);
    }

    @Transactional
    public EmployeeDTO activateEmployee(UUID employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setStatus("ACTIVE");

        // Activate associated user account
        if (employee.getUserAccount() != null) {
            userAccountService.activateUser(employee.getUserAccount());
        }

        EmployeeEntity activatedEmployee = employeeRepository.save(employee);
        return convertToDTO(activatedEmployee);
    }

    @Transactional
    public void deleteEmployee(UUID employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Delete associated user account
        if (employee.getUserAccount() != null) {
            userAccountService.deleteUser(employee.getUserAccount().getUserId());
        }

        employeeRepository.delete(employee);
    }

    @Transactional
    public EmployeeDTO assignRole(UUID employeeId, String roleId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        RoleEntity role = roleService.getRoleById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        employee.setRole(role);
        EmployeeEntity updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    @Transactional
    public EmployeeDTO assignDepartment(UUID employeeId, UUID departmentId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        DepartmentEntity department = departmentService.getDepartmentById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        employee.setDepartment(department);
        EmployeeEntity updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    @Transactional
    public EmployeeDTO assignJobTitle(UUID employeeId, UUID jobId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        JobTitleEntity jobTitle = jobTitleService.getJobTitleById(jobId)
                .orElseThrow(() -> new RuntimeException("Job title not found"));

        employee.setJobTitle(jobTitle);
        EmployeeEntity updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    private EmployeeDTO convertToDTO(EmployeeEntity employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setGender(employee.getGender());
        dto.setHireDate(employee.getHireDate());
        dto.setDateOfBirth(employee.getDateOfBirth());
        dto.setAddress(employee.getAddress());
        dto.setPhoneNumber(employee.getPhoneNumber());
        dto.setMaritalStatus(employee.getMaritalStatus());
        dto.setStatus(employee.getStatus());

        if (employee.getDepartment() != null) {
            dto.setDepartmentId(employee.getDepartment().getDepartmentId());
            dto.setDepartmentName(employee.getDepartment().getDepartmentName());
        }

        if (employee.getJobTitle() != null) {
            dto.setJobId(employee.getJobTitle().getJobId());
            dto.setJobName(employee.getJobTitle().getJobName());
        }

        if (employee.getRole() != null) {
            dto.setRoleId(employee.getRole().getRoleId());
            dto.setRoleName(employee.getRole().getRoleName());
        }

        return dto;
    }
    
    /**
     * Check if the provided employee ID matches the currently authenticated user's employee ID
     * Used for authorization checks in controllers
     *
     * @param employeeId The employee ID to check
     * @return true if the ID matches the current user's employee ID, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isCurrentEmployee(UUID employeeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String email = authentication.getName();
        Optional<EmployeeEntity> employeeOptional = employeeRepository.findByEmail(email);
        
        if (employeeOptional.isEmpty()) {
            return false;
        }

        return employeeOptional.get().getEmployeeId().equals(employeeId);
    }

    /**
     * Get the current authenticated employee
     *
     * @return Optional containing the current employee DTO if found
     */
    @Transactional(readOnly = true)
    public Optional<EmployeeDTO> getCurrentEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String email = authentication.getName();
        return employeeRepository.findByEmail(email)
                .map(this::convertToDTO);
    }
} 