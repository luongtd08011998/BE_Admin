package vn.hoidanit.springrestwithai.qlkh.dto;

import jakarta.validation.constraints.NotBlank;

public class DeviceUnregisterRequest {

    @NotBlank(message = "deviceToken không được để trống")
    private String deviceToken;

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}

