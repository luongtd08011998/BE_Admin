package vn.hoidanit.springrestwithai.gv.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GvTaskReminderLogRepository extends JpaRepository<GvTaskReminderLog, Long> {
    boolean existsByTaskIdAndRecipientIdAndReminderType(Long taskId, Long recipientId, String reminderType);
}
