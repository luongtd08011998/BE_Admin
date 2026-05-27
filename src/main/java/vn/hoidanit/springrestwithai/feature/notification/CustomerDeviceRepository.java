package vn.hoidanit.springrestwithai.feature.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.feature.notification.entity.CustomerDevice;

import java.util.List;

@Repository
public interface CustomerDeviceRepository extends JpaRepository<CustomerDevice, Long> {

    List<CustomerDevice> findByCustomerId(Integer customerId);

    boolean existsByCustomerIdAndDeviceToken(Integer customerId, String deviceToken);

    @Modifying
    @Query("DELETE FROM CustomerDevice cd WHERE cd.customerId = :customerId AND cd.deviceToken = :deviceToken")
    long deleteByCustomerIdAndDeviceToken(@Param("customerId") Integer customerId, @Param("deviceToken") String deviceToken);

    @Modifying
    @Query("DELETE FROM CustomerDevice cd WHERE cd.deviceToken IN :tokens")
    int deleteByDeviceTokenIn(@Param("tokens") List<String> deviceTokens);

    @Query("SELECT DISTINCT cd.customerId FROM CustomerDevice cd")
    List<Integer> findAllRegisteredCustomerIds();

    @Query("SELECT cd.customerId, cd.platform FROM CustomerDevice cd WHERE cd.customerId IN :customerIds")
    List<Object[]> findPlatformsByCustomerIds(@Param("customerIds") List<Integer> customerIds);

    @Query("SELECT cd.customerId, COUNT(cd), MAX(cd.createdAt) FROM CustomerDevice cd WHERE cd.customerId IN :customerIds GROUP BY cd.customerId")
    List<Object[]> findDeviceStatsByCustomerIds(@Param("customerIds") List<Integer> customerIds);
}
