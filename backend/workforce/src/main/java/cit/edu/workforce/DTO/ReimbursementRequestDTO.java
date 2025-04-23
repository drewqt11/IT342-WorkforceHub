package cit.edu.workforce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementRequestDTO {
    
    private String requestId;
    private String employeeId;
    private String employeeName;
    private LocalDate requestDate;
    private LocalDate expenseDate;
    private String expenseType;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String receiptUrl;
    private String status;
    private LocalDate approvalDate;
    private String approverId;
    private String approverName;
    private String approverComments;
    private LocalDate paymentDate;
    private String paymentReference;
}

// New file: DTO for reimbursement requests 