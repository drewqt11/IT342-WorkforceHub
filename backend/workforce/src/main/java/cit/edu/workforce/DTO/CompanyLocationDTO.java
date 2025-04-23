package cit.edu.workforce.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Company Location DTO")
public class CompanyLocationDTO {

    private String id;

    @NotBlank(message = "Location name is required")
    private String locationName;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotNull(message = "Allowed radius is required")
    @Positive(message = "Allowed radius must be positive")
    private Double allowedRadius;

    private String notes;

    private Boolean isActive;
} 