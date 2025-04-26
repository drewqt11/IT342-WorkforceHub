package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.AttendanceRecordDTO;
import cit.edu.workforce.DTO.ClockInRequestDTO;
import cit.edu.workforce.Entity.AttendanceRecordEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Repository.AttendanceRecordRepository;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AttendanceService - Service for managing attendance records
 * Updated file: Removed location validation to follow the ERD
 */
@Service
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public AttendanceService(
            AttendanceRecordRepository attendanceRepository,
            EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Clock in an employee
     */
    @Transactional
    public AttendanceRecordDTO clockIn(ClockInRequestDTO clockInRequest) {
        EmployeeEntity employee = getCurrentEmployee();
        LocalDate today = LocalDate.now();

        // Check if employee has already clocked in today
        Optional<AttendanceRecordEntity> existingRecord = attendanceRepository.findByEmployeeAndDate(employee, today);
        if (existingRecord.isPresent() && existingRecord.get().getClockInTime() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already clocked in today");
        }

        // Create or update attendance record
        AttendanceRecordEntity record = existingRecord.orElse(new AttendanceRecordEntity());
        if (record.getAttendanceId() == null) {
            record.setEmployee(employee);
            record.setDate(today);
        }

        LocalTime currentTime = LocalTime.now();
        record.setClockInTime(currentTime);
        
        // Set status based on time
        LocalTime startOfDay = LocalTime.of(9, 0); // 9:00 AM
        if (currentTime.isAfter(startOfDay.plusMinutes(15))) {
            record.setStatus("LATE");
        } else {
            record.setStatus("PRESENT");
        }
        
        if (clockInRequest.getRemarks() != null && !clockInRequest.getRemarks().isEmpty()) {
            record.setRemarks(clockInRequest.getRemarks());
        }

        AttendanceRecordEntity savedRecord = attendanceRepository.save(record);
        return convertToDTO(savedRecord);
    }

    /**
     * Clock out an employee
     */
    @Transactional
    public AttendanceRecordDTO clockOut(ClockInRequestDTO clockOutRequest) {
        EmployeeEntity employee = getCurrentEmployee();
        LocalDate today = LocalDate.now();

        // Find today's attendance record
        AttendanceRecordEntity record = attendanceRepository.findByEmployeeAndDate(employee, today)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No clock-in record found for today"));

        if (record.getClockInTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must clock in before clocking out");
        }
        
        if (record.getClockOutTime() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already clocked out today");
        }

        // Update record with clock out time
        LocalTime currentTime = LocalTime.now();
        record.setClockOutTime(currentTime);
        
        // Calculate total hours
        Duration duration = Duration.between(record.getClockInTime(), currentTime);
        double hours = duration.toMinutes() / 60.0;
        BigDecimal totalHours = BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP);
        record.setTotalHours(totalHours);
        
        // Calculate overtime hours (anything over 8 hours)
        if (hours > 8) {
            BigDecimal overtimeHours = BigDecimal.valueOf(hours - 8).setScale(2, RoundingMode.HALF_UP);
            record.setOvertimeHours(overtimeHours);
        } else {
            record.setOvertimeHours(BigDecimal.ZERO);
        }
        
        if (clockOutRequest.getRemarks() != null && !clockOutRequest.getRemarks().isEmpty()) {
            String combinedRemarks = record.getRemarks() != null 
                ? record.getRemarks() + " | " + clockOutRequest.getRemarks()
                : clockOutRequest.getRemarks();
            record.setRemarks(combinedRemarks);
        }

        AttendanceRecordEntity savedRecord = attendanceRepository.save(record);
        return convertToDTO(savedRecord);
    }

    /**
     * Get the current employee's attendance record for today
     */
    @Transactional(readOnly = true)
    public Optional<AttendanceRecordDTO> getTodayAttendance() {
        EmployeeEntity employee = getCurrentEmployee();
        LocalDate today = LocalDate.now();
        
        return attendanceRepository.findByEmployeeAndDate(employee, today)
                .map(this::convertToDTO);
    }

    /**
     * Get attendance records for the current employee
     */
    @Transactional(readOnly = true)
    public List<AttendanceRecordDTO> getCurrentEmployeeAttendance() {
        EmployeeEntity employee = getCurrentEmployee();
        
        return attendanceRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get attendance records for the current employee with pagination
     */
    @Transactional(readOnly = true)
    public Page<AttendanceRecordDTO> getCurrentEmployeeAttendance(Pageable pageable) {
        EmployeeEntity employee = getCurrentEmployee();
        
        return attendanceRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get attendance records for the current employee between two dates with pagination
     */
    @Transactional(readOnly = true)
    public Page<AttendanceRecordDTO> getCurrentEmployeeAttendanceBetweenDates(
            LocalDate startDate, LocalDate endDate, Pageable pageable) {
        EmployeeEntity employee = getCurrentEmployee();
        
        return attendanceRepository.findByEmployeeAndDateBetween(employee, startDate, endDate, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get attendance record by ID
     */
    @Transactional(readOnly = true)
    public Optional<AttendanceRecordDTO> getAttendanceById(String attendanceId) {
        return attendanceRepository.findById(attendanceId)
                .map(this::convertToDTO);
    }

    /**
     * Get attendance records for a specific employee with pagination (HR only)
     */
    @Transactional(readOnly = true)
    public Page<AttendanceRecordDTO> getEmployeeAttendance(String employeeId, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return attendanceRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get attendance records for a specific employee between two dates with pagination (HR only)
     */
    @Transactional(readOnly = true)
    public Page<AttendanceRecordDTO> getEmployeeAttendanceBetweenDates(
            String employeeId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        
        return attendanceRepository.findByEmployeeAndDateBetween(employee, startDate, endDate, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Approve an attendance record (HR only)
     */
    @Transactional
    public AttendanceRecordDTO approveAttendance(String attendanceId) {
        AttendanceRecordEntity record = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance record not found"));
        
        record.setApprovedByManager(true);
        AttendanceRecordEntity savedRecord = attendanceRepository.save(record);
        
        return convertToDTO(savedRecord);
    }

    /**
     * Update an attendance record status and remarks (HR only)
     */
    @Transactional
    public AttendanceRecordDTO updateAttendanceStatus(String attendanceId, String status, String remarks) {
        AttendanceRecordEntity record = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance record not found"));
        
        if (status != null && !status.isEmpty()) {
            record.setStatus(status);
        }
        
        if (remarks != null) {
            record.setRemarks(remarks);
        }
        
        AttendanceRecordEntity savedRecord = attendanceRepository.save(record);
        return convertToDTO(savedRecord);
    }

    /**
     * Convert AttendanceRecordEntity to AttendanceRecordDTO
     */
    private AttendanceRecordDTO convertToDTO(AttendanceRecordEntity entity) {
        if (entity == null) {
            return null;
        }
        
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setAttendanceId(entity.getAttendanceId());
        dto.setEmployeeId(entity.getEmployee().getEmployeeId());
        dto.setEmployeeName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName());
        dto.setDate(entity.getDate());
        dto.setClockInTime(entity.getClockInTime());
        dto.setClockOutTime(entity.getClockOutTime());
        dto.setTotalHours(entity.getTotalHours());
        dto.setStatus(entity.getStatus());
        dto.setRemarks(entity.getRemarks());
        dto.setOvertimeHours(entity.getOvertimeHours());
        dto.setReasonForAbsence(entity.getReasonForAbsence());
        dto.setApprovedByManager(entity.isApprovedByManager());
        
        return dto;
    }

    /**
     * Get the currently authenticated employee
     */
    private EmployeeEntity getCurrentEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        return employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Employee not found"));
    }
} 