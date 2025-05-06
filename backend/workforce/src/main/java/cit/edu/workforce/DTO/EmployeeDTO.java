package cit.edu.workforce.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private String employeeId;

    private String idNumber;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private String gender;

    private LocalDate hireDate;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private String address;

    private String phoneNumber;

    private String maritalStatus;

    private Boolean status = false;

    private String employmentStatus = "PENDING";

    private String departmentId;

    private String departmentName;

    private String jobId;

    private String jobName;

    private String roleId;

    private String roleName;

    private LocalDateTime createdAt;

    private LocalTime workTimeInSched;

    private LocalTime workTimeOutSched;

    // User account fields
    private String userId;
    private LocalDateTime lastLogin;
    private Boolean isActive;
}