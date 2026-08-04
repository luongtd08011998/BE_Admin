package vn.hoidanit.springrestwithai.qlkh.paybill;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "paybill")
public class PaybillProperties {
    private String baseUrl;
    private String billerCode;
    private String serviceCode;
    private String password;
    private boolean skipSslVerify;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBillerCode() {
        return billerCode;
    }

    public void setBillerCode(String billerCode) {
        this.billerCode = billerCode;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSkipSslVerify() {
        return skipSslVerify;
    }

    public void setSkipSslVerify(boolean skipSslVerify) {
        this.skipSslVerify = skipSslVerify;
    }
}
