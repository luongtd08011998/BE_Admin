package vn.hoidanit.springrestwithai.feature.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface CustomerRefreshTokenRepository extends JpaRepository<CustomerRefreshToken, Long> {

    Optional<CustomerRefreshToken> findByToken(String token);

    /** Xóa 1 token cụ thể (logout thiết bị hiện tại — dành cho tương lai). */
    void deleteByToken(String token);

    /** Xóa tất cả token của 1 khách hàng (logout trên mọi thiết bị). */
    void deleteByCustomerId(Integer customerId);

    /** Dọn các token đã hết hạn (dùng cho cleanup định kỳ). */
    void deleteByExpiresAtBefore(Instant now);
}
