package vn.hoidanit.springrestwithai.qlkh.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AdminInvoiceResponse {
    private Integer id;
    private String digiCode;
    private String customerName;
    private Double totalAmount;
    private String yearMonth;
    private String invoiceNo;
    private Integer paymentStatus;
    private Boolean isReminded;
    private Boolean hasReplacement;

    @JsonIgnore
    private String fkey;

    public AdminInvoiceResponse(Integer id, String digiCode, String customerName, Double totalAmount, String yearMonth, String fkey, Integer paymentStatus, Boolean hasReplacement) {
        this.id = id;
        this.digiCode = digiCode;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.yearMonth = yearMonth;
        this.fkey = fkey;
        this.paymentStatus = paymentStatus;
        this.invoiceNo = ""; // will be populated later
        this.isReminded = false;
        this.hasReplacement = hasReplacement;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getFkey() {
        return fkey;
    }

    public void setFkey(String fkey) {
        this.fkey = fkey;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Boolean getIsReminded() {
        return isReminded;
    }

    public void setIsReminded(Boolean isReminded) {
        this.isReminded = isReminded;
    }

    public Boolean getHasReplacement() {
        return hasReplacement;
    }

    public void setHasReplacement(Boolean hasReplacement) {
        this.hasReplacement = hasReplacement;
    }
}
