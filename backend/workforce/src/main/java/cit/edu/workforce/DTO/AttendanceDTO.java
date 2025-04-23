package cit.edu.workforce.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Attendance DTO for clock-in/out operations")
public class AttendanceDTO {

    private String id;
    
    private String employeeId;
    
    private String employeeName;

    @NotNull(message = "Clock-in time is required")
    private LocalDateTime clockInTime;

    private LocalDateTime clockOutTime;

    @NotNull(message = "Clock-in latitude is required")
    private Double clockInLatitude;

    @NotNull(message = "Clock-in longitude is required")
    private Double clockInLongitude;

    private Double clockOutLatitude;

    private Double clockOutLongitude;

    private String clockInStatus;

    private String clockOutStatus;

    private Boolean isWithinBoundary;

    private Double distanceFromOffice;
    
    private String notes;
    
    private Boolean isOvertime;
} 