package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.EmployeeDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.UserEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, UserRepository userRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }
    
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        EmployeeEntity employee = convertToEntity(employeeDTO);
        employee.setUser(user);
        
        EmployeeEntity savedEmployee = employeeRepository.save(employee);
        return convertToDTO(savedEmployee);
    }
    
    public EmployeeDTO getEmployeeById(Long id) {
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return convertToDTO(employee);
    }
    
    public EmployeeDTO getEmployeeByUserId(Long userId) {
        EmployeeEntity employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return convertToDTO(employee);
    }
    
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        EmployeeEntity existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        // Update fields
        existingEmployee.setFirstName(employeeDTO.getFirstName());
        existingEmployee.setLastName(employeeDTO.getLastName());
        existingEmployee.setEmail(employeeDTO.getEmail());
        existingEmployee.setPhoneNumber(employeeDTO.getPhoneNumber());
        existingEmployee.setDateOfBirth(employeeDTO.getDateOfBirth());
        existingEmployee.setDepartment(employeeDTO.getDepartment());
        existingEmployee.setPosition(employeeDTO.getPosition());
        
        EmployeeEntity updatedEmployee = employeeRepository.save(existingEmployee);
        return convertToDTO(updatedEmployee);
    }
    
    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee not found");
        }
        employeeRepository.deleteById(id);
    }
    
    // Helper methods for DTO conversion
    private EmployeeEntity convertToEntity(EmployeeDTO dto) {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setHireDate(dto.getHireDate());
        entity.setDepartment(dto.getDepartment());
        entity.setPosition(dto.getPosition());
        return entity;
    }
    
    private EmployeeDTO convertToDTO(EmployeeEntity entity) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setHireDate(entity.getHireDate());
        dto.setDepartment(entity.getDepartment());
        dto.setPosition(entity.getPosition());
        return dto;
    }
} 