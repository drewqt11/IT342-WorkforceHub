package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.AttendanceRecordDTO;
import cit.edu.workforce.DTO.ClockInRequestDTO;
import cit.edu.workforce.Entity.AttendanceRecordEntity;
import cit.edu.workforce.Service.AttendanceService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * AttendanceController - Provides API endpoints for attendance management
 * New file: Provides API endpoints for attendance clock-in/out and management
 * This controller handles all attendance-related operations including:
 * - Clock-in and clock-out for employees
 * - Retrieving attendance records
 * - Managing attendance approvals and statistics
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Attendance Management", description = "API for managing employee attendance records")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Clock in for an employee
     * Records the start time of an employee's work day
     */
    @PostMapping("/employee/attendance/clock-in")
    @Operation(summary = "Clock in", description = "Record employee clock-in time for today")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<AttendanceRecordDTO> clockIn(@Valid @RequestBody ClockInRequestDTO clockInRequest) {
        return new ResponseEntity<>(attendanceService.clockIn(clockInRequest), HttpStatus.CREATED);
    }

    /**
     * Clock out for an employee
     * Records the end time of an employee's work day and calculates hours worked
     */
    @PostMapping("/employee/attendance/clock-out")
    @Operation(summary = "Clock out", description = "Record employee clock-out time for today")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<AttendanceRecordDTO> clockOut(@Valid @RequestBody ClockInRequestDTO clockOutRequest) {
        return ResponseEntity.ok(attendanceService.clockOut(clockOutRequest));
    }

    /**
     * Update clock-out time and related fields
     * Updates only the clock-out time, status, and total hours for an existing attendance record
     */
    @PutMapping("/employee/attendance/{id}/clock-out")
    @Operation(summary = "Update clock-out time", description = "Update only the clock-out time, status, and total hours for an existing attendance record")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN') or @attendanceService.isOwnAttendance(#id)")
    public ResponseEntity<AttendanceRecordDTO> updateClockOut(
            @PathVariable String id,
            @Valid @RequestBody ClockInRequestDTO clockOutRequest) {
        return ResponseEntity.ok(attendanceService.updateClockOut(id, clockOutRequest));
    }

    /**
     * Get today's attendance record for the current employee
     */
    @GetMapping("/employee/attendance/today")
    @Operation(summary = "Get today's attendance", description = "Get the current employee's attendance record for today")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<AttendanceRecordDTO> getTodayAttendance() {
        return attendanceService.getTodayAttendance()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get attendance record by ID
     */
    @GetMapping("/employee/attendance/{id}")
    @Operation(summary = "Get attendance by ID", description = "Get a specific attendance record by its ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN') or @attendanceService.isOwnAttendance(#id)")
    public ResponseEntity<AttendanceRecordDTO> getAttendanceById(@PathVariable String id) {
        return attendanceService.getAttendanceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get attendance records for the current employee
     */
    @GetMapping("/employee/attendance")
    @Operation(summary = "Get my attendance records", description = "Get all attendance records for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<AttendanceRecordDTO>> getMyAttendanceRecords() {
        return ResponseEntity.ok(attendanceService.getCurrentEmployeeAttendance());
    }

    /**
     * Get attendance records for the current employee with pagination
     */
    @GetMapping("/employee/attendance/paged")
    @Operation(summary = "Get my attendance records (paged)", description = "Get paginated attendance records for the current employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<AttendanceRecordDTO>> getMyAttendanceRecordsPaged(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(attendanceService.getCurrentEmployeeAttendance(pageable));
    }

    /**
     * Get attendance records for the current employee within a date range
     */
    @GetMapping("/employee/attendance/date-range")
    @Operation(summary = "Get my attendance records by date range", description = "Get attendance records for the current employee within a date range")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<AttendanceRecordDTO>> getMyAttendanceRecordsByDateRange(
            @Parameter(description = "Start date (inclusive)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(attendanceService.getCurrentEmployeeAttendanceBetweenDates(startDate, endDate, pageable));
    }

    /**
     * Get attendance records for a specific employee with pagination
     * Admin/HR only endpoint
     */
    @GetMapping("/hr/attendance/employee/{employeeId}")
    @Operation(summary = "Get employee attendance records", description = "Get paginated attendance records for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<AttendanceRecordDTO>> getEmployeeAttendanceRecords(
            @Parameter(description = "Employee ID") @PathVariable String employeeId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(attendanceService.getEmployeeAttendance(employeeId, pageable));
    }

    /**
     * Get attendance records for a specific employee within a date range
     * Admin/HR only endpoint
     */
    @GetMapping("/hr/attendance/employee/{employeeId}/date-range")
    @Operation(summary = "Get employee attendance records by date range", description = "Get attendance records for a specific employee within a date range")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<AttendanceRecordDTO>> getEmployeeAttendanceRecordsByDateRange(
            @Parameter(description = "Employee ID") @PathVariable String employeeId,
            @Parameter(description = "Start date (inclusive)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(attendanceService.getEmployeeAttendanceBetweenDates(employeeId, startDate, endDate, pageable));
    }

    /**
     * Approve an attendance record
     * Admin/HR only endpoint
     */
    @PatchMapping("/hr/attendance/{id}/approve")
    @Operation(summary = "Approve attendance", description = "Approve an attendance record")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<AttendanceRecordDTO> approveAttendance(@PathVariable String id) {
        return ResponseEntity.ok(attendanceService.approveAttendance(id));
    }

    /**
     * Update attendance status and remarks
     * Admin/HR only endpoint
     */
    @PatchMapping("/hr/attendance/{id}/status")
    @Operation(summary = "Update attendance status", description = "Update the status and remarks of an attendance record")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<AttendanceRecordDTO> updateAttendanceStatus(
            @PathVariable String id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String remarks) {

        return ResponseEntity.ok(attendanceService.updateAttendanceStatus(id, status, remarks));
    }

    /**
     * Get all attendance records with pagination
     * Admin/HR only endpoint
     */
    @GetMapping("/hr/attendance/all")
    @Operation(summary = "Get all attendance records", description = "Get all attendance records with pagination")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<AttendanceRecordDTO>> getAllAttendanceRecords(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(attendanceService.getAllAttendanceRecords(pageable));
    }

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
} 