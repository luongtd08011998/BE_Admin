package vn.hoidanit.springrestwithai.qlkh.device;

import vn.hoidanit.springrestwithai.qlkh.device.dto.DeviceRegisterRequest;
import vn.hoidanit.springrestwithai.qlkh.device.dto.DeviceUnregisterRequest;

public interface CustomerDeviceService {
    void registerDevice(String authHeader, DeviceRegisterRequest request);
    void unregisterDevice(String authHeader, DeviceUnregisterRequest request);
}
