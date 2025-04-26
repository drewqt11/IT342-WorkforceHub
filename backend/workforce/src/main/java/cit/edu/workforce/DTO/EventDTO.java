package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * EventDTO - Data Transfer Object for event information
 * New file: This DTO is used to transfer event data between the service layer and API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    
    private String eventId;
    private String eventType;
    private String title;
    private String description;
    private String location;
    private LocalDateTime eventDatetime;
    private BigDecimal durationHours;
    private boolean isActive;
    private String createdById;
    private String createdByName;
    private int enrollmentCount;
} 