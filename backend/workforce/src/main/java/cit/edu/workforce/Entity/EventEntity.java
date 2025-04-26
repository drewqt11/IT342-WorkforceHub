package cit.edu.workforce.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * EventEntity - Represents events like seminars, webinars, workshops, etc.
 * New file: This entity stores information about various events employees can attend
 */
@Entity
@Table(name = "event")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity {

    @Id
    @GeneratedValue(generator = "custom-event-id")
    @GenericGenerator(name = "custom-event-id", strategy = "cit.edu.workforce.Utils.EventIdGenerator")
    @Column(name = "event_id", updatable = false, nullable = false, length = 16)
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType; // Seminar, Webinar, Workshop, etc.

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location", nullable = false)
    private String location; // Online URL or physical venue

    @Column(name = "event_datetime", nullable = false)
    private LocalDateTime eventDatetime;

    @Column(name = "duration_hours", precision = 5, scale = 2, nullable = false)
    private BigDecimal durationHours;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // New relationship added: Event is created by a HR Admin (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserAccountEntity createdBy;

    // New relationship added: Event has many Training Enrollments
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingEnrollmentEntity> enrollments = new ArrayList<>();
} 