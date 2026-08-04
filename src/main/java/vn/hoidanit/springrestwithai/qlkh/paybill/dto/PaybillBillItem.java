package vn.hoidanit.springrestwithai.qlkh.paybill.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaybillBillItem {

    @JsonProperty("BillId")
    private String billId;

    @JsonProperty("Period")
    private String period;

    @JsonProperty("Money")
    private long money;

    @JsonProperty("Note")
    private String note;

    @JsonProperty("InfoEx")
    private String infoEx;

    public PaybillBillItem() {}

    public PaybillBillItem(String billId, String period, long money) {
        this.billId = billId;
        this.period = period;
        this.money = money;
    }

    public String getBillId() { return billId; }
    public void setBillId(String billId) { this.billId = billId; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public long getMoney() { return money; }
    public void setMoney(long money) { this.money = money; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getInfoEx() { return infoEx; }
    public void setInfoEx(String infoEx) { this.infoEx = infoEx; }
}
