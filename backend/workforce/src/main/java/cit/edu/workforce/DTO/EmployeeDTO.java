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
public class EmployeeDTO {
    private UUID employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    
    private String address;
    private String phone;
    private String status;
    
    private UUID userId;
    private String roleId;
    private String roleName;
    private UUID jobId;
    private String jobName;
    private UUID departmentId;
    private String departmentName;
} 