package vn.hoidanit.springrestwithai.qlkh.paybill.dto;

import java.util.List;

public class PaybillQrResult {
    private String qrContent;
    private String qrIdentify;
    private String expiredDate;
    private long totalAmount;
    private List<String> periods;

    public PaybillQrResult() {}

    public PaybillQrResult(String qrContent, String qrIdentify, String expiredDate, long totalAmount, List<String> periods) {
        this.qrContent = qrContent;
        this.qrIdentify = qrIdentify;
        this.expiredDate = expiredDate;
        this.totalAmount = totalAmount;
        this.periods = periods;
    }

    public String getQrContent() { return qrContent; }
    public void setQrContent(String qrContent) { this.qrContent = qrContent; }

    public String getQrIdentify() { return qrIdentify; }
    public void setQrIdentify(String qrIdentify) { this.qrIdentify = qrIdentify; }

    public String getExpiredDate() { return expiredDate; }
    public void setExpiredDate(String expiredDate) { this.expiredDate = expiredDate; }

    public long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(long totalAmount) { this.totalAmount = totalAmount; }

    public List<String> getPeriods() { return periods; }
    public void setPeriods(List<String> periods) { this.periods = periods; }
}
