package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEntity {

    @Id
    @GeneratedValue(generator = "custom-employee-id")
    @GenericGenerator(name = "custom-employee-id", strategy = "cit.edu.workforce.Utils.EmployeeIdGenerator")
    @Column(name = "employee_id", updatable = false, nullable = false, length = 16)
    private String employeeId;

    @Column(name = "id_number", unique = true)
    private String idNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "gender")
    private String gender;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "marital_status")
    private String maritalStatus;

    @Column(name = "status", nullable = false)
    private Boolean status = false;

    @Column(name = "employment_status", nullable = false)
    private String employmentStatus = "FULL_TIME";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "work_time_in_sched")
    @Temporal(TemporalType.TIME)
    private LocalTime workTimeInSched;

    @Column(name = "work_time_out_sched")
    @Temporal(TemporalType.TIME)
    private LocalTime workTimeOutSched;

    // New relationship added: Employee belongs to a Department
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    // New relationship added: Employee has a JobTitle
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id")
    private JobTitleEntity jobTitle;

    // New relationship added: Employee has a Role
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private RoleEntity role;

    // New relationship added: Employee has a UserAccount
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccountEntity userAccount;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}