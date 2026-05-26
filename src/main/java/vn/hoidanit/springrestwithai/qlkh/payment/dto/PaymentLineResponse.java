package vn.hoidanit.springrestwithai.qlkh.payment.dto;

import vn.hoidanit.springrestwithai.qlkh.payment.entity.PaymentLine;
import java.time.LocalDateTime;

/**
 * Record DTO chứa thông tin chi tiết của dòng thanh toán (PaymentLine).
 */
public record PaymentLineResponse(
        Integer paymentLineId,
        String paymentId,
        String yearMonth,
        String paidDate,
        Double amount,
        String remark,
        String paymentNum,
        String referenceNumber,
        Integer customerId,
        String roadId,
        Integer status,
        Integer source,
        LocalDateTime smsDate,
        String bankId
) {
    public static PaymentLineResponse fromEntity(PaymentLine pl) {
        if (pl == null) return null;
        return new PaymentLineResponse(
                pl.getPaymentLineId(),
                pl.getPaymentId(),
                pl.getYearMonth(),
                pl.getPaidDate(),
                pl.getAmount(),
                pl.getRemark(),
                pl.getPaymentNum(),
                pl.getReferenceNumber(),
                pl.getCustomerId(),
                pl.getRoadId(),
                pl.getStatus(),
                pl.getSource(),
                pl.getSmsDate(),
                pl.getBankId()
        );
    }
}
