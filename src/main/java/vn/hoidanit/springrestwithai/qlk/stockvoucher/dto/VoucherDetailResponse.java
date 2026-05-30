package vn.hoidanit.springrestwithai.qlk.stockvoucher.dto;

import java.math.BigDecimal;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.VoucherDetail;
import vn.hoidanit.springrestwithai.qlk.material.dto.MaterialResponse;

public record VoucherDetailResponse(
    Long id,
    MaterialResponse material,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal amount,
    String note
) {
    public static VoucherDetailResponse from(VoucherDetail detail) {
        if (detail == null) return null;
        return new VoucherDetailResponse(
            detail.getId(),
            detail.getMaterial() != null ? MaterialResponse.from(detail.getMaterial()) : null,
            detail.getQuantity(),
            detail.getUnitPrice(),
            detail.getAmount(),
            detail.getNote()
        );
    }
}
