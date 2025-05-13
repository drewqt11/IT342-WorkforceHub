package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.EventDTO;
import cit.edu.workforce.Service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * EventController - Provides API endpoints for event management
 * This controller handles all event-related operations including
 * creating, reading, updating, and deleting events.
 */
@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Management", description = "Event management APIs")
@SecurityRequirement(name = "bearerAuth")
public class EventController {
    
    @Autowired
    private EventService eventService;
    
    @Operation(summary = "Create a new event", description = "Creates a new event. Requires HR or Admin role.")
    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventDTO eventDTO) {
        EventDTO createdEvent = eventService.createEvent(eventDTO);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Get all events", description = "Returns a list of all events")
    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents() {
        List<EventDTO> events = eventService.getAllEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }
    
    @Operation(summary = "Get paginated events", description = "Returns a paginated list of events")
    @GetMapping("/paged")
    public ResponseEntity<Page<EventDTO>> getPaginatedEvents(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "eventDatetime") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<EventDTO> eventsPage = eventService.getAllEvents(pageable);
        
        return new ResponseEntity<>(eventsPage, HttpStatus.OK);
    }
    
    @Operation(summary = "Get event by ID", description = "Returns an event by its ID")
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDTO> getEventById(
            @Parameter(description = "Event ID") @PathVariable String eventId) {
        
        Optional<EventDTO> event = eventService.getEventById(eventId);
        
        return event.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @Operation(summary = "Update an event", description = "Updates an existing event. Requires HR or Admin role.")
    @PutMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<EventDTO> updateEvent(
            @Parameter(description = "Event ID") @PathVariable String eventId,
            @Valid @RequestBody EventDTO eventDTO) {
        
        EventDTO updatedEvent = eventService.updateEvent(eventId, eventDTO);
        return new ResponseEntity<>(updatedEvent, HttpStatus.OK);
    }
    
    @Operation(summary = "Delete an event", description = "Deletes an event. Requires HR or Admin role.")
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "Event ID") @PathVariable String eventId) {
        
        eventService.deleteEvent(eventId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @Operation(summary = "Get active events", description = "Returns a list of active events")
    @GetMapping("/active")
    public ResponseEntity<List<EventDTO>> getActiveEvents() {
        List<EventDTO> events = eventService.getActiveEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }
    
    @Operation(summary = "Get paginated active events", description = "Returns a paginated list of active events")
    @GetMapping("/active/paged")
    public ResponseEntity<Page<EventDTO>> getPaginatedActiveEvents(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "eventDatetime") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<EventDTO> eventsPage = eventService.getActiveEvents(pageable);
        
        return new ResponseEntity<>(eventsPage, HttpStatus.OK);
    }
    
    @Operation(summary = "Get active and upcoming events", 
            description = "Returns a list of active events that are scheduled in the future")
    @GetMapping("/active-and-upcoming")
    public ResponseEntity<List<EventDTO>> getActiveAndUpcomingEvents() {
        List<EventDTO> events = eventService.getActiveAndUpcomingEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }
    
    @Operation(summary = "Search events by title", description = "Returns events matching the title search query")
    @GetMapping("/search/title")
    public ResponseEntity<Page<EventDTO>> searchEventsByTitle(
            @Parameter(description = "Title search query") @RequestParam String title,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<EventDTO> results = eventService.searchEventsByTitle(title, pageable);
        
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
    
    @Operation(summary = "Search events by location", description = "Returns events matching the location search query")
    @GetMapping("/search/location")
    public ResponseEntity<Page<EventDTO>> searchEventsByLocation(
            @Parameter(description = "Location search query") @RequestParam String location,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<EventDTO> results = eventService.searchEventsByLocation(location, pageable);
        
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
    
    @Operation(summary = "Get events by type", description = "Returns events with the specified event type")
    @GetMapping("/type/{eventType}")
    public ResponseEntity<Page<EventDTO>> getEventsByType(
            @Parameter(description = "Event type") @PathVariable String eventType,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<EventDTO> results = eventService.getEventsByType(eventType, pageable);
        
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
    
    @Operation(summary = "Get events by date range", 
            description = "Returns events scheduled within the specified date range")
    @GetMapping("/date-range")
    public ResponseEntity<Page<EventDTO>> getEventsByDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<EventDTO> results = eventService.getEventsByDateRange(startDate, endDate, pageable);
        
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
    
    @Operation(summary = "Get events happening today", description = "Returns events scheduled for today")
    @GetMapping("/today")
    public ResponseEntity<List<EventDTO>> getEventsHappeningToday() {
        List<EventDTO> events = eventService.getEventsHappeningToday();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }
} 