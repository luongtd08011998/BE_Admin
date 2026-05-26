package vn.hoidanit.springrestwithai.qlkh.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entity ánh xạ bảng {@code paymentline} trong cơ sở dữ liệu QLKH.
 * Bảng này ở chế độ chỉ đọc (Read-only) đối với ứng dụng hiện tại.
 */
@Entity
@Table(name = "paymentline")
public class PaymentLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentLineId")
    private Integer paymentLineId;

    @Column(name = "PaymentId", columnDefinition = "mediumtext")
    private String paymentId;

    @Column(name = "YearMonth", length = 8)
    private String yearMonth;

    @Column(name = "PaidDate", length = 14)
    private String paidDate;

    @Column(name = "Amount")
    private Double amount;

    @Column(name = "Remark", length = 100)
    private String remark;

    @Column(name = "ModifiedById")
    private Integer modifiedById;

    @Column(name = "ModifiedDate")
    private LocalDateTime modifiedDate;

    @Column(name = "EmployeeId")
    private Integer employeeId;

    @Column(name = "PaymentNum", length = 20)
    private String paymentNum;

    @Column(name = "ReferenceNumber", length = 50)
    private String referenceNumber;

    @Column(name = "CustomerId")
    private Integer customerId;

    @Column(name = "RoadId", columnDefinition = "mediumtext")
    private String roadId;

    @Column(name = "Status")
    private Integer status;

    @Column(name = "Source")
    private Integer source;

    @Column(name = "SmsDate")
    private LocalDateTime smsDate;

    @Column(name = "BankId", columnDefinition = "mediumtext")
    private String bankId;

    // --- Getters & Setters ---

    public Integer getPaymentLineId() {
        return paymentLineId;
    }

    public void setPaymentLineId(Integer paymentLineId) {
        this.paymentLineId = paymentLineId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public String getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(String paidDate) {
        this.paidDate = paidDate;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getModifiedById() {
        return modifiedById;
    }

    public void setModifiedById(Integer modifiedById) {
        this.modifiedById = modifiedById;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getPaymentNum() {
        return paymentNum;
    }

    public void setPaymentNum(String paymentNum) {
        this.paymentNum = paymentNum;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getRoadId() {
        return roadId;
    }

    public void setRoadId(String roadId) {
        this.roadId = roadId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public LocalDateTime getSmsDate() {
        return smsDate;
    }

    public void setSmsDate(LocalDateTime smsDate) {
        this.smsDate = smsDate;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }
}
