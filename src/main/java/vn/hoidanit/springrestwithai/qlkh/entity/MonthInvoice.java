package vn.hoidanit.springrestwithai.qlkh.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "monthinvoice")
public class MonthInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MonthInvoiceId")
    private Integer monthInvoiceId;

    @Column(name = "CustomerId")
    private Integer customerId;

    @Column(name = "YearMonth")
    private String yearMonth;

    @Column(name = "Amount")
    private Double amount;

    @Column(name = "EnvFee")
    private Double envFee;

    @Column(name = "TaxFee")
    private Double taxFee;

    @Column(name = "InvStatus")
    private Integer invStatus;

    @Column(name = "PaymentStatus")
    private Integer paymentStatus;

    @Column(name = "CreatedDate")
    private String createdDate;

    @Column(name = "OldVal")
    private Integer oldVal;

    @Column(name = "NewVal")
    private Integer newVal;

    @Column(name = "WaterMeterSerial")
    private String waterMeterSerial;

    @Column(name = "StubNum")
    private String stubNum;

    @Column(name = "NumOfHouseHold")
    private Integer numOfHouseHold;

    @Column(name = "StartDate")
    private String startDate;

    @Column(name = "EndDate")
    private String endDate;

    @Column(name = "Status")
    private Integer status;

    public Integer getMonthInvoiceId() { return monthInvoiceId; }
    public void setMonthInvoiceId(Integer monthInvoiceId) { this.monthInvoiceId = monthInvoiceId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Double getEnvFee() { return envFee; }
    public void setEnvFee(Double envFee) { this.envFee = envFee; }

    public Double getTaxFee() { return taxFee; }
    public void setTaxFee(Double taxFee) { this.taxFee = taxFee; }

    public Integer getInvStatus() { return invStatus; }
    public void setInvStatus(Integer invStatus) { this.invStatus = invStatus; }

    public Integer getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(Integer paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public Integer getOldVal() { return oldVal; }
    public void setOldVal(Integer oldVal) { this.oldVal = oldVal; }

    public Integer getNewVal() { return newVal; }
    public void setNewVal(Integer newVal) { this.newVal = newVal; }

    public String getWaterMeterSerial() { return waterMeterSerial; }
    public void setWaterMeterSerial(String waterMeterSerial) { this.waterMeterSerial = waterMeterSerial; }

    public String getStubNum() { return stubNum; }
    public void setStubNum(String stubNum) { this.stubNum = stubNum; }

    public Integer getNumOfHouseHold() { return numOfHouseHold; }
    public void setNumOfHouseHold(Integer numOfHouseHold) { this.numOfHouseHold = numOfHouseHold; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
