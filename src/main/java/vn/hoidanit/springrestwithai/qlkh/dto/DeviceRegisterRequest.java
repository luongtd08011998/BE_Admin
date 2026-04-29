package vn.hoidanit.springrestwithai.qlkh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class DeviceRegisterRequest {

    @NotBlank(message = "deviceToken không được để trống")
    private String deviceToken;

    @Pattern(regexp = "ANDROID|IOS", message = "platform phải là ANDROID hoặc IOS")
    private String platform;

    public String getDeviceToken() { return deviceToken; }

    public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }

    public String getPlatform() { return platform; }

    public void setPlatform(String platform) { this.platform = platform; }
}
