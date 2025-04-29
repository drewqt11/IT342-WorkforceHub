package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClockInRequestDTO - DTO for handling clock-in requests
 * Updated file: Removed location fields to follow the ERD
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClockInRequestDTO {
    private String remarks;
} 