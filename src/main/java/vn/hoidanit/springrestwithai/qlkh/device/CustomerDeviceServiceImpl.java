package vn.hoidanit.springrestwithai.qlkh.device;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import vn.hoidanit.springrestwithai.qlkh.customer.CustomerRepository;
import vn.hoidanit.springrestwithai.qlkh.device.dto.DeviceRegisterRequest;
import vn.hoidanit.springrestwithai.qlkh.device.dto.DeviceUnregisterRequest;
import vn.hoidanit.springrestwithai.qlkh.notification.NotificationService;

@Service
public class CustomerDeviceServiceImpl implements CustomerDeviceService {

    private final NotificationService notificationService;
    private final JwtDecoder jwtDecoder;
    private final CustomerRepository customerRepository;

    public CustomerDeviceServiceImpl(NotificationService notificationService,
                                     JwtDecoder jwtDecoder,
                                     CustomerRepository customerRepository) {
        this.notificationService = notificationService;
        this.jwtDecoder = jwtDecoder;
        this.customerRepository = customerRepository;
    }

    @Override
    public void registerDevice(String authHeader, DeviceRegisterRequest request) {
        Integer customerId = extractCustomerId(authHeader);
        notificationService.registerDevice(customerId, request.getDeviceToken(), request.getPlatform());
    }

    @Override
    public void unregisterDevice(String authHeader, DeviceUnregisterRequest request) {
        Integer customerId = extractCustomerId(authHeader);
        notificationService.unregisterDevice(customerId, request.getDeviceToken());
    }

    private Integer extractCustomerId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token không hợp lệ");
        }
        var jwt = jwtDecoder.decode(authHeader.substring(7));
        String digiCode = jwt.getClaimAsString("digiCode");
        return customerRepository.findByDigiCode(digiCode)
                .orElseThrow(() -> new vn.hoidanit.springrestwithai.exception.ResourceNotFoundException(
                        "Khách hàng", "digiCode", digiCode))
                .getCustomerId();
    }
}
