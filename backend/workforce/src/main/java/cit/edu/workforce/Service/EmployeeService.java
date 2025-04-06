package cit.edu.workforce.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cit.edu.workforce.DTO.EmployeeDTO;
import cit.edu.workforce.DTO.EmployeeRegistrationDTO;
import cit.edu.workforce.Entity.DepartmentEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.JobTitleEntity;
import cit.edu.workforce.Entity.RoleEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.EmployeeRepository;

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
    public Page<EmployeeDTO> getAllEmployeesPaged(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> searchEmployees(String name, String employeeId, String department, String status, Pageable pageable) {
        // Using a simplified approach for demo purposes
        // In a real implementation, you would use a more sophisticated query builder
        if (name != null && !name.isEmpty()) {
            return employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name, pageable)
                    .map(this::convertToDTO);
        } else if (employeeId != null && !employeeId.isEmpty()) {
            try {
                UUID empId = UUID.fromString(employeeId);
                return employeeRepository.findByEmployeeId(empId, pageable)
                        .map(this::convertToDTO);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid employee ID format");
            }
        } else if (department != null && !department.isEmpty()) {
            return employeeRepository.findByDepartmentDepartmentNameContainingIgnoreCase(department, pageable)
                    .map(this::convertToDTO);
        } else if (status != null && !status.isEmpty()) {
            return employeeRepository.findByStatus(status, pageable)
                    .map(this::convertToDTO);
        } else {
            return employeeRepository.findAll(pageable)
                    .map(this::convertToDTO);
        }
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllActiveEmployees() {
        return employeeRepository.findByStatus("ACTIVE").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getAllActiveEmployeesPaged(Pageable pageable) {
        return employeeRepository.findByStatus("ACTIVE", pageable)
                .map(this::convertToDTO);
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email domain");
        }

        // Check if email already exists
        if (employeeRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        // Create user account
        UserAccountEntity userAccount = userAccountService.createUserAccount(
                registrationDTO.getEmail(),
                registrationDTO.getPassword()
        );

        // Get default role (EMPLOYEE)
        RoleEntity role = roleService.getRoleById("ROLE_EMPLOYEE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Default role not found"));

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
        employee.setEmploymentStatus("PENDING"); // New employees start as pending until approved
        employee.setRole(role);
        employee.setUserAccount(userAccount);

        EmployeeEntity savedEmployee = employeeRepository.save(employee);
        return convertToDTO(savedEmployee);
    }

    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        // Validate email domain
        if (!emailDomainListService.isValidDomain(employeeDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email domain");
        }

        // Check if email already exists
        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        // Get role
        RoleEntity role = roleService.getRoleById(employeeDTO.getRoleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        // Get department if specified
        DepartmentEntity department = null;
        if (employeeDTO.getDepartmentId() != null) {
            department = departmentService.getDepartmentById(employeeDTO.getDepartmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        }

        // Get job title if specified
        JobTitleEntity jobTitle = null;
        if (employeeDTO.getJobId() != null) {
            jobTitle = jobTitleService.getJobTitleById(employeeDTO.getJobId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job title not found"));
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
        employee.setEmploymentStatus(employeeDTO.getEmploymentStatus() != null ? employeeDTO.getEmploymentStatus() : "FULL_TIME");
        employee.setRole(role);
        employee.setDepartment(department);
        employee.setJobTitle(jobTitle);

        EmployeeEntity savedEmployee = employeeRepository.save(employee);
        return convertToDTO(savedEmployee);
    }

    @Transactional
    public EmployeeDTO updateEmployee(UUID employeeId, EmployeeDTO employeeDTO) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        // Update email if changed and not already taken
        if (!employee.getEmail().equals(employeeDTO.getEmail())) {
            if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
            }
            
            // Validate new email domain
            if (!emailDomainListService.isValidDomain(employeeDTO.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email domain");
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
        employee.setEmploymentStatus(employeeDTO.getEmploymentStatus());

        // Update role if specified
        if (employeeDTO.getRoleId() != null) {
            RoleEntity role = roleService.getRoleById(employeeDTO.getRoleId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
            employee.setRole(role);
        }

        // Update department if specified
        if (employeeDTO.getDepartmentId() != null) {
            DepartmentEntity department = departmentService.getDepartmentById(employeeDTO.getDepartmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
            employee.setDepartment(department);
        }

        // Update job title if specified
        if (employeeDTO.getJobId() != null) {
            JobTitleEntity jobTitle = jobTitleService.getJobTitleById(employeeDTO.getJobId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job title not found"));
            employee.setJobTitle(jobTitle);
        }

        EmployeeEntity updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    @Transactional
    public EmployeeDTO updateEmployeePartially(UUID employeeId, EmployeeDTO employeeDTO) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        // Only update fields that are not null
        if (employeeDTO.getFirstName() != null) {
            employee.setFirstName(employeeDTO.getFirstName());
        }
        
        if (employeeDTO.getLastName() != null) {
            employee.setLastName(employeeDTO.getLastName());
        }
        
        if (employeeDTO.getEmail() != null && !employee.getEmail().equals(employeeDTO.getEmail())) {
            if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
            }
            
            // Validate new email domain
            if (!emailDomainListService.isValidDomain(employeeDTO.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email domain");
            }
            
            employee.setEmail(employeeDTO.getEmail());
        }
        
        if (employeeDTO.getGender() != null) {
            employee.setGender(employeeDTO.getGender());
        }
        
        if (employeeDTO.getHireDate() != null) {
            employee.setHireDate(employeeDTO.getHireDate());
        }
        
        if (employeeDTO.getDateOfBirth() != null) {
            employee.setDateOfBirth(employeeDTO.getDateOfBirth());
        }
        
        if (employeeDTO.getAddress() != null) {
            employee.setAddress(employeeDTO.getAddress());
        }
        
        if (employeeDTO.getPhoneNumber() != null) {
            employee.setPhoneNumber(employeeDTO.getPhoneNumber());
        }
        
        if (employeeDTO.getMaritalStatus() != null) {
            employee.setMaritalStatus(employeeDTO.getMaritalStatus());
        }
        
        if (employeeDTO.getStatus() != null) {
            employee.setStatus(employeeDTO.getStatus());
        }
        
        if (employeeDTO.getEmploymentStatus() != null) {
            employee.setEmploymentStatus(employeeDTO.getEmploymentStatus());
        }

        EmployeeEntity updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    @Transactional
    public EmployeeDTO deactivateEmployee(UUID employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        employee.setStatus("INACTIVE");
        employee.setEmploymentStatus("INACTIVE");
        
        // Also deactivate the user account if it exists
        if (employee.getUserAccount() != null) {
            UserAccountEntity userAccount = employee.getUserAccount();
            userAccount.setActive(false);
            // The UserAccountEntity will be automatically saved by cascade
        }

        EmployeeEntity updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    @Transactional
    public EmployeeDTO activateEmployee(UUID employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        employee.setStatus("ACTIVE");
        employee.setEmploymentStatus("ACTIVE");
        
        // Also activate the user account if it exists
        if (employee.getUserAccount() != null) {
            UserAccountEntity userAccount = employee.getUserAccount();
            userAccount.setActive(true);
            // The UserAccountEntity will be automatically saved by cascade
        }

        EmployeeEntity updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    @Transactional
    public EmployeeDTO assignRole(UUID employeeId, String roleId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        RoleEntity role = roleService.getRoleById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        employee.setRole(role);
        EmployeeEntity updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    @Transactional
    public EmployeeDTO assignDepartment(UUID employeeId, UUID departmentId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        DepartmentEntity department = departmentService.getDepartmentById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

        employee.setDepartment(department);
        EmployeeEntity updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    @Transactional
    public EmployeeDTO assignJobTitle(UUID employeeId, UUID jobId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        JobTitleEntity jobTitle = jobTitleService.getJobTitleById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job title not found"));

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
        dto.setEmploymentStatus(employee.getEmploymentStatus());

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

    @Transactional(readOnly = true)
    public boolean isCurrentEmployee(UUID employeeId) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        String email = authentication.getName();
        Optional<EmployeeEntity> employeeOpt = employeeRepository.findByEmail(email);
        if (employeeOpt.isEmpty()) {
            return false;
        }

        return employeeOpt.get().getEmployeeId().equals(employeeId);
    }

    @Transactional(readOnly = true)
    public Optional<EmployeeDTO> getCurrentEmployee() {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }

        String email = authentication.getName();
        return employeeRepository.findByEmail(email)
                .map(this::convertToDTO);
    }
} 