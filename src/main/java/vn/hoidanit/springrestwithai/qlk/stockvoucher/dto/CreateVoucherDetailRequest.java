package vn.hoidanit.springrestwithai.qlk.stockvoucher.dto;

import java.math.BigDecimal;

public record CreateVoucherDetailRequest(
    Long materialId,
    Integer quantity,
    BigDecimal unitPrice,
    String note
) {}
