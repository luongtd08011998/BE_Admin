package vn.hoidanit.springrestwithai.qlkh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Legacy bảng {@code monthinvoice} — map các cột dùng cho danh sách / trạng thái thanh toán
 * và khóa tích hợp hóa đơn điện tử ({@code RootKey}, {@code Fkey}).
 */
@Entity
@Table(name = "monthinvoice")
public class MonthInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MonthInvoiceId")
    private Integer monthInvoiceId;

    @Column(name = "CustomerId")
    private Integer customerId;

    @Column(name = "Amount")
    private Double amount;

    @Column(name = "EnvFee")
    private Double envFee;

    @Column(name = "TaxFee")
    private Double taxFee;

    @Column(name = "PaymentStatus")
    private Integer paymentStatus;

    @Column(name = "OldVal")
    private Integer oldVal;

    @Column(name = "NewVal")
    private Integer newVal;

    @Column(name = "YearMonth")
    private String yearMonth;

    @Column(name = "CreatedDate")
    private String createdDate;

    @Column(name = "NumOfHouseHold")
    private Integer numOfHouseHold;

    @Column(name = "WaterMeterSerial")
    private String waterMeterSerial;

    @Column(name = "RootKey", length = 30)
    private String rootKey;

    @Column(name = "Fkey", length = 36)
    private String fkey;

    public Integer getMonthInvoiceId() {
        return monthInvoiceId;
    }

    public void setMonthInvoiceId(Integer monthInvoiceId) {
        this.monthInvoiceId = monthInvoiceId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getEnvFee() {
        return envFee;
    }

    public void setEnvFee(Double envFee) {
        this.envFee = envFee;
    }

    public Double getTaxFee() {
        return taxFee;
    }

    public void setTaxFee(Double taxFee) {
        this.taxFee = taxFee;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Integer getOldVal() {
        return oldVal;
    }

    public void setOldVal(Integer oldVal) {
        this.oldVal = oldVal;
    }

    public Integer getNewVal() {
        return newVal;
    }

    public void setNewVal(Integer newVal) {
        this.newVal = newVal;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getNumOfHouseHold() {
        return numOfHouseHold;
    }

    public void setNumOfHouseHold(Integer numOfHouseHold) {
        this.numOfHouseHold = numOfHouseHold;
    }

    public String getWaterMeterSerial() {
        return waterMeterSerial;
    }

    public void setWaterMeterSerial(String waterMeterSerial) {
        this.waterMeterSerial = waterMeterSerial;
    }

    public String getRootKey() {
        return rootKey;
    }

    public void setRootKey(String rootKey) {
        this.rootKey = rootKey;
    }

    public String getFkey() {
        return fkey;
    }

    public void setFkey(String fkey) {
        this.fkey = fkey;
    }
}
