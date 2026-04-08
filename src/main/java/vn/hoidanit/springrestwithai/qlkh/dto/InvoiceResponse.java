package vn.hoidanit.springrestwithai.qlkh.dto;

/**
 * Hóa đơn theo tháng + thông tin hiển thị từ khách hàng (DigiCode, Name).
 */
public record InvoiceResponse(
        Integer id,
        String digiCode,
        String customerName,
        Double amount,
        Double envFee,
        Double taxFee,
        Double totalAmount,
        Integer paymentStatus,
        String paymentStatusLabel,
        Integer oldVal,
        Integer newVal) {
}
