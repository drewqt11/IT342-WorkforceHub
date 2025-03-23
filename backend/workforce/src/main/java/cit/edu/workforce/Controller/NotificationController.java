package cit.edu.workforce.Controller;

import cit.edu.workforce.DTO.ApiResponseDTO;
import cit.edu.workforce.DTO.NotificationDTO;
import cit.edu.workforce.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping
    @PreAuthorize("hasRole('HR_STAFF') or hasRole('ADMIN') or @securityService.isUserOwner(authentication, #userId)")
    public ResponseEntity<?> getNotifications(@RequestParam Long userId) {
        try {
            List<NotificationDTO> notifications = notificationService.getNotificationsByUserId(userId);
            return ResponseEntity.ok(ApiResponseDTO.success(notifications));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
    
    @GetMapping("/unread")
    @PreAuthorize("hasRole('HR_STAFF') or hasRole('ADMIN') or @securityService.isUserOwner(authentication, #userId)")
    public ResponseEntity<?> getUnreadNotifications(@RequestParam Long userId) {
        try {
            List<NotificationDTO> notifications = notificationService.getUnreadNotificationsByUserId(userId);
            return ResponseEntity.ok(ApiResponseDTO.success(notifications));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
    
    @GetMapping("/count")
    @PreAuthorize("hasRole('HR_STAFF') or hasRole('ADMIN') or @securityService.isUserOwner(authentication, #userId)")
    public ResponseEntity<?> getUnreadNotificationCount(@RequestParam Long userId) {
        try {
            long count = notificationService.countUnreadNotifications(userId);
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(ApiResponseDTO.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('HR_STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> createNotification(
            @RequestParam Long userId,
            @RequestParam String message,
            @RequestParam String type) {
        try {
            NotificationDTO notification = notificationService.createNotification(userId, message, type);
            return ResponseEntity.ok(ApiResponseDTO.success("Notification sent successfully", notification));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('HR_STAFF') or hasRole('ADMIN') or @securityService.isNotificationOwner(authentication, #id)")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long id) {
        try {
            NotificationDTO notification = notificationService.markNotificationAsRead(id);
            return ResponseEntity.ok(ApiResponseDTO.success("Notification marked as read", notification));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }
} 