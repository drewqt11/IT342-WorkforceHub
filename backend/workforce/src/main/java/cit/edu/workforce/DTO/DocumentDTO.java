package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private String documentId;
    private String documentType;
    private String fileName;
    private String status;
    private LocalDateTime uploadedAt;
    private LocalDateTime approvedAt;
    private String employeeId;
} 