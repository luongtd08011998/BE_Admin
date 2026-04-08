package vn.hoidanit.springrestwithai.qlkh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Legacy bảng {@code monthinvoice} — chỉ map các cột dùng cho danh sách / trạng thái thanh toán
 * (theo docs/decisions/databaseqlkh.md).
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
}
