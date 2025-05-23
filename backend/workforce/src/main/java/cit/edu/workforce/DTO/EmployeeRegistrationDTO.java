package cit.edu.workforce.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRegistrationDTO {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private String gender;

    @Past(message = "Date of birth should be in the past")
    private LocalDate dateOfBirth;

    private String address;

    private String phoneNumber;

    private String maritalStatus;

    private LocalTime workTimeInSched;

    private LocalTime workTimeOutSched;
}