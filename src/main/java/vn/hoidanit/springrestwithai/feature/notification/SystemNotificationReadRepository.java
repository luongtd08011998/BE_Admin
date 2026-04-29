package vn.hoidanit.springrestwithai.feature.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.feature.notification.entity.SystemNotificationRead;

import java.util.List;

@Repository
public interface SystemNotificationReadRepository extends JpaRepository<SystemNotificationRead, Long> {

    List<SystemNotificationRead> findByCustomerId(Integer customerId);

    boolean existsBySystemNotificationIdAndCustomerId(Long systemNotificationId, Integer customerId);
}
