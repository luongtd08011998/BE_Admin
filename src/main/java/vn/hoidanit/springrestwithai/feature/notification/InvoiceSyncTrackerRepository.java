package vn.hoidanit.springrestwithai.feature.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.feature.notification.entity.InvoiceSyncTracker;

import java.util.Optional;

@Repository
public interface InvoiceSyncTrackerRepository extends JpaRepository<InvoiceSyncTracker, Long> {

    Optional<InvoiceSyncTracker> findByDatePrefix(String datePrefix);

    @Modifying
    @Query("UPDATE InvoiceSyncTracker t SET t.lastProcessedInvoiceId = :newId, t.updatedAt = CURRENT_TIMESTAMP WHERE t.datePrefix = :datePrefix AND t.lastProcessedInvoiceId < :newId")
    int atomicUpdateLastProcessedId(@Param("datePrefix") String datePrefix, @Param("newId") Integer newId);

    @Modifying
    @Query(value = "INSERT IGNORE INTO invoice_sync_tracker (date_prefix, last_processed_invoice_id, updated_at) VALUES (:datePrefix, :maxId, NOW())", nativeQuery = true)
    void insertIfNotExists(@Param("datePrefix") String datePrefix, @Param("maxId") Integer maxId);
}
