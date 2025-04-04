package cit.edu.workforce.DTO;

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
    private LocalDate hireDate;
    private LocalDate dateOfBirth;
    private String address;
    private String phoneNumber;
    private String maritalStatus;
    private String status;
    private UUID departmentId;
    private String departmentName;
    private UUID jobId;
    private String jobName;
    private String roleId;
    private String roleName;
} 