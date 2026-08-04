package vn.hoidanit.springrestwithai.qlkh.paybill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaybillQrResponse {

    @JsonProperty("returncode")
    private int returnCode;

    @JsonProperty("description")
    private String description;

    @JsonProperty("QRContent")
    private String qrContent;

    @JsonProperty("QRCodeIndentify")
    private String qrIndentify;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("checksum")
    private String checksum;

    @JsonProperty("ExpiredDate")
    private String expiredDate;

    // Getters and Setters

    public int getReturnCode() { return returnCode; }
    public void setReturnCode(int returnCode) { this.returnCode = returnCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getQrContent() { return qrContent; }
    public void setQrContent(String qrContent) { this.qrContent = qrContent; }

    public String getQrIndentify() { return qrIndentify; }
    public void setQrIndentify(String qrIndentify) { this.qrIndentify = qrIndentify; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public String getExpiredDate() { return expiredDate; }
    public void setExpiredDate(String expiredDate) { this.expiredDate = expiredDate; }
}
