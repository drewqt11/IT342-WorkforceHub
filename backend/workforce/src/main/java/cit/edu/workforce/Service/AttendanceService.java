package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.AttendanceDTO;
import cit.edu.workforce.Entity.AttendanceEntity;
import cit.edu.workforce.Entity.CompanyLocationEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Repository.AttendanceRepository;
import cit.edu.workforce.Repository.CompanyLocationRepository;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Utils.LocationUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyLocationRepository companyLocationRepository;
    private final EmployeeService employeeService;
    private final LocationUtil locationUtil;

    @Autowired
    public AttendanceService(
            AttendanceRepository attendanceRepository,
            EmployeeRepository employeeRepository,
            CompanyLocationRepository companyLocationRepository,
            EmployeeService employeeService,
            LocationUtil locationUtil) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.companyLocationRepository = companyLocationRepository;
        this.employeeService = employeeService;
        this.locationUtil = locationUtil;
    }

    @Transactional(readOnly = true)
    public Optional<EmployeeEntity> getCurrentEmployee() {
        return employeeService.getCurrentEmployee()
                .map(employeeDTO -> employeeRepository.findById(employeeDTO.getEmployeeId())
                        .orElseThrow(() -> new EntityNotFoundException("Employee not found")));
    }

    @Transactional(readOnly = true)
    public Page<AttendanceDTO> getEmployeeAttendance(String employeeId, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

        return attendanceRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public AttendanceDTO clockIn(AttendanceDTO attendanceDTO) {
        EmployeeEntity employee = getCurrentEmployee()
                .orElseThrow(() -> new IllegalStateException("No authenticated employee found"));

        // Check if employee already has an active attendance record
        Optional<AttendanceEntity> activeAttendance = attendanceRepository.findActiveAttendanceByEmployee(
                employee, LocalDateTime.now().truncatedTo(ChronoUnit.DAYS));
        
        if (activeAttendance.isPresent()) {
            throw new IllegalStateException("Employee already clocked in for today");
        }

        // Get all active company locations
        List<CompanyLocationEntity> activeLocations = companyLocationRepository.findByIsActiveTrue();
        if (activeLocations.isEmpty()) {
            throw new IllegalStateException("No active company locations found");
        }

        // Find closest location and calculate distance
        CompanyLocationEntity closestLocation = findClosestLocation(
                attendanceDTO.getClockInLatitude(), 
                attendanceDTO.getClockInLongitude(), 
                activeLocations);
        
        double distance = locationUtil.calculateDistance(
                attendanceDTO.getClockInLatitude(),
                attendanceDTO.getClockInLongitude(),
                closestLocation.getLatitude(),
                closestLocation.getLongitude());
        
        boolean isWithinBoundary = distance <= closestLocation.getAllowedRadius();
        
        // Create new attendance record
        AttendanceEntity attendanceEntity = new AttendanceEntity();
        attendanceEntity.setEmployee(employee);
        attendanceEntity.setClockInTime(LocalDateTime.now());
        attendanceEntity.setClockInLatitude(attendanceDTO.getClockInLatitude());
        attendanceEntity.setClockInLongitude(attendanceDTO.getClockInLongitude());
        attendanceEntity.setDistanceFromOffice(distance);
        attendanceEntity.setIsWithinBoundary(isWithinBoundary);
        attendanceEntity.setClockInStatus(isWithinBoundary ? "VALID" : "INVALID_LOCATION");
        attendanceEntity.setNotes(attendanceDTO.getNotes());

        return convertToDTO(attendanceRepository.save(attendanceEntity));
    }

    @Transactional
    public AttendanceDTO clockOut(String attendanceId, AttendanceDTO attendanceDTO) {
        AttendanceEntity attendanceEntity = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new EntityNotFoundException("Attendance record not found with id: " + attendanceId));

        EmployeeEntity employee = getCurrentEmployee()
                .orElseThrow(() -> new IllegalStateException("No authenticated employee found"));

        // Verify this attendance record belongs to the current employee
        if (!attendanceEntity.getEmployee().getEmployeeId().equals(employee.getEmployeeId())) {
            throw new IllegalStateException("Attendance record does not belong to authenticated employee");
        }

        // Verify employee hasn't already clocked out
        if (attendanceEntity.getClockOutTime() != null) {
            throw new IllegalStateException("Employee has already clocked out for this record");
        }

        // Get closest location and calculate distance for clock out
        List<CompanyLocationEntity> activeLocations = companyLocationRepository.findByIsActiveTrue();
        CompanyLocationEntity closestLocation = findClosestLocation(
                attendanceDTO.getClockOutLatitude(),
                attendanceDTO.getClockOutLongitude(),
                activeLocations);
        
        double distance = locationUtil.calculateDistance(
                attendanceDTO.getClockOutLatitude(),
                attendanceDTO.getClockOutLongitude(),
                closestLocation.getLatitude(),
                closestLocation.getLongitude());
        
        boolean isWithinBoundary = distance <= closestLocation.getAllowedRadius();

        // Update attendance record
        attendanceEntity.setClockOutTime(LocalDateTime.now());
        attendanceEntity.setClockOutLatitude(attendanceDTO.getClockOutLatitude());
        attendanceEntity.setClockOutLongitude(attendanceDTO.getClockOutLongitude());
        attendanceEntity.setClockOutStatus(isWithinBoundary ? "VALID" : "INVALID_LOCATION");
        
        // Check if this is overtime (if needed based on business rules)
        // This is a simple example - you would define your own business rules
        LocalDateTime regularEndTime = attendanceEntity.getClockInTime().plusHours(8);
        if (attendanceEntity.getClockOutTime().isAfter(regularEndTime)) {
            attendanceEntity.setIsOvertime(true);
        }

        return convertToDTO(attendanceRepository.save(attendanceEntity));
    }

    private CompanyLocationEntity findClosestLocation(Double latitude, Double longitude, 
                                                     List<CompanyLocationEntity> locations) {
        if (locations.isEmpty()) {
            throw new IllegalStateException("No active company locations found");
        }
        
        CompanyLocationEntity closestLocation = locations.get(0);
        double shortestDistance = locationUtil.calculateDistance(
                latitude, longitude, 
                closestLocation.getLatitude(), closestLocation.getLongitude());
        
        for (int i = 1; i < locations.size(); i++) {
            CompanyLocationEntity location = locations.get(i);
            double distance = locationUtil.calculateDistance(
                    latitude, longitude, 
                    location.getLatitude(), location.getLongitude());
            
            if (distance < shortestDistance) {
                shortestDistance = distance;
                closestLocation = location;
            }
        }
        
        return closestLocation;
    }

    @Transactional(readOnly = true)
    public Optional<AttendanceDTO> getAttendanceById(String id) {
        return attendanceRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<AttendanceDTO> getActiveAttendance() {
        EmployeeEntity employee = getCurrentEmployee()
                .orElseThrow(() -> new IllegalStateException("No authenticated employee found"));

        return attendanceRepository.findActiveAttendanceByEmployee(employee, LocalDateTime.now().truncatedTo(ChronoUnit.DAYS))
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getEmployeeAttendanceForDateRange(String employeeId, LocalDateTime startDate, LocalDateTime endDate) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

        return attendanceRepository.findByEmployeeAndClockInTimeBetween(employee, startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private AttendanceDTO convertToDTO(AttendanceEntity entity) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getEmployee().getEmployeeId());
        dto.setEmployeeName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName());
        dto.setClockInTime(entity.getClockInTime());
        dto.setClockOutTime(entity.getClockOutTime());
        dto.setClockInLatitude(entity.getClockInLatitude());
        dto.setClockInLongitude(entity.getClockInLongitude());
        dto.setClockOutLatitude(entity.getClockOutLatitude());
        dto.setClockOutLongitude(entity.getClockOutLongitude());
        dto.setClockInStatus(entity.getClockInStatus());
        dto.setClockOutStatus(entity.getClockOutStatus());
        dto.setIsWithinBoundary(entity.getIsWithinBoundary());
        dto.setDistanceFromOffice(entity.getDistanceFromOffice());
        dto.setNotes(entity.getNotes());
        dto.setIsOvertime(entity.getIsOvertime());
        return dto;
    }
} 