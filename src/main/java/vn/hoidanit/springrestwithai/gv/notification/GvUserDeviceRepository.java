package vn.hoidanit.springrestwithai.gv.notification;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GvUserDeviceRepository extends JpaRepository<GvUserDevice, Long> {
    List<GvUserDevice> findByUserId(Long userId);
    Optional<GvUserDevice> findByUserIdAndDeviceToken(Long userId, String deviceToken);
    void deleteByUserIdAndDeviceToken(Long userId, String deviceToken);
    void deleteByDeviceToken(String deviceToken);
}
