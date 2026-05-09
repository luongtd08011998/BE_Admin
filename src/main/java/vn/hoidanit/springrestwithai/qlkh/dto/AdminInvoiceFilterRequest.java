package vn.hoidanit.springrestwithai.qlkh.dto;

public class AdminInvoiceFilterRequest {
    private String yearMonth;
    private Integer paymentStatus;
    private String customerName;
    private String digiCode;

    public AdminInvoiceFilterRequest() {
    }

    public AdminInvoiceFilterRequest(String yearMonth, Integer paymentStatus, String customerName, String digiCode) {
        this.yearMonth = yearMonth;
        this.paymentStatus = paymentStatus;
        this.customerName = customerName;
        this.digiCode = digiCode;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getDigiCode() {
        return digiCode;
    }

    public void setDigiCode(String digiCode) {
        this.digiCode = digiCode;
    }
}
