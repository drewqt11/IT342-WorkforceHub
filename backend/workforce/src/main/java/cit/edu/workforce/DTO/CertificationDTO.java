package cit.edu.workforce.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificationDTO {
    private UUID certificateId;
    private String certificateName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    private String status;
    private UUID employeeId;
} 