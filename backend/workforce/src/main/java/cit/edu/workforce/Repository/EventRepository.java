package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EventEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * EventRepository - Repository for managing events
 * New file: Provides methods to access event data
 */
@Repository
public interface EventRepository extends JpaRepository<EventEntity, String> {
    
    /**
     * Find events with a specific title (case-insensitive partial match)
     */
    List<EventEntity> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Find paginated events with a specific title (case-insensitive partial match)
     */
    Page<EventEntity> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    /**
     * Find events by type
     */
    List<EventEntity> findByEventType(String eventType);
    
    /**
     * Find paginated events by type
     */
    Page<EventEntity> findByEventType(String eventType, Pageable pageable);
    
    /**
     * Find active events
     */
    List<EventEntity> findByIsActiveTrue();
    
    /**
     * Find paginated active events
     */
    Page<EventEntity> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Find events by created user
     */
    List<EventEntity> findByCreatedBy(UserAccountEntity createdBy);
    
    /**
     * Find paginated events by created user
     */
    Page<EventEntity> findByCreatedBy(UserAccountEntity createdBy, Pageable pageable);
    
    /**
     * Find events scheduled within a date-time range
     */
    List<EventEntity> findByEventDatetimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
    
    /**
     * Find paginated events scheduled within a date-time range
     */
    Page<EventEntity> findByEventDatetimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);
    
    /**
     * Find events by location (case-insensitive partial match)
     */
    List<EventEntity> findByLocationContainingIgnoreCase(String location);
    
    /**
     * Find paginated events by location (case-insensitive partial match)
     */
    Page<EventEntity> findByLocationContainingIgnoreCase(String location, Pageable pageable);
    
    /**
     * Find events that are active and scheduled in the future
     */
    @Query("SELECT e FROM EventEntity e WHERE e.isActive = true AND e.eventDatetime >= CURRENT_TIMESTAMP")
    List<EventEntity> findActiveAndUpcomingEvents();
    
    /**
     * Find paginated events that are active and scheduled in the future
     */
    @Query("SELECT e FROM EventEntity e WHERE e.isActive = true AND e.eventDatetime >= CURRENT_TIMESTAMP")
    Page<EventEntity> findActiveAndUpcomingEvents(Pageable pageable);
    
    /**
     * Find events happening today
     */
    @Query("SELECT e FROM EventEntity e WHERE FUNCTION('DATE', e.eventDatetime) = CURRENT_DATE")
    List<EventEntity> findEventsHappeningToday();
} 