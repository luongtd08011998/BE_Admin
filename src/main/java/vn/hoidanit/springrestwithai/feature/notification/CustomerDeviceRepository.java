package vn.hoidanit.springrestwithai.feature.notification;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.feature.notification.entity.CustomerDevice;

import java.util.List;

@Repository
public interface CustomerDeviceRepository extends JpaRepository<CustomerDevice, Long> {

    List<CustomerDevice> findByCustomerId(Integer customerId);

    boolean existsByCustomerIdAndDeviceToken(Integer customerId, String deviceToken);

    long deleteByCustomerIdAndDeviceToken(Integer customerId, String deviceToken);

    @Modifying
    @Transactional("primaryTransactionManager")
    void deleteByDeviceTokenIn(List<String> deviceTokens);
}
