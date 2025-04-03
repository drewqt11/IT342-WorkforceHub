package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.EmployeeDTO;
import cit.edu.workforce.Entity.Department;
import cit.edu.workforce.Entity.Employee;
import cit.edu.workforce.Entity.JobTitle;
import cit.edu.workforce.Entity.Role;
import cit.edu.workforce.Repository.DepartmentRepository;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.JobTitleRepository;
import cit.edu.workforce.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final JobTitleRepository jobTitleRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,
                          RoleRepository roleRepository,
                          JobTitleRepository jobTitleRepository,
                          DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.jobTitleRepository = jobTitleRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<EmployeeDTO> getAllEmployees(String status) {
        List<Employee> employees;
        
        if (status != null && !status.isEmpty()) {
            employees = employeeRepository.findByStatus(status);
        } else {
            employees = employeeRepository.findAll();
        }
        
        return employees.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return convertToDTO(employee);
    }

    public EmployeeDTO getEmployeeByUserId(UUID userId) {
        Employee employee = employeeRepository.findByUserAccountUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee profile not found"));
        
        return convertToDTO(employee);
    }

    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        // Check if email already exists
        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already taken");
        }
        
        Employee employee = new Employee();
        updateEmployeeFromDTO(employee, employeeDTO);
        Employee savedEmployee = employeeRepository.save(employee);
        
        return convertToDTO(savedEmployee);
    }

    public EmployeeDTO updateEmployee(UUID employeeId, EmployeeDTO employeeDTO) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        // Check if email is being changed and already exists
        if (!employee.getEmail().equals(employeeDTO.getEmail()) && 
                employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already taken");
        }
        
        updateEmployeeFromDTO(employee, employeeDTO);
        Employee updatedEmployee = employeeRepository.save(employee);
        
        return convertToDTO(updatedEmployee);
    }

    public EmployeeDTO updateEmployeeRole(UUID employeeId, String roleId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        
        employee.setRole(role);
        Employee updatedEmployee = employeeRepository.save(employee);
        
        return convertToDTO(updatedEmployee);
    }

    public EmployeeDTO updateEmployeeJobTitle(UUID employeeId, UUID jobId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        JobTitle jobTitle = jobTitleRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job title not found"));
        
        employee.setJobTitle(jobTitle);
        Employee updatedEmployee = employeeRepository.save(employee);
        
        return convertToDTO(updatedEmployee);
    }

    public EmployeeDTO updateEmployeeDepartment(UUID employeeId, UUID departmentId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        
        employee.setDepartment(department);
        Employee updatedEmployee = employeeRepository.save(employee);
        
        return convertToDTO(updatedEmployee);
    }

    public void deactivateEmployee(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        employee.setStatus("INACTIVE");
        employeeRepository.save(employee);
    }

    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setGender(employee.getGender());
        dto.setHireDate(employee.getHireDate());
        dto.setBirthDate(employee.getBirthDate());
        dto.setAddress(employee.getAddress());
        dto.setPhone(employee.getPhone());
        dto.setStatus(employee.getStatus());
        
        if (employee.getRole() != null) {
            dto.setRoleId(employee.getRole().getRoleId());
            dto.setRoleName(employee.getRole().getRoleName());
        }
        
        if (employee.getJobTitle() != null) {
            dto.setJobId(employee.getJobTitle().getJobId());
            dto.setJobName(employee.getJobTitle().getJobName());
        }
        
        if (employee.getDepartment() != null) {
            dto.setDepartmentId(employee.getDepartment().getDepartmentId());
            dto.setDepartmentName(employee.getDepartment().getDepartmentName());
        }
        
        return dto;
    }

    private void updateEmployeeFromDTO(Employee employee, EmployeeDTO dto) {
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setGender(dto.getGender());
        employee.setHireDate(dto.getHireDate());
        employee.setBirthDate(dto.getBirthDate());
        employee.setAddress(dto.getAddress());
        employee.setPhone(dto.getPhone());
        employee.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        
        if (dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
            employee.setRole(role);
        }
        
        if (dto.getJobId() != null) {
            JobTitle jobTitle = jobTitleRepository.findById(dto.getJobId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job title not found"));
            employee.setJobTitle(jobTitle);
        }
        
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
            employee.setDepartment(department);
        }
    }
} 