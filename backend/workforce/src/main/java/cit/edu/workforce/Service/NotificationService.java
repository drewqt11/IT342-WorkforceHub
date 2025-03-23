package cit.edu.workforce.Service;

import cit.edu.workforce.DTO.NotificationDTO;
import cit.edu.workforce.Entity.NotificationEntity;
import cit.edu.workforce.Entity.UserEntity;
import cit.edu.workforce.Repository.NotificationRepository;
import cit.edu.workforce.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }
    
    @Transactional
    public NotificationDTO createNotification(Long userId, String message, String type) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        NotificationEntity notification = new NotificationEntity();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        
        NotificationEntity savedNotification = notificationRepository.save(notification);
        return convertToDTO(savedNotification);
    }
    
    public List<NotificationDTO> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<NotificationDTO> getUnreadNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public NotificationDTO markNotificationAsRead(Long id) {
        NotificationEntity notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        
        NotificationEntity updatedNotification = notificationRepository.save(notification);
        return convertToDTO(updatedNotification);
    }
    
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }
    
    private NotificationDTO convertToDTO(NotificationEntity entity) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId());
        dto.setMessage(entity.getMessage());
        dto.setType(entity.getType());
        dto.setRead(entity.isRead());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setReadAt(entity.getReadAt());
        return dto;
    }
} 