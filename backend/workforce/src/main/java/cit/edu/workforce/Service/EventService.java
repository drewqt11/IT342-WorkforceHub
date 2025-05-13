package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.EventDTO;
import cit.edu.workforce.Entity.EventEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.EventRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * EventService - Service for managing company events
 * Provides functionality for creating and managing company events
 */
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserAccountRepository userAccountRepository;
    
    private static final List<String> VALID_EVENT_TYPES = Arrays.asList("Seminar", "Webinar", "Workshop", "Meeting", "Other");

    /**
     * Create a new event
     * 
     * @param eventDTO The DTO containing event data
     * @return The created event DTO
     */
    @Transactional
    public EventDTO createEvent(EventDTO eventDTO) {
        // Validate input
        if (eventDTO.getTitle() == null || eventDTO.getTitle().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        
        if (eventDTO.getEventDatetime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event date and time are required");
        }
        
        if (eventDTO.getEventType() == null || eventDTO.getEventType().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event type is required");
        }
        
        if (!VALID_EVENT_TYPES.contains(eventDTO.getEventType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Invalid event type. Must be one of: " + String.join(", ", VALID_EVENT_TYPES));
        }
        
        // Get the current user as creator
        UserAccountEntity createdBy = getCurrentUser();
        
        // Create the event
        EventEntity event = new EventEntity();
        event.setEventType(eventDTO.getEventType());
        event.setTitle(eventDTO.getTitle());
        event.setDescription(eventDTO.getDescription());
        event.setLocation(eventDTO.getLocation());
        event.setEventDatetime(eventDTO.getEventDatetime());
        event.setDurationHours(eventDTO.getDurationHours());
        event.setActive(eventDTO.isActive());
        event.setCreatedBy(createdBy);
        
        EventEntity savedEvent = eventRepository.save(event);
        return convertToDTO(savedEvent);
    }

    /**
     * Get event by ID
     * 
     * @param eventId The event ID
     * @return The event DTO if found
     */
    public Optional<EventDTO> getEventById(String eventId) {
        return eventRepository.findById(eventId)
                .map(this::convertToDTO);
    }

    /**
     * Update an existing event
     * 
     * @param eventId The ID of the event to update
     * @param eventDTO The updated event data
     * @return The updated event DTO
     */
    @Transactional
    public EventDTO updateEvent(String eventId, EventDTO eventDTO) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        
        // Update fields if provided
        if (eventDTO.getEventType() != null && !eventDTO.getEventType().isEmpty()) {
            if (!VALID_EVENT_TYPES.contains(eventDTO.getEventType())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Invalid event type. Must be one of: " + String.join(", ", VALID_EVENT_TYPES));
            }
            event.setEventType(eventDTO.getEventType());
        }
        
        if (eventDTO.getTitle() != null && !eventDTO.getTitle().isEmpty()) {
            event.setTitle(eventDTO.getTitle());
        }
        
        if (eventDTO.getDescription() != null) {
            event.setDescription(eventDTO.getDescription());
        }
        
        if (eventDTO.getLocation() != null) {
            event.setLocation(eventDTO.getLocation());
        }
        
        if (eventDTO.getEventDatetime() != null) {
            event.setEventDatetime(eventDTO.getEventDatetime());
        }
        
        if (eventDTO.getDurationHours() != null) {
            event.setDurationHours(eventDTO.getDurationHours());
        }
        
        event.setActive(eventDTO.isActive());
        
        EventEntity updatedEvent = eventRepository.save(event);
        return convertToDTO(updatedEvent);
    }

    /**
     * Delete an event
     * 
     * @param eventId The ID of the event to delete
     */
    @Transactional
    public void deleteEvent(String eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
        
        eventRepository.deleteById(eventId);
    }

    /**
     * Get all events
     * 
     * @return List of event DTOs
     */
    public List<EventDTO> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated events
     */
    @Transactional(readOnly = true)
    public Page<EventDTO> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get all active events
     */
    @Transactional(readOnly = true)
    public List<EventDTO> getActiveEvents() {
        return eventRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated active events
     */
    @Transactional(readOnly = true)
    public Page<EventDTO> getActiveEvents(Pageable pageable) {
        return eventRepository.findByIsActiveTrue(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get all active and upcoming events
     */
    @Transactional(readOnly = true)
    public List<EventDTO> getActiveAndUpcomingEvents() {
        return eventRepository.findActiveAndUpcomingEvents().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search events by title
     */
    @Transactional(readOnly = true)
    public Page<EventDTO> searchEventsByTitle(String title, Pageable pageable) {
        return eventRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Search events by location
     */
    @Transactional(readOnly = true)
    public Page<EventDTO> searchEventsByLocation(String location, Pageable pageable) {
        return eventRepository.findByLocationContainingIgnoreCase(location, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get events by type
     */
    @Transactional(readOnly = true)
    public Page<EventDTO> getEventsByType(String eventType, Pageable pageable) {
        if (!VALID_EVENT_TYPES.contains(eventType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid event type. Must be one of: " + String.join(", ", VALID_EVENT_TYPES));
        }
        
        return eventRepository.findByEventType(eventType, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get events by date range
     */
    @Transactional(readOnly = true)
    public Page<EventDTO> getEventsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before end date");
        }
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        return eventRepository.findByEventDatetimeBetween(startDateTime, endDateTime, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get events happening today
     */
    @Transactional(readOnly = true)
    public List<EventDTO> getEventsHappeningToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        
        return eventRepository.findByEventDatetimeBetween(startOfDay, endOfDay).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map an EventEntity to an EventDTO
     */
    private EventDTO convertToDTO(EventEntity entity) {
        EventDTO dto = new EventDTO();
        dto.setEventId(entity.getEventId());
        dto.setEventType(entity.getEventType());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setLocation(entity.getLocation());
        dto.setEventDatetime(entity.getEventDatetime());
        dto.setDurationHours(entity.getDurationHours());
        dto.setActive(entity.isActive());
        
        if (entity.getCreatedBy() != null) {
            dto.setCreatedById(entity.getCreatedBy().getUserId());
            dto.setCreatedByName(entity.getCreatedBy().getEmailAddress());
        }
        
        return dto;
    }

    /**
     * Get the current user account from the security context
     */
    private UserAccountEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String currentUserName = authentication.getName();
        return userAccountRepository.findByEmailAddress(currentUserName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));
    }
} 