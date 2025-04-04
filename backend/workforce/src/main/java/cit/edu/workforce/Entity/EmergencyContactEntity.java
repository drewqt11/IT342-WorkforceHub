package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "emergency_contact")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "contact_id", updatable = false, nullable = false)
    private UUID contactId;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(name = "relationship", nullable = false)
    private String relationship;

    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    @Column(name = "address")
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private EmployeeEntity employee;
} 