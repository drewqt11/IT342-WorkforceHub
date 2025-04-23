package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.AttendanceSummaryDTO;
import cit.edu.workforce.Entity.AttendanceEntity;
import cit.edu.workforce.Entity.AttendanceSummaryEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Repository.AttendanceRepository;
import cit.edu.workforce.Repository.AttendanceSummaryRepository;
import cit.edu.workforce.Repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceSummaryService {

    private final AttendanceSummaryRepository attendanceSummaryRepository;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public AttendanceSummaryService(
            AttendanceSummaryRepository attendanceSummaryRepository,
            AttendanceRepository attendanceRepository,
            EmployeeRepository employeeRepository) {
        this.attendanceSummaryRepository = attendanceSummaryRepository;
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Page<AttendanceSummaryDTO> getEmployeeAttendanceSummary(String employeeId, Pageable pageable) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

        return attendanceSummaryRepository.findByEmployee(employee, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<AttendanceSummaryDTO> getEmployeeAttendanceSummaryForDateRange(String employeeId, LocalDate startDate, LocalDate endDate) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

        return attendanceSummaryRepository.findByEmployeeAndDateBetween(employee, startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void generateDailySummary(LocalDate date) {
        // Get all employees
        List<EmployeeEntity> employees = employeeRepository.findAll();
        
        for (EmployeeEntity employee : employees) {
            // Check if summary already exists for this employee and date
            Optional<AttendanceSummaryEntity> existingSummary = 
                    attendanceSummaryRepository.findByEmployeeAndDate(employee, date);
            
            if (existingSummary.isPresent()) {
                continue; // Skip if already processed
            }
            
            // Get attendance records for this employee on this date
            LocalDateTime startDateTime = date.atStartOfDay();
            LocalDateTime endDateTime = date.atTime(LocalTime.MAX);
            
            List<AttendanceEntity> attendanceRecords = 
                    attendanceRepository.findByEmployeeAndClockInTimeBetween(employee, startDateTime, endDateTime);
            
            // Create summary
            AttendanceSummaryEntity summary = new AttendanceSummaryEntity();
            summary.setEmployee(employee);
            summary.setDate(date);
            
            if (attendanceRecords.isEmpty()) {
                // Employee was absent
                summary.setIsAbsent(true);
                summary.setHoursWorked(0.0);
                summary.setOvertimeHours(0.0);
                summary.setStatus("ABSENT");
            } else {
                // Calculate hours worked and overtime
                double totalHoursWorked = 0.0;
                double overtimeHours = 0.0;
                
                for (AttendanceEntity record : attendanceRecords) {
                    if (record.getClockOutTime() != null) {
                        Duration duration = Duration.between(record.getClockInTime(), record.getClockOutTime());
                        double hours = duration.toMinutes() / 60.0;
                        
                        totalHoursWorked += hours;
                        
                        // If this record is marked as overtime, count those hours
                        if (Boolean.TRUE.equals(record.getIsOvertime())) {
                            // Standard workday is 8 hours
                            double standardHours = 8.0;
                            if (hours > standardHours) {
                                overtimeHours += (hours - standardHours);
                            }
                        }
                    }
                }
                
                summary.setIsAbsent(false);
                summary.setHoursWorked(totalHoursWorked);
                summary.setOvertimeHours(overtimeHours);
                
                // Determine status based on hours worked
                if (totalHoursWorked >= 8.0) {
                    summary.setStatus("PRESENT");
                } else if (totalHoursWorked >= 4.0) {
                    summary.setStatus("HALF_DAY");
                } else if (totalHoursWorked > 0) {
                    summary.setStatus("PARTIAL");
                } else {
                    summary.setStatus("ABSENT");
                    summary.setIsAbsent(true);
                }
            }
            
            attendanceSummaryRepository.save(summary);
        }
    }

    @Transactional(readOnly = true)
    public Optional<AttendanceSummaryDTO> getSummaryById(String id) {
        return attendanceSummaryRepository.findById(id)
                .map(this::convertToDTO);
    }

    private AttendanceSummaryDTO convertToDTO(AttendanceSummaryEntity entity) {
        AttendanceSummaryDTO dto = new AttendanceSummaryDTO();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getEmployee().getEmployeeId());
        dto.setEmployeeName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName());
        dto.setDate(entity.getDate());
        dto.setHoursWorked(entity.getHoursWorked());
        dto.setOvertimeHours(entity.getOvertimeHours());
        dto.setIsAbsent(entity.getIsAbsent());
        dto.setStatus(entity.getStatus());
        dto.setNotes(entity.getNotes());
        return dto;
    }
} 