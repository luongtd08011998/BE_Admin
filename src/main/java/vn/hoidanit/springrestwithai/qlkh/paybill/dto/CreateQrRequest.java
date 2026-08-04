package vn.hoidanit.springrestwithai.qlkh.paybill.dto;

import java.util.List;

public class CreateQrRequest {
    private Integer customerId;
    private List<Integer> monthInvoiceIds;

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public List<Integer> getMonthInvoiceIds() { return monthInvoiceIds; }
    public void setMonthInvoiceIds(List<Integer> monthInvoiceIds) { this.monthInvoiceIds = monthInvoiceIds; }
}
