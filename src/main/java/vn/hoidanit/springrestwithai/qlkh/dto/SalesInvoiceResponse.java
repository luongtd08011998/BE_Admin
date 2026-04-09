package vn.hoidanit.springrestwithai.qlkh.dto;

/**
 * Hóa đơn bán (salesinvoice) + thông tin hiển thị từ khách hàng (DigiCode, Name).
 */
public record SalesInvoiceResponse(
        Integer salesInvoiceId,
        String invoiceNum,
        String invoiceDate,
        String templateCode,
        String digiCode,
        String customerName,
        String address,
        Double invoiceTotal,
        Integer status) {
}
