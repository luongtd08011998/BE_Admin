package vn.hoidanit.springrestwithai.feature.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.feature.notification.entity.Notification;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

    List<Notification> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    List<Notification> findByCustomerIdAndTypeOrderByCreatedAtDesc(Integer customerId, String type);

    List<Notification> findByCustomerIdAndTypeNotOrderByCreatedAtDesc(Integer customerId, String type);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.customerId = :customerId AND n.isRead = false")
    long countUnreadByCustomerId(@Param("customerId") Integer customerId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.customerId = :customerId AND n.isRead = false AND n.type = :type")
    long countUnreadByCustomerIdAndType(@Param("customerId") Integer customerId, @Param("type") String type);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.customerId = :customerId AND n.isRead = false AND n.type != :excludeType")
    long countUnreadByCustomerIdAndTypeNot(@Param("customerId") Integer customerId, @Param("excludeType") String excludeType);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.customerId = :customerId AND n.id IN :ids")
    int markAsRead(@Param("customerId") Integer customerId, @Param("ids") List<Long> ids);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.customerId = :customerId")
    int markAllAsRead(@Param("customerId") Integer customerId);

    @Query("SELECT n FROM Notification n WHERE n.type IN :types AND n.referenceId IS NULL")
    List<Notification> findByTypeInAndReferenceIdNull(@Param("types") List<String> types);

    @Query("SELECT n FROM Notification n WHERE n.type IN :types")
    List<Notification> findByTypeIn(@Param("types") List<String> types);

    @Query("SELECT DISTINCT n.referenceId FROM Notification n WHERE n.type = 'DEBT_REMINDER' AND n.referenceId IS NOT NULL")
    List<Long> findAllRemindedInvoiceIds();

    @Query("SELECT DISTINCT n.referenceId FROM Notification n WHERE n.type = 'OVERDUE' AND n.referenceId IS NOT NULL")
    List<Long> findAllOverdueInvoiceIds();

    @Query("SELECT DISTINCT n.referenceId FROM Notification n WHERE n.type = 'WATER_CUTOFF' AND n.referenceId IS NOT NULL")
    List<Long> findAllCutwaterInvoiceIds();

    // --- Admin monitoring queries ---

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.deliveryStatus = :status, n.deliveredAt = :deliveredAt, n.failureReason = :reason WHERE n.id = :id")
    int updateDeliveryStatus(@Param("id") Long id, @Param("status") vn.hoidanit.springrestwithai.feature.notification.entity.DeliveryStatus status,
                             @Param("deliveredAt") LocalDateTime deliveredAt, @Param("reason") String reason);

    @Query("SELECT n.type, COUNT(n) FROM Notification n GROUP BY n.type")
    List<Object[]> countGroupByType();

    @Query("SELECT n.deliveryStatus, COUNT(n) FROM Notification n GROUP BY n.deliveryStatus")
    List<Object[]> countGroupByDeliveryStatus();

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
