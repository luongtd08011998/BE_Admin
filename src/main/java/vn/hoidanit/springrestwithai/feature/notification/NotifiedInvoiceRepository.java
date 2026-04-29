package vn.hoidanit.springrestwithai.feature.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.feature.notification.entity.NotifiedInvoice;

@Repository
public interface NotifiedInvoiceRepository extends JpaRepository<NotifiedInvoice, Long> {

    /** Kiểm tra hóa đơn này đã được gửi thông báo chưa */
    boolean existsByMonthInvoiceId(Integer monthInvoiceId);

    /** Lấy danh sách ID các hóa đơn đã được thông báo từ một danh sách đầu vào */
    @org.springframework.data.jpa.repository.Query("SELECT n.monthInvoiceId FROM NotifiedInvoice n WHERE n.monthInvoiceId IN :ids")
    java.util.List<Integer> findNotifiedInvoiceIds(@org.springframework.data.repository.query.Param("ids") java.util.List<Integer> ids);
}
