package vn.hoidanit.springrestwithai.qlkh.qrpayment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QrGenerateRequest(
    @NotBlank(message = "Mã khách hàng không được để trống") String customerCode,
    @NotBlank(message = "Kỳ hóa đơn không được để trống") String yearMonth,
    @NotNull(message = "Số tiền không được để trống") Double amount,
    Double envFee,
    Double taxFee,
    String fkey
) {}
