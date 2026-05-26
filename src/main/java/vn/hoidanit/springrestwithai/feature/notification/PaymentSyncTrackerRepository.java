package vn.hoidanit.springrestwithai.feature.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.feature.notification.entity.PaymentSyncTracker;

@Repository
public interface PaymentSyncTrackerRepository extends JpaRepository<PaymentSyncTracker, Long> {

    @Modifying
    @Query("UPDATE PaymentSyncTracker t SET t.lastProcessedPaymentLineId = :newId, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = 1 AND t.lastProcessedPaymentLineId < :newId")
    int atomicUpdateLastProcessedId(@Param("newId") Integer newId);

    @Modifying
    @Query(value = "INSERT IGNORE INTO payment_sync_tracker (id, last_processed_payment_line_id, updated_at) VALUES (1, :maxId, NOW())", nativeQuery = true)
    void insertIfNotExists(@Param("maxId") Integer maxId);
}
