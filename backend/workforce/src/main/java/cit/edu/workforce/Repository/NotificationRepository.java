package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<NotificationEntity> findByUserIdAndReadOrderByCreatedAtDesc(Long userId, boolean read);
    long countByUserIdAndRead(Long userId, boolean read);
} 