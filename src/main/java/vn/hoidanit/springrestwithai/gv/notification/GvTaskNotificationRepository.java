package vn.hoidanit.springrestwithai.gv.notification;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GvTaskNotificationRepository extends JpaRepository<GvTaskNotification, Long> {

    Page<GvTaskNotification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    List<GvTaskNotification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId);
}
