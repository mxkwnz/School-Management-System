package school.repository;

import school.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(String userId);
    List<Notification> findByUserIdAndIsReadFalse(String userId);
    long countByUserIdAndIsReadFalse(String userId);
}

