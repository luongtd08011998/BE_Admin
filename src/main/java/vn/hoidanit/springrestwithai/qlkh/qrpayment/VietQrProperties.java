package vn.hoidanit.springrestwithai.qlkh.qrpayment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vietqr")
public class VietQrProperties {
    private String baseUrl = "https://img.vietqr.io/image";
    private String bankId = "970415";
    private String accountNo = "113601145666";
    private String accountName = "CONG TY TNHH CAP NUOC TOC TIEN";
    private String template = "tY8MBU9";
    private String addInfoPrefix = "TOCTIEN";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getAddInfoPrefix() {
        return addInfoPrefix;
    }

    public void setAddInfoPrefix(String addInfoPrefix) {
        this.addInfoPrefix = addInfoPrefix;
    }
}
