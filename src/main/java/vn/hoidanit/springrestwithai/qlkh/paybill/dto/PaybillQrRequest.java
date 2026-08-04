package vn.hoidanit.springrestwithai.qlkh.paybill.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaybillQrRequest {

    @JsonProperty("BillerCode")
    private String billerCode;

    @JsonProperty("ServiceCode")
    private String serviceCode;

    @JsonProperty("Timestamp")
    private String timestamp;

    @JsonProperty("Checksum")
    private String checksum;

    @JsonProperty("CustomerCode")
    private String customerCode;

    @JsonProperty("CustomerName")
    private String customerName;

    @JsonProperty("Phone")
    private String phone;

    @JsonProperty("Address")
    private String address;

    @JsonProperty("EffectiveDate")
    private String effectiveDate;

    @JsonProperty("ExpiredDate")
    private String expiredDate;

    @JsonProperty("Bills")
    private List<PaybillBillItem> bills;

    // Getters and Setters

    public String getBillerCode() { return billerCode; }
    public void setBillerCode(String billerCode) { this.billerCode = billerCode; }

    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(String effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getExpiredDate() { return expiredDate; }
    public void setExpiredDate(String expiredDate) { this.expiredDate = expiredDate; }

    public List<PaybillBillItem> getBills() { return bills; }
    public void setBills(List<PaybillBillItem> bills) { this.bills = bills; }
}
