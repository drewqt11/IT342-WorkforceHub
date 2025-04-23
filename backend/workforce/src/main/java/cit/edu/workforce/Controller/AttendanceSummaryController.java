package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.AttendanceSummaryDTO;
import cit.edu.workforce.Service.AttendanceSummaryService;
import cit.edu.workforce.Service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Attendance Summary", description = "APIs for attendance summary and payroll integration")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceSummaryController {

    private final AttendanceSummaryService attendanceSummaryService;
    private final EmployeeService employeeService;

    @Autowired
    public AttendanceSummaryController(
            AttendanceSummaryService attendanceSummaryService,
            EmployeeService employeeService) {
        this.attendanceSummaryService = attendanceSummaryService;
        this.employeeService = employeeService;
    }

    @GetMapping("/employee/attendance-summary")
    @Operation(summary = "Get my attendance summary", description = "Get attendance summary for the currently logged-in employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<AttendanceSummaryDTO>> getMyAttendanceSummary(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {
        
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return employeeService.getCurrentEmployee()
                .map(employee -> ResponseEntity.ok(attendanceSummaryService.getEmployeeAttendanceSummary(employee.getEmployeeId(), pageable)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/hr/attendance-summary/{employeeId}")
    @Operation(summary = "Get employee attendance summary", description = "Get attendance summary for a specific employee")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<AttendanceSummaryDTO>> getEmployeeAttendanceSummary(
            @PathVariable String employeeId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {
        
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(attendanceSummaryService.getEmployeeAttendanceSummary(employeeId, pageable));
    }

    @GetMapping("/hr/attendance-summary/{employeeId}/date-range")
    @Operation(summary = "Get employee attendance summary by date range", 
            description = "Get attendance summary for a specific employee within a date range")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<List<AttendanceSummaryDTO>> getEmployeeAttendanceSummaryByDateRange(
            @PathVariable String employeeId,
            @Parameter(description = "Start date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        return ResponseEntity.ok(attendanceSummaryService.getEmployeeAttendanceSummaryForDateRange(
                employeeId, startDate, endDate));
    }

    @PostMapping("/hr/attendance-summary/generate")
    @Operation(summary = "Generate daily attendance summary", 
            description = "Generate attendance summaries for all employees for a specific date")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<Void> generateDailySummary(
            @Parameter(description = "Date to generate summary for") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        attendanceSummaryService.generateDailySummary(date);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hr/attendance-summary/record/{id}")
    @Operation(summary = "Get attendance summary by ID", description = "Get a specific attendance summary record by ID")
    @PreAuthorize("hasAnyRole('ROLE_HR', 'ROLE_ADMIN')")
    public ResponseEntity<AttendanceSummaryDTO> getAttendanceSummaryById(@PathVariable String id) {
        return attendanceSummaryService.getSummaryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
} 