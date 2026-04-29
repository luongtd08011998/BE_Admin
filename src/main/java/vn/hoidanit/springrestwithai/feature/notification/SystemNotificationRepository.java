package vn.hoidanit.springrestwithai.feature.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.feature.notification.entity.SystemNotification;

@Repository
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {
}
