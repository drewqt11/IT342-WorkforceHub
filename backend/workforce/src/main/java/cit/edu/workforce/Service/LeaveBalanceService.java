package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.LeaveBalanceDTO;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.LeaveBalanceEntity;
import cit.edu.workforce.Entity.LeaveTypeEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.LeaveBalanceRepository;
import cit.edu.workforce.Repository.LeaveTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeService employeeService;

    @Autowired
    public LeaveBalanceService(
            LeaveBalanceRepository leaveBalanceRepository,
            EmployeeRepository employeeRepository,
            LeaveTypeRepository leaveTypeRepository,
            EmployeeService employeeService) {
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.employeeRepository = employeeRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.employeeService = employeeService;
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceDTO> getEmployeeLeaveBalances(String employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

        return leaveBalanceRepository.findByEmployeeAndIsActiveTrue(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<LeaveBalanceDTO> getEmployeeLeaveBalancesPaged(String employeeId, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

        return leaveBalanceRepository.findByEmployeeAndIsActiveTrue(employee, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceDTO> getCurrentEmployeeLeaveBalances() {
        return employeeService.getCurrentEmployee()
                .map(employeeDTO -> getEmployeeLeaveBalances(employeeDTO.getEmployeeId()))
                .orElseThrow(() -> new IllegalStateException("No authenticated employee found"));
    }

    @Transactional(readOnly = true)
    public Optional<LeaveBalanceDTO> getLeaveBalanceById(String id) {
        return leaveBalanceRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public LeaveBalanceDTO createLeaveBalance(LeaveBalanceDTO leaveBalanceDTO) {
        EmployeeEntity employee = employeeRepository.findById(leaveBalanceDTO.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + leaveBalanceDTO.getEmployeeId()));

        LeaveTypeEntity leaveType = leaveTypeRepository.findById(leaveBalanceDTO.getLeaveTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Leave type not found with id: " + leaveBalanceDTO.getLeaveTypeId()));

        // Check if employee already has this leave type allocated for the year
        Optional<LeaveBalanceEntity> existingBalance = leaveBalanceRepository
                .findByEmployeeAndLeaveTypeAndYearAndIsActiveTrue(employee, leaveType, leaveBalanceDTO.getYear());
        
        if (existingBalance.isPresent()) {
            throw new IllegalArgumentException("Employee already has " + leaveType.getName() + 
                    " leave balance for year " + leaveBalanceDTO.getYear());
        }

        LeaveBalanceEntity entity = new LeaveBalanceEntity();
        entity.setEmployee(employee);
        entity.setLeaveType(leaveType);
        entity.setAllowedDays(leaveBalanceDTO.getAllowedDays());
        entity.setUsedDays(0.0);
        entity.setPendingDays(0.0);
        entity.setYear(leaveBalanceDTO.getYear());
        entity.setExpiryDate(leaveBalanceDTO.getExpiryDate());
        entity.setIsActive(true);

        return convertToDTO(leaveBalanceRepository.save(entity));
    }

    @Transactional
    public LeaveBalanceDTO updateLeaveBalance(String id, LeaveBalanceDTO leaveBalanceDTO) {
        LeaveBalanceEntity existingBalance = leaveBalanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Leave balance not found with id: " + id));

        existingBalance.setAllowedDays(leaveBalanceDTO.getAllowedDays());
        if (leaveBalanceDTO.getUsedDays() != null) {
            existingBalance.setUsedDays(leaveBalanceDTO.getUsedDays());
        }
        if (leaveBalanceDTO.getPendingDays() != null) {
            existingBalance.setPendingDays(leaveBalanceDTO.getPendingDays());
        }
        existingBalance.setExpiryDate(leaveBalanceDTO.getExpiryDate());
        if (leaveBalanceDTO.getIsActive() != null) {
            existingBalance.setIsActive(leaveBalanceDTO.getIsActive());
        }

        return convertToDTO(leaveBalanceRepository.save(existingBalance));
    }

    @Transactional
    public LeaveBalanceDTO assignLeaveToEmployee(String employeeId, String leaveTypeId, Integer year) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

        LeaveTypeEntity leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Leave type not found with id: " + leaveTypeId));

        // Check if employee already has this leave type allocated for the year
        Optional<LeaveBalanceEntity> existingBalance = leaveBalanceRepository
                .findByEmployeeAndLeaveTypeAndYearAndIsActiveTrue(employee, leaveType, year);
        
        if (existingBalance.isPresent()) {
            throw new IllegalArgumentException("Employee already has " + leaveType.getName() + 
                    " leave balance for year " + year);
        }

        // Create a new leave balance with default values
        LeaveBalanceEntity leaveBalance = new LeaveBalanceEntity();
        leaveBalance.setEmployee(employee);
        leaveBalance.setLeaveType(leaveType);
        leaveBalance.setAllowedDays((double) leaveType.getDefaultDays());
        leaveBalance.setUsedDays(0.0);
        leaveBalance.setPendingDays(0.0);
        leaveBalance.setYear(year);
        
        // Set expiry date to end of the following year
        leaveBalance.setExpiryDate(LocalDate.of(year + 1, 12, 31));
        
        leaveBalance.setIsActive(true);

        return convertToDTO(leaveBalanceRepository.save(leaveBalance));
    }

    @Transactional
    public void updateLeaveBalanceForRequest(String employeeId, String leaveTypeId, Integer year, Double daysPending, boolean isApproved) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

        LeaveTypeEntity leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Leave type not found with id: " + leaveTypeId));

        LeaveBalanceEntity leaveBalance = leaveBalanceRepository
                .findByEmployeeAndLeaveTypeAndYearAndIsActiveTrue(employee, leaveType, year)
                .orElseThrow(() -> new EntityNotFoundException("Leave balance not found for employee with leave type for year " + year));

        if (isApproved) {
            // Move pending days to used days
            double newUsedDays = leaveBalance.getUsedDays() + daysPending;
            double newPendingDays = leaveBalance.getPendingDays() - daysPending;
            
            leaveBalance.setUsedDays(newUsedDays);
            leaveBalance.setPendingDays(newPendingDays);
        } else {
            // Just reduce pending days
            double newPendingDays = leaveBalance.getPendingDays() - daysPending;
            leaveBalance.setPendingDays(newPendingDays);
        }

        leaveBalanceRepository.save(leaveBalance);
    }

    @Transactional
    public void addPendingDaysToLeaveBalance(String employeeId, String leaveTypeId, Integer year, Double days) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

        LeaveTypeEntity leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Leave type not found with id: " + leaveTypeId));

        LeaveBalanceEntity leaveBalance = leaveBalanceRepository
                .findByEmployeeAndLeaveTypeAndYearAndIsActiveTrue(employee, leaveType, year)
                .orElseThrow(() -> new EntityNotFoundException("Leave balance not found for employee with leave type for year " + year));

        double newPendingDays = leaveBalance.getPendingDays() + days;
        leaveBalance.setPendingDays(newPendingDays);

        leaveBalanceRepository.save(leaveBalance);
    }

    @Transactional
    public LeaveBalanceDTO deactivateLeaveBalance(String id) {
        LeaveBalanceEntity leaveBalance = leaveBalanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Leave balance not found with id: " + id));
        
        leaveBalance.setIsActive(false);
        return convertToDTO(leaveBalanceRepository.save(leaveBalance));
    }

    private LeaveBalanceDTO convertToDTO(LeaveBalanceEntity entity) {
        LeaveBalanceDTO dto = new LeaveBalanceDTO();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getEmployee().getEmployeeId());
        dto.setEmployeeName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName());
        dto.setLeaveTypeId(entity.getLeaveType().getId());
        dto.setLeaveTypeName(entity.getLeaveType().getName());
        dto.setAllowedDays(entity.getAllowedDays());
        dto.setUsedDays(entity.getUsedDays());
        dto.setPendingDays(entity.getPendingDays());
        dto.setRemainingDays(entity.getAllowedDays() - entity.getUsedDays() - entity.getPendingDays());
        dto.setYear(entity.getYear());
        dto.setExpiryDate(entity.getExpiryDate());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }
}

// New file: Service for leave balance management
// Handles employee leave entitlements, usage tracking, and balance calculations 