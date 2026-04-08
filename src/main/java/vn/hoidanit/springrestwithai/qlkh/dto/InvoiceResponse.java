package vn.hoidanit.springrestwithai.qlkh.dto;

public record InvoiceResponse(
        Integer monthInvoiceId,
        Integer customerId,
        String yearMonth,
        Double amount,
        Double envFee,
        Double taxFee,
        Integer invStatus,
        Integer paymentStatus,
        String paymentStatusLabel,
        String createdDate,
        String startDate,
        String endDate,
        Integer oldVal,
        Integer newVal,
        String waterMeterSerial,
        Integer numOfHouseHold
) {}
