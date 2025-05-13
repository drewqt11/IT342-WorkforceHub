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
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AttendanceService - Service for managing attendance records
 * Updated file: Added time zone configuration for Asia/Manila
 */
@Service
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Manila");

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
    public AttendanceRecordDTO clockIn(ClockInRequestDTO request) {
        // Get current employee
        EmployeeEntity employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        // Check if employee has already clocked in today
        LocalDate today = LocalDate.now(ZONE_ID);
        Optional<AttendanceRecordEntity> existingRecord = attendanceRepository.findByEmployeeAndDate(employee, today);
        if (existingRecord.isPresent() && existingRecord.get().getClockInTime() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already clocked in today");
        }

        // Create new attendance record
        AttendanceRecordEntity record = existingRecord.orElse(new AttendanceRecordEntity());
        if (record.getAttendanceId() == null) {
            record.setEmployee(employee);
            record.setDate(today);
        }

        // Set clock in time and status - Ensure military time format
        LocalTime currentTime = LocalTime.now(ZONE_ID).truncatedTo(ChronoUnit.SECONDS);
        // Validate time format
        if (currentTime.getHour() < 0 || currentTime.getHour() > 23 || 
            currentTime.getMinute() < 0 || currentTime.getMinute() > 59 ||
            currentTime.getSecond() < 0 || currentTime.getSecond() > 59) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid time format");
        }
        record.setClockInTime(currentTime);
        record.setStatus("CLOCKED_IN");
        
        // Calculate tardiness minutes if scheduled time is set
        if (employee.getWorkTimeInSched() != null) {
            // If current time is after scheduled time, calculate tardiness
            if (currentTime.isAfter(employee.getWorkTimeInSched())) {
                long tardiness = ChronoUnit.MINUTES.between(employee.getWorkTimeInSched(), currentTime);
                record.setTardinessMinutes((int) tardiness);
            } else {
                record.setTardinessMinutes(0);
            }
        }
        
        // Set remarks if provided
        if (request.getRemarks() != null && !request.getRemarks().isEmpty()) {
            record.setRemarks(request.getRemarks());
        }

        // Save the record
        AttendanceRecordEntity savedRecord = attendanceRepository.save(record);
        return convertToDTO(savedRecord);
    }

    /**
     * Clock out an employee
     */
    @Transactional
    public AttendanceRecordDTO clockOut(ClockInRequestDTO clockOutRequest) {
        EmployeeEntity employee = getCurrentEmployee();
        LocalDate today = LocalDate.now(ZONE_ID);

        // Find today's attendance record
        AttendanceRecordEntity record = attendanceRepository.findByEmployeeAndDate(employee, today)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No clock-in record found for today"));

        if (record.getClockInTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must clock in before clocking out");
        }
        
        if (record.getClockOutTime() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already clocked out today");
        }

        // Update record with clock out time - Ensure military time format
        LocalTime currentTime = LocalTime.now(ZONE_ID).truncatedTo(ChronoUnit.SECONDS);
        // Validate time format
        if (currentTime.getHour() < 0 || currentTime.getHour() > 23 || 
            currentTime.getMinute() < 0 || currentTime.getMinute() > 59 ||
            currentTime.getSecond() < 0 || currentTime.getSecond() > 59) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid time format");
        }
        record.setClockOutTime(currentTime);
        
        // Calculate total hours
        Duration duration = Duration.between(record.getClockInTime(), currentTime);
        double totalMinutes = duration.toMinutes();
        
        // Deduct 1 hour (60 minutes) for break time
        double hours = (totalMinutes - 60) / 60.0;
        BigDecimal totalHours = BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP);
        if (totalHours.compareTo(BigDecimal.ZERO) > 0) {
            record.setTotalHours(totalHours);
        }else{
            record.setTotalHours(BigDecimal.ZERO);
        }
        
        // Set overtime to zero for now
        record.setOvertimeHours(BigDecimal.ZERO);

        // Calculate undertime minutes if scheduled time is set
        if (employee.getWorkTimeOutSched() != null) {
            // If clock out time is before scheduled time, calculate undertime
            if (currentTime.isBefore(employee.getWorkTimeOutSched())) {
                // Calculate minutes from clock out time to scheduled end time
                long undertime = ChronoUnit.MINUTES.between(currentTime, employee.getWorkTimeOutSched());
                // Only set undertime if it's a positive value
                if (undertime > 0) {
                    record.setUndertimeMinutes((int) undertime);
                } else {
                    record.setUndertimeMinutes(0);
                }
            } else {
                record.setUndertimeMinutes(0);
            }
        } else {
            record.setUndertimeMinutes(0);
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
        LocalDate today = LocalDate.now(ZONE_ID);
        
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
     * Update clock-out time and related fields for an existing attendance record
     * Only updates the clock-out time, status, and total hours
     */
    @Transactional
    public AttendanceRecordDTO updateClockOut(String attendanceId, ClockInRequestDTO clockOutRequest) {
        AttendanceRecordEntity record = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance record not found"));

        if (record.getClockInTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot clock out without clocking in first");
        }

        if (record.getClockOutTime() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already clocked out today");
        }

        // Update clock out time - Ensure military time format
        LocalTime currentTime = LocalTime.now(ZONE_ID).truncatedTo(ChronoUnit.SECONDS);
        // Validate time format
        if (currentTime.getHour() < 0 || currentTime.getHour() > 23 || 
            currentTime.getMinute() < 0 || currentTime.getMinute() > 59 ||
            currentTime.getSecond() < 0 || currentTime.getSecond() > 59) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid time format");
        }
        record.setClockOutTime(currentTime);
        
        // Calculate total hours
        Duration duration = Duration.between(record.getClockInTime(), currentTime);
        double totalMinutes = duration.toMinutes();
        
        // Deduct 1 hour (60 minutes) for break time
        double hours = (totalMinutes - 60) / 60.0;
        BigDecimal totalHours = BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP);
        if (totalHours.compareTo(BigDecimal.ZERO) > 0) {
            record.setTotalHours(totalHours);
        }else{
            record.setTotalHours(BigDecimal.ZERO);
        }
        
        // Set overtime to zero for now
        record.setOvertimeHours(BigDecimal.ZERO);

        // Calculate undertime minutes if scheduled time is set
        EmployeeEntity employee = record.getEmployee();
        if (employee.getWorkTimeOutSched() != null) {
            // If clock out time is before scheduled time, calculate undertime
            if (currentTime.isBefore(employee.getWorkTimeOutSched())) {
                // Calculate minutes from clock out time to scheduled end time
                long undertime = ChronoUnit.MINUTES.between(currentTime, employee.getWorkTimeOutSched());
                // Only set undertime if it's a positive value
                if (undertime > 0) {
                    record.setUndertimeMinutes((int) undertime);
                } else {
                    record.setUndertimeMinutes(0);
                }
            } else {
                record.setUndertimeMinutes(0);
            }
        } else {
            record.setUndertimeMinutes(0);
        }
        
        // Update status
        record.setStatus("CLOCKED_OUT");
        record.setRemarks("PRESENT");
        
        // Update remarks if provided
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
     * Get all attendance records with pagination (HR only)
     */
    @Transactional(readOnly = true)
    public Page<AttendanceRecordDTO> getAllAttendanceRecords(Pageable pageable) {
        return attendanceRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Update overtime hours for a specific attendance record
     */
    @Transactional
    public AttendanceRecordDTO updateOvertimeHours(String attendanceId, BigDecimal overtimeHours) {
        AttendanceRecordEntity record = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance record not found"));

        if (overtimeHours.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Overtime hours cannot be negative");
        }

        record.setOvertimeHours(overtimeHours);
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
        dto.setTardinessMinutes(entity.getTardinessMinutes());
        dto.setUndertimeMinutes(entity.getUndertimeMinutes());
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