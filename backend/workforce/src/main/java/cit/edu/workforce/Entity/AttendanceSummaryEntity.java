package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "attendance_summary")
public class AttendanceSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Double hoursWorked;

    @Column
    private Double overtimeHours;

    @Column
    private Boolean isAbsent;

    @Column
    private String status; // "PRESENT", "ABSENT", "HALF_DAY", "ON_LEAVE", etc.

    @Column
    private String notes;
} 