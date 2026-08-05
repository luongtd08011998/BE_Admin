package vn.hoidanit.springrestwithai.qlkh.paybill.dto;

import java.util.List;

public class CreateQrRequest {
    private String digiCode;
    private List<Integer> monthInvoiceIds;

    public String getDigiCode() { return digiCode; }
    public void setDigiCode(String digiCode) { this.digiCode = digiCode; }

    public List<Integer> getMonthInvoiceIds() { return monthInvoiceIds; }
    public void setMonthInvoiceIds(List<Integer> monthInvoiceIds) { this.monthInvoiceIds = monthInvoiceIds; }
}
