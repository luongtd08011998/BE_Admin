package vn.hoidanit.springrestwithai.gv.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GvDeviceTokenRequest {
    @NotBlank(message = "deviceToken không được để trống")
    private String deviceToken;
    private String platform;
}
