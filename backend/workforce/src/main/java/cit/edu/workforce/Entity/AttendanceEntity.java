package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "attendance")
public class AttendanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    @Column(nullable = false)
    private LocalDateTime clockInTime;

    @Column
    private LocalDateTime clockOutTime;

    @Column(nullable = false)
    private Double clockInLatitude;

    @Column(nullable = false)
    private Double clockInLongitude;

    @Column
    private Double clockOutLatitude;

    @Column
    private Double clockOutLongitude;

    @Column
    private String clockInStatus; // "VALID", "INVALID_LOCATION", etc.

    @Column
    private String clockOutStatus;

    @Column
    private Boolean isWithinBoundary;

    @Column
    private Double distanceFromOffice; // in meters
    
    @Column
    private String notes;
    
    @Column
    private Boolean isOvertime = false;
} 