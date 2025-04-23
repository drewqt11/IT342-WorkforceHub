package cit.edu.workforce.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Attendance Summary DTO")
public class AttendanceSummaryDTO {

    private String id;
    
    private String employeeId;
    
    private String employeeName;

    private LocalDate date;

    private Double hoursWorked;

    private Double overtimeHours;

    private Boolean isAbsent;

    private String status;

    private String notes;
} 