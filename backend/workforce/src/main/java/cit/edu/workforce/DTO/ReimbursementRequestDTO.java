package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ReimbursementRequestDTO - Data Transfer Object for reimbursement request information
 * New file: This DTO is used to transfer reimbursement request data between the service layer and API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementRequestDTO {
    
    private String reimbursementId;
    private String employeeId;
    private String employeeName;
    private String planId;
    private String planName;
    private String planType;
    private LocalDate requestDate;
    private LocalDate expenseDate;
    private BigDecimal amountRequested;
    private String documentPath;
    private String reason;
    private String status;
    private String reviewedById;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private String remarks;
}