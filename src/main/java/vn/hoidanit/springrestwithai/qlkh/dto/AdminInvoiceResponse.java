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
    private Boolean isOverdue;
    private Boolean isWaterCutoff;
    private Boolean hasReplacement;

    private String fkey;
    private String qrUrl;
    private String blankNo;
    private Integer roadId;

    public AdminInvoiceResponse(Integer id, String digiCode, String customerName, Double totalAmount, String yearMonth, String fkey, Integer paymentStatus, Boolean hasReplacement, String blankNo, Integer roadId) {
        this.id = id;
        this.digiCode = digiCode;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.yearMonth = yearMonth;
        this.fkey = fkey;
        this.paymentStatus = paymentStatus;
        this.invoiceNo = blankNo != null ? blankNo : ""; // Dùng blankNo từ DB, không gọi VNPT real-time
        this.isReminded = false;
        this.isOverdue = false;
        this.isWaterCutoff = false;
        this.hasReplacement = hasReplacement;
        this.blankNo = blankNo;
        this.roadId = roadId;
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

    public Boolean getIsOverdue() {
        return isOverdue;
    }

    public void setIsOverdue(Boolean isOverdue) {
        this.isOverdue = isOverdue;
    }

    public Boolean getIsWaterCutoff() {
        return isWaterCutoff;
    }

    public void setIsWaterCutoff(Boolean isWaterCutoff) {
        this.isWaterCutoff = isWaterCutoff;
    }

    public Boolean getHasReplacement() {
        return hasReplacement;
    }

    public void setHasReplacement(Boolean hasReplacement) {
        this.hasReplacement = hasReplacement;
    }

    public String getQrUrl() {
        return qrUrl;
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }

    public String getBlankNo() {
        return blankNo;
    }

    public void setBlankNo(String blankNo) {
        this.blankNo = blankNo;
    }

    public Integer getRoadId() {
        return roadId;
    }

    public void setRoadId(Integer roadId) {
        this.roadId = roadId;
    }
}
