package vn.hoidanit.springrestwithai.qlkh.dto;

/**
 * Chỉ số đồng hồ (legacy OldVal/NewVal) theo khách — mã KH (DigiCode) từ bảng customer.
 */
public record MonthInvoiceReadingItemResponse(String digiCode, Integer oldVal, Integer newVal) {
}
