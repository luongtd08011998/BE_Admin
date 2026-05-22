package vn.hoidanit.springrestwithai.qlkh.invoice.dto;

public record InvoiceItem(
        String no,
        String name,
        String unit,
        String quantity,
        String unitPrice,
        String amount,
        String taxRate,
        String taxAmount
) {}
