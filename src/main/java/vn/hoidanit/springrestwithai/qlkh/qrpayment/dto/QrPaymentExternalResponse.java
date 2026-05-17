package vn.hoidanit.springrestwithai.qlkh.qrpayment.dto;

public record QrPaymentExternalResponse(
    String fkey,
    String customerCode,
    String yearMonth,
    Double totalAmount,
    String qrUrl,
    String addInfo
) {}
