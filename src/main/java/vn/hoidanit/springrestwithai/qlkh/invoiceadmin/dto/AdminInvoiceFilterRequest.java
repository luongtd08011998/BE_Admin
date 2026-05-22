package vn.hoidanit.springrestwithai.qlkh.invoiceadmin.dto;

public class AdminInvoiceFilterRequest {
    private String yearMonth;
    private Integer paymentStatus;
    private String customerName;
    private String digiCode;
    private Integer remindStatus;
    private Integer roadId;

    public AdminInvoiceFilterRequest() {
    }

    public AdminInvoiceFilterRequest(String yearMonth, Integer paymentStatus, String customerName, String digiCode, Integer remindStatus) {
        this.yearMonth = yearMonth;
        this.paymentStatus = paymentStatus;
        this.customerName = customerName;
        this.digiCode = digiCode;
        this.remindStatus = remindStatus;
        this.roadId = null; // optional constructor arg or let default to null
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

    public Integer getRemindStatus() {
        return remindStatus;
    }

    public void setRemindStatus(Integer remindStatus) {
        this.remindStatus = remindStatus;
    }

    public Integer getRoadId() {
        return roadId;
    }

    public void setRoadId(Integer roadId) {
        this.roadId = roadId;
    }
}
