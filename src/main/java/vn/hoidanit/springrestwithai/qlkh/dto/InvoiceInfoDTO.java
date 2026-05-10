package vn.hoidanit.springrestwithai.qlkh.dto;

public class InvoiceInfoDTO {
    private Integer customerId;
    private Integer monthInvoiceId;
    private String yearMonth;
    private String digiCode;
    private String customerName;
    private Double amount;

    public InvoiceInfoDTO(Integer customerId, Integer monthInvoiceId, String yearMonth, String digiCode, String customerName, Double amount) {
        this.customerId = customerId;
        this.monthInvoiceId = monthInvoiceId;
        this.yearMonth = yearMonth;
        this.digiCode = digiCode;
        this.customerName = customerName;
        this.amount = amount;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getMonthInvoiceId() {
        return monthInvoiceId;
    }

    public void setMonthInvoiceId(Integer monthInvoiceId) {
        this.monthInvoiceId = monthInvoiceId;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public String getDigiCode() {
        return digiCode;
    }

    public void setDigiCode(String digiCode) {
        this.digiCode = digiCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
