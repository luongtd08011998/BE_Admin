package vn.hoidanit.springrestwithai.feature.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.feature.notification.entity.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

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
}
