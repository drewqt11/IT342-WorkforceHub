package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.AttendanceDTO;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Attendance Management", description = "APIs for employee attendance tracking")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/employee/attendance")
    @Operation(summary = "Get my attendance", description = "Get the attendance records for the currently logged-in employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<AttendanceDTO>> getMyAttendance(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "clockInTime") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {
        
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return attendanceService.getCurrentEmployee()
                .map(employee -> ResponseEntity.ok(attendanceService.getEmployeeAttendance(employee.getEmployeeId(), pageable)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/hr/attendance/{employeeId}")
    @Operation(summary = "Get employee attendance", description = "Get attendance records for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<AttendanceDTO>> getEmployeeAttendance(
            @PathVariable String employeeId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "clockInTime") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {
        
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(attendanceService.getEmployeeAttendance(employeeId, pageable));
    }
    
    @GetMapping("/hr/attendance/{employeeId}/date-range")
    @Operation(summary = "Get employee attendance by date range", description = "Get attendance records for a specific employee within a date range")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<AttendanceDTO>> getEmployeeAttendanceByDateRange(
            @PathVariable String employeeId,
            @Parameter(description = "Start date and time") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date and time") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        return ResponseEntity.ok(attendanceService.getEmployeeAttendanceForDateRange(employeeId, startDate, endDate));
    }
    
    @PostMapping("/employee/attendance/clock-in")
    @Operation(summary = "Clock in", description = "Record employee clock-in with location")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<AttendanceDTO> clockIn(@Valid @RequestBody AttendanceDTO attendanceDTO) {
        try {
            return new ResponseEntity<>(attendanceService.clockIn(attendanceDTO), HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    
    @PutMapping("/employee/attendance/{id}/clock-out")
    @Operation(summary = "Clock out", description = "Record employee clock-out with location")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<AttendanceDTO> clockOut(
            @PathVariable String id,
            @Valid @RequestBody AttendanceDTO attendanceDTO) {
        try {
            return ResponseEntity.ok(attendanceService.clockOut(id, attendanceDTO));
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    
    @GetMapping("/employee/attendance/active")
    @Operation(summary = "Get active attendance", description = "Get the active attendance record (if any) for the currently logged-in employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<AttendanceDTO> getActiveAttendance() {
        return attendanceService.getActiveAttendance()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/hr/attendance/record/{id}")
    @Operation(summary = "Get attendance by ID", description = "Get a specific attendance record by ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<AttendanceDTO> getAttendanceById(@PathVariable String id) {
        return attendanceService.getAttendanceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
} 