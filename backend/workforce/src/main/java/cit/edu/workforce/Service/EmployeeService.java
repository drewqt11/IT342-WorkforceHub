package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.EmployeeDTO;
import cit.edu.workforce.DTO.EmployeeRegistrationDTO;
import cit.edu.workforce.Entity.*;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
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
    public Page<EmployeeDTO> getAllEmployeesPaged(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDTO> searchEmployees(String name, String employeeId, String department, String status,
            Pageable pageable) {
        // Using a simplified approach for demo purposes
        // In a real implementation, you would use a more sophisticated query builder
        if (name != null && !name.isEmpty()) {
            return employeeRepository
                    .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name, pageable)
                    .map(this::convertToDTO);
        } else if (employeeId != null && !employeeId.isEmpty()) {
            return employeeRepository.findByEmployeeId(employeeId, pageable)
                    .map(this::convertToDTO);
        } else if (department != null && !department.isEmpty()) {
            return employeeRepository.findByDepartmentDepartmentNameContainingIgnoreCase(department, pageable)
                    .map(this::convertToDTO);
        } else if (status != null && !status.isEmpty()) {
            return employeeRepository.findByStatus(Boolean.parseBoolean(status), pageable)
                    .map(this::convertToDTO);
        } else {
            return employeeRepository.findAll(pageable)
                    .map(this::convertToDTO);
        }
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllActiveEmployees() {
        return employeeRepository.findByStatus(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getAllActiveEmployeesPaged(Pageable pageable) {
        return employeeRepository.findByStatus(true, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Update an employee's role
     *
     * @param employeeId The ID of the employee to update
     * @param role       The new role to assign
     * @return The updated employee DTO
     */
    @Transactional
    public EmployeeDTO updateEmployeeRole(String employeeId, RoleEntity role) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Employee not found with ID: " + employeeId));

        // Validate that the role exists in the predefined list
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role cannot be null");
        }

        // Update the employee's role
        employee.setRole(role);
        EmployeeEntity updatedEmployee = employeeRepository.save(employee);

        return convertToDTO(updatedEmployee);
    }

    /**
     * Update an employee's job title
     *
     * @param employeeId The ID of the employee to update
     * @param jobTitle   The new job title to assign
     * @return The updated employee DTO
     */
    @Transactional
    public EmployeeDTO updateEmployeeJobTitle(String employeeId, JobTitleEntity jobTitle) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Employee not found with ID: " + employeeId));

        // Validate that the job title exists in the predefined list
        if (jobTitle == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job title cannot be null");
        }

        // Update the employee's job title
        employee.setJobTitle(jobTitle);
        EmployeeEntity updatedEmployee = employeeRepository.save(employee);

        return convertToDTO(updatedEmployee);
    }

    /**
     * Update an employee's department
     *
     * @param employeeId The ID of the employee to update
     * @param department The new department to assign
     * @return The updated employee DTO
     */
    @Transactional
    public EmployeeDTO updateEmployeeDepartment(String employeeId, DepartmentEntity department) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Employee not found with ID: " + employeeId));

        // Validate that the department exists in the predefined list
        if (department == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department cannot be null");
        }

        // Update the employee's department and set job title to null
        employee.setDepartment(department);
        employee.setJobTitle(null); // Set job title to null when department changes
        EmployeeEntity updatedEmployee = employeeRepository.save(employee);

        return convertToDTO(updatedEmployee);
    }

    @Transactional(readOnly = true)
    public Optional<EmployeeDTO> getEmployeeById(String employeeId) {
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
                registrationDTO.getEmail());

        // Get default role (EMPLOYEE)
        RoleEntity role = roleService.getRoleById("ROLE_EMPLOYEE")
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Default role not found"));

        // Extract ID number and first name if the first name contains an ID number
        String idNumber = "";
        String firstName = registrationDTO.getFirstName();

        if (firstName != null && firstName.matches(".*\\d+.*")) {
            String[] parts = extractIdNumberAndName(firstName);
            idNumber = parts[0];
            firstName = parts[1];
        }

        // Create employee
        EmployeeEntity employee = new EmployeeEntity();
        employee.setFirstName(firstName);
        employee.setIdNumber(idNumber);
        employee.setLastName(registrationDTO.getLastName());
        employee.setEmail(registrationDTO.getEmail());
        employee.setGender(registrationDTO.getGender());
        employee.setDateOfBirth(registrationDTO.getDateOfBirth());
        employee.setAddress(registrationDTO.getAddress());
        employee.setPhoneNumber(registrationDTO.getPhoneNumber());
        employee.setMaritalStatus(registrationDTO.getMaritalStatus());
        employee.setHireDate(LocalDate.now());
        employee.setStatus(false);
        employee.setEmploymentStatus("PENDING"); // New employees start as pending until approved
        employee.setRole(role);
        employee.setWorkTimeInSched(registrationDTO.getWorkTimeInSched());
        employee.setWorkTimeOutSched(registrationDTO.getWorkTimeOutSched());
        employee.setUserAccount(userAccount);

        EmployeeEntity savedEmployee = employeeRepository.save(employee);
        return convertToDTO(savedEmployee);
    }

    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        // Extract ID number and first name if the first name contains an ID number
        if (employeeDTO.getFirstName() != null && employeeDTO.getFirstName().matches(".*\\d+.*")) {
            String[] parts = extractIdNumberAndName(employeeDTO.getFirstName());
            employeeDTO.setIdNumber(parts[0]);
            employeeDTO.setFirstName(parts[1]);
        }

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
        employee.setIdNumber(employeeDTO.getIdNumber());
        employee.setLastName(employeeDTO.getLastName());
        employee.setEmail(employeeDTO.getEmail());
        employee.setGender(employeeDTO.getGender());
        employee.setHireDate(employeeDTO.getHireDate() != null ? employeeDTO.getHireDate() : LocalDate.now());
        employee.setDateOfBirth(employeeDTO.getDateOfBirth());
        employee.setAddress(employeeDTO.getAddress());
        employee.setPhoneNumber(employeeDTO.getPhoneNumber());
        employee.setMaritalStatus(employeeDTO.getMaritalStatus());
        employee.setStatus(employeeDTO.getStatus() != null ? employeeDTO.getStatus() : true);
        employee.setEmploymentStatus(
                employeeDTO.getEmploymentStatus() != null ? employeeDTO.getEmploymentStatus() : "FULL_TIME");
        employee.setRole(role);
        employee.setDepartment(department);
        employee.setJobTitle(jobTitle);
        employee.setWorkTimeInSched(employeeDTO.getWorkTimeInSched());
        employee.setWorkTimeOutSched(employeeDTO.getWorkTimeOutSched());


        EmployeeEntity savedEmployee = employeeRepository.save(employee);
        return convertToDTO(savedEmployee);
    }

    @Transactional
    public EmployeeDTO updateEmployee(String employeeId, EmployeeDTO employeeDTO) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        // Extract ID number and first name if the first name contains an ID number
        if (employeeDTO.getFirstName() != null && employeeDTO.getFirstName().matches(".*\\d+.*")) {
            String[] parts = extractIdNumberAndName(employeeDTO.getFirstName());
            employeeDTO.setIdNumber(parts[0]);
            employeeDTO.setFirstName(parts[1]);
        }

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
        employee.setIdNumber(employeeDTO.getIdNumber());
        employee.setLastName(employeeDTO.getLastName());
        employee.setGender(employeeDTO.getGender());
        employee.setHireDate(employeeDTO.getHireDate());
        employee.setDateOfBirth(employeeDTO.getDateOfBirth());
        employee.setAddress(employeeDTO.getAddress());
        employee.setPhoneNumber(employeeDTO.getPhoneNumber());
        employee.setMaritalStatus(employeeDTO.getMaritalStatus());
        employee.setStatus(employeeDTO.getStatus() != null ? employeeDTO.getStatus() : true);
        employee.setEmploymentStatus(
                employeeDTO.getEmploymentStatus() != null ? employeeDTO.getEmploymentStatus() : "FULL_TIME");
        employee.setWorkTimeInSched(employeeDTO.getWorkTimeInSched());
        employee.setWorkTimeOutSched(employeeDTO.getWorkTimeOutSched());

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
    public EmployeeDTO updateEmployeePartially(String employeeId, EmployeeDTO employeeDTO) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        // Extract ID number and first name if the first name contains an ID number
        if (employeeDTO.getFirstName() != null && employeeDTO.getFirstName().matches(".*\\d+.*")) {
            String[] parts = extractIdNumberAndName(employeeDTO.getFirstName());
            employeeDTO.setIdNumber(parts[0]);
            employeeDTO.setFirstName(parts[1]);
        }

        // Only update fields that are not null
        if (employeeDTO.getFirstName() != null) {
            employee.setFirstName(employeeDTO.getFirstName());
        }

        if (employeeDTO.getIdNumber() != null) {
            employee.setIdNumber(employeeDTO.getIdNumber());
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
            employee.setStatus(employeeDTO.getStatus() != null ? employeeDTO.getStatus() : true);
        }

        if (employeeDTO.getEmploymentStatus() != null) {
            employee.setEmploymentStatus(
                    employeeDTO.getEmploymentStatus() != null ? employeeDTO.getEmploymentStatus() : "FULL_TIME");
        }

        if (employeeDTO.getWorkTimeInSched() != null) {
            employee.setWorkTimeInSched(employeeDTO.getWorkTimeInSched());
        }

        if (employeeDTO.getWorkTimeOutSched() != null) {
            employee.setWorkTimeOutSched(employeeDTO.getWorkTimeOutSched());
        }

        EmployeeEntity updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    @Transactional
    public EmployeeDTO deactivateEmployee(String employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        employee.setStatus(false);
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
    public EmployeeDTO activateEmployee(String employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        employee.setStatus(true);
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
    public EmployeeDTO assignRole(String employeeId, String roleId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        RoleEntity role = roleService.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        // Update the employee's role
        employee.setRole(role);
        employee = employeeRepository.save(employee);

        // Convert to DTO and return
        return convertToDTO(employee);
    }

    @Transactional
    public EmployeeDTO assignDepartment(String employeeId, String departmentId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        DepartmentEntity department = departmentService.getDepartmentById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

        employee.setDepartment(department);
        EmployeeEntity updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    @Transactional
    public EmployeeDTO assignJobTitle(String employeeId, String jobId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        JobTitleEntity jobTitle = jobTitleService.getJobTitleById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job title not found"));

        employee.setJobTitle(jobTitle);
        EmployeeEntity updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    /**
     * Update an employee's work time schedule
     * 
     * @param employeeId The ID of the employee to update
     * @param workTimeInSched The new work time in schedule
     * @param workTimeOutSched The new work time out schedule
     * @return The updated employee DTO
     */
    @Transactional
    public EmployeeDTO updateWorkTimeSchedule(String employeeId, LocalTime workTimeInSched, LocalTime workTimeOutSched) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        // Validate that work time out is after work time in
        if (workTimeInSched != null && workTimeOutSched != null && workTimeOutSched.isBefore(workTimeInSched)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Work time out must be after work time in");
        }

        employee.setWorkTimeInSched(workTimeInSched);
        employee.setWorkTimeOutSched(workTimeOutSched);
        
        EmployeeEntity updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    private EmployeeDTO convertToDTO(EmployeeEntity employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setIdNumber(employee.getIdNumber());
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
        dto.setCreatedAt(employee.getCreatedAt());

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

        if (employee.getWorkTimeInSched() != null) {
            dto.setWorkTimeInSched(employee.getWorkTimeInSched());
        }

        if (employee.getWorkTimeOutSched() != null) {
            dto.setWorkTimeOutSched(employee.getWorkTimeOutSched());
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public boolean isCurrentEmployee(String employeeId) {
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

    /**
     * Extracts ID number and name from a string containing both
     * 
     * @param input String containing ID number and name (e.g., "22-3326-574 Katrina" or "223326574 Katrina")
     * @return String array where [0] is the ID number and [1] is the name
     */
    private String[] extractIdNumberAndName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new String[] { "", "" };
        }

        input = input.trim();

        // Split by whitespace
        String[] parts = input.split("\\s+", 2);

        if (parts.length == 2) {
            String potentialIdNumber = parts[0];
            String name = parts[1];

            // Check if the first part matches either:
            // 1. ID number format with dashes (numbers and dashes only)
            // 2. Pure number format (only digits)
            if (potentialIdNumber.matches("^\\d+(-\\d+)*$") || potentialIdNumber.matches("^\\d+$")) {
                return new String[] { potentialIdNumber, name };
            }
        }

        // If no valid ID number found, return the original input as the name
        return new String[] { "", input };
    }
}