package vn.hoidanit.springrestwithai.feature.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.feature.notification.entity.NotifiedPayment;

import java.util.List;

@Repository
public interface NotifiedPaymentRepository extends JpaRepository<NotifiedPayment, Long> {

    /** Kiểm tra hóa đơn này đã được gửi thông báo thanh toán chưa */
    boolean existsByMonthInvoiceId(Integer monthInvoiceId);

    /** Lấy tập hợp ID các hóa đơn đã gửi thông báo thanh toán (tối ưu N+1) */
    @Query("SELECT n.monthInvoiceId FROM NotifiedPayment n WHERE n.monthInvoiceId IN :ids")
    List<Integer> findNotifiedPaymentIds(@Param("ids") List<Integer> ids);

    /**
     * Lấy toàn bộ monthInvoiceId đã gửi thông báo thanh toán trong khoảng yearMonth gần đây.
     * Dùng để loại trừ trước khi query DB qlkh → giảm lượng data load mỗi lần cron chạy.
     */
    @Query("SELECT n.monthInvoiceId FROM NotifiedPayment n WHERE n.yearMonth >= :fromYearMonth")
    List<Integer> findNotifiedMonthInvoiceIdsSince(@Param("fromYearMonth") String fromYearMonth);
}
