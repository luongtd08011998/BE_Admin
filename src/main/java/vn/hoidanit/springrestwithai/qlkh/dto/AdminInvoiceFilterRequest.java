package vn.hoidanit.springrestwithai.qlkh.dto;

public class AdminInvoiceFilterRequest {
    private String yearMonth;
    private Integer paymentStatus;

    public AdminInvoiceFilterRequest() {
    }

    public AdminInvoiceFilterRequest(String yearMonth, Integer paymentStatus) {
        this.yearMonth = yearMonth;
        this.paymentStatus = paymentStatus;
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
}
